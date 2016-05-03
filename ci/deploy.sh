#!/usr/bin/env bash

set -e

pushd java-buildpack-auto-reconfiguration
  ./mvnw -Dmaven.test.skip=true deploy
popd
