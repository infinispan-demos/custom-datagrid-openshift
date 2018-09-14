```bash
$ oc cluster up

$ oc login -u system:admin
$ oc create \
  -n openshift \
  -f https://raw.githubusercontent.com/jboss-container-images/jboss-datagrid-7-openshift-image/f91b94cfd7da4630ca188cd43c26755ecfc99bdd/services/datagrid-service.json

$ oc login -u developer
$ oc process openshift//datagrid-service \
  -p IMAGE=docker-registry.engineering.redhat.com/gzamarre/datagrid72-openshift:JDG-2055 \
  -p NUMBER_OF_INSTANCES=1 \
  -p APPLICATION_USER=test \
  -p APPLICATION_USER_PASSWORD=test \
  | oc create -f -
```

```bash

```
