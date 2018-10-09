package automation;

import automation.payment.BPayPaymentSession;
import automation.payment.IPayment;
import automation.payment.OnlineCcPaymentSession;
import com.amazonaws.services.s3.AmazonS3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.text.SimpleDateFormat;
import java.util.Calendar;

@Test(groups = "Spike")
public class SpikeTests {
  private static Logger logger = LoggerFactory.getLogger(SpikeTests.class);

  public void s3test() throws Exception {
    logger.info("AWS_ACCESS_KEY_ID=" + System.getenv().getOrDefault("AWS_ACCESS_KEY_ID", "nothing"));
    new S3Service().writeStringToS3("sit-payment-file-validator", "TransactionFileDelivered/bpay/automationSpikeFiles", "test");

  }
}
