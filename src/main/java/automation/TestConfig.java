package automation;

import org.openqa.selenium.support.ui.FluentWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Properties;

/**
 * provide test data management.
 */
public class TestConfig {
  static Logger logger = LoggerFactory.getLogger(TestConfig.class);
  private static Properties config = null;

  public synchronized static Properties getConfig() {
    if (config == null) {
      config = new Properties();
      try (InputStream inputStream = TestConfig.class.getResourceAsStream("/test.properties")) {
        config.load(inputStream);
      } catch (FileNotFoundException e) {
        logger.warn("Can't find config file", e);
      } catch (IOException e) {
        logger.warn("Error reading config file", e);
      }
    }
    return config;
  }

  public static FluentWait getWait(Object o) {
    FluentWait<Object> wait = new FluentWait<Object>(o)
        .withTimeout(Duration.ofSeconds(60))
        .pollingEvery(Duration.ofSeconds(1));

    return wait;
  }
}
