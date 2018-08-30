#!/usr/bin/env bash

set -e -x

APP=app

mvn compile
mvn -s settings.xml compile

oc start-build ${APP} --from-dir=. --follow
