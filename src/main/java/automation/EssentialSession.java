package automation;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.util.List;

public class EssentialSession implements AutoCloseable {
  private static Logger logger = LoggerFactory.getLogger(EssentialSession.class);

  private WebDriverUtil driverUtil;

  private String essentialUrl = TestConfig.getConfig().getProperty("EssentialUrl");
  private String essentialUserName = TestConfig.getConfig().getProperty("essentialUserName");
  private String essentialUserPass = TestConfig.getConfig().getProperty("essentialUserPass");


  EssentialSession() throws Exception {
    driverUtil = TestConfig.createWebDriver();
    logger.info("web driver created");

    login();
  }

  private void login() throws Exception {
    // login page
    driverUtil.driver.get(essentialUrl);
    driverUtil.driver.switchTo().activeElement();

    driverUtil.setText(By.id("UserName"), essentialUserName);
    driverUtil.setText(By.id("Password"), essentialUserPass);
    driverUtil.click(By.cssSelector("button.btn-primary"));

    logger.info("user logged in");

    chooseBusiness();
  }

  private void chooseBusiness() {
    // choose the first business name
    driverUtil.clickUtil(By.cssSelector(".business-name a"), By.id("stats-container"));
  }

  String createInvoice(int amountInDollor, String emailAddress) throws Exception {
    DecimalFormat df2 = new DecimalFormat(".##");
    String invoiceNumber = createInvoice(df2.format(amountInDollor));

    sendEmail(invoiceNumber, emailAddress);

    return invoiceNumber;
  }

  void verifyInvoiceStatus(String invoiceNumber, String status) {
    driverUtil.driver.navigate().refresh();

    driverUtil.setText(By.id("search"), invoiceNumber);

    driverUtil.getWait().until(d -> {
      try {
        return d.findElements(By.cssSelector("tbody tr")).stream().filter(r -> {
          List<WebElement> fields = r.findElements(By.tagName("td"));
          return fields.size() > 1
              && fields.stream().filter(f -> f.getText().equalsIgnoreCase(invoiceNumber)).count() == 1
              && fields.stream().filter(f -> f.getText().equalsIgnoreCase(status)).count() == 1;
        }).count() == 1;
      } catch (Exception e) {
        return false;
      }
    });
  }

  private String createInvoice(String price) {
    driverUtil.click(By.cssSelector("a[data-automation='sales_group']"));
    driverUtil.click(By.cssSelector("a[data-automation='sales_invoices']"));

    driverUtil.clickUtil(By.id("createButton"), By.id("contactId"));

    // fill in forms
    driverUtil.setText(By.id("contactId"), "ABC");
    driverUtil.click(By.linkText("ABC"));

    driverUtil.setText(By.cssSelector("textarea[name='description']"), "autoItem");
    driverUtil.setText(By.cssSelector("input[name='accountId']"), "Sales 1");
    driverUtil.click(By.linkText("Sales 1"));

    driverUtil.setText(By.cssSelector("input[name='unitPrice']"), price);

    return driverUtil.getText(By.id("invoiceNumber"));

  }


  private void sendEmail(String invoiceId, String emailAddress) {
    // send email
    driverUtil.clickUtil(By.id("email"), By.id("email-tokenfield"));

    driverUtil.driver.findElements(By.cssSelector("div#to a.close")).forEach(WebElement::click);
    driverUtil.setText(By.id("email-tokenfield"), emailAddress + "\n");
    driverUtil.click(By.id("emailButton"));

    System.out.println("invoiceId=" + invoiceId);

    driverUtil.clickUtil(By.id("save"), By.id("search"));

    verifyInvoiceStatus(invoiceId, "Not paid");
  }

  @Override public void close() throws Exception {
    if (driverUtil != null) driverUtil.close();
  }
}
