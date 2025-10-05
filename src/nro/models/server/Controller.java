package nro.models.server;

import nro.models.services.PlayerService;
import nro.models.services.IntrinsicService;
import nro.models.database.SuperRankDAO;
import nro.models.boss.Boss;
import nro.models.boss.Boss_Manager.BossManager;
import nro.models.consts.ConstAchievement;
import nro.models.services.ClanService;
import nro.models.services.ChatGlobalService;
import nro.models.services.SubMenuService;
import nro.models.services.Service;
import nro.models.services.FlagBagService;
import nro.models.services.ItemTimeService;
import nro.models.services.SkillService;
import nro.models.map.service.NpcService;
import nro.models.services.TaskService;
import nro.models.map.service.ItemMapService;
import nro.models.services.FriendAndEnemyService;
import nro.models.data.LocalManager;
import nro.models.consts.ConstIgnoreName;
import nro.models.consts.ConstMap;
import nro.models.utils.Util;
import nro.models.data.DataGame;
import nro.models.network.MySession;
import java.io.IOException;
import nro.models.map.service.ChangeMapService;
import nro.models.services_func.UseItem;
import nro.models.services_func.Input;
import nro.models.consts.ConstNpc;
import nro.models.consts.ConstTask;
import nro.models.data.ItemData;
import nro.models.database.PlayerDAO;
import nro.models.radar.Card;
import nro.models.services.RadarService;
import nro.models.map.service.NpcManager;
import nro.models.player.Player;
import nro.models.matches.PVPService;
import nro.models.services.AchievementService;
import nro.models.shop.ShopService;
import nro.models.interfaces.IMessageHandler;
import nro.models.network.Message;
import nro.models.interfaces.ISession;
import nro.models.network.netty.NettySession;
import nro.models.services_dungeon.BlackBallWarService;
import nro.models.matches.dai_hoi_vo_thuat.SuperRankService;
import nro.models.services_dungeon.TrainingService;
import nro.models.map.service.MapService;
import nro.models.combine.CombineService;
import nro.models.services_func.LuckyRound;
import nro.models.services_func.TransactionService;
import nro.models.skill.Skill;
import nro.models.utils.Logger;
import nro.models.data.LocalResultSet;
import nro.models.shop_ky_gui.ConsignShopService;

/**
 *
 * @author By Mr Blue
 *
 */
public class Controller implements IMessageHandler {

    private int errors;
    private static Controller instance;

    public static Controller gI() {
        if (instance == null) {
            instance = new Controller();
        }
        return instance;
    }

