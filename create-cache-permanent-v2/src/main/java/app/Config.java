package app;

import org.infinispan.client.hotrod.configuration.ClientIntelligence;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.configuration.SaslQop;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

class Config {

   private static final char[] TRUSTSTORE_PASSWORD = "secret".toCharArray();
   private static final String TRUSTSTORE_PATH = "target/truststore.pkcs12";

   private Config() {
   }

   static ConfigurationBuilder create(String host, String saslName) {
      TrustStore.createFromCmdLine(TRUSTSTORE_PATH, TRUSTSTORE_PASSWORD);

      ConfigurationBuilder cfg = new ConfigurationBuilder();

      cfg
         .addServer()
            .host(host)
            .port(443)
         .security().authentication()
            .enable()
            .username("test")
            .password("test")
            .realm("ApplicationRealm")
            .serverName(saslName)
            .saslMechanism("DIGEST-MD5")
            .saslQop(SaslQop.AUTH)
         .ssl()
            .enable()
            .sniHostName(host)
            .trustStoreFileName(TRUSTSTORE_PATH)
            .trustStorePassword(TRUSTSTORE_PASSWORD);

      cfg = workaroundDockerForMac(cfg);

      return cfg;
   }

   private static ConfigurationBuilder workaroundDockerForMac(ConfigurationBuilder cfg) {
      String osName = System.getProperty("os.name").toLowerCase();
      boolean isMacOs = osName.startsWith("mac os x");
      return isMacOs
         ? cfg.clientIntelligence(ClientIntelligence.BASIC)
         : cfg;
   }

   private static void createTruststoreFromCrtFile(String crtPath, String tsPath, char[] password) {
      createTruststore(parseCrtFile(crtPath), tsPath, password);
   }

   private static void createTruststore(List<String> certs, String path, char[] password) {
      try {
         try (FileOutputStream output = new FileOutputStream(path)) {
            KeyStore trustStore = KeyStore.getInstance("PKCS12");
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            trustStore.load(null, null);

            for (int i = 0; i < certs.size(); i++) {
               String alias = i < 10 ? "service-crt-0" : "service-crt-";
               String cert = certs.get(i);
               try (InputStream input =
                       Base64.getDecoder().wrap(new ByteArrayInputStream(cert.getBytes(StandardCharsets.UTF_8)))) {
                  Certificate certificate = cf.generateCertificate(input);
                  trustStore.setCertificateEntry(alias + i, certificate);
               }
            }
            trustStore.store(output, password);
         }
      } catch (Exception e) {
         throw new RuntimeException(e);
      }
   }

   private static List<String> parseCrtFile(String path) {
      try {
         List<String> certs = new ArrayList<>();
         StringBuilder sb = new StringBuilder();
         for (String line : Files.readAllLines(Paths.get(path))) {
            if (line.isEmpty() || line.contains("BEGIN CERTIFICATE"))
               continue;

            if (line.contains("END CERTIFICATE")) {
               certs.add(sb.toString());
               sb.setLength(0);
            } else {
               sb.append(line);
            }
         }
         return certs;
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

}
