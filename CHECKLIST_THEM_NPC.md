# ✅ CHECKLIST THÊM NPC - CHECK NHANH

## 📝 TRƯỚC KHI BẮT ĐẦU

- [ ] Đã backup database
- [ ] Đã backup source code
- [ ] Đã chuẩn bị thông tin NPC:
  - [ ] Tên NPC: _______________
  - [ ] Map spawn: _______________
  - [ ] Tọa độ (x, y): _______________
  - [ ] Chức năng: _______________

---

## 1️⃣ DATABASE (MySQL)

### NPC Template
- [ ] Chọn ID NPC chưa dùng: `SELECT MAX(id) FROM npc_template;`
- [ ] Chọn head/body/leg: `SELECT id, name, head, body, leg FROM npc_template LIMIT 20;`
- [ ] Insert NPC template:
```sql
INSERT INTO npc_template VALUES (?, 'Tên NPC', head, body, leg, avatar);
```
- [ ] Kiểm tra: `SELECT * FROM npc_template WHERE id = ?;`

### Shop (nếu cần)
- [ ] Insert shop: `INSERT INTO shop VALUES (?, ?, 'TAG_NAME', 0);`
- [ ] Insert shop_tab: `INSERT INTO shop_tab VALUES (?, ?, 'Tab Name', 0);`
- [ ] Insert shop_item: `INSERT INTO shop_item VALUES (...);`
- [ ] Kiểm tra: `SELECT * FROM shop WHERE npc_id = ?;`

---

## 2️⃣ CODE JAVA

### ConstNpc.java
- [ ] File: `src/nro/models/consts/ConstNpc.java`
- [ ] Thêm const: `public static final byte TEN_NPC = 100;`
- [ ] Thêm menu index (nếu cần): `public static final int MENU_XXX = 10001;`

### Tạo NPC Class
- [ ] File: `src/nro/models/npc_list/TenNpc.java`
- [ ] Package: `package nro.models.npc_list;`
- [ ] Extends: `public class TenNpc extends Npc`
- [ ] Constructor: ✅
- [ ] Override `openBaseMenu()`: ✅
- [ ] Override `confirmMenu()`: ✅
- [ ] Implement các chức năng: ✅

### NpcFactory.java
- [ ] File: `src/nro/models/npc/NpcFactory.java`
- [ ] Import: `import nro.models.npc_list.TenNpc;`
- [ ] Thêm case trong `createNPC()`:
```java
case ConstNpc.TEN_NPC -> 
    new TenNpc(mapId, status, cx, cy, tempId, avatar);
```

### Spawn NPC vào Map
- [ ] File: `src/nro/models/server/Manager.java`
- [ ] Method: `loadAllMap()`
- [ ] Code spawn:
```java
Npc npc = NpcFactory.createNPC(mapId, status, x, y, ConstNpc.TEN_NPC);
map.npcs.add(npc);
```

---

## 3️⃣ BUILD & TEST

### Build
- [ ] NetBeans: F11 (Clean & Build)
- [ ] Hoặc Ant: `ant clean && ant compile && ant jar`
- [ ] Không có lỗi compile ✅

### Restart Server
- [ ] Stop server
- [ ] Start lại server
- [ ] Server start thành công ✅
- [ ] Không có exception trong log ✅

---

## 4️⃣ TEST TRONG GAME

### Test cơ bản
- [ ] Login vào game
- [ ] Đi đến map đã spawn NPC
- [ ] NPC xuất hiện đúng vị trí ✅
- [ ] Trang phục hiển thị đúng ✅
- [ ] Tên NPC đúng ✅

### Test chức năng
- [ ] Click vào NPC
- [ ] Menu hiển thị ✅
- [ ] Chọn option "Shop" → Shop mở ✅
- [ ] Chọn option khác → Hoạt động đúng ✅
- [ ] Đóng menu → OK ✅

### Test chi tiết
- [ ] Mua item từ shop → Thành công ✅
- [ ] Kiểm tra giá item → Đúng ✅
- [ ] Kiểm tra điều kiện (power, level) → Đúng ✅
- [ ] Test các chức năng đặc biệt → OK ✅

---

## 5️⃣ FINAL CHECK

