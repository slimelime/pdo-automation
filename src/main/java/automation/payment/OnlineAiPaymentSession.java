package automation.payment;

import automation.Invoice;
import automation.S3Service;
import automation.TestConfig;
import automation.WebDriverUtil;
import org.openqa.selenium.By;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class OnlineAiPaymentSession implements IPayment {
  private WebDriverUtil driverUtil;
  private static Logger logger = LoggerFactory.getLogger(OnlineAiPaymentSession.class);
  private String ccTransactionFileS3Bucket = TestConfig.getConfig().getProperty("ccTransactionFileS3Bucket");
  private String deFileS3Bucket = TestConfig.getConfig().getProperty("deFileS3Bucket");
  private String deAckFileS3Bucket = TestConfig.getConfig().getProperty("deAckFileS3Bucket");

  private static String UUID = "37375cb8-c17d-4947-8c0e-d9ab086fa6e6";
  private static String MerchantId="MYOBRy-1435112";
  private static String BusinessId="62895";

  S3Service s3 = new S3Service();

  private String reference="";

  public void payInvoice(Invoice invoice) throws Exception {
    driverUtil = TestConfig.createWebDriver();
    driverUtil.driver.navigate().to(invoice.getInvoiceLink());

    driverUtil.click(By.id("payment-credit-card"));
    payCreditCard(invoice);

    // now start settlement

  }

  private void payCreditCard(Invoice invoice) throws IOException {

    driverUtil.getElement(By.id("credit-card-payment-frame"));
    driverUtil.driver.switchTo().frame("credit-card-payment-frame");

    NumberFormat formatter = NumberFormat.getCurrencyInstance();
    String formatted = formatter.format(new Double(invoice.getAmountInCents())/100).replace("$", "$ ");

    assert(driverUtil.getText(By.id("MYOBTotalAmount")).trim().equalsIgnoreCase(String.format("Total %s", formatted)));

    driverUtil.setText(By.id("CardName"), "John Watson");
    driverUtil.setText(By.id("CardNumber"), "4976000000003436");
    driverUtil.setText(By.id("ExpiryDateMonthList"), "12");
    driverUtil.setText(By.id("ExpiryDateYearList"), "2020");
    driverUtil.setText(By.id("CV2"), "452");

    driverUtil.click(By.id("SubmitButton"));

    // verify it's successfully
    driverUtil.getElement(new By.ByCssSelector("span.payment-success-msg"));

    // get transaction reference
    reference = driverUtil.driver.findElements(By.cssSelector(".paymentreceipt .detail__field")).stream()
        .filter(e -> e.findElement(By.cssSelector(".detail__field__title")).getText().contains("Reference Number"))
        .findFirst().get().findElement(By.cssSelector(".detail__field__value")).getText();


    List<String> currentDeFiles = getDeFiles();

    generateTransactionFile(invoice);

    logger.info("checking DE file is generated");

    StringBuffer expectAmount = new StringBuffer("");
    for(int i=0; i<11-String.valueOf(invoice.getAmountInCents()).length(); i++) {
      expectAmount.append("0");
    }
    expectAmount.append(String.valueOf(invoice.getAmountInCents()));

    String expectDe = String.format("062-161   123456 5%stest account", expectAmount.toString());
    List<String> deFile = (List<String>) TestConfig.getWait().withTimeout(Duration.ofMinutes(10)).pollingEvery(Duration.ofSeconds(5)).withMessage("Getting DE "
        + "file")
        .until(s -> {
              List<String> deFiles = getDeFiles();
              deFiles.removeAll(currentDeFiles);
              List<String> rtn = deFiles.stream().filter(k ->
                  s3.getObjectContent(deFileS3Bucket.split(":")[0], k).get()
                      .contains(expectDe))
                  .collect(Collectors.toList());

              if (rtn.isEmpty()) return null;
              else return rtn;
            }
        );

    String[] deFileNames = deFile.get(0).split("/");
    generateBankDeAck(s3, deFileNames[deFileNames.length-1]);

    // now status should be paid
    logger.info("AI payment done");
  }

  private void generateTransactionFile(Invoice invoice) throws IOException {
    String ccSettlement = String.format("\"CrossReference\",\"PublicUID\",\"GatewayRouteID\",\"MerchantAccountID\",\"MerchantID\",\"BankTransactionID\",\"CardType\","
         + "\"TransactionDateTime\",\"Amount\",\"CurrencyShort\",\"OrderID\",\"ClientReference\",\"AuthCode\",\"DynamicDescriptor\","
         + "\"TransactionType\",\"CardNumberFirstSix\",\"CardNumberLastFour\",\"ExpiryDate\",\"Acquirer\",\"TerminalID\","
         + "\"CaptureEnvironment\"\n"
         + "\"%s\",\"%s\",\"NAB-ECOM-AUD\",\"TESTMID1-NAB-ECOM-AUD-2-180314110537\","
         + "\"%s\",\"%s\",\"VISA\",\"%s\",\"%s\",\"AUD\",\"%s\",\"%s\","
         + "\"279054\",\"\",\"SALE\",\"497635\",\"6891\",\"06/21\",\"NAB\",\"\",\"ECOM\"",
        reference, UUID, MerchantId, new SimpleDateFormat("YYYYMMddhhmmss").format(new Date()),
        new SimpleDateFormat("dd/MM/YYYY hh:mm:ss").format(new Date()),
        invoice.getAmountInCents(),
        invoice.getInvoiceNumber(), BusinessId);

    String today = new SimpleDateFormat("YYYYMMdd").format(new Date());
    String ccFile = String.format(ccTransactionFileS3Bucket.split(":")[1], today, invoice.getInvoiceNumber());

    logger.info("uploading Cc Transaction file to S3: " + ccFile);
    s3.writeStringToS3(ccTransactionFileS3Bucket.split(":")[0], ccFile, ccSettlement);
  }

  private List<String> getDeFiles() {
    String today = new SimpleDateFormat("YYYYMMdd").format(Calendar.getInstance().getTime());
    return s3.listObjects(deFileS3Bucket.split(":")[0], String.format(deFileS3Bucket.split(":")[1], today.substring(0,6), today));
  }

  private void generateBankDeAck(S3Service s3, String deFileName) throws IOException {

    String timestamp = new SimpleDateFormat("YYYYMMddmmssSSS").format(Calendar.getInstance().getTime());
    String today = timestamp.substring(0, 8);

    String fileContent = String.format("01,CommBiz Status Message,431049710805,%s,11:25,MYOB Automation(100351219),02\n"
        + "02,437603591556,CBZ100351219::101005206,%s,%s,0.12,0.12,2\n"
        + "99,1,0,1", new SimpleDateFormat("dd/MM/YY").format(Calendar.getInstance().getTime()), deFileName, today);

    s3.writeStringToS3(deAckFileS3Bucket.split(":")[0], String.format(deAckFileS3Bucket.split(":")[1], today.substring(0,6), today, deFileName), fileContent);
  }
  @Override public void close() throws Exception {
    if (driverUtil != null) driverUtil.close();
  }
}
