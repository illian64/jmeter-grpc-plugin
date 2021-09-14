package com.podoinikovi.jmeter.grpc.client;

import com.google.common.base.Strings;
import com.google.common.net.HostAndPort;
import lombok.Data;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class ConnectionParams {
    private final HostAndPort hostAndPort;
    private final String protoDiscoveryRoot;
    private final boolean useTls;
    private final boolean useInsecureTls;
    private final List<String> includePathsList;

    public ConnectionParams(String hostPort, String protoDiscoveryRoot, String includePaths,
                            boolean useTls, boolean useInsecureTls) {
        this.hostAndPort = HostAndPort.fromString(hostPort);
        this.protoDiscoveryRoot = protoDiscoveryRoot;
        this.useTls = useTls;
        this.useInsecureTls = useInsecureTls;

        this.includePathsList = buildIncludePath(includePaths);
    }

    private List<String> buildIncludePath(String includePaths) {
        if (Strings.isNullOrEmpty(includePaths)) {
            return Collections.emptyList();
        } else {
            return Arrays.stream(includePaths.split(",")).collect(Collectors.toList());
        }
    }
}
