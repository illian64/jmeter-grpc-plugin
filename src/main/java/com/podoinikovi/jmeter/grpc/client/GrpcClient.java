package com.podoinikovi.jmeter.grpc.client;

import com.google.common.collect.ImmutableList;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.util.JsonFormat;
import com.podoinikovi.jmeter.grpc.client.grpc.ChannelFactory;
import com.podoinikovi.jmeter.grpc.client.grpc.DynamicGrpcClient;
import com.podoinikovi.jmeter.grpc.client.io.MessageReader;
import com.podoinikovi.jmeter.grpc.client.io.MessageWriter;
import com.podoinikovi.jmeter.grpc.client.io.output.Output;
import com.podoinikovi.jmeter.grpc.client.io.output.OutputFixedMessagesCountImpl;
import com.podoinikovi.jmeter.grpc.client.io.output.OutputImpl;
import com.podoinikovi.jmeter.grpc.client.protobuf.ProtoMethodName;
import com.podoinikovi.jmeter.grpc.client.protobuf.ProtocInvoker;
import com.podoinikovi.jmeter.grpc.client.protobuf.ServiceResolver;
import com.podoinikovi.jmeter.grpc.exception.GrpcPluginException;
import com.podoinikovi.jmeter.grpc.exception.GrpcPluginParseMessageException;
import com.podoinikovi.jmeter.grpc.exception.GrpcPluginSystemException;
import io.grpc.*;
import io.grpc.stub.MetadataUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
public class GrpcClient {
    private static final Map<String, DescriptorProtos.FileDescriptorSet> serviceResolverCache = new ConcurrentHashMap<>();

    private final JsonFormat.TypeRegistry registry;
    private final ManagedChannel channel;
    private final ServiceResolver serviceResolver;

    public GrpcClient(ConnectionParams connectionParams) {
        try {
            final ChannelFactory channelFactory = new ChannelFactory();

            final DescriptorProtos.FileDescriptorSet fileDescriptorSet =
                    getServiceResolver(connectionParams.getProtoDiscoveryRoot(), connectionParams.getIncludePathsList());

            this.serviceResolver = ServiceResolver.fromFileDescriptorSet(fileDescriptorSet);

            this.registry = JsonFormat.TypeRegistry.newBuilder().add(serviceResolver.listMessageTypes()).build();
            this.channel = channelFactory.createChannel(connectionParams.getHostAndPort(), connectionParams.isUseTls(),
                    connectionParams.isUseInsecureTls());
        } catch (Exception e) {
            shutdownChannel();
            throw new GrpcPluginException(e);
        }
    }

    public boolean isShutdown() {
        return channel.isShutdown();
    }

    public boolean isTerminated() {
        return channel.isTerminated();
    }

    public Output call(String fullMethodName, String jsonData, CallParams callParams) {
        Output output = getOutput(callParams);
        try {
            ProtoMethodName grpcMethodName = ProtoMethodName.parseFullGrpcMethodName(fullMethodName);
            Descriptors.MethodDescriptor methodDescriptor = serviceResolver.resolveServiceMethod(grpcMethodName);

            DynamicGrpcClient dynamicClient = DynamicGrpcClient.create(
                    methodDescriptor, wrapChannelToMetadataInterceptor(output.getMetadataCapture(), callParams));
            ImmutableList<DynamicMessage> requestMessages = new MessageReader(methodDescriptor.getInputType(), registry, jsonData).read();

            MessageWriter<DynamicMessage> streamObserver = MessageWriter.create(output, registry);

            dynamicClient.call(requestMessages, streamObserver, callParams).get();

            return output;
        } catch (GrpcPluginParseMessageException e) {
            shutdownChannel();
            throw new GrpcPluginException("Caught exception while parsing request for rpc", e);
        } catch (Exception e) {
            shutdownChannel();
            if (e.getCause() instanceof StatusRuntimeException) {
                throw new GrpcPluginSystemException(e.getCause());
            }
            throw new GrpcPluginException("Caught exception while waiting for rpc", e);
        }
    }

    protected Output getOutput(CallParams callParams) {
        if (callParams.getStreamMessageLimit() > 0) {
            return new OutputFixedMessagesCountImpl(callParams.getStreamMessageLimit());
        } else {
            return new OutputImpl();
        }
    }

    protected Channel wrapChannelToMetadataInterceptor(MetadataCapture metadataCapture, CallParams callParams) {
        ClientInterceptor respHeadersInterceptor = MetadataUtils.newCaptureMetadataInterceptor(
                metadataCapture.getHeadersCapture(), metadataCapture.getTrailersCapture());
        ClientInterceptor reqHeadersInterceptor = MetadataUtils.newAttachHeadersInterceptor(callParams.getMetadataMap());

        return ClientInterceptors.intercept(channel, respHeadersInterceptor, reqHeadersInterceptor);
    }

    public void shutdownChannel() {
        try {
            if (channel != null) {
                channel.shutdown();
                channel.awaitTermination(5, TimeUnit.SECONDS);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new GrpcPluginException("Caught exception while shutting down channel", e);
        }
    }

    protected static DescriptorProtos.FileDescriptorSet getServiceResolver(String protoDiscoveryRoot, List<String> includePathsList) throws ProtocInvoker.ProtocInvocationException {
        DescriptorProtos.FileDescriptorSet fileDescriptorSet = serviceResolverCache.get(protoDiscoveryRoot);
        if (fileDescriptorSet == null) {
            synchronized (GrpcClient.class) {
                fileDescriptorSet = serviceResolverCache.get(protoDiscoveryRoot);
                if (fileDescriptorSet == null) {
                    fileDescriptorSet = ProtocInvoker.forConfig(protoDiscoveryRoot, includePathsList).invoke();
                    serviceResolverCache.put(protoDiscoveryRoot, fileDescriptorSet);
                }
            }
        }

        return fileDescriptorSet;
    }
}
