package automation;

import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.FluentWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.Properties;

/**
 * provide test data management.
 */
public class TestConfig {
  private static Logger logger = LoggerFactory.getLogger(TestConfig.class);
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

  public static FluentWait getWait() {
    return new FluentWait<Object>("")
        .withTimeout(Duration.ofSeconds(Integer.parseInt(getConfig().getProperty("WebDriverTimeout", "30"))))
        .pollingEvery(Duration.ofSeconds(1));
  }

  public static WebDriverUtil createWebDriver() throws MalformedURLException {
    int timeout = Integer.parseInt(TestConfig.getConfig().getProperty("WebDriverTimeout", "30"));

    if (TestConfig.getConfig().getProperty("webdriver.chrome.driver") != null) {
      System.setProperty("webdriver.chrome.driver", TestConfig.getConfig().getProperty("webdriver.chrome.driver"));
      ChromeDriver driver = new ChromeDriver();
      return new WebDriverUtil(driver, timeout);
    } else {
      String remoteWebDriverUrl = TestConfig.getConfig().getProperty("WebDriverUrl");
      DesiredCapabilities capabilities = DesiredCapabilities.chrome();
      RemoteWebDriver driver = new RemoteWebDriver(new URL(remoteWebDriverUrl), capabilities);
      return new WebDriverUtil(driver, timeout);
    }
  }
}
