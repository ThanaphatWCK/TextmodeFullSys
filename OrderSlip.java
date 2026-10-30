// OrderSlip.java
// Class : OrderSlip (ใบสั่งอาหาร) - หลักฐานใบสั่งอาหารที่พิมพ์ส่งให้ฝ่ายครัวปรุง
// ที่มา: attributeAll_meaning/320.jpg, ClassDiagram_Operations_Composition/386.jpg
// Responsibility 22: Print Order Slip (conceptual_responsibility.jpg)

public class OrderSlip
{
   private String slipId;      // รหัสใบสั่งอาหาร (Primary Key) เช่น "SLIP-001", "SLIP-002"
   private String orderId;     // รหัสออเดอร์ที่อ้างอิง เช่น "ORD-001"
   private String tableId;     // หมายเลขโต๊ะที่สั่ง เช่น "TB-1"
   private String employeeId;  // รหัสพนักงานที่พิมพ์ส่งครัว เช่น "EMP-01"
   private Double totalAmount; // ยอดรวมในใบสั่งอาหาร เช่น 290.00, 200.00

   // constructor ไม่มี parameter (+ OrderSlip() ตาม Class Diagram) ทำงานตอน new OrderSlip()
   // ตั้งรหัสต่าง ๆ เป็น "" และยอดรวม = 0.0 รอกำหนดค่าจริงภายหลัง
   public OrderSlip()
   {
      slipId = "";
      orderId = "";
      tableId = "";
      employeeId = "";
      totalAmount = 0.0;
   }

   // constructor ใช้ตอนสร้างใบสั่งอาหารจากตะกร้า (FR1 ข้อ 13 / FR7 ข้อ 10)
   public OrderSlip(String theSlipId, String theOrderId, String theTableId,
      String theEmployeeId, Double theTotalAmount)
   {
      slipId = theSlipId;
      orderId = theOrderId;
      tableId = theTableId;
      employeeId = theEmployeeId;
      totalAmount = theTotalAmount;
   }

   // + printOrderSlip(): void
   // พิมพ์ใบสั่งอาหารตาม textmode 143 (FR1) และ 160 (FR7 "ฉบับปรับปรุง")
   // parameter: รหัสตะกร้า, รายการในตะกร้า และบอกว่าเป็นฉบับปรับปรุงหรือไม่
   //   (OrderSlip ไม่มี attribute รายการอาหาร จึงรับรายการจาก Order.basket[] มาแสดง)
   public void printOrderSlip(String basketId, Basket[] lines, boolean revised)
   {
      System.out.println();
      System.out.println("==================================================");
      System.out.println("   ซอยถี่ อาหารอีสาน (Text Mode)");
      System.out.println("   พิมพ์ใบสั่งอาหาร");
      System.out.println("==================================================");
      System.out.println("          ใบสั่งอาหาร | โต๊ะ: " + tableId
         + (revised ? " (ฉบับปรับปรุง)" : ""));
      System.out.println("รหัสใบสั่งอาหาร: " + slipId + "     พนักงานรับออเดอร์: " + employeeId);
      System.out.println("รหัสตะกร้า: " + basketId + "          รหัสออเดอร์: " + orderId);
      System.out.println("--------------------------------------------------");
      System.out.println("   รหัสเมนู   |   รายการ    |  จำนวน  |  ราคา");
      System.out.println("--------------------------------------------------");
      for (Basket line : lines)
         System.out.println(String.format("     %-6s  |  %-10s |   %3d   |  %,.0f.-",
            line.getMenuId(), line.getMenuName(), line.getMenuQuantity(), line.getItemPrice()));
      System.out.println("--------------------------------------------------");
      System.out.println(String.format("                 รวม %,.0f.- บาท", totalAmount));
      System.out.println("==================================================");
   }

   public String getSlipId() { return slipId; } // getter: คืนค่า รหัสใบสั่งอาหาร
   public String getOrderId() { return orderId; } // getter: คืนค่า รหัสออเดอร์ที่อ้างอิง
   public String getTableId() { return tableId; } // getter: คืนค่า หมายเลขโต๊ะที่สั่ง
   public String getEmployeeId() { return employeeId; } // getter: คืนค่า รหัสพนักงานที่พิมพ์ส่งครัว
   public Double getTotalAmount() { return totalAmount; } // getter: คืนค่า ยอดรวมในใบสั่งอาหาร
} // end class OrderSlip
