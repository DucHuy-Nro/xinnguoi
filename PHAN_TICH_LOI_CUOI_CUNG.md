# 🔬 PHÂN TÍCH LỖI CUỐI CÙNG - CLIENT DISCONNECT NGAY

## 📊 HIỆN TRẠNG:

```
🟢 Client connected & key sent: 127.0.0.1 (ID: 0)
🔴 Client disconnected: 127.0.0.1 (ID: 0)
```

**Client disconnect ngay sau khi nhận key = PROTOCOL MISMATCH!**

---

## 🔍 PHÂN TÍCH CÁC VẤN ĐỀ:

### **Vấn đề 1: Message Format không đúng**

**Code gốc (MessageSendCollect.doSendMessage):**
```java
// Ghi với ENCRYPTION!
dos.writeByte(encryptKey(cmd));
dos.writeByte(encryptKey(b1));
dos.writeByte(encryptKey(b2));
dos.write(encryptedData);
```

**Netty Encoder hiện tại:**
```java
// Ghi KHÔNG MÃ HÓA!
out.writeByte(msg.command);
out.writeShort(data.length);
out.writeBytes(data);
```

**→ CLIENT KHÔNG HIỂU vì không có encryption!**

---

### **Vấn đề 2: Session Key format không đúng**

**Message -27 (session key) có format đặc biệt:**
- Phải gửi TRƯỚC KHI start send thread
- Phải gửi qua OUTPUT STREAM trực tiếp (không qua encoder)

**Code hiện tại:** Gửi qua Netty encoder → Sai format!

---

### **Vấn đề 3: QueueHandler chưa start**

```java
.startQueueHandler();  // ← Gọi TRƯỚC khi gửi key
```

**Sai vì:** QueueHandler phải start SAU khi exchange key xong!

---

## ✅ GIẢI PHÁP CUỐI CÙNG:

### **CẦN DÙNG MessageSendCollect CHO ENCODER/DECODER!**

Netty phải dùng **MessageSendCollect** giống hệt code cũ!

---

## 🔧 CODE CUỐI CÙNG:

### **FILE 1: NettyMessageDecoder.java (VIẾT LẠI HOÀN TOÀN)**

```java
package nro.models.network.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.handler.codec.ByteToMessageDecoder;
import nro.models.network.Message;
import nro.models.interfaces.IMessageSendCollect;
import io.netty.util.AttributeKey;
import java.io.*;
import java.util.List;

public class NettyMessageDecoder extends ByteToMessageDecoder {
    
    private static final AttributeKey<NettySession> SESSION_KEY = AttributeKey.valueOf("session");
    
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        NettySession session = ctx.channel().attr(SESSION_KEY).get();
        
        if (session == null || session.getSendCollect() == null) {
            return;
        }
        
        try {
            // Chuyển ByteBuf → InputStream
            int readable = in.readableBytes();
            if (readable == 0) {
                return;
            }
            
            byte[] buffer = new byte[readable];
            in.markReaderIndex();
            in.readBytes(buffer);
            
            ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
            DataInputStream dis = new DataInputStream(bais);
            
            // Dùng MessageSendCollect để decode (có encryption!)
            IMessageSendCollect collect = session.getSendCollect();
            Message msg = collect.readMessage(session, dis);
            
            if (msg != null) {
                // Chỉ consume bytes đã đọc
                int consumed = readable - dis.available();
                in.resetReaderIndex();
                in.skipBytes(consumed);
                
                out.add(msg);
            } else {
                // Chưa đủ data, rollback
                in.resetReaderIndex();
            }
            
        } catch (Exception e) {
            // Rollback nếu lỗi
            in.resetReaderIndex();
        }
    }
}
```

---

### **FILE 2: NettyMessageEncoder.java (VIẾT LẠI)**

