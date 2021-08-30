package com.podoinikovi.jmeter.grpc.client.io;

import com.podoinikovi.jmeter.grpc.client.MetadataCapture;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@ToString
@NoArgsConstructor
public class OutputImpl implements Output {
    public static final String OUTPUT_MESSAGE_SEPARATOR = "\n\n";

    private final List<Object> output = new ArrayList<>();
    private final MetadataCapture metadataCapture = new MetadataCapture();

    @Override
    public void storeGrpcMessage(Object message) {
        output.add(message);
    }

    @Override
    public String getGrpcMessageString() {
        if (output.size() == 1) {
            return output.get(0).toString();
        }
        StringBuilder sb = new StringBuilder();
        for (Object message : output) {
            sb.append(message).append(OUTPUT_MESSAGE_SEPARATOR);
        }
        return sb.toString();
    }

    @Override
    public MetadataCapture getMetadataCapture() {
        return metadataCapture;
    }

    @Override
    public int messagesCount() {
        return output.size();
    }
}
