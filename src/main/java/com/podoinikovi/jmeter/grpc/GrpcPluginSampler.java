package com.podoinikovi.jmeter.grpc;

import com.podoinikovi.jmeter.grpc.client.ConnectionParams;
import com.podoinikovi.jmeter.grpc.client.GrpcClient;
import com.podoinikovi.jmeter.grpc.client.MetadataCapture;
import com.podoinikovi.jmeter.grpc.client.io.Output;
import com.podoinikovi.jmeter.grpc.exception.GrpcPluginSystemException;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.ThreadListener;

import java.nio.charset.StandardCharsets;

@Slf4j
@SuppressWarnings("WeakerAccess")
public class GrpcPluginSampler extends AbstractSampler implements ThreadListener {
    public static final String PREFIX = "GrpcPluginSampler.";
    public static final String METADATA = PREFIX + "metadata";
    public static final String LIB_FOLDER = PREFIX + "libFolder";
    public static final String PROTO_FOLDER = PREFIX + "protoFolder";
    public static final String HOST = PREFIX + "host";
    public static final String PORT = PREFIX + "port";
    public static final String FULL_METHOD = PREFIX + "fullMethod";
    public static final String REQUEST_JSON = PREFIX + "requestJson";
    public static final String DEADLINE = PREFIX + "deadline";
    public static final String TLS = PREFIX + "tls";
    public static final String TLS_DISABLE_VERIFICATION = PREFIX + "tlsDisableVerification";

    private transient GrpcClient grpcClient = null;
    private transient ConnectionParams connectionParams = null;

    private void initGrpcClient() {
        if (grpcClient == null) {
            connectionParams = new ConnectionParams(getHostPort(), getProtoFolder(), getLibFolder(), isTls(), isTlsDisableVerification(), getMetadata());
            grpcClient = new GrpcClient(connectionParams);
        }
    }

    @Override
    public SampleResult sample(Entry ignored) {
        SampleResult sampleResult = new SampleResult();
        try {
            initGrpcClient();
            sampleResult.setSampleLabel(getName());
            sampleResult.setSamplerData(getRequestJson());
            sampleResult.setRequestHeaders(connectionParams.metadataString());
            sampleResult.sampleStart();
            Output output = grpcClient.call(getFullMethod(), getRequestJson(), parsingDeadlineTime(getDeadline()));
            sampleResult.sampleEnd();
            sampleResult.setSuccessful(true);
            sampleResult.setResponseData(output.getGrpcMessageString(), StandardCharsets.UTF_8.name());
            sampleResult.setResponseMessage("Success, received messages: " + output.messagesCount());
            sampleResult.setResponseHeaders(output.getMetadataCapture().toHeadersString());
            sampleResult.setDataType(SampleResult.TEXT);
            sampleResult.setResponseCode("0, OK");
        } catch (RuntimeException e) {
            errorResult(sampleResult, e);
        }
        return sampleResult;
    }

    @Override
    public void threadStarted() {
        log.debug("{}\ttestStarted", whoAmI());
    }

    @Override
    public void threadFinished() {
        log.debug("{}\ttestEnded", whoAmI());
        if (grpcClient != null) {
            grpcClient.shutdownChannel();
            grpcClient = null;
        }
    }

    private String whoAmI() {
        return Thread.currentThread().getName() + "@" + Integer.toHexString(hashCode()) + "-" + getName();
    }

    private void errorResult(SampleResult sampleResult, Exception e) {
        sampleResult.sampleEnd();
        sampleResult.setSuccessful(false);
        sampleResult.setResponseData("Exception: " + e.getCause().getMessage(), "UTF-8");
        sampleResult.setResponseMessage("Exception: " + e.getCause().getMessage());
        sampleResult.setDataType(SampleResult.TEXT);

        if (e instanceof GrpcPluginSystemException && e.getCause() instanceof StatusRuntimeException) {
            StatusRuntimeException statusRuntimeException = (StatusRuntimeException) e.getCause();
            sampleResult.setResponseHeaders(MetadataCapture.metadataToString(statusRuntimeException.getTrailers()));
            sampleResult.setResponseCode(statusRuntimeException.getStatus().getCode().value() + ", "
                    + statusRuntimeException.getStatus().getCode().toString());
        } else {
            sampleResult.setResponseCode("500");
        }
    }

    public String getMetadata() {
        return getPropertyAsString(METADATA);
    }

    public void setMetadata(String metadata) {
        setProperty(METADATA, metadata);
    }

    public String getLibFolder() {
        return getPropertyAsString(LIB_FOLDER);
    }

    public void setLibFolder(String libFolder) {
        setProperty(LIB_FOLDER, libFolder);
    }

    public String getProtoFolder() {
        return getPropertyAsString(PROTO_FOLDER);
    }

    public void setProtoFolder(String protoFolder) {
        setProperty(PROTO_FOLDER, protoFolder);
    }

    public String getFullMethod() {
        return getPropertyAsString(FULL_METHOD);
    }

    public void setFullMethod(String fullMethod) {
        setProperty(FULL_METHOD, fullMethod);
    }

    public String getRequestJson() {
        return getPropertyAsString(REQUEST_JSON);
    }

    public void setRequestJson(String requestJson) {
        setProperty(REQUEST_JSON, requestJson);
    }

    public String getDeadline() {
        return getPropertyAsString(DEADLINE);
    }

    public void setDeadline(String deadline) {
        setProperty(DEADLINE, deadline);
    }

    public boolean isTls() {
        return getPropertyAsBoolean(TLS);
    }

    public void setTls(boolean tls) {
        setProperty(TLS, tls);
    }

    public boolean isTlsDisableVerification() {
        return getPropertyAsBoolean(TLS_DISABLE_VERIFICATION);
    }

    public void setTlsDisableVerification(boolean tlsDisableVerification) {
        setProperty(TLS_DISABLE_VERIFICATION, tlsDisableVerification);
    }

    public String getHost() {
        return getPropertyAsString(HOST);
    }

    public void setHost(String host) {
        setProperty(HOST, host);
    }

    public String getPort() {
        return getPropertyAsString(PORT);
    }

    public void setPort(String port) {
        setProperty(PORT, port);
    }

    private String getHostPort() {
        return getHost() + ":" + getPort();
    }

    private Long parsingDeadlineTime(String deadlineMs) {
        try {
            return Long.parseLong(deadlineMs);
        } catch (Exception e) {
            throw new IllegalArgumentException("Caught exception while parsing deadline to long", e);
        }
    }
}
