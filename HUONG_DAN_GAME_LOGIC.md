# 🎮 HƯỚNG DẪN GAME LOGIC - NGỌC RỒNG ONLINE

## 📋 MỤC LỤC
1. [Hệ thống NPC](#hệ-thống-npc)
2. [Hệ thống Map](#hệ-thống-map)  
3. [Tiềm Năng Sức Mạnh (TNSM)](#tiềm-năng-sức-mạnh)
4. [Deploy VPS](#deploy-vps)

---

# 🎭 HỆ THỐNG NPC

## 📊 CẤU TRÚC NPC:

```
src/nro/models/npc/
├── Npc.java              - Base class
├── NpcFactory.java       - Tạo NPC
├── BaseMenu.java         - Menu NPC
└── IAtionNpc.java        - Interface

src/nro/models/npc_list/  - 61 NPCs cụ thể:
├── QuyLaoKame.java
├── Bulma.java
├── GokuSSJ.java
├── Rong1Sao.java - Rong7Sao.java
└── ...
```

---

## 🔧 CÁCH THÊM NPC MỚI:

### **Bước 1: Tạo class NPC**

**File:** `src/nro/models/npc_list/NpcCuaToi.java`

```java
package nro.models.npc_list;

import nro.models.npc.Npc;
import nro.models.player.Player;
import nro.models.services.Service;

public class NpcCuaToi extends Npc {
    
    public NpcCuaToi(int mapId, int status, int cx, int cy, int tempId, int avatar) {
        super(mapId, status, cx, cy, tempId, avatar);
    }
    
    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            // Menu chính
            createOtherMenu(player, 0, 
                "Xin chào " + player.name + "!", 
                "Shop", "Nhiệm vụ", "Từ chối"
            );
        }
    }
    
    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            switch (player.idMark.getIndexMenu()) {
                case 0: // Menu chính
                    switch (select) {
                        case 0: // Shop
                            nro.models.shop.ShopService.gI().openShop(player, "SHOP_NPC_CUA_TOI", true);
                            break;
                        case 1: // Nhiệm vụ
                            createOtherMenu(player, 1,
                                "Hãy đi đánh 10 con quái!",
                                "OK", "Không"
                            );
                            break;
                    }
                    break;
                    
                case 1: // Menu nhiệm vụ
                    if (select == 0) {
                        Service.gI().sendThongBao(player, "Nhiệm vụ đã nhận!");
                    }
                    break;
            }
        }
    }
}
```

---

### **Bước 2: Thêm vào ConstNpc.java**

**File:** `src/nro/models/consts/ConstNpc.java`

```java
public class ConstNpc {
    // ... existing NPCs ...
    public static final byte NPC_CUA_TOI = 100; // ID NPC mới
}
```

---

### **Bước 3: Register vào NpcFactory.java**

**File:** `src/nro/models/npc/NpcFactory.java` (dòng ~109)

```java
import nro.models.npc_list.NpcCuaToi;

public static Npc createNPC(int mapId, int status, int cx, int cy, int tempId) {
    int avatar = Manager.NPC_TEMPLATES.get(tempId).avatar;
    try {
        return switch (tempId) {
            // ... existing cases ...
            case ConstNpc.NPC_CUA_TOI -> 
                new NpcCuaToi(mapId, status, cx, cy, tempId, avatar);
            // ...
        };
    }
}
```

---

### **Bước 4: Thêm vào database**

**SQL:**
```sql
-- Thêm template NPC
INSERT INTO npc_template (id, name, head, body, leg, avatar) 
VALUES (100, 'NPC Của Tôi', 200, 201, 202, 300);

-- Thêm NPC vào map
-- (Map 5, tọa độ x=100, y=200)
-- Sẽ được load tự động từ map data file
```

---

### **Bước 5: Thêm vào Map**

**Có 2 cách:**

**Cách 1: Hardcode (dễ)**

**File:** `src/nro/models/map/Map.java`

```java
// Trong method load map hoặc initNpc()
if (mapId == 5) { // Map làng Aru
    Npc npc = NpcFactory.createNPC(5, 0, 100, 200, ConstNpc.NPC_CUA_TOI);
    this.npcs.add(npc);
}
```

**Cách 2: Từ file data (khuyến nghị)**

Sửa file map data (nếu có) hoặc load từ database.

---

# 🗺️ HỆ THỐNG MAP

## 📊 CẤU TRÚC MAP:

```
Map
├── mapId: int (ID map)
├── mapName: String (Tên map)
├── planetId: byte (Hành tinh: 0=TĐ, 1=Namek, 2=Xayda)
├── zones: List<Zone> (Khu vực)
├── npcs: List<Npc> (NPCs trong map)
├── wayPoints: List<WayPoint> (Điểm di chuyển)
└── tileMap: int[][] (Tile data)

Zone (Khu vực trong map)
├── zoneId: int
├── players: List<Player>
├── mobs: List<Mob>
├── items: List<ItemMap>
└── bosses: List<Boss>
```

---

## 🔧 CÁCH THÊM MAP MỚI:

### **Bước 1: Chuẩn bị dữ liệu Map**

**Map cần:**
- ✅ Map ID (unique)
- ✅ Tile data (2D array)
- ✅ Background ID
- ✅ Waypoints (điểm warp)

---

### **Bước 2: Thêm vào Manager.java**

**File:** `src/nro/models/server/Manager.java`

**Method:** `loadAllMap()`

```java
// Load map từ file hoặc database
Map newMap = new Map(
    999,                    // mapId
    "Map Của Tôi",         // mapName
    (byte) 0,              // planetId (0=Trái Đất)
    (byte) 1,              // tileId
    (byte) 24,             // bgId
    (byte) 0,              // bgType
    (byte) 0,              // type
    tileMap,               // int[][] tile data
    tileTop,               // int[] tile top
    3,                     // số zones
    50,                    // max players/zone
    wayPoints              // List<WayPoint>
);

Manager.MAPS.add(newMap);
```

---

### **Bước 3: Thêm Waypoint (điểm warp)**

```java
// Trong map hiện tại
WayPoint wp = new WayPoint();
wp.goMapId = 999;     // ID map đích
wp.goX = 100;         // Tọa độ x đích
wp.goY = 200;         // Tọa độ y đích
wp.minPower = 0;      // Sức mạnh tối thiểu (0 = ai cũng vào được)
wp.name = "Đến Map Mới";

currentMap.wayPoints.add(wp);
```

---

### **Bước 4: Thêm NPCs vào map**

```java
newMap.npcs = new ArrayList<>();

// Thêm NPC Bulma
Npc bulma = NpcFactory.createNPC(999, 0, 50, 100, ConstNpc.BUNMA);
newMap.npcs.add(bulma);

// Thêm shop
Npc shop = NpcFactory.createNPC(999, 0, 150, 100, ConstNpc.CUA_HANG_KY_GUI);
newMap.npcs.add(shop);
```

---

### **Bước 5: Spawn Mobs**

```java
// Trong Zone của map
Zone zone = newMap.zones.get(0);

// Thêm mob (ID 0 = Ốc mượn hermit)
for (int i = 0; i < 10; i++) {
    Mob mob = new Mob();
    mob.id = i;
    mob.tempId = 0;  // Template ID từ MOB_TEMPLATES
    mob.sys = (byte) Util.nextInt(0, 2); // Hệ
    mob.location.x = Util.nextInt(50, 500);
    mob.location.y = 200;
    zone.mobs.add(mob);
}
```

---

# 💪 TIỀM NĂNG SỨC MẠNH (TNSM)

## 📊 HỆ THỐNG STATS:

**File:** `src/nro/models/player/NPoint.java`

### **Các chỉ số chính:**

```java
public long power;        // Sức mạnh
public long tiemNang;     // Tiềm năng

public int hp, hpMax;     // HP
public int mp, mpMax;     // MP
public int dame;          // Sức đánh
public int def;           // Giáp
public int crit;          // Chí mạng

// Tỉ lệ %
public List<Integer> tlTNSM;    // % Tiềm năng SM
public List<Integer> tlHp;      // % HP
public List<Integer> tlMp;      // % MP
public List<Integer> tlDame;    // % Dame
public List<Integer> tlDef;     // % Def
```

---

## 🔧 CÁCH THAY ĐỔI TNSM:

### **Cách 1: Thay đổi trực tiếp**

```java
// Trong NPC hoặc Item handler
player.nPoint.tiemNang += 1000000;  // Thêm 1 triệu TNSM
player.nPoint.power += 1000000;     // Thêm 1 triệu SM

// Recalculate stats
player.nPoint.calPoint();

// Update client
Service.gI().point(player);
```

---

### **Cách 2: Qua items (% TNSM)**

**Items với option 103 = % TNSM:**

```java
Item item = player.inventory.itemsBody.get(0); // Áo

// Thêm option +50% TNSM
ItemOption option = new ItemOption();
option.optionTemplate = Manager.ITEM_OPTION_TEMPLATES.get(103);
option.param = 50; // 50%

item.itemOptions.add(option);

// Recalculate
player.nPoint.calPoint();
```

---

### **Cách 3: Formula tính toán**

**File:** `NPoint.java` method `calPoint()`

**Formula cơ bản:**
```java
// HP = HPgốc + HPAdd + (HPgốc * %HP/100)
hpMax = hpg + hpAdd;
for (Integer tl : tlHp) {
    hpMax += (hpg * tl / 100);
}

// TNSM từ items
int tnsmFromItems = 0;
for (Integer tl : tlTNSM) {
    tnsmFromItems += tl;
}

tiemNang = tiemNang + (tiemNang * tnsmFromItems / 100);
```

---

### **Cách 4: Thay đổi công thức tính**

**Tìm method `calPoint()` trong NPoint.java:**

```java
public void calPoint() {
    // Sửa công thức ở đây!
    
    // Ví dụ: Tăng gấp đôi HP
    hpMax = (hpg + hpAdd) * 2;
    
    // Ví dụ: TNSM tối đa 200%
    int totalTNSM = 0;
    for (Integer tl : tlTNSM) {
        totalTNSM += tl;
    }
    if (totalTNSM > 200) {
        totalTNSM = 200; // Cap ở 200%
    }
    
    tiemNang = tiemNang + (tiemNang * totalTNSM / 100);
}
```

---

## 💎 CÁCH THAY ĐỔI REWARDS/QUEST:

### **VD: QuyLaoKame - Thay đổi quest rewards**

**File:** `src/nro/models/npc_list/QuyLaoKame.java` (dòng 70-77)

```java
static {
    // Quest 1: Thu thập 100 cuốn chả giò
    KOL_QUESTS.put(1, new KOLQuestData(
        ConstNpc.KOL_QUEST_TYPE_ITEM_COLLECTION,
        1778,    // ID item cần thu thập
        100,     // Số lượng cần
        Arrays.asList(
            new RewardItem(1821, 5),  // Thưởng: 5 item ID 1821
            new RewardItem(457, 10)   // THÊM: 10 ngọc xanh
        ), 
        "Thu thập 100 cuốn chả giò"
    ));
    
    // Thêm quest mới
    KOL_QUESTS.put(8, new KOLQuestData(
        ConstNpc.KOL_QUEST_TYPE_ITEM_COLLECTION,
        20,      // Đá Namek
        999,     // Cần 999 viên
        Arrays.asList(
            new RewardItem(457, 999),  // 999 ngọc xanh
            new RewardItem(1360, 1)    // 1 hồn Bông tai
        ),
        "Nhiệm vụ VIP:\nThu thập 999 đá Namek"
    ));
}
```

---

## 🛒 SHOP SYSTEM:

### **Thêm item vào shop NPC:**

**File:** Database hoặc `src/nro/models/shop/`

```sql
-- Thêm shop mới
INSERT INTO shop (npc_id, type_shop) VALUES (100, 0);

-- Thêm items vào shop
INSERT INTO shop_item (shop_id, item_id, gold, gem, power_require)
VALUES 
(100, 457, 50000000, 0, 80000000000),   -- Ngọc xanh: 50tr vàng, cần 80 tỷ SM
(100, 1360, 0, 5000, 0);                 -- Hồn bông tai: 5000 ngọc
```

---

# 🗺️ THÊM MAP CHI TIẾT:

## 📝 Các file cần:

```
1. Map data: int[][] tileMap
2. Background: bgId
3. NPCs: List<Npc>  
4. Mobs: spawn trong zones
5. Waypoints: nơi warp
```

---

## 🎯 VÍ DỤ THÊM MAP ĐẦY ĐỦ:

**File:** `src/nro/models/server/Manager.java`

**Trong method `loadAllMap()`:**

```java
public void loadAllMap() {
    // ... existing maps ...
    
    // THÊM MAP MỚI
    int[][] tileMap = new int[50][50]; // 50x50 tiles
    for (int i = 0; i < 50; i++) {
        for (int j = 0; j < 50; j++) {
            tileMap[i][j] = (j == 25) ? 2 : 0; // 2 = đất, 0 = trời
        }
    }
    
    int[] tileTop = new int[50];
    for (int i = 0; i < 50; i++) {
        tileTop[i] = 2; // Tile top
    }
    
    List<WayPoint> wayPoints = new ArrayList<>();
    WayPoint wp1 = new WayPoint();
    wp1.goMapId = 5;  // Warp về làng Aru
    wp1.goX = 300;
    wp1.goY = 300;
    wp1.minPower = 0;
    wayPoints.add(wp1);
    
    Map newMap = new Map(
        999,                  // mapId
        "Hành Tinh Bí Ẩn",   // mapName  
        (byte) 3,            // planetId (tùy chỉnh)
        (byte) 0,            // tileId
        (byte) 24,           // bgId
        (byte) 0,            // bgType
        (byte) 0,            // type
        tileMap,             // tile data
        tileTop,             // tile top
        5,                   // số zones
        30,                  // max players/zone
        wayPoints            // waypoints
    );
    
    MAPS.add(newMap);
}
```

---

# 🚀 DEPLOY LÊN VPS:

## ✅ CHECKLIST DEPLOY:

### **1. Chuẩn bị VPS:**

**Windows Server 2019/2022:**
```
CPU: 4 cores
RAM: 8 GB
Disk: 40 GB SSD
IP: Public
```

---

### **2. Cài đặt:**

**2.1. Java JDK 17:**
```
https://adoptium.net/temurin/releases/
Chọn: JDK 17 - Windows x64
```

**2.2. MySQL 8.0:**
```
https://dev.mysql.com/downloads/installer/
MySQL Community Server
```

**2.3. NetBeans (optional):**
Hoặc chỉ cần JRE để run

---

### **3. Upload code:**

**Remote Desktop:**
```
Win + R → mstsc
Nhập IP VPS
Copy/paste folder project
```

---

### **4. Config:**

**File:** `Config.properties`

```properties
# Server
server.ip=103.123.45.67  ← IP PUBLIC VPS
server.port=14445
server.name=Server Test

# Database
database.host=localhost
database.port=3306
database.name=ngocrong
database.user=root
database.pass=password123  ← ĐỔI PASSWORD!
database.min=5
database.max=20  ← TĂNG connection pool
```

---

### **5. Import database:**

**MySQL Command Line:**
```sql
CREATE DATABASE ngocrong CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE ngocrong;
SOURCE E:\path\to\sql\ngocrong.sql;
```

---

### **6. Mở port Firewall:**

**Windows Firewall:**
```cmd
netsh advfirewall firewall add rule name="NRO Port" dir=in action=allow protocol=TCP localport=14445
```

---

### **7. Run server:**

**Tạo file `start_server.bat`:**
```bat
@echo off
echo Starting Ngoc Rong Online Server...
java -Xms2G -Xmx6G -XX:+UseG1GC -jar dist\NgocRongOnline.jar
pause
```

**Auto restart `restart_server.bat`:**
```bat
@echo off
:restart
echo Starting server...
java -Xms2G -Xmx6G -jar dist\NgocRongOnline.jar
echo Server stopped! Restarting in 10 seconds...
timeout /t 10
goto restart
```

---

### **8. Config client:**

**File client:** `server.txt` hoặc `ip.txt`

```
103.123.45.67
14445
```

**Build lại APK (Android) hoặc JAR (PC)**

---

## 🔒 BẢO MẬT:

### **1. MySQL:**
```sql
-- Đổi password
ALTER USER 'root'@'localhost' IDENTIFIED BY 'MậtKhẩuMạnh123!@#';

-- Tạo user riêng
CREATE USER 'nro_user'@'localhost' IDENTIFIED BY 'password';
GRANT ALL ON ngocrong.* TO 'nro_user'@'localhost';
```

### **2. Firewall:**
- Chỉ mở port 14445 (game)
- Chặn port 3306 (MySQL) từ internet
- Chỉ allow RDP từ IP cố định

### **3. Backup:**
```bat
:: Backup database hàng ngày
mysqldump -u root -p ngocrong > backup_%date%.sql
```

---

## 📊 MONITORING:

### **Check performance:**

**Task Manager:**
- Threads: ~36 (cố định)
- RAM: 2-4 GB (100 players)
- CPU: 20-40%

**Nếu quá tải:**
- Tăng RAM VPS
- Tắt bớt boss managers
- Optimize database queries

---

## 🎯 TROUBLESHOOTING:

### **Lỗi: Cannot connect to database**
```
→ Check MySQL đang chạy
→ Check firewall
→ Check password trong Config.properties
```

### **Lỗi: Port already in use**
```
→ Kill process đang dùng port
→ Hoặc đổi port trong config
```

### **Client không kết nối được:**
```
→ Check firewall VPS
→ Check IP trong client config
→ Check server đang chạy
```

---

**🎊 CHÚC MỪNG! BẠN ĐÃ CÓ GAME SERVER HOÀN CHỈNH!** 🎉

**Questions? Cứ hỏi tôi!** 💪
