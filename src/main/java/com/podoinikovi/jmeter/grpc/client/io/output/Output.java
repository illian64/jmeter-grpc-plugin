package com.podoinikovi.jmeter.grpc.client.io.output;

import com.podoinikovi.jmeter.grpc.client.MetadataCapture;

public interface Output {
    void storeGrpcMessage(Object message);

    String getGrpcMessageString();

    MetadataCapture getMetadataCapture();

    int messagesCount();
}
