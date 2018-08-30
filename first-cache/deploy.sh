#!/usr/bin/env bash

set -e -x

APP=app

oc login -u developer -p developer
oc project myproject

mvn -s settings.xml compile

oc start-build ${APP} --from-dir=. --follow
