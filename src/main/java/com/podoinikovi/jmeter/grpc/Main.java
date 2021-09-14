package com.podoinikovi.jmeter.grpc;

import com.podoinikovi.jmeter.grpc.client.CallParams;
import com.podoinikovi.jmeter.grpc.client.ConnectionParams;
import com.podoinikovi.jmeter.grpc.client.GrpcClient;
import com.podoinikovi.jmeter.grpc.client.io.output.Output;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;

@Slf4j
public class Main {
    public static void main(String[] args) {
        String metadata = "key1::" + Instant.now().toString();
        ConnectionParams connectionParams = new ConnectionParams("localhost:8080",
//                "D:\\Prog\\YandexDisk\\Java\\spring-cloud\\spring-cloud-sleuth\\proto",
                "/opt/var/1.proto",
                "", false, true);
        CallParams callParams = CallParams.unary(metadata, 10000L);
        GrpcClient grpcClient = new GrpcClient(connectionParams);
        //Output call = grpcClient.call("my.test.HelloService/helloServerStream", "{\"requestId\": \"1\"}", 100_000L);
        Output call = grpcClient.call("my.test.HelloService/helloClientStream", "{\"requestId\": \"1\"}\n\n{\"requestId\": \"2\"}\n\n{\"requestId\": \"3\"}", callParams);

        //Output call = grpcClient.call("my.test.HelloService/helloServerStream", "{\"requestId\": \"1\"}\n\n{\"requestId\": \"2\"}\n\n{\"requestId\": \"3\"}", 100_000L);


        log.info("call counts: {}", call.messagesCount());
        grpcClient.shutdownChannel();


        //GrpcClient grpcClient1
        /*
        int count = 15;
        List<Integer> list = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            list.add(i);
        }
        GrpcConnectionPool connectionPool = new GrpcConnectionPool();
        list.parallelStream().forEach(i -> {
            GrpcClient grpcClient = connectionPool.setupClient(params);
            grpcClient.openChannel();
            Output output = grpcClient.call("{}", "100999");
            grpcClient.shutdownChannel();
            log.info("output = {}", output);
            //System.out.println(output);
        });*/
    }
}
