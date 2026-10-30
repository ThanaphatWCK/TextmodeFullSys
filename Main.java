// Main.java
// คลาสรันระบบหลักของ "ซอยถี่ อาหารอีสาน (Text Mode) ระบบจัดการร้านอาหาร"
// ที่มา: เทียบ ATM.java + ATMCaseStudy.java ใน atmCaseStudyCode
//   - main() สร้างระบบแล้วเรียก run()  (เหมือน ATMCaseStudy.main -> theATM.run())
//   - run() วนแสดงหน้าจอหลักและแยกงานตามเมนู (เหมือน ATM.run / performTransactions)
//   - เก็บข้อมูลไว้ในหน่วยความจำ และสร้างข้อมูลตั้งต้นตอนเริ่ม (เหมือน BankDatabase ใน ATM)
// การรับค่าใช้ Scanner และการแสดงผลใช้ System.out โดยตรงในคลาสนี้
//   (ไม่สร้างคลาส Screen/Keypad เพิ่ม เพื่อให้มีเฉพาะ 22 คลาสตาม Class Diagram + คลาสรันระบบ)
//
// เมนูหลักตาม textmode 140 และ use case diagram (usecaseDiagram.jpg):
//   [1] FR1 รับออเดอร์ / FR7 ยกเลิกรายการ   (พนักงานขาย)
//   [2] FR2 จัดการสมาชิก คะแนนแต้มสะสม      (พนักงานขาย)
//   [6] FR6 ชำระเงิน                        (พนักงานขาย)
//   [C] FR8 จัดการออเดอร์อาหารในครัว        (พนักงานครัว)
//   [M] จัดการร้าน -> [3] FR3, [4] FR4, [5] FR5 (ผู้จัดการร้าน)

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

public class Main
{
   private static final String DOUBLE_LINE = "==================================================";
   private static final String SINGLE_LINE = "--------------------------------------------------";
   // ล้างหน้าจอได้เฉพาะเมื่อรันในหน้าต่าง console จริง (ถ้ารันแบบส่งข้อมูลผ่านไฟล์ จะไม่ล้าง)
   private static final boolean CAN_CLEAR_SCREEN = System.console() != null;
   private static boolean inputSinceClear = true;    // ผู้ใช้พิมพ์อะไรไปแล้วหลังล้างจอครั้งล่าสุด
   private static boolean messageSinceInput = false; // มีข้อความ [ระบบ] ที่ผู้ใช้ยังไม่ได้อ่าน
   private static final String SHOP_TITLE = "ซอยถี่ อาหารอีสาน (Text Mode)";

   private Scanner input; // รับค่าจากคีย์บอร์ด (เทียบ Keypad ใน ATM)

   // ===== ข้อมูลของร้าน (object ของ 22 คลาสตาม Class Diagram) =====
   private List<Table> tables = new ArrayList<Table>();
   private List<Menu> menus = new ArrayList<Menu>();
   private List<Ingredient> ingredients = new ArrayList<Ingredient>();
   private List<Member> members = new ArrayList<Member>();
   private List<Customer> customers = new ArrayList<Customer>();
   private List<Order> orders = new ArrayList<Order>();
   private List<OrderSlip> orderSlips = new ArrayList<OrderSlip>();
   private List<CancelBasketItem> cancelBasketItems = new ArrayList<CancelBasketItem>();
   private List<Payment> payments = new ArrayList<Payment>();
   private List<Receipt> receipts = new ArrayList<Receipt>();
   private List<PointRedemption> pointRedemptions = new ArrayList<PointRedemption>();
   private List<StockNotification> stockNotifications = new ArrayList<StockNotification>();
   private List<IngredientOrder> ingredientOrders = new ArrayList<IngredientOrder>();
   private List<StockReceiving> stockReceivings = new ArrayList<StockReceiving>();
   private List<SalesReport> salesReports = new ArrayList<SalesReport>();

   private Supplier supplier;               // SUP-001 เบทาโกร (textmode 148)
   private SalesEmployee salesEmployee;     // EMP-01 พนักงานขาย (textmode 142)
   private KitchenEmployee kitchenEmployee; // CHEF-01 พนักงานครัว (textmode 161)
   private Manager manager;                 // MGR-01 ผู้จัดการร้าน (textmode 151)
   private PaymentMethod cashMethod;        // PM-01 เงินสด (Cash) (332.jpg)
   private PaymentMethod qrMethod;          // PM-02 QR PromptPay (332.jpg)
   private PaymentStatus pendingStatus;     // PS-01 รอชำระเงิน (332.jpg)
   private PaymentStatus paidStatus;        // PS-02 ชำระเงินสำเร็จ (332.jpg)

   // ===== ข้อมูลประกอบที่หน้าจอ textmode ต้องใช้ แต่ "ไม่มี" attribute ใน Class Diagram =====
   // สูตรอาหาร: ปริมาณวัตถุดิบต่อ 1 จาน (textmode 147: ลาบเป็ด ใช้เนื้อเป็ด 0.1 กก. ฯลฯ)
   private Map<String, Map<String, Double>> recipes = new HashMap<String, Map<String, Double>>();
   // ราคาต่อหน่วยจากผู้จัดจำหน่าย (textmode 148: น้ำปลา 10 ขวด = 320 บาท -> 32 บาท/ขวด)
   private Map<String, Double> unitCosts = new HashMap<String, Double>();
   // เวลาที่ส่งตะกร้าเข้าครัว (textmode 159 "สั่งเมื่อ 12:00 น." และ 161 คอลัมน์ "เวลา")
   private Map<String, Date> basketSentTimes = new HashMap<String, Date>();
   // รายการในตะกร้าที่ตัดสต็อกแล้ว (กันตัดซ้ำ - UseCase FR3 ข้อ 1)
   private Set<Basket> deductedLines =
      Collections.newSetFromMap(new IdentityHashMap<Basket, Boolean>());
   // ออเดอร์ที่ PointRedemption แต่ละรายการอ้างอิง (PointRedemption ไม่มี orderId ใน diagram)
   private Map<PointRedemption, String> redemptionOrderIds =
      new IdentityHashMap<PointRedemption, String>();
   // ตัวนับรหัส เช่น "ORD" -> 1 แปลว่าใช้ ORD-001 ไปแล้ว
   private Map<String, Integer> idCounters = new HashMap<String, Integer>();

   // ===================================================================
   //  main และ run (เทียบ ATMCaseStudy.main และ ATM.run)
   // ===================================================================

