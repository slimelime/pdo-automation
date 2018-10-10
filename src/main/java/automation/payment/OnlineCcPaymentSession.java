package automation.payment;

import automation.Invoice;
import automation.S3Service;
import automation.TestConfig;
import automation.WebDriverUtil;
import org.openqa.selenium.By;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

public class OnlineCcPaymentSession implements IPayment {
  private WebDriverUtil driverUtil;
  private static Logger logger = LoggerFactory.getLogger(OnlineCcPaymentSession.class);

  public void payInvoice(Invoice invoice) throws Exception {
    driverUtil = TestConfig.createWebDriver();
    driverUtil.driver.navigate().to(invoice.getInvoiceLink());

    driverUtil.click(By.id("payment-credit-card"));
    payCreditCard();
  }

  private void payCreditCard() {
    driverUtil.getElement(By.id("credit-card-payment-frame"));
    driverUtil.driver.switchTo().frame("credit-card-payment-frame");

    driverUtil.setText(By.id("cardHolderName"), "pdoTest");
    driverUtil.setText(By.id("cardNo"), "4564456445644564");
    driverUtil.setText(By.id("cardSecureId"), "123");
    driverUtil.setText(By.id("cardExpiry"), "10/20");
    driverUtil.click(By.cssSelector("button[type='submit'].btn-primary"));

    // verify it's successfully
    driverUtil.getElement(new By.ByCssSelector("span.payment-success-msg"));

  }

  @Override public void close() throws Exception {
    if (driverUtil != null) driverUtil.close();
  }
}
