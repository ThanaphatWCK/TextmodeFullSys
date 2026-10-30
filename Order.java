// Order.java
// Class : Order (ออเดอร์) - บันทึกภาพรวมคำสั่งซื้อ ยอดเงิน ภาษี และสถานะของแต่ละโต๊ะ
// ที่มา: attributeAll_meaning/323.jpg, ClassDiagram_Operations_Composition/388.jpg
// Composition (380.jpg, 382.jpg):
//   A Order has a Table          -> table: Table
//   A Order has a Order Slip     -> orderSlip: OrderSlip
//   A Order has a Sales Employee -> salesEmployee: SalesEmployee
//   A Order has a Basket         -> basket: Basket[]   (1 --- 1..*)
//   A Order has a Customer       -> customer: Customer
//   A Order has a Payment        -> payment: Payment
// Responsibility 23: Confirm Kitchen Order (+ saveOrder จาก Class Diagram)

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Order
{
   private String orderId;               // รหัสออเดอร์ (Primary Key) เช่น "ORD-001"
   private String tableId;               // หมายเลขโต๊ะ เช่น "TB-1"
   private Integer orderTotalItems;      // จำนวนรายการอาหารทั้งหมด เช่น 3
   private Double orderSubTotal;         // ยอดรวมก่อนหักส่วนลด เช่น 290.00
   private Double orderDiscount;         // ส่วนลดสมาชิก (5%) เช่น 14.50
   private Double orderNetAfterDiscount; // ยอดหลังหักส่วนลด (ก่อน VAT) เช่น 257.48
   private Double orderVatAmount;        // ภาษีมูลค่าเพิ่ม (VAT 7%) เช่น 18.02
   private Double orderGrandTotal;       // ยอดสุทธิที่ต้องชำระ เช่น 275.50
   private Table table;                  // composition A Order has a Table
   private OrderSlip orderSlip;          // composition A Order has a Order Slip
   private SalesEmployee salesEmployee;  // composition A Order has a Sales Employee
   private Basket[] basket;              // composition A Order has a Basket
   private Customer customer;            // composition A Order has a Customer
   private Payment payment;              // composition A Order has a Payment

   // constructor ไม่มี parameter (+ Order() ตาม Class Diagram) ทำงานตอน new Order()
   // ยอดเงินทุกช่อง = 0 เพราะยังไม่มีรายการอาหาร, basket = array ว่าง (กัน null ตอนวนลูป),
   // payment = null หมายถึง "ยังไม่ชำระเงิน" และส่วนประกอบอื่นเป็น null รอผูกตอนเปิดออเดอร์
   public Order()
   {
      orderId = "";
      tableId = "";
      orderTotalItems = 0;
      orderSubTotal = 0.0;
      orderDiscount = 0.0;
      orderNetAfterDiscount = 0.0;
      orderVatAmount = 0.0;
      orderGrandTotal = 0.0;
      table = null;
      orderSlip = null;
      salesEmployee = null;
      basket = new Basket[0];
      customer = null;
      payment = null;
   }

   // constructor ใช้ตอนเปิดออเดอร์ใหม่จากโต๊ะว่าง (UseCase FR1 ข้อ 3 "สร้าง ออเดอร์ใหม่")
   public Order(String theOrderId, Table theTable, SalesEmployee theEmployee,
      Customer theCustomer)
   {
      this();
      orderId = theOrderId;
      table = theTable;
      tableId = theTable.getTableId();
      salesEmployee = theEmployee;
      customer = theCustomer;
   }

   // + saveOrder(): void
   // UseCase FR1 ข้อ 11: "ระบบ บันทึก ตะกร้าสั่งอาหารเข้าสู่ออเดอร์"
   // บันทึก/คำนวณยอดของออเดอร์ใหม่จากรายการใน basket[] (ใช้ซ้ำหลังยกเลิกรายการ FR7 ข้อ 10)
   public void saveOrder()
   {
      // ตัดรายการที่ถูกยกเลิกจนจำนวนเหลือ 0 ออกจากออเดอร์
      List<Basket> remaining = new ArrayList<Basket>();
      for (Basket line : basket)
         if (line.getMenuQuantity() > 0)
            remaining.add(line);
      basket = remaining.toArray(new Basket[0]);

      orderTotalItems = basket.length; // จำนวนรายการอาหาร เช่น 3 รายการ
      double subTotal = 0.0;
      for (Basket line : basket)
         subTotal += line.getItemPrice(); // รวมราคาทุกรายการ เช่น 70+180+40 = 290
      orderSubTotal = subTotal;
   }

   // นำรายการตะกร้ารอบใหม่เข้าออเดอร์ แล้วบันทึก (เรียกตอน SEND ใน FR1 textmode 142)
   public void addBasketItems(Basket[] newItems)
   {
      Basket[] merged = Arrays.copyOf(basket, basket.length + newItems.length);
      System.arraycopy(newItems, 0, merged, basket.length, newItems.length);
      basket = merged;
      saveOrder();
   }

   // + confirmKitchenOrder(): void
   // Responsibility 23 "Confirm Kitchen Order" / UseCase FR8 ข้อ 5-6
   // ยืนยันทุกรายการที่ "รอยืนยัน" เป็น "ยืนยันแล้ว" (คำสั่ง ALL ใน textmode 161)
   public void confirmKitchenOrder()
   {
      for (Basket line : basket)
         if (Basket.STATUS_WAITING.equals(line.getCookingStatus()))
            line.updateCookingStatus(Basket.STATUS_CONFIRMED);
   }

   // รายการทั้งหมดของตะกร้ารอบหนึ่ง เช่น BSK-001 (FR7 เลือกตะกร้า textmode 159)
   public Basket[] getBasketItems(String basketId)
   {
      List<Basket> lines = new ArrayList<Basket>();
      for (Basket line : basket)
         if (line.getBasketId().equals(basketId))
            lines.add(line);
      return lines.toArray(new Basket[0]);
   }

   // รหัสตะกร้าทุกรอบในออเดอร์ (ไม่ซ้ำ เรียงตามลำดับการสั่ง) เช่น [BSK-001, BSK-002]
   public List<String> getBasketIds()
   {
      List<String> ids = new ArrayList<String>();
      for (Basket line : basket)
         if (!ids.contains(line.getBasketId()))
            ids.add(line.getBasketId());
      return ids;
   }

   // จำนวนชิ้นรวมทั้งหมด เช่น "(รวม 5 ชิ้น)" (textmode 142)
   public int getTotalQuantity()
   {
      int total = 0;
      for (Basket line : basket)
         total += line.getMenuQuantity();
      return total;
   }

   // บันทึกผลสรุปยอดชำระ (เรียกจาก Payment.summarizePaymentAmount - FR6 ข้อ 6)
   public void setPaymentSummary(Double discount, Double netAfterDiscount,
      Double vatAmount, Double grandTotal)
   {
      orderDiscount = discount;
      orderNetAfterDiscount = netAfterDiscount;
      orderVatAmount = vatAmount;
      orderGrandTotal = grandTotal;
   }

   public String getOrderId() { return orderId; } // getter: คืนค่า รหัสออเดอร์
   public String getTableId() { return tableId; } // getter: คืนค่า หมายเลขโต๊ะ
   public Integer getOrderTotalItems() { return orderTotalItems; } // getter: คืนค่า จำนวนรายการอาหารทั้งหมด
   public Double getOrderSubTotal() { return orderSubTotal; } // getter: คืนค่า ยอดรวมก่อนหักส่วนลด
   public Double getOrderDiscount() { return orderDiscount; } // getter: คืนค่า ส่วนลดสมาชิก
   public Double getOrderNetAfterDiscount() { return orderNetAfterDiscount; } // getter: คืนค่า ยอดหลังหักส่วนลด
   public Double getOrderVatAmount() { return orderVatAmount; } // getter: คืนค่า ภาษีมูลค่าเพิ่ม
   public Double getOrderGrandTotal() { return orderGrandTotal; } // getter: คืนค่า ยอดสุทธิที่ต้องชำระ
   public Table getTable() { return table; } // getter: คืนค่า Table (ส่วนประกอบ composition)
   public OrderSlip getOrderSlip() { return orderSlip; } // getter: คืนค่า Order Slip (ส่วนประกอบ composition)
   public SalesEmployee getSalesEmployee() { return salesEmployee; } // getter: คืนค่า Sales Employee (ส่วนประกอบ composition)
   public Basket[] getBasket() { return basket; } // getter: คืนค่า Basket (ส่วนประกอบ composition)
   public Customer getCustomer() { return customer; } // getter: คืนค่า Customer (ส่วนประกอบ composition)
   public Payment getPayment() { return payment; } // getter: คืนค่า Payment (ส่วนประกอบ composition)

   public void setOrderSlip(OrderSlip theSlip) { orderSlip = theSlip; } // setter: แก้ไขค่า Order Slip (ส่วนประกอบ composition)
   public void setPayment(Payment thePayment) { payment = thePayment; } // setter: แก้ไขค่า Payment (ส่วนประกอบ composition)
} // end class Order
