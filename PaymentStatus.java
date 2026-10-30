// PaymentStatus.java
// Class : PaymentStatus (สถานะการชำระเงิน) - สถานะของธุรกรรมการชำระเงิน
// ที่มา: attributeAll_meaning/332.jpg, ClassDiagram_Operations_Composition/395.jpg

public class PaymentStatus
{
   private String paymentStatusId;   // รหัสสถานะ เช่น "PS-01", "PS-02"
   private String paymentStatusName; // ชื่อสถานะ เช่น "รอชำระเงิน", "ชำระเงินสำเร็จ"

   // constructor ไม่มี parameter (+ PaymentStatus() ตาม Class Diagram) ทำงานตอน new PaymentStatus()
   // ตั้งรหัสและชื่อสถานะเป็น "" รอกำหนดค่าจริงภายหลัง (กันค่า null)
   public PaymentStatus()
   {
      paymentStatusId = "";
      paymentStatusName = "";
   }

   // constructor สำหรับสร้าง PS-01 / PS-02 ใน Main.seedData()
   public PaymentStatus(String theId, String theName)
   {
      paymentStatusId = theId;
      paymentStatusName = theName;
   }

   public String getPaymentStatusId() { return paymentStatusId; } // getter: คืนค่า รหัสสถานะ
   public String getPaymentStatusName() { return paymentStatusName; } // getter: คืนค่า ชื่อสถานะ
} // end class PaymentStatus
