# 🎬 MovieApp - Ứng dụng xem phim Android

![Android](https://img.shields.io/badge/Platform-Android-brightgreen) ![Kotlin](https://img.shields.io/badge/Language-Kotlin-blue) ![Firebase](https://img.shields.io/badge/Backend-Firebase-orange) ![PayOS](https://img.shields.io/badge/Payment-PayOS-blueviolet)

**MovieApp** là một ứng dụng xem phim dành cho nền tảng Android, hỗ trợ người dùng xem phim online, đăng ký gói thuê bao hoặc mua lẻ từng phim. Ứng dụng sử dụng các công nghệ hiện đại như Firebase, PayOS, TMDB API và Bunny Stream để cung cấp trải nghiệm người dùng mượt mà và bảo mật.

---

## 🚀 Tính năng chính

- 🔐 **Xác thực người dùng** với Firebase Authentication
- 📺 **Xem danh sách phim** lấy từ API TMDB (poster, thông tin chi tiết, đánh giá)
- 💳 **Thanh toán online** bằng PayOS thông qua Railway backend
- 🎞️ **Lưu trữ video** chất lượng cao bằng Bunny Stream
- 🔔 **Nhận thông báo đẩy** khi có phim mới, khuyến mãi
- 📦 **Gói thuê bao** linh hoạt (1/3/12 tháng) hoặc mua phim lẻ
- 🧾 **Lưu trữ lịch sử giao dịch** theo người dùng trên Firebase Firestore

---

## 🛠️ Công nghệ sử dụng

| Thành phần | Công nghệ |
|------------|-----------|
| Ngôn ngữ   | Kotlin, Java (backend) |
| Android    | Android Studio, ViewModel, RecyclerView, Glide |
| Backend    | Node.js, ExpressJS (triển khai trên Railway) |
| CSDL       | Firebase Firestore (NoSQL) |
| Thanh toán | [PayOS](https://payos.vn/) |
| API phim   | [TMDB API](https://developer.themoviedb.org/) |
| Streaming  | [Bunny Stream](https://bunny.net/video/) |
| Khác       | Firebase Cloud Messaging, Firebase Admin SDK, HMAC verification

---

## 📸 Giao diện (Screenshots)
<img src="https://github.com/user-attachments/assets/8618963b-8983-46cf-a58c-73e4a80a24a1" alt = "Home" width="200"/>
<img src="https://github.com/user-attachments/assets/8ca3ab97-b1c9-4c3e-9123-bb28fa942a49" alt = "Danh mục" width="200"/>
<img src="https://github.com/user-attachments/assets/8c567907-96c7-4344-a9b2-7f45576c2921" alt = "watchMovie" width="200"/>
<img src="https://github.com/user-attachments/assets/f083967b-5f95-49ba-bf58-e3978eb5408c" alt = "Detail" width="200"/>




