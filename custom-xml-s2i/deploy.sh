#!/usr/bin/env bash

set -e -x

APP=app

mvn compile

oc start-build ${APP} --from-dir=. --follow
