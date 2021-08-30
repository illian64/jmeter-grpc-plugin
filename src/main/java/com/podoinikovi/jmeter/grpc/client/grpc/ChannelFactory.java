package com.podoinikovi.jmeter.grpc.client.grpc;

import com.google.common.net.HostAndPort;
import com.podoinikovi.jmeter.grpc.exception.GrpcPluginException;
import io.grpc.*;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NegotiationType;
import io.grpc.netty.NettyChannelBuilder;
import io.netty.handler.ssl.ApplicationProtocolConfig;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

import javax.net.ssl.SSLException;
import java.util.Map;

public class ChannelFactory {
    public ManagedChannel createChannel(HostAndPort endpoint, boolean useTls, boolean useInsecureTls,
                                        Map<String, String> metadata) {
        ManagedChannelBuilder<NettyChannelBuilder> managedChannelBuilder =
                createChannelBuilder(endpoint, useTls, useInsecureTls, metadata);

        return managedChannelBuilder.build();
    }

    private NettyChannelBuilder createChannelBuilder(HostAndPort endpoint, boolean useTls, boolean useInsecureTls,
                                                     Map<String, String> metadata) {
        if (!useTls) {
            return NettyChannelBuilder.forAddress(endpoint.getHost(), endpoint.getPort())
                    .negotiationType(NegotiationType.PLAINTEXT)
                    .intercept(metadataInterceptor(metadata)/*, captureMetadataInterceptor(metadataCapture)*/);
        } else {
            return NettyChannelBuilder.forAddress(endpoint.getHost(), endpoint.getPort())
                    .sslContext(createSslContext(useInsecureTls))
                    .negotiationType(NegotiationType.TLS)
                    .intercept(metadataInterceptor(metadata)/*, captureMetadataInterceptor(metadataCapture)*/);
        }
    }

    private SslContext createSslContext(boolean useInsecureTls) {
        try {
            SslContextBuilder grpcSslContexts = GrpcSslContexts.forClient();
            if (useInsecureTls) {
                grpcSslContexts.trustManager(InsecureTrustManagerFactory.INSTANCE);
            }
            return grpcSslContexts
                    .applicationProtocolConfig(
                            new ApplicationProtocolConfig(ApplicationProtocolConfig.Protocol.NPN_AND_ALPN,
                                    ApplicationProtocolConfig.SelectorFailureBehavior.NO_ADVERTISE,
                                    ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT,
                                    ApplicationProtocolNames.HTTP_2))
                    .build();
        } catch (SSLException e) {
            throw new GrpcPluginException("Error in create SSL connection!", e);
        }
    }

    private ClientInterceptor metadataInterceptor(Map<String, String> metadataHash) {
        return new ClientInterceptor() {
            @Override
            public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
                    final io.grpc.MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, final Channel next) {
                return new ClientInterceptors.CheckedForwardingClientCall<ReqT, RespT>(next.newCall(method, callOptions)) {
                    @Override
                    protected void checkedStart(Listener<RespT> responseListener, Metadata headers) {
                        for (Map.Entry<String, String> entry : metadataHash.entrySet()) {
                            Metadata.Key<String> key = Metadata.Key.of(entry.getKey(), Metadata.ASCII_STRING_MARSHALLER);
                            headers.put(key, entry.getValue());
                        }
                        delegate().start(responseListener, headers);
                    }
                };
            }
        };
    }
}
