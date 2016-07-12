#!/usr/bin/env sh

set -e

mkdir -p /data/db
mongod &

cd java-buildpack-auto-reconfiguration
./mvnw -q test
