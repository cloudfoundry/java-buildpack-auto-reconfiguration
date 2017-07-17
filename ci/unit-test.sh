#!/usr/bin/env sh

set -e -u

mkdir -p /data/db
mongod &

[[ -d $PWD/maven && ! -d $HOME/.m2 ]] && ln -s $PWD/maven $HOME/.m2

cd java-buildpack-auto-reconfiguration
./mvnw -q test
