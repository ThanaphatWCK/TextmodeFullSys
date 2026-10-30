@echo off
rem คอมไพล์และรันระบบร้านอาหาร (ต้องติดตั้ง JDK 8 ขึ้นไปที่มี javac)
chcp 65001 > nul
if not exist out mkdir out
javac -encoding UTF-8 -d out *.java || goto :eof
java -Dfile.encoding=UTF-8 -cp out Main
