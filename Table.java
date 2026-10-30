// Table.java
// Class : Table (โต๊ะ) - จัดเก็บหมายเลขโต๊ะและสถานะความพร้อมให้บริการ
// ที่มา: attributeAll_meaning/320.jpg, ClassDiagram_Operations_Composition/386.jpg
// Responsibility 1: Display Table Information (conceptual_responsibility.jpg)

public class Table
{
   // ค่าสถานะโต๊ะตามความหมาย attribute: "ว่าง", "ใช้งาน" (320.jpg)
   public static final String STATUS_AVAILABLE = "ว่าง";
   public static final String STATUS_IN_USE = "ใช้งาน";

   private String tableId;     // หมายเลขโต๊ะ (Primary Key) เช่น "TB-1"
   private String tableStatus; // สถานะของโต๊ะ เช่น "ว่าง", "ใช้งาน"

   // + Table() ตาม Class Diagram : โต๊ะใหม่เริ่มต้นสถานะ "ว่าง"
   public Table()
   {
      tableId = "";
      tableStatus = STATUS_AVAILABLE;
   }

   // constructor สำหรับสร้างโต๊ะ TB-1..TB-4 ใน Main.seedData()
   public Table(String theTableId)
   {
      tableId = theTableId;
      tableStatus = STATUS_AVAILABLE;
   }

   // + displayTableInformation(): void
   // แสดง "รหัสโต๊ะ TB-1 : [ ว่าง ]" ตาม textmode 141 (UseCase FR1 ข้อ 2)
   public void displayTableInformation()
   {
      System.out.print("รหัสโต๊ะ " + tableId + " : [ " + tableStatus + " ]");
   }

   public String getTableId() { return tableId; } // getter: คืนค่า หมายเลขโต๊ะ
   public String getTableStatus() { return tableStatus; } // getter: คืนค่า สถานะของโต๊ะ

   // ใช้เปลี่ยนสถานะเมื่อเปิดออเดอร์ (FR1) และเมื่อชำระเงินเสร็จ (FR6 ข้อ 17)
   public void setTableStatus(String newStatus) { tableStatus = newStatus; } // setter: แก้ไขค่า สถานะของโต๊ะ
} // end class Table
