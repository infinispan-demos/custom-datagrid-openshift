package app;

public class Test {

   public static void main(String[] args) {
      final String name = System.getenv("PARAM");
      System.out.printf("Hello %s!%n", name);
   }

}
