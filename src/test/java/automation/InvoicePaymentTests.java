package automation;

import automation.payment.BPayPaymentSession;
import automation.payment.IPayment;
import automation.payment.OnlineCcPaymentSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

@Test(groups = "OnlineCcPayment")
public class InvoicePaymentTests {
  private static Logger logger = LoggerFactory.getLogger(InvoicePaymentTests.class);

  public void ccPayment() throws Exception {
    Invoice invoice = new Invoice();
    invoice.setAmountInCents(1000);
    invoice.setPaymentMethod("credit card");

    makePayment(invoice);
  }

  public void bpayPayment() throws Exception {
    Invoice invoice = new Invoice();
    invoice.setAmountInCents(1100);
    invoice.setPaymentMethod("BPAY");

    makePayment(invoice);
  }

  private void makePayment(Invoice invoice) throws Exception {

    // setup an email account
    try (TenMinutEmailService email = new TenMinutEmailService()) {

      String emailAddress = email.getNewEmailAddress();
      logger.info("email address is " + emailAddress);
      invoice.setReceiptEmail(emailAddress);
      // create invoice
      try (EssentialSession es = new EssentialSession()) {
        String invoiceNumber = es.createInvoice(invoice);
        logger.info("invoice number is " + invoiceNumber);
        invoice.setInvoiceNumber(invoiceNumber);
        // get invoice email and open invoice to pay
        invoice.setInvoiceLink(email.getInvoiceLink());
        logger.info("online invoice link is " + invoice.getInvoiceLink());

        try (IPayment payment = "credit card".equalsIgnoreCase(invoice.getPaymentMethod())
                              ? new OnlineCcPaymentSession()
                              : new BPayPaymentSession()) {

         payment.payInvoice(invoice);
         logger.info("invoice payment is made");
        }
        // check invoice status
        es.verifyInvoiceStatus(invoiceNumber, "Paid");
      }
    }
  }



}
