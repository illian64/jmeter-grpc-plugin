package com.podoinikovi.jmeter.grpc.exception;

public class GrpcPluginException extends RuntimeException {
    public GrpcPluginException(String message) {
        super(message);
    }

    public GrpcPluginException(String message, Throwable cause) {
        super(message, cause);
    }

    public GrpcPluginException(Throwable cause) {
        super(cause);
    }
}
