# Loading Custom XML in Red Hat Data Grid for OpenShift
Use the S2I process to modify a Red Hat Data Grid deployment configuration with custom XML.

This repository contains:
* A modified XML configuration that adds a cache named `custom-cache`.
* An application that you can use to verify access to the cache.

**Important:** Red Hat recommends that you configure Data Grid deployments using environment variables. Data Grid on OpenShift does not support all configuration parameters that are supported with Data Grid outside OpenShift. For more information, see the [Red Hat Data Grid for Openshift documentation](https://access.redhat.com/documentation/en-us/red_hat_jboss_data_grid/7.2/html-single/data_grid_for_openshift/index).

## Building a Deployment Configuration with Custom XML

1. Import the Data Grid image and `basic` template if they are not already available.

 ```bash
 $ oc login -u system:admin

 $ oc create -n openshift -f https://raw.githubusercontent.com/jboss-container-images/jboss-datagrid-7-openshift-image/1.0/templates/datagrid72-image-stream.json

 $ oc create -n openshift -f https://raw.githubusercontent.com/jboss-container-images/jboss-datagrid-7-openshift-image/1.0/templates/datagrid72-basic.json
 ```

2. Create a Data Grid pod from the `basic` template

 ```bash
 $ oc login -u developer

 $ oc process openshift//datagrid72-basic | oc create -f -
 ```

3. Create a new build with a custom image stream based on `jboss-datagrid72-openshift`.

 ```bash
 $ oc new-build --name=customdg -i openshift/jboss-datagrid72-openshift:1.1 --binary=true --to='customimg:1.0'
 ```

4. Set build triggers so that the `datagrid-app` deployment configuration uses the custom image stream.

 ```bash
 $ oc set triggers dc/datagrid-app --from-image=openshift/jboss-datagrid72-openshift:1.1 --remove

 $ oc set triggers dc/datagrid-app --from-image=customimg:1.0 -c datagrid-app
 ```

5. Change to the `openshift` directory in this repository.

 ```bash
 $ cd openshift
 ```

6. Start the new build and upload the current directory as binary input.

 ```bash
 $ oc start-build customdg --from-dir=. -F
 ```

 **Tip:** You can extract the XML configuration from the Data Grid image on the Red Hat Container Registry if you want to reset the deployment configuration, as follows:

 ```bash
 $ docker run registry.access.redhat.com/jboss-datagrid-7/datagrid72-openshift:1.1 /bin/sh -c 'cat /opt/datagrid/standalone/configuration/clustered-openshift.xml' > clustered-openshift.xml
 ```

## Verifying Access to the Cache

Run the application to verify that you can access the cache in the Data Grid pod:

```bash
$ ./first-deploy.sh

$ curl http://app-myproject.127.0.0.1.nip.io/test
```

Use the `deploy.sh` script to recompile, build, and update the application. If you change dependencies, you must run `first-deploy.sh`.
