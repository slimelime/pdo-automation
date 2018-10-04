package automation;

import org.testng.annotations.Test;

@Test(groups = "OnlineCcPayment")
public class OnlineCcPaymentTests {

  public void ccPayment() throws Exception {
    int amountInDollor = 10;

    // setup an email account
    try (TenMinutEmailService email = new TenMinutEmailService()) {

      String emailAddress = email.getNewEmailAddress();

      // create invoice
      try (EssentialSession es = new EssentialSession()) {
        String invoiceNumber = es.createInvoice(amountInDollor, emailAddress);

        // get invoice email and open invoice to pay
        String link = email.getInvoiceLink();
        try (OnlinePaymentSession payment = new OnlinePaymentSession()) {
          payment.payInvoice(link, "credit card", String.valueOf(amountInDollor * 100));
        }
        // check invoice status
        es.verifyInvoiceStatus(invoiceNumber, "Paid");
      }
    }
  }
}
