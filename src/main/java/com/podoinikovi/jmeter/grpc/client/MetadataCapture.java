package com.podoinikovi.jmeter.grpc.client;

import io.grpc.Metadata;
import lombok.Getter;
import lombok.ToString;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

@Getter
@ToString
public class MetadataCapture {
    public static final String KEY_VALUE_DELIMITER = "::";
    public static final String VALUES_DELIMITER = ", ";
    public static final String EMPTY_RESULT = "";

    private final AtomicReference<Metadata> headersCapture = new AtomicReference<>();
    private final AtomicReference<Metadata> trailersCapture = new AtomicReference<>();

    public String toHeadersString() {
        return "Metadata:\n" + metadataToString(headersCapture.get()) +
                "\nTrailers:\n" + metadataToString(trailersCapture.get());
    }

    public static String metadataToString(@Nullable Metadata metadata) {
        if (metadata == null) {
            return EMPTY_RESULT;
        }

        Set<String> keys = metadata.keys();
        if (keys.isEmpty()) {
            return EMPTY_RESULT;
        }

        StringBuilder sb = new StringBuilder();
        for (String key : keys) {
            sb.append(key).append(KEY_VALUE_DELIMITER);
            Iterable<String> values = metadata.getAll(Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER));
            if (values != null) {
                int count = 0;
                for (String nextValue : values) {
                    if (count == 0) {
                        sb.append(nextValue);
                    } else {
                        sb.append(VALUES_DELIMITER).append(nextValue);
                    }
                    count++;
                }
                sb.append('\n');
            }
        }

        return sb.toString();
    }
}
