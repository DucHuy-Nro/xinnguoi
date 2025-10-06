# 📚 INDEX TÀI LIỆU - NGỌC RỒNG ONLINE

## 🎯 BẠN MUỐN LÀM GÌ?

### 🎭 THÊM NPC MỚI
- **Mới bắt đầu?** → Đọc [`HUONG_DAN_THEM_NPC_CHI_TIET.md`](HUONG_DAN_THEM_NPC_CHI_TIET.md)
- **Muốn code nhanh?** → Copy [`TEMPLATE_NPC_MOI.java`](TEMPLATE_NPC_MOI.java)
- **Thêm vào database?** → Copy [`SQL_TEMPLATE_NPC.sql`](SQL_TEMPLATE_NPC.sql)
- **Check từng bước?** → Dùng [`CHECKLIST_THEM_NPC.md`](CHECKLIST_THEM_NPC.md)

### 🗺️ THÊM MAP MỚI
- **Tổng quan hệ thống** → [`HUONG_DAN_GAME_LOGIC.md`](HUONG_DAN_GAME_LOGIC.md) - Phần "Hệ thống Map"

### 💪 THAY ĐỔI TIỀM NĂNG/SỨC MẠNH
- **Sửa công thức** → [`HUONG_DAN_GAME_LOGIC.md`](HUONG_DAN_GAME_LOGIC.md) - Phần "Tiềm năng sức mạnh"

### 🚀 DEPLOY SERVER LÊN VPS
- **Hướng dẫn deploy** → [`HUONG_DAN_GAME_LOGIC.md`](HUONG_DAN_GAME_LOGIC.md) - Phần "Deploy VPS"

### ⚡ NETTY - GIẢM THREADS
- **Phân tích performance** → [`PHAN_TICH_PERFORMANCE_VA_NETTY.md`](PHAN_TICH_PERFORMANCE_VA_NETTY.md)
- **Cài đặt Netty** → [`HUONG_DAN_CAI_DAT_NETTY.md`](HUONG_DAN_CAI_DAT_NETTY.md)
- **So sánh Netty vs Old** → [`SO_SANH_NETTY_VS_OLD.md`](SO_SANH_NETTY_VS_OLD.md)
- **Tóm tắt migration** → [`NETTY_MIGRATION_SUMMARY.md`](NETTY_MIGRATION_SUMMARY.md)

---

## 📖 TÀI LIỆU CHI TIẾT

### 1. NPC SYSTEM

#### 📘 HUONG_DAN_THEM_NPC_CHI_TIET.md
**Nội dung:**
- ✅ 8 bước chi tiết từ Database → Code → Test
- ✅ Ví dụ đầy đủ: NPC "Thầy Rùng"
- ✅ Thêm shop, đổi ngọc, nhiệm vụ
- ✅ Tùy chỉnh trang phục
- ✅ Spawn NPC vào map
- ✅ Troubleshooting

**Đọc khi:**
- Lần đầu thêm NPC
- Muốn hiểu chi tiết từng bước
- Gặp lỗi cần fix

---

#### 📗 TEMPLATE_NPC_MOI.java
**Nội dung:**
- ✅ Code mẫu hoàn chỉnh
- ✅ Đầy đủ comments tiếng Việt
- ✅ Các chức năng thông dụng:
  - Shop
  - Đổi vật phẩm
  - Nhận quà random
  - Nhiệm vụ
  - Teleport
  - Buff player
  - Utility methods

**Dùng khi:**
- Muốn code nhanh
- Copy-paste và sửa
- Cần template chuẩn

---

#### 📙 SQL_TEMPLATE_NPC.sql
**Nội dung:**
- ✅ SQL mẫu cho NPC template
- ✅ Tạo shop đầy đủ
- ✅ Thêm items vào shop
- ✅ Query kiểm tra
- ✅ Bảng tham khảo Item IDs
- ✅ Commands xóa (nếu sai)

**Dùng khi:**
- Cần thêm NPC vào database
- Tạo shop cho NPC
- Tham khảo Item IDs

---

