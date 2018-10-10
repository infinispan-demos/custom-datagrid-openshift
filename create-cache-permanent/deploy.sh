#!/usr/bin/env bash

set -e -x

oc login -u developer -p developer
oc project myproject

APP=app

# || true to make it idempotent
oc new-build --binary --name=${APP} -l app=${APP} || true

mvn -s settings.xml clean dependency:copy-dependencies compile -DincludeScope=runtime

oc start-build ${APP} --from-dir=. --follow

oc run app --image=172.30.1.1:5000/myproject/app --replicas=1  --restart=OnFailure

getPodStatus() {
  oc get pod -l run=app -o jsonpath="{.items[0].status.phase}"
}

expected="Succeeded"
status="NA"
while [[ ${status} -ne ${expected} ]]; do
  sleep .5
  status=$(getPodStatus)
  echo "status of pod: ${status}"
done

POD=$(oc get pod -l run=app -o jsonpath="{.items[0].metadata.name}")
oc logs $POD -f
