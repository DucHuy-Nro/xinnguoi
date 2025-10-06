# 🎭 HƯỚNG DẪN THÊM NPC CHI TIẾT (A-Z)

## 📋 VÍ DỤ: THÊM NPC "THẦY RÙNG" VÀO GAME

**Mục tiêu:**
- ✅ Tạo NPC tên "Thầy Rùng"
- ✅ Thêm shop bán đồ
- ✅ Thêm chức năng đổi ngọc
- ✅ Trang phục: Áo đen, quần trắng, đầu 100
- ✅ Đặt ở Map 5 (Làng Aru)

---

# 🎯 BƯỚC 1: THÊM VÀO DATABASE

## 1.1. Thêm NPC Template

**Mở MySQL Workbench/Command Line:**

```sql
USE ngocrong;

-- Thêm NPC template (ID 100)
INSERT INTO `npc_template` VALUES (
    100,                    -- ID (chọn ID chưa dùng)
    'Thầy Rùng',           -- Tên NPC
    400,                    -- head (ID đầu)
    401,                    -- body (ID áo)
    402,                    -- leg (ID quần)
    4000                    -- avatar (ID avatar)
);
```

**🔍 Giải thích các trường:**

- **id**: ID duy nhất của NPC (100)
- **NAME**: Tên hiển thị
- **head**: ID sprite đầu (400 = đầu số 400 trong game data)
- **body**: ID sprite áo (401)
- **leg**: ID sprite quần (402)
- **avatar**: ID avatar hiển thị khi chat (4000)

**💡 Tip:**
- Xem các NPC khác để chọn head/body/leg phù hợp
- Ví dụ: Bulma có head=42, body=43, leg=44

---

## 1.2. Tạo Shop cho NPC

```sql
-- Tạo shop (ID shop = 100)
INSERT INTO `shop` VALUES (
    100,                    -- shop_id
    100,                    -- npc_id (trùng với npc_template.id)
    'THAY_RUNG',           -- tag_name (tên gọi trong code)
    0                       -- type_shop (0=thường, 1=kỹ năng, 3=đặc biệt)
);

-- Tạo tab shop
INSERT INTO `shop_tab` VALUES (
    100,                    -- tab_id
    100,                    -- shop_id
    'Đồ Trang Bị',         -- tab_name
    0                       -- tab_index
);

-- Thêm items vào shop
-- Item 1: Áo giáp Thần (ID 12)
INSERT INTO `shop_item` VALUES (
    NULL,                   -- shop_item_id (auto increment)
    100,                    -- tab_id
    12,                     -- item_template_id (Áo giáp Thần)
    50000000,              -- gold_price (50 triệu vàng)
    0,                      -- gem_price (0 ngọc)
    80000000000,           -- power_required (cần 80 tỷ SM)
    10000                   -- quantity (-1 = vô hạn, >0 = giới hạn)
);

-- Item 2: Găng tay (ID 136)
INSERT INTO `shop_item` VALUES (
    NULL,
    100,
    136,                    -- Găng tay
    100000000,             -- 100tr vàng
    0,
    0,                      -- Không cần SM
    -1                      -- Vô hạn
);

-- Item 3: Ngọc xanh (ID 457) - đổi bằng ngọc đỏ
INSERT INTO `shop_item` VALUES (
    NULL,
    100,
    457,                    -- Ngọc xanh
    0,                      -- Không bán bằng vàng
    100,                    -- 100 ngọc đỏ
    0,
    -1
);
```

**💡 Các loại shop:**
- `type_shop = 0`: Shop thường (bán bằng vàng/ngọc)
- `type_shop = 1`: Shop kỹ năng
- `type_shop = 3`: Shop đặc biệt

---

# 🎯 BƯỚC 2: THÊM CONST NPC

**File:** `src/nro/models/consts/ConstNpc.java`

```java
public class ConstNpc {
    // ... existing NPCs ...
    public static final byte CHI_CHI = 81;
    public static final byte RUONG_SUU_TAM = 82;
    public static final byte DR_MYUU = 83;
    
    // ⭐ THÊM NPC MỚI ⭐
    public static final byte THAY_RUNG = 100;
    
    // ... menu indexes ...
    
    // ⭐ THÊM MENU INDEX CHO NPC MỚI ⭐
    public static final int THAY_RUNG_DOI_NGOC = 10001;
    public static final int THAY_RUNG_CONFIRM_DOI = 10002;
}
```

---

# 🎯 BƯỚC 3: TẠO CLASS NPC

**File:** `src/nro/models/npc_list/ThayRung.java`

