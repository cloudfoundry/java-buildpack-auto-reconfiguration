#!/usr/bin/env sh

set -e -u

ln -fs $PWD/maven $HOME/.m2

cd java-buildpack-auto-reconfiguration
./mvnw -q -Dmaven.test.skip=true deploy
