# Encrypt Cluster Communications

```bash
$ oc login -u system:admin

$ oc create \
  -n openshift \
  -f https://raw.githubusercontent.com/jboss-container-images/jboss-datagrid-7-openshift-image/1.1/templates/datagrid72-image-stream.json

$ oc create \
  -n openshift \
  -f https://raw.githubusercontent.com/jboss-container-images/jboss-datagrid-7-openshift-image/1.1/templates/datagrid72-https.json
```

```bash
$ oc login -u developer

$ oc create \
   -f https://raw.githubusercontent.com/jboss-openshift/application-templates/master/secrets/datagrid-app-secret.json

$ oc process openshift//datagrid72-https | oc create -f -
```
