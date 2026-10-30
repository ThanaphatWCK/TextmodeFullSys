// Receipt.java
// Class : Receipt (ใบเสร็จ) - จัดเก็บหลักฐานใบเสร็จรับเงินที่พิมพ์ให้ลูกค้า
// ที่มา: attributeAll_meaning/329.jpg, ClassDiagram_Operations_Composition/392.jpg
// Composition (382.jpg):
//   A Receipt has a Customer -> customer: Customer
//   A Receipt has a Payment  -> payment: Payment
// Responsibility 19: Print Receipt (conceptual_responsibility.jpg)

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Date;

public class Receipt
{
   private String receiptId;      // รหัสใบเสร็จ (Primary Key) เช่น "REC-001"
   private String shopName;       // ชื่อร้านค้า เช่น "ร้านซอยถี่"
   private String orderId;        // รหัสออเดอร์ที่อ้างอิง เช่น "ORD-001"
   private String tableId;        // หมายเลขโต๊ะ เช่น "TB-1"
   private String memberName;     // ชื่อสมาชิกที่แสดงในบิล เช่น "สมชาย ใจดี"
   private Double subtotal;       // ยอดรวมก่อนหักส่วนลด เช่น 290.00
   private Double discountAmount; // ส่วนลด 5% เช่น 14.50
   private Double vatAmount;      // VAT 7% เช่น 18.02
   private Double grandTotal;     // ยอดสุทธิที่ชำระ เช่น 275.50
   private Date issuedAt;         // วันและเวลาที่พิมพ์ใบเสร็จ
   private Customer customer;     // composition A Receipt has a Customer
   private Payment payment;       // composition A Receipt has a Payment

   // constructor ไม่มี parameter (+ Receipt() ตาม Class Diagram) ทำงานตอน new Receipt()
   // ชื่อร้าน = "ร้านซอยถี่" (ค่าคงที่ของร้าน), เวลาออกใบเสร็จ = ตอนนี้, ยอดเงิน = 0.0 และ customer/payment = null รอผูก
   public Receipt()
   {
      receiptId = "";
      shopName = "ร้านซอยถี่";
      orderId = "";
      tableId = "";
      memberName = "";
      subtotal = 0.0;
      discountAmount = 0.0;
      vatAmount = 0.0;
      grandTotal = 0.0;
      issuedAt = new Date();
      customer = null;
      payment = null;
   }

   // constructor ใช้ตอน "ระบบ บันทึก ข้อมูลการชำระเงิน และ แสดง ใบเสร็จรับเงิน" (FR6 ข้อ 13)
   public Receipt(String theReceiptId, Order order, Payment thePayment, Date theIssuedAt)
   {
      this();
      receiptId = theReceiptId;
      orderId = order.getOrderId();
      tableId = order.getTableId();
      customer = order.getCustomer();
      memberName = (customer != null && customer.isMember())
         ? customer.getMember().getFullName() : "";
      subtotal = order.getOrderSubTotal();
      discountAmount = order.getOrderDiscount();
      vatAmount = order.getOrderVatAmount();
      grandTotal = order.getOrderGrandTotal();
      issuedAt = theIssuedAt;
      payment = thePayment;
   }

