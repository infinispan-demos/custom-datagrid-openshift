package app;

import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.commons.api.CacheContainerAdmin;

public class CreateCache {

   public static void main(String[] args) {
      final String svcName = "datagrid-service";
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

      System.out.printf("--- Cache '%s' created ---%n", cacheName);
   }

}
