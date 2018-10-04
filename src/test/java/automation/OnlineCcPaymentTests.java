package automation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

@Test(groups = "OnlineCcPayment")
public class OnlineCcPaymentTests {
  private static Logger logger = LoggerFactory.getLogger(OnlineCcPaymentTests.class);

  public void ccPayment() throws Exception {
    int amountInDollor = 10;

    // setup an email account
    try (TenMinutEmailService email = new TenMinutEmailService()) {

      String emailAddress = email.getNewEmailAddress();
      logger.info("email address is " + emailAddress);

      // create invoice
      try (EssentialSession es = new EssentialSession()) {
        String invoiceNumber = es.createInvoice(amountInDollor, emailAddress);
        logger.info("invoice number is " + invoiceNumber);
        // get invoice email and open invoice to pay
        String link = email.getInvoiceLink();
        logger.info("online invoice link is " + link);
        try (OnlinePaymentSession payment = new OnlinePaymentSession()) {
          payment.payInvoice(link, "credit card", String.valueOf(amountInDollor * 100));
          logger.info("invoice payment is made");
        }
        // check invoice status
        es.verifyInvoiceStatus(invoiceNumber, "Paid");
      }
    }
  }
}
