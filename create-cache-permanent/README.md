# Creating a Permanent Cache
Learn how to create permanent cache instances with Red Hat Data Grid.

A permanent cache can survive between application restarts so you only need to create the cache once. However, data that resides in the cache is not persisted between restarts unless you configure persistent storage.

This repository contains:
* An application that you can use to create a cache instance and perform operations on the cache.
* A `pom.xml` file that defines properties and dependencies for deploying the application.

## Creating a Data Grid Pod

1. Import the Data Grid service template if it is not already available.

  ```bash
  $ oc login -u system:admin

  $ oc create \
    -n openshift \
    -f https://raw.githubusercontent.com/jboss-container-images/jboss-datagrid-7-openshift-image/f91b94cfd7da4630ca188cd43c26755ecfc99bdd/services/datagrid-service.json
  ```

2. Instantiate the template with the Data Grid image.

  ```bash
  $ oc login -u developer

  $ oc process openshift//datagrid-service \
    -p IMAGE=docker-registry.engineering.redhat.com/gzamarre/datagrid72-openshift:JDG-2055 \
    -p NUMBER_OF_INSTANCES=1 \
    -p APPLICATION_USER=test \
    -p APPLICATION_USER_PASSWORD=test \
    | oc create -f -
  ```

## Creating the Permanent Cache

1. Change to the `create-cache-permanent` directory in this repository.

  ```bash
  $ cd create-cache-permanent
  ```

2. Deploy the sample `create-cache-permanent` application.

  This application uses the OpenShift source-to-image process to build and deploy the application.

  ```bash
  $ mvn fabric8:deploy
  ```

3. Check the status of the project and note the project name and IP address for the `create-cache-permanent` application.

  ```bash
  $ oc get status
  ```

4. Connect to the Data Grid service. Modify the project name and IP address if required.

  ```bash
  $ curl http://create-cache-permanent-myproject.127.0.0.1.nip.io/connect/datagrid-service
  Successfully connected to Data Grid
  ```

5. Create a cache instance with the `create-cache-permanent` application. The application generates a random name for the cache

  ```bash
  $ curl http://create-cache-permanent-myproject.127.0.0.1.nip.io/create-cache
  Cache f62e4b80-90ca-44ec-9085-231dd9b60335-0 created
  ```
  The application generates a random name for the cache that the invocation to `create-cache` returns.

## Verifying that the Cache is Permanent

1. Invoke a `get/put` operation on the cache to add an entry.

  ```bash
  $ curl http://create-cache-permanent-myproject.127.0.0.1.nip.io/get-cache
  Got cache, put/get returned: sample-value
  ```

2. Scale the Data Grid stateful set to `0` replicas.

  ```bash
  $ oc get sts
  NAME               DESIRED   CURRENT   AGE
  datagrid-service   1         1         19m

  $ oc scale sts datagrid-service --replicas=0
  statefulset "datagrid-service" scaled

  $ oc get sts
  NAME               DESIRED   CURRENT   AGE
  datagrid-service   0         0         7m
  ```

3. Scale the Data Grid stateful set to `1` replica.

  ```bash
  $ oc scale sts datagrid-service --replicas=1
  statefulset "datagrid-service" scaled

  $ oc get sts
  NAME               DESIRED   CURRENT   AGE
  datagrid-service   1         1         7m
  ```

4. Watch the pod and wait until the Data Grid service starts running.

  ```bash
  $ oc get pods -w
  NAME                                 READY     STATUS      RESTARTS   AGE
  create-cache-permanent-1-mxmb7       1/1       Running     0          3m
  create-cache-permanent-s2i-1-build   0/1       Completed   0          5m
  datagrid-service-0                   0/1       Running     0          17s
  datagrid-service-0                   1/1       Running     0          50s
  ```

5. Invoke a `get/put` operation on the cache to ensure that the same sample entry exists.

  ```bash
  $ curl http://create-cache-permanent-myproject.127.0.0.1.nip.io/get-cache
  Got cache, put/get returned: sample-value
  ```

## Looking at the Application Code
As demonstrated in the previous steps, the `create-cache-permanent` application shows how to create permanent cache instances with the Data Grid service. You can create your own applications to do this too.

The first step is to instantiate `RemoteCacheManager` to connect to the Data Grid service. You do this with the HTTP call to `/connect/datagrid-service` in the preceding steps.

When you call `/create-cache` the application generates a random cache name and creates the cache using `RemoteCacheManager` as follows:

```java
RemoteCacheManager remoteCacheManager = ...

RemoteCache<K, V> remoteCache = remoteCacheManager
   .administration()
      .withFlags(CacheContainerAdmin.AdminFlag.PERMANENT)
      .createCache(cacheName, "replicated");
```

`AdminFlag.PERMANENT` creates a permanent cache. If that flag is not included, an ephemeral cache is created.

The remote cache is also created using the `replicated` cache template that is included in the Data Grid service. As a result, the cache is replicated to all nodes in the cluster.
