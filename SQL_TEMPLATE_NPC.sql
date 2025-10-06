-- ═══════════════════════════════════════════════════════════════
-- TEMPLATE SQL ĐỂ THÊM NPC MỚI
-- ═══════════════════════════════════════════════════════════════
-- 
-- HƯỚNG DẪN:
-- 1. Thay đổi các giá trị có dấu ⭐
-- 2. Copy từng block và chạy trong MySQL
-- 3. Kiểm tra kết quả bằng SELECT
--
-- ═══════════════════════════════════════════════════════════════

USE ngocrong;

-- ═══════════════════════════════════════════════════════════════
-- BƯỚC 1: THÊM NPC TEMPLATE
-- ═══════════════════════════════════════════════════════════════

INSERT INTO `npc_template` VALUES (
    100,                    -- ⭐ id: Chọn ID chưa dùng (check: SELECT MAX(id) FROM npc_template)
    'Thầy Rùng',           -- ⭐ name: Tên NPC hiển thị trong game
    400,                    -- ⭐ head: ID sprite đầu
    401,                    -- ⭐ body: ID sprite áo
    402,                    -- ⭐ leg: ID sprite quần
    4000                    -- ⭐ avatar: ID hình khi chat
);

-- Kiểm tra:
SELECT * FROM npc_template WHERE id = 100;

-- ═══════════════════════════════════════════════════════════════
-- BƯỚC 2: TẠO SHOP CHO NPC (NẾU CẦN)
-- ═══════════════════════════════════════════════════════════════

-- 2.1. Tạo shop
INSERT INTO `shop` VALUES (
    100,                    -- ⭐ shop_id: ID shop (thường trùng npc_id)
    100,                    -- ⭐ npc_id: ID NPC (phải tồn tại trong npc_template)
    'THAY_RUNG',           -- ⭐ tag_name: Tên gọi trong code (VIẾT HOA, KHÔNG DẤU)
    0                       -- type_shop: 0=thường, 1=kỹ năng, 3=đặc biệt
);

-- 2.2. Tạo tab shop
INSERT INTO `shop_tab` VALUES (
    100,                    -- ⭐ tab_id
    100,                    -- shop_id (trùng shop ở trên)
    'Đồ Trang Bị',         -- ⭐ tab_name: Tên tab hiển thị
    0                       -- tab_index: Thứ tự tab (0, 1, 2...)
);

-- Nếu muốn nhiều tabs:
INSERT INTO `shop_tab` VALUES (101, 100, 'Vật Phẩm', 1);
INSERT INTO `shop_tab` VALUES (102, 100, 'Ngọc', 2);

-- Kiểm tra:
SELECT s.*, st.* 
FROM shop s
LEFT JOIN shop_tab st ON s.shop_id = st.shop_id
WHERE s.shop_id = 100;

-- ═══════════════════════════════════════════════════════════════
-- BƯỚC 3: THÊM ITEMS VÀO SHOP
-- ═══════════════════════════════════════════════════════════════

-- 3.1. Item 1: Áo giáp Thần (ID 12)
INSERT INTO `shop_item` VALUES (
    NULL,                   -- shop_item_id (auto increment)
    100,                    -- ⭐ tab_id (tab muốn thêm vào)
    12,                     -- ⭐ item_template_id (ID item, check: SELECT * FROM item_template WHERE name LIKE '%tên%')
    50000000,              -- ⭐ gold_price (giá vàng, 0 = không bán bằng vàng)
    0,                      -- ⭐ gem_price (giá ngọc xanh, 0 = không bán bằng ngọc)
    80000000000,           -- ⭐ power_required (SM yêu cầu, 0 = không yêu cầu)
    -1                      -- ⭐ quantity (-1 = vô hạn, >0 = giới hạn số lượng)
);

-- 3.2. Item 2: Găng tay (ID 136)
INSERT INTO `shop_item` VALUES (NULL, 100, 136, 100000000, 0, 0, -1);

-- 3.3. Item 3: Ngọc xanh (ID 457) - đổi bằng ngọc đỏ
INSERT INTO `shop_item` VALUES (
    NULL,
    100,
    457,                    -- Ngọc xanh
    0,                      -- Không bán bằng vàng
    100,                    -- 100 ngọc đỏ (gem_price = ruby nếu type = 1)
    0,
    -1
);

-- Kiểm tra:
SELECT 
    si.*,
    it.name AS item_name
FROM shop_item si
JOIN shop_tab st ON si.tab_id = st.tab_id
JOIN nro.models.item_template it ON si.item_template_id = it.id
WHERE st.shop_id = 100;

-- ═══════════════════════════════════════════════════════════════
-- VÍ DỤ: THÊM NHIỀU ITEMS CÙNG LÚC
-- ═══════════════════════════════════════════════════════════════

INSERT INTO `shop_item` 
(tab_id, item_template_id, gold_price, gem_price, power_required, quantity)
VALUES
-- Tab Trang Bị
(100, 0,   20000000, 0, 0, -1),          -- Áo Kakarot
(100, 6,   30000000, 0, 0, -1),          -- Quần Kakarot
(100, 21,  40000000, 0, 0, -1),          -- Găng tay
(100, 27,  50000000, 0, 0, -1),          -- Giày

-- Tab Vật phẩm (tab_id = 101)
(101, 193, 5000000,  0, 0, -1),          -- Đậu thần
(101, 194, 5000000,  0, 0, -1),          -- Đậu Senzu
(101, 342, 1000000,  0, 0, -1),          -- Capsule

-- Tab Ngọc (tab_id = 102)
(102, 457, 0,  50, 0, -1),               -- Ngọc xanh (50 ruby)
(102, 861, 0, 500, 0, -1);               -- Hồng ngọc (500 ruby)

-- ═══════════════════════════════════════════════════════════════
-- BẢNG TÌM KIẾM ITEM ID
-- ═══════════════════════════════════════════════════════════════

-- Tìm item theo tên:
SELECT id, name, type, iconID 
FROM item_template 
WHERE name LIKE '%áo%'
LIMIT 20;

-- Xem tất cả items của 1 loại:
SELECT id, name 
FROM item_template 
WHERE type = 0  -- 0=áo, 1=quần, 2=găng, 3=giày, 4=rada
ORDER BY id;

-- ═══════════════════════════════════════════════════════════════
-- BẢNG THAM KHẢO: ITEM TYPE
-- ═══════════════════════════════════════════════════════════════
/*
type = 0: Áo
type = 1: Quần
type = 2: Găng tay
type = 3: Giày
type = 4: Rada
type = 5: Vật phẩm
type = 6: Đá nâng cấp
type = 7: Sách skill
type = 8: Pet
type = 9: Capsule
*/

-- ═══════════════════════════════════════════════════════════════
-- BẢNG THAM KHẢO: NPC TEMPLATES CÓ SẴN
-- ═══════════════════════════════════════════════════════════════

-- Xem 20 NPCs để tham khảo head/body/leg:
SELECT id, name, head, body, leg, avatar 
FROM npc_template 
WHERE id < 20
ORDER BY id;

-- Kết quả mẫu:
/*
id | name           | head | body | leg  | avatar
---|----------------|------|------|------|-------
0  | Ông Gôhan      | 18   | 19   | 20   | 349
7  | Bunma          | 42   | 43   | 44   | 562
13 | Quy Lão Kame   | 33   | 34   | 35   | 564
*/

-- ═══════════════════════════════════════════════════════════════
-- XÓA NPC (NẾU THÊM SAI)
-- ═══════════════════════════════════════════════════════════════

-- Xóa theo thứ tự ngược lại:
DELETE FROM shop_item WHERE tab_id = 100;
DELETE FROM shop_tab WHERE shop_id = 100;
DELETE FROM shop WHERE shop_id = 100;
DELETE FROM npc_template WHERE id = 100;

-- ═══════════════════════════════════════════════════════════════
-- KIỂM TRA TOÀN BỘ
-- ═══════════════════════════════════════════════════════════════

-- Xem tất cả thông tin NPC vừa tạo:
SELECT 
    nt.id AS npc_id,
    nt.name AS npc_name,
    nt.head, nt.body, nt.leg, nt.avatar,
    s.shop_id,
    s.tag_name,
    st.tab_id,
    st.tab_name,
    COUNT(si.shop_item_id) AS item_count
FROM npc_template nt
LEFT JOIN shop s ON nt.id = s.npc_id
LEFT JOIN shop_tab st ON s.shop_id = st.shop_id
LEFT JOIN shop_item si ON st.tab_id = si.tab_id
WHERE nt.id = 100
GROUP BY nt.id, st.tab_id;

-- ═══════════════════════════════════════════════════════════════
-- DONE! ✅
-- ═══════════════════════════════════════════════════════════════

-- Sau khi chạy xong SQL:
-- 1. Restart MySQL (nếu cần)
-- 2. Kiểm tra lại bằng các query SELECT ở trên
-- 3. Tiếp tục code Java phần NPC class
