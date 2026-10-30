// KitchenEmployee.java
// Class : KitchenEmployee (พนักงานครัว) - Actor ของ FR8 (usecaseDiagram.jpg)
// ที่มา: attributeAll_meaning/327.jpg, ClassDiagram_Operations_Composition/391.jpg

public class KitchenEmployee
{
   private String kitchenEmployeeId; // รหัสพนักงานครัว (Primary Key) เช่น "CHEF-01"

   // constructor ไม่มี parameter (+ KitchenEmployee() ตาม Class Diagram) ทำงานตอน new KitchenEmployee()
   // ตั้งรหัสพนักงานครัวเป็น "" รอกำหนดค่าจริงภายหลัง (กันค่า null)
   public KitchenEmployee()
   {
      kitchenEmployeeId = "";
   }

   // constructor สำหรับสร้างพนักงานครัวตัวอย่าง CHEF-01 ใน Main.seedData()
   public KitchenEmployee(String theKitchenEmployeeId)
   {
      kitchenEmployeeId = theKitchenEmployeeId;
   }

   public String getKitchenEmployeeId() { return kitchenEmployeeId; } // getter: คืนค่า รหัสพนักงานครัว
} // end class KitchenEmployee
