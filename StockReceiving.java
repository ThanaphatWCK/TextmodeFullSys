// StockReceiving.java
// Class : StockReceiving (การรับเข้าสต็อก) - บันทึกประวัติการตรวจรับวัตถุดิบเข้าคลังจริง
// ที่มา: attributeAll_meaning/321.jpg, ClassDiagram_Operations_Composition/388.jpg
// Responsibility 11: Record Stock Receiving (conceptual_responsibility.jpg)
// หมายเหตุ: Class Diagram ของ StockReceiving ไม่มี composition attribute
//   จึงรับใบสั่งซื้อ/วัตถุดิบผ่าน parameter ของ recordStockReceiving แทน

import java.util.Date;

public class StockReceiving
{
   private String stockReceivingId;        // รหัสการตรวจรับสต็อก (Primary Key) เช่น "RCV-001"
   private Date stockReceivingDate;        // วันที่และเวลาที่บันทึกรับเข้า
   private String ingredientName;          // ชื่อวัตถุดิบ เช่น "น้ำปลา"
   private String movementType;            // ประเภทการเคลื่อนไหวสต็อก เช่น "รับเข้า"
   private Integer stockReceivingQuantity; // จำนวนที่รับเข้า เช่น 10

   // ใบสั่งซื้อที่อ้างอิง (ใช้แสดงคอลัมน์ "รหัสใบสั่งซื้อ" ใน Stock Movement History textmode 148)
   private String referenceOrderId;

   // constructor ไม่มี parameter (+ StockReceiving() ตาม Class Diagram) ทำงานตอน new StockReceiving() (FR3)
   // วันที่ตั้งเป็นเวลาปัจจุบัน ข้อความเป็น "" และจำนวนเป็น 0 จากนั้นเรียก recordStockReceiving()
   public StockReceiving()
   {
      stockReceivingId = "";
      stockReceivingDate = new Date();
      ingredientName = "";
      movementType = "";
      stockReceivingQuantity = 0;
      referenceOrderId = "";
   }

   // + recordStockReceiving(): void
   // UseCase FR3 ข้อ 11-12: "ระบบ บันทึก การรับเข้าสต็อก" แล้ว "อัปเดต จำนวนสต็อกวัตถุดิบใหม่"
   public void recordStockReceiving(String theReceivingId, IngredientOrder order,
      Ingredient ingredient)
   {
      stockReceivingId = theReceivingId;
      stockReceivingDate = new Date();             // เวลาที่บันทึกรับเข้าจริง
      ingredientName = order.getIngredientName();
      movementType = "รับเข้า";                     // ตามตัวอย่าง attribute 321.jpg
      stockReceivingQuantity = order.getIngredientOrderQuantity();
      referenceOrderId = order.getIngredientOrderId();
      ingredient.addStock(stockReceivingQuantity.doubleValue()); // เพิ่มสต็อกจริง
   }

   public String getStockReceivingId() { return stockReceivingId; } // getter: คืนค่า รหัสการตรวจรับสต็อก
   public Date getStockReceivingDate() { return stockReceivingDate; } // getter: คืนค่า วันที่และเวลาที่บันทึกรับเข้า
   public String getIngredientName() { return ingredientName; } // getter: คืนค่า ชื่อวัตถุดิบ
   public String getMovementType() { return movementType; } // getter: คืนค่า ประเภทการเคลื่อนไหวสต็อก
   public Integer getStockReceivingQuantity() { return stockReceivingQuantity; } // getter: คืนค่า จำนวนที่รับเข้า
   public String getReferenceOrderId() { return referenceOrderId; } // getter: คืนค่า รหัสใบสั่งซื้อที่อ้างอิง เช่น "PO-001"
} // end class StockReceiving
