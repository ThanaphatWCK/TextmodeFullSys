// PointRedemption.java
// Class : PointRedemption (การใช้คะแนนแลกส่วนลด)
//   บันทึกประวัติการคำนวณแต้มและการนำคะแนนไปแลกส่วนลด
// ที่มา: attributeAll_meaning/331.jpg, ClassDiagram_Operations_Composition/394.jpg
// Composition: "A Point Redemption has a Member" (382.jpg) -> member: Member
// Responsibility 6: Redeem Points for Discount (conceptual_responsibility.jpg)

public class PointRedemption
{
   // เงื่อนไขการแลก: 100 คะแนน = ส่วนลด 10 บาท (textmode 145)
   public static final int POINTS_PER_REDEEM_UNIT = 100;
   public static final double BAHT_PER_REDEEM_UNIT = 10.0;

   private String redemptionId;     // รหัสรายการแลกแต้ม (Primary Key) เช่น "RED-001"
   private String memberId;         // รหัสสมาชิกที่ใช้สิทธิ์ เช่น "MEM-001"
   private Double purchaseAmount;   // ยอดซื้อที่นำมาคิดคะแนน เช่น 500.00
   private Integer pointsEarned;    // คะแนนสะสมที่ได้รับเพิ่ม เช่น 50
   private Integer pointsRedeemed;  // คะแนนที่นำไปแลกส่วนลด เช่น 500
   private Double discountReceived; // มูลค่าส่วนลดที่ได้รับ (บาท) เช่น 50.00
   private Integer remainingPoints; // คะแนนคงเหลือหลังการแลก เช่น 750
   private Member member;           // composition A Point Redemption has a Member

   // constructor ไม่มี parameter (+ PointRedemption() ตาม Class Diagram) ทำงานตอน new PointRedemption()
   // ยอดซื้อ/คะแนน/ส่วนลดเริ่มที่ 0 เพราะยังไม่มีการสะสมหรือแลกแต้ม และ member = null รอผูกสมาชิก
   public PointRedemption()
   {
      redemptionId = "";
      memberId = "";
      purchaseAmount = 0.0;
      pointsEarned = 0;
      pointsRedeemed = 0;
      discountReceived = 0.0;
      remainingPoints = 0;
      member = null;
   }

   // constructor ใช้ตอนเริ่มบันทึกรายการแต้มของสมาชิกในออเดอร์ (FR2)
   public PointRedemption(String theRedemptionId, Member theMember)
   {
      this();
      redemptionId = theRedemptionId;
      member = theMember;
      memberId = theMember.getMemberId();
      remainingPoints = theMember.getAccumulatedPoints();
   }

   // + redeemPointsForDiscount(): void
   // UseCase FR2 ข้อ 8: "ระบบ บันทึก การใช้คะแนนแลกส่วนลด และ อัปเดต คะแนนคงเหลือเข้าสู่ระบบ"
   // parameter: จำนวนคะแนนที่ต้องการแลก (พนักงานระบุ ข้อ 8 ย่อย 1)
   public void redeemPointsForDiscount(Integer points)
   {
      pointsRedeemed = pointsRedeemed + points;
      // ส่วนลด = (คะแนน / 100) x 10 บาท  เช่น 500 คะแนน -> 50 บาท
      discountReceived = (pointsRedeemed / POINTS_PER_REDEEM_UNIT) * BAHT_PER_REDEEM_UNIT;
      // หักคะแนนออกจากสมาชิก แล้วเก็บคะแนนคงเหลือ เช่น 1,250 - 500 = 750
      member.setAccumulatedPoints(member.getAccumulatedPoints() - points);
      remainingPoints = member.getAccumulatedPoints();
   }

   // บันทึกยอดซื้อและคะแนนที่ได้รับ (FR2 ข้อ 7 "บันทึกประวัติการคำนวณแต้ม" ตามความหมายคลาส)
   public void recordEarnedPoints(Double thePurchaseAmount, Integer thePointsEarned)
   {
      purchaseAmount = thePurchaseAmount;
      pointsEarned = pointsEarned + thePointsEarned;
      remainingPoints = member.getAccumulatedPoints();
   }

   public String getRedemptionId() { return redemptionId; } // getter: คืนค่า รหัสรายการแลกแต้ม
   public String getMemberId() { return memberId; } // getter: คืนค่า รหัสสมาชิกที่ใช้สิทธิ์
   public Double getPurchaseAmount() { return purchaseAmount; } // getter: คืนค่า ยอดซื้อที่นำมาคิดคะแนน
   public Integer getPointsEarned() { return pointsEarned; } // getter: คืนค่า คะแนนสะสมที่ได้รับเพิ่ม
   public Integer getPointsRedeemed() { return pointsRedeemed; } // getter: คืนค่า คะแนนที่นำไปแลกส่วนลด
   public Double getDiscountReceived() { return discountReceived; } // getter: คืนค่า มูลค่าส่วนลดที่ได้รับ
   public Integer getRemainingPoints() { return remainingPoints; } // getter: คืนค่า คะแนนคงเหลือหลังการแลก
   public Member getMember() { return member; } // getter: คืนค่า Member (ส่วนประกอบ composition)
} // end class PointRedemption