#### 📕 CHECKLIST_THEM_NPC.md
**Nội dung:**
- ✅ Checklist từng bước
- ✅ Troubleshooting nhanh
- ✅ Quick commands
- ✅ Item IDs thông dụng
- ✅ Maps thông dụng

**Dùng khi:**
- Đang thực hiện thêm NPC
- Cần check từng bước
- Debug lỗi nhanh

---

### 2. GAME LOGIC

#### 📘 HUONG_DAN_GAME_LOGIC.md
**Nội dung:**
- ✅ **Hệ thống NPC**: Cấu trúc, cách thêm
- ✅ **Hệ thống Map**: Thêm map, spawn mobs, waypoints
- ✅ **Tiềm năng sức mạnh**: Sửa công thức, thay đổi stats
- ✅ **Deploy VPS**: Windows Server, từ A-Z
- ✅ **Shop System**: Thêm items, config
- ✅ **Quest/Reward**: Sửa nhiệm vụ, phần thưởng

**Đọc khi:**
- Cần hiểu tổng quan game
- Muốn sửa game logic
- Deploy server

---

### 3. NETTY OPTIMIZATION

#### 📘 PHAN_TICH_PERFORMANCE_VA_NETTY.md
**Đã có sẵn** - Phân tích chi tiết performance

#### 📗 HUONG_DAN_CAI_DAT_NETTY.md
**Đã có sẵn** - Hướng dẫn cài đặt Netty

#### 📙 SO_SANH_NETTY_VS_OLD.md
**Đã có sẵn** - Benchmark so sánh

#### 📕 NETTY_MIGRATION_SUMMARY.md
**Đã có sẵn** - Tóm tắt migration

---

## 🎯 LỘ TRÌNH HỌC TẬP

### Người mới (chưa biết code)
1. Đọc [`TOM_TAT_CHO_NGUOI_KHONG_BIET_CODE.md`](TOM_TAT_CHO_NGUOI_KHONG_BIET_CODE.md)
2. Thực hành theo [`QUICK_START_3_BUOC.txt`](QUICK_START_3_BUOC.txt)
3. Thử thêm NPC theo [`HUONG_DAN_THEM_NPC_CHI_TIET.md`](HUONG_DAN_THEM_NPC_CHI_TIET.md)

### Người đã biết code Java
1. Đọc [`HUONG_DAN_GAME_LOGIC.md`](HUONG_DAN_GAME_LOGIC.md) để hiểu cấu trúc
2. Thực hành thêm NPC với [`TEMPLATE_NPC_MOI.java`](TEMPLATE_NPC_MOI.java)
3. Tối ưu server với Netty docs

### Người muốn deploy
1. Chuẩn bị VPS theo [`HUONG_DAN_GAME_LOGIC.md`](HUONG_DAN_GAME_LOGIC.md) - Phần Deploy
2. Config database
3. Test và monitor

---

## 🔍 TÌM NHANH

### Thêm NPC:
```
Database    → SQL_TEMPLATE_NPC.sql
Code        → TEMPLATE_NPC_MOI.java
Hướng dẫn   → HUONG_DAN_THEM_NPC_CHI_TIET.md
Checklist   → CHECKLIST_THEM_NPC.md
```

### Thêm Map:
```
Hướng dẫn   → HUONG_DAN_GAME_LOGIC.md (Section: Hệ thống Map)
```

### Sửa Stats:
```
File code   → src/nro/models/player/NPoint.java
Hướng dẫn   → HUONG_DAN_GAME_LOGIC.md (Section: TNSM)
```

### Deploy:
```
Hướng dẫn   → HUONG_DAN_GAME_LOGIC.md (Section: Deploy VPS)
```

---

## 📁 CẤU TRÚC THỨ MỤC

