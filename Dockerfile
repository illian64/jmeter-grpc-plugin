#docker build -t jmeter-test .
FROM egaillardon/jmeter:5.3.0-1.0.1
COPY target/jmeter-grpc-plugin-1.0-SNAPSHOT.jar /opt/apache-jmeter-5.3/lib
COPY proto/HelloService.proto /opt