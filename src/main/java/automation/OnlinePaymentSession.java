package automation;

import org.openqa.selenium.By;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class OnlinePaymentSession implements AutoCloseable {
  private WebDriverUtil driverUtil;

  void payInvoice(String invoiceLink, String type, String amountInCents) throws Exception {
    driverUtil = TestConfig.createWebDriver();
    driverUtil.driver.navigate().to(invoiceLink);

    if ("credit card".equalsIgnoreCase(type)) {
      driverUtil.click(By.id("payment-credit-card"));
      payCreditCard();
    } else {
      driverUtil.click(By.id("payment-bpay"));
      String reference = driverUtil.getText(By.cssSelector("span[analytics-label='ref text']"));
      payBpay(amountInCents, reference);
    }
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

  private void payBpay(String amountInCents, String referenceCode) throws Exception {
    String fileContent = "01,CBABPAY,MYOBDSRV,$Today,0930,1,,,2/\n"
        + "02,848283,CBA,1,$Yesterday,,,3/\n"
        + "03,310710102355,,231,1000,2,,250,,0,,550,0,0,/\n"
        + "30,399,$AmountInCents,0,$ReferenceCode,$Comments,0,05,101,$Yesterday,111601,004,,,,,,,,,/\n"
        + "49,2000,3/\n"
        + "98,2000,1,5/\n"
        + "99,2000,1,7/";

    SimpleDateFormat format = new SimpleDateFormat("YYYYMMDD");
    Calendar cal = Calendar.getInstance();
    String today = new SimpleDateFormat("YYYYMMDDmmssSSS").format(cal.getTime());
    fileContent = fileContent.replaceAll("\\$Today", today.substring(0, 8));
    fileContent = fileContent.replaceAll("\\$Comments", "ABCD" + today);
    cal.add(Calendar.DATE, -1);
    fileContent = fileContent.replaceAll("\\$Yesterday", format.format(cal.getTime()));

    fileContent = fileContent.replaceAll("\\$AmountInCents", amountInCents);
    fileContent = fileContent.replaceAll("\\$ReferenceCode", referenceCode);

    PrintWriter out = new PrintWriter(String.format("BPAY-AUTO-%s.pgp", today));
    out.print(fileContent);
    out.close();

    // upload to S3
    throw new Exception("Not implemented BPAY");
  }

  @Override public void close() throws Exception {
    if (driverUtil != null) driverUtil.close();
  }
}
