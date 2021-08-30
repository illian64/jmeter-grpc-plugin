package com.podoinikovi.jmeter.grpc.client;

import com.google.protobuf.DynamicMessage;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.MethodDescriptor;
import io.grpc.stub.AbstractStub;
import io.grpc.stub.StreamObserver;

import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
import static io.grpc.stub.ClientCalls.asyncUnaryCall;

public class DynamicStub extends AbstractStub<DynamicStub> {
    public DynamicStub(Channel channel, CallOptions callOptions) {
        super(channel, callOptions);
    }

    @Override
    protected DynamicStub build(Channel channel, CallOptions callOptions) {
        return new DynamicStub(channel, callOptions);
    }

    public void stubAsyncUnaryCall(DynamicMessage request, MethodDescriptor<DynamicMessage, DynamicMessage> methodDescriptor, StreamObserver<DynamicMessage>responseObserver) {
        asyncUnaryCall(getChannel().newCall(methodDescriptor, getCallOptions()), request, responseObserver);
    }

    public void stubAsyncServerStreamingCall(DynamicMessage request, MethodDescriptor<DynamicMessage, DynamicMessage> methodDescriptor, StreamObserver<DynamicMessage>responseObserver) {
        asyncServerStreamingCall(getChannel().newCall(methodDescriptor, getCallOptions()), request, responseObserver);
    }
}
