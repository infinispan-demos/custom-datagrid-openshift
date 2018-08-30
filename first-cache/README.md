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

$ curl http://app-myproject.127.0.0.1.nip.io/connect/caching-service-app
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

$ curl http://app-myproject.127.0.0.1.nip.io/connect/caching-service-app
Infinispan connection successful

$ curl http://app-myproject.127.0.0.1.nip.io/get-cache
...
```

# TODO

Test cache metadata survival in multi node environment.
If using replicated caches, invocations are round robing.
So with N pods, you could try to get the cache and do a get N times. 
