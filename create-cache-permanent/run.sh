#!/usr/bin/env bash

#set -e -x
set -e

#oc delete all,secrets,sa,templates,configmaps,daemonsets,clusterroles,rolebindings,serviceaccounts--selector=run=app || true
oc delete all --selector=run=app || true
oc run app --image=172.30.1.1:5000/myproject/app --replicas=1  --restart=OnFailure

getPodStatus() {
  oc get pod -l run=app -o jsonpath="{.items[0].status.phase}"
}

#expected=Succeeded
status=NA

while [ "$status" != "Succeeded" ];
do
  status=$(getPodStatus)
  echo "Status of pod: ${status}"
  sleep .5
done

#while [[ "${status}" -ne "${expected}" ]]; do
#  sleep .5
#  status=$(getPodStatus)
#  echo "status of pod: ${status}"
#done

POD=$(oc get pod -l run=app -o jsonpath="{.items[0].metadata.name}")
oc logs $POD