```java
package nro.models.npc_list;

import nro.models.consts.ConstNpc;
import nro.models.npc.Npc;
import nro.models.player.Player;
import nro.models.services.Service;
import nro.models.services.InventoryService;
import nro.models.shop.ShopService;
import nro.models.item.Item;

public class ThayRung extends Npc {

    public ThayRung(int mapId, int status, int cx, int cy, int tempId, int avatar) {
        super(mapId, status, cx, cy, tempId, avatar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            // Menu chính
            createOtherMenu(player, ConstNpc.BASE_MENU,
                "Xin chào " + player.name + "!\nTa là Thầy Rùng, người bảo vệ làng này\n"
                + "Ta có thể giúp gì cho ngươi?",
                "Shop\nTrang Bị",
                "Đổi\nNgọc",
                "Từ chối"
            );
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            // Xử lý menu chính
            if (player.idMark.isBaseMenu()) {
                switch (select) {
                    case 0: // Shop
                        ShopService.gI().opendShop(player, "THAY_RUNG", true);
                        break;
                        
                    case 1: // Đổi ngọc
                        createOtherMenu(player, ConstNpc.THAY_RUNG_DOI_NGOC,
                            "Ta có thể đổi cho ngươi:\n"
                            + "- 10 Ngọc Đỏ = 1 Ngọc Xanh\n"
                            + "- 10 Ngọc Xanh = 1 Hồng Ngọc\n"
                            + "Ngươi muốn đổi gì?",
                            "Đổi Ngọc Xanh",
                            "Đổi Hồng Ngọc",
                            "Từ chối"
                        );
                        break;
                }
            }
            // Xử lý menu đổi ngọc
            else if (player.idMark.getIndexMenu() == ConstNpc.THAY_RUNG_DOI_NGOC) {
                switch (select) {
                    case 0: // Đổi ngọc xanh
                        doiNgocXanh(player);
                        break;
                        
                    case 1: // Đổi hồng ngọc
                        doiHongNgoc(player);
                        break;
                }
            }
        }
    }
    
    /**
     * Đổi 10 ngọc đỏ = 1 ngọc xanh
     */
    private void doiNgocXanh(Player player) {
        if (player.inventory.ruby >= 10) {
            player.inventory.ruby -= 10;
            player.inventory.gem += 1;
            Service.gI().sendMoney(player);
            Service.gI().sendThongBao(player, "Đổi thành công! +1 Ngọc Xanh");
        } else {
            Service.gI().sendThongBao(player, "Bạn không đủ 10 Ngọc Đỏ!");
        }
    }
    
    /**
     * Đổi 10 ngọc xanh = 1 hồng ngọc
     */
    private void doiHongNgoc(Player player) {
        // Tìm hồng ngọc trong túi đồ
        Item hongNgoc = InventoryService.gI().findItemBag(player, 861); // ID 861 = Hồng Ngọc
        
        if (player.inventory.gem >= 10) {
            player.inventory.gem -= 10;
            
            if (hongNgoc == null) {
                // Tạo mới nếu chưa có
                hongNgoc = InventoryService.gI().createItemNull();
                hongNgoc.template = nro.models.server.Manager.ITEM_TEMPLATES.get(861);
                hongNgoc.quantity = 1;
                InventoryService.gI().addItemBag(player, hongNgoc);
            } else {
                // Tăng số lượng
                hongNgoc.quantity += 1;
            }
            
            InventoryService.gI().sendItemBags(player);
            Service.gI().sendMoney(player);
            Service.gI().sendThongBao(player, "Đổi thành công! +1 Hồng Ngọc");
        } else {
            Service.gI().sendThongBao(player, "Bạn không đủ 10 Ngọc Xanh!");
        }
    }
}
```

**🔍 Các method quan trọng:**

- **`openBaseMenu()`**: Hiển thị menu đầu tiên
- **`confirmMenu()`**: Xử lý khi player chọn menu
- **`createOtherMenu()`**: Tạo menu con
- **`canOpenNpc()`**: Check xem player có thể mở NPC không

---

# 🎯 BƯỚC 4: REGISTER NPC VÀO FACTORY

**File:** `src/nro/models/npc/NpcFactory.java`

## 4.1. Import class

```java
package nro.models.npc;

// ... existing imports ...
import nro.models.npc_list.ThayRung;  // ⭐ THÊM IMPORT
```

## 4.2. Thêm vào switch-case

**Tìm method `createNPC()` (dòng ~109):**

