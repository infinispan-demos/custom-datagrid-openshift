package app;

import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.commons.api.CacheContainerAdmin;

public class CreateCache {

   public static void main(String[] args) {
      final String svcName = System.getProperty("svcName");
      final String host = svcName + "-hotrod-myproject.127.0.0.1.nip.io";

      ConfigurationBuilder cfg =
         Config.create(host, svcName);

      final RemoteCacheManager remote = new RemoteCacheManager(cfg.build());

      final String cacheName = "custom";

      remote.administration()
         .withFlags(CacheContainerAdmin.AdminFlag.PERMANENT)
         .createCache(cacheName, "replicated");

      System.out.printf("Cache %s created", cacheName);
   }

}
