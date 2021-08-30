package com.podoinikovi.jmeter.grpc.client;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors.MethodDescriptor;
import com.google.protobuf.Descriptors.ServiceDescriptor;
import com.podoinikovi.jmeter.grpc.client.protobuf.ProtocInvoker;
import com.podoinikovi.jmeter.grpc.client.protobuf.ServiceResolver;
import lombok.experimental.UtilityClass;

import java.util.LinkedList;
import java.util.List;

@UtilityClass
public class ClientList {
    public static List<String> listServices(String protoFile, List<String> libFolder) {
        List<String> methods = new LinkedList<>();

        final DescriptorProtos.FileDescriptorSet fileDescriptorSet;
        try {
            ProtocInvoker invoker = ProtocInvoker.forConfig(protoFile, libFolder);
            fileDescriptorSet = invoker.invoke();
        } catch (Exception e) {
            throw new RuntimeException("Unable to resolve service by invoking protoc", e);
        }

        ServiceResolver serviceResolver = ServiceResolver.fromFileDescriptorSet(fileDescriptorSet);
        for (ServiceDescriptor descriptor : serviceResolver.listServices()) {
            for (MethodDescriptor method : descriptor.getMethods()) {
                methods.add(descriptor.getFullName() + "/" + method.getName());
            }
        }

        return methods;
    }
}
