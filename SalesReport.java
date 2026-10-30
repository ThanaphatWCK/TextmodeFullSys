// SalesReport.java
// Class : SalesReport (รายงานยอดขาย)
//   จัดเก็บตัวเลขสถิติและผลสรุปยอดขายสำหรับออกรายงาน
// ที่มา: attributeAll_meaning/324.jpg, ClassDiagram_Operations_Composition/390.jpg
// Composition (380.jpg, 382.jpg):
//   A Sales Report has a Manager -> manager: Manager
//   A Sales Report has a Order   -> orders: Order[]  (SalesReport 1 --- 0..* Order)
// Responsibility 15,16: Summarize Sales Report, Export Sales Report

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

public class SalesReport
{
   private String reportId;          // รหัสรายงาน (Primary Key) เช่น "REP-D01", "REP-M01"
   private Date startDate;           // วันเริ่มต้นช่วงดูรายงาน เช่น 2026-06-01
   private Date endDate;             // วันสิ้นสุดช่วงดูรายงาน เช่น 2026-06-30
   private Double dailyTotalSales;   // ยอดขายรวมประจำวัน เช่น 7520.00
   private Integer dailyTotalOrders; // จำนวนออเดอร์ทั้งหมดในวัน เช่น 142
   private Double monthlyTotalSales; // รายได้รวมทั้งเดือน เช่น 50250.00
   private Double dailyAverageSales; // ยอดขายเฉลี่ยต่อวัน เช่น 11621.00
   private String peakSalesTime;     // ช่วงเวลาที่ขายดีที่สุด เช่น "12:00 - 13:00 น."
   private String peakSalesDay;      // วันที่ขายได้สูงสุด เช่น "วันที่ 14 ... (10,500 บาท)"
   private Manager manager;          // composition A Sales Report has a Manager
   private Order[] orders;           // composition A Sales Report has a Order

   // constructor ไม่มี parameter (+ SalesReport() ตาม Class Diagram) ทำงานตอน new SalesReport()
   // วันที่ = วันนี้, ยอดขาย = 0, ช่วงขายดี = "-" (ยังไม่มีข้อมูล) และ orders = array ว่าง รอ summarizeSalesReport()
   public SalesReport()
   {
      reportId = "";
      startDate = new Date();
      endDate = new Date();
      dailyTotalSales = 0.0;
      dailyTotalOrders = 0;
      monthlyTotalSales = 0.0;
      dailyAverageSales = 0.0;
      peakSalesTime = "-";
      peakSalesDay = "-";
      manager = null;
      orders = new Order[0];
   }

   // constructor ใช้ตอนผู้จัดการเลือกช่วงวันและประเภทรายงาน (UseCase FR5 ข้อ 2-4)
   public SalesReport(String theReportId, Manager theManager, Date theStart, Date theEnd)
   {
      this();
      reportId = theReportId;
      manager = theManager;
      startDate = theStart;
      endDate = theEnd;
   }

   // + summarizeSalesReport(): void
   // UseCase FR5 ข้อ 3-4: "ระบบ ประมวลผล และ แสดง ข้อมูลสรุปยอดขายรายวัน/รายเดือน"
   // parameter: ใบเสร็จที่ชำระแล้วในช่วงวันที่เลือก (ยอดขายคิดจากยอดสุทธิที่ชำระเรียบร้อยแล้ว
   //   ตาม textmode 154) และออเดอร์ที่ตรงกับใบเสร็จเหล่านั้น
   public void summarizeSalesReport(Receipt[] receipts, Order[] theOrders)
   {
      orders = theOrders;

      Map<Integer, Double> salesByHour = new LinkedHashMap<Integer, Double>();
      Map<String, Double> salesByDay = new LinkedHashMap<String, Double>();
      Map<String, Date> dayDates = new LinkedHashMap<String, Date>();
      double total = 0.0;
      double firstDayTotal = 0.0;
      int firstDayOrders = 0;
      String firstDayKey = dayKey(startDate);

      for (Receipt receipt : receipts)
      {
         double amount = receipt.getGrandTotal();
         total += amount;

         Calendar c = new GregorianCalendar();
         c.setTime(receipt.getIssuedAt());
         int hour = c.get(Calendar.HOUR_OF_DAY);
         salesByHour.put(hour, valueOf(salesByHour.get(hour)) + amount);

         String dayKey = dayKey(receipt.getIssuedAt());
         salesByDay.put(dayKey, valueOf(salesByDay.get(dayKey)) + amount);
         dayDates.put(dayKey, receipt.getIssuedAt());

         if (dayKey.equals(firstDayKey)) // ยอดของวันเริ่มต้น = ยอดรายวัน
         {
            firstDayTotal += amount;
            firstDayOrders++;
         }
      }

      dailyTotalSales = firstDayTotal;
      dailyTotalOrders = firstDayOrders;
      monthlyTotalSales = total;

      // ยอดขายเฉลี่ยต่อวัน = รายได้รวม / จำนวนวันในช่วงที่เลือก
      long days = (stripTime(endDate) - stripTime(startDate)) / (24L * 60 * 60 * 1000) + 1;
      dailyAverageSales = total / Math.max(1, days);

      // ช่วงเวลาขายดีที่สุด (รายชั่วโมง) เช่น "12:00 - 13:00 น."
      Integer bestHour = null;
      for (Map.Entry<Integer, Double> entry : salesByHour.entrySet())
         if (bestHour == null || entry.getValue() > salesByHour.get(bestHour))
            bestHour = entry.getKey();
      peakSalesTime = (bestHour == null) ? "-" : formatHourSlot(bestHour);

      // วันที่ขายได้สูงสุด เช่น "วันที่ 14 มิถุนายน 2569 (10,500บาท)"
      String bestDay = null;
      for (Map.Entry<String, Double> entry : salesByDay.entrySet())
         if (bestDay == null || entry.getValue() > salesByDay.get(bestDay))
            bestDay = entry.getKey();
      peakSalesDay = (bestDay == null) ? "-" : "วันที่ "
         + formatThaiLongDate(dayDates.get(bestDay)) + " ("
         + String.format("%,.0f", salesByDay.get(bestDay)) + "บาท)";
   }

