// Payment.java
// Class : Payment (การชำระเงิน) - บันทึกธุรกรรมการชำระเงิน ยอดรับ และเงินทอน
// ที่มา: attributeAll_meaning/331.jpg, ClassDiagram_Operations_Composition/394.jpg
// Composition (380.jpg, 382.jpg):
//   A Payment has a Sales Employee   -> salesEmployee: SalesEmployee
//   A Payment has a Point Redemption -> pointRedemption: PointRedemption (0..1)
//   A Payment has a PaymentMethod    -> paymentMethod: PaymentMethod
//   A Payment has a PaymentStatus    -> paymentStatus: PaymentStatus
// Responsibility 17,18: Summarize Payment Amount, Process Payment

public class Payment
{
   public static final double MEMBER_DISCOUNT_RATE = 0.05; // ส่วนลดสมาชิก 5% (textmode 155)
   public static final double VAT_RATE = 0.07;             // VAT 7% (textmode 155)

   private String paymentId;                // รหัสการชำระเงิน (Primary Key) เช่น "PAY-001"
   private String orderId;                  // รหัสออเดอร์ที่อ้างอิง เช่น "ORD-001"
   private Double payableAmount;            // ยอดเงินที่ต้องชำระ เช่น 275.50
   private Double receivedAmount;           // จำนวนเงินที่รับมา เช่น 300.00
   private Double changeAmount;             // เงินทอน เช่น 24.50
   private SalesEmployee salesEmployee;     // composition A Payment has a Sales Employee
   private PointRedemption pointRedemption; // composition A Payment has a Point Redemption
   private PaymentMethod paymentMethod;     // composition A Payment has a Payment Method
   private PaymentStatus paymentStatus;     // composition A Payment has a Payment Status

   // constructor ไม่มี parameter (+ Payment() ตาม Class Diagram) ทำงานตอน new Payment()
   // ยอดเงินทั้งหมด = 0.0 เพราะยังไม่ได้คำนวณ และส่วนประกอบทั้ง 4 เป็น null รอกำหนดตอนคิดเงิน (FR6)
   public Payment()
   {
      paymentId = "";
      orderId = "";
      payableAmount = 0.0;
      receivedAmount = 0.0;
      changeAmount = 0.0;
      salesEmployee = null;
      pointRedemption = null;
      paymentMethod = null;
      paymentStatus = null;
   }

   // constructor ใช้ตอนพนักงานยืนยันรายการและไปหน้าคิดเงิน (UseCase FR6 ข้อ 5)
   public Payment(String thePaymentId, String theOrderId, SalesEmployee theEmployee,
      PointRedemption theRedemption, PaymentStatus pendingStatus)
   {
      this();
      paymentId = thePaymentId;
      orderId = theOrderId;
      salesEmployee = theEmployee;
      pointRedemption = theRedemption;
      paymentStatus = pendingStatus; // เริ่มต้น "รอชำระเงิน"
   }

   // + summarizePaymentAmount(): void
   // UseCase FR6 ข้อ 6: "ระบบ คำนวณ และ แสดง ข้อมูลสรุปยอดชำระพร้อมส่วนลดสมาชิกและภาษี"
   // ตัวอย่าง textmode 155: ยอดรวม 290.00 / ส่วนลด 5% 14.50 / หลังหักส่วนลด 257.48
   //                        VAT 7% 18.02 / ยอดสุทธิ 275.50
   // ตัวเลขชุดนี้คือราคาที่รวม VAT แล้ว: 290 - 14.50 = 275.50 (ยอดสุทธิ)
   //   แยก VAT ออก = 275.50 x 7/107 = 18.02 และยอดก่อน VAT = 275.50 - 18.02 = 257.48
   public void summarizePaymentAmount(Order order)
   {
      double subTotal = order.getOrderSubTotal();

      // ส่วนลดสมาชิก 5% เฉพาะลูกค้าที่เป็นสมาชิก
      double memberDiscount = 0.0;
      if (order.getCustomer() != null && order.getCustomer().isMember())
         memberDiscount = round2(subTotal * MEMBER_DISCOUNT_RATE);

      // ส่วนลดจากการแลกคะแนน (ถ้ามีการแลกใน FR2) - "หน้าจอคิดเงินดึงมูลค่าส่วนลดจากการแลกแต้ม" (382.jpg)
      double pointDiscount = 0.0;
      if (pointRedemption != null)
         pointDiscount = pointRedemption.getDiscountReceived();

      double grandTotal = Math.max(0.0, round2(subTotal - memberDiscount - pointDiscount));
      double vatAmount = round2(grandTotal * VAT_RATE / (1 + VAT_RATE));
      double netAfterDiscount = round2(grandTotal - vatAmount);

      order.setPaymentSummary(memberDiscount, netAfterDiscount, vatAmount, grandTotal);
      payableAmount = grandTotal; // ยอดที่ต้องชำระ = ยอดสุทธิ
   }

   // + processPayment(): void
   // UseCase FR6 ข้อ 11-13, 17: รับเงิน คำนวณเงินทอน และเปลี่ยนสถานะเป็น "ชำระเงินสำเร็จ"
   // parameter: เงินที่รับมา, วิธีชำระ (เงินสด/QR) และสถานะที่ชำระแล้ว
   public void processPayment(Double theReceivedAmount, PaymentMethod method,
      PaymentStatus paidStatus)
   {
      receivedAmount = theReceivedAmount;
      changeAmount = round2(receivedAmount - payableAmount); // เงินทอน เช่น 300 - 275.50 = 24.50
      paymentMethod = method;
      paymentStatus = paidStatus;
   }

   // ปัดเศษทศนิยม 2 ตำแหน่ง (หน่วยสตางค์)
   private static double round2(double value)
   {
      return Math.round(value * 100) / 100.0;
   }

   public String getPaymentId() { return paymentId; } // getter: คืนค่า รหัสการชำระเงิน
   public String getOrderId() { return orderId; } // getter: คืนค่า รหัสออเดอร์ที่อ้างอิง
   public Double getPayableAmount() { return payableAmount; } // getter: คืนค่า ยอดเงินที่ต้องชำระ
   public Double getReceivedAmount() { return receivedAmount; } // getter: คืนค่า จำนวนเงินที่รับมา
   public Double getChangeAmount() { return changeAmount; } // getter: คืนค่า เงินทอน
   public SalesEmployee getSalesEmployee() { return salesEmployee; } // getter: คืนค่า Sales Employee (ส่วนประกอบ composition)
   public PointRedemption getPointRedemption() { return pointRedemption; } // getter: คืนค่า Point Redemption (ส่วนประกอบ composition)
   public PaymentMethod getPaymentMethod() { return paymentMethod; } // getter: คืนค่า Payment Method (ส่วนประกอบ composition)
   public PaymentStatus getPaymentStatus() { return paymentStatus; } // getter: คืนค่า Payment Status (ส่วนประกอบ composition)
} // end class Payment
