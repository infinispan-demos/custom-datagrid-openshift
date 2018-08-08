# S2I for custom configuration

## Trying it out

This is a demo repository showing how S2I can be used to load custom data grid XML configuration.
This repo already contains a modified XML that adds `custom-cache` named cache.
The accompanying application verifies that the custom cache can be accessed correctly.

To try this out, first load data grid with the custom XML configuration:

```bash
oc cluster up

oc login -u system:admin
oc create -n openshift -f https://raw.githubusercontent.com/jboss-container-images/jboss-datagrid-7-openshift-image/1.0/templates/datagrid72-image-stream.json
oc create -n openshift -f https://raw.githubusercontent.com/jboss-container-images/jboss-datagrid-7-openshift-image/1.0/templates/datagrid72-basic.json

oc login -u developer
oc process openshift//datagrid72-basic | oc create -f -

oc new-build --name=customdg -i openshift/jboss-datagrid72-openshift:1.0 --binary=true --to='customdg:1.0'
oc set triggers dc/datagrid-app --from-image=openshift/jboss-datagrid72-openshift:1.0 --remove
oc set triggers dc/datagrid-app --from-image=customdg:1.0 -c datagrid-app

cd openshift
oc start-build customdg --from-dir=. -F
```

Once the data grid pod has started fine, exercise the application to verify it works as expected:

```bash
./first-deploy.sh
...
curl http://app-myproject.127.0.0.1.nip.io/test
```

To apply modifications to the application, you can call `deploy.sh` script.
This script recompiles, builds and pushes updates to the existing application.
Note that if making dependency changes, you'll have to call `first-deploy.sh` again.

## Extracting base XML configuration

In this section you can find instructions to extra base XML configuration.
This might be needed if using a more recent base data grid version.

```bash
oc cluster up

oc login -u system:admin
oc create -n openshift -f https://raw.githubusercontent.com/jboss-container-images/jboss-datagrid-7-openshift-image/1.0/templates/datagrid72-image-stream.json
oc create -n openshift -f https://raw.githubusercontent.com/jboss-container-images/jboss-datagrid-7-openshift-image/1.0/templates/datagrid72-basic.json

oc login -u developer
oc process openshift//datagrid72-basic | oc create -f -

oc new-build --name=customdg -i openshift/jboss-datagrid72-openshift:1.0 --binary=true --to='customdg:1.0'
oc set triggers dc/datagrid-app --from-image=openshift/jboss-datagrid72-openshift:1.0 --remove
oc set triggers dc/datagrid-app --from-image=customdg:1.0 -c datagrid-app

cd openshift/configuration
docker run registry.access.redhat.com/jboss-datagrid-7/datagrid72-openshift:1.0 /bin/sh -c 'cat /opt/datagrid/standalone/configuration/clustered-openshift.xml' > clustered-openshift.xml
```

Then, just make necessary changes to the XML and try out the application. 
