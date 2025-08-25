# 🎮 Game LAN - JavaFX + Hibernate + SQL Server

## 📌 Giới thiệu
Đây là một project game chạy trên **mạng LAN**, được phát triển bằng:
- **Ngôn ngữ:** Java  
- **Framework:** Hibernate (ORM)  
- **UI:** JavaFX  
- **CSDL:** SQL Server  

Ứng dụng được thiết kế theo mô hình **Client - Server**, cho phép 2 người chơi trong mạng LAN cùng tham gia, đồng bộ dữ liệu qua hệ thống client-server đơn giản.

---

## ⚙️ Tính năng chính
- 🔗 **Kết nối mạng LAN**: Người chơi có thể tham gia thông qua LAN.  
- 🗄 **Quản lý dữ liệu với Hibernate**: Toàn bộ dữ liệu game được ánh xạ (ORM) và lưu trữ vào SQL Server.  
- 🎨 **Giao diện JavaFX**: Xây dựng giao diện trực quan, dễ sử dụng.  
- 🔄 **Đồng bộ dữ liệu real-time** giữa client và server.  

---

## 🚀 Cách chạy ứng dụng

### 1. Yêu cầu hệ thống
- JDK 11+  
- SQL Server (có sẵn database, cấu hình trong `hibernate.cfg.xml`)  
- Thư viện Hibernate + JavaFX đã được thêm vào project  

### 2. Chạy server
Trong folder dist,
chạy ServerInit.exe để khởi tạo server
LittleDream.exe để khởi chạy game (client)

Hoặc vào source để chạy (recommend)