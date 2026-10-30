// IngredientOrder.java
// Class : IngredientOrder (การสั่งซื้อวัตถุดิบ)
//   จัดเก็บบันทึกคำสั่งซื้อวัตถุดิบที่ส่งไปยังผู้จัดจำหน่ายภายนอก
// ที่มา: attributeAll_meaning/318.jpg, ClassDiagram_Operations_Composition/385.jpg
// Composition (380.jpg):
//   "A IngredientOrder has a Supplier"   -> supplier: Supplier
//   "A Ingredient Order has a Ingredient" -> ingredients: Ingredient[]
// Responsibility 10: Order Ingredients (conceptual_responsibility.jpg)

public class IngredientOrder
{
   private String ingredientOrderId;        // รหัสใบสั่งซื้อ (Primary Key) เช่น "PO-001"
   private String supplierId;               // รหัสผู้จัดจำหน่าย เช่น "SUP-001"
   private String ingredientName;           // ชื่อวัตถุดิบที่สั่งซื้อ เช่น "น้ำปลา"
   private Integer ingredientOrderQuantity; // จำนวนที่สั่งซื้อ เช่น 10
   private String unit;                     // หน่วยนับ เช่น "ขวด"
   private Double totalCost;                // ยอดรวมคำสั่งซื้อ เช่น 320.00
   private Supplier supplier;               // composition A Ingredient Order has a Supplier
   private Ingredient[] ingredients;        // composition A Ingredient Order has a Ingredient

   // constructor ไม่มี parameter (+ IngredientOrder() ตาม Class Diagram) ทำงานตอน new IngredientOrder() (FR3 กด P)
   // ค่าตัวเลขเป็น 0, supplier = null และ ingredients เป็น array ว่าง (กัน null ตอนวนลูป) จากนั้นเรียก orderIngredients()
   public IngredientOrder()
   {
      ingredientOrderId = "";
      supplierId = "";
      ingredientName = "";
      ingredientOrderQuantity = 0;
      unit = "";
      totalCost = 0.0;
      supplier = null;
      ingredients = new Ingredient[0];
   }

   // + orderIngredients(): void
   // UseCase FR3 ข้อ 7-10: คำนวณยอดคำสั่งซื้อรวม และบันทึกรายการสั่งซื้อวัตถุดิบ
   // parameter: รหัสใบสั่งซื้อ, ผู้จัดจำหน่าย, วัตถุดิบ, จำนวน และราคาต่อหน่วยจากผู้จัดจำหน่าย
   public void orderIngredients(String theOrderId, Supplier theSupplier,
      Ingredient theIngredient, Integer quantity, Double unitCost)
   {
      ingredientOrderId = theOrderId;
      supplier = theSupplier;
      supplierId = theSupplier.getSupplierId();
      ingredients = new Ingredient[] { theIngredient };
      ingredientName = theIngredient.getIngredientName();
      unit = theIngredient.getUnit();
      ingredientOrderQuantity = quantity;
      totalCost = quantity * unitCost; // ยอดรวม = จำนวน x ราคาต่อหน่วย เช่น 10 x 32 = 320
   }

   public String getIngredientOrderId() { return ingredientOrderId; } // getter: คืนค่า รหัสใบสั่งซื้อ
   public String getSupplierId() { return supplierId; } // getter: คืนค่า รหัสผู้จัดจำหน่าย
   public String getIngredientName() { return ingredientName; } // getter: คืนค่า ชื่อวัตถุดิบที่สั่งซื้อ
   public Integer getIngredientOrderQuantity() { return ingredientOrderQuantity; } // getter: คืนค่า จำนวนที่สั่งซื้อ
   public String getUnit() { return unit; } // getter: คืนค่า หน่วยนับ
   public Double getTotalCost() { return totalCost; } // getter: คืนค่า ยอดรวมคำสั่งซื้อ
   public Supplier getSupplier() { return supplier; } // getter: คืนค่า Supplier (ส่วนประกอบ composition)
   public Ingredient[] getIngredients() { return ingredients; } // getter: คืนค่า Ingredient (ส่วนประกอบ composition)
} // end class IngredientOrder
