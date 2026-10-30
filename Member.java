// Member.java
// Class : Member (สมาชิก) - จัดเก็บข้อมูลสมาชิกและแต้มคะแนนสะสม
// ที่มา: attributeAll_meaning/329.jpg, ClassDiagram_Operations_Composition/392.jpg
// Responsibility 4,5,7 (conceptual_responsibility.jpg):
//   4.Verify Member Information, 5.Collect Member Points, 7.Register New Member

public class Member
{
   private String memberId;           // รหัสสมาชิก (Primary Key) เช่น "MEM-001"
   private String firstName;          // ชื่อจริงสมาชิก เช่น "สมชาย"
   private String lastName;           // นามสกุล เช่น "ใจดี"
   private String phone;              // หมายเลขโทรศัพท์ เช่น "081-234-5678"
   private Integer accumulatedPoints; // คะแนนสะสมปัจจุบัน เช่น 1250

   // constructor ไม่มี parameter (+ Member() ตาม Class Diagram) ทำงานตอน new Member() ในหน้าสมัครสมาชิก (FR2)
   // ข้อความตั้งเป็น "" และคะแนนสะสม = 0 เพราะสมาชิกใหม่ยังไม่มีแต้ม จากนั้นค่อยเรียก registerNewMember()
   public Member()
   {
      memberId = "";
      firstName = "";
      lastName = "";
      phone = "";
      accumulatedPoints = 0;
   }

   // constructor สำหรับสร้างสมาชิกตัวอย่าง MEM-001 (สมชาย ใจดี) ใน Main.seedData()
   public Member(String theMemberId, String theFirstName, String theLastName,
      String thePhone, Integer thePoints)
   {
      memberId = theMemberId;
      firstName = theFirstName;
      lastName = theLastName;
      phone = thePhone;
      accumulatedPoints = thePoints;
   }

   // + verifyMemberInformation(): void
   // UseCase FR2 ข้อ 6: "ระบบ แสดง รายละเอียดสมาชิกและคะแนนสะสมปัจจุบัน" (textmode 144)
   // parameter: Customer ที่ผูกกับสมาชิก (เพื่อแสดงรหัสลูกค้า)
   public void verifyMemberInformation(Customer customer)
   {
      System.out.println("[ รายละเอียดสมาชิก ]");
      if (customer != null)
         System.out.println("   รหัสลูกค้า   : " + customer.getCustomerId());
      System.out.println("   รหัสสมาชิก   : " + memberId);
      System.out.println("   ชื่อสมาชิก    : " + getFullName());
      System.out.println("   เบอร์โทรศัพท์ : " + phone);
      System.out.println("   คะแนนสะสม    : "
         + String.format("%,d", accumulatedPoints) + " คะแนน"); // เช่น 1,250 คะแนน
   }

   // + collectMemberPoints(): void
   // UseCase FR2 ข้อ 7: "ระบบ บันทึก และ แสดง คะแนนสะสมรวมปัจจุบัน" (textmode 145)
   // parameter: จำนวนคะแนนที่ได้รับเพิ่มจากยอดซื้อ
   public void collectMemberPoints(Integer pointsEarned)
   {
      accumulatedPoints = accumulatedPoints + pointsEarned;
   }

   // + registerNewMember(): void
   // UseCase FR2 ข้อ 9: "พนักงานขาย บันทึก ข้อมูลสมาชิกใหม่" (textmode 146)
   // สมาชิกใหม่เริ่มต้นคะแนนสะสม 0
   public void registerNewMember(String newMemberId, String newFirstName,
      String newLastName, String newPhone)
   {
      memberId = newMemberId;
      firstName = newFirstName;
      lastName = newLastName;
      phone = newPhone;
      accumulatedPoints = 0;
   }

   // ชื่อเต็ม "สมชาย ใจดี" ใช้ในหลายหน้าจอ (textmode 144, 155, 157)
   public String getFullName()
   {
      return firstName + " " + lastName;
   }

   public String getMemberId() { return memberId; } // getter: คืนค่า รหัสสมาชิก
   public String getFirstName() { return firstName; } // getter: คืนค่า ชื่อจริงสมาชิก
   public String getLastName() { return lastName; } // getter: คืนค่า นามสกุล
   public String getPhone() { return phone; } // getter: คืนค่า หมายเลขโทรศัพท์
   public Integer getAccumulatedPoints() { return accumulatedPoints; } // getter: คืนค่า คะแนนสะสมปัจจุบัน

   // ใช้โดย PointRedemption.redeemPointsForDiscount() เพื่ออัปเดตคะแนนคงเหลือ
   public void setAccumulatedPoints(Integer points) { accumulatedPoints = points; } // setter: แก้ไขค่า คะแนนสะสมปัจจุบัน
} // end class Member