   // + printReceipt(): void
   // UseCase FR6 ข้อ 13-15: แสดง/พิมพ์ใบเสร็จรับเงิน ตามรูปแบบ textmode 157
   // parameter: Order (เพื่อดึงรายการอาหารและยอดหลังหักส่วนลด)
   public void printReceipt(Order order)
   {
      System.out.println();
      System.out.println("==================================================");
      System.out.println("   ซอยถี่ อาหารอีสาน (Text Mode)");
      System.out.println("   พิมพ์ใบเสร็จรับเงิน");
      System.out.println("==================================================");
      System.out.println("เลขที่ใบเสร็จ : " + receiptId);
      System.out.println("รหัสการชำระเงิน : " + payment.getPaymentId() + " รหัสออเดอร์ : " + orderId);
      System.out.println("รหัสโต๊ะ : " + tableId + " รหัสพนักงานขาย : "
         + payment.getSalesEmployee().getEmployeeId());
      System.out.println("วันที่ : " + formatThaiDateTime(issuedAt));
      String memberText = (customer != null && customer.isMember())
         ? " |รหัสสมาชิก: " + customer.getMemberId() + " (" + memberName + ")" : "";
      System.out.println("รหัสลูกค้า : "
         + (customer == null ? "-" : customer.getCustomerId()) + memberText);
      System.out.println("--------------------------------------------------");
      System.out.println(" รหัสเมนู | ชื่ออาหาร | จำนวน | ราคาต่อหน่วย | ราคารวม");
      for (Basket line : order.getBasket())
         System.out.println(String.format(" [%s] | %-9s | %3d  | %,10.2f | %,.2f",
            line.getMenuId(), line.getMenuName(), line.getMenuQuantity(),
            line.getMenu().getPrice(), line.getItemPrice()));
      System.out.println("--------------------------------------------------");
      System.out.println(String.format("ยอดรวม :                 %,.2f บาท", subtotal));
      if (discountAmount > 0)
         System.out.println(String.format("ส่วนลดสมาชิก (5%%):        -%,.2f บาท", discountAmount));
      PointRedemption redemption = payment.getPointRedemption();
      if (redemption != null && redemption.getDiscountReceived() > 0)
         System.out.println(String.format("ส่วนลดแลกคะแนน :          -%,.2f บาท",
            redemption.getDiscountReceived()));
      System.out.println(String.format("ยอดหลังหักส่วนลด :        %,.2f บาท",
         order.getOrderNetAfterDiscount()));
      System.out.println(String.format("VAT 7%% :                 %,.2f บาท", vatAmount));
      System.out.println(String.format("ยอดสุทธิ :                %,.2f บาท", grandTotal));
      System.out.println("--------------------------------------------------");
      if (payment.getPaymentMethod().getPaymentMethodId().equals("PM-01"))
         System.out.println(String.format("ชำระโดย : เงินสด (รับมา %,.0f บาท / ทอน %,.2f บาท)",
            payment.getReceivedAmount(), payment.getChangeAmount()));
      else
         System.out.println(String.format("ชำระโดย : %s (%,.2f บาท)",
            payment.getPaymentMethod().getPaymentMethodName(), payment.getReceivedAmount()));
      System.out.println("--------------------------------------------------");
      System.out.println("             ** ขอบคุณที่ใช้บริการครับ **");
      System.out.println("--------------------------------------------------");
   }

   // วันที่และเวลาแบบไทย พ.ศ. เช่น "01/06/2569 13:15 น." (textmode 157)
   private static String formatThaiDateTime(Date date)
   {
      Calendar c = new GregorianCalendar();
      c.setTime(date);
      return String.format("%02d/%02d/%04d %02d:%02d น.", c.get(Calendar.DAY_OF_MONTH),
         c.get(Calendar.MONTH) + 1, c.get(Calendar.YEAR) + 543,
         c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
   }

   public String getReceiptId() { return receiptId; } // getter: คืนค่า รหัสใบเสร็จ
   public String getShopName() { return shopName; } // getter: คืนค่า ชื่อร้านค้า
   public String getOrderId() { return orderId; } // getter: คืนค่า รหัสออเดอร์ที่อ้างอิง
   public String getTableId() { return tableId; } // getter: คืนค่า หมายเลขโต๊ะ
   public String getMemberName() { return memberName; } // getter: คืนค่า ชื่อสมาชิกที่แสดงในบิล
   public Double getSubtotal() { return subtotal; } // getter: คืนค่า ยอดรวมก่อนหักส่วนลด
   public Double getDiscountAmount() { return discountAmount; } // getter: คืนค่า ส่วนลด 5%
   public Double getVatAmount() { return vatAmount; } // getter: คืนค่า VAT 7%
   public Double getGrandTotal() { return grandTotal; } // getter: คืนค่า ยอดสุทธิที่ชำระ
   public Date getIssuedAt() { return issuedAt; } // getter: คืนค่า วันและเวลาที่พิมพ์ใบเสร็จ
   public Customer getCustomer() { return customer; } // getter: คืนค่า Customer (ส่วนประกอบ composition)
   public Payment getPayment() { return payment; } // getter: คืนค่า Payment (ส่วนประกอบ composition)
} // end class Receipt
