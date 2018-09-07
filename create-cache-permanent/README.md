# Create Cache Permanent

This demo shows how to create a cache permanently using the datagrid service.
By making it permanent, the cache only needs to be created once and its definition survive complete data grid restarts.
Creating a cache permanently does not imply that its data will be persisted.
To achieve that, the cache needs to be configured with a persistent store.

To run this demo, start OpenShift first, then load and instantiate the template with one instance:

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

Next, use the sample application provided to connect to the data grid and create a cache.
The application generates a random name for the created cache, which the invocation to `create-cache` returns:

```bash
$ ./first-deploy.sh
...

$ curl http://app-myproject.127.0.0.1.nip.io/connect/datagrid-service
Infinispan connection successful

$ curl http://app-myproject.127.0.0.1.nip.io/create-cache 
Cache f62e4b80-90ca-44ec-9085-231dd9b60335-0 created
```

Once the cache is created, make a put/get invocation to the cache:

```bash
$ curl http://app-myproject.127.0.0.1.nip.io/get-cache/1
Got cache, 1 calls to put/get returned: [sample-value]
```

Next, verify that the cache definition survives a complete restart.
To do that, scale down the data grid stateful set to 0 replicas.
Then, scale up the data grid stateful set to 1 replica and wait for the pod to be ready.

```bash
$ oc get statefulsets
NAME               DESIRED   CURRENT   AGE
datagrid-service   1         1         19m

$ oc scale statefulsets datagrid-service --replicas=0
statefulset.apps "datagrid-service" scaled

$ oc scale statefulsets datagrid-service --replicas=1
statefulset.apps "datagrid-service" scaled
```

You can verify check when the pod is ready by getting a continuous stream of pod events, e.g.

```bash
$ oc get pods -w                                                                                                                                                master ⬆ ✱ ◼
NAME                 READY     STATUS      RESTARTS   AGE
app-1-build          0/1       Completed   0          2m
app-1-flvz6          1/1       Running     0          1m
datagrid-service-0   0/1       Running     0          10s
datagrid-service-0   1/1       Running   0         59s
```

Once the pod is ready, make a put/get invocation to the cache.
If the cache definition was made permanent, this invocation should be successful:

```bash
$ curl http://app-myproject.127.0.0.1.nip.io/get-cache/1
Got cache, 1 calls to put/get returned: [sample-value]
```

You can make multiple invocations in one by changing the number at the end of URL, e.g.

```bash
$ curl http://app-myproject.127.0.0.1.nip.io/get-cache/2
Got cache, 2 calls to put/get returned: [sample-value, sample-value]
```

This can be useful to verify the same behaviour in a multi-node environment.
Since the cache created is replicated, invocations are round robin so it distributes calls around the cluster.

If making changes to the application, only `./deploy.sh` needs to be called unless dependencies are changed.
If changing dependencies, you should call `./first-deploy.sh` again instead.
