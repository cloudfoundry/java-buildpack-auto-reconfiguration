#!/usr/bin/env sh

set -e -u

mkdir -p /data/db
mongod &

ln -fs $PWD/maven $HOME/.m2

cd java-buildpack-auto-reconfiguration
./mvnw -q test