### Performance
- [ ] FPS không drop
- [ ] Không lag khi đứng gần NPC
- [ ] Không có memory leak

### Security
- [ ] Không có bug duplicate item
- [ ] Không có bug duplicate ngọc/vàng
- [ ] Validate đầu vào đúng

### Log
- [ ] Không có exception trong console
- [ ] Không có warning bất thường
- [ ] Log spawn NPC đúng: `✅ Spawned NPC: Tên at Map X`

---

## 🐛 TROUBLESHOOTING CHECKLIST

### NPC không xuất hiện?
- [ ] Check database: `SELECT * FROM npc_template WHERE id = ?;`
- [ ] Check code spawn: `Logger.warning("Spawning NPC at map " + mapId);`
- [ ] Check map ID đúng: `System.out.println("Map " + mapId + " exists: " + (map != null));`
- [ ] Restart server lại

### Click NPC không có gì?
- [ ] Check `openBaseMenu()` đã implement
- [ ] Check `canOpenNpc()` return true
- [ ] Check log: `System.out.println("Opened NPC: " + tempId);`
- [ ] Check NPC ID trong Controller

### Shop không mở?
- [ ] Check tag_name trong database
- [ ] Check `ShopService.gI().opendShop(player, "TAG_NAME", true);`
- [ ] Check shop có items: `SELECT COUNT(*) FROM shop_item WHERE tab_id = ?;`
- [ ] Check log ShopService

### Trang phục sai?
- [ ] Check head/body/leg trong database
- [ ] Check sprite ID tồn tại: `SELECT * FROM part WHERE id = ?;`
- [ ] Thử đổi sang ID khác (VD: Bulma 42/43/44)

---

## 📋 ITEM IDs THÔNG DỤNG

**Ngọc:**
- 457: Ngọc xanh
- 861: Hồng ngọc

**Đồ cơ bản:**
- 0-11: Áo
- 6-17: Quần
- 21-32: Găng tay
- 27-38: Giày

**Vật phẩm:**
- 193: Đậu thần
- 194: Đậu Senzu
- 342: Capsule

---

## 📊 MAPS THÔNG DỤNG

- Map 0: Trường Võ Đài
- Map 5: Làng Aru
- Map 7: Rừng Bambo
- Map 14: Làng Kakarot
- Map 24: Thánh địa Karin

**Lấy danh sách maps:**
```java
for (nro.models.map.Map map : Manager.MAPS) {
    System.out.println("Map " + map.mapId + ": " + map.mapName);
}
```

---

## ⚡ QUICK COMMANDS

**MySQL:**
```sql
-- Xem tất cả NPCs
SELECT id, name FROM npc_template ORDER BY id;

-- Xem items trong shop
SELECT si.*, it.name 
FROM shop_item si 
JOIN item_template it ON si.item_template_id = it.id
WHERE tab_id = ?;

-- Xóa NPC
DELETE FROM npc_template WHERE id = ?;
```

**Java Debug:**
```java
// Trong openBaseMenu()
System.out.println("Player " + player.name + " opened NPC " + tempId);

// Trong confirmMenu()
System.out.println("Player selected option " + select);

// Check NPC spawned
System.out.println("Total NPCs: " + Manager.NPCS.size());
```

---

## 🎯 KẾT QUẢ MONG ĐỢI

✅ **Thành công khi:**
- NPC xuất hiện trong game
- Click vào NPC hiển thị menu
- Shop hoạt động (nếu có)
- Các chức năng đặc biệt chạy đúng
- Không có lỗi trong console
- Performance ổn định

❌ **Thất bại nếu:**
- NPC không xuất hiện
- Click NPC bị crash
- Shop không mở hoặc rỗng
- Chức năng không hoạt động
- Server bị lag/crash

---

**📞 CẦN HELP?**

Nếu stuck ở bước nào, check lại:
1. File `HUONG_DAN_THEM_NPC_CHI_TIET.md` - Hướng dẫn đầy đủ
2. File `TEMPLATE_NPC_MOI.java` - Code template
3. File `SQL_TEMPLATE_NPC.sql` - SQL template

Hoặc mở console và check log!

---

**🎉 HOÀN THÀNH! CHÚC MỪNG BẠN ĐÃ THÊM NPC THÀNH CÔNG!**
