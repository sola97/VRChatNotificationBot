FROM alpine:3.10 as builder

LABEL maintainer="sola97 <my@sora.vip> "

ENV JAR_FILE https://github.com/sola97/VRChatNotificationBot/releases/download/latest/vrchat-notification-bot.jar
RUN  apk update && \
    apk add --no-cache curl && \
    curl -sSL ${JAR_FILE} > /app.jar

FROM openjdk:8-jdk-alpine
WORKDIR /
COPY --from=builder /app.jar app.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]