```
/workspace/
│
├── 📄 INDEX_TAI_LIEU.md (FILE NÀY)
│
├── 🎭 NPC SYSTEM
│   ├── HUONG_DAN_THEM_NPC_CHI_TIET.md
│   ├── TEMPLATE_NPC_MOI.java
│   ├── SQL_TEMPLATE_NPC.sql
│   └── CHECKLIST_THEM_NPC.md
│
├── 🎮 GAME LOGIC
│   └── HUONG_DAN_GAME_LOGIC.md
│
├── ⚡ NETTY (Performance)
│   ├── PHAN_TICH_PERFORMANCE_VA_NETTY.md
│   ├── HUONG_DAN_CAI_DAT_NETTY.md
│   ├── SO_SANH_NETTY_VS_OLD.md
│   └── NETTY_MIGRATION_SUMMARY.md
│
├── 📚 CHO NGƯỜI MỚI
│   ├── TOM_TAT_CHO_NGUOI_KHONG_BIET_CODE.md
│   ├── QUICK_START_3_BUOC.txt
│   └── BAT_DAU_TU_DAY.md
│
└── 📊 KẾT LUẬN
    └── KET_LUAN_VA_GIAI_PHAP.md
```

---

## ❓ FAQ - CÂU HỎI THƯỜNG GẶP

### Q: Tôi muốn thêm NPC, bắt đầu từ đâu?
**A:** Đọc [`HUONG_DAN_THEM_NPC_CHI_TIET.md`](HUONG_DAN_THEM_NPC_CHI_TIET.md), làm theo 8 bước.

### Q: Có file code mẫu để copy không?
**A:** Có! File [`TEMPLATE_NPC_MOI.java`](TEMPLATE_NPC_MOI.java) có đầy đủ code.

### Q: Làm sao thêm items vào shop?
**A:** Dùng SQL trong [`SQL_TEMPLATE_NPC.sql`](SQL_TEMPLATE_NPC.sql), section "Thêm items".

### Q: NPC không xuất hiện, fix thế nào?
**A:** Check [`CHECKLIST_THEM_NPC.md`](CHECKLIST_THEM_NPC.md) - Section "Troubleshooting".

### Q: Muốn sửa sức mạnh/tiềm năng?
**A:** Đọc [`HUONG_DAN_GAME_LOGIC.md`](HUONG_DAN_GAME_LOGIC.md) - Section "TNSM".

### Q: Deploy server lên VPS?
**A:** [`HUONG_DAN_GAME_LOGIC.md`](HUONG_DAN_GAME_LOGIC.md) - Section "Deploy VPS".

### Q: Server bị lag/thread cao?
**A:** Đọc Netty docs, bắt đầu với [`PHAN_TICH_PERFORMANCE_VA_NETTY.md`](PHAN_TICH_PERFORMANCE_VA_NETTY.md).

---

## 🎓 HỌC THÊM

### Source Code Structure:
```
src/nro/models/
├── npc/              ← NPC system
├── npc_list/         ← Các NPC cụ thể
├── map/              ← Map system
├── player/           ← Player, stats
├── shop/             ← Shop system
├── services/         ← Game services
└── network/          ← Network (Netty)
```

### Key Files:
- **NPoint.java**: Stats, TNSM
- **Manager.java**: Load game data
- **Controller.java**: Message handler
- **NpcFactory.java**: Tạo NPCs
- **ShopService.java**: Shop logic

---

## 📞 SUPPORT

**Gặp vấn đề?**
1. Check file tương ứng trong index này
2. Đọc phần Troubleshooting
3. Check console log
4. Hỏi tôi!

---

## ⭐ TIPS PRO

### 1. Sao lưu trước khi sửa
```bash
# Backup database
mysqldump -u root -p ngocrong > backup_$(date +%Y%m%d).sql

# Backup code
zip -r source_backup_$(date +%Y%m%d).zip src/
```

### 2. Test trên server test trước
- Đừng sửa thẳng server chính
- Test kỹ trước khi deploy

### 3. Đọc code NPCs có sẵn
- `Bulma.java` - Shop đơn giản
- `QuyLaoKame.java` - Nhiệm vụ phức tạp
- `BaHatMit.java` - Chế tạo

### 4. Comment code của bạn
```java
/**
 * Đổi 10 ngọc đỏ = 1 ngọc xanh
 * @param player Player thực hiện
 */
private void doiNgocXanh(Player player) {
    // ...
}
```

---

**📚 TÀI LIỆU HOÀN CHỈNH - SẴN SÀNG SỬ DỤNG!**

**🎉 CHÚC BẠN THÀNH CÔNG!**
