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

$ curl http://app-myproject.127.0.0.1.nip.io/get-cache
Got cache, put/get returned: sample-value
```

After making changes to the application, only `./deploy.sh` needs to be called unless dependencies are changed.
If changing dependencies, you should call `./first-deploy.sh` again instead.


# Test cache metadata survival

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

$ curl http://app-myproject.127.0.0.1.nip.io/get-cache
Got cache, put/get returned: sample-value
```

# Test cache metadata survival on multi-node

Start by deleting any created caches during the session:

```bash
$ curl http://app-myproject.127.0.0.1.nip.io/destroy-cache
Cache e8814aa2-f11f-4f64-baa5-e34feeb6baa0-0 destroyed
```

TODO 1: 
Scale service to 2 nodes
Create a cache (repl)
Exercise 2 times
Scale down to 0
Scale up to 2 nodes
Exercise 2 times

TODO 1:
Scale service 1 node
Create a cache (repl)
Exercise 1 times
Scale down to 0
Scale up to 2 nodes
Exercise 2 times

Test cache metadata survival in multi node environment.
If using replicated caches, invocations are round robing.
So with N pods, you could try to get the cache and do a get N times. 
