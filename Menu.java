// Menu.java
// Class : Menu (เมนู) - จัดเก็บรายละเอียด ราคา หมวดหมู่ และสถานะจำหน่ายของเมนู
// ที่มา: attributeAll_meaning/328.jpg, ClassDiagram_Operations_Composition/392.jpg
// Composition: "A Menu has a Ingredient" (380.jpg) -> ingredients: Ingredient[]
// Responsibility 12,13,14 (conceptual_responsibility.jpg):
//   12.Display Menu Information, 13.Add New Menu, 14.Update Menu Price

public class Menu
{
   // ค่าสถานะพร้อมจำหน่ายตามความหมาย attribute: "ขาย", "หมด" (328.jpg)
   public static final String STATUS_AVAILABLE = "ขาย";
   public static final String STATUS_SOLD_OUT = "หมด";

   private String menuId;            // รหัสเมนูอาหาร (Primary Key) เช่น "M-01"
   private String menuName;          // ชื่อเมนู เช่น "ส้มตำ", "ลาบเป็ด"
   private String category;          // หมวดหมู่ เช่น "อาหารคาว", "เครื่องดื่ม"
   private Double price;             // ราคาอาหาร เช่น 70.00
   private String description;       // รายละเอียดเมนู
   private String imagePath;         // ที่อยู่ไฟล์รูปภาพ เช่น "images/laab.png"
   private String menuStatus;        // สถานะพร้อมจำหน่าย เช่น "ขาย", "หมด"
   private Ingredient[] ingredients; // composition A Menu has a Ingredient

   // constructor ไม่มี parameter (+ Menu() ตาม Class Diagram) ทำงานตอน new Menu() ในหน้าเพิ่มเมนู (FR4)
   // สถานะเริ่มเป็น "ขาย" เพราะเมนูใหม่พร้อมขาย และ ingredients เป็น array ว่าง (กัน null) จากนั้นเรียก addNewMenu()
   public Menu()
   {
      menuId = "";
      menuName = "";
      category = "";
      price = 0.0;
      description = "";
      imagePath = "";
      menuStatus = STATUS_AVAILABLE;
      ingredients = new Ingredient[0];
   }

   // constructor สำหรับสร้างเมนูตัวอย่าง M-01..M-05 ใน Main.seedData()
   public Menu(String theId, String theName, String theCategory, Double thePrice,
      String theDescription, String theImagePath, String theStatus,
      Ingredient[] theIngredients)
   {
      menuId = theId;
      menuName = theName;
      category = theCategory;
      price = thePrice;
      description = theDescription;
      imagePath = theImagePath;
      menuStatus = theStatus;
      ingredients = theIngredients;
   }

   // + displayMenuInformation(): void
   // UseCase FR1 ข้อ 4: "ระบบ แสดง รายการเมนูหรือเครื่องดื่มในรูปแบบข้อความ"
   // รูปแบบบรรทัด: "[M-01]   ส้มตำ   70.- บาท" (textmode 141)
   public void displayMenuInformation()
   {
      System.out.print(String.format("      [%s]   %-12s %,.0f.- บาท", menuId, menuName, price));
      if (STATUS_SOLD_OUT.equals(menuStatus))
         System.out.print("  (หมด)");
      System.out.println();
   }

   // + addNewMenu(): void
   // UseCase FR4 ข้อ 3: "ผู้จัดการร้าน บันทึก ข้อมูลเมนูอาหารใหม่" (textmode 149)
   // เมนูใหม่เริ่มต้นสถานะ "ขาย"
   public void addNewMenu(String newMenuId, String newName, String newCategory,
      Double newPrice, String newDescription, String newImagePath)
   {
      menuId = newMenuId;
      menuName = newName;
      category = newCategory;
      price = newPrice;
      description = newDescription;
      imagePath = newImagePath;
      menuStatus = STATUS_AVAILABLE;
   }

   // + updateMenuPrice(): void
   // UseCase FR4 ข้อ 4: "ระบบ บันทึก ข้อมูลการแก้ไขราคาใหม่เข้าสู่ระบบ" (textmode 150)
   public void updateMenuPrice(Double newPrice)
   {
      price = newPrice;
   }

   public String getMenuId() { return menuId; } // getter: คืนค่า รหัสเมนูอาหาร
   public String getMenuName() { return menuName; } // getter: คืนค่า ชื่อเมนู
   public String getCategory() { return category; } // getter: คืนค่า หมวดหมู่
   public Double getPrice() { return price; } // getter: คืนค่า ราคาอาหาร
   public String getDescription() { return description; } // getter: คืนค่า รายละเอียดเมนู
   public String getImagePath() { return imagePath; } // getter: คืนค่า ที่อยู่ไฟล์รูปภาพ
   public String getMenuStatus() { return menuStatus; } // getter: คืนค่า สถานะพร้อมจำหน่าย
   public Ingredient[] getIngredients() { return ingredients; } // getter: คืนค่า Ingredient (ส่วนประกอบ composition)

   // setter: ใช้ในหน้า [E] แก้ไข ของ FR4 (textmode 151)
   //   [N] แก้ไขชื่อเมนู, [C] แก้ไขประเภท, [T] เปลี่ยนสถานะ ขาย/หมด
   //   (การแก้ราคาใช้ updateMenuPrice() ตาม Class Diagram)
   public void setMenuName(String newName) { menuName = newName; } // setter: แก้ไขค่า ชื่อเมนู
   public void setCategory(String newCategory) { category = newCategory; } // setter: แก้ไขค่า หมวดหมู่
   public void setMenuStatus(String newStatus) { menuStatus = newStatus; } // setter: แก้ไขค่า สถานะพร้อมจำหน่าย
} // end class Menu
