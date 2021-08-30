package com.podoinikovi.jmeter.grpc.client.io;

import com.google.common.collect.ImmutableList;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.util.JsonFormat;
import com.podoinikovi.jmeter.grpc.exception.GrpcPluginParseMessageException;

/** A utility class which knows how to read proto files written using {@link MessageWriter}. */
public class MessageReader {
    public static final String READER_MESSAGE_SEPARATOR = "\n\n";

    private final JsonFormat.Parser jsonParser;
    private final Descriptor descriptor;
    private final String source;

    public MessageReader(Descriptor descriptor, JsonFormat.TypeRegistry registry, String source) {
        this.jsonParser = JsonFormat.parser().usingTypeRegistry(registry).ignoringUnknownFields();
        this.descriptor = descriptor;
        this.source = source;
    }

    public ImmutableList<DynamicMessage> read() {
        ImmutableList.Builder<DynamicMessage> resultBuilder = ImmutableList.builder();
        try {
            String[] messages = source.split(READER_MESSAGE_SEPARATOR);
            for (String message : messages) {
                DynamicMessage.Builder nextMessage = DynamicMessage.newBuilder(descriptor);
                jsonParser.merge(message, nextMessage);
                // Clean up and prepare for next message.
                resultBuilder.add(nextMessage.build());
            }

            return resultBuilder.build();
        } catch (Exception e) {
            throw new GrpcPluginParseMessageException("Unable to read messages from: " + source, e);
        }
    }
}
