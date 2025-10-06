package nro.models.npc_list;

import nro.models.consts.ConstNpc;
import nro.models.npc.Npc;
import nro.models.player.Player;
import nro.models.services.Service;
import nro.models.services.InventoryService;
import nro.models.shop.ShopService;
import nro.models.item.Item;
import nro.models.utils.Util;

/**
 * ⭐ TEMPLATE NPC MỚI ⭐
 * 
 * HƯỚNG DẪN SỬ DỤNG:
 * 1. Copy file này
 * 2. Đổi tên class thành tên NPC của bạn (VD: ThayRung)
 * 3. Thay đổi các giá trị CONST phía dưới
 * 4. Sửa logic trong openBaseMenu() và confirmMenu()
 * 5. Thêm vào NpcFactory.java
 * 
 * @author Your Name
 */
public class TEMPLATE_NPC_MOI extends Npc {

    // ═══════════════════════════════════════════════════════════════
    // CONST - THAY ĐỔI PHẦN NÀY
    // ═══════════════════════════════════════════════════════════════
    
    /** ID NPC trong ConstNpc.java */
    private static final byte NPC_ID = 100; // ⭐ ĐỔI ID
    
    /** Tên shop trong database */
    private static final String SHOP_NAME = "TEN_SHOP"; // ⭐ ĐỔI TÊN SHOP
    
    /** Lời chào của NPC */
    private static final String GREETING = 
        "Xin chào {player}!\n" +
        "Ta là [TÊN NPC], ta có thể giúp gì cho ngươi?";
    
    // ═══════════════════════════════════════════════════════════════
    // CONSTRUCTOR - KHÔNG CẦN SỬA
    // ═══════════════════════════════════════════════════════════════
    
    public TEMPLATE_NPC_MOI(int mapId, int status, int cx, int cy, int tempId, int avatar) {
        super(mapId, status, cx, cy, tempId, avatar);
    }

    // ═══════════════════════════════════════════════════════════════
    // MENU CHÍNH - SỬA THEO Ý MUỐN
    // ═══════════════════════════════════════════════════════════════
    
    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            
            // ─────────────────────────────────────────────────────
            // ĐIỀU KIỆN MỞ NPC (TÙY CHỌN)
            // ─────────────────────────────────────────────────────
            
            // VD: Chỉ mở nếu đủ level
            if (player.nPoint.power < 1000000) {
                createOtherMenu(player, ConstNpc.IGNORE_MENU,
                    "Ngươi cần ít nhất 1 triệu sức mạnh!",
                    "Đóng"
                );
                return;
            }
            
            // VD: Chỉ mở nếu đúng hệ
            // if (player.gender != ConstPlayer.TRAI_DAT) {
            //     createOtherMenu(player, ConstNpc.IGNORE_MENU,
            //         "Ta chỉ giúp người Trái Đất!",
            //         "Đóng"
            //     );
            //     return;
            // }
            
            // ─────────────────────────────────────────────────────
            // MENU CHÍNH
            // ─────────────────────────────────────────────────────
            
            String greeting = GREETING.replace("{player}", player.name);
            
            createOtherMenu(player, ConstNpc.BASE_MENU,
                greeting,
                "Shop",              // Option 0
                "Đổi vật phẩm",     // Option 1
                "Nhận quà",         // Option 2
                "Nhiệm vụ",         // Option 3
                "Từ chối"           // Option 4
            );
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // XỬ LÝ MENU - SỬA THEO CHỨC NĂNG
    // ═══════════════════════════════════════════════════════════════
    
    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            
            // ─────────────────────────────────────────────────────
            // XỬ LÝ MENU CHÍNH
            // ─────────────────────────────────────────────────────
            
            if (player.idMark.isBaseMenu()) {
                switch (select) {
                    case 0: // Shop
                        handleShop(player);
                        break;
                        
                    case 1: // Đổi vật phẩm
                        handleDoiVatPham(player);
                        break;
                        
                    case 2: // Nhận quà
                        handleNhanQua(player);
                        break;
                        
                    case 3: // Nhiệm vụ
                        handleNhiemVu(player);
                        break;
                }
            }
            
            // ─────────────────────────────────────────────────────
            // XỬ LÝ MENU CON
            // ─────────────────────────────────────────────────────
            
            else if (player.idMark.getIndexMenu() == ConstNpc.ORTHER_MENU) {
                // Xử lý menu con ở đây
                handleSubMenu(player, select);
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // CHỨC NĂNG 1: SHOP
    // ═══════════════════════════════════════════════════════════════
    
    private void handleShop(Player player) {
        // Mở shop
        ShopService.gI().opendShop(player, SHOP_NAME, true);
    }

    // ═══════════════════════════════════════════════════════════════
    // CHỨC NĂNG 2: ĐỔI VẬT PHẨM
    // ═══════════════════════════════════════════════════════════════
    
    private void handleDoiVatPham(Player player) {
        // Hiển thị menu con
        createOtherMenu(player, ConstNpc.ORTHER_MENU,
            "Ta có thể đổi cho ngươi:\n" +
            "- 10 Ngọc Đỏ = 1 Ngọc Xanh\n" +
            "- 100 Vàng = 1 Ngọc Đỏ",
            "Đổi Ngọc Xanh",
            "Đổi Ngọc Đỏ",
            "Từ chối"
        );
    }
    
    private void handleSubMenu(Player player, int select) {
        switch (select) {
            case 0: // Đổi ngọc xanh
                doiNgocXanh(player);
                break;
                
            case 1: // Đổi ngọc đỏ
                doiNgocDo(player);
                break;
        }
    }
    
    /**
     * Đổi 10 ngọc đỏ = 1 ngọc xanh
     */
    private void doiNgocXanh(Player player) {
        final int COST = 10;    // Giá
        final int REWARD = 1;   // Thưởng
        
        if (player.inventory.ruby >= COST) {
            player.inventory.ruby -= COST;
            player.inventory.gem += REWARD;
            
            Service.gI().sendMoney(player);
            Service.gI().sendThongBao(player, 
                "Đổi thành công! +" + REWARD + " Ngọc Xanh");
        } else {
            Service.gI().sendThongBao(player, 
                "Bạn không đủ " + COST + " Ngọc Đỏ!");
        }
    }
    
    /**
     * Đổi 100 triệu vàng = 1 ngọc đỏ
     */
    private void doiNgocDo(Player player) {
        final long COST = 100_000_000;  // 100 triệu
        final int REWARD = 1;
        
        if (player.inventory.gold >= COST) {
            player.inventory.gold -= COST;
            player.inventory.ruby += REWARD;
            
            Service.gI().sendMoney(player);
            Service.gI().sendThongBao(player, 
                "Đổi thành công! +" + REWARD + " Ngọc Đỏ");
        } else {
            Service.gI().sendThongBao(player, 
                "Bạn không đủ " + COST + " vàng!");
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // CHỨC NĂNG 3: NHẬN QUÀ
    // ═══════════════════════════════════════════════════════════════
    
    private void handleNhanQua(Player player) {
        // Kiểm tra đã nhận quà hôm nay chưa
        long today = System.currentTimeMillis() / 86400000;
        
        // VD: Lưu vào player.lastTimeNhanQua (cần thêm field)
        // if (player.lastTimeNhanQua == today) {
        //     Service.gI().sendThongBao(player, "Hôm nay bạn đã nhận quà rồi!");
        //     return;
        // }
        
        // Tặng random quà
        giveRandomReward(player);
        
        // player.lastTimeNhanQua = today;
    }
    
    /**
     * Tặng quà random
     */
    private void giveRandomReward(Player player) {
        int random = Util.nextInt(1, 100);
        
        int itemId;
        int quantity;
        String itemName;
        
        if (random <= 1) {
            // 1% - Hồng ngọc
            itemId = 861;
            quantity = 1;
            itemName = "Hồng Ngọc";
        } else if (random <= 10) {
            // 9% - Ngọc xanh
            itemId = 457;
            quantity = 5;
            itemName = "Ngọc Xanh";
        } else if (random <= 30) {
            // 20% - Ngọc đỏ
            player.inventory.ruby += 10;
            Service.gI().sendMoney(player);
            Service.gI().sendThongBao(player, "Bạn nhận được 10 Ngọc Đỏ!");
            return;
        } else {
            // 70% - Vàng
            long gold = Util.nextInt(1, 10) * 1000000; // 1-10 triệu
            player.inventory.gold += gold;
            Service.gI().sendMoney(player);
            Service.gI().sendThongBao(player, 
                "Bạn nhận được " + (gold/1000000) + " triệu vàng!");
            return;
        }
        
        // Tạo item
        Item item = InventoryService.gI().createItemNull();
        item.template = nro.models.server.Manager.ITEM_TEMPLATES.get(itemId);
        item.quantity = quantity;
        
        if (InventoryService.gI().addItemBag(player, item)) {
            InventoryService.gI().sendItemBags(player);
            Service.gI().sendThongBao(player, 
                "Bạn nhận được " + quantity + " " + itemName + "!");
        } else {
            Service.gI().sendThongBao(player, "Túi đồ đầy!");
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // CHỨC NĂNG 4: NHIỆM VỤ
    // ═══════════════════════════════════════════════════════════════
    
    private void handleNhiemVu(Player player) {
        // Kiểm tra nhiệm vụ
        // if (player.playerTask.sideTask.isReceivedQuest) {
        //     Service.gI().sendThongBao(player, "Bạn đã nhận nhiệm vụ rồi!");
        //     return;
        // }
        
        createOtherMenu(player, ConstNpc.ORTHER_MENU1,
            "Nhiệm vụ của ta:\n" +
            "Đi đánh 10 con Ốc Mượn Hermit\n" +
            "Thưởng: 1 triệu vàng",
            "Nhận",
            "Từ chối"
        );
    }

    // ═══════════════════════════════════════════════════════════════
    // UTILITY METHODS - CÁC HÀM TIỆN ÍCH
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Kiểm tra player có item không
     */
    private boolean hasItem(Player player, int itemId, int quantity) {
        Item item = InventoryService.gI().findItemBag(player, itemId);
        return item != null && item.quantity >= quantity;
    }
    
    /**
     * Trừ item từ túi
     */
    private boolean removeItem(Player player, int itemId, int quantity) {
        Item item = InventoryService.gI().findItemBag(player, itemId);
        if (item != null && item.quantity >= quantity) {
            item.quantity -= quantity;
            if (item.quantity <= 0) {
                InventoryService.gI().removeItemBag(player, item);
            }
            InventoryService.gI().sendItemBags(player);
            return true;
        }
        return false;
    }
    
    /**
     * Tặng item
     */
    private boolean giveItem(Player player, int itemId, int quantity) {
        Item item = InventoryService.gI().findItemBag(player, itemId);
        
        if (item == null) {
            // Tạo mới
            item = InventoryService.gI().createItemNull();
            item.template = nro.models.server.Manager.ITEM_TEMPLATES.get(itemId);
            item.quantity = quantity;
            
            if (InventoryService.gI().addItemBag(player, item)) {
                InventoryService.gI().sendItemBags(player);
                return true;
            }
        } else {
            // Tăng số lượng
            item.quantity += quantity;
            InventoryService.gI().sendItemBags(player);
            return true;
        }
        
        return false;
    }
    
    /**
     * Check cooldown (1 ngày)
     */
    private boolean isInCooldown(long lastTime) {
        long today = System.currentTimeMillis() / 86400000;
        long lastDay = lastTime / 86400000;
        return today == lastDay;
    }

    // ═══════════════════════════════════════════════════════════════
    // VÍ DỤ: TELEPORT ĐẾN MAP KHÁC
    // ═══════════════════════════════════════════════════════════════
    
    private void teleportToMap(Player player, int mapId, int x, int y) {
        nro.models.map.service.ChangeMapService.gI().changeMapInYard(
            player,
            mapId,
            0,      // zoneId
            x,
            y
        );
        Service.gI().sendThongBao(player, "Đã dịch chuyển!");
    }

    // ═══════════════════════════════════════════════════════════════
    // VÍ DỤ: BUFF PLAYER
    // ═══════════════════════════════════════════════════════════════
    
    private void buffPlayer(Player player, int minutes) {
        // Buff x2 exp
        player.effectSkin.isTimx2 = true;
        player.effectSkin.lastTimeThoiMien = System.currentTimeMillis();
        
        Service.gI().sendThongBao(player, 
            "Bạn được buff x2 EXP trong " + minutes + " phút!");
    }

    // ═══════════════════════════════════════════════════════════════
    // VÍ DỤ: TĂNG SỨC MẠNH
    // ═══════════════════════════════════════════════════════════════
    
    private void tangSucManh(Player player, long amount) {
        player.nPoint.tiemNang += amount;
        player.nPoint.power += amount;
        player.nPoint.calPoint();
        
        nro.models.services.Service.gI().point(player);
        Service.gI().sendThongBao(player, 
            "Bạn nhận được " + (amount/1000000) + " triệu SM!");
    }
}