 @Override
public void onMessage(ISession s, Message _msg) {
    long st = System.currentTimeMillis();
    MySession _session = (MySession) s;  // NettySession extends MySession
    Player player = null;
    System.out.println("🎮 onMessage: cmd=" + _msg.command + ", player=" + (player != null ? player.name : "null"));
    try {
        player = _session.player;
        byte cmd = _msg.command;
        System.out.println("🎮 Switch: cmd=" + cmd + ", player=" + (player != null ? "exists" : "null"));
        switch (cmd) {
                case -100:
                    if (player == null) {
                        return;
                    }
                    if (TransactionService.gI().check(player)) {
                        Service.gI().sendThongBao(player, "Không thể thực hiện");
                        return;
                    }
                    if (player.baovetaikhoan) {
                        Service.gI().sendThongBao(player, "Chức năng bảo vệ đã được bật. Bạn vui lòng kiểm tra lại");
                        return;
                    }
                    byte action = _msg.reader().readByte();
                    switch (action) {
                        case 0:
                            short idItem = _msg.reader().readShort();
                            byte moneyType = _msg.reader().readByte();
                            int money = _msg.reader().readInt();
                            int quantity;
                            if (player.getSession().version >= 220) {
                                quantity = _msg.reader().readInt();
                            } else {
                                quantity = _msg.reader().readByte();
                            }
                            if (quantity > 0) {
                                ConsignShopService.gI().KiGui(player, idItem, money, moneyType, quantity);
                            }
                            break;
                        case 1:
                        case 2:
                            idItem = _msg.reader().readShort();
                            ConsignShopService.gI().claimOrDel(player, action, idItem);
                            break;
                        case 3:
                            idItem = _msg.reader().readShort();
                            _msg.reader().readByte();
                            _msg.reader().readInt();
                            ConsignShopService.gI().buyItem(player, idItem);
                            break;
                        case 4:
                            moneyType = _msg.reader().readByte();
                            money = _msg.reader().readByte();
                            ConsignShopService.gI().openShopKyGui(player, moneyType, money);
                            break;
                        case 5:
                            idItem = _msg.reader().readShort();
                            ConsignShopService.gI().upItemToTop(player, idItem);
                            break;
                        default:
                            Service.gI().sendThongBao(player, "Không thể thực hiện");
                            break;
                    }
                    break;

                case 127:
                    if (player != null) {
                        byte actionRadar = _msg.reader().readByte();
                        switch (actionRadar) {
                            case 0:
                                RadarService.gI().sendRadar(player, player.Cards);
                                break;
                            case 1:
                                short idC = _msg.reader().readShort();
                                Card card = player.Cards.stream().filter(r -> r != null && r.Id == idC).findFirst().orElse(null);
                                if (card != null) {
                                    if (card.Level == 0) {
                                        return;
                                    }
                                    if (card.Used == 0) {
                                        if (player.Cards.stream().anyMatch(c -> c != null && c.Used == 1)) {
                                            Service.gI().sendThongBao(player, "Số thẻ sử dụng đã đạt tối đa");
                                            return;
                                        }
                                        card.Used = 1;
                                    } else {
                                        card.Used = 0;
                                    }
                                    RadarService.gI().Radar1(player, idC, card.Used);
                                    Service.gI().point(player);
                                }
                                break;
                        }
                    }
                    break;
                case -105:
                    if (player != null) {
                        if (player.type == 0 && player.maxTime == 30) {
                            ChangeMapService.gI().changeMapBySpaceShip(player, 102, -1, Util.nextInt(60, 200));
                            player.idMark.setGotoFuture(false);
                        } else if (player.type == 1 && player.maxTime == 5) {
                            if (player.idMark != null && player.idMark.isGoToBDKB()) {
                                ChangeMapService.gI().changeMap(player, MapService.gI().getMapCanJoin(player, 135, -1), 35, 35);
                                player.idMark.setGoToBDKB(false);
                            }
                        } else if (player.type == 2 && player.maxTime == 5) {
                            if (MapService.gI().isMapHanhTinhThucVat(player.zone.map.mapId)) {
                                ChangeMapService.gI().changeMap(player, 80, -1, -1, 5);
                            } else {
                                ChangeMapService.gI().changeMap(player, 160, -1, -1, 5);
                            }
                        } else if (player.type == 3 && player.maxTime == 5) {
                            ChangeMapService.gI().changeMap(player, player.idMark.getZoneKhiGasHuyDiet(), player.idMark.getXMapKhiGasHuyDiet(), player.idMark.getYMapKhiGasHuyDiet());
                            player.idMark.setZoneKhiGasHuyDiet(null);
                        } else if (player.type == 4 && player.maxTime == 5) {
                            if (player.idMark != null && player.idMark.isGoToKGHD()) {
                                ChangeMapService.gI().changeMap(player, MapService.gI().getMapCanJoin(player, 149, -1), 100 + (Util.nextInt(-10, 10)), 336);
                                player.idMark.setGoToKGHD(false);
                            }
                        } else if (player.type == 5 && player.maxTime == 5) {
                            ChangeMapService.gI().changeMap(player, MapService.gI().getMapCanJoin(player, 156, -1), 100 + (Util.nextInt(-10, 10)), 336);
                        }
                    }
                    break;
                case 42:
                    if (s instanceof MySession) {
                        Service.gI().regisAccount((MySession) s, _msg);
                    }
                    break;
                case -127:
                    if (player != null) {
                        LuckyRound.gI().readOpenBall(player, _msg);
                    }
                    break;
                case -125:
                    if (player != null) {
                        Input.gI().doInput(player, _msg);
                    }
                    break;
                case 112:
                    if (player != null) {
                        IntrinsicService.gI().showMenu(player);
                    }
                    break;
                case -34:
                    if (player != null) {
                        switch (_msg.reader().readByte()) {
                            case 1:
                                player.magicTree.openMenuTree();
                                break;
                            case 2:
                                player.magicTree.loadMagicTree();
                                break;
                        }
                    }
                    break;
                case -99:
                    if (player != null) {
                        FriendAndEnemyService.gI().controllerEnemy(player, _msg);
                    }
                    break;
                case -85: { // client gửi captcha
                    byte type = _msg.reader().readByte();

                    if (type == 0) {
                    } else if (type == 1) {
                    } else if (type == 2) {
                        String input = _msg.reader().readUTF();
                        if (player.requireCaptcha && input.equalsIgnoreCase(player.captchaAnswer)) {
                            player.requireCaptcha = false;
                            player.captchaAnswer = "";
                            Service.gI().sendThongBao(player, "Xác nhận captcha thành công!");
                            Message done = new Message(-85);
                            done.writer().writeByte(2);
                            player.sendMessage(done);
                            done.cleanup();
                        } else {
                            Service.gI().sendThongBao(player, "Sai captcha! Vui lòng thử lại.");
                        }
                    }
                    break;
                }

                case 18:
                    if (player != null) {
                        player.changeMapVIP = true;
                        FriendAndEnemyService.gI().goToPlayerWithYardrat(player, _msg);
                    }
                    break;
                case -72:
                    if (player != null) {
                        FriendAndEnemyService.gI().chatPrivate(player, _msg);
                    }
                    break;
                case -80:
                    if (player != null) {
                        FriendAndEnemyService.gI().controllerFriend(player, _msg);
                    }
                    break;
                case -59:
                    if (player != null) {
                        if (player.baovetaikhoan) {
                            Service.gI().sendThongBao(player, "Chức năng bảo vệ đã được bật. Bạn vui lòng kiểm tra lại");
                            return;
                        }
                        PVPService.gI().controllerThachDau(player, _msg);
                    }
                    break;
                case -86:
                    if (player != null) {
                        TransactionService.gI().controller(player, _msg);
                    }
                    break;
                case -107:
                    if (player != null) {
                        Service.gI().showInfoPet(player);
                    }
                    break;
                case -108:
                    if (player != null && player.pet != null) {
                        player.pet.changeStatus(_msg.reader().readByte());
                    }
                    break;
                case 6: // buy item
                    try {
                    if (player != null && !Maintenance.isRunning) {
                        if (TransactionService.gI().check(player)) {
                            Service.gI().sendThongBao(player, "Không thể thực hiện");
                            return;
                        }
                        if (player.baovetaikhoan) {
                            Service.gI().sendThongBao(player, "Chức năng bảo vệ đã được bật. Bạn vui lòng kiểm tra lại");
                            return;
                        }

                        byte typeBuy = _msg.reader().readByte();
                        int tempId = _msg.reader().readShort();

                        ShopService.gI().takeItem(player, typeBuy, tempId);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
                case 7: //sell item
                    if (player != null && !Maintenance.isRunning) {
                        if (TransactionService.gI().check(player)) {
                            Service.gI().sendThongBao(player, "Không thể thực hiện");
                            return;
                        }
                        if (player.baovetaikhoan) {
                            Service.gI().sendThongBao(player, "Chức năng bảo vệ đã được bật. Bạn vui lòng kiểm tra lại");
                            return;
                        }
                        action = _msg.reader().readByte();
                        if (action == 0) {
                            ShopService.gI().showConfirmSellItem(player, _msg.reader().readByte(),
                                    _msg.reader().readShort());
                        } else {
                            ShopService.gI().sellItem(player, _msg.reader().readByte(),
                                    _msg.reader().readShort());
                        }
                    }
                    break;
                case 29:
                    if (player != null) {
                        ChangeMapService.gI().openZoneUI(player);
                    }
                    break;
                case 21:
                    if (player != null) {
                        int zoneId = _msg.reader().readByte();
                        ChangeMapService.gI().changeZone(player, zoneId);
                    }
                    break;
                case -71:
                    if (player != null) {
                        if (TransactionService.gI().check(player)) {
                            Service.gI().sendThongBao(player, "Không thể thực hiện");
                            return;
                        }
                        ChatGlobalService.gI().chat(player, _msg.reader().readUTF());
                    }
                    break;
                case -79:
                    if (player != null) {
                        Service.gI().getPlayerMenu(player, _msg.reader().readInt());
                    }
                    break;
                case -113:
                    if (player != null) {
                        for (int i = 0; i < 10; i++) {
                            try {
                                player.playerSkill.skillShortCut[i] = _msg.reader().readByte();
                            } catch (IOException e) {
                                player.playerSkill.skillShortCut[i] = -1;
                            }
                        }
                        player.playerSkill.sendSkillShortCut();
                    }
                    break;
                case -101:
                    if (s instanceof MySession) {
                        login2((MySession) s, _msg);
                    }
                    break;
                case -103:
                    if (player != null) {
                        byte act = _msg.reader().readByte();
                        switch (act) {
                            case 0 ->
                                Service.gI().openFlagUI(player);
                            case 1 ->
                                Service.gI().chooseFlag(player, _msg.reader().readByte());
                        }
                    }
                    break;
                case -7:
                    if (player != null) {
                        int toX = player.location.x;
                        int toY = player.location.y;
                        try {
                            byte b = _msg.reader().readByte();
                            toX = _msg.reader().readShort();
                            toY = _msg.reader().readShort();
                        } catch (Exception e) {
                        }
                        //    player.playerTask.achivements.get(ConstAchive.KHINH_CONG_THANH_THAO).count++;
                        PlayerService.gI().playerMove(player, toX, toY);
                    }
                    break;
                case -74:
                    String ip = s.getIP();
Logger.warning("✨ Địa chỉ " + ip + " đang tải dữ liệu!\n");
                    byte type = _msg.reader().readByte();
                    System.out.println("📥 ✨ CMD -74 (LOAD DATA REQUEST): type=" + type);
                    if (type == 1) {
                       System.out.println("📤 Sending data SIZE list...");
                        DataGame.sendSizeRes(_session);
                        System.out.println("✅ Data size sent! Client will request actual data...");
                    } else if (type == 2) {
                        System.out.println("📤 Sending ACTUAL data...");
                        DataGame.sendRes(_session);
                       System.out.println("✅ ACTUAL DATA SENT! Client loading...");
                    }
                    break;
                case -81:
                    if (player != null) {
                        try {
                            _msg.reader().readByte();
                            int[] indexItem = new int[_msg.reader().readByte()];
                            for (int i = 0; i < indexItem.length; i++) {
                                indexItem[i] = _msg.reader().readByte();
                            }
                            CombineService.gI().showInfoCombine(player, indexItem);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
              case -87:
    DataGame.updateData(_session);
                    break;
             case -67:
    int id = _msg.reader().readInt();
    DataGame.sendIcon(_session, id);
                    break;
             case 66:
    DataGame.sendImageByName(_session, _msg.reader().readUTF());
                    break;
                case -66:
                    if (player != null) {
                        int effId = _msg.reader().readShort();
                        int idT = effId;
                        if (player.zone == null) {
                            break;
                        }
                        int shenronType = player.zone.shenronType;
                        if (idT == 25 && shenronType != -1 && player.zone.map.mapId != 0 && player.zone.map.mapId != 7 && player.zone.map.mapId != 14) {
                            idT = shenronType == 1 ? 59 : shenronType == 0 ? 59 : 60;
                        }
                        DataGame.sendEffectTemplate(_session, effId, idT);
                    }
                    break;
                case -62:
                    if (player != null) {
                        FlagBagService.gI().sendIconFlagChoose(player, _msg.reader().readByte());
                    }
                    break;
                case -63:
                    if (player != null) {
                        byte fbid = _msg.reader().readByte();
                        int fbidz = fbid & 0xFF; //Chuyển sang byte không dấu
                        FlagBagService.gI().sendIconEffectFlag(player, fbidz);
                    }
                    break;
               case -32:
    int bgId = _msg.reader().readShort();
    DataGame.sendItemBGTemplate(_session, bgId);
                    break;
                case 22:
                    if (player != null) {
                        _msg.reader().readByte();
                        NpcManager.getNpc(ConstNpc.DAU_THAN).confirmMenu(player, _msg.reader().readByte());
                    }
                    break;
                case -33:
                case -23:
                    if (player != null) {
                        ChangeMapService.gI().changeMapWaypoint(player);
                        Service.gI().hideWaitDialog(player);
                    }
                    break;
                case -45:
                    if (player != null) {
                        if (TransactionService.gI().check(player)) {
                            Service.gI().sendThongBao(player, "Không thể thực hiện");
                            return;
                        }
                        byte status = _msg.reader().readByte();
                        SkillService.gI().useSkill(player, null, null, status, _msg);
                    }
                    break;
                case -46:
                    if (player != null) {
                        ClanService.gI().getClan(player, _msg);
                    }
                    break;
                case -51:
                    if (player != null) {
                        ClanService.gI().clanMessage(player, _msg);
                    }
                    break;
                case -54:
                    if (player != null) {
                        ClanService.gI().clanDonate(player, _msg);
                    }
                    break;
                case -49:
                    if (player != null) {
                        ClanService.gI().joinClan(player, _msg);
                    }
                    break;
                case -50:
                    if (player != null) {
                        ClanService.gI().sendListMemberClan(player, _msg.reader().readInt());
                    }
                    break;
                case -56:
                    if (player != null) {
                        ClanService.gI().clanRemote(player, _msg);
                    }
                    break;
                case -47:
                    if (player != null) {
                        ClanService.gI().sendListClan(player, _msg.reader().readUTF());
                    }
                    break;
                case -55:
                    if (player != null) {
                        ClanService.gI().showMenuLeaveClan(player);
                    }
                    break;
                case -57:
                    if (player != null) {
                        ClanService.gI().clanInvite(player, _msg);
                    }
                    break;
                case -40:
                    if (player != null) {
                        if (TransactionService.gI().check(player)) {
                            Service.gI().sendThongBao(player, "Không thể thực hiện");
                            return;
                        }
                       UseItem.gI().getItem(_session, _msg);
                    }
                    break;
               case -41:
    Service.gI().sendCaption(_session, _msg.reader().readByte());
                    break;
                case -43:
                    if (player != null) {
                        if (TransactionService.gI().check(player)) {
                            Service.gI().sendThongBao(player, "Không thể thực hiện");
                            return;
                        }
                        if (player.baovetaikhoan) {
                            Service.gI().sendThongBao(player, "Chức năng bảo vệ đã được bật. Bạn vui lòng kiểm tra lại");
                            return;
                        }
                        UseItem.gI().doItem(player, _msg);
                    }
                    break;
                case -91:
                    if (player != null) {
                        switch (player.idMark.getTypeChangeMap()) {
                            case ConstMap.CHANGE_CAPSULE -> {
                                UseItem.gI().choseMapCapsule(player, _msg.reader().readByte());
                            }
                            case ConstMap.CHANGE_BLACK_BALL -> {
                                BlackBallWarService.gI().changeMap(player, _msg.reader().readByte());
                            }
                        }
                    }
                    break;
                case -39:
                    if (player != null) {
                        ChangeMapService.gI().finishLoadMap(player);
                    }
                    break;
             case 11:
    byte modId = _msg.reader().readByte();
    DataGame.requestMobTemplate(_session, modId);
                    break;
                case 44:
                    if (player != null) {
                        if (TransactionService.gI().check(player)) {
                            Service.gI().sendThongBao(player, "Không thể thực hiện");
                            return;
                        }
                        Command.gI().chat(player, _msg.reader().readUTF());
                    }
                    break;
                case 32:
                    if (player != null) {
                        int npcId = _msg.reader().readShort();
                        int select = _msg.reader().readByte();
                        MenuController.gI().doSelectMenu(player, npcId, select);
                    }
                    break;
             case 33:
    if (player != null) {
        MenuController.gI().openMenuNPC(_session, _msg.reader().readShort(), player);
                    }
                    break;
                case 34:
                    if (player != null) {
                        int selectSkill = _msg.reader().readShort();
                        SkillService.gI().selectSkill(player, selectSkill);
                    }
                    break;
                case 54:
                    if (player != null) {
                        int mobId = _msg.reader().readByte();
                        int masterId = -1;
                        boolean isMobMe = mobId == -1;
                        if (isMobMe) {
                            masterId = _msg.reader().readInt();
                        }
//                        _msg.reader().readByte();
                        Service.gI().attackMob(player, mobId, isMobMe, masterId);
                    }
                    break;
                case -60:
                    if (player != null) {
                        int playerId = _msg.reader().readInt();
//                        _msg.reader().readByte();
                        Service.gI().attackPlayer(player, playerId);
                    }
                    break;
             case -27:       
                   System.out.println("📥 Controller: cmd=-27, sentKey=" + _session.sentKey());
                    try {
                        
                        
                   // CHỈ GỬI version info, KHÔNG gửi data!
                        System.out.println("📤 Sending VersionRes (-74)...");
                        DataGame.sendVersionRes((ISession) _session);

                        System.out.println("✅ VersionRes sent! Client will request data...");
                    } catch (Exception ex) {
                         System.out.println("❌ Error: " + ex.getMessage());
                        ex.printStackTrace();
                    }
                    break;
             case -111:
    DataGame.sendDataImageVersion(_session);
                    break;
                case -20:
                    if (player != null && !player.isDie()) {
                        int itemMapId = _msg.reader().readShort();
                        ItemMapService.gI().pickItem(player, itemMapId, false);
                    }
                    break;
                case -28:
                    messageNotMap(_session, _msg);
                    break;
                case -29:
                     System.out.println("📥 Controller: case -29, calling messageNotLogin...");
                    try {
                        messageNotLogin(_session, _msg);
                        System.out.println("✅ messageNotLogin returned");
                    } catch (Exception ex) {
                        System.out.println("❌ Exception in messageNotLogin: " + ex.getMessage());
                        ex.printStackTrace();
                    }
                    break;
                case -30:
                    messageSubCommand(_session, _msg);
                    break;
                case -15: // về nhà
                    if (player != null) {
                        int mapId = MapService.gI().isMapMaBu(player.zone.map.mapId) ? 114 : player.gender + 21;
                        ChangeMapService.gI().changeMapBySpaceShip(player, mapId, 0, -1);
                    }
                    break;
                case -16: // hồi sinh
                    if (player != null && !player.isPKDHVT) {
                        PlayerService.gI().hoiSinh(player);
                    }
                    break;
                case -104:
                    if (player != null) {
                        Service.gI().mabaove(player, _msg.reader().readInt());
                    }
                    break;
                case -118:
                    if (player != null) {
                        int _id = _msg.reader().readInt();
                        int menuType = player.idMark.getMenuType();
                        switch (menuType) {
                            case 0, 1, 2 -> {
                                SuperRankService.gI().competing(player, _id);
                            }
                            default -> {
                                if (player.isAdmin()) {
                                    Boss boss = BossManager.gI().getBoss(_id);
                                    if (boss != null) {
                                        ChangeMapService.gI().changeMapYardrat(player, boss.zone, boss.location.x, boss.location.y);
                                    }
                                } else {
                                    Service.gI().sendThongBao(player, "Không thể thực hiện");
                                }
                            }
                        }
                    }
                    break;
                case -38: //finish update
                    if (player != null) {
                        finishUpdate(player);
                    }
                    break;
                case 126: //androidPack2
                    break;
                case -78: //checkMMove
                    _msg.reader().readInt(); // second
                    break;
                case -114: //RequestPean
                    break;
                case 27:
//                    short menuid
                    break;
                case -76:
                    AchievementService.gI().confirmAchievement(player, _msg.reader().readByte());
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (errors < 5) {
                errors++;
                Logger.logException(Controller.class, e);
                if (player != null) {
                    //Logger.warning("Player: " + player.name + "\n");
                }
                Logger.errorln("Lỗi function: 'onMessage'\n");
                Logger.errorln("Lỗi controller message command: " + _msg.command + "\n");
            }
        } finally {
            _msg.cleanup();
            _msg.dispose();
            long timeDo = System.currentTimeMillis() - st;
            if (timeDo > 10000) {
            }
        }
    }

   public void messageNotLogin(MySession session, Message msg) {
        if (msg != null && session instanceof MySession) {
            MySession mySession = (MySession) session;
            try {
                byte cmd = msg.reader().readByte();
                System.out.println("📥 messageNotLogin: subCmd=" + cmd);
                switch (cmd) {
                    case 0:
                        String user = msg.reader().readUTF();
                        String pass = msg.reader().readUTF();
                        System.out.println("📥 Login: user=" + user);
                        session.login(user, pass);  // MySession has login() method
                        break;
                    case 2:
                        Service.gI().setClientType(mySession, msg);
                        break;
                    default:
                        System.out.println("⚠️ Unknown subCmd: " + cmd);
                        break;
                }
            } catch (IOException e) {
                System.out.println("❌ messageNotLogin error: " + e.getMessage());
                e.printStackTrace();
                mySession.disconnect();
                Logger.logException(Controller.class, e);
            }
        }
    }

    public void messageNotMap(MySession _session, Message _msg) {
        if (_msg != null && _session instanceof MySession) {
            MySession mySession = (MySession) _session;
            Player player = mySession.player;
            try {
                byte cmd = _msg.reader().readByte();
                switch (cmd) {
                    case 2:
                        System.out.println("📥 setClientType");
                        createChar(mySession, _msg);
                        break;
                    case 6:
                        DataGame.updateMap(mySession);
                        break;
                    case 7:
                        DataGame.updateSkill(mySession);
                        break;
                    case 8:
                        ItemData.updateItem(mySession);
                        break;
                    case 10:
                        DataGame.sendMapTemp(mySession, _msg.reader().readUnsignedByte());
                        break;
                    case 13:
                        //client ok
                        if (player != null && player.isPl()) {
                            Service.gI().player(player);
                            Service.gI().Send_Caitrang(player);

                            // -64 my flag bag
                            Service.gI().sendFlagBag(player);

                            // -113 skill shortcut
                            player.playerSkill.sendSkillShortCut();
                            // item time
                            ItemTimeService.gI().sendAllItemTime(player);

                            // send thong bao map tuong lai
                            //  TaskService.gI().sendInfoMapTuongLai(player);
                            // -70 thông báo bigmessage
                            sendThongBaoServer(player);

                            if (TaskService.gI().getIdTask(player) == ConstTask.TASK_0_0) {
                                NpcService.gI().createTutorial(player, -1,
                                        "Chào Mừng " + player.name + " Đến Với: " + ServerManager.NAME + "\n"
                                        + "Nhiệm vụ đầu tiên của bạn là di chuyển\n"
                                        + "Bạn hãy di chuyển nhân vật theo mũi tên chỉ hướng");
                            } else {
                                // -70 thông báo bigmessage
                                //sendThongBaoServer(player);
                            }

                            if (player.inventory.itemsBody.get(10).isNotNullItem()) {
                                Service.gI().sendChibi(player);
                            }
                            if (player.zone != null) {
                                player.zone.mapInfo(player);

                                if (player.getSession().version >= 220) {
                                    for (Skill skill : player.playerSkill.skills) {
                                        if (skill.currLevel <= 0 || skill.template.type != 4) {
                                            continue;
                                        }
                                        SkillService.gI().sendCurrLevelSpecial(player, skill);
                                    }
                                }
                            } else {
                                System.out.println("WARNING: player.zone is null for player " + player.name);
                            }

                            Service.gI().sendTimeSkill(player);
                            TrainingService.gI().tnsmLuyenTapUp(player);
                            player.sendNewPet();
                            if (player.getSession() != null && player.getSession().tongnap > 0) {
                                AchievementService.gI().checkDoneTask(player, ConstAchievement.LAN_DAU_NAP_NGOC);
                            }
                        }
                        break;
                    default:
                        break;
                }
            } catch (IOException e) {
                Logger.logException(Controller.class, e);
            }
        }
    }

    public void messageSubCommand(MySession _session, Message _msg) {
        if (_msg != null && _session instanceof MySession) {
            MySession mySession = (MySession) _session;
            Player player = mySession.player;
            try {
                byte command = _msg.reader().readByte();
                switch (command) {
                    case 16:
                        byte type = _msg.reader().readByte();
                        short point = _msg.reader().readShort();
                        if (player != null && player.nPoint != null) {
                            player.nPoint.increasePoint(type, point);
                        }
                        break;
                    case 64:
                        int playerId = _msg.reader().readInt();
                        int menuId = _msg.reader().readShort();
                        SubMenuService.gI().controller(player, playerId, menuId);
                        break;
                    default:
                        break;
                }
            } catch (IOException e) {
                Logger.logException(Controller.class, e);
            }
        }
    }

    private void sendThongBaoServer(Player player) {
        Service.gI().sendThongBaoFromAdmin(player, "Khuyến mãi X3 từ 16 đến hết ngày 17 mọi vũ trụ."
                + "\nX3 EXP đến hết ngày 18 tháng 8."
                + "\nTham gia sự kiện hè 3 siêu nóng!"
                + "\nĐua Top hấp dẫn để nhận các vật phẩm giá trị."
                + "\nHạ băng Rocket để nhận Pet Pikachu vĩnh viễn."
                + "\nTìm vỏ xên bọ hung để đổi phần thưởng hấp dẫn."
                + "\nVòng quay may mắn nhận nhiều vật phẩm thú vị."
                + "\nChi tiết xem tại diễn đàn, fanpage.");
    }

    public void createChar(ISession session, Message msg) {
        if (!Maintenance.isRunning && session instanceof MySession) {
            MySession mySession = (MySession) session;
            LocalResultSet rs = null;
            boolean created = false;
            try {
                String name = msg.reader().readUTF();
                int gender = msg.reader().readByte();
                int hair = msg.reader().readByte();
                if (name.length() >= 5 && name.length() <= 10) {
                    rs = LocalManager.executeQuery("select * from player where name = ?", name);
                    if (rs.first()) {
                        Service.gI().sendThongBaoOK(mySession, "Tên nhân vật đã tồn tại");
                    } else {
                        if (Util.haveSpecialCharacter(name)) {
                            Service.gI().sendThongBaoOK(mySession, "Tên nhân vật không được chứa ký tự đặc biệt");
                        } else {
                            boolean isNotIgnoreName = true;
                            for (String n : ConstIgnoreName.IGNORE_NAME) {
                                if (name.equals(n)) {
                                    Service.gI().sendThongBaoOK(mySession, "Tên nhân vật đã tồn tại");
                                    isNotIgnoreName = false;
                                    break;
                                }
                            }
                            if (isNotIgnoreName) {
                                created = PlayerDAO.createNewPlayer(mySession.userId, name.toLowerCase(), (byte) gender, hair);
                            }
                        }
                    }
                } else {
                    Service.gI().sendThongBaoOK(mySession, "Tên nhân vật chỉ đồng ý các ký tự a-z, 0-9 và chiều dài từ 5 đến 10 ký tự");
                }
            } catch (Exception e) {
                Logger.logException(Controller.class, e);
            } finally {
                if (rs != null) {
                    rs.dispose();
                }
            }
            if (created) {
                mySession.login(mySession.uu, mySession.pp);
            }
        }
    }

    public void login2(MySession session, Message msg) {
        Service.gI().sendThongBaoOK(session, "Truy Cập: " + ServerManager.DOMAIN + "\n Đề Đăng Ký & Tải Game");
    }

    public void sendInfo(MySession session) {
        try {
            Player player = session.player;
            DataGame.sendTileSetInfo(session);
            IntrinsicService.gI().sendInfoIntrinsic(player);
            Service.gI().point(player);
            TaskService.gI().sendTaskMain(player);
            Service.gI().clearMap(player);
            ClanService.gI().sendMyClan(player);
            PlayerService.gI().sendMaxStamina(player);
            PlayerService.gI().sendCurrentStamina(player);
            Service.gI().sendDanhQuaiNhanNgoc(player);
            Service.gI().sendNangDong(player);
            Service.gI().sendHavePet(player);
            Service.gI().sendTopRank(player);
            if (player.superRank != null && player.superRank.rank < 1) {
                player.superRank.rank = SuperRankDAO.getRank((int) player.id);
                player.superRank.lastRewardTime = System.currentTimeMillis();
                SuperRankDAO.insertData(player);
            }
            ServerNotify.gI().sendNotifyTab(player);
            player.setClothes.setup();
            if (player.pet != null) {
                player.pet.setClothes.setup();
            }
            ItemTimeService.gI().sendCanAutoPlay(player);
            player.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void finishUpdate(Player player) {
        if (player.getSession() != null) {
            player.getSession().finishUpdate = true;
        }
    }
}

