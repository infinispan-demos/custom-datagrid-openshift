package app;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;

public class GetCache {

   public static void main(String[] args) {
      final String svcName = "datagrid-service";

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

}
