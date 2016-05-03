#!/usr/bin/env bash

set -e

service mongodb start

pushd java-buildpack-auto-reconfiguration
  ./mvnw test
popd
