// StockNotification.java
// Class : StockNotification (การแจ้งเตือนสต็อก)
//   จัดเก็บประวัติและข้อความแจ้งเตือนเมื่อวัตถุดิบใกล้หมดสต็อก
// ที่มา: attributeAll_meaning/319.jpg, ClassDiagram_Operations_Composition/385.jpg
// Composition: "A Stock Notification has a Ingredient" (380.jpg) -> ingredient: Ingredient
// Responsibility 9: Notify Ingredient Stock (conceptual_responsibility.jpg)

public class StockNotification
{
   private String notiId;              // รหัสการแจ้งเตือน (Primary Key) เช่น "NOTI-001"
   private String ingredientId;        // รหัสวัตถุดิบที่ต้องแจ้งเตือน เช่น "ING-005"
   private String notificationMessage; // ข้อความ เช่น "น้ำปลา เหลือ 1 ขวด (ต่ำกว่าจุดแจ้งเตือน 2 ขวด)"
   private Boolean isConfirmed;        // true = ยืนยันรับทราบแล้ว, false = ยังไม่ยืนยัน
   private Ingredient ingredient;      // composition A Stock Notification has a Ingredient

   // constructor ไม่มี parameter (+ StockNotification() ตาม Class Diagram) ทำงานตอน new StockNotification()
   // isConfirmed = false เพราะการแจ้งเตือนใหม่ยังไม่มีใครกดยืนยัน และ ingredient = null รอผูกวัตถุดิบ
   public StockNotification()
   {
      notiId = "";
      ingredientId = "";
      notificationMessage = "";
      isConfirmed = false;
      ingredient = null;
   }

   // constructor ใช้ตอนระบบสร้างการแจ้งเตือนใหม่ (FR3 ข้อ 3)
   public StockNotification(String theNotiId, Ingredient theIngredient)
   {
      this();
      notiId = theNotiId;
      ingredient = theIngredient;
      ingredientId = theIngredient.getIngredientId();
   }

   // + notifyIngredientStock(): void
   // UseCase FR3 ข้อ 3: "ระบบ สร้าง และ แสดง การแจ้งเตือนวัตถุดิบใกล้หมด" (textmode 147)
   // สร้างข้อความแจ้งเตือนจากข้อมูลวัตถุดิบ แล้วแสดงหน้าจอ "รับแจ้งเตือนวัตถุดิบต่ำ"
   public void notifyIngredientStock()
   {
      String stock = formatQuantity(ingredient.getCurrentStock()) + " " + ingredient.getUnit();
      String reorder = formatQuantity(ingredient.getReorderPoint()) + " " + ingredient.getUnit();
      notificationMessage = ingredient.getIngredientName() + " เหลือ " + stock
         + " (ต่ำกว่าจุดแจ้งเตือน " + reorder + ")";

      System.out.println();
      System.out.println("==================================================");
      System.out.println("   ซอยถี่ อาหารอีสาน (Text Mode)");
      System.out.println("   รับแจ้งเตือนวัตถุดิบต่ำ");
      System.out.println("==================================================");
      System.out.println("   รหัสการแจ้งเตือน : " + notiId);
      System.out.println("   [ การแจ้งเตือนจากระบบ ]");
      System.out.println("   [!] วัตถุดิบใกล้หมด!");
      System.out.println("--------------------------------------------------");
      System.out.println("      รหัสวัตถุดิบ  :  " + ingredientId);
      System.out.println("      รายการ      :  \"" + ingredient.getIngredientName() + "\"");
      System.out.println("      คงเหลือ      :  " + stock);
      System.out.println("      หมายเหตุ     :  (ต่ำกว่าจุดแจ้งเตือน " + reorder + ")");
      System.out.println("--------------------------------------------------");
      System.out.println("[ระบบ]: มีรายการวัตถุดิบต้องสั่งซื้อเพิ่ม");
   }

   // แสดงปริมาณแบบตัดศูนย์ท้าย เช่น 1.0 -> "1", 1.70 -> "1.7"
   private static String formatQuantity(double quantity)
   {
      if (quantity == Math.rint(quantity))
         return String.format("%.0f", quantity);
      return String.format("%.3f", quantity).replaceAll("0+$", "");
   }

   public String getNotiId() { return notiId; } // getter: คืนค่า รหัสการแจ้งเตือน
   public String getIngredientId() { return ingredientId; } // getter: คืนค่า รหัสวัตถุดิบที่ต้องแจ้งเตือน
   public String getNotificationMessage() { return notificationMessage; } // getter: คืนค่า ข้อความ
   public Boolean getIsConfirmed() { return isConfirmed; } // getter: คืนค่า true = ยืนยันรับทราบแล้ว, false = ยังไม่ยืนยัน
   public Ingredient getIngredient() { return ingredient; } // getter: คืนค่า Ingredient (ส่วนประกอบ composition)

   // UseCase FR3 ข้อ 6: "ผู้จัดการร้าน ยืนยัน การรับทราบการแจ้งเตือน"
   public void setIsConfirmed(Boolean confirmed) { isConfirmed = confirmed; } // setter: แก้ไขค่า true = ยืนยันรับทราบแล้ว, false = ยังไม่ยืนยัน
} // end class StockNotification
