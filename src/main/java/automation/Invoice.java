package automation;

import java.util.Properties;

public class Invoice extends Properties{
  private int amountInCents;
  private String invoiceNumber;
  private String receiptEmail;
  private String invoiceLink;
  private PaymentMethod paymentMethod = PaymentMethod.PAYCORP;

  public enum PaymentMethod {
    AI, PAYCORP, BPAY
  }

  public int getAmountInCents() {
    return amountInCents;
  }

  public void setAmountInCents(int amountInCents) {
    this.amountInCents = amountInCents;
  }

  public String getInvoiceNumber() {
    return invoiceNumber;
  }

  public void setInvoiceNumber(String invoiceNumber) {
    this.invoiceNumber = invoiceNumber;
  }

  public String getReceiptEmail() {
    return receiptEmail;
  }

  public void setReceiptEmail(String receiptEmail) {
    this.receiptEmail = receiptEmail;
  }

  public String getInvoiceLink() {
    return invoiceLink;
  }

  public void setInvoiceLink(String invoiceLink) {
    this.invoiceLink = invoiceLink;
  }

  public PaymentMethod getPaymentMethod() {
    return paymentMethod;
  }

  public void setPaymentMethod(PaymentMethod paymentMethod) {
    this.paymentMethod = paymentMethod;
  }
}
