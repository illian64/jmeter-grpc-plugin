package com.podoinikovi.jmeter.grpc.client.io.output;

import com.podoinikovi.jmeter.grpc.client.MetadataCapture;

import java.util.Collection;

public abstract class AbstractOutput implements Output {
    public static final String OUTPUT_MESSAGE_SEPARATOR = "\n\n";

    protected final MetadataCapture metadataCapture = new MetadataCapture();
    protected final Collection<Object> outputCollection;

    protected AbstractOutput(Collection<Object> outputCollection) {
        this.outputCollection = outputCollection;
    }

    @Override
    public void storeGrpcMessage(Object message) {
        outputCollection.add(message);
    }

    @Override
    public MetadataCapture getMetadataCapture() {
        return metadataCapture;
    }

    @Override
    public String getGrpcMessageString() {
        if (outputCollection.size() == 1) {
            return outputCollection.iterator().next().toString();
        }
        StringBuilder sb = new StringBuilder();
        for (Object message : outputCollection) {
            sb.append(message).append(OUTPUT_MESSAGE_SEPARATOR);
        }

        return sb.toString();
    }

    @Override
    public int messagesCount() {
        return outputCollection.size();
    }
}
