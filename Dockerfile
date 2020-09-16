FROM maven:3.6.3-jdk-8 as builder
LABEL maintainer="sola97 <my@sora.vip> "
WORKDIR /
RUN git clone https://github.com/sola97/VRChatNotificationBot.git && \
    cd VRChatNotificationBot && \
    mvn --no-transfer-progress package && \
    mv target/vrchat*.jar /app.jar && \
    chmod +x /app.jar

FROM openjdk:8-jdk-alpine
WORKDIR /
COPY --from=builder /app.jar app.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar","-Xmx256m","-Xms64m","-XX:+UseG1GC"]

