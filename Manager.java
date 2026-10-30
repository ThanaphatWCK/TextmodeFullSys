// Manager.java
// Class : Manager (ผู้จัดการร้าน) - Actor ของ FR3, FR4, FR5 (usecaseDiagram.jpg)
// ที่มา: attributeAll_meaning/328.jpg, ClassDiagram_Operations_Composition/391.jpg

public class Manager
{
   private String managerId; // รหัสผู้จัดการ (Primary Key) เช่น "MGR-01"
   private String roleTitle; // ตำแหน่งสิทธิ์ เช่น "เจ้าของร้าน"

   // constructor ไม่มี parameter (+ Manager() ตาม Class Diagram) ทำงานตอน new Manager()
   // ตั้งรหัสผู้จัดการและตำแหน่งเป็น "" รอกำหนดค่าจริงภายหลัง (กันค่า null)
   public Manager()
   {
      managerId = "";
      roleTitle = "";
   }

   // constructor สำหรับสร้างผู้จัดการตัวอย่าง MGR-01 ใน Main.seedData()
   public Manager(String theManagerId, String theRoleTitle)
   {
      managerId = theManagerId;
      roleTitle = theRoleTitle;
   }

   public String getManagerId() { return managerId; } // getter: คืนค่า รหัสผู้จัดการ
   public String getRoleTitle() { return roleTitle; } // getter: คืนค่า ตำแหน่งสิทธิ์
} // end class Manager
