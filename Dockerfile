FROM maven:3-jdk-11
WORKDIR /build

COPY . /build
RUN mvn -DskipTests install dependency:copy-dependencies

FROM openjdk:11

WORKDIR /app

RUN mkdir /db

COPY --from=0 /build/sccp-rdf/target/rdfendpoint-*.jar /app/
COPY --from=0 /build/sccp-rdf/target/dependency/ /app/dependency/

COPY ./sccp-rdf/native /native
ENV LD_LIBRARY_PATH /native/linux

CMD java -Djava.library.path=/native/linux/ -cp "./*:dependency/*" com.colabriq.endpoint.EndpointModule
