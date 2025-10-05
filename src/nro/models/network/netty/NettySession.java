package nro.models.network.netty;

import io.netty.channel.ChannelHandlerContext;
import nro.models.consts.SocketType;
import nro.models.interfaces.*;
import nro.models.network.Message;
import nro.models.network.QueueHandler;

/**
 * Netty Session - Thay thế cho Session.java
 * 
 * Implement ISession để tương thích 100% với code cũ
 */
public class NettySession implements ISession {
    
    private static int ID_COUNTER = 0;
    
    private final int id;
    private final ChannelHandlerContext ctx;
    private final String ip;
    private IMessageHandler messageHandler;
    private IKeySessionHandler keyHandler;
    private IMessageSendCollect sendCollect;
    private QueueHandler queueHandler;
    private byte[] keys;
    private boolean sentKey;
    
    public NettySession(ChannelHandlerContext ctx) {
        this.id = ID_COUNTER++;
        this.ctx = ctx;
        this.ip = extractIP(ctx);
        this.keys = "NguyenDucVuEntertainment".getBytes();
        this.sentKey = false;
        
        // Create queue handler (giống Session.java cũ)
        this.queueHandler = new QueueHandler(this);
    }
    
    @Override
    public void sendMessage(Message msg) {
        if (ctx != null && ctx.channel().isActive()) {
            ctx.writeAndFlush(msg);
        }
    }
    
    @Override
    public void doSendMessage(Message msg) throws Exception {
        sendMessage(msg);
    }
    
    @Override
    public void disconnect() {
        if (ctx != null && ctx.channel().isActive()) {
            ctx.close();
        }
    }
    
    @Override
    public void dispose() {
        if (queueHandler != null) {
            queueHandler.dispose();
            queueHandler = null;
        }
        messageHandler = null;
        keyHandler = null;
        sendCollect = null;
        keys = null;
    }
    
    @Override
    public String getIP() {
        return ip;
    }
    
    @Override
    public long getID() {
        return id;
    }
    
    @Override
    public boolean isConnected() {
        return ctx != null && ctx.channel().isActive();
    }
    
    @Override
    public ISession setSendCollect(IMessageSendCollect collect) {
        this.sendCollect = collect;
        return this;
    }
    
    @Override
    public ISession setMessageHandler(IMessageHandler handler) {
        this.messageHandler = handler;
        if (queueHandler != null) {
            queueHandler.setMessageHandler(handler);
        }
        return this;
    }
    
    @Override
    public ISession setKeyHandler(IKeySessionHandler handler) {
        this.keyHandler = handler;
        return this;
    }
    
    @Override
    public ISession startSend() {
        // Netty handles this automatically
        return this;
    }
    
    @Override
    public ISession startCollect() {
        // Netty handles this automatically
        return this;
    }
    
    @Override
    public ISession startQueueHandler() {
        if (queueHandler != null) {
            new Thread(queueHandler, "QueueHandler-" + id).start();
        }
        return this;
    }
    
    @Override
    public ISession start() {
        startQueueHandler();
        return this;
    }
    
    @Override
    public void sendKey() throws Exception {
        if (keyHandler != null) {
            keyHandler.sendKey(this);
        }
    }
    
    @Override
    public void setKey(Message msg) throws Exception {
        if (keyHandler != null) {
            keyHandler.setKey(this, msg);
        }
    }
    
    @Override
    public void setKey(byte[] keys) {
        this.keys = keys;
    }
    
    @Override
    public byte[] getKey() {
        return keys;
    }
    
    @Override
    public boolean sentKey() {
        return sentKey;
    }
    
    @Override
    public void setSentKey(boolean sent) {
        this.sentKey = sent;
    }
    
    @Override
    public SocketType getSocketType() {
        return SocketType.SERVER;
    }
    
    @Override
    public QueueHandler getQueueHandler() {
        return queueHandler;
    }
    
    public IMessageHandler getMessageHandler() {
        return messageHandler;
    }
    
    private String extractIP(ChannelHandlerContext ctx) {
        String address = ctx.channel().remoteAddress().toString();
        // Format: /127.0.0.1:12345 → 127.0.0.1
        if (address.startsWith("/")) {
            address = address.substring(1);
        }
        int colonIndex = address.lastIndexOf(':');
        if (colonIndex > 0) {
            address = address.substring(0, colonIndex);
        }
        return address;
    }
    
    // Login method (copy from MySession)
    public void login(String username, String password) {
        nro.models.player_system.AntiLogin al = nro.models.player_system.AntiLogin.getByIP(this.ipAddress);
        if (al == null) {
            al = new nro.models.player_system.AntiLogin();
            nro.models.player_system.AntiLogin.putByIP(this.ipAddress, al);
        }
        if (!al.canLogin()) {
            nro.models.services.Service.gI().sendThongBaoOK(this, al.getNotifyCannotLogin());
            return;
        }
        if (nro.models.server.Manager.LOCAL) {
            nro.models.services.Service.gI().sendThongBaoOK(this, "Server này chỉ để lưu dữ liệu\nVui lòng qua server khác");
            return;
        }
        if (nro.models.server.Maintenance.isRunning) {
            nro.models.services.Service.gI().sendThongBaoOK(this, "Server đang trong thời gian bảo trì, vui lòng quay lại sau");
            return;
        }
        if (!this.isAdmin && nro.models.server.Client.gI().getPlayers().size() >= nro.models.server.Manager.MAX_PLAYER) {
            nro.models.services.Service.gI().sendThongBaoOK(this, "Máy chủ hiện đang quá tải, cư dân vui lòng di chuyển sang máy chủ khác.");
            return;
        }
        if (this.player == null) {
            Player pl = null;
            try {
                long st = System.currentTimeMillis();
                this.uu = username;
                this.pp = password;
                // Tạm thời dùng reflection hoặc skip login
                // pl = nro.models.database.MrBlue.login(this, al);
                nro.models.utils.Logger.warning("⚠️ Login for NettySession not implemented yet!");
                if (pl != null) {
                    this.timeWait = 0;
                    this.joinedGame = true;
                    pl.nPoint.calPoint();
                    pl.nPoint.setHp(pl.nPoint.hp);
                    pl.nPoint.setMp(pl.nPoint.mp);
                    pl.zone.addPlayer(pl);
                    if (pl.pet != null) {
                        pl.pet.nPoint.calPoint();
                        pl.pet.nPoint.setHp(pl.pet.nPoint.hp);
                        pl.pet.nPoint.setMp(pl.pet.nPoint.mp);
                    }

                    pl.setSession(this);
                    nro.models.server.Client.gI().put(pl);
                    this.player = pl;
                    
                    nro.models.server.Controller.gI().sendInfo(this);
                    nro.models.utils.Logger.warning("✅ Login success for player " + this.player.name);
                    
                    if (this.player.notify != null && !this.player.notify.equals("null") && !this.player.notify.isEmpty()) {
                        nro.models.services.Service.gI().sendThongBao(this.player, this.player.notify);
                        this.player.notify = null;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (pl != null) {
                    pl.dispose();
                }
            }
        }
    }
}
