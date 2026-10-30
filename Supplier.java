// Supplier.java
// Class : Supplier (ผู้จัดจำหน่าย) - จัดเก็บข้อมูลคู่ค้าที่ส่งวัตถุดิบให้ร้าน
// ที่มา: attributeAll_meaning/319.jpg, ClassDiagram_Operations_Composition/386.jpg,
//        STACTIC_DIAGRAMS (Supplier: - supplierId, - companyName, + Supplier())

public class Supplier
{
   private String supplierId;  // รหัสผู้จัดจำหน่าย (Primary Key) เช่น "SUP-001"
   private String companyName; // ชื่อบริษัทคู่ค้า เช่น "เบทาโกร"

   // constructor ไม่มี parameter (+ Supplier() ตาม Class Diagram) ทำงานตอน new Supplier()
   // ตั้งรหัสและชื่อบริษัทเป็น "" (ข้อความว่าง) แทน null เพื่อกัน NullPointerException ตอนนำไปแสดงผล
   public Supplier()
   {
      supplierId = "";
      companyName = "";
   }

   // constructor แบบมี parameter ใช้สร้างข้อมูลตั้งต้นใน Main.seedData()
   // (แนวเดียวกับ Account(...) ที่ BankDatabase ของ ATM ใช้สร้างบัญชีทดสอบ)
   public Supplier(String theSupplierId, String theCompanyName)
   {
      supplierId = theSupplierId;
      companyName = theCompanyName;
   }

   // getter: attribute เป็น private (-) จึงต้องเข้าถึงผ่านเมธอด (encapsulation)
   public String getSupplierId() { return supplierId; } // getter: คืนค่า รหัสผู้จัดจำหน่าย
   public String getCompanyName() { return companyName; } // getter: คืนค่า ชื่อบริษัทคู่ค้า
} // end class Supplier
