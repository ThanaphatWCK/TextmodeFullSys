// SalesEmployee.java
// Class : SalesEmployee (พนักงานขาย) - Actor ของ FR1, FR2, FR6, FR7 (usecaseDiagram.jpg)
// ที่มา: attributeAll_meaning/321.jpg, ClassDiagram_Operations_Composition/388.jpg

public class SalesEmployee
{
   private String employeeId; // รหัสพนักงานขาย (Primary Key) เช่น "EMP-01"

   // constructor ไม่มี parameter (+ SalesEmployee() ตาม Class Diagram) ทำงานตอน new SalesEmployee()
   // ตั้งรหัสพนักงานเป็น "" รอกำหนดค่าจริงภายหลัง (กันค่า null)
   public SalesEmployee()
   {
      employeeId = "";
   }

   // constructor สำหรับสร้างพนักงานตัวอย่าง EMP-01 ใน Main.seedData()
   public SalesEmployee(String theEmployeeId)
   {
      employeeId = theEmployeeId;
   }

   public String getEmployeeId() { return employeeId; } // getter: คืนค่า รหัสพนักงานขาย
} // end class SalesEmployee
