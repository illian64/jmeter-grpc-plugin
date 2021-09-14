package com.podoinikovi.jmeter.grpc.client.grpc;

import com.google.common.net.HostAndPort;
import com.podoinikovi.jmeter.grpc.exception.GrpcPluginException;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NegotiationType;
import io.grpc.netty.NettyChannelBuilder;
import io.netty.handler.ssl.ApplicationProtocolConfig;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

import javax.net.ssl.SSLException;

public class ChannelFactory {
    public ManagedChannel createChannel(HostAndPort endpoint, boolean useTls, boolean useInsecureTls) {
        ManagedChannelBuilder<NettyChannelBuilder> managedChannelBuilder =
                createChannelBuilder(endpoint, useTls, useInsecureTls);

        return managedChannelBuilder.build();
    }

    private NettyChannelBuilder createChannelBuilder(HostAndPort endpoint, boolean useTls, boolean useInsecureTls) {
        if (!useTls) {
            return NettyChannelBuilder.forAddress(endpoint.getHost(), endpoint.getPort())
                    .negotiationType(NegotiationType.PLAINTEXT);
        } else {
            return NettyChannelBuilder.forAddress(endpoint.getHost(), endpoint.getPort())
                    .sslContext(createSslContext(useInsecureTls))
                    .negotiationType(NegotiationType.TLS);
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
}
