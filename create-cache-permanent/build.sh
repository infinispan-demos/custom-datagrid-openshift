#!/usr/bin/env bash

set -e -x

oc login -u developer -p developer
oc project myproject

APP=app

# || true to make it idempotent
oc new-build --binary --strategy=source --name=${APP} -l app=${APP} fabric8/s2i-java:2.3 || true

mvn -s settings.xml clean package -DincludeScope=runtime

oc start-build ${APP} --from-dir=target/ --follow