```java
public static Npc createNPC(int mapId, int status, int cx, int cy, int tempId) {
    int avatar = Manager.NPC_TEMPLATES.get(tempId).avatar;
    try {
        return switch (tempId) {
            // ... existing cases ...
            case ConstNpc.DR_MYUU -> 
                new DrMyuu(mapId, status, cx, cy, tempId, avatar);
            
            // ⭐ THÊM CASE MỚI ⭐
            case ConstNpc.THAY_RUNG -> 
                new ThayRung(mapId, status, cx, cy, tempId, avatar);
            
            default -> new Npc(mapId, status, cx, cy, tempId, avatar) {
                @Override
                public void confirmMenu(Player pl, int select) {
                }
            };
        };
    } catch (Exception e) {
        e.printStackTrace();
    }
    return null;
}
```

---

# 🎯 BƯỚC 5: THÊM NPC VÀO MAP

## Cách 1: Hardcode trong Manager.java (DỄ)

**File:** `src/nro/models/server/Manager.java`

**Tìm method `loadAllMap()` (dòng ~300+):**

```java
private void loadAllMap() {
    // ... code load maps ...
    
    // Sau khi load xong tất cả maps
    // Thêm NPC vào Map 5 (Làng Aru)
    nro.models.map.Map mapLangAru = getMapById(5);
    if (mapLangAru != null) {
        // ⭐ SPAWN NPC "THẦY RÙNG" ⭐
        Npc thayRung = NpcFactory.createNPC(
            5,              // mapId (Làng Aru)
            0,              // status
            520,            // tọa độ x
            336,            // tọa độ y
            ConstNpc.THAY_RUNG,  // tempId
            Manager.NPC_TEMPLATES.get(100).avatar  // avatar
        );
        mapLangAru.npcs.add(thayRung);
        Logger.success("✅ Spawned NPC: Thầy Rùng at Map 5");
    }
}

private nro.models.map.Map getMapById(int mapId) {
    for (nro.models.map.Map map : MAPS) {
        if (map.mapId == mapId) {
            return map;
        }
    }
    return null;
}
```

---

## Cách 2: Từ file Map Data (KHUYẾN NGHỊ)

**Nếu game load NPCs từ file data:**

**File:** `data/map/npc_map.txt` (hoặc tương tự)

```
# Format: mapId|npcTempId|x|y|status
5|100|520|336|0
```

**Code load:**

```java
// Trong Manager.java
private void loadNpcFromFile() {
    try {
        DataInputStream dis = new DataInputStream(
            new FileInputStream("data/map/npc_map.txt")
        );
        
        int npcCount = dis.readShort();
        for (int i = 0; i < npcCount; i++) {
            int mapId = dis.readByte();
            int npcTempId = dis.readByte();
            int x = dis.readShort();
            int y = dis.readShort();
            int status = dis.readByte();
            
            nro.models.map.Map map = getMapById(mapId);
            if (map != null) {
                Npc npc = NpcFactory.createNPC(mapId, status, x, y, npcTempId);
                map.npcs.add(npc);
            }
        }
        dis.close();
    } catch (Exception e) {
        e.printStackTrace();
    }
}
```

---

# 🎯 BƯỚC 6: TÙY CHỈNH TRANG PHỤC

## 6.1. Chọn Head/Body/Leg

**Cách 1: Dùng ID có sẵn**

Xem database `npc_template`:

```sql
SELECT * FROM npc_template WHERE id < 20;
```

Chọn head/body/leg phù hợp:
- **Bulma**: head=42, body=43, leg=44
- **Quy Lão Kame**: head=33, body=34, leg=35
- **Ông Gohan**: head=18, body=19, leg=20

**Cách 2: Tạo sprite mới**

Nếu muốn trang phục riêng:

1. **Thiết kế sprite** (PNG 24x32 pixels)
2. **Thêm vào file data** `part_image.img`
3. **Cập nhật database:**

```sql
-- Giả sử sprite mới có ID 2000-2002
UPDATE npc_template 
SET head = 2000, body = 2001, leg = 2002
WHERE id = 100;
```

---

## 6.2. Chọn Avatar (khi chat)

**Avatar** = hình hiển thị khi NPC chat

Xem trong database:

```sql
SELECT id, NAME, avatar FROM npc_template LIMIT 20;
```

Hoặc chọn tùy chỉnh:
- Bulma: 562
- Quy Lão Kame: 564
- Ông Gohan: 349

```sql
UPDATE npc_template SET avatar = 564 WHERE id = 100;
```

---

# 🎯 BƯỚC 7: THÊM CHỨC NĂNG NÂNG CAO

## 7.1. Thêm nhiệm vụ

