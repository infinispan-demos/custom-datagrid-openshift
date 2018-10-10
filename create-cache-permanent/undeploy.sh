#!/usr/bin/env bash

set -e -x

oc delete all,secrets,sa,templates,configmaps,daemonsets,clusterroles,rolebindings,serviceaccounts,statefulsets --selector=app=app || true
oc delete all,secrets,sa,templates,configmaps,daemonsets,clusterroles,rolebindings,serviceaccounts,statefulsets --selector=run=app || true
