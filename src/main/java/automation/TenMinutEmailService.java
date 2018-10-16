package automation;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;

import java.net.MalformedURLException;

public class TenMinutEmailService implements AutoCloseable {
  private WebDriverUtil driverUtil;


  TenMinutEmailService() throws MalformedURLException {
    driverUtil = TestConfig.createWebDriver();
  }

  String getNewEmailAddress() {
    driverUtil.driver.get("https://10minutemail.com/");
    return driverUtil.getText(By.cssSelector(".mail-address-address"));
  }

  String getInvoiceLink() {
   return driverUtil.getWait(60*5)
       .ignoring(TimeoutException.class)
       .withMessage("can't get invoice link")
       .until(d -> {
          driverUtil.clickUtil(By.cssSelector("div#messagesList h3"), By.partialLinkText("Pay now"), 1);
          return driverUtil.getElement(By.partialLinkText("Pay now")).getAttribute("href");
        });
  }

  @Override public void close() throws Exception {
    if (driverUtil != null) driverUtil.close();
  }
}