```java
@Override
public void confirmMenu(Player player, int select) {
    if (canOpenNpc(player)) {
        if (player.idMark.isBaseMenu()) {
            switch (select) {
                case 0: // Nhận nhiệm vụ
                    if (!player.playerTask.sideTask.isReceivedQuest) {
                        player.playerTask.sideTask.questId = 1; // ID quest
                        player.playerTask.sideTask.isReceivedQuest = true;
                        Service.gI().sendThongBao(player, 
                            "Nhiệm vụ: Đi đánh 10 con Ốc Mượn Hermit!");
                    } else {
                        Service.gI().sendThongBao(player, 
                            "Bạn đã nhận nhiệm vụ rồi!");
                    }
                    break;
            }
        }
    }
}
```

---

## 7.2. Thêm teleport

```java
case 2: // Đi đến map khác
    nro.models.map.service.ChangeMapService.gI().changeMapInYard(
        player,
        24,     // mapId đích
        0,      // zoneId
        200,    // x đích
        300     // y đích
    );
    break;
```

---

## 7.3. Buff player

```java
case 3: // Buff
    // Buff x2 exp trong 10 phút
    player.effectSkin.isTimx2 = true;
    player.effectSkin.lastTimeThoiMien = System.currentTimeMillis();
    Service.gI().sendThongBao(player, "Bạn được buff x2 EXP trong 10 phút!");
    break;
```

---

## 7.4. Tặng item

```java
case 4: // Tặng quà
    Item item = InventoryService.gI().createItemNull();
    item.template = Manager.ITEM_TEMPLATES.get(457); // Ngọc xanh
    item.quantity = 10;
    
    if (InventoryService.gI().addItemBag(player, item)) {
        InventoryService.gI().sendItemBags(player);
        Service.gI().sendThongBao(player, "Bạn nhận được 10 Ngọc Xanh!");
    } else {
        Service.gI().sendThongBao(player, "Túi đồ đầy!");
    }
    break;
```

---

# 🎯 BƯỚC 8: COMPILE VÀ TEST

## 8.1. Build project

**NetBeans:**
```
F11 hoặc Run → Build Project
```

**Ant:**
```bash
cd /path/to/project
ant clean
ant compile
ant jar
```

---

## 8.2. Restart server

```bat
:: Windows
taskkill /F /IM java.exe
start_server.bat

:: Hoặc Linux
pkill -9 java
./start_server.sh
```

---

## 8.3. Test trong game

1. **Login vào game**
2. **Đi đến Map 5** (Làng Aru)
3. **Tìm NPC "Thầy Rùng"** ở tọa độ (520, 336)
4. **Click vào NPC**
5. **Test các chức năng:**
   - ✅ Menu hiển thị đúng
   - ✅ Shop mở được
   - ✅ Đổi ngọc hoạt động
   - ✅ Trang phục hiển thị đúng

---

# 🐛 TROUBLESHOOTING

## Lỗi 1: NPC không xuất hiện

**Nguyên nhân:**
- Chưa restart server
- Map ID sai
- Tọa độ ngoài map

**Fix:**
```java
// Check log khi server start
Logger.success("✅ Spawned NPC: Thầy Rùng at Map " + mapId);

// Hoặc check trong game
for (Npc npc : Manager.NPCS) {
    if (npc.tempId == ConstNpc.THAY_RUNG) {
        System.out.println("NPC found at map: " + npc.mapId);
    }
}
```

---

## Lỗi 2: Click NPC không có gì xảy ra

**Nguyên nhân:**
- Chưa implement `openBaseMenu()`
- NPC ID không đúng
- Controller chưa handle

**Fix:**
```java
// Kiểm tra trong Controller.java
@Override
public void onMessage(ISession s, Message _msg) {
    // ...
    byte cmd = _msg.command;
    switch (cmd) {
        case -33: // Click NPC
            System.out.println("Clicked NPC ID: " + npcId);
            break;
    }
}
```

---

## Lỗi 3: Shop không mở

**Nguyên nhân:**
- Tag name không khớp
- Chưa load shop từ database

**Fix:**
```sql
-- Kiểm tra shop
SELECT * FROM shop WHERE tag_name = 'THAY_RUNG';

-- Kiểm tra items
SELECT si.* FROM shop_item si
JOIN shop_tab st ON si.tab_id = st.tab_id
WHERE st.shop_id = 100;
```

```java
// Check trong ShopService.java
private Shop getShop(String tagName) throws Exception {
    System.out.println("Loading shop: " + tagName);
    // ...
}
```

---

## Lỗi 4: Trang phục hiển thị sai

**Nguyên nhân:**
- Sprite ID không tồn tại
- Client chưa có sprite data

