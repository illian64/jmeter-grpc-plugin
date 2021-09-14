package com.podoinikovi.jmeter.grpc.client;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import io.grpc.CallOptions;
import io.grpc.Metadata;
import lombok.Data;

import java.util.concurrent.TimeUnit;

@Data
public class CallParams {
    public static final int DEFAULT_VALUE = 0;
    public static final char DELIMITER = ',';
    public static final String DELIMITER_STR = Character.toString(DELIMITER);

    private final String metadataStr;
    private final Metadata metadataMap;
    private final Integer streamStopAfterMessages;
    private final Integer streamStopAfterTime;
    private final Integer streamMessageLimit;
    private final Long deadlineMs;

    public CallParams(String metadata, Long deadlineMs, Integer streamStopAfterMessages,
                      Integer streamStopAfterTime, Integer streamMessageLimit) {
        this.metadataMap = metadataMap(metadata);
        this.metadataStr = metadataStr(metadata);
        this.deadlineMs = deadlineMs;

        this.streamStopAfterMessages = streamStopAfterMessages;
        this.streamStopAfterTime = streamStopAfterTime;
        this.streamMessageLimit = streamMessageLimit;
    }

    public static CallParams unary(String metadata, Long deadline) {
        return new CallParams(metadata, deadline, DEFAULT_VALUE, DEFAULT_VALUE, DEFAULT_VALUE);
    }

    public CallOptions callOptions() {
        CallOptions result = CallOptions.DEFAULT;
        if (deadlineMs > 0) {
            result = result.withDeadlineAfter(deadlineMs, TimeUnit.MILLISECONDS);
        }

        return result;
    }

    public String metadataStr(String metadata) {
        if (Strings.isNullOrEmpty(metadata)) {
            return "";
        }

        return metadata.replace(DELIMITER, '\n');
    }

    private Metadata metadataMap(String metadata) {
        Metadata result = new Metadata();

        if (Strings.isNullOrEmpty(metadata)) {
            return result;
        }

        String[] keyValue;
        for (String part : metadata.split(DELIMITER_STR)) {
            keyValue = part.split(MetadataCapture.KEY_VALUE_DELIMITER, 2);

            Preconditions.checkArgument(keyValue.length == 2,
                    String.format("Metadata entry must be defined in key1%svalue1,key2%svalue2 format: %s",
                            MetadataCapture.KEY_VALUE_DELIMITER, MetadataCapture.KEY_VALUE_DELIMITER, metadata));

            Metadata.Key<String> key = Metadata.Key.of(keyValue[0], Metadata.ASCII_STRING_MARSHALLER);
            result.put(key, keyValue[1]);
        }

        return result;
    }
}
