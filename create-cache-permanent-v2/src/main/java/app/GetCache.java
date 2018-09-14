package app;

import oracle.jrockit.jfr.StringConstantPool;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.commons.api.CacheContainerAdmin;

import java.util.stream.IntStream;

public class GetCache {

   public static void main(String[] args) {
      final String svcName = System.getProperty("svcName");

//      String numCallsParam = System.getProperty("numCalls");
//      int numCalls = Integer.parseInt(numCallsParam);

      ConfigurationBuilder cfg =
         Config.create(svcName + "-hotrod", svcName);

      final RemoteCacheManager remote = new RemoteCacheManager(cfg.build());

      final String cacheName = "custom";

      final RemoteCache<String, String> cache = remote.getCache(cacheName);
      cache.put("sample-key", "sample-value");
      final String value = cache.get("sample-key");

      System.out.printf("Got cache, put/get returned: %s", value);
   }

}
