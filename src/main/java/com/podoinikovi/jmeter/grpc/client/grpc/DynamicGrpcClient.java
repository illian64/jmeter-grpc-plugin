package com.podoinikovi.jmeter.grpc.client.grpc;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.Descriptors.MethodDescriptor;
import com.google.protobuf.DynamicMessage;
import com.podoinikovi.jmeter.grpc.client.CallParams;
import com.podoinikovi.jmeter.grpc.client.io.MessageWriter;
import com.podoinikovi.jmeter.grpc.client.protobuf.DynamicMessageMarshaller;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.MethodDescriptor.MethodType;
import io.grpc.stub.ClientCalls;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** A grpc client which operates on dynamic messages. */
public class DynamicGrpcClient {
    private static final Logger logger = LoggerFactory.getLogger(DynamicGrpcClient.class);
    private final MethodDescriptor protoMethodDescriptor;
    private final Channel channel;

    /** Creates a client for the supplied method, talking to the supplied endpoint. */
    public static DynamicGrpcClient create(MethodDescriptor protoMethod, Channel channel) {
        return new DynamicGrpcClient(protoMethod, channel);
    }

    @VisibleForTesting
    DynamicGrpcClient(MethodDescriptor protoMethodDescriptor, Channel channel) {
        this.protoMethodDescriptor = protoMethodDescriptor;
        this.channel = channel;
    }

    /**
     * Makes an rpc to the remote endpoint and respects the supplied callback. Returns a future which
     * terminates once the call has ended. For calls which are single-request, this throws
     * {@link IllegalArgumentException} if the size of {@code requests} is not exactly 1.
     */
    public ListenableFuture<Void> call(
            ImmutableList<DynamicMessage> requests,
            MessageWriter<DynamicMessage> responseObserver,
            CallParams callParams) {
        Preconditions.checkArgument(!requests.isEmpty(), "Can't make call without any requests");
        MethodType methodType = getMethodType();
        long numRequests = requests.size();
        if (methodType == MethodType.UNARY) {
            logger.debug("Making unary call");
            Preconditions.checkArgument(numRequests == 1,
                    "Need exactly 1 request for unary call, but got: " + numRequests);
            return callUnary(requests.get(0), responseObserver, callParams);
        } else if (methodType == MethodType.SERVER_STREAMING) {
            logger.debug("Making server streaming call");
            Preconditions.checkArgument(numRequests == 1,
                    "Need exactly 1 request for server streaming call, but got: " + numRequests);
            return callServerStreaming(requests.get(0), responseObserver, callParams);
        } else if (methodType == MethodType.CLIENT_STREAMING) {
            logger.debug("Making client streaming call with {} requests", + requests.size());
            return callClientStreaming(requests, responseObserver, callParams);
        } else {
            logger.debug("Making bidi streaming call with {} requests", requests.size());
            return callBidiStreaming(requests, responseObserver, callParams);
        }
    }

    private ListenableFuture<Void> callBidiStreaming(
            ImmutableList<DynamicMessage> requests,
            MessageWriter<DynamicMessage> responseObserver,
            CallParams callParams) {
        DoneObserver<DynamicMessage> doneObserver = new DoneObserver<>();
        StreamObserver<DynamicMessage> requestObserver = ClientCalls.asyncBidiStreamingCall(
                createCall(callParams.callOptions()),
                CompositeStreamObserver.of(responseObserver, doneObserver));
        requests.forEach(requestObserver::onNext);
        if (callParams.getStreamStopAfterMessages() > 0) {
            if (responseObserver.getOutput().messagesCount() >= callParams.getStreamStopAfterMessages()) {
                requestObserver.onCompleted();
            }
        } else if (callParams.getStreamStopAfterTime() > 0) {
            try {
                Thread.sleep(callParams.getStreamStopAfterTime());
            } catch (InterruptedException e) {
                logger.error("Error waiting for stream completed, waiting time: {}", callParams.getStreamStopAfterTime());
                Thread.currentThread().interrupt();
            } finally {
                requestObserver.onCompleted();
            }
        } else {
            requestObserver.onCompleted();
        }

        return doneObserver.getCompletionFuture();
    }

    private ListenableFuture<Void> callClientStreaming(
            ImmutableList<DynamicMessage> requests,
            StreamObserver<DynamicMessage> responseObserver,
            CallParams callParams) {
        DoneObserver<DynamicMessage> doneObserver = new DoneObserver<>();
        StreamObserver<DynamicMessage> requestObserver = ClientCalls.asyncClientStreamingCall(
                createCall(callParams.callOptions()),
                CompositeStreamObserver.of(responseObserver, doneObserver));
        requests.forEach(requestObserver::onNext);
        requestObserver.onCompleted();
        return doneObserver.getCompletionFuture();
    }

    private ListenableFuture<Void> callServerStreaming(
            DynamicMessage request,
            StreamObserver<DynamicMessage> responseObserver,
            CallParams callParams) {
        DoneObserver<DynamicMessage> doneObserver = new DoneObserver<>();
        ClientCalls.asyncServerStreamingCall(
                createCall(callParams.callOptions()),
                request,
                CompositeStreamObserver.of(responseObserver, doneObserver));
        return doneObserver.getCompletionFuture();
    }

    private ListenableFuture<Void> callUnary(
            DynamicMessage request,
            StreamObserver<DynamicMessage> responseObserver,
            CallParams callParams) {
        DoneObserver<DynamicMessage> doneObserver = new DoneObserver<>();
        ClientCalls.asyncUnaryCall(
                createCall(callParams.callOptions()),
                request,
                CompositeStreamObserver.of(responseObserver, doneObserver));
        return doneObserver.getCompletionFuture();
    }

    private ClientCall<DynamicMessage, DynamicMessage> createCall(CallOptions callOptions) {
        return channel.newCall(createGrpcMethodDescriptor(), callOptions);
    }

    private io.grpc.MethodDescriptor<DynamicMessage, DynamicMessage> createGrpcMethodDescriptor() {
        return io.grpc.MethodDescriptor.<DynamicMessage, DynamicMessage>create(
                getMethodType(),
                getFullMethodName(),
                new DynamicMessageMarshaller(protoMethodDescriptor.getInputType()),
                new DynamicMessageMarshaller(protoMethodDescriptor.getOutputType()));
    }

    private String getFullMethodName() {
        String serviceName = protoMethodDescriptor.getService().getFullName();
        String methodName = protoMethodDescriptor.getName();
        return io.grpc.MethodDescriptor.generateFullMethodName(serviceName, methodName);
    }

    /** Returns the appropriate method type based on whether the client or server expect streams. */
    private MethodType getMethodType() {
        boolean clientStreaming = protoMethodDescriptor.toProto().getClientStreaming();
        boolean serverStreaming = protoMethodDescriptor.toProto().getServerStreaming();

        if (!clientStreaming && !serverStreaming) {
            return MethodType.UNARY;
        } else if (!clientStreaming && serverStreaming) {
            return MethodType.SERVER_STREAMING;
        } else if (clientStreaming && !serverStreaming) {
            return MethodType.CLIENT_STREAMING;
        } else {
            return MethodType.BIDI_STREAMING;
        }
    }
}
