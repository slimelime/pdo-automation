package automation.payment;

import automation.Invoice;

import java.io.Closeable;

public interface IPayment extends AutoCloseable {
  void payInvoice(Invoice invoice) throws Exception;
}
