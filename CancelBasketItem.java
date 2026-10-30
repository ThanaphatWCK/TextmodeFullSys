// CancelBasketItem.java
// Class : CancelBasketItem (การยกเลิกรายการอาหารในตะกร้า)
//   จัดเก็บบันทึกประวัติการกดยกเลิกรายการอาหารในตะกร้า
// ที่มา: attributeAll_meaning/323.jpg, ClassDiagram_Operations_Composition/390.jpg
// Composition (380.jpg, 382.jpg):
//   A Cancel Basket Item has a Sales Employee -> salesEmployee: SalesEmployee
//   A Cancel Basket Item has a Basket         -> basket: Basket[]
// Responsibility 21: Cancel Basket Item (conceptual_responsibility.jpg)
// หมายเหตุ: STACTIC_DIAGRAMS เขียน "+ CancelOrderItem() / + cancelOrderItem()"
//   แต่ 390.jpg เขียน "+ CancelBasketItem() / + cancelBasketItem()"
//   ใน Java ชื่อ constructor ต้องตรงกับชื่อคลาส จึงใช้ CancelBasketItem ตาม 390.jpg

public class CancelBasketItem
{
   private String cancelId;             // รหัสการยกเลิก (Primary Key) เช่น "CNL-001"
   private String orderId;              // รหัสออเดอร์ที่สั่งยกเลิก เช่น "ORD-001"
   private String menuName;             // ชื่อรายการอาหารที่ขอยกเลิก เช่น "ลาบเป็ด"
   private Integer cancelQuantity;      // จำนวนจานที่ยกเลิก เช่น 1
   private String cancelledBy;          // รหัสพนักงานที่ทำการยกเลิก เช่น "EMP-01"
   private SalesEmployee salesEmployee; // composition A Cancel Basket Item has a Sales Employee
   private Basket[] basket;             // composition A Cancel Basket Item has a Basket

   // constructor ไม่มี parameter (+ CancelBasketItem() ตาม Class Diagram) ทำงานตอน new CancelBasketItem()
   // จำนวนยกเลิก = 0, basket = array ว่าง (กัน null) และ salesEmployee = null รอระบุพนักงานผู้ยกเลิก
   public CancelBasketItem()
   {
      cancelId = "";
      orderId = "";
      menuName = "";
      cancelQuantity = 0;
      cancelledBy = "";
      salesEmployee = null;
      basket = new Basket[0];
   }

   // constructor ใช้ตอนพนักงานระบุเมนูที่ต้องการยกเลิก (UseCase FR7 ข้อ 6)
   public CancelBasketItem(String theCancelId, String theOrderId,
      SalesEmployee theEmployee, Basket[] theBasket, String theMenuName,
      Integer theQuantity)
   {
      cancelId = theCancelId;
      orderId = theOrderId;
      salesEmployee = theEmployee;
      cancelledBy = theEmployee.getEmployeeId();
      basket = theBasket;
      menuName = theMenuName;
      cancelQuantity = theQuantity;
   }

   // + cancelBasketItem(): void
   // UseCase FR7 ข้อ 9: "ระบบ บันทึก ข้อมูลการยกเลิกรายการอาหารในตะกร้า"
   // ลดจำนวนของรายการที่ตรงกับชื่อเมนูในตะกร้า เช่น ลาบเป็ด 2 -> 1 (textmode 160)
   public void cancelBasketItem()
   {
      for (Basket line : basket)
      {
         if (line.getMenuName().equals(menuName))
         {
            line.reduceQuantity(cancelQuantity);
            return;
         }
      }
   }

   public String getCancelId() { return cancelId; } // getter: คืนค่า รหัสการยกเลิก
   public String getOrderId() { return orderId; } // getter: คืนค่า รหัสออเดอร์ที่สั่งยกเลิก
   public String getMenuName() { return menuName; } // getter: คืนค่า ชื่อรายการอาหารที่ขอยกเลิก
   public Integer getCancelQuantity() { return cancelQuantity; } // getter: คืนค่า จำนวนจานที่ยกเลิก
   public String getCancelledBy() { return cancelledBy; } // getter: คืนค่า รหัสพนักงานที่ทำการยกเลิก
   public SalesEmployee getSalesEmployee() { return salesEmployee; } // getter: คืนค่า Sales Employee (ส่วนประกอบ composition)
   public Basket[] getBasket() { return basket; } // getter: คืนค่า Basket (ส่วนประกอบ composition)
} // end class CancelBasketItem
