#!/usr/bin/env sh

set -e -u

cd java-buildpack-auto-reconfiguration
./mvnw -q -Dmaven.repo.local=../m2/repository -Dmaven.user.home=../m2 -Dmaven.test.skip=true deploy
