package com.podoinikovi.jmeter.grpc.client.io.output;

import com.google.common.collect.EvictingQueue;
import lombok.ToString;

@ToString
public class OutputFixedMessagesCountImpl extends AbstractOutput {
    public OutputFixedMessagesCountImpl(int messagesLimit) {
        super(EvictingQueue.create(messagesLimit));
    }
}
