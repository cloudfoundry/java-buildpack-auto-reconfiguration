#!/usr/bin/env bash

set -e -x

service mongodb start

pushd java-buildpack-auto-reconfiguration
  ./mvnw test
popd
