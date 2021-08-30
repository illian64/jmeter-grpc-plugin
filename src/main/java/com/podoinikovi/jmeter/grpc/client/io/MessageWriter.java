package com.podoinikovi.jmeter.grpc.client.io;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import com.google.protobuf.util.JsonFormat.TypeRegistry;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;

@Slf4j
public class MessageWriter<T extends Message> implements StreamObserver<T> {
    private final JsonFormat.Printer jsonPrinter;
    private final Output output;

    public Output getOutput() {
        return output;
    }

    public static <T extends Message> MessageWriter<T> create(Output output, TypeRegistry registry) {
        return new MessageWriter<>(JsonFormat.printer().usingTypeRegistry(registry), output);
    }

    private MessageWriter(JsonFormat.Printer jsonPrinter, Output output) {
        this.jsonPrinter = jsonPrinter;
        this.output = output;
    }

    @Override
    public void onCompleted() {
        log.debug("On completed gRPC message: {}", output.getGrpcMessageString());
    }

    @Override
    public void onError(Throwable t) {
        output.storeGrpcMessage(String.format("%s. %s",  t.getMessage(), ExceptionUtils.getStackTrace(t)));
        log.error(t.getMessage());
    }

    @Override
    public void onNext(T message) {
        try {
            output.storeGrpcMessage(jsonPrinter.print(message));
        } catch (InvalidProtocolBufferException e) {
            log.warn(e.getMessage());
        }
    }
}
