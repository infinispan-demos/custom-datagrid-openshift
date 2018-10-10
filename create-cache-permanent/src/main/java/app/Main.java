package app;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.commons.api.CacheContainerAdmin;

public class Main {

   public static void main(String[] args) throws Exception {
      final String cmd = System.getenv("CMD");
      final String svcName = System.getenv("NAME");
      
      switch (cmd) {
         case "create-cache":
            createCache(svcName);
            break;
         case "get-cache":
            getCache(svcName);
            break;
         case "destroy-cache":
            destroyCache(svcName);
            break;
         default:
            throw new Exception("Unknown command: " + cmd);
      }
   }

   private static void createCache(String svcName) {
      final String host = svcName + "-hotrod";

      ConfigurationBuilder cfg =
         DatagridCfg.create(host, svcName);

      System.out.printf("--- Connect to %s ---%n", svcName);
      final RemoteCacheManager remote = new RemoteCacheManager(cfg.build());

      final String cacheName = "custom";

      System.out.printf("--- Create cache in %s ---%n", svcName);

      remote.administration()
         .withFlags(CacheContainerAdmin.AdminFlag.PERMANENT)
         .createCache(cacheName, "replicated");

      System.out.printf("--- Cache '%s' created in '%s'   ---%n", cacheName, svcName);
   }

   private static void getCache(String svcName) {
      ConfigurationBuilder cfg =
         DatagridCfg.create(svcName + "-hotrod", svcName);

      System.out.printf("--- Connect to %s ---%n", svcName);
      final RemoteCacheManager remote = new RemoteCacheManager(cfg.build());

      final String cacheName = "custom";

      final RemoteCache<String, String> cache = remote.getCache(cacheName);
      cache.put("sample-key", "sample-value");
      final String value = cache.get("sample-key");

      System.out.printf("--- Got cache, put/get returned: %s ---%n", value);
   }

   private static void destroyCache(String svcName) {
      ConfigurationBuilder cfg =
         DatagridCfg.create(svcName + "-hotrod", svcName);

      System.out.printf("--- Connect to %s ---%n", svcName);
      final RemoteCacheManager remote = new RemoteCacheManager(cfg.build());

      final String cacheName = "custom";

      remote.administration()
         .withFlags(CacheContainerAdmin.AdminFlag.PERMANENT)
         .removeCache(cacheName);

      System.out.printf("--- Cache '%s' destroyed in '%s' ---%n", cacheName, svcName);
   }

}
