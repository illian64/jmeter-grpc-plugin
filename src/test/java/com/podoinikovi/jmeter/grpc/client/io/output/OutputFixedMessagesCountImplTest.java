package com.podoinikovi.jmeter.grpc.client.io.output;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OutputFixedMessagesCountImplTest {
    @Test
    void add() {
        int limit = 3;
        Output output = new OutputFixedMessagesCountImpl(limit);
        for (int i = 0; i < 10; i++) {
            output.storeGrpcMessage(i);
        }

        assertEquals(limit, output.messagesCount());
        String newLine = AbstractOutput.OUTPUT_MESSAGE_SEPARATOR;
        assertEquals("7" + newLine + "8" + newLine + "9" + newLine, output.getGrpcMessageString());
    }
}