#!/usr/bin/env sh

set -e -u

mkdir -p /data/db
mongod &

cd java-buildpack-auto-reconfiguration
./mvnw -q -Dmaven.repo.local=../m2/repository -Dmaven.user.home=../m2 test
