// PaymentMethod.java
// Class : PaymentMethod (วิธีการชำระเงิน) - ประเภทช่องทางการชำระเงินที่ร้านรองรับ
// ที่มา: attributeAll_meaning/332.jpg, ClassDiagram_Operations_Composition/395.jpg

public class PaymentMethod
{
   private String paymentMethodId;   // รหัสประเภทการชำระเงิน เช่น "PM-01", "PM-02"
   private String paymentMethodName; // ชื่อประเภท เช่น "เงินสด (Cash)", "QR PromptPay"

   // constructor ไม่มี parameter (+ PaymentMethod() ตาม Class Diagram) ทำงานตอน new PaymentMethod()
   // ตั้งรหัสและชื่อวิธีชำระเป็น "" รอกำหนดค่าจริงภายหลัง (กันค่า null)
   public PaymentMethod()
   {
      paymentMethodId = "";
      paymentMethodName = "";
   }

   // constructor สำหรับสร้าง PM-01 / PM-02 ใน Main.seedData()
   public PaymentMethod(String theId, String theName)
   {
      paymentMethodId = theId;
      paymentMethodName = theName;
   }

   public String getPaymentMethodId() { return paymentMethodId; } // getter: คืนค่า รหัสประเภทการชำระเงิน
   public String getPaymentMethodName() { return paymentMethodName; } // getter: คืนค่า ชื่อประเภท
} // end class PaymentMethod
