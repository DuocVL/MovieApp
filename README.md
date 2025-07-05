Dưới đây là phiên bản **README.md** chi tiết và chuyên nghiệp hơn cho dự án **MovieApp**, bao gồm phiên bản công cụ, cấu trúc thư mục và mô tả chức năng từng module – bạn có thể dán vào file `README.md` trong repo của mình:

---

```markdown
# 🎬 MovieApp – Ứng dụng xem phim trên Android

MovieApp là ứng dụng xem phim trực tuyến/offline, được phát triển bằng Kotlin trên Android. Tích hợp TMDB API, Bunny Stream, PayOS & Firebase, được triển khai backend trên Railway.

---

## 🛠 Công nghệ & Phiên bản sử dụng

| Thành phần           | Công nghệ / Thư viện                        | Phiên bản                   |
|----------------------|---------------------------------------------|-----------------------------|
| Ngôn ngữ chính        | Kotlin                                      | 1.8+                        |
| Android SDK          | Minimum/Target SDK                          | 21 / 33                     |
| IDE                  | Android Studio                              | Flamingo (2022.2.1)         |
| UI                   | ViewBinding, Jetpack MVVM, RecyclerView     | AndroidX Components         |
| Video Player         | ExoPlayer                                   | 2.18+                       |
| TMDB API             | TMDB v3/v4                                  |                             |
| Network              | Retrofit + OkHttp + Moshi                   | Retrofit 2.9, Moshi 1.14    |
| Tuyền tải dữ liệu     | Coroutines, LiveData                        | KotlinX Coroutines 1.7+     |
| CSDL Client          | Firebase Firestore                          | Google Firebase SDKs        |
| Xác thực             | Firebase Authentication                     |                             |
| Backend              | Node.js + Express.js                        | Node.js 18+                 |
| Thanh toán           | PayOS QR payment API                        |                             |
| Streaming            | Bunny Stream                                | CDN                         |
| Triển khai backend   | Railway                                     |                             |

---

## 📁 Cấu trúc thư mục

```
root/
├── app/                        # Android client
│   ├── src/
│   │   ├── Activities/         # MainActivity, Detail, Player,...
│   │   ├── Fragments/          # Home, Search, WatchLater, Auth...
│   │   ├── Adapters/           # RecyclerView adapters
│   │   ├── Models/             # Movie, User, Comment, Rating
│   │   ├── Network/            # TMDB, Backend, PayOS API
│   │   ├── Utils/              # ExoPlayer helper, Extensions...
│   │   └── Constants.kt        # API keys, endpoint URLs
├── server/                     # Node.js backend
│   ├── routes/
│   │   ├── payment.js          # QR generation, webhook
│   │   ├── comment.js          # Coment API routes
│   │   ├── rating.js           # Rating API
│   └── index.js                # Server startup, middleware setup
├── demo/                       # Hình/Video demo
└── README.md                   # (This file)
```

---

## 🔍 Mô tả từng module

### 1. Auth (Đăng nhập / Guest)
- Cho phép đăng ký/đăng nhập bằng email/password qua Firebase Auth.
- Hỗ trợ “khách” (guest), hạn chế tương tác: chỉ xem những phim miễn phí.
- Firebase duy trì session, phân quyền toàn diện.

### 2. HomeFragment (Trang chủ)
- Lấy danh sách phim “popular”, “top rated”, “upcoming” từ TMDB API.
- Banner nổi bật (Admin chọn trước trên Firebase).
- Hiển thị dưới dạng RecyclerView, tích hợp điều hướng đến chi tiết phim.

### 3. SearchFragment (Tìm kiếm)
- Gọi endpoint TMDB `/search/movie`, phản hồi gợi ý theo từng kí tự nhập.
- Hiển thị realtime, click chuyển sang chi tiết phim.

### 4. MovieDetailActivity (Chi tiết phim)
- Gọi TMDB API (`/movie/{id}`) lấy chi tiết phim.
- Lấy trailer qua `/movie/{id}/videos`, phát trailer nếu có.
- Hiển thị đánh giá, bình luận, nút mua phim, lưu xem sau.
- Gợi ý phim liên quan qua `/movie/{id}/similar`.

### 5. PlayerActivity (Xem phim)
- Nếu phim đã mua:
  - Chọn phát online từ Bunny Stream qua ExoPlayer.
  - Hoặc phát offline nếu đã tải về thiết bị.
  - Lưu tiến trình xem (timestamp) vào Firestore.
- Nếu là khách hoặc chưa mua → chuyển hướng hiển thị thông báo.

### 6. Download Manager (Tải phim)
- Cho phép tải video về bộ nhớ trong sau khi xác thực mua.
- Theo dõi trạng thái: đang tải, đã tải, lưu vị trí xem offline.

### 7. Watch Later (Danh sách xem sau)
- Lưu phim theo người dùng vào Firebase (collection `watchLater/{userId}`).
- Fragment truy vấn và hiển thị danh sách để xem hoặc xóa.

### 8. Bình luận & Đánh giá
- Người dùng đã login có thể:
  - Đánh giá phim (1–5 sao). Firestore lưu collection `ratings/{movieId}`.
  - Viết bình luận với nội dung, emoji. Lưu tại collection `comments/{movieId}`.

### 9. Backend Payment (Node.js trên Railway)
- Route `POST /create-payment`: gọi PayOS API tạo QR code.
- Route `POST /webhook`: PayOS webhook gửi trạng thái giao dịch, backend kiểm tra signature, cập nhật quyền mua phim trên Firestore.
- Dùng Firebase Admin SDK để ghi dữ liệu giao dịch, đánh giá, bình luận.

---

## 🚀 Cài đặt & chạy

### Android client

```bash
git clone https://github.com/DuocVL/MovieApp.git
cd MovieApp
```

1. Mở bằng Android Studio Flamingo.
2. Copy `google-services.json` vào `app/`.
3. Thiết lập API key:
   - `Constants.kt`: `TMDB_API_KEY=…`, `PAYOS_CLIENT_ID`, `PAYOS_API_KEY`.
4. Chạy app trên emulator hoặc thiết bị Android.

### Backend (Node.js)

```bash
cd server
npm install
```

Tạo file `.env`:

```env
PAYOS_CLIENT_ID=...
PAYOS_API_KEY=...
FIREBASE_ADMIN_SDK_JSON=...  # JSON từ Firebase project
```

Khởi động:

```bash
node index.js
```

- Hoặc deploy trên Railway – kết nối repo Github, CI/CD tự động.

---

## ✅ Tính năng đã hoàn thành

- ✅ Đăng ký/đăng nhập email-password, guest view.
- ✅ Xem phim online/offline, tải phim.
- ✅ Thanh toán QR qua PayOS và xử lý webhook.
- ✅ Đánh giá sao & bình luận, lưu "xem sau".
- ✅ Gợi ý phim liên quan, tìm kiếm realtime.
- ✅ Backend bảo mật, triển khai trên Railway.

---

## 📌 Hướng phát triển

- Thêm login bằng Google/Facebook.
- Hệ thống gợi ý cá nhân hóa dựa trên lịch sử xem/đánh giá.
- Thông báo FCM khi phim mới hoặc khuyến mãi.
- Tối ưu bộ nhớ khi tải phim (compress, cảnh báo).

---

## 📬 Liên hệ

- **Tác giả:** Lê Văn Được – MSSV: 20225296  
- GitHub: https://github.com/DuocVL  
- Giảng viên hướng dẫn: TS. Đỗ Công Thuần

---

_Cảm ơn bạn đã ghé thăm repo!_ 🚀
```

---

👉 Bạn chỉ cần **tạo file `README.md` trong repo** và dán nội dung trên vào. Nó sẽ giúp người xem dễ hiểu, chuyên nghiệp và đầy đủ. Nếu bạn cần hỗ trợ chỉnh sửa thêm hoặc xuất bản bản PDF/Word, cứ nhắn nhé!
