# Creating Permanent Caches
Permanent caches survive between application restarts. You only need to create a permanent cache once.

_NOTE:_ Data in the cache is not persisted between restarts unless you add storage.

This repository contains:

* A set of Java class files that demonstrate how to programmatically create a cache instance and perform get and put operations on the cache.
* A `pom.xml` file that defines properties and dependencies for compiling the Java class files into an application.

## System Requirements

* Java 8.0 (Java SDK 1.8) or later.
* Maven 3.0 or later.
* A running OpenShift environment.

## Creating a Data Grid Service

1. Import the Data Grid service if necessary.
  ```bash
  $ oc login -u system:admin

  $ oc create \
    -n openshift \
    -f https://raw.githubusercontent.com/jboss-container-images/jboss-datagrid-7-openshift-image/f91b94cfd7da4630ca188cd43c26755ecfc99bdd/services/datagrid-service.json
  ```

2. Create a new Data Grid service.
  ```bash
  $ oc login -u developer

  $ oc new-app datagrid-service \
    -p IMAGE=docker-registry.engineering.redhat.com/gzamarre/datagrid72-openshift:JDG-2055 \
    -p NUMBER_OF_INSTANCES=1 \
    -p APPLICATION_USER=test \
    -p APPLICATION_USER_PASSWORD=test
  ```

## Creating a Permanent Cache

1. Run the sample application to connect to the Data Grid service and create a cache.
  ```bash
  $ mvn fabric8:run -Pcreate-cache
  ...
  [INFO] F8: --- Connect to datagrid-service ---
  ...
  [INFO] F8: --- Create cache in datagrid-service ---
  ...
  [INFO] F8: --- Cache 'custom' created ---
  ```

2. Invoke a put/get operation on the cache as follows:
  ```bash
  $ mvn fabric8:run -Pget-cache
  ...
  [INFO] F8: --- Got cache, put/get returned: sample-value ---
  ```

## Verifying that the Cache is Permanent
To verify the cache is permanent, scale the Data Grid service down and then up.

1. Scale the Data Grid stateful set to `0` replicas.
  ```bash
  $ oc get statefulsets
  NAME               DESIRED   CURRENT   AGE
  datagrid-service   1         1         19m

  $ oc scale statefulsets datagrid-service --replicas=0
  statefulset "datagrid-service" scaled

  $ oc get statefulsets
  NAME               DESIRED   CURRENT   AGE
  datagrid-service   0         0         7m
  ```

2. Scale the Data Grid stateful set to `1` replica.
  ```bash
  $ oc scale statefulsets datagrid-service --replicas=1
  statefulset "datagrid-service" scaled

  $ oc get statefulsets
  NAME               DESIRED   CURRENT   AGE
  datagrid-service   1         1         7m
  ```

3. Watch the pod until the Data Grid service starts running.
  ```bash
  $ oc get pods -w
  NAME                 READY     STATUS      RESTARTS   AGE
  ...
  datagrid-service-0   0/1       Running     0          10s
  datagrid-service-0   1/1       Running     0          44s
  ```

4. Check that the sample entry you created still exists in the cache.
  ```bash
  $  mvn fabric8:run -Pget-cache
  ...
  [INFO] F8: --- Got cache, put/get returned: sample-value ---
  ```

## Looking at the Sample Code
As demonstrated in the preceding steps, the sample application creates permanent cache instances with the Data Grid service.

The first step is to instantiate `RemoteCacheManager` to connect to the Data Grid service.

When you call `mvn fabric8:run -Pcreate-cache`, the application generates a random name for the cache and creates it using `RemoteCacheManager`.

```java
RemoteCacheManager remoteCacheManager = ...

RemoteCache<K, V> remoteCache = remoteCacheManager
   .administration()
      .withFlags(CacheContainerAdmin.AdminFlag.PERMANENT)
      .createCache(cacheName, "replicated");
```

`AdminFlag.PERMANENT` creates a permanent cache. If that flag is not included, an ephemeral cache is created.

The remote cache is also created using the `replicated` cache template that is included in the Data Grid service. As a result, the cache is replicated to all nodes in the cluster.
