// Customer.java
// Class : Customer (ลูกค้า) - จัดเก็บข้อมูลลูกค้าที่เข้ามานั่งรับประทานอาหารที่โต๊ะ
// ที่มา: attributeAll_meaning/327.jpg, ClassDiagram_Operations_Composition/391.jpg
// Composition: "A Customer has a Member" (CompositionRelationship/382.jpg)
//   multiplicity Customer 1 --- 0..1 Member -> ลูกค้าอาจไม่เป็นสมาชิกก็ได้ (member = null)

public class Customer
{
   private String customerId; // รหัสลูกค้า (Primary Key) เช่น "CUST-001"
   private String memberId;   // รหัสสมาชิกที่เชื่อมโยง เช่น "MEM-001" ("" = ไม่ใช่สมาชิก)
   private Member member;     // composition A Customer has a Member

   // constructor ไม่มี parameter (+ Customer() ตาม Class Diagram) ทำงานตอน new Customer()
   // member = null และ memberId = "" หมายถึงยังไม่เป็นสมาชิก (multiplicity 0..1)
   public Customer()
   {
      customerId = "";
      memberId = "";
      member = null;
   }

   // constructor สำหรับสร้างลูกค้าตัวอย่าง/ลูกค้าใหม่ใน Main.seedData()
   public Customer(String theCustomerId, Member theMember)
   {
      customerId = theCustomerId;
      setMember(theMember);
   }

   // ผูกสมาชิกให้ลูกค้า (FR2 เมื่อพนักงานใช้สิทธิ์สมาชิกกับออเดอร์ของลูกค้า)
   public void setMember(Member theMember)
   {
      member = theMember;
      memberId = (theMember == null) ? "" : theMember.getMemberId();
   }

   // ตรวจว่าลูกค้าเป็นสมาชิกหรือไม่ (ใช้ตัดสินส่วนลดสมาชิก 5% ใน FR6)
   public boolean isMember()
   {
      return member != null;
   }

   public String getCustomerId() { return customerId; } // getter: คืนค่า รหัสลูกค้า
   public String getMemberId() { return memberId; } // getter: คืนค่า รหัสสมาชิกที่เชื่อมโยง
   public Member getMember() { return member; } // getter: คืนค่า Member (ส่วนประกอบ composition)
} // end class Customer
