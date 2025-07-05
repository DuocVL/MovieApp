# 🎬 MovieApp – Ứng dụng xem phim trên Android

[![GitHub license](https://img.shields.io/github/license/DuocVL/MovieApp)](https://github.com/DuocVL/MovieApp/blob/main/LICENSE)
[![GitHub stars](https://img.shields.io/github/stars/DuocVL/MovieApp)](https://github.com/DuocVL/MovieApp/stargazers)
[![GitHub forks](https://img.shields.io/github/forks/DuocVL/MovieApp)](https://github.com/DuocVL/MovieApp/network/members)

Một ứng dụng xem phim trên Android, cung cấp trải nghiệm giải trí phong phú với các tính năng như xem phim trực tuyến, tải xuống offline, thanh toán mua phim, đánh giá, bình luận và nhiều hơn nữa.

## Mục lục

- [Tổng quan](#tổng-quan)
- [Tính năng chính](#tính-năng-chính)
- [Công nghệ sử dụng](#công-nghệ-sử-dụng)
- [Kiến trúc ứng dụng](#kiến-trúc-ứng-dụng)
- [Cài đặt và Chạy ứng dụng](#cài-đặt-và-chạy-ứng-dụng)
  - [Yêu cầu hệ thống](#yêu-cầu-hệ-thống)
  - [Cấu hình Backend (Server)](#cấu-hình-backend-server)
  - [Cấu hình Client (Ứng dụng Android)](#cấu-hình-client-ứng-dụng-android)
  - [Chạy ứng dụng](#chạy-ứng-dụng)
- [Cấu trúc Project](#cấu-trúc-project)
- [Demo](#demo)

## Tổng quan

Ứng dụng xem phim này được phát triển bằng Kotlin cho nền tảng Android, cung cấp một thư viện phim phong phú, được cập nhật từ TMDB API. Người dùng có thể khám phá các bộ phim mới, xem chi tiết, xem trailer, và trải nghiệm xem phim trực tuyến hoặc tải xuống để xem ngoại tuyến. Ứng dụng cũng tích hợp hệ thống thanh toán PayOS để mua phim/gói VIP và các tính năng tương tác cộng đồng như đánh giá, bình luận phim.

## Tính năng chính

* **Đăng nhập & Xác thực:**
    * Đăng ký/đăng nhập bằng tài khoản email/mật khẩu.
    * Đăng nhập với tư cách khách (quyền truy cập hạn chế).
* **Duyệt và Tìm kiếm phim:**
    * Hiển thị danh sách phim đa dạng (phim mới, phim nổi bật, theo thể loại).
    * Xem thông tin chi tiết phim (tóm tắt, diễn viên, đạo diễn, poster, trailer).
    * Tìm kiếm phim theo từ khóa và bộ lọc.
    * Gợi ý phim liên quan.
* **Trải nghiệm xem phim:**
    * Phát phim trực tuyến mượt mà từ Bunny Stream.
    * Tải phim xuống để xem ngoại tuyến.
    * Tự động lưu và tiếp tục tiến trình xem phim dở.
* **Tương tác cộng đồng:**
    * Đánh giá phim bằng hệ thống sao.
    * Bình luận phim và xem bình luận của người khác.
    * Thêm phim vào danh sách "Xem sau".
* **Thanh toán:**
    * Mua gói dịch vụ hoặc mua từng bộ phim VIP qua PayOS.
    * Xử lý giao dịch an toàn với mã QR.
* **Thông báo:**
    * Nhận thông báo về phim mới và các chương trình khuyến mãi qua Firebase Cloud Messaging.

## Công nghệ sử dụng

Dự án này sử dụng các công nghệ và thư viện sau:

### Frontend (Ứng dụng Android)

* **Ngôn ngữ:** Kotlin
* **Framework:** Android SDK, Android Jetpack
* **Kiến trúc:** MVC (Model-View-Controller)
* **Thư viện UI:** AndroidX AppCompat, Material Design,...
* **Networking:** OkHttp, Glide (tải ảnh)
* **Media Playback:** ExoPlayer
* **Firebase:** Authentication, Cloud Firestore, Cloud Messaging (FCM)
* **Gradle:** Quản lý dependencies

### Backend (Server API)

* **Ngôn ngữ:** Node.js
* **Framework:** Express.js
* **Deployment:** Railway
* **Database:** Firebase Cloud Firestore
* **API Integrations:**
    * TMDB API (The Movie Database)
    * PayOS API (Cổng thanh toán)
    * Firebase Admin SDK (tương tác với Firebase)

### Lưu trữ & Phân phối Nội dung

* **Video Hosting:** Bunny Stream

## Kiến trúc ứng dụng

Ứng dụng được thiết kế theo kiến trúc Client-Server.

**Client (Ứng dụng Android):** Tuân theo mô hình MVC.
* **View:** Các Activity/Fragment hiển thị UI và lắng nghe sự kiện.
* **Controller:** Các Activity/Fragment trực tiếp xử lý logic UI, gọi API, tương tác với Firebase và cập nhật View.
* **Model:** Các Dataclass và các lớp trực tiếp tương tác với các nguồn dữ liệu (OkHttp calls to TMDB, Firebase Firestore calls).

**Server (Backend Node.js API):**
* Cung cấp các RESTful API cho ứng dụng di động.
* Xử lý logic thanh toán với PayOS, quản lý trạng thái giao dịch.
* Quản lý việc gửi thông báo đẩy.
* Làm cầu nối giữa ứng dụng và Firebase (Firestore, FCM).

**Dịch vụ bên ngoài:** TMDB, Bunny Stream, PayOS, Firebase.


## Cài đặt và Chạy ứng dụng

Làm theo các bước dưới đây để cài đặt và chạy dự án cục bộ trên máy tính của bạn.

### Yêu cầu hệ thống

* **Android Studio:** Phiên bản Arctic Fox 2020.3.1 trở lên (hoặc phiên bản bạn đang dùng).
* **JDK:** Phiên bản 11 trở lên.
* **Node.js:** Phiên bản 14.x trở lên.
* **npm :** Trình quản lý gói cho Node.js.
* **Git:** Để clone repository.
* Kết nối Internet ổn định.

### Cấu hình Backend (Server)

1.  **Clone mã nguồn Backend:**
    ```bash
    git clone https://github.com/DuocVL/cloudinary-backend.git
    cd cloudinary-backend # <-- Thay đổi tên thư mục nếu khác
    ```
2.  **Cài đặt Dependencies:**
    ```bash
    npm install # hoặc yarn install
    ```
3.  **Cấu hình biến môi trường:**
    Tạo file `.env` trong thư mục gốc của backend và điền các thông tin sau:
    ```
    PORT=3000
    PAYOS_CLIENT_ID=YOUR_PAYOS_CLIENT_ID
    PAYOS_API_KEY=YOUR_PAYOS_API_KEY
    PAYOS_CHECKSUM_KEY=YOUR_PAYOS_CHECKSUM_KEY
    FIREBASE_SERVICE_ACCOUNT_PATH=./path/to/your/firebase-service-account.json # Đảm bảo đường dẫn chính xác
    ```
    * **PAYOS_CLIENT_ID, PAYOS_API_KEY, PAYOS_CHECKSUM_KEY:** Lấy từ tài khoản PayOS Developer của bạn.
    * **FIREBASE_SERVICE_ACCOUNT_PATH:** Đường dẫn đến file JSON Service Account Key của Firebase. File này cần được tạo từ Firebase Console (`Project settings` -> `Service accounts`).
4.  **Chạy Backend (Development):**
    ```bash
    npm start # hoặc npm run dev (nếu có script dev)
    ```
    Backend sẽ chạy trên cổng được cấu hình (mặc định là 3000 hoặc PORT bạn thiết lập).

### Cấu hình Client (Ứng dụng Android)

1.  **Clone mã nguồn Client:**
    ```bash
    git clone [https://github.com/DuocVL/MovieApp.git](https://github.com/DuocVL/MovieApp.git)
    cd MovieApp
    ```
2.  **Mở Project trong Android Studio:**
    Mở thư mục `MovieApp` (chứa file `build.gradle` cấp project) trong Android Studio.
3.  **Cấu hình API Keys và Endpoint Backend:**
    * Tạo hoặc chỉnh sửa file `local.properties` (nếu chưa có) ở thư mục gốc của project Android (cùng cấp với `build.gradle`):
        ```properties
        tmdbApiKey="YOUR_TMDB_API_KEY"
        backendBaseUrl="http://YOUR_LOCAL_BACKEND_IP:3000" # Hoặc URL của Railway nếu đã deploy
        ```
        * Thay `YOUR_LOCAL_BACKEND_IP` bằng địa chỉ IP cục bộ của máy bạn nếu bạn đang chạy backend trên máy tính. Nếu bạn deploy lên Railway, hãy sử dụng URL đã được cung cấp.
    * **Kết nối Firebase:** Tải file `google-services.json` từ Firebase Console (`Project settings` -> `General` -> `Your apps` -> `Android`) và đặt nó vào thư mục `app/` của project Android.
4.  **Đồng bộ Gradle:** Sau khi cấu hình, Android Studio sẽ nhắc bạn đồng bộ Gradle. Nhấp vào `Sync Now` nếu có.

### Chạy ứng dụng

1.  Đảm bảo Backend Node.js của bạn đang chạy (nếu chạy cục bộ).
2.  Trong Android Studio, chọn một thiết bị giả lập (Emulator) hoặc kết nối thiết bị Android vật lý của bạn.
3.  Nhấp vào nút `Run 'app'` (biểu tượng mũi tên màu xanh lá) trên thanh công cụ của Android Studio.

## 📁 Cấu trúc Project

```text
├── MovieApp/               # Thư mục gốc của dự án Android Client
│   ├── app/                # Module ứng dụng Android chính
│   │   ├── src/main/java/com/example/movieapp/
│   │   │   ├── Activities/ # Các Activity của ứng dụng
│   │   │   ├── Adapters/   # Các Adapter cho RecyclerView
│   │   │   ├── Dataclass/  # Các lớp dữ liệu (Movie, Actor, etc.)
│   │   │   └── Fragment    # Các Fragment 
│   │   │   └── ...         # Các package khác             
│   │   ├── src/main/res/   # Tài nguyên UI (layout, drawable, values, etc.)
│   │   └── google-services.json # File cấu hình Firebase cho Android
│   ├── build.gradle        # File Gradle của module app
│   └── local.properties    # File chứa các biến môi trường cục bộ (API Keys, Backend URL)

cloudinary-backend/     # Thư mục gốc của dự án Node.js Server Backend, chứa toàn bộ mã nguồn backend.
├── node_modules/       # Thư viện và dependencies Node.js
├── public/             # Các tệp tĩnh (HTML, CSS, JS,...)
├── index.js            # File chính khởi tạo Express App
├── .env                # Cấu hình biến môi trường
├── package.json        # File manifest của Node.js
├── package-lock.json   # Khóa phiên bản dependencies
└── Procfile            # File khai báo tiến trình khi deploy (Railway)



## Demo
<h3>📸 Demo giao diện ứng dụng MovieApp</h3>

<p align="center">
  <img src="https://github.com/user-attachments/assets/3ce27e9b-3aa4-474c-be27-128da8832337" alt="Screenshot 1" width="270"/>
  <img src="https://github.com/user-attachments/assets/359eb09d-eff4-4dcf-8c1b-f153e87a9268" alt="Screenshot 2" width="270"/>
  <img src="https://github.com/user-attachments/assets/41ee2a19-565a-4db4-8428-1b2bde91efa6" alt="Screenshot 3" width="270"/>
</p>

<p align="center">
  <img src="https://github.com/user-attachments/assets/a216719b-1ab9-452d-b446-b795df82d45d" alt="Screenshot 4" width="270"/>
  <img src="https://github.com/user-attachments/assets/3c7933cc-efc0-4658-97e2-b81dfce7f245" alt="Screenshot 5" width="270"/>
  <img src="https://github.com/user-attachments/assets/bc6146d9-bfe6-413e-902f-c214e76f4e41" alt="Screenshot 6" width="270"/>
</p>

<p align="center">
  <img src="https://github.com/user-attachments/assets/d633623f-586a-4d6f-8825-df6a707cbbdc" alt="Screenshot 7" width="270"/>
  <img src="https://github.com/user-attachments/assets/8dcdc81e-154e-4b5b-90df-c416d0f25b9b" alt="Screenshot 8" width="270"/>
  <img src="https://github.com/user-attachments/assets/f5f6d3a9-7247-4bc4-82dd-f96dd7bc9c69" alt="Screenshot 9" width="270"/>
</p>

<p align="center">
  <img src="https://github.com/user-attachments/assets/33ccb21b-39e7-43db-b5b3-ea48f1da7a73" alt="Screenshot 10" width="270"/>
  <img src="https://github.com/user-attachments/assets/336f9db8-5fc7-4764-b47a-3547d87ada92" alt="Screenshot 11" width="270"/>
</p>



