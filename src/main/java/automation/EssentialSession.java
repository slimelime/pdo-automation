package automation;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.NoSuchElementException;

public class EssentialSession implements AutoCloseable {
  private static Logger logger = LoggerFactory.getLogger(EssentialSession.class);

  private WebDriverUtil driverUtil;

  private String essentialUrl = TestConfig.getConfig().getProperty("EssentialUrl");
  private String essentialUserName = TestConfig.getConfig().getProperty("EssentialUserName");
  private String essentialUserPass = TestConfig.getConfig().getProperty("EssentialUserPass");


  private void login() throws Exception {
    driverUtil = TestConfig.createWebDriver();
    logger.info("web driver created");

    // login page
    driverUtil.driver.get(essentialUrl);
    driverUtil.driver.switchTo().activeElement();

    driverUtil.setText(By.id("UserName"), essentialUserName);
    driverUtil.setText(By.id("Password"), essentialUserPass);
    driverUtil.click(By.cssSelector("button.btn-primary"));

    logger.info("user logged in");
  }

  private void chooseBusiness(String businessName) {
   driverUtil.getWait().until(d -> {

     try {
       if (d.findElements(By.id("stats-container")).isEmpty()) {
         d.findElements(By.cssSelector(".business-name a")).stream()
             .filter(e -> e.getText().equalsIgnoreCase(businessName))
             .findFirst().get()
             .click();
         return !d.findElements(By.id("stats-container")).isEmpty();
       }
       return true;
     } catch (Exception e) {
        return false;
     }
   });
  }

  String createInvoice(Invoice invoice) throws Exception {
    login();

    chooseBusiness(invoice.getPaymentMethod() == Invoice.PaymentMethod.BPAY? Invoice.PaymentMethod.PAYCORP.toString(): invoice.getPaymentMethod().toString());

    NumberFormat formatter = NumberFormat.getCurrencyInstance();
    String formatted = formatter.format(new Double(invoice.getAmountInCents())/100).replace("$", "");
    String invoiceNumber = createInvoice(formatted);

    sendEmail(invoiceNumber, invoice.getReceiptEmail());

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