**Fix:**
```sql
-- Xem danh sách sprites
SELECT * FROM part WHERE type = 0 ORDER BY id; -- Head
SELECT * FROM part WHERE type = 1 ORDER BY id; -- Body
SELECT * FROM part WHERE type = 2 ORDER BY id; -- Leg
```

---

# 📊 CHECKLIST HOÀN CHỈNH

## ✅ Database:
- [x] Thêm `npc_template`
- [x] Tạo `shop`
- [x] Tạo `shop_tab`
- [x] Thêm `shop_item`

## ✅ Code:
- [x] Thêm const `ConstNpc.THAY_RUNG`
- [x] Tạo class `ThayRung.java`
- [x] Import trong `NpcFactory.java`
- [x] Thêm case trong `createNPC()`
- [x] Spawn NPC vào map

## ✅ Test:
- [x] Build project thành công
- [x] Server start không lỗi
- [x] NPC xuất hiện trong game
- [x] Menu hiển thị đúng
- [x] Shop hoạt động
- [x] Chức năng đổi ngọc OK

---

# 🎉 KẾT QUẢ MONG ĐỢI

**Vào game sẽ thấy:**
```
🧑 Thầy Rùng (Map 5 - Làng Aru)
└── Menu:
    ├── Shop Trang Bị
    │   ├── Áo giáp Thần: 50tr vàng
    │   ├── Găng tay: 100tr vàng
    │   └── Ngọc xanh: 100 ngọc đỏ
    ├── Đổi Ngọc
    │   ├── 10 Ngọc Đỏ → 1 Ngọc Xanh
    │   └── 10 Ngọc Xanh → 1 Hồng Ngọc
    └── Từ chối
```

---

# 💡 MẸO NÂNG CAO

## 1. Điều kiện mở NPC

```java
@Override
public void openBaseMenu(Player player) {
    // Chỉ mở nếu đủ level
    if (player.nPoint.power < 1000000) {
        createOtherMenu(player, ConstNpc.IGNORE_MENU,
            "Ngươi cần ít nhất 1 triệu sức mạnh!", "Đóng");
        return;
    }
    
    // Chỉ mở 1 lần/ngày
    long today = System.currentTimeMillis() / 86400000;
    if (player.playerTask.lastTimeOpenNpc == today) {
        createOtherMenu(player, ConstNpc.IGNORE_MENU,
            "Hôm nay ngươi đã đến rồi!", "Đóng");
        return;
    }
    
    // Menu bình thường
    openBaseMenu(player);
}
```

---

## 2. Random rewards

```java
private void giveRandomReward(Player player) {
    int random = Util.nextInt(1, 100);
    
    int itemId;
    int quantity;
    
    if (random <= 1) { // 1% - Hồng ngọc
        itemId = 861;
        quantity = 1;
    } else if (random <= 10) { // 9% - Ngọc xanh
        itemId = 457;
        quantity = 5;
    } else { // 90% - Vàng
        player.inventory.gold += 10000000;
        Service.gI().sendMoney(player);
        Service.gI().sendThongBao(player, "Bạn nhận được 10 triệu vàng!");
        return;
    }
    
    // Tạo item
    Item item = InventoryService.gI().createItemNull();
    item.template = Manager.ITEM_TEMPLATES.get(itemId);
    item.quantity = quantity;
    InventoryService.gI().addItemBag(player, item);
    InventoryService.gI().sendItemBags(player);
}
```

---

## 3. NPC di động

```java
public class ThayRung extends Npc implements Runnable {
    private boolean isMoving = false;
    
    public ThayRung(...) {
        super(...);
        new Thread(this, "NPC-ThayRung").start();
    }
    
    @Override
    public void run() {
        while (isMoving) {
            try {
                // Di chuyển random
                this.cx += Util.nextInt(-50, 50);
                this.cy += Util.nextInt(-10, 10);
                
                // Update vị trí cho client
                updatePosition();
                
                Thread.sleep(5000); // 5 giây di chuyển 1 lần
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
```

---

# 📚 TÀI LIỆU THAM KHẢO

**Các NPC mẫu để học:**
- `Bulma.java` - Shop cơ bản
- `QuyLaoKame.java` - Nhiệm vụ phức tạp
- `BaHatMit.java` - Chế tạo đồ
- `Rong1Sao.java` - Điều ước rồng

**Các service quan trọng:**
- `ShopService` - Mở shop
- `InventoryService` - Quản lý đồ
- `Service` - Gửi thông báo, update UI
- `ChangeMapService` - Di chuyển map

---

**🎊 CHÚC MỪNG! BẠN ĐÃ THÊM NPC THÀNH CÔNG!** 🎉

**Có thắc mắc? Cứ hỏi tôi!** 💪
