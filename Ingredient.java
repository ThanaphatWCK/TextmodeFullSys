// Ingredient.java
// Class : Ingredient (วัตถุดิบ) - จัดเก็บข้อมูลวัตถุดิบและปริมาณคงเหลือในคลังร้าน
// ที่มา: attributeAll_meaning/318.jpg, ClassDiagram_Operations_Composition/385.jpg
// Responsibility 8: Deduct Ingredient Stock (conceptual_responsibility.jpg)

public class Ingredient
{
   private String ingredientId;   // รหัสวัตถุดิบ (Primary Key) เช่น "ING-001"
   private String ingredientName; // ชื่อวัตถุดิบ เช่น "เนื้อเป็ด", "น้ำปลา"
   private Double currentStock;   // จำนวนคงเหลือปัจจุบันในสต็อก เช่น 15.5
   private String unit;           // หน่วยนับ เช่น "ขวด", "กก.", "ลูก"
   private Double reorderPoint;   // จุดแจ้งเตือนสั่งซื้อขั้นต่ำ เช่น 2.0

   // constructor ไม่มี parameter (+ Ingredient() ตาม Class Diagram) ทำงานตอน new Ingredient()
   // ข้อความตั้งเป็น "" และสต็อก/จุดแจ้งเตือน = 0.0 เพราะยังไม่มีวัตถุดิบในคลัง
   public Ingredient()
   {
      ingredientId = "";
      ingredientName = "";
      currentStock = 0.0;
      unit = "";
      reorderPoint = 0.0;
   }

   // constructor สำหรับสร้างวัตถุดิบตัวอย่าง ING-001..ING-005 ใน Main.seedData()
   public Ingredient(String theId, String theName, Double theStock,
      String theUnit, Double theReorderPoint)
   {
      ingredientId = theId;
      ingredientName = theName;
      currentStock = theStock;
      unit = theUnit;
      reorderPoint = theReorderPoint;
   }

   // + deductIngredientStock(): void
   // UseCase FR3 ข้อ 1: "ระบบ ตรวจสอบ และ ตัด สต็อกวัตถุดิบอัตโนมัติตามรายการขายออเดอร์ล่าสุด"
   // parameter: ปริมาณที่ใช้ เช่น เนื้อเป็ด -0.1 กก. (textmode 147)
   public void deductIngredientStock(Double usedQuantity)
   {
      // ปัดทศนิยม 3 ตำแหน่ง ป้องกันค่าคลาดเคลื่อนของ double เช่น 1.7000000000000002
      currentStock = Math.round((currentStock - usedQuantity) * 1000) / 1000.0;
   }

   // เพิ่มสต็อกเมื่อรับเข้า (เรียกจาก StockReceiving.recordStockReceiving - FR3 ข้อ 12)
   public void addStock(Double receivedQuantity)
   {
      currentStock = Math.round((currentStock + receivedQuantity) * 1000) / 1000.0;
   }

   // UseCase FR3 ข้อ 2: "ระบบ เปรียบเทียบ ปริมาณวัตถุดิบคงเหลือกับค่าจุดแจ้งเตือนขั้นต่ำ"
   // คืน true เมื่อคงเหลือต่ำกว่าจุดแจ้งเตือน (textmode 147 "ต่ำกว่าจุดแจ้งเตือน 2 ขวด")
   public boolean isBelowReorderPoint()
   {
      return currentStock < reorderPoint;
   }

   public String getIngredientId() { return ingredientId; } // getter: คืนค่า รหัสวัตถุดิบ
   public String getIngredientName() { return ingredientName; } // getter: คืนค่า ชื่อวัตถุดิบ
   public Double getCurrentStock() { return currentStock; } // getter: คืนค่า จำนวนคงเหลือปัจจุบันในสต็อก
   public String getUnit() { return unit; } // getter: คืนค่า หน่วยนับ
   public Double getReorderPoint() { return reorderPoint; } // getter: คืนค่า จุดแจ้งเตือนสั่งซื้อขั้นต่ำ
} // end class Ingredient
