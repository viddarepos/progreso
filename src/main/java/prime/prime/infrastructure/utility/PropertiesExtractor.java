package prime.prime.infrastructure.utility;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

public class PropertiesExtractor {

  private static final Properties properties;

  static {
    properties = new Properties();
    URL url = PropertiesExtractor.class.getClassLoader().getResource("application.properties");
    try{
      assert url != null;
      properties.load(new FileInputStream(url.getPath()));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static String getProperty(String key){
    return properties.getProperty(key);
  }


}
