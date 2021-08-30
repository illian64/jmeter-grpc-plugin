package com.podoinikovi.jmeter.grpc.client;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.net.HostAndPort;
import lombok.Data;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
public class ConnectionParams {
    private final HostAndPort hostAndPort;
    private final String protoDiscoveryRoot;
    private final String includePaths;
    private final boolean useTls;
    private final boolean useInsecureTls;
    private final Map<String, String> metadataMap;
    private final List<String> includePathsList;
    private final int hashCode;

    public ConnectionParams(String hostPort, String protoDiscoveryRoot, String includePaths,
                            boolean useTls, boolean useInsecureTls, String metadata) {
        this.hostAndPort = HostAndPort.fromString(hostPort);
        this.protoDiscoveryRoot = protoDiscoveryRoot;
        this.includePaths = includePaths;
        this.useTls = useTls;
        this.useInsecureTls = useInsecureTls;

        this.metadataMap = buildMetadata(metadata); //TODO move - not connection param! it's call param
        this.includePathsList = Collections.singletonList(includePaths); //TODO support for multiple paths
        this.hashCode = getHash();
    }

    public String metadataString() {
        if (metadataMap.isEmpty()) {
            return MetadataCapture.EMPTY_RESULT;
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : metadataMap.entrySet()) {
            sb.append(entry.getKey()).append(MetadataCapture.KEY_VALUE_DELIMITER).append(entry.getValue());
            sb.append('\n');
        }

        return sb.toString();
    }

    private Map<String, String> buildMetadata(String metadata) {
        Map<String, String> metadataHash = new LinkedHashMap<>();

        if (Strings.isNullOrEmpty(metadata))
            return metadataHash;

        String[] keyValue;
        for (String part : metadata.split(",")) {
            keyValue = part.split(":", 2);

            Preconditions.checkArgument(keyValue.length == 2,
                    "Metadata entry must be defined in key1:value1,key2:value2 format: " + metadata);

            metadataHash.put(keyValue[0], keyValue[1]);
        }

        return metadataHash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        ConnectionParams that = (ConnectionParams) o;

        return new EqualsBuilder()
                .append(useTls, that.useTls)
                .append(useInsecureTls, that.useInsecureTls)
                .append(hostAndPort, that.hostAndPort)
                .append(protoDiscoveryRoot, that.protoDiscoveryRoot)
                .append(includePathsList, that.includePathsList)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    public int getHash() {
        return new HashCodeBuilder(17, 37)
                .append(hostAndPort)
                .append(protoDiscoveryRoot)
                .append(includePathsList)
                .append(useTls)
                .append(useInsecureTls)
                .toHashCode();
    }
}
