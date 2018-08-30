# My First Cache

This demo shows how to get started with the caching service:

```bash
oc cluster up
...

```


# Test out application

```bash
$ ./first-deploy.sh
...

$ curl http://app-myproject.127.0.0.1.nip.io/connect/caching-service-app/caching-service
Infinispan connection successful

$ curl http://app-myproject.127.0.0.1.nip.io/create-cache 
Cache f62e4b80-90ca-44ec-9085-231dd9b60335-0 created

$ curl http://app-myproject.127.0.0.1.nip.io/get-cache/1
Got cache, put/get returned: sample-value
```

After making changes to the application, only `./deploy.sh` needs to be called unless dependencies are changed.
If changing dependencies, you should call `./first-deploy.sh` again instead.


# Test cache metadata survival

## 1 node -> 1 node

After testing out the application, try:

```bash
$ oc get statefulsets
NAME                  DESIRED   CURRENT   AGE
caching-service-app   1         1         3d

$ oc scale statefulsets caching-service-app --replicas=0
statefulset.apps "caching-service-app" scaled

$ oc scale statefulsets caching-service-app --replicas=1
statefulset.apps "caching-service-app" scaled

$ curl http://app-myproject.127.0.0.1.nip.io/connect/caching-service-app/caching-service
Infinispan connection successful

$ curl http://app-myproject.127.0.0.1.nip.io/get-cache/1
Got cache, put/get returned: sample-value
```

## 2 nodes -> 2 nodes 

Start by deleting any created caches during the session:

```bash
$ curl http://app-myproject.127.0.0.1.nip.io/destroy-cache
Cache e8814aa2-f11f-4f64-baa5-e34feeb6baa0-0 destroyed
```

Next, try out and make sure it works:

```bash
$ oc scale statefulsets caching-service-app --replicas=2
statefulset.apps "caching-service-app" scaled

$ curl http://app-myproject.127.0.0.1.nip.io/connect/caching-service-app/caching-service
Infinispan connection successful

$ curl http://app-myproject.127.0.0.1.nip.io/create-cache 
Cache f62e4b80-90ca-44ec-9085-231dd9b60335-0 created

$ curl http://app-myproject.127.0.0.1.nip.io/get-cache/2
Got cache, 2 calls to put/get returned: [sample-value, sample-value]

$ oc scale statefulsets caching-service-app --replicas=0
statefulset.apps "caching-service-app" scaled

$ oc scale statefulsets caching-service-app --replicas=2
statefulset.apps "caching-service-app" scaled

$ curl http://app-myproject.127.0.0.1.nip.io/connect/caching-service-app/caching-service
Infinispan connection successful

$ curl http://app-myproject.127.0.0.1.nip.io/get-cache/2
Got cache, 2 calls to put/get returned: [sample-value, sample-value]
```

## 1 nodes -> 2 nodes 

Start by deleting any created caches during the session:

```bash
$ curl http://app-myproject.127.0.0.1.nip.io/destroy-cache
Cache e8814aa2-f11f-4f64-baa5-e34feeb6baa0-0 destroyed
```

Next, try out and make sure it works:

```bash
$ oc scale statefulsets caching-service-app --replicas=1
statefulset.apps "caching-service-app" scaled

$ curl http://app-myproject.127.0.0.1.nip.io/connect/caching-service-app/caching-service
Infinispan connection successful

$ curl http://app-myproject.127.0.0.1.nip.io/create-cache 
Cache f62e4b80-90ca-44ec-9085-231dd9b60335-0 created

$ curl http://app-myproject.127.0.0.1.nip.io/get-cache/2
Got cache, 2 calls to put/get returned: [sample-value, sample-value]

$ oc scale statefulsets caching-service-app --replicas=0
statefulset.apps "caching-service-app" scaled

$ oc scale statefulsets caching-service-app --replicas=2
statefulset.apps "caching-service-app" scaled

$ curl http://app-myproject.127.0.0.1.nip.io/connect/caching-service-app/caching-service
Infinispan connection successful

$ curl http://app-myproject.127.0.0.1.nip.io/get-cache/2
Got cache, 2 calls to put/get returned: [sample-value, sample-value]
```