```java
package nro.models.network.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.AttributeKey;
import nro.models.network.Message;
import nro.models.interfaces.IMessageSendCollect;
import java.io.*;

public class NettyMessageEncoder extends MessageToByteEncoder<Message> {
    
    private static final AttributeKey<NettySession> SESSION_KEY = AttributeKey.valueOf("session");
    
    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) {
        NettySession session = ctx.channel().attr(SESSION_KEY).get();
        
        if (session == null || session.getSendCollect() == null) {
            // Fallback: plain write
            byte[] data = msg.getData();
            out.writeByte(msg.command);
            out.writeShort(data.length);
            out.writeBytes(data);
            return;
        }
        
        try {
            // Dùng MessageSendCollect để encode (có encryption!)
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            
            IMessageSendCollect collect = session.getSendCollect();
            collect.doSendMessage(session, dos, msg);
            
            byte[] encoded = baos.toByteArray();
            out.writeBytes(encoded);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

---

### **FILE 3: NettyServerHandler.java (SỬA LẠI channelActive)**

```java
@Override
public void channelActive(ChannelHandlerContext ctx) {
    String ip = getClientIP(ctx);
    
    try {
        // 1. Tạo session
        NettySession session = new NettySession(ctx);
        ctx.channel().attr(SESSION_KEY).set(session);
        
        // 2. Init handlers (set sendCollect, keyHandler, messageHandler)
        if (acceptHandler != null) {
            acceptHandler.sessionInit(session);
        }
        
        // 3. QUAN TRỌNG: Gửi session key TRỰC TIẾP (không qua encoder)
        sendSessionKeyDirect(ctx, session);
        
        // 4. Start QueueHandler SAU khi gửi key
        session.startQueueHandler();
        
        Logger.warning("🟢 Client connected, key sent, queue started: " + ip);
        
    } catch (Exception e) {
        Logger.error("❌ Error: " + e.getMessage());
        e.printStackTrace();
        ctx.close();
    }
}

// Method mới: Gửi session key trực tiếp (không qua pipeline)
private void sendSessionKeyDirect(ChannelHandlerContext ctx, NettySession session) {
    try {
        // Tạo message -27
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        
        byte[] keys = session.getKey();
        
        // Write plain (chưa có encryption lúc này!)
        dos.writeByte(-27);  // cmd
        dos.writeShort(keys.length + 1);  // size
        dos.writeByte(keys.length);  // key length
        dos.writeByte(keys[0]);
        for (int i = 1; i < keys.length; i++) {
            dos.writeByte(keys[i] ^ keys[i - 1]);
        }
        
        byte[] data = baos.toByteArray();
        
        // Gửi trực tiếp qua channel (bypass encoder!)
        ByteBuf buf = ctx.alloc().buffer(data.length);
        buf.writeBytes(data);
        ctx.writeAndFlush(buf);
        
        session.setSentKey(true);
        
    } catch (Exception e) {
        e.printStackTrace();
    }
}
```

---

## 📋 CÁC FILE CẦN SỬA:

1. ✅ **NettyMessageDecoder.java** - Dùng MessageSendCollect.readMessage()
2. ✅ **NettyMessageEncoder.java** - Dùng MessageSendCollect.doSendMessage()
3. ✅ **NettyServerHandler.java** - Gửi key direct, start queue sau

---

## 🚀 LÝ DO CÁCH NÀY SẼ WORK:

### **Trước (SAI):**
```
1. Client connect
2. Server gửi key qua Netty encoder (không mã hóa)
3. Client nhận key sai format
4. Client disconnect
```

### **Sau (ĐÚNG):**
```
1. Client connect
2. Server gửi key TRỰC TIẾP (plain, chưa mã hóa)
3. Client nhận key đúng format
4. Client gửi request (đã mã hóa)
5. Server decode qua MessageSendCollect (có mã hóa)
6. Login thành công!
```

---

## 💡 KEY INSIGHTS:

1. **Message -27 (session key)** phải gửi PLAIN (không mã hóa)
2. **Tất cả messages sau** phải qua MessageSendCollect (có mã hóa)
3. **QueueHandler** phải start SAU khi exchange key
4. **Encoder/Decoder** phải dùng MessageSendCollect

---

**ĐÂY LÀ GIẢI PHÁP CUỐI CÙNG!**

Sửa 3 files trên theo code mẫu!
