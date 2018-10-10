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

public class BPayPaymentSession implements IPayment {
  private WebDriverUtil driverUtil;
  private static Logger logger = LoggerFactory.getLogger(BPayPaymentSession.class);
  private String bpayReference;

  private String transactionFileS3Bucket = TestConfig.getConfig().getProperty("transactionFileS3Bucket");
  private String deFileS3Bucket = TestConfig.getConfig().getProperty("deFileS3Bucket");
  private String deAckFileS3Bucket = TestConfig.getConfig().getProperty("deAckFileS3Bucket");

  S3Service s3 = new S3Service();


  public void payInvoice(Invoice invoice) throws Exception {
    driverUtil = TestConfig.createWebDriver();
    driverUtil.driver.navigate().to(invoice.getInvoiceLink());

    driverUtil.click(By.id("payment-bpay"));
      bpayReference = driverUtil.getText(By.cssSelector("span[analytics-label='ref text']"));
      logger.info("Paying BPAY with reference =" + bpayReference);
      invoice.setProperty("BPAYReference", bpayReference);
      payBpay(invoice);
  }

  private void payBpay(Invoice invoice) throws Exception {
    String invoiceNumber = invoice.getInvoiceNumber();

    Calendar cal = Calendar.getInstance();
    String timestamp = new SimpleDateFormat("YYYYMMddmmssSSS").format(cal.getTime());
    String today = timestamp.substring(0, 8);


    List<String> currentDeFiles = getDeFiles();

    generateTransactionFile(invoice);

    logger.info("checking DE file is generated");
    List<String> deFile = (List<String>) TestConfig.getWait().withTimeout(Duration.ofMinutes(10)).pollingEvery(Duration.ofSeconds(5)).withMessage("Getting DE "
        + "file")
        .until(s -> {
              List<String> deFiles = getDeFiles();
              deFiles.removeAll(currentDeFiles);
              List<String> rtn = deFiles.stream().filter(k ->
                  s3.getObjectContent(deFileS3Bucket.split(":")[0], k).get()
                  .contains(invoice.getInvoiceNumber()))
                  .collect(Collectors.toList());

              if (rtn.isEmpty()) return null;
              else return rtn;
            }
        );

    String[] deFileNames = deFile.get(0).split("/");
    generateBankDeAck(s3, deFileNames[deFileNames.length-1]);

    // now status should be paid
    logger.info("Bpay payment done");
  }

  private List<String> getDeFiles() {
   String today = new SimpleDateFormat("YYYYMMdd").format(Calendar.getInstance().getTime());
   return s3.listObjects(deFileS3Bucket.split(":")[0], String.format(deFileS3Bucket.split(":")[1], today.substring(0,6), today));
  }

  private void generateTransactionFile(Invoice invoice) throws IOException {
    String amountInCents = String.valueOf(invoice.getAmountInCents());

    String fileContent = "01,CBABPAY,MYOBDSRV,$Today,0930,1,,,2/\n"
        + "02,848283,CBA,1,$Yesterday,,,3/\n"
        + "03,310710102355,,231,1000,2,,250,,0,,550,0,0,/\n"
        + "30,399,$AmountInCents,0,$ReferenceCode,$Comments,0,05,101,$Yesterday,111601,004,,,,,,,,,/\n"
        + "49,2000,3/\n"
        + "98,2000,1,5/\n"
        + "99,2000,1,7/";

    SimpleDateFormat format = new SimpleDateFormat("YYYYMMdd");
    Calendar cal = Calendar.getInstance();
    String timestamp = new SimpleDateFormat("YYYYMMddmmssSSS").format(cal.getTime());
    String today = timestamp.substring(0, 8);

    fileContent = fileContent.replaceAll("\\$Today", today);
    fileContent = fileContent.replaceAll("\\$Comments", "ABCD" + timestamp);
    cal.add(Calendar.DATE, -1);
    fileContent = fileContent.replaceAll("\\$Yesterday", format.format(cal.getTime()));

    fileContent = fileContent.replaceAll("\\$AmountInCents", amountInCents);
    fileContent = fileContent.replaceAll("\\$ReferenceCode", bpayReference);

    String bpayfile = String.format(transactionFileS3Bucket.split(":")[1], today, bpayReference);

    logger.info("uploading BPAY file to S3: " + bpayfile);
    s3.writeStringToS3(transactionFileS3Bucket.split(":")[0], bpayfile, fileContent);
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