   public static void main(String[] args) throws Exception
   {
      // ตั้งให้ System.out แสดงผลแบบ UTF-8 เพื่อให้ภาษาไทยแสดงถูกต้อง
      System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, "UTF-8"));
      Main app = new Main();
      app.run();
   }

   // constructor: เตรียม Scanner และสร้างข้อมูลตั้งต้น (เทียบ ATM() + BankDatabase())
   public Main()
   {
      input = new Scanner(System.in, "UTF-8");
      seedData();
   }

   // วนแสดงหน้าจอหลักไปเรื่อย ๆ (เหมือน ATM.run ที่วน while(true))
   public void run()
   {
      while (true)
      {
         String choice = displayMainMenu().toUpperCase();
         switch (choice)
         {
            case "1": takeOrder(); break;           // FR1 (และเข้าสู่ FR7 เมื่อเลือกโต๊ะที่ใช้งาน)
            case "2": manageMembers(); break;       // FR2
            case "6": processPayment(); break;      // FR6
            case "C": manageKitchenOrders(); break; // FR8
            case "M": manageShop(); break;          // เมนูผู้จัดการร้าน FR3/FR4/FR5
            default:
               systemMessage("กรุณาเลือกรายการให้ถูกต้อง (1, 2, 6, C, M)");
         }
      }
   }

   // หน้าจอหลัก ตาม textmode 140 (ซ้าย)
   private String displayMainMenu()
   {
      header(SHOP_TITLE, "ระบบจัดการร้านอาหาร");
      System.out.println(" [ พนักงานขาย ]");
      System.out.println("        [1] รับออเดอร์/ยกเลิก (สั่งอาหารหรือเครื่องดื่ม)");
      System.out.println("        [2] สมาชิก (สมัคร/สะสมแต้ม)");
      System.out.println("        [6] ชำระเงิน (คิดเงิน/พิมพ์บิล)");
      System.out.println(SINGLE_LINE);
      System.out.println(" [ พนักงานครัว ]");
      System.out.println("        [C] จัดการออเดอร์อาหารในครัว");
      System.out.println(" [ ผู้จัดการร้าน ]");
      System.out.println("        [M] จัดการร้าน (เข้าสู่เมนูสำหรับเจ้าของร้าน)");
      System.out.println(DOUBLE_LINE);
      return prompt(" กรุณาเลือกรายการที่ต้องการดำเนินการ:");
   }

   // [M] จัดการร้าน - เมนูสำหรับผู้จัดการร้าน ตาม textmode 140 (ขวา)
   private void manageShop()
   {
      while (true)
      {
         header("จัดการร้าน", "เมนูสำหรับผู้จัดการร้าน");
         System.out.println(" [3] แจ้งเตือนสต็อก (เพิ่ม/แก้ไข/ลบ/แจ้งเตือนสต็อกวัตถุดิบ)");
         System.out.println(" [4] จัดการเมนูอาหาร (เพิ่ม/แก้ไข/ลบ/กำหนดราคาเมนูอาหาร)");
         System.out.println(" [5] รายงานยอดขาย (สรุปยอด/Export PDF)");
         System.out.println(" [B] กลับหน้าหลัก");
         System.out.println(DOUBLE_LINE);
         String choice = prompt(" กรุณาเลือกเมนู :").toUpperCase();
         switch (choice)
         {
            case "3": manageStockNotification(); break; // FR3
            case "4": manageMenu(); break;              // FR4
            case "5": viewSalesReport(); break;         // FR5
            case "B": return;
            default: systemMessage("กรุณาเลือกเมนูให้ถูกต้อง (3, 4, 5, B)");
         }
      }
   }

   // ===================================================================
   //  FR1 รับออเดอร์อาหารหรือเครื่องดื่ม  (usecase_Scenario/120.jpg, textmode 141-143)
   //  Actor: พนักงานขาย
   // ===================================================================

   private void takeOrder()
   {
      // UseCase FR1 ข้อ 2: "ระบบ แสดง รายการโต๊ะที่มีสถานะว่างหรือสถานะใช้งาน"
      Table table = selectTable();
      if (table == null)
         return;

      Order activeOrder = findActiveOrderByTable(table.getTableId());
      if (activeOrder == null)
         takeNewBasketRound(table, null);   // โต๊ะว่าง -> FR1 ข้อ 3 สร้างออเดอร์ใหม่
      else
         manageExistingOrder(activeOrder);  // โต๊ะใช้งาน -> FR7 ยกเลิกรายการ / สั่งเพิ่ม (textmode 158)
   }

   // หน้าจอ "เลือกหมายเลขโต๊ะ" (textmode 141 และ 158)
   private Table selectTable()
   {
      header(SHOP_TITLE, "เลือกหมายเลขโต๊ะ");
      for (int i = 0; i < tables.size(); i++)
      {
         Table table = tables.get(i);
         System.out.print(" โต๊ะ[" + (i + 1) + "] ");
         table.displayTableInformation(); // Table.displayTableInformation() ตาม Class Diagram
         Order order = findActiveOrderByTable(table.getTableId());
         if (order != null)
            System.out.print(" (ออเดอร์: " + order.getOrderId() + ")");
         System.out.println();
      }
      System.out.println(DOUBLE_LINE);
      String choice = prompt("กรุณาพิมพ์หมายเลขโต๊ะที่ต้องการเลือก:");
      int index = parseIntOrDefault(choice, 0);
      if (index < 1 || index > tables.size())
      {
         systemMessage("ไม่พบหมายเลขโต๊ะ กลับสู่หน้าหลัก");
         return null;
      }
      return tables.get(index - 1);
   }

   // รับตะกร้าสั่งอาหาร 1 รอบ (ใช้ทั้งออเดอร์ใหม่ และการสั่งเพิ่ม [ADD] ของออเดอร์เดิม)
   private void takeNewBasketRound(Table table, Order existingOrder)
   {
      // แสดงรหัสที่จะใช้ก่อนบันทึกจริง (textmode 141-142 แสดง BSK-001 / ORD-001 ตั้งแต่ก่อนยืนยัน)
      String orderId = (existingOrder != null) ? existingOrder.getOrderId() : peekId("ORD", 3);
      String customerId = (existingOrder != null)
         ? existingOrder.getCustomer().getCustomerId() : peekId("CUST", 3);
      String basketId = peekId("BSK", 3);

      // ตะกร้ารอบนี้ (1 แถวต่อ 1 เมนู) เก็บตามลำดับที่เลือก
      Map<String, Basket> cart = new LinkedHashMap<String, Basket>();

      boolean showMenu = true;
      while (true)
      {
         if (showMenu)
            selectMenuItems(table, basketId, orderId, cart); // FR1 ข้อ 4-5

         // FR1 ข้อ 6-8: แสดงตะกร้าและยอดรวม แล้วรอคำสั่ง (textmode 142 ซ้าย)
         displayBasketScreen(orderId, customerId, table.getTableId(), basketId,
            cart.values().toArray(new Basket[0]));
         String command = prompt("กรุณาเลือกคำสั่งดำเนินการ:");
         String upper = command.toUpperCase();

         if (upper.equals("ADD"))          // กลับไปหน้าเมนูเพื่อสั่งเพิ่ม
         {
            showMenu = true;
            continue;
         }
         showMenu = false;

         if (upper.endsWith(" D"))         // "[รหัสเมนู] [จำนวน] [D]" ลดจำนวนในตะกร้าที่ยังไม่ส่ง
         {
            removeFromCart(cart, command);
            continue;
         }
         if (!upper.equals("Y"))
         {
            systemMessage("คำสั่งไม่ถูกต้อง");
            continue;
         }
         if (cart.isEmpty())
         {
            systemMessage("ตะกร้ายังไม่มีรายการอาหาร กรุณาเลือกเมนู");
            showMenu = true;
            continue;
         }

         Basket[] lines = cart.values().toArray(new Basket[0]);
         double basketTotal = sumItemPrice(lines);

         // FR1 ข้อ 7-9: ตรวจสอบและยืนยันตะกร้าสั่งอาหาร (textmode 142 กลาง)
         header("ตรวจสอบและยืนยันตะกร้าสั่งอาหาร");
         System.out.println("รหัสตะกร้า: " + basketId + "          รหัสโต๊ะ: " + table.getTableId());
         System.out.println("รหัสออเดอร์: " + orderId);
         System.out.println(SINGLE_LINE);
         System.out.println("          รายการทั้งหมด " + lines.length + " รายการ");
         System.out.println("              (รวม " + sumQuantity(lines) + " ชิ้น)");
         System.out.println("          ยอดรวม " + formatPrice(basketTotal) + " บาท");
         System.out.println(DOUBLE_LINE);
         if (!prompt("     พิมพ์ 'Y' เพื่อกดยืนยันตะกร้าสั่งอาหาร").equalsIgnoreCase("Y"))
            continue; // ไม่ยืนยัน -> กลับไปหน้าตะกร้า

         // FR1 ข้อ 10: แจ้งราคาให้ลูกค้าทราบและส่งตะกร้าเข้าสู่ระบบ (textmode 142 ขวา)
         header("ประมวลผลและบันทึกตะกร้าส่งไปยังครัว");
         System.out.println("[ระบบ]:  ประมวลผลข้อมูลตะกร้า: " + basketId + " เข้าสู่ออเดอร์: "
            + orderId + " เรียบร้อย..");
         System.out.println(SINGLE_LINE);
         System.out.println("          แจ้งราคากับลูกค้า: " + formatNumber(basketTotal) + " บาท");
         System.out.println(SINGLE_LINE);
         System.out.println("     ส่งออเดอร์เข้าสู่ระบบและห้องครัวหรือไม่?");
         System.out.println(DOUBLE_LINE);
         if (!prompt(" พิมพ์ 'SEND' เพื่อบันทึกและส่งออเดอร์เข้าครัว").equalsIgnoreCase("SEND"))
         {
            systemMessage("ยังไม่ส่งออเดอร์ กลับไปหน้าตะกร้า");
            continue;
         }

         // FR1 ข้อ 11: ระบบบันทึกตะกร้าเข้าสู่ออเดอร์ และส่งรายการไปยังพนักงานครัว
         Order order = existingOrder;
         if (order == null)
         {
            Customer customer = new Customer(nextId("CUST", 3), null); // ลูกค้าที่นั่งโต๊ะนี้
            customers.add(customer);
            order = new Order(nextId("ORD", 3), table, salesEmployee, customer);
            orders.add(order);
            table.setTableStatus(Table.STATUS_IN_USE); // โต๊ะเปลี่ยนเป็น "ใช้งาน"
         }
         nextId("BSK", 3); // จองรหัสตะกร้าที่แสดงไปแล้ว
         for (Basket line : lines)
            line.updateCookingStatus(Basket.STATUS_WAITING); // ส่งเข้าครัว สถานะ "รอยืนยัน"
         order.addBasketItems(lines);  // -> Order.saveOrder() คำนวณยอดออเดอร์ใหม่
         basketSentTimes.put(basketId, new Date());

         // FR1 ข้อ 12: แสดงสถานะการรับออเดอร์สำเร็จ (textmode 143 ซ้าย)
         header("แสดงสถานะการรับออเดอร์สำเร็จ");
         System.out.println(" [ SUCCESS ] บันทึกตะกร้าเข้าสู่ออเดอร์ " + order.getOrderId() + " สำเร็จ!");
         System.out.println();
         System.out.println("         ส่งรายการไปยังจอครัวเรียบร้อย");
         System.out.println(SINGLE_LINE);
         systemMessage("กำลังพิมพ์ใบสั่งอาหาร...");

         // FR1 ข้อ 13: ส่งมอบใบสั่งอาหารพร้อมรหัสออเดอร์ให้ลูกค้า (textmode 143 ขวา)
         OrderSlip slip = new OrderSlip(nextId("SLIP", 3), order.getOrderId(),
            table.getTableId(), salesEmployee.getEmployeeId(), basketTotal);
         orderSlips.add(slip);
         order.setOrderSlip(slip);
         newPage();
         slip.printOrderSlip(basketId, lines, false);
         System.out.println("     [ระบบ]: ใบสั่งอาหารพิมพ์สำเร็จ (Order ID: #" + order.getOrderId() + ")");
         prompt("      กด Enter เพื่อกลับสู่หน้าหลัก...");
         return; // FR1 ข้อ 14 จบการทำงาน
      }
   }

   // FR1 ข้อ 4-5: แสดงเมนูและรับ "[รหัสเมนู] [จำนวน]" จนกว่าจะพิมพ์ END (textmode 141 ขวา)
   private void selectMenuItems(Table table, String basketId, String orderId,
      Map<String, Basket> cart)
   {
      header("แสดงเมนูอาหารและเลือกใส่ตะกร้า");
      System.out.println("        รหัสโต๊ะ: " + table.getTableId() + "        รหัสตะกร้า: " + basketId);
      System.out.println(SINGLE_LINE);
      System.out.println("        รหัสเมนู  |    รายการ     |    ราคา");
      System.out.println(SINGLE_LINE);
      for (Menu menu : menus)
         menu.displayMenuInformation(); // Menu.displayMenuInformation() ตาม Class Diagram
      System.out.println(DOUBLE_LINE);
      System.out.println("รูปแบบการป้อนข้อมูล:");
      System.out.println("   พิมพ์ [รหัสเมนู] [จำนวน]");
      System.out.println("(พิมพ์ END เพื่อเสร็จสิ้น)");
      System.out.println("      " + SINGLE_LINE);
      System.out.println("กรุณาป้อนข้อมูล:");
      while (true)
      {
         System.out.print("  > ");
         String line = readLine();
         if (line.equalsIgnoreCase("END"))
            return;
         String[] parts = line.split("\\s+");
         Menu menu = (parts.length == 2) ? findMenu(parts[0]) : null;
         int quantity = (parts.length == 2) ? parseIntOrDefault(parts[1], 0) : 0;
         if (menu == null || quantity <= 0)
         {
            systemMessage("รูปแบบไม่ถูกต้อง หรือไม่พบรหัสเมนู (ตัวอย่าง: M-01 1)");
            continue;
         }
         if (Menu.STATUS_SOLD_OUT.equals(menu.getMenuStatus()))
         {
            systemMessage("เมนู \"" + menu.getMenuName() + "\" หมด ไม่สามารถสั่งได้");
            continue;
         }
         Basket item = cart.get(menu.getMenuId());
         if (item == null)
         {
            item = new Basket(basketId, orderId, table.getTableId());
            cart.put(menu.getMenuId(), item);
         }
         item.selectAndAddItemsToBasket(menu, quantity); // Basket.selectAndAddItemsToBasket()
         item.calculateAndSaveBasket();                  // Basket.calculateAndSaveBasket()
      }
   }

   // คำสั่ง "M-02 1 D" ในตะกร้าที่ยังไม่ส่งครัว: ลดจำนวน/ลบออกจากตะกร้า
   private void removeFromCart(Map<String, Basket> cart, String command)
   {
      String[] parts = command.split("\\s+");
      Menu menu = (parts.length == 3) ? findMenu(parts[0]) : null;
      int quantity = (parts.length == 3) ? parseIntOrDefault(parts[1], 0) : 0;
      Basket item = (menu == null) ? null : cart.get(menu.getMenuId());
      if (item == null || quantity <= 0 || quantity > item.getMenuQuantity())
      {
         systemMessage("ไม่พบรายการ หรือจำนวนที่ยกเลิกไม่ถูกต้อง");
         return;
      }
      item.reduceQuantity(quantity);
      if (item.getMenuQuantity() == 0)
         cart.remove(menu.getMenuId());
   }

   // หน้าจอ "แสดงตะกร้าสั่งอาหารและยอดรวม" ใช้ร่วมกัน FR1 (textmode 142) และ FR7 (textmode 159)
   private void displayBasketScreen(String orderId, String customerId, String tableId,
      String basketId, Basket[] lines)
   {
      header("แสดงตะกร้าสั่งอาหารและยอดรวม");
      System.out.println(" รหัสออเดอร์: " + orderId + "  รหัสพนักงานรับออเดอร์: "
         + salesEmployee.getEmployeeId());
      System.out.println(" รหัสลูกค้า: " + customerId + "    รหัสโต๊ะ: " + tableId);
      System.out.println();
      System.out.println("            ตะกร้าสั่งอาหาร (" + basketId + ")");
      System.out.println(SINGLE_LINE);
      System.out.println("      รหัสเมนู  |    รายการ    | จำนวน |  ราคา");
      System.out.println(SINGLE_LINE);
      for (Basket line : lines)
         line.displayBasketInformation(); // Basket.displayBasketInformation() ตาม Class Diagram
      System.out.println(SINGLE_LINE);
      System.out.println(String.format("      ยอดรวม                   |  %3d  |  %s",
         sumQuantity(lines), formatPrice(sumItemPrice(lines))));
      System.out.println(DOUBLE_LINE);
      System.out.println("[ตัวเลือกดำเนินการ]");
      System.out.println(" หากต้องการยกเลิก:");
      System.out.println("     พิมพ์ [รหัสเมนู] [จำนวน] [D]");
      System.out.println("พิมพ์ [Y] เพื่อถัดไป");
      System.out.println("หรือ [ADD] เพื่อไปหน้าแสดงเมนูอาหารและสั่งเพิ่มใส่ตะกร้า");
      System.out.println();
   }

   // ===================================================================
   //  FR7 ยกเลิกรายการอาหาร  (usecase_Scenario/133.jpg, textmode 158-160)
   //  Actor: พนักงานขาย  (เข้าจากเมนู [1] แล้วเลือกโต๊ะที่ "ใช้งาน")
   // ===================================================================

   private void manageExistingOrder(Order order)
   {
      List<String> basketIds = order.getBasketIds();
      if (basketIds.isEmpty())
      {
         takeNewBasketRound(order.getTable(), order);
         return;
      }

      // หมายเหตุ textmode 159: กรณี 1 โต๊ะมีหลายตะกร้า ระบบจะแสดงรายการตะกร้าให้เลือกก่อน
      String basketId = basketIds.get(0);
      if (basketIds.size() > 1)
      {
         header(SHOP_TITLE, "เลือกตะกร้าที่ต้องการยกเลิกรายการสั่งอาหาร");
         System.out.println("     รหัสออเดอร์: " + order.getOrderId() + "     รหัสโต๊ะ: "
            + order.getTableId());
         System.out.println(SINGLE_LINE);
         for (int i = 0; i < basketIds.size(); i++)
         {
            Basket[] lines = order.getBasketItems(basketIds.get(i));
            System.out.println(" [" + (i + 1) + "] ตะกร้า: " + basketIds.get(i) + " (สั่งเมื่อ "
               + formatTime(basketSentTimes.get(basketIds.get(i))) + " น. | " + lines.length
               + " รายการ | " + basketGroupStatus(lines) + ")");
         }
         System.out.println(SINGLE_LINE);
         int index = parseIntOrDefault(prompt(" กรุณาพิมพ์หมายเลขตะกร้าที่ต้องการจัดการ:"), 0);
         if (index < 1 || index > basketIds.size())
         {
            systemMessage("ไม่พบหมายเลขตะกร้า กลับสู่หน้าหลัก");
            return;
         }
         basketId = basketIds.get(index - 1);
      }

      // FR7 ข้อ 4: ระบบแสดงรายการตะกร้าสั่งอาหารของโต๊ะที่เลือก (textmode 159 ขวา)
      Basket[] lines = order.getBasketItems(basketId);
      List<CancelBasketItem> requests = new ArrayList<CancelBasketItem>();
      Map<String, Integer> requestedQuantity = new HashMap<String, Integer>();
      displayBasketScreen(order.getOrderId(), order.getCustomer().getCustomerId(),
         order.getTableId(), basketId, lines);
      System.out.println("กรุณาเลือกคำสั่งดำเนินการ:");
      while (true)
      {
         System.out.print("     > ");
         String command = readLine();
         String upper = command.toUpperCase();
         if (upper.equals("Y"))
            break;
         if (upper.equals("ADD"))
         {
            takeNewBasketRound(order.getTable(), order); // สั่งเพิ่มเป็นตะกร้ารอบใหม่
            return;
         }
         // FR7 ข้อ 6: พนักงานขายระบุรหัสเมนูที่ต้องการยกเลิก "[รหัสเมนู] [จำนวน] [D]"
         String[] parts = command.split("\\s+");
         Basket target = null;
         if (parts.length == 3 && parts[2].equalsIgnoreCase("D"))
            for (Basket line : lines)
               if (line.getMenuId().equalsIgnoreCase(parts[0]))
                  target = line;
         int quantity = (parts.length == 3) ? parseIntOrDefault(parts[1], 0) : 0;
         if (target == null || quantity <= 0)
         {
            systemMessage("รูปแบบไม่ถูกต้อง (ตัวอย่าง: M-02 1 D) หรือไม่พบเมนูในตะกร้านี้");
            continue;
         }
         int already = requestedQuantity.containsKey(target.getMenuId())
            ? requestedQuantity.get(target.getMenuId()) : 0;
         if (quantity + already > target.getMenuQuantity())
         {
            systemMessage("จำนวนที่ยกเลิกมากกว่าจำนวนที่สั่ง");
            continue;
         }
         // FR7 ข้อ 5: ตรวจสอบกับพนักงานครัว - รายการที่ทำเสร็จแล้วยกเลิกไม่ได้
         if (Basket.STATUS_DONE.equals(target.getCookingStatus()))
         {
            systemMessage("รายการนี้ครัวทำเสร็จแล้ว ไม่สามารถยกเลิกได้");
            continue;
         }
         requestedQuantity.put(target.getMenuId(), quantity + already);
         requests.add(new CancelBasketItem(null, order.getOrderId(), salesEmployee, lines,
            target.getMenuName(), quantity));
         systemMessage("รับคำขอยกเลิก [" + target.getMenuId() + "] " + target.getMenuName()
            + " " + quantity + " จาน (พิมพ์ Y เพื่อถัดไป)");
      }

      if (requests.isEmpty())
      {
         systemMessage("ไม่มีรายการที่ต้องการยกเลิก กลับสู่หน้าหลัก");
         return;
      }

      boolean anyCancelled = false;
      for (CancelBasketItem request : requests)
      {
         String cancelId = peekId("CNL", 3);
         // FR7 ข้อ 7-8: หน้าจอตรวจสอบและยืนยันการยกเลิก (textmode 160 ซ้าย)
         header(SHOP_TITLE, "ตรวจสอบและยืนยันการยกเลิก");
         System.out.println("จากออเดอร์: " + order.getOrderId() + " รหัสตะกร้า: " + basketId);
         System.out.println("รหัสการยกเลิก: " + cancelId);
         System.out.println("        +------------------------------+");
         System.out.println("        |   รหัสเมนู: " + findMenuIdByName(lines, request.getMenuName()));
         System.out.println("        |   รายการ: " + request.getMenuName());
         System.out.println("        |   จำนวนที่ยกเลิก: " + request.getCancelQuantity() + " จาน");
         System.out.println("        +------------------------------+");
         System.out.println(SINGLE_LINE);
         System.out.println(" [ ตัวเลือกดำเนินการ ]");
         System.out.println("   [Y] ยืนยันรายการถูกต้อง");
         System.out.println("   [N] ยกเลิก / เปลี่ยนใจ");
         System.out.println(SINGLE_LINE);
         if (!prompt("กรุณาพิมพ์คำสั่งดำเนินการ:").equalsIgnoreCase("Y"))
         {
            systemMessage("ไม่ยกเลิกรายการ " + request.getMenuName());
            continue;
         }

         // FR7 ข้อ 9: บันทึกข้อมูลการยกเลิก และส่งรายการแจ้งยกเลิกไปยังครัว (textmode 160 กลาง)
         header(SHOP_TITLE, "บันทึกการยกเลิกรายการอาหารในตะกร้า");
         System.out.println(" รหัสการยกเลิก: " + cancelId);
         System.out.println(" พนักงานที่ทำรายการ: " + salesEmployee.getEmployeeId());
         System.out.println(" รหัสตะกร้า: " + basketId + "| รหัสออเดอร์: " + order.getOrderId());
         System.out.println();
         System.out.println("[ยืนยันการยกเลิก]");
         System.out.println("  รหัสเมนู: " + findMenuIdByName(lines, request.getMenuName()) + " ("
            + request.getMenuName() + " " + request.getCancelQuantity() + " จาน)");
         System.out.println();
         System.out.println(" [ ตัวเลือกดำเนินการ ]");
         System.out.println("   [S] บันทึกและส่งข้อมูลการยกเลิก");
         System.out.println(SINGLE_LINE);
         if (!prompt(" กรุณาพิมพ์คำสั่งดำเนินการ:").equalsIgnoreCase("S"))
         {
            systemMessage("ไม่ได้บันทึกการยกเลิก");
            continue;
         }
         CancelBasketItem cancel = new CancelBasketItem(nextId("CNL", 3), order.getOrderId(),
            salesEmployee, lines, request.getMenuName(), request.getCancelQuantity());
         cancel.cancelBasketItem(); // CancelBasketItem.cancelBasketItem() ตาม Class Diagram
         cancelBasketItems.add(cancel);
         systemMessage("บันทึกการยกเลิกเรียบร้อย...");
         systemMessage("กำลังส่งรายการแจ้งยกเลิกไปยังครัว...");
         systemMessage("[SUCCESS] แจ้งเตือนครัวสำเร็จ!");
         anyCancelled = true;
      }
      if (!anyCancelled)
         return;

      // FR7 ข้อ 10: อัปเดตตะกร้าสั่งอาหารของโต๊ะ และพิมพ์ใบสั่งอาหารส่งครัว (textmode 160 ขวา)
      order.saveOrder(); // Order.saveOrder() คำนวณยอดใหม่ และตัดรายการที่จำนวนเหลือ 0
      Basket[] remaining = order.getBasketItems(basketId);
      OrderSlip slip = new OrderSlip(nextId("SLIP", 3), order.getOrderId(), order.getTableId(),
         salesEmployee.getEmployeeId(), sumItemPrice(remaining));
      orderSlips.add(slip);
      order.setOrderSlip(slip);
      newPage();
      slip.printOrderSlip(basketId, remaining, true);
      systemMessage("พิมพ์ใบสั่งอาหารใหม่ส่งครัว (" + slip.getSlipId() + ")... เรียบร้อย");

      if (order.getBasket().length == 0) // ยกเลิกหมดทั้งออเดอร์ -> คืนโต๊ะเป็นว่าง
      {
         orders.remove(order);
         order.getTable().setTableStatus(Table.STATUS_AVAILABLE);
         systemMessage("ออเดอร์ " + order.getOrderId() + " ไม่มีรายการเหลือ คืนสถานะโต๊ะเป็น \"ว่าง\"");
      }
      waitForZero("[ตัวเลือกดำเนินการ]\n[0] กลับสู่หน้าหลัก");
   }

   // ===================================================================
   //  FR2 จัดการสมาชิก คะแนนแต้มสะสม  (usecase_Scenario/122.jpg, textmode 144-146)
   //  Actor: พนักงานขาย
   // ===================================================================

   private void manageMembers()
   {
      while (true)
      {
         // FR2 ข้อ 4: ระบบแสดงหน้าเมนูการจัดการสมาชิก (textmode 144 กลาง)
         header(SHOP_TITLE, "การจัดการสมาชิก");
         System.out.println(" [ ตัวเลือกดำเนินการ ]");
         System.out.println("   [1] ค้นหาข้อมูลสมาชิก (เบอร์โทรศัพท์ / ชื่อ)");
         System.out.println("   [2] สมัครสมาชิกใหม่ (สำหรับลูกค้าใหม่)");
         System.out.println("   [0] ย้อนกลับหน้าหลัก");
         System.out.println(DOUBLE_LINE);
         String choice = prompt("กรุณาเลือกรายการที่ต้องการดำเนินการ:");
         if (choice.equals("1"))
         {
            // FR2 ข้อ 5: ค้นหาข้อมูลสมาชิกด้วยเบอร์โทรศัพท์หรือชื่อสมาชิก
            Member member = searchMember(prompt("[ระบบ]: กรุณาพิมพ์เบอร์โทรศัพท์ หรือ ชื่อสมาชิก:"));
            if (member == null)
               systemMessage("ไม่พบข้อมูลสมาชิก");
            else
               showMemberDetail(member);
         }
         else if (choice.equals("2"))
            registerNewMember();
         else if (choice.equals("0"))
            return;
         else
            systemMessage("กรุณาเลือกรายการให้ถูกต้อง");
      }
   }

   // FR2 ข้อ 6: แสดงรายละเอียดสมาชิกและคะแนนสะสมปัจจุบัน (textmode 144 ขวา / 145 ซ้าย)
   private void showMemberDetail(Member member)
   {
      while (true)
      {
         header(SHOP_TITLE, "ข้อมูลสมาชิก");
         member.verifyMemberInformation(findCustomerOfMember(member)); // Member.verifyMemberInformation()
         System.out.println(SINGLE_LINE);
         System.out.println("[ ตัวเลือกดำเนินการ ]");
         System.out.println("[1] คำนวณและบันทึกคะแนนสะสม");
         System.out.println("[2] ใช้คะแนนเป็นส่วนลด");
         System.out.println("[0] ย้อนกลับ");
         System.out.println(SINGLE_LINE);
         String choice = prompt("  กรุณาเลือกรายการที่ต้องการดำเนินการ:");
         if (choice.equals("1"))
            collectPoints(member);
         else if (choice.equals("2"))
            redeemPoints(member);
         else if (choice.equals("0"))
            return;
      }
   }

   // FR2 ข้อ 7: สะสมคะแนนจากยอดซื้อ (textmode 145 กลาง) อัตรา 100 บาท = 10 คะแนน
   private void collectPoints(Member member)
   {
      Order order = chooseUnpaidOrderForMember(member);
      if (order == null)
         return;
      PointRedemption existing = findRedemptionForOrder(order.getOrderId());
      if (existing != null && existing.getPointsEarned() > 0)
      {
         systemMessage("ออเดอร์ " + order.getOrderId() + " บันทึกคะแนนสะสมไปแล้ว");
         return;
      }
      double purchase = order.getOrderSubTotal();
      int points = ((int) (purchase / 100)) * 10; // ทุก 100 บาท ได้ 10 คะแนน

      header(SHOP_TITLE, "คำนวณและบันทึกคะแนนสะสม");
      System.out.println("   รหัสสมาชิก : " + member.getMemberId());
      System.out.println("   รหัสออเดอร์ : " + order.getOrderId());
      System.out.println("[ รายละเอียดการคำนวณคะแนน ]");
      System.out.println("  ยอดซื้อสินค้า    : " + formatNumber(purchase) + " บาท");
      System.out.println("  อัตราแลกคะแนน  : 100 บาท = 10 คะแนน");
      System.out.println("  คะแนนที่จะได้รับ : +" + points + " คะแนน");
      System.out.println(SINGLE_LINE);
      System.out.println("[ ตัวเลือกดำเนินการ ]");
      System.out.println("  [S] พิมพ์ S เพื่อบันทึกคะแนนสะสม");
      System.out.println("  [C] ยกเลิกรายการ");
      System.out.println(SINGLE_LINE);
      if (!prompt(" พิมพ์คำสั่งดำเนินการ:").equalsIgnoreCase("S"))
      {
         systemMessage("ยกเลิกรายการ");
         return;
      }
      order.getCustomer().setMember(member);       // ใช้สิทธิ์สมาชิกกับออเดอร์นี้ (ส่วนลด 5% ใน FR6)
      PointRedemption record = getOrCreateRedemption(order, member);
      member.collectMemberPoints(points);           // Member.collectMemberPoints()
      record.recordEarnedPoints(purchase, points);  // บันทึกประวัติการคำนวณแต้ม
      systemMessage("บันทึกคะแนนสำเร็จ!");
      systemMessage("คะแนนสะสมรวมปัจจุบัน: " + formatNumber(member.getAccumulatedPoints()) + " คะแนน");
      System.out.println(DOUBLE_LINE);
   }

   // FR2 ข้อ 8: ใช้คะแนนแลกส่วนลด (textmode 145 ขวา) เงื่อนไข 100 คะแนน = ส่วนลด 10 บาท
   private void redeemPoints(Member member)
   {
      Order order = chooseUnpaidOrderForMember(member);
      if (order == null)
         return;
      PointRedemption existing = findRedemptionForOrder(order.getOrderId());

      header(SHOP_TITLE, "ใช้คะแนนแลกส่วนลดและแสดงคะแนนคงเหลือ");
      System.out.println(" รหัสการแลกคะแนน: "
         + (existing != null ? existing.getRedemptionId() : peekId("RED", 3)));
      System.out.println(" รหัสสมาชิก : " + member.getMemberId());
      System.out.println(" เงื่อนไขการแลก   : 100 คะแนน = ส่วนลด 10 บาท");
      System.out.println(SINGLE_LINE);
      int points = parseIntOrDefault(prompt("กรุณาระบุจำนวนคะแนนที่ต้องการแลก:"), -1);
      double alreadyDiscount = (existing != null) ? existing.getDiscountReceived() : 0.0;
      double discount = (points / PointRedemption.POINTS_PER_REDEEM_UNIT)
         * PointRedemption.BAHT_PER_REDEEM_UNIT;
      if (points <= 0 || points % PointRedemption.POINTS_PER_REDEEM_UNIT != 0)
      {
         systemMessage("จำนวนคะแนนต้องเป็นทวีคูณของ 100");
         return;
      }
      if (points > member.getAccumulatedPoints())
      {
         systemMessage("คะแนนสะสมไม่เพียงพอ");
         return;
      }
      if (alreadyDiscount + discount > order.getOrderSubTotal())
      {
         systemMessage("ส่วนลดเกินยอดซื้อของออเดอร์");
         return;
      }
      System.out.println();
      System.out.println(" [ สรุปรายการส่วนลด ]");
      System.out.println("   ส่วนลดที่ได้        : -" + formatNumber(discount) + " บาท");
      System.out.println("   คะแนนคงเหลือ  :  "
         + formatNumber(member.getAccumulatedPoints() - points) + " คะแนน");
      System.out.println(SINGLE_LINE);
      System.out.println(" [ ตัวเลือกดำเนินการ ]");
      System.out.println("   [Y] พิมพ์ Y เพื่อบันทึกการใช้ส่วนลด");
      System.out.println("   [N] ยกเลิก");
      System.out.println(SINGLE_LINE);
      if (!prompt("พิมพ์คำสั่งดำเนินการ:").equalsIgnoreCase("Y"))
      {
         systemMessage("ยกเลิกการแลกคะแนน");
         return;
      }
      order.getCustomer().setMember(member);
      PointRedemption record = getOrCreateRedemption(order, member);
      record.redeemPointsForDiscount(points); // PointRedemption.redeemPointsForDiscount()
      systemMessage("ทำรายการสำเร็จ! บันทึกส่วนลด " + formatNumber(discount) + " บาทเรียบร้อย");
      System.out.println(DOUBLE_LINE);
   }

   // FR2 ข้อ 9: สมัครสมาชิกใหม่ (textmode 146)
   private void registerNewMember()
   {
      header(SHOP_TITLE, "สมัครสมาชิกใหม่");
      System.out.println("    รหัสลูกค้าใหม่ : " + peekId("CUST", 3));
      System.out.println("    รหัสสมาชิกใหม่ : " + peekId("MEM", 3));
      System.out.println(SINGLE_LINE);
      System.out.println("     กรุณากรอกข้อมูลสมาชิกใหม่ตามรายการด้านล่าง:");
      String firstName = prompt("  ชื่อ:");
      String lastName = prompt("  นามสกุล:");
      String phone = prompt("  เบอร์โทรศัพท์:");
      if (firstName.isEmpty() || lastName.isEmpty() || phone.replaceAll("[^0-9]", "").length() < 9)
      {
         systemMessage("ข้อมูลไม่ครบหรือเบอร์โทรศัพท์ไม่ถูกต้อง ยกเลิกการสมัคร");
         return;
      }
      if (searchMember(phone) != null)
      {
         systemMessage("เบอร์โทรศัพท์นี้เป็นสมาชิกอยู่แล้ว");
         return;
      }
      System.out.println(SINGLE_LINE);
      System.out.println("  [ ตัวเลือกดำเนินการ ]");
      System.out.println("    [S] พิมพ์ S เพื่อบันทึกข้อมูลสมาชิกใหม่");
      System.out.println("    [C] ยกเลิกการสมัคร");
      System.out.println(SINGLE_LINE);
      if (!prompt("  พิมพ์คำสั่งดำเนินการ:").equalsIgnoreCase("S"))
      {
         systemMessage("ยกเลิกการสมัคร");
         return;
      }
      Member member = new Member();
      member.registerNewMember(nextId("MEM", 3), firstName, lastName, phone); // Member.registerNewMember()
      Customer customer = new Customer(nextId("CUST", 3), member);
      members.add(member);
      customers.add(customer);
      systemMessage("สมัครสมาชิกสำเร็จ!");
      systemMessage("บันทึกข้อมูลสมาชิกใหม่ (" + member.getMemberId() + ") เข้าสู่ระบบเรียบร้อย");
      System.out.println(DOUBLE_LINE);
   }

   // เลือกออเดอร์ที่ยังไม่ชำระเงินเพื่อใช้สิทธิ์สมาชิก
   // (textmode 145 แสดง "รหัสออเดอร์ : ORD-001" จึงต้องระบุว่าใช้สิทธิ์กับออเดอร์ใด)
   private Order chooseUnpaidOrderForMember(Member member)
   {
      List<Order> unpaid = new ArrayList<Order>();
      for (Order order : orders)
         if (order.getPayment() == null)
            unpaid.add(order);
      if (unpaid.isEmpty())
      {
         systemMessage("ไม่มีออเดอร์ที่ยังไม่ชำระเงิน");
         return null;
      }
      System.out.println(" ออเดอร์ที่ยังไม่ชำระเงิน:");
      for (Order order : unpaid)
         System.out.println("   " + order.getOrderId() + " (โต๊ะ " + order.getTableId() + ") ยอดรวม "
            + formatMoney(order.getOrderSubTotal()) + " บาท");
      Order order = findOrder(prompt("กรุณาระบุรหัสออเดอร์ของลูกค้า:"));
      if (order == null || order.getPayment() != null)
      {
         systemMessage("ไม่พบออเดอร์ที่ยังไม่ชำระเงิน");
         return null;
      }
      Member linked = order.getCustomer().getMember();
      if (linked != null && linked != member)
      {
         systemMessage("ออเดอร์นี้ใช้สิทธิ์ของสมาชิก " + linked.getMemberId() + " แล้ว");
         return null;
      }
      return order;
   }

   private PointRedemption getOrCreateRedemption(Order order, Member member)
   {
      PointRedemption record = findRedemptionForOrder(order.getOrderId());
      if (record == null)
      {
         record = new PointRedemption(nextId("RED", 3), member);
         pointRedemptions.add(record);
         redemptionOrderIds.put(record, order.getOrderId());
      }
      return record;
   }

   // ===================================================================
   //  FR3 รับการแจ้งเตือนสต็อกวัตถุดิบ  (usecase_Scenario/124.jpg, textmode 147-148)
   //  Actor: ผู้จัดการร้าน
   // ===================================================================

   private void manageStockNotification()
   {
      // FR3 ข้อ 1: ตรวจสอบและตัดสต็อกวัตถุดิบอัตโนมัติตามรายการขายออเดอร์ล่าสุด (textmode 147 กลาง)
      header(SHOP_TITLE, "ตัดสต็อกอัตโนมัติ");
      Map<Ingredient, Double> used = new LinkedHashMap<Ingredient, Double>();
      System.out.println("[รายการขายออเดอร์ล่าสุด]:");
      boolean anySale = false;
      for (Order order : orders)
      {
         for (Basket line : order.getBasket())
         {
            if (deductedLines.contains(line))
               continue;
            anySale = true;
            System.out.println("   รหัสออเดอร์ " + order.getOrderId() + " (" + line.getMenuId() + " "
               + line.getMenuName() + " " + line.getMenuQuantity() + " " + unitOf(line.getMenuId()) + ")");
            Map<String, Double> recipe = recipes.get(line.getMenuId());
            if (recipe != null)
               for (Map.Entry<String, Double> entry : recipe.entrySet())
               {
                  Ingredient ingredient = findIngredient(entry.getKey());
                  double amount = entry.getValue() * line.getMenuQuantity();
                  used.put(ingredient, (used.containsKey(ingredient) ? used.get(ingredient) : 0.0) + amount);
               }
            deductedLines.add(line);
         }
      }
      if (!anySale)
         System.out.println("   (ไม่มีรายการขายใหม่)");
      System.out.println();
      System.out.println("[ระบบตัดสต็อกวัตถุดิบอัตโนมัติ]:");
      for (Map.Entry<Ingredient, Double> entry : used.entrySet())
      {
         entry.getKey().deductIngredientStock(entry.getValue()); // Ingredient.deductIngredientStock()
         System.out.println("   - [" + entry.getKey().getIngredientId() + "] "
            + entry.getKey().getIngredientName() + " : -" + formatQuantity(entry.getValue()) + " "
            + entry.getKey().getUnit());
      }
      System.out.println(SINGLE_LINE);
      systemMessage(used.isEmpty() ? "ไม่มีวัตถุดิบที่ต้องตัดสต็อก" : "ตัดสต็อกวัตถุดิบเรียบร้อยแล้ว");

      // FR3 ข้อ 2-3: เปรียบเทียบกับจุดแจ้งเตือน แล้วสร้างการแจ้งเตือนวัตถุดิบใกล้หมด
      for (Ingredient ingredient : ingredients)
         if (ingredient.isBelowReorderPoint() && findPendingNotification(ingredient) == null)
            stockNotifications.add(new StockNotification(nextId("NOTI", 3), ingredient));

      List<StockNotification> pending = new ArrayList<StockNotification>();
      for (StockNotification notification : stockNotifications)
         if (!notification.getIsConfirmed())
            pending.add(notification);
      if (pending.isEmpty())
      {
         systemMessage("ไม่มีวัตถุดิบที่ต่ำกว่าจุดแจ้งเตือน");
         prompt("กด Enter เพื่อกลับสู่เมนูจัดการร้าน...");
         return;
      }

      for (StockNotification notification : pending)
      {
         newPage();
         notification.notifyIngredientStock(); // StockNotification.notifyIngredientStock() (textmode 147 ขวา)
         Ingredient ingredient = notification.getIngredient();

         // FR3 ข้อ 5-6: หน้าตรวจสอบและยืนยันการแจ้งเตือน (textmode 148 ซ้าย)
         header(SHOP_TITLE, "ตรวจสอบและยืนยันการแจ้งเตือน");
         System.out.println("             [ โปรดตรวจสอบ ! ]");
         System.out.println("   รหัสการแจ้งเตือน : " + notification.getNotiId());
         System.out.println("   [!] [" + ingredient.getIngredientId() + "] " + ingredient.getIngredientName()
            + " จำนวนคงเหลือ " + formatQuantity(ingredient.getCurrentStock()) + " " + ingredient.getUnit());
         System.out.println(SINGLE_LINE);
         System.out.println(" [ ตัวเลือกดำเนินการ ]");
         System.out.println("   [Y] พิมพ์ Y เพื่อยืนยันการรับทราบการแจ้งเตือน");
         System.out.println("   [N] ข้าม");
         System.out.println(SINGLE_LINE);
         if (!prompt(" พิมพ์คำสั่งดำเนินการ:").equalsIgnoreCase("Y"))
         {
            systemMessage("ข้ามการแจ้งเตือน " + notification.getNotiId());
            continue;
         }
         notification.setIsConfirmed(true);
         systemMessage("ยืนยันการรับทราบเรียบร้อยแล้ว");

         // FR3 ข้อ 7-12: สั่งซื้อ -> รับเข้าสต็อก -> แสดงสต็อกใหม่
         // จำนวนสั่งซื้อเริ่มต้น = จุดแจ้งเตือน x 5 (น้ำปลา จุดแจ้งเตือน 2 ขวด -> สั่ง 10 ขวด ตาม textmode 148)
         int quantity = (int) Math.ceil(ingredient.getReorderPoint() * 5);
         if (!purchaseIngredient(ingredient, quantity))
            return; // [0] ย้อนกลับหน้าหลัก
      }
   }

   // หน้าสั่งซื้อวัตถุดิบ + ประวัติรับเข้าสต็อก (textmode 148 กลาง/ขวา)
   // คืน true = ทำรายการแจ้งเตือนต่อ, false = ผู้ใช้เลือกย้อนกลับหน้าหลัก
   private boolean purchaseIngredient(Ingredient ingredient, int quantity)
   {
      double unitCost = unitCosts.containsKey(ingredient.getIngredientId())
         ? unitCosts.get(ingredient.getIngredientId()) : 0.0;
      header(SHOP_TITLE, "สั่งซื้อวัตถุดิบ");
      System.out.println(" รหัสใบสั่งซื้อ : " + peekId("PO", 3));
      System.out.println(" รหัสผู้จัดจำหน่าย : " + supplier.getSupplierId() + " ("
         + supplier.getCompanyName() + ")");
      System.out.println();
      System.out.println(" [ รายการสั่งซื้อวัตถุดิบ ]");
      System.out.println("    รายการวัตถุดิบ : [" + ingredient.getIngredientId() + "] "
         + ingredient.getIngredientName());
      System.out.println("    จำนวนที่สั่งซื้อ : " + quantity + " " + ingredient.getUnit());
      System.out.println("    " + SINGLE_LINE);
      System.out.println("    ยอดคำสั่งซื้อรวม : " + formatNumber(quantity * unitCost) + " บาท");
      System.out.println("    " + SINGLE_LINE);
      System.out.println(" [ ตัวเลือกดำเนินการ ]");
      System.out.println("   [P] พิมพ์ P เพื่อยืนยันการสั่งซื้อวัตถุดิบ");
      System.out.println("   [C] ยกเลิก");
      System.out.println(SINGLE_LINE);
      if (!prompt("พิมพ์คำสั่งดำเนินการ:").equalsIgnoreCase("P"))
      {
         systemMessage("ยกเลิกการสั่งซื้อ");
         return true;
      }

      // FR3 ข้อ 8-10: ผู้จัดการยืนยัน -> ระบบบันทึกรายการสั่งซื้อวัตถุดิบ
      IngredientOrder po = new IngredientOrder();
      po.orderIngredients(nextId("PO", 3), supplier, ingredient, quantity, unitCost); // orderIngredients()
      ingredientOrders.add(po);
      systemMessage("บันทึกรายการสั่งซื้อวัตถุดิบสำเร็จ!");
      System.out.println(DOUBLE_LINE);

      // FR3 ข้อ 11: หน้าประวัติรับเข้าสต็อก แสดงประวัติเดิม + รายการรับเข้าจากใบสั่งซื้อนี้
      // (รายการล่าสุดยังไม่ถูกบันทึกจนกว่าผู้จัดการจะพิมพ์ [1] - textmode 148 ขวา)
      Date receivingDate = new Date();
      String receivingId = peekId("RCV", 3);
      header(SHOP_TITLE, "ประวัติรับเข้าสต็อก");
      System.out.println("             Stock Movement History");
      System.out.println();
      System.out.println(" วันที่ | รายการ | ประเภทเคลื่อนไหว |รหัสใบสั่งซื้อ| จำนวน | รหัสรับเข้า");
      System.out.println(SINGLE_LINE);
      for (StockReceiving record : stockReceivings)
         displayStockMovementLine(record.getStockReceivingDate(), record.getIngredientName(),
            record.getMovementType(), record.getReferenceOrderId(),
            record.getStockReceivingQuantity(), record.getStockReceivingId());
      displayStockMovementLine(receivingDate, po.getIngredientName(), "รับเข้า",
         po.getIngredientOrderId(), po.getIngredientOrderQuantity(), receivingId);
      System.out.println(SINGLE_LINE);
      System.out.println("[ ตัวเลือกดำเนินการ ]");
      System.out.println("  [1] เพิ่มรายการรับเข้าใหม่");
      System.out.println("  [0] ย้อนกลับหน้าหลัก");
      System.out.println(SINGLE_LINE);
      if (!prompt("พิมพ์คำสั่งดำเนินการ:").equals("1"))
      {
         systemMessage("ยังไม่บันทึกรับเข้าสต็อกของ " + po.getIngredientOrderId());
         return false;
      }

      // [1] เพิ่มรายการรับเข้าใหม่: ใช้ข้อมูลวัตถุดิบ/จำนวนจากใบสั่งซื้อในตาราง ไม่ต้องกรอกซ้ำ
      // FR3 ข้อ 11-12: ระบบบันทึกการรับเข้าสต็อก แล้วอัปเดตและแสดงจำนวนสต็อกใหม่
      StockReceiving receiving = new StockReceiving();
      receiving.recordStockReceiving(nextId("RCV", 3), po, ingredient); // recordStockReceiving()
      stockReceivings.add(receiving);
      systemMessage("สต็อกใหม่ของ \"" + ingredient.getIngredientName() + "\" อัปเดตเป็น "
         + formatQuantity(ingredient.getCurrentStock()) + " " + ingredient.getUnit() + "เรียบร้อยแล้ว");
      System.out.println(DOUBLE_LINE);
      return true; // FR3 ข้อ 13 จบการทำงาน (ถ้ามีแจ้งเตือนรายการอื่นค้างอยู่ จะแสดงต่อ)
   }

   // 1 บรรทัดของ Stock Movement History
   // รูปแบบ: "01/07/69 | [ING-005] น้ำปลา | รับเข้า | PO-001 |10 ขวด | RCV-001 (06.12น.)" (textmode 148)
   private void displayStockMovementLine(Date date, String ingredientName, String movementType,
      String orderId, int quantity, String receivingId)
   {
      Ingredient ingredient = null;
      for (Ingredient item : ingredients)
         if (item.getIngredientName().equals(ingredientName))
            ingredient = item;
      String idText = (ingredient == null) ? "" : "[" + ingredient.getIngredientId() + "] ";
      String unit = (ingredient == null) ? "" : " " + ingredient.getUnit();
      String dateTime = formatShortThaiDateTime(date);     // "01/07/69 (06.12น.)"
      int split = dateTime.indexOf(' ');
      System.out.println(" " + dateTime.substring(0, split) + " | " + idText + ingredientName + " | "
         + movementType + " | " + orderId + " |" + quantity + unit + " | " + receivingId);
      System.out.println(" " + dateTime.substring(split + 1));
   }

   // ===================================================================
   //  FR4 จัดการข้อมูลเมนูอาหาร  (usecase_Scenario/126.jpg, textmode 149, 151)
   //  Actor: ผู้จัดการร้าน
   // ===================================================================

   private void manageMenu()
   {
      while (true)
      {
         // FR4 ข้อ 2: ระบบแสดงหน้าสำหรับจัดการเมนูอาหาร (textmode 149)
         header("จัดการเมนูอาหาร", "เมนูสำหรับจัดการเมนูอาหาร");
         System.out.println("        [P] เพิ่มเมนูอาหาร");
         System.out.println("        [E] แก้ไข/ลบเมนูอาหาร");
         System.out.println("        [0] ย้อนกลับ"); // เพิ่มเพื่อให้กลับเมนูจัดการร้านได้
         System.out.println(SINGLE_LINE);
         String choice = prompt(" กรุณาเลือกเมนู :").toUpperCase();
         if (choice.equals("P"))
            addNewMenu();
         else if (choice.equals("E"))
         {
            if (editOrDeleteMenu())
               return; // บันทึกแล้วแสดงรายการเมนูทั้งหมด -> [0] กลับหน้าหลักจัดการร้าน
         }
         else if (choice.equals("0"))
            return;
      }
   }

   // FR4 ข้อ 3: เพิ่มและบันทึกเมนูใหม่ (textmode 149)
   private void addNewMenu()
   {
      String menuId = peekId("M", 2);
      header(SHOP_TITLE, "เพิ่มและบันทึกเมนูใหม่");
      System.out.println("รหัสเมนูใหม่: " + menuId);
      System.out.println("กรุณากรอกรายละเอียดเมนูใหม่:");
      String name = prompt("   ชื่ออาหาร :");
      String category = prompt("   ประเภท  :");
      double price = parseDoubleOrDefault(prompt("   ราคา (บาท):"), -1);
      String description = prompt("   รายละเอียด:");
      String imagePath = prompt("   รูปภาพ   : (พิมพ์ที่อยู่ไฟล์รูปภาพ)");
      if (name.isEmpty() || category.isEmpty() || price <= 0)
      {
         systemMessage("ข้อมูลไม่ครบหรือราคาไม่ถูกต้อง ยกเลิกการเพิ่มเมนู");
         return;
      }
      System.out.println(SINGLE_LINE);
      System.out.println(" [ ตัวเลือกดำเนินการ ]");
      System.out.println("   [S] พิมพ์ S เพื่อบันทึกเมนูใหม่");
      System.out.println("   [C] ยกเลิก");
      System.out.println(SINGLE_LINE);
      if (!prompt("พิมพ์คำสั่งดำเนินการ:").equalsIgnoreCase("S"))
      {
         systemMessage("ยกเลิกการเพิ่มเมนู");
         return;
      }
      Menu menu = new Menu();
      menu.addNewMenu(nextId("M", 2), name, category, price, description, imagePath); // Menu.addNewMenu()
      menus.add(menu);
      systemMessage("บันทึกข้อมูลเมนู \"" + name + "\"(" + menu.getMenuId() + ") เข้าสู่ระบบเรียบร้อย!");
      System.out.println(DOUBLE_LINE);
   }

   // FR4 ข้อ 4: หน้าแก้ไข / ลบ เมนู (textmode 151 ซ้าย)
   // คืน true เมื่อดำเนินการจนถึงหน้าแสดงรายการเมนูทั้งหมดแล้วเลือก [0]
   private boolean editOrDeleteMenu()
   {
      while (true)
      {
         header(SHOP_TITLE, "แก้ไข / ลบ เมนู");
         System.out.println("          [ รายการเมนูอาหารทั้งหมด ]");
         System.out.println();
         System.out.println("รหัสเมนู | ชื่ออาหาร | ราคา | ประเภท | สถานะ |   จัดการ");
         System.out.println(SINGLE_LINE);
         for (Menu menu : menus)
            System.out.println(" " + menu.getMenuId() + " |  " + menu.getMenuName() + "  | "
               + formatPrice(menu.getPrice()) + " | " + menu.getCategory() + " | "
               + menu.getMenuStatus() + " | [E]แก้ไข [D]ลบ");
         System.out.println(SINGLE_LINE);
         System.out.println(" [ ตัวเลือกดำเนินการ ]");
         System.out.println("  พิมพ์ [E] [รหัสเมนู] เพื่อแก้ไข");
         System.out.println("  พิมพ์ [D] [รหัสเมนู] เพื่อลบเมนู");
         System.out.println("  พิมพ์ [S]  เพื่อบันทึกความเปลี่ยนแปลง");
         System.out.println(SINGLE_LINE);
         String[] parts = prompt("พิมพ์คำสั่งดำเนินการ:").split("\\s+");
         String command = parts[0].toUpperCase();
         if (command.equals("S"))
            return displayAllMenus();
         Menu menu = (parts.length == 2) ? findMenu(parts[1]) : null;
         if ((command.equals("E") || command.equals("D")) && menu == null)
         {
            systemMessage("ไม่พบรหัสเมนู");
            continue;
         }
         if (command.equals("E"))
         {
            if (editMenu(menu))
               return displayAllMenus(); // FR4 ข้อ 4 สุดท้าย: แสดงรายการเมนูที่พร้อมใช้งาน
         }
         else if (command.equals("D"))
         {
            menus.remove(menu);
            systemMessage("ลบเมนู \"" + menu.getMenuName() + "\"(" + menu.getMenuId() + ") ออกจากระบบเรียบร้อย!");
         }
         else
            systemMessage("คำสั่งไม่ถูกต้อง");
      }
   }

   // หน้า [E] แก้ไข (textmode 151 ที่สอง) คืน true เมื่อบันทึกการแก้ไขสำเร็จ
   // ขั้นตอน: 1) เลือกหัวข้อที่จะแก้ทีละคำสั่ง (D/N/T/C) จนพิมพ์ END หรือ S
   //          2) กรอกค่าใหม่ของแต่ละหัวข้อตามลำดับที่เลือก
   //          3) แสดง [ สรุปการแก้ไข ] แล้วยืนยัน [S] บันทึก / [C] ยกเลิก
   private boolean editMenu(Menu menu)
   {
      header(SHOP_TITLE, "แก้ไข");
      System.out.println("  รหัสเมนูที่เลือก:   " + menu.getMenuId());
      System.out.println("  เมนูที่เลือก:       " + menu.getMenuName());
      System.out.println("  ประเภท:           " + menu.getCategory());
      System.out.println("  สถานะ:            " + menu.getMenuStatus());
      System.out.println("  ราคา:             " + formatNumber(menu.getPrice()) + " บาท");
      System.out.println(SINGLE_LINE);
      System.out.println(" [ ตัวเลือกดำเนินการ ]");
      System.out.println("[D] เพื่อแก้ไขราคา                [N] เพื่อแก้ไขชื่อเมนู");
      System.out.println("[T] เพื่อเปลี่ยนสถานะ ขาย/หมด      [C] เพื่อแก้ไขประเภท");
      System.out.println("[S] บันทึกความเปลี่ยนแปลง          [END] เพื่อจบการทำงาน");
      System.out.println(SINGLE_LINE);

      // 1) รับคำสั่งหัวข้อที่ต้องการแก้ไข (พิมพ์ทีละบรรทัด เช่น D, T แล้ว END)
      List<String> fields = new ArrayList<String>();
      System.out.println("พิมพ์คำสั่งดำเนินการ:");
      while (true)
      {
         System.out.print("  >");
         String command = readLine().toUpperCase();
         if (command.equals("END") || command.equals("S"))
            break;
         if (command.equals("D") || command.equals("N") || command.equals("T") || command.equals("C"))
         {
            if (!fields.contains(command))
               fields.add(command);
         }
         else
            systemMessage("คำสั่งไม่ถูกต้อง (D, N, T, C หรือ END)");
      }
      if (fields.isEmpty())
      {
         systemMessage("ไม่มีรายการที่ต้องแก้ไข");
         return false;
      }
      System.out.println(SINGLE_LINE);

      // 2) กรอกค่าใหม่ (ค่าเริ่มต้น = ค่าเดิม ถ้าไม่ได้เลือกแก้หัวข้อนั้น)
      String newName = menu.getMenuName();
      String newCategory = menu.getCategory();
      String newStatus = menu.getMenuStatus();
      double newPrice = menu.getPrice();
      for (String field : fields)
      {
         if (field.equals("D"))
         {
            newPrice = parseDoubleOrDefault(prompt("กรุณากรอกราคาใหม่:"), -1);
            if (newPrice <= 0)
            {
               systemMessage("ราคาไม่ถูกต้อง ยกเลิกการแก้ไข");
               return false;
            }
         }
         else if (field.equals("N"))
         {
            newName = prompt("กรุณากรอกชื่อเมนูใหม่:");
            if (newName.isEmpty())
            {
               systemMessage("ชื่อเมนูว่าง ยกเลิกการแก้ไข");
               return false;
            }
         }
         else if (field.equals("C"))
         {
            newCategory = prompt("กรุณากรอกประเภทใหม่:");
            if (newCategory.isEmpty())
            {
               systemMessage("ประเภทว่าง ยกเลิกการแก้ไข");
               return false;
            }
         }
         else if (field.equals("T"))
         {
            // S = ขาย (Sale), O = หมด (Out of stock) ตาม textmode 151
            String status = prompt("เปลี่ยนสถานะ ขาย[S]/หมด[O]:").toUpperCase();
            if (status.equals("S"))
               newStatus = Menu.STATUS_AVAILABLE;
            else if (status.equals("O"))
               newStatus = Menu.STATUS_SOLD_OUT;
            else
            {
               systemMessage("สถานะไม่ถูกต้อง ยกเลิกการแก้ไข");
               return false;
            }
         }
      }

      // 3) สรุปการแก้ไขและยืนยันบันทึก
      System.out.println();
      System.out.println(" [ สรุปการแก้ไข ]");
      System.out.println("   รหัสเมนู :   " + menu.getMenuId());
      System.out.println("   ชื่อเมนู :     " + newName);
      System.out.println("   ประเภท :    " + newCategory);
      System.out.println("   สถานะ:      " + newStatus);
      System.out.println("   ราคาใหม่ :   " + formatNumber(newPrice) + " บาท");
      System.out.println(SINGLE_LINE);
      System.out.println(" [ ตัวเลือกดำเนินการ ]");
      System.out.println("   [S] พิมพ์ S เพื่อบันทึกการแก้ไข");
      System.out.println("   [C] ยกเลิก");
      System.out.println(SINGLE_LINE);
      if (!prompt("พิมพ์คำสั่งดำเนินการ:").equalsIgnoreCase("S"))
      {
         systemMessage("ยกเลิกการแก้ไข");
         return false;
      }
      menu.updateMenuPrice(newPrice); // Menu.updateMenuPrice() ตาม Class Diagram
      menu.setMenuName(newName);
      menu.setCategory(newCategory);
      menu.setMenuStatus(newStatus);  // สถานะ "หมด" -> FR1 สั่งเมนูนี้ไม่ได้
      systemMessage("บันทึกการแก้ไขเมนู" + newName + "สำเร็จ!");
      System.out.println(DOUBLE_LINE);
      return true;
   }

   // แสดงรายการเมนูทั้งหมด (textmode 151 ที่สาม)
   // [F] [คำค้น] -> หน้าค้นหาและกรองประเภทเมนู, [0] กลับหน้าหลักจัดการร้าน
   private boolean displayAllMenus()
   {
      while (true)
      {
         header(SHOP_TITLE, "แสดงรายการเมนูทั้งหมด");
         System.out.println("[ผลลัพธ์รายการเมนู]");
         for (Menu menu : menus)
            displayMenuResultLine(menu);
         System.out.println(SINGLE_LINE);
         System.out.println("[ ตัวเลือกดำเนินการ ]");
         System.out.println("  [F] ค้นหาและกรองประเภทเมนู   (พิมพ์ F [ประเภท/ชื่ออาหาร] เช่น F อาหารคาว)");
         System.out.println("  [0] ย้อนกลับหน้าหลักจัดการร้าน");
         System.out.println(SINGLE_LINE);
         String command = prompt("พิมพ์คำสั่งดำเนินการ:");
         if (command.equals("0"))
            return true;
         String[] parts = command.split("\\s+", 2);
         if (parts[0].equalsIgnoreCase("F"))
         {
            if (filterMenus(parts.length == 2 ? parts[1].trim() : ""))
               return true;
         }
         else
            systemMessage("คำสั่งไม่ถูกต้อง");
      }
   }

   // หน้าค้นหาและกรองประเภทเมนู (textmode 151 ขวาสุด)
   // แสดงเมนูที่ประเภท ชื่อ หรือรหัส ตรงกับคำค้น เช่น "อาหารคาว" -> M-01..M-04
   // คืน true เมื่อพิมพ์ [0] ย้อนกลับหน้าหลักจัดการร้าน
   private boolean filterMenus(String keyword)
   {
      header(SHOP_TITLE, "ค้นหาและกรองประเภทเมนู");
      System.out.println("[ผลลัพธ์รายการเมนู]");
      boolean found = false;
      for (Menu menu : menus)
      {
         if (keyword.isEmpty() || menu.getCategory().contains(keyword)
            || menu.getMenuName().contains(keyword) || menu.getMenuId().equalsIgnoreCase(keyword))
         {
            displayMenuResultLine(menu);
            found = true;
         }
      }
      if (!found)
         System.out.println("  (ไม่พบเมนูที่ตรงกับ \"" + keyword + "\")");
      System.out.println(SINGLE_LINE);
      System.out.println("[ ตัวเลือกดำเนินการ ]");
      System.out.println("  [0] ย้อนกลับหน้าหลักจัดการร้าน");
      System.out.println(SINGLE_LINE);
      while (!prompt("พิมพ์คำสั่งดำเนินการ:").equals("0"))
         systemMessage("พิมพ์ 0 เพื่อย้อนกลับ");
      return true;
   }

   // บรรทัดผลลัพธ์ "[M-01] ส้มตำ   70.-  | อาหารคาว  | [สถานะ: ขาย]" (textmode 151)
   private void displayMenuResultLine(Menu menu)
   {
      System.out.println("  [" + menu.getMenuId() + "] " + menu.getMenuName() + "   "
         + formatPrice(menu.getPrice()) + "  | " + menu.getCategory() + "  | [สถานะ: "
         + menu.getMenuStatus() + "]");
   }

   // ===================================================================
   //  FR5 ดูและสรุปรายงานยอดขาย  (usecase_Scenario/128.jpg, textmode 151-154)
   //  Actor: ผู้จัดการร้าน
   // ===================================================================

   private void viewSalesReport()
   {
      // FR5 ข้อ 2: หน้าจอเลือกช่วงเวลาและประเภทรายงานยอดขาย (textmode 151 กลาง / 153 กลาง)
      header(SHOP_TITLE, "เลือกวันที่");
      System.out.println("[ ตัวกรองรายงานยอดขาย ]");
      Date start = parseThaiDate(prompt("   ระบุวันที่เริ่มต้น (วว/ดด/ปปปป พ.ศ. เช่น 01/06/2569) :"));
      Date end = parseThaiDate(prompt("   ระบุวันที่สิ้นสุด (วว/ดด/ปปปป พ.ศ.) :"));
      if (start == null || end == null || end.before(start))
      {
         systemMessage("รูปแบบวันที่ไม่ถูกต้อง กลับสู่เมนูจัดการร้าน");
         return;
      }
      System.out.println(SINGLE_LINE);
      System.out.println("[ ตัวเลือกดำเนินการ ]");
      System.out.println("  [1] แสดงรายงานยอดขายวัน");
      System.out.println("  [2] แสดงรายงานยอดขายรายเดือน");
      System.out.println("  [0] ย้อนกลับหน้าหลัก");
      System.out.println(SINGLE_LINE);
      String choice = prompt("กรุณาเลือกคำสั่งดำเนินการ:");
      if (!choice.equals("1") && !choice.equals("2"))
         return;
      boolean daily = choice.equals("1");

      // ใบเสร็จที่ชำระเงินแล้วในช่วงวันที่เลือก และออเดอร์ที่ตรงกัน
      List<Receipt> rangeReceipts = new ArrayList<Receipt>();
      List<Order> rangeOrders = new ArrayList<Order>();
      long from = startOfDay(start);
      long to = startOfDay(end) + 24L * 60 * 60 * 1000;
      for (Receipt receipt : receipts)
      {
         long time = receipt.getIssuedAt().getTime();
         if (time >= from && time < to)
         {
            rangeReceipts.add(receipt);
            rangeOrders.add(findOrder(receipt.getOrderId()));
         }
      }

      SalesReport report = new SalesReport(nextId(daily ? "REP-D" : "REP-M", 2), manager, start, end);
      report.summarizeSalesReport(rangeReceipts.toArray(new Receipt[0]),
         rangeOrders.toArray(new Order[0])); // SalesReport.summarizeSalesReport()
      salesReports.add(report);
      String reportId = report.getReportId(); // เช่น REP-D01 / REP-M01

      if (daily)
         showDailyReport(report, reportId, rangeReceipts);
      else
         showMonthlyReport(report, reportId);
   }

   // FR5 ข้อ 3: รายงานยอดขายรายวัน (textmode 151 ขวา) และอันดับเมนูขายดีรายวัน (textmode 152)
   private void showDailyReport(SalesReport report, String reportId, List<Receipt> rangeReceipts)
   {
      // ยอดขายและเมนูขายดีแยกตามช่วงเวลา (ชั่วโมง)
      Map<Integer, Double> salesByHour = new HashMap<Integer, Double>();
      Map<Integer, Map<String, Integer>> itemsByHour = new HashMap<Integer, Map<String, Integer>>();
      Map<String, String> names = new HashMap<String, String>();
      for (Receipt receipt : rangeReceipts)
      {
         Calendar c = new GregorianCalendar();
         c.setTime(receipt.getIssuedAt());
         int hour = c.get(Calendar.HOUR_OF_DAY);
         salesByHour.put(hour, (salesByHour.containsKey(hour) ? salesByHour.get(hour) : 0.0)
            + receipt.getGrandTotal());
         if (!itemsByHour.containsKey(hour))
            itemsByHour.put(hour, new HashMap<String, Integer>());
         for (Basket line : findOrder(receipt.getOrderId()).getBasket())
         {
            Map<String, Integer> items = itemsByHour.get(hour);
            items.put(line.getMenuId(), (items.containsKey(line.getMenuId())
               ? items.get(line.getMenuId()) : 0) + line.getMenuQuantity());
            names.put(line.getMenuId(), line.getMenuName());
         }
      }
      List<Integer> hours = new ArrayList<Integer>(salesByHour.keySet());
      final Map<Integer, Double> hourSales = salesByHour;
      Collections.sort(hours, new Comparator<Integer>()
      {
         public int compare(Integer a, Integer b) { return Double.compare(hourSales.get(b), hourSales.get(a)); }
      });

      header(SHOP_TITLE, "ข้อมูลยอดขายรายวัน");
      System.out.println("รหัสรายงาน :        " + reportId);
      System.out.println("รหัสผู้จัดการร้าน : " + report.getManager().getManagerId());
      System.out.println(" [ สรุปยอดขายรายวัน ]");
      int orderCount = report.getDailyTotalOrders();
      System.out.println("   - ยอดขายรวมประจำวัน : " + formatNumber(report.getDailyTotalSales()) + " บาท");
      System.out.println("   - จำนวนออเดอร์ทั้งหมด : " + orderCount + " ออเดอร์ (เฉลี่ย "
         + String.format("%,.1f", orderCount == 0 ? 0.0 : report.getDailyTotalSales() / orderCount)
         + " บาท/บิล)");
      System.out.println("   - ช่วงเวลาที่ขายดีที่สุด : " + report.getPeakSalesTime()
         + (hours.isEmpty() ? "" : " (ยอดขาย " + formatNumber(salesByHour.get(hours.get(0))) + " บาท)"));
      System.out.println("      " + SINGLE_LINE);
      System.out.println(" [ รายการสินค้าขายดีแยกตามช่วงเวลา ]");
      for (int i = 0; i < hours.size() && i < 2; i++)
      {
         int hour = hours.get(i);
         String label = (i == 0) ? "(ช่วงขายดีที่สุด)" : (hour < 15 ? "(มื้อกลางวัน)" : "(มื้อเย็น)");
         System.out.println("   * " + SalesReport.formatHourSlot(hour) + " " + label + " [ยอดรวม "
            + formatNumber(salesByHour.get(hour)) + " บาท]");
         List<Map.Entry<String, Integer>> top = sortByValue(itemsByHour.get(hour));
         for (int j = 0; j < top.size() && j < 3; j++)
            System.out.println("        [" + top.get(j).getKey() + "] " + names.get(top.get(j).getKey())
               + " (" + top.get(j).getValue() + " " + unitOf(top.get(j).getKey()) + "),");
      }
      System.out.println("      " + SINGLE_LINE);
      System.out.println(" [ ตัวเลือกดำเนินการ ]");
      System.out.println("   [N] ดูอันดับเมนูขายดี");
      System.out.println("   [0] ย้อนกลับ");
      System.out.println(SINGLE_LINE);
      if (!prompt("กรุณาเลือกคำสั่งดำเนินการ:").equalsIgnoreCase("N"))
         return;

      // FR5 ข้อ 3 (ต่อ): อันดับเมนูขายดีประจำวัน (textmode 152)
      header(SHOP_TITLE, "เมนูที่ขายดีที่สุดรายวัน");
      System.out.println(" รหัสรายงาน :  " + reportId);
      System.out.println("              [ อันดับเมนูขายดีประจำวัน ]");
      displayRanking(report);
      waitForZero("[ ตัวเลือกดำเนินการ ]\n  [0] ย้อนกลับหน้าหลัก");
   }

   // FR5 ข้อ 4-7: รายงานรายเดือน อันดับเมนู สรุปรายได้ และส่งออก (textmode 153-154)
   private void showMonthlyReport(SalesReport report, String reportId)
   {
      header(SHOP_TITLE, "ข้อมูลยอดขายรายเดือน");
      System.out.println(" รหัสรายงาน :       " + reportId);
      System.out.println(" รหัสผู้จัดการร้าน : " + report.getManager().getManagerId());
      System.out.println("[ สรุปภาพรวมประจำเดือน : " + formatThaiMonthYear(report.getStartDate()) + " ]");
      System.out.println("    - ยอดขายรวมทั้งเดือน : " + formatNumber(report.getMonthlyTotalSales()) + " บาท");
      System.out.println("    - วันที่ขายได้สูงสุด : " + report.getPeakSalesDay());
      System.out.println("    - ยอดขายเฉลี่ยต่อวัน : " + formatNumber(report.getDailyAverageSales()) + " บาท");
      System.out.println(SINGLE_LINE);
      System.out.println(" [ ตัวเลือกดำเนินการ ]");
      System.out.println("   [N] ดูอันดับเมนูขายดีประจำเดือน");
      System.out.println("   [0] ย้อนกลับ");
      System.out.println(SINGLE_LINE);
      if (!prompt(" กรุณาเลือกคำสั่งดำเนินการ:").equalsIgnoreCase("N"))
         return;

      header(SHOP_TITLE, "เมนูขายดีรายเดือน");
      System.out.println(" รหัสรายงาน : " + reportId);
      System.out.println("              [ อันดับเมนูขายดีประจำเดือน ]");
      displayRanking(report);
      System.out.println(" [ ตัวเลือกดำเนินการ ]");
      System.out.println("   [A] ดูสรุปรายได้รวมและส่งออกรายงาน");
      System.out.println("   [0] ย้อนกลับ");
      System.out.println(SINGLE_LINE);
      if (!prompt("กรุณาเลือกคำสั่งดำเนินการ:").equalsIgnoreCase("A"))
         return;

      // FR5 ข้อ 5: แสดงข้อมูลสรุปรายได้รวมและตัวเลือกการพิมพ์รายงาน (textmode 154 ขวา)
      while (true)
      {
         header(SHOP_TITLE, "แสดงรายได้รวม & พิมพ์รายงาน");
         System.out.println(" รหัสรายงาน : " + reportId);
         System.out.println(" [ สรุปรายได้รวมสุทธิ ]");
         System.out.println("   รายได้รวมทั้งหมด (เดือนนี้): " + formatNumber(report.getMonthlyTotalSales()) + " บาท");
         System.out.println("   (* คำนวณจากยอดสุทธิที่ชำระเงินเรียบร้อยแล้ว)");
         System.out.println("      " + SINGLE_LINE);
         System.out.println(" [ ตัวเลือกดำเนินการส่งออกรายงาน ]");
         System.out.println("   [P] พิมพ์รายงาน (Print Report)");
         System.out.println("   [E] ส่งออกไฟล์ (Export File)");
         System.out.println("   [0] ย้อนกลับหน้าหลัก");
         System.out.println("      " + SINGLE_LINE);
         String choice = prompt("กรุณาเลือกคำสั่งดำเนินการ:").toUpperCase();
         String content = buildReportText(report, reportId);
         if (choice.equals("P"))
         {
            System.out.println(content);
            systemMessage("กำลังพิมพ์รายงาน " + reportId + "... สำเร็จ!");
         }
         else if (choice.equals("E"))
         {
            // FR5 ข้อ 6-7: สร้างและส่งออกรายงานยอดขาย (ตกลงกับผู้ใช้ให้ส่งออกเป็นไฟล์ .txt)
            String fileName = "SalesReport_"
               + new SimpleDateFormat("MMMyyyy", Locale.ENGLISH).format(report.getStartDate())
               + "_" + reportId + ".txt";
            systemMessage("กำลังสร้างไฟล์รายงาน \"" + fileName + "\"...");
            try
            {
               report.exportSalesReport(fileName, content); // SalesReport.exportSalesReport()
               systemMessage("ส่งออกไฟล์สำเร็จ! บันทึกที่ reports/" + fileName + " เรียบร้อย");
            }
            catch (IOException e)
            {
               systemMessage("ส่งออกไฟล์ไม่สำเร็จ: " + e.getMessage());
            }
         }
         else if (choice.equals("0"))
            return;
      }
   }

   // ตารางอันดับเมนูขายดี 5 อันดับ + สรุปสัดส่วนเมนูยอดฮิต (textmode 152, 154 กลาง)
   private void displayRanking(SalesReport report)
   {
      Map<String, Integer> quantity = new HashMap<String, Integer>();
      final Map<String, Double> revenue = new HashMap<String, Double>();
      Map<String, String> names = new HashMap<String, String>();
      for (Order order : report.getOrders())
         for (Basket line : order.getBasket())
         {
            String id = line.getMenuId();
            quantity.put(id, (quantity.containsKey(id) ? quantity.get(id) : 0) + line.getMenuQuantity());
            revenue.put(id, (revenue.containsKey(id) ? revenue.get(id) : 0.0) + line.getItemPrice());
            names.put(id, line.getMenuName());
         }
      List<Map.Entry<String, Integer>> ranking = sortByValue(quantity);

      System.out.println(" อันดับ | รหัสเมนู |   ชื่อเมนู   | จำนวนที่ขายได้ | ยอดขายรวม");
      System.out.println(SINGLE_LINE);
      for (int i = 0; i < ranking.size() && i < 5; i++)
      {
         String id = ranking.get(i).getKey();
         System.out.println(String.format("   %d   |  %-5s  | %-10s |  %4d %-4s | %s บาท", i + 1, id,
            names.get(id), ranking.get(i).getValue(), unitOf(id), formatNumber(revenue.get(id))));
      }
      if (!ranking.isEmpty())
      {
         String topRevenue = null;
         for (String id : revenue.keySet())
            if (topRevenue == null || revenue.get(id) > revenue.get(topRevenue))
               topRevenue = id;
         System.out.println();
         System.out.println(" [ สรุปสัดส่วนเมนูยอดฮิต ]");
         System.out.println("   เมนูขายดีอันดับ 1 : [" + ranking.get(0).getKey() + "] "
            + names.get(ranking.get(0).getKey()));
         System.out.println("   เมนูสร้างรายได้สูงสุด: [" + topRevenue + "] " + names.get(topRevenue)
            + " (" + formatNumber(revenue.get(topRevenue)) + " บาท)");
      }
      else
         System.out.println("   (ไม่มีข้อมูลยอดขายในช่วงวันที่เลือก)");
      System.out.println(SINGLE_LINE);
   }

   // ข้อความรายงานสำหรับพิมพ์/ส่งออกไฟล์
   private String buildReportText(SalesReport report, String reportId)
   {
      StringBuilder text = new StringBuilder();
      text.append(DOUBLE_LINE).append("\n");
      text.append("   ").append(SHOP_TITLE).append("\n");
      text.append("   รายงานยอดขาย ").append(reportId).append("\n");
      text.append(DOUBLE_LINE).append("\n");
      text.append("ช่วงวันที่ : ").append(formatThaiDate(report.getStartDate())).append(" - ")
         .append(formatThaiDate(report.getEndDate())).append("\n");
      text.append("ผู้จัดการร้าน : ").append(report.getManager().getManagerId()).append("\n");
      text.append("ยอดขายรวมทั้งช่วง : ").append(formatNumber(report.getMonthlyTotalSales())).append(" บาท\n");
      text.append("วันที่ขายได้สูงสุด : ").append(report.getPeakSalesDay()).append("\n");
      text.append("ยอดขายเฉลี่ยต่อวัน : ").append(formatNumber(report.getDailyAverageSales())).append(" บาท\n");
      text.append("ช่วงเวลาขายดีที่สุด : ").append(report.getPeakSalesTime()).append("\n");
      text.append("จำนวนออเดอร์ : ").append(report.getOrders().length).append(" ออเดอร์\n");
      text.append("(* คำนวณจากยอดสุทธิที่ชำระเงินเรียบร้อยแล้ว)\n");
      text.append(DOUBLE_LINE).append("\n");
      return text.toString();
   }

   // ===================================================================
   //  FR6 ชำระเงินค่าอาหารและเครื่องดื่ม  (usecase_Scenario/131.jpg, textmode 155-157)
   //  Actor: พนักงานขาย
   // ===================================================================

   private void processPayment()
   {
      // FR6 ข้อ 2: แสดงรายการโต๊ะที่ต้องการปิดบิล (textmode 155 กลาง)
      header(SHOP_TITLE, "เลือกโต๊ะ & แสดงรายการที่สั่ง");
      System.out.println(" [ เลือกโต๊ะที่ต้องการชำระเงิน ]");
      for (int i = 0; i < tables.size(); i++)
      {
         Order order = findActiveOrderByTable(tables.get(i).getTableId());
         System.out.println("   [" + (i + 1) + "] โต๊ะ " + (i + 1) + " (" + tables.get(i).getTableId() + ")"
            + (order != null ? " (มีออเดอร์ค้างชำระ: " + order.getOrderId() + ")" : ""));
      }
      int index = parseIntOrDefault(prompt(" กรุณาเลือกโต๊ะ :"), 0);
      if (index < 1 || index > tables.size())
      {
         systemMessage("ไม่พบหมายเลขโต๊ะ");
         return;
      }
      Table table = tables.get(index - 1);
      Order order = findActiveOrderByTable(table.getTableId());
      if (order == null || order.getBasket().length == 0)
      {
         systemMessage("โต๊ะ " + table.getTableId() + " ไม่มีออเดอร์ค้างชำระ");
         return;
      }

      // FR6 ข้อ 4: แสดงรายการตะกร้าสั่งอาหารทั้งหมดและยอดรวมของโต๊ะ
      Customer customer = order.getCustomer();
      System.out.println("      " + SINGLE_LINE);
      System.out.println("รหัสโต๊ะ :        " + table.getTableId());
      System.out.println("รหัสออเดอร์ : " + order.getOrderId());
      if (customer.isMember())
      {
         System.out.println("ลูกค้า :         " + customer.getCustomerId() + " (" + customer.getMemberId() + ")");
         System.out.println("                " + customer.getMember().getFullName());
      }
      else
         System.out.println("ลูกค้า :         " + customer.getCustomerId() + " (ไม่ใช่สมาชิก)");
      System.out.println("                 [ รายการอาหารที่สั่ง ]");
      System.out.println("   รหัสเมนู | ชื่ออาหาร | จำนวน | รวม (บาท)");
      System.out.println("      " + SINGLE_LINE);
      for (Basket line : order.getBasket())
         System.out.println(" [" + line.getMenuId() + "]   " + line.getMenuName() + "    "
            + line.getMenuQuantity() + "     " + formatPrice(line.getItemPrice()));
      System.out.println("      " + SINGLE_LINE);
      System.out.println(" [ ตัวเลือกดำเนินการ ]");
      System.out.println("   [1] ยืนยันรายการและไปหน้าคิดเงิน");
      System.out.println("   [0] ย้อนกลับหน้าหลัก");
      System.out.println("      " + SINGLE_LINE);
      if (!prompt("กรุณาพิมพ์คำสั่งดำเนินการ:").equals("1"))
         return;

      // FR6 ข้อ 5-6: คำนวณส่วนลดสมาชิกและภาษี (textmode 155 ขวา / 156 ซ้าย)
      Payment payment = new Payment(peekId("PAY", 3), order.getOrderId(), salesEmployee,
         findRedemptionForOrder(order.getOrderId()), pendingStatus);
      payment.summarizePaymentAmount(order); // Payment.summarizePaymentAmount()
      header(SHOP_TITLE, "คำนวณส่วนลดสมาชิก (5%) & VAT");
      System.out.println(" รหัสออเดอร์ : " + order.getOrderId());
      if (customer.isMember())
         System.out.println(" รหัสสมาชิก : " + customer.getMemberId() + " (คุณ"
            + customer.getMember().getFullName() + ")");
      System.out.println("        [ รายละเอียดคำนวณยอดชำระ ]");
      System.out.println(String.format("  ยอดรวม:                    %,10.2f บาท", order.getOrderSubTotal()));
      System.out.println("  สถานะสมาชิก:          " + (customer.isMember()
         ? "ใช่สมาชิก [" + customer.getMemberId() + "]" : "ไม่ใช่สมาชิก"));
      if (customer.isMember())
      {
         System.out.println("  คะแนนสะสม:                 "
            + formatNumber(customer.getMember().getAccumulatedPoints()) + " คะแนน");
         System.out.println(String.format("  ส่วนลดสมาชิก (5%%):        - %,.2f บาท", order.getOrderDiscount()));
      }
      PointRedemption redemption = payment.getPointRedemption();
      if (redemption != null && redemption.getDiscountReceived() > 0)
         System.out.println(String.format("  ส่วนลดแลกคะแนน (%s): - %,.2f บาท",
            redemption.getRedemptionId(), redemption.getDiscountReceived()));
      System.out.println(SINGLE_LINE);
      System.out.println(String.format("  ยอดหลังหักส่วนลด:          %,10.2f บาท", order.getOrderNetAfterDiscount()));
      System.out.println(String.format("  VAT 7%%:                    %,10.2f บาท", order.getOrderVatAmount()));
      System.out.println(SINGLE_LINE);
      System.out.println(String.format("  ยอดสุทธิ:                  %,10.2f บาท", order.getOrderGrandTotal()));
      System.out.println(SINGLE_LINE);
      System.out.println(" [ ตัวเลือกดำเนินการ ]");
      System.out.println("   [N] เลือกวิธีชำระเงิน");
      System.out.println("   [0] ย้อนกลับ");
      System.out.println(SINGLE_LINE);
      // FR6 ข้อ 7: พนักงานแจ้งยอดชำระสุทธิให้ลูกค้าทราบ
      if (!prompt("กรุณาเลือกคำสั่งดำเนินการ:").equalsIgnoreCase("N"))
         return;

      // FR6 ข้อ 8-10: หน้าเลือกวิธีชำระเงิน (textmode 156 กลาง)
      header(SHOP_TITLE, "เลือกวิธีการชำระเงิน");
      System.out.println("  รหัสออเดอร์ : " + order.getOrderId());
      System.out.println("  ยอดสุทธิที่ต้องชำระ : " + formatMoney(payment.getPayableAmount()) + " บาท");
      System.out.println(SINGLE_LINE);
      System.out.println(" [ ตัวเลือกช่องทางการชำระเงิน ]");
      System.out.println("   [1] " + cashMethod.getPaymentMethodName());
      System.out.println("   [2] " + qrMethod.getPaymentMethodName());
      System.out.println(SINGLE_LINE);
      String method = prompt("  กรุณาเลือกช่องทางการชำระเงิน:");

      if (method.equals("1"))
      {
         // FR6 ข้อ 11: ชำระด้วยเงินสด -> หน้าจอคำนวณเงินทอน (textmode 156 ขวา)
         header(SHOP_TITLE, "คำนวณเงินทอน (เงินสด)");
         System.out.println("  รหัสการชำระเงิน : " + payment.getPaymentId());
         System.out.println("  อ้างอิงออเดอร์ : " + order.getOrderId());
         System.out.println("  พนักงานรับเงิน : " + salesEmployee.getEmployeeId());
         System.out.println(SINGLE_LINE);
         System.out.println("        ยอดที่ต้องชำระ: " + formatMoney(payment.getPayableAmount()) + " บาท");
         System.out.println(SINGLE_LINE);
         double received;
         while (true)
         {
            received = parseDoubleOrDefault(prompt(" กรุณาระบุจำนวนเงินที่รับมา:"), -1);
            if (received >= payment.getPayableAmount())
               break;
            systemMessage("จำนวนเงินที่รับมาไม่พอชำระ กรุณาระบุใหม่");
         }
         System.out.println(SINGLE_LINE);
         System.out.println("        [ ผลการคำนวณเงินทอน ]");
         System.out.println("   รับเงินมา:              " + formatNumber(received) + " บาท");
         System.out.println("   เงินทอน:                " + formatMoney(received - payment.getPayableAmount()) + " บาท");
         System.out.println(SINGLE_LINE);
         System.out.println(" [ ตัวเลือกดำเนินการ ]");
         System.out.println("[ENTER] ยืนยันการรับเงินและออกใบเสร็จ");
         System.out.println(" [C] ยกเลิกรายการ");
         System.out.println(SINGLE_LINE);
         String confirm = prompt("กรุณาเลือกคำสั่งดำเนินการ:");
         if (!confirm.isEmpty() && !confirm.equalsIgnoreCase("ENTER"))
         {
            systemMessage("ยกเลิกรายการชำระเงิน");
            return;
         }
         payment.processPayment(received, cashMethod, paidStatus); // Payment.processPayment()
      }
      else if (method.equals("2"))
      {
         // FR6 ข้อ 12: ชำระด้วย QR Code ให้ลูกค้าสแกน แล้วพนักงานตรวจสอบการชำระเงินในระบบ
         header(SHOP_TITLE, "ชำระเงินด้วย QR PromptPay");
         System.out.println("  รหัสการชำระเงิน : " + payment.getPaymentId());
         System.out.println("  อ้างอิงออเดอร์ : " + order.getOrderId());
         System.out.println("  ยอดที่ต้องชำระ : " + formatMoney(payment.getPayableAmount()) + " บาท");
         System.out.println(SINGLE_LINE);
         systemMessage("แสดง QR PromptPay ให้ลูกค้าสแกนชำระเงิน");
         System.out.println(" [ ตัวเลือกดำเนินการ ]");
         System.out.println("   [Y] ตรวจสอบแล้ว ยอดเงินเข้าถูกต้อง");
         System.out.println("   [C] ยกเลิกรายการ");
         System.out.println(SINGLE_LINE);
         if (!prompt("กรุณาเลือกคำสั่งดำเนินการ:").equalsIgnoreCase("Y"))
         {
            systemMessage("ยกเลิกรายการชำระเงิน");
            return;
         }
         payment.processPayment(payment.getPayableAmount(), qrMethod, paidStatus);
      }
      else
      {
         systemMessage("ไม่พบช่องทางการชำระเงิน");
         return;
      }

      // FR6 ข้อ 13: ระบบบันทึกข้อมูลการชำระเงิน และแสดงใบเสร็จรับเงิน (textmode 157 ซ้าย)
      nextId("PAY", 3);
      payments.add(payment);
      order.setPayment(payment);
      Receipt receipt = new Receipt(nextId("REC", 3), order, payment, new Date());
      receipts.add(receipt);
      newPage();
      receipt.printReceipt(order); // Receipt.printReceipt()
      System.out.println("[ ตัวเลือกดำเนินการ ]");
      System.out.println("  [P] พิมพ์ใบเสร็จ    [0] ข้าม");
      // FR6 ข้อ 14-16: พนักงานสั่งพิมพ์ ระบบพิมพ์ใบเสร็จ แล้วส่งมอบให้ลูกค้า
      if (prompt("กรุณาเลือกคำสั่งดำเนินการ:").equalsIgnoreCase("P"))
         systemMessage("กำลังพิมพ์ใบเสร็จรับเงิน " + receipt.getReceiptId() + "... สำเร็จ!");

      // FR6 ข้อ 17: อัปเดตสถานะการชำระเงินเป็นชำระแล้ว และเปลี่ยนสถานะโต๊ะเป็นว่าง (textmode 157 ขวา)
      table.setTableStatus(Table.STATUS_AVAILABLE);
      header(SHOP_TITLE, "อัปเดตสถานะโต๊ะ");
      System.out.println("  [ ✓ ] " + payment.getPaymentStatus().getPaymentStatusName());
      System.out.println("  รหัสการชำระเงิน : " + payment.getPaymentId());
      System.out.println("  รหัสออเดอร์       : " + order.getOrderId());
      System.out.println("  รหัสโต๊ะ           : " + table.getTableId());
      System.out.println("  สถานะโต๊ะ " + table.getTableId() + "   : " + table.getTableStatus());
      System.out.println();
      waitForZero("[ ตัวเลือกดำเนินการ ]\n  [0] กลับสู่หน้าหลัก");
   }

   // ===================================================================
   //  FR8 จัดการออเดอร์อาหารในครัว  (usecase_Scenario/135.jpg, textmode 161-162)
   //  Actor: พนักงานครัว
   // ===================================================================

   private void manageKitchenOrders()
   {
      // FR8 ข้อ 2: แสดงรายการออเดอร์ที่รอยืนยัน (textmode 161 กลาง)
      List<Order> kitchenOrders = new ArrayList<Order>();
      for (Order order : orders)
         for (Basket line : order.getBasket())
            if (!Basket.STATUS_DONE.equals(line.getCookingStatus()) && !kitchenOrders.contains(order))
               kitchenOrders.add(order);

      header(SHOP_TITLE, "รายการออเดอร์ที่รอยืนยัน");
      System.out.println("รหัสออเดอร์ | รหัสโต๊ะ |  เวลา    | สถานะ");
      System.out.println(SINGLE_LINE);
      for (Order order : kitchenOrders)
         System.out.println(order.getOrderId() + "     |   " + order.getTableId() + "   | "
            + formatTime(basketSentTimes.get(order.getBasketIds().get(0))) + " น. | "
            + basketGroupStatus(order.getBasket()));
      if (kitchenOrders.isEmpty())
         System.out.println("   (ไม่มีออเดอร์ที่ต้องดำเนินการ)");
      System.out.println(SINGLE_LINE);

      // FR8 ข้อ 3: พนักงานครัวเลือกรหัสออเดอร์ที่ต้องการจัดการ
      Order order = findOrder(prompt("  กรุณาเลือกรหัสออเดอร์ที่ต้องการจัดการ:"));
      if (order == null || !kitchenOrders.contains(order))
      {
         systemMessage("ไม่พบออเดอร์ที่รอดำเนินการ กลับสู่หน้าหลัก");
         return;
      }

      // FR8 ข้อ 4-6: แสดงรายละเอียดออเดอร์ และยืนยันรายการอาหาร (textmode 161 ขวา)
      header(SHOP_TITLE, "รายละเอียดออเดอร์");
      displayKitchenOrderHeader(order);
      System.out.println();
      System.out.println("[รายการอาหาร]");
      displayKitchenItems(order);
      while (hasStatus(order, Basket.STATUS_WAITING))
      {
         System.out.println("[ รูปแบบการยืนยัน: ]");
         System.out.println("  พิมพ์ [A ] [รหัสเมนู] หรือ [ALL] เพื่อยืนยันทั้งหมด  (พิมพ์ N เพื่อข้าม)");
         System.out.println(SINGLE_LINE);
         String command = prompt("กรุณายืนยันรายการอาหาร:");
         String[] parts = command.split("\\s+");
         if (command.equalsIgnoreCase("N"))
            break;
         if (command.equalsIgnoreCase("ALL"))
            order.confirmKitchenOrder(); // Order.confirmKitchenOrder() ยืนยันทั้งหมด
         else if (parts.length == 2 && parts[0].equalsIgnoreCase("A")
            && updateItemStatus(order, parts[1], Basket.STATUS_WAITING, Basket.STATUS_CONFIRMED))
         {
            // ยืนยันทีละรายการผ่าน Basket.updateCookingStatus()
         }
         else
         {
            systemMessage("ไม่พบรายการที่รอยืนยัน หรือรูปแบบไม่ถูกต้อง");
            continue;
         }
         System.out.println("[ระบบ] บันทึกการยืนยันออเดอร์ " + order.getOrderId() + " เข้าสู่ระบบเรียบร้อย!");
      }
      System.out.println(SINGLE_LINE);
      System.out.println("[ ตัวเลือกดำเนินการ ]");
      System.out.println("     [N] ดูหน้าสถานะรายการอาหาร");
      System.out.println("     [0] กลับสู่หน้าหลัก");
      System.out.println(SINGLE_LINE);
      if (!prompt("กรุณาเลือกคำสั่งดำเนินการ:").equalsIgnoreCase("N"))
         return;

      // FR8 ข้อ 7-10: หน้าสถานะรายการอาหาร -> ระบุเมนูที่เริ่มทำ เป็น "กำลังทำ" (textmode 162 ซ้าย)
      header(SHOP_TITLE, "สถานะรายการอาหาร");
      displayKitchenOrderHeader(order);
      System.out.println("            [ สถานะรายการอาหาร ]");
      displayKitchenItems(order);
      String startMenu = prompt("กรุณาระบุรหัสเมนูที่เริ่มทำ:");
      System.out.println("[ ตัวเลือกสถานะ: [1] กำลังทำ, [0] ย้อนกลับ ]");
      System.out.println(SINGLE_LINE);
      if (prompt("กรุณาเลือกสถานะ:").equals("1"))
      {
         if (updateItemStatus(order, startMenu, Basket.STATUS_CONFIRMED, Basket.STATUS_COOKING))
            systemMessage("\n   อัปเดตสถานะ [" + startMenu.toUpperCase() + "] " + menuNameOf(order, startMenu)
               + " เป็น \"" + Basket.STATUS_COOKING + "\" สำเร็จ!");
         else
            systemMessage("ไม่พบรายการที่ \"ยืนยันแล้ว\" ของเมนู " + startMenu);
      }
      System.out.println(DOUBLE_LINE);

      // FR8 ข้อ 11-12: เมื่อทำอาหารเสร็จ ระบุเมนูที่ทำเสร็จ เป็น "ทำเสร็จแล้ว" (textmode 162 กลาง)
      header(SHOP_TITLE, "อัปเดตสถานะรายการอาหาร");
      displayKitchenOrderHeader(order);
      System.out.println("         [ สถานะรายการอาหารปัจจุบัน ]");
      displayKitchenItems(order);
      System.out.println("[ เมื่อทำรายการอาหารเสร็จสิ้น ]");
      String doneMenu = prompt("  กรุณาระบุรหัสเมนูที่ทำเสร็จ:");
      System.out.println("[ ตัวเลือกสถานะ: [2] ทำเสร็จ, [S] ข้าม ]");
      System.out.println(SINGLE_LINE);
      if (prompt("  กรุณาเลือกสถานะ:").equals("2"))
      {
         if (updateItemStatus(order, doneMenu, Basket.STATUS_COOKING, Basket.STATUS_DONE))
            systemMessage("\nอัปเดตสถานะ \"[" + doneMenu.toUpperCase() + "] " + menuNameOf(order, doneMenu)
               + "\" เป็น \"ทำเสร็จ\" สำเร็จ!");
         else
            systemMessage("ไม่พบรายการที่ \"กำลังทำ\" ของเมนู " + doneMenu);
      }
      System.out.println(DOUBLE_LINE);

      // FR8 ข้อ 13: ระบบบันทึกสถานะรายการอาหารในตะกร้าเข้าสู่ระบบ (textmode 162 ขวา)
      header(SHOP_TITLE, "อัปเดตสถานะออเดอร์");
      displayKitchenOrderHeader(order);
      System.out.println("            [รายการอาหาร ]");
      displayKitchenItems(order);
      waitForZero("[ ตัวเลือกดำเนินการ ]\n   [0] กลับสู่หน้าหลัก");
   }

   private void displayKitchenOrderHeader(Order order)
   {
      System.out.println("รหัสออเดอร์ : " + order.getOrderId() + "   รหัสพนักงานครัว : "
         + kitchenEmployee.getKitchenEmployeeId());
      System.out.println("รหัสโต๊ะ :       " + order.getTableId() + "          รหัสสมาชิก :      "
         + (order.getCustomer().isMember() ? order.getCustomer().getMemberId() : "-"));
   }

   private void displayKitchenItems(Order order)
   {
      System.out.println(SINGLE_LINE);
      System.out.println(" รหัสเมนู | ชื่ออาหาร | จำนวน |    สถานะ");
      System.out.println(SINGLE_LINE);
      for (Basket line : order.getBasket())
         System.out.println("  " + line.getMenuId() + " |  " + line.getMenuName() + "  |   "
            + line.getMenuQuantity() + "   |  " + line.getCookingStatus());
      System.out.println(SINGLE_LINE);
   }

   // เปลี่ยนสถานะรายการของเมนูที่ระบุ จากสถานะ from เป็น to ผ่าน Basket.updateCookingStatus()
   private boolean updateItemStatus(Order order, String menuId, String from, String to)
   {
      boolean updated = false;
      for (Basket line : order.getBasket())
         if (line.getMenuId().equalsIgnoreCase(menuId) && from.equals(line.getCookingStatus()))
         {
            line.updateCookingStatus(to);
            updated = true;
         }
      return updated;
   }

   private boolean hasStatus(Order order, String status)
   {
      for (Basket line : order.getBasket())
         if (status.equals(line.getCookingStatus()))
            return true;
      return false;
   }

   private String menuNameOf(Order order, String menuId)
   {
      for (Basket line : order.getBasket())
         if (line.getMenuId().equalsIgnoreCase(menuId))
            return line.getMenuName();
      return "";
   }

   // สถานะรวมของกลุ่มรายการ (ใช้แสดงในรายการตะกร้า FR7 และรายการออเดอร์ FR8)
   private String basketGroupStatus(Basket[] lines)
   {
      boolean allDone = lines.length > 0;
      for (Basket line : lines)
      {
         if (Basket.STATUS_WAITING.equals(line.getCookingStatus()))
            return Basket.STATUS_WAITING;
         if (!Basket.STATUS_DONE.equals(line.getCookingStatus()))
            allDone = false;
      }
      return allDone ? Basket.STATUS_DONE : "กำลังปรุง";
   }

   // ===================================================================
   //  การค้นหาข้อมูล (เทียบ BankDatabase.getAccount ใน ATM)
   // ===================================================================

   private Menu findMenu(String menuId)
   {
      for (Menu menu : menus)
         if (menu.getMenuId().equalsIgnoreCase(menuId))
            return menu;
      return null;
   }

   private Ingredient findIngredient(String ingredientId)
   {
      for (Ingredient ingredient : ingredients)
         if (ingredient.getIngredientId().equalsIgnoreCase(ingredientId))
            return ingredient;
      return null;
   }

   private Order findOrder(String orderId)
   {
      for (Order order : orders)
         if (order.getOrderId().equalsIgnoreCase(orderId))
            return order;
      return null;
   }

   // ออเดอร์ที่ยังไม่ชำระเงินของโต๊ะ (payment == null)
   private Order findActiveOrderByTable(String tableId)
   {
      for (Order order : orders)
         if (order.getTableId().equals(tableId) && order.getPayment() == null)
            return order;
      return null;
   }

   // ค้นหาสมาชิกด้วยเบอร์โทรศัพท์ (ไม่สนขีด) หรือชื่อ / ชื่อ-นามสกุล (FR2 ข้อ 5)
   private Member searchMember(String keyword)
   {
      String digits = keyword.replaceAll("[^0-9]", "");
      for (Member member : members)
      {
         if (digits.length() >= 9 && member.getPhone().replaceAll("[^0-9]", "").equals(digits))
            return member;
         if (!keyword.isEmpty() && (member.getFirstName().equals(keyword)
            || member.getFullName().equals(keyword)))
            return member;
      }
      return null;
   }

   private Customer findCustomerOfMember(Member member)
   {
      for (Customer customer : customers)
         if (customer.getMember() == member)
            return customer;
      return null;
   }

   private PointRedemption findRedemptionForOrder(String orderId)
   {
      for (PointRedemption redemption : pointRedemptions)
         if (redemptionOrderIds.get(redemption).equals(orderId))
            return redemption;
      return null;
   }

   private StockNotification findPendingNotification(Ingredient ingredient)
   {
      for (StockNotification notification : stockNotifications)
         if (notification.getIngredient() == ingredient && !notification.getIsConfirmed())
            return notification;
      return null;
   }

   private String findMenuIdByName(Basket[] lines, String menuName)
   {
      for (Basket line : lines)
         if (line.getMenuName().equals(menuName))
            return line.getMenuId();
      return "";
   }

   // หน่วยนับในรายงาน: เครื่องดื่ม = ขวด, อาหาร = จาน (textmode 151-152)
   private String unitOf(String menuId)
   {
      Menu menu = findMenu(menuId);
      return (menu != null && menu.getCategory().contains("เครื่องดื่ม")) ? "ขวด" : "จาน";
   }

   // ===================================================================
   //  สร้างรหัส เช่น ORD-001, BSK-001, SLIP-001, M-06
   // ===================================================================

   // ดูรหัสถัดไปโดยยังไม่จอง (แสดงบนหน้าจอก่อนผู้ใช้ยืนยัน)
   private String peekId(String prefix, int digits)
   {
      int next = (idCounters.containsKey(prefix) ? idCounters.get(prefix) : 0) + 1;
      String separator = prefix.endsWith("-D") || prefix.endsWith("-M") ? "" : "-";
      return String.format("%s%s%0" + digits + "d", prefix, separator, next);
   }

   // จองรหัสถัดไปจริง (เรียกตอนบันทึกข้อมูล)
   private String nextId(String prefix, int digits)
   {
      String id = peekId(prefix, digits);
      idCounters.put(prefix, (idCounters.containsKey(prefix) ? idCounters.get(prefix) : 0) + 1);
      return id;
   }

   // ===================================================================
   //  ข้อมูลตั้งต้น (เทียบ BankDatabase() ใน ATM ที่สร้างบัญชีทดสอบ)
   //  ค่าตัวอย่างนำมาจาก textmode_original และ attributeAll_meaning
   // ===================================================================

   private void seedData()
   {
      // โต๊ะ TB-1..TB-4 (textmode 141)
      for (int i = 1; i <= 4; i++)
         tables.add(new Table("TB-" + i));

      // วัตถุดิบ ING-001..ING-005 (textmode 147) และราคาต่อหน่วยจากผู้จัดจำหน่าย
      Ingredient duck = new Ingredient("ING-001", "เนื้อเป็ด", 5.0, "กก.", 1.0);
      Ingredient chili = new Ingredient("ING-002", "พริกผง", 2.0, "กก.", 0.2);
      Ingredient lime = new Ingredient("ING-003", "มะนาว", 50.0, "ลูก", 10.0);
      Ingredient rice = new Ingredient("ING-004", "ข้าวคั่ว", 2.0, "กก.", 0.2);
      Ingredient fishSauce = new Ingredient("ING-005", "น้ำปลา", 2.0, "ขวด", 2.0);
      ingredients.add(duck);
      ingredients.add(chili);
      ingredients.add(lime);
      ingredients.add(rice);
      ingredients.add(fishSauce);
      unitCosts.put("ING-001", 450.0);
      unitCosts.put("ING-002", 200.0);
      unitCosts.put("ING-003", 3.0);
      unitCosts.put("ING-004", 150.0);
      unitCosts.put("ING-005", 32.0);

      // เมนู M-01..M-05 (textmode 141, 150)
      menus.add(new Menu("M-01", "ส้มตำ", "อาหารคาว", 70.0, "ส้มตำรสจัดจ้าน",
         "images/somtum.png", Menu.STATUS_AVAILABLE, new Ingredient[] { chili, lime, fishSauce }));
      menus.add(new Menu("M-02", "ลาบเป็ด", "อาหารคาว", 90.0,
         "เป็ดสับคลุกเคล้าข้าวคั่วและพริกป่นรสจัดจ้าน", "images/laab.png",
         Menu.STATUS_AVAILABLE, new Ingredient[] { duck, chili, lime, rice, fishSauce }));
      menus.add(new Menu("M-03", "ไก่ย่าง", "อาหารคาว", 120.0, "ไก่ย่างสูตรอีสาน",
         "images/kaiyang.png", Menu.STATUS_SOLD_OUT, new Ingredient[0]));
      menus.add(new Menu("M-04", "ข้าวเหนียว", "อาหารคาว", 20.0, "ข้าวเหนียวนึ่ง",
         "images/sticky_rice.png", Menu.STATUS_AVAILABLE, new Ingredient[0]));
      menus.add(new Menu("M-05", "น้ำเปล่า", "เครื่องดื่ม", 10.0, "น้ำดื่มขวด",
         "images/water.png", Menu.STATUS_AVAILABLE, new Ingredient[0]));
      idCounters.put("M", 5); // เมนูใหม่ถัดไป = M-06 (textmode 149)

      // สูตรอาหาร: ปริมาณต่อจาน (ลาบเป็ดตาม textmode 147)
      Map<String, Double> somtum = new LinkedHashMap<String, Double>();
      somtum.put("ING-002", 0.01);
      somtum.put("ING-003", 1.0);
      somtum.put("ING-005", 0.1);
      recipes.put("M-01", somtum);
      Map<String, Double> laab = new LinkedHashMap<String, Double>();
      laab.put("ING-001", 0.1);
      laab.put("ING-002", 0.01);
      laab.put("ING-003", 1.0);
      laab.put("ING-004", 0.01);
      laab.put("ING-005", 0.1);
      recipes.put("M-02", laab);

      // สมาชิก MEM-001 สมชาย ใจดี 1,250 คะแนน ผูกกับลูกค้า CUST-001 (textmode 144)
      Member somchai = new Member("MEM-001", "สมชาย", "ใจดี", "081-234-5678", 1250);
      members.add(somchai);
      customers.add(new Customer("CUST-001", somchai));
      idCounters.put("MEM", 1);
      idCounters.put("CUST", 1);

      supplier = new Supplier("SUP-001", "เบทาโกร");
      salesEmployee = new SalesEmployee("EMP-01");
      kitchenEmployee = new KitchenEmployee("CHEF-01");
      manager = new Manager("MGR-01", "เจ้าของร้าน");
      cashMethod = new PaymentMethod("PM-01", "เงินสด (Cash)");
      qrMethod = new PaymentMethod("PM-02", "QR PromptPay");
      pendingStatus = new PaymentStatus("PS-01", "รอชำระเงิน");
      paidStatus = new PaymentStatus("PS-02", "ชำระเงินสำเร็จ");

      seedJuneSalesHistory();
   }

   // ประวัติการขายเดือนมิถุนายน 2569 (ค.ศ. 2026) เพื่อให้รายงาน FR5 มีข้อมูลตัวอย่าง
   // (ตกลงกับผู้ใช้: ข้อมูลตัวอย่าง + นับรวมออเดอร์จริงที่ชำระเงินในรอบที่รัน)
   // ใช้ Random แบบกำหนด seed ตัวเลขจึงออกมาเหมือนเดิมทุกครั้ง
   private void seedJuneSalesHistory()
   {
      Random random = new Random(2569);
      int[] busyHours = { 11, 12, 12, 12, 13, 17, 18, 18, 18, 19, 20 };
      String[] soldMenuIds = { "M-01", "M-02", "M-03", "M-04", "M-05" };
      Customer walkIn = new Customer("CUST-000", null);
      int sequence = 0;
      for (int day = 1; day <= 30; day++)
      {
         int orderCount = 8 + random.nextInt(7); // 8-14 ออเดอร์ต่อวัน
         for (int n = 0; n < orderCount; n++)
         {
            sequence++;
            Calendar c = new GregorianCalendar();
            c.clear();
            c.set(2026, Calendar.JUNE, day, busyHours[random.nextInt(busyHours.length)],
               random.nextInt(60));
            Table table = tables.get(random.nextInt(tables.size()));
            Order order = new Order(String.format("H-ORD-%04d", sequence), table, salesEmployee, walkIn);
            String basketId = String.format("H-BSK-%04d", sequence);

            List<Basket> lines = new ArrayList<Basket>();
            List<String> used = new ArrayList<String>();
            int lineCount = 1 + random.nextInt(3);
            while (lines.size() < lineCount)
            {
               String menuId = soldMenuIds[random.nextInt(soldMenuIds.length)];
               if (used.contains(menuId))
                  continue;
               used.add(menuId);
               Basket line = new Basket(basketId, order.getOrderId(), table.getTableId());
               line.selectAndAddItemsToBasket(findMenu(menuId), 1 + random.nextInt(3));
               line.calculateAndSaveBasket();
               line.updateCookingStatus(Basket.STATUS_DONE);
               deductedLines.add(line); // ประวัติเก่า ไม่ต้องตัดสต็อกอีก
               lines.add(line);
            }
            order.addBasketItems(lines.toArray(new Basket[0]));

            Payment payment = new Payment(String.format("H-PAY-%04d", sequence), order.getOrderId(),
               salesEmployee, null, pendingStatus);
            payment.summarizePaymentAmount(order);
            payment.processPayment(order.getOrderGrandTotal(), cashMethod, paidStatus);
            order.setPayment(payment);
            orders.add(order);
            receipts.add(new Receipt(String.format("H-REC-%04d", sequence), order, payment, c.getTime()));
         }
      }
   }

   // ===================================================================
   //  การรับค่า / แสดงผล Text Mode (Scanner + System.out)
   // ===================================================================

   // อ่านข้อความ 1 บรรทัดจากคีย์บอร์ด; ถ้าไม่มีข้อมูลเข้าแล้ว (EOF) ให้จบโปรแกรม
   private String readLine()
   {
      if (!input.hasNextLine())
      {
         System.out.println();
         System.exit(0);
      }
      String line = input.nextLine().trim();
      inputSinceClear = true;
      messageSinceInput = false;
      return line;
   }

   // ขึ้นหน้าจอใหม่แบบคำสั่ง cls: ล้างหน้าจอเก่าออกก่อนแสดงหน้าถัดไป
   // ล้างเฉพาะเมื่อผู้ใช้พิมพ์คำสั่งแล้ว (หน้าที่แสดงต่อกันโดยไม่มีการพิมพ์ จะอยู่ในหน้าเดียวกัน)
   // ถ้ามีข้อความ [ระบบ] ค้างอยู่ ให้กด Enter ก่อน เพื่อให้อ่านข้อความทันก่อนหน้าจอถูกล้าง
   private void newPage()
   {
      if (!inputSinceClear)
         return;
      inputSinceClear = false;
      if (!CAN_CLEAR_SCREEN)
         return;
      if (messageSinceInput)
      {
         System.out.print("   (กด Enter เพื่อดำเนินการต่อ...)");
         readLine();
         inputSinceClear = false;
      }
      System.out.flush();
      try
      {
         if (System.getProperty("os.name").startsWith("Windows"))
            new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor(); // เหมือนพิมพ์ cls
         else
            System.out.print("[H[2J"); // Mac/Linux: รหัสล้างหน้าจอ (ANSI)
      }
      catch (Exception e)
      {
         System.out.println(); // ล้างไม่ได้ ก็แสดงต่อด้านล่างตามปกติ
      }
   }

   // แสดงคำถาม แล้วรอรับคำตอบในบรรทัด "   >" แบบเดียวกับ textmode
   private String prompt(String message)
   {
      System.out.println(message);
      System.out.print("   >");
      return readLine();
   }

   // วนรอจนกว่าผู้ใช้จะพิมพ์ 0 (หน้าจอที่มีตัวเลือก "[0] กลับสู่หน้าหลัก")
   private void waitForZero(String options)
   {
      System.out.println(options);
      System.out.println(SINGLE_LINE);
      while (!prompt("กรุณาเลือกคำสั่งดำเนินการ:").equals("0"))
         systemMessage("พิมพ์ 0 เพื่อกลับ");
   }

   // หัวหน้าจอ: ===== / ชื่อหน้าจอ / ===== (ทุกภาพใน textmode_original)
   private void header(String... titles)
   {
      newPage(); // ล้างหน้าจอเก่าก่อนแสดงหน้าใหม่
      System.out.println();
      System.out.println(DOUBLE_LINE);
      for (String title : titles)
         System.out.println("   " + title);
      System.out.println(DOUBLE_LINE);
   }

   // ข้อความจากระบบ "[ระบบ]: ..." ตามที่ใช้ใน textmode
   private static void systemMessage(String message)
   {
      System.out.println("[ระบบ]: " + message);
      messageSinceInput = true;
   }

   private static int parseIntOrDefault(String text, int defaultValue)
   {
      try { return Integer.parseInt(text.trim()); }
      catch (NumberFormatException e) { return defaultValue; }
   }

   private static double parseDoubleOrDefault(String text, double defaultValue)
   {
      try { return Double.parseDouble(text.trim().replace(",", "")); }
      catch (NumberFormatException e) { return defaultValue; }
   }

   private static double sumItemPrice(Basket[] lines)
   {
      double total = 0.0;
      for (Basket line : lines)
         total += line.getItemPrice();
      return total;
   }

   private static int sumQuantity(Basket[] lines)
   {
      int total = 0;
      for (Basket line : lines)
         total += line.getMenuQuantity();
      return total;
   }

   // เรียง Map ตามค่าจากมากไปน้อย (ใช้จัดอันดับเมนูขายดี)
   private static List<Map.Entry<String, Integer>> sortByValue(Map<String, Integer> map)
   {
      List<Map.Entry<String, Integer>> entries = new ArrayList<Map.Entry<String, Integer>>(map.entrySet());
      Collections.sort(entries, new Comparator<Map.Entry<String, Integer>>()
      {
         public int compare(Map.Entry<String, Integer> a, Map.Entry<String, Integer> b)
         {
            int byValue = b.getValue().compareTo(a.getValue());
            return byValue != 0 ? byValue : a.getKey().compareTo(b.getKey());
         }
      });
      return entries;
   }

   // ราคาแบบ "70.-" (textmode 141-143)
   private static String formatPrice(double amount)
   {
      return (amount == Math.rint(amount)) ? String.format("%,.0f.-", amount)
         : String.format("%,.2f", amount);
   }

   // เงินทศนิยม 2 ตำแหน่ง "275.50" (textmode 155-157)
   private static String formatMoney(double amount)
   {
      return String.format("%,.2f", amount);
   }

   // ตัวเลขจำนวนเต็มมีจุลภาค "1,250", "7,520" (textmode 144, 151)
   private static String formatNumber(double amount)
   {
      return String.format("%,.0f", amount);
   }

   // ปริมาณวัตถุดิบตัดศูนย์ท้าย "0.1", "1.7", "11" (textmode 147-148)
   private static String formatQuantity(double quantity)
   {
      if (quantity == Math.rint(quantity))
         return String.format("%.0f", quantity);
      return String.format("%.3f", quantity).replaceAll("0+$", "");
   }

   // วันที่ พ.ศ. "01/06/2569"
   private static String formatThaiDate(Date date)
   {
      Calendar c = new GregorianCalendar();
      c.setTime(date);
      return String.format("%02d/%02d/%04d", c.get(Calendar.DAY_OF_MONTH),
         c.get(Calendar.MONTH) + 1, c.get(Calendar.YEAR) + 543);
   }

   // เวลา "12:00" (textmode 159, 161)
   private static String formatTime(Date date)
   {
      if (date == null)
         return "--:--";
      Calendar c = new GregorianCalendar();
      c.setTime(date);
      return String.format("%02d:%02d", c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
   }

   // วันที่ย่อ "01/07/69 (06.12น.)" (ประวัติรับเข้าสต็อก textmode 148)
   private static String formatShortThaiDateTime(Date date)
   {
      Calendar c = new GregorianCalendar();
      c.setTime(date);
      return String.format("%02d/%02d/%02d (%02d.%02dน.)", c.get(Calendar.DAY_OF_MONTH),
         c.get(Calendar.MONTH) + 1, (c.get(Calendar.YEAR) + 543) % 100,
         c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
   }

   // "มิถุนายน 2569" (textmode 153)
   private static String formatThaiMonthYear(Date date)
   {
      String[] months = { "มกราคม", "กุมภาพันธ์", "มีนาคม", "เมษายน", "พฤษภาคม",
         "มิถุนายน", "กรกฎาคม", "สิงหาคม", "กันยายน", "ตุลาคม", "พฤศจิกายน", "ธันวาคม" };
      Calendar c = new GregorianCalendar();
      c.setTime(date);
      return months[c.get(Calendar.MONTH)] + " " + (c.get(Calendar.YEAR) + 543);
   }

   // แปลง "01/06/2569" (พ.ศ.) เป็น Date; คืน null ถ้ารูปแบบผิด
   private static Date parseThaiDate(String text)
   {
      try
      {
         String[] parts = text.trim().split("/");
         Calendar c = new GregorianCalendar();
         c.setLenient(false);
         c.clear();
         c.set(Integer.parseInt(parts[2]) - 543, Integer.parseInt(parts[1]) - 1,
            Integer.parseInt(parts[0]));
         return c.getTime();
      }
      catch (RuntimeException e)
      {
         return null;
      }
   }

   private static long startOfDay(Date date)
   {
      Calendar c = new GregorianCalendar();
      c.setTime(date);
      c.set(Calendar.HOUR_OF_DAY, 0);
      c.set(Calendar.MINUTE, 0);
      c.set(Calendar.SECOND, 0);
      c.set(Calendar.MILLISECOND, 0);
      return c.getTimeInMillis();
   }
} // end class Main