   // + exportSalesReport(): void
   // UseCase FR5 ข้อ 6-7: "ระบบ สร้าง และ ส่งออก รายงานยอดขาย"
   // บันทึกเป็นไฟล์ข้อความในโฟลเดอร์ reports (ตกลงกับผู้ใช้ว่าใช้ .txt แทน PDF)
   public void exportSalesReport(String fileName, String content) throws IOException
   {
      File folder = new File("reports");
      if (!folder.exists())
         folder.mkdirs();
      Writer writer = new OutputStreamWriter(
         new FileOutputStream(new File(folder, fileName)), "UTF-8");
      try
      {
         writer.write(content);
      }
      finally
      {
         writer.close();
      }
   }

   // ข้อความช่วงเวลา 1 ชั่วโมง เช่น 12 -> "12:00 - 13:00 น."
   public static String formatHourSlot(int hour)
   {
      return String.format("%02d:00 - %02d:00 น.", hour, hour + 1);
   }

   // คีย์ของวัน (ไม่สนเวลา) ใช้จัดกลุ่มยอดขายรายวัน
   private static String dayKey(Date date)
   {
      Calendar c = new GregorianCalendar();
      c.setTime(date);
      return c.get(Calendar.YEAR) + "-" + c.get(Calendar.DAY_OF_YEAR);
   }

   // วันที่เต็มภาษาไทย เช่น "14 มิถุนายน 2569" (textmode 153)
   private static String formatThaiLongDate(Date date)
   {
      String[] months = { "มกราคม", "กุมภาพันธ์", "มีนาคม", "เมษายน", "พฤษภาคม",
         "มิถุนายน", "กรกฎาคม", "สิงหาคม", "กันยายน", "ตุลาคม", "พฤศจิกายน", "ธันวาคม" };
      Calendar c = new GregorianCalendar();
      c.setTime(date);
      return c.get(Calendar.DAY_OF_MONTH) + " " + months[c.get(Calendar.MONTH)] + " "
         + (c.get(Calendar.YEAR) + 543);
   }

   // แปลง Double เป็น double โดยถ้าเป็น null (ยังไม่มียอดของช่วงนั้นใน Map) ให้ถือเป็น 0.0
   private static double valueOf(Double value)
   {
      return (value == null) ? 0.0 : value;
   }

   // ตัดเวลาออกให้เหลือเฉพาะวัน (ใช้นับจำนวนวัน)
   private static long stripTime(Date date)
   {
      Calendar c = new GregorianCalendar();
      c.setTime(date);
      c.set(Calendar.HOUR_OF_DAY, 12); // ใช้เที่ยงวันกันปัญหาเวลาออมแสง
      c.set(Calendar.MINUTE, 0);
      c.set(Calendar.SECOND, 0);
      c.set(Calendar.MILLISECOND, 0);
      return c.getTimeInMillis();
   }

   public String getReportId() { return reportId; } // getter: คืนค่า รหัสรายงาน
   public Date getStartDate() { return startDate; } // getter: คืนค่า วันเริ่มต้นช่วงดูรายงาน
   public Date getEndDate() { return endDate; } // getter: คืนค่า วันสิ้นสุดช่วงดูรายงาน
   public Double getDailyTotalSales() { return dailyTotalSales; } // getter: คืนค่า ยอดขายรวมประจำวัน
   public Integer getDailyTotalOrders() { return dailyTotalOrders; } // getter: คืนค่า จำนวนออเดอร์ทั้งหมดในวัน
   public Double getMonthlyTotalSales() { return monthlyTotalSales; } // getter: คืนค่า รายได้รวมทั้งเดือน
   public Double getDailyAverageSales() { return dailyAverageSales; } // getter: คืนค่า ยอดขายเฉลี่ยต่อวัน
   public String getPeakSalesTime() { return peakSalesTime; } // getter: คืนค่า ช่วงเวลาที่ขายดีที่สุด
   public String getPeakSalesDay() { return peakSalesDay; } // getter: คืนค่า วันที่ขายได้สูงสุด
   public Manager getManager() { return manager; } // getter: คืนค่า Manager (ส่วนประกอบ composition)
   public Order[] getOrders() { return orders; } // getter: คืนค่า Order (ส่วนประกอบ composition)
} // end class SalesReport
