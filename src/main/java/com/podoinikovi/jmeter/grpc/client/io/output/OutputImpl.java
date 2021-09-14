package com.podoinikovi.jmeter.grpc.client.io.output;

import lombok.ToString;

import java.util.ArrayList;

@ToString
public class OutputImpl extends AbstractOutput {
    public OutputImpl() {
        super(new ArrayList<>());
    }
}
