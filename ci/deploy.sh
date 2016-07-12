#!/usr/bin/env sh

set -e

cd java-buildpack-auto-reconfiguration
./mvnw -q -Dmaven.test.skip=true deploy
