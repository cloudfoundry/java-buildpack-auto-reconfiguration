FROM java:8-jdk-alpine

RUN echo "http://dl-cdn.alpinelinux.org/alpine/edge/testing" >> /etc/apk/repositories

RUN apk add --update \
    mongodb \
 && rm -rf /var/cache/apk/*
