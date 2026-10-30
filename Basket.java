// Basket.java
// Class : Basket (ตะกร้าสั่งอาหาร)
//   จัดเก็บกลุ่มรายการอาหารที่ลูกค้าสั่งในแต่ละรอบภายใต้ออเดอร์
// ที่มา: attributeAll_meaning/326.jpg, ClassDiagram_Operations_Composition/390.jpg
// Composition: "A Basket has a Menu" (382.jpg) -> menu: Menu
// Responsibility 2,3,20,24 (conceptual_responsibility.jpg):
//   2.Select and Add Items to Basket, 3.Calculate and Save Basket,
//   20.Display Basket Information, 24.Update Cooking Status
//
// การตีความ: 1 object Basket = 1 รายการเมนูในตะกร้า (มี menuId, menuQuantity, itemPrice เดียว)
//   รายการหลายแถวที่มี basketId เดียวกัน (เช่น BSK-001) = ตะกร้า 1 รอบ (textmode 142)
//   Order เก็บทุกแถวไว้ใน basket: Basket[] (388.jpg)

public class Basket
{
   // ค่าสถานะการปรุงในครัว (326.jpg: "ยืนยันแล้ว","กำลังทำ","ทำเสร็จแล้ว" + "รอยืนยัน" จาก textmode 161)
   public static final String STATUS_WAITING = "รอยืนยัน";
   public static final String STATUS_CONFIRMED = "ยืนยันแล้ว";
   public static final String STATUS_COOKING = "กำลังทำ";
   public static final String STATUS_DONE = "ทำเสร็จแล้ว";

   private String basketId;      // รหัสตะกร้าสั่งอาหาร เช่น "BSK-001"
   private String orderId;       // รหัสออเดอร์หลักที่สังกัด เช่น "ORD-001"
   private String tableId;       // หมายเลขโต๊ะที่สั่ง เช่น "TB-1"
   private String menuId;        // รหัสเมนูอาหาร เช่น "M-01"
   private String menuName;      // ชื่อเมนูอาหาร เช่น "ส้มตำ"
   private Integer menuQuantity; // จำนวนจานที่สั่ง เช่น 1, 2
   private Double itemPrice;     // ราคารวมของรายการนี้ เช่น 70.00, 180.00
   private String cookingStatus; // สถานะการปรุงในครัว
   private Menu menu;            // composition A Basket has a Menu

   // constructor ไม่มี parameter (+ Basket() ตาม Class Diagram) ทำงานตอน new Basket()
   // จำนวน = 0 และราคา = 0.0 เพราะยังไม่ได้เลือกเมนู, สถานะ = "รอยืนยัน" รอครัวยืนยัน, menu = null รอเลือกเมนู
   public Basket()
   {
      basketId = "";
      orderId = "";
      tableId = "";
      menuId = "";
      menuName = "";
      menuQuantity = 0;
      itemPrice = 0.0;
      cookingStatus = STATUS_WAITING;
      menu = null;
   }

   // constructor ใช้ตอนเริ่มตะกร้ารอบใหม่ของออเดอร์ (FR1 ข้อ 3)
   public Basket(String theBasketId, String theOrderId, String theTableId)
   {
      this();
      basketId = theBasketId;
      orderId = theOrderId;
      tableId = theTableId;
   }

   // + selectAndAddItemsToBasket(): void
   // UseCase FR1 ข้อ 5: "พนักงานขาย ระบุ รหัสเมนูอาหารหรือเครื่องดื่มพร้อมจำนวนเพื่อเพิ่มลงในตะกร้า"
   // parameter: เมนูที่เลือก และจำนวน (รูปแบบ "M-01 1" ใน textmode 141)
   public void selectAndAddItemsToBasket(Menu selectedMenu, Integer quantity)
   {
      menu = selectedMenu;
      menuId = selectedMenu.getMenuId();
      menuName = selectedMenu.getMenuName();
      menuQuantity = menuQuantity + quantity; // ถ้าเลือกเมนูเดิมซ้ำ ให้รวมจำนวน
   }

   // + calculateAndSaveBasket(): void
   // UseCase FR1 ข้อ 8: "ระบบ คำนวณและแสดง ราคารวมของตะกร้าสั่งอาหาร"
   // ราคารวมรายการ = ราคาเมนู x จำนวน เช่น ลาบเป็ด 90 x 2 = 180 (textmode 142)
   public void calculateAndSaveBasket()
   {
      itemPrice = menu.getPrice() * menuQuantity;
   }

   // + displayBasketInformation(): void
   // UseCase FR1 ข้อ 6: "ระบบ แสดง รายการตะกร้าสั่งอาหารที่บันทึกไว้"
   // รูปแบบบรรทัด: "[M-01] | ส้มตำ | 1 | 70.-" (textmode 142, 159)
   public void displayBasketInformation()
   {
      System.out.println(String.format("      [%s]   |  %-10s |  %3d  |  %,.0f.-",
         menuId, menuName, menuQuantity, itemPrice));
   }

   // + updateCookingStatus(): void
   // UseCase FR8 ข้อ 6,10,12: ยืนยัน/กำลังทำ/ทำเสร็จ (textmode 161-162)
   public void updateCookingStatus(String newStatus)
   {
      cookingStatus = newStatus;
   }

   // ลดจำนวนเมื่อยกเลิกรายการ (เรียกจาก CancelBasketItem.cancelBasketItem - FR7 ข้อ 9)
   public void reduceQuantity(Integer cancelQuantity)
   {
      menuQuantity = menuQuantity - cancelQuantity;
      calculateAndSaveBasket(); // คำนวณราคารวมของรายการใหม่ เช่น ลาบเป็ด 2 -> 1 = 90
   }

   public String getBasketId() { return basketId; } // getter: คืนค่า รหัสตะกร้าสั่งอาหาร
   public String getOrderId() { return orderId; } // getter: คืนค่า รหัสออเดอร์หลักที่สังกัด
   public String getTableId() { return tableId; } // getter: คืนค่า หมายเลขโต๊ะที่สั่ง
   public String getMenuId() { return menuId; } // getter: คืนค่า รหัสเมนูอาหาร
   public String getMenuName() { return menuName; } // getter: คืนค่า ชื่อเมนูอาหาร
   public Integer getMenuQuantity() { return menuQuantity; } // getter: คืนค่า จำนวนจานที่สั่ง
   public Double getItemPrice() { return itemPrice; } // getter: คืนค่า ราคารวมของรายการนี้
   public String getCookingStatus() { return cookingStatus; } // getter: คืนค่า สถานะการปรุงในครัว
   public Menu getMenu() { return menu; } // getter: คืนค่า Menu (ส่วนประกอบ composition)
} // end class Basket
