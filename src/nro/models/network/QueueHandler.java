package nro.models.network;

import lombok.NonNull;
import lombok.Setter;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import nro.models.interfaces.IMessageHandler;
import nro.models.interfaces.ISession;

public class QueueHandler implements Runnable {

    private ISession session;
    private BlockingDeque<Message> messages;
    @Setter
    private IMessageHandler messageHandler;

    public QueueHandler(@NonNull ISession session) {
        try {
            this.session = session;
            this.messages = new LinkedBlockingDeque<>();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        
        System.out.println("🚀 QUEUE: Started for session " + session.getID());
        
        try {
            while (session != null && session.isConnected()) {
                if (messages != null) {
                    Message message = messages.poll(5, TimeUnit.SECONDS);
                    if (message != null) {
                        System.out.println("⚙️ QUEUE: Processing message cmd=" + message.command);
                        
                        this.messageHandler.onMessage(this.session, message);
                        message.cleanup();
                        
                        System.out.println("✅ QUEUE: Message processed");
                    }
                }
                
                TimeUnit.MILLISECONDS.sleep(33);
            }
        } catch (Exception e) {
            System.out.println("❌ QUEUE: Error - " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("🛑 QUEUE: Stopped");
    }

    public void addMessage(Message msg) {
        try {
            System.out.println("➕ addMessage: cmd=" + msg.command + ", connected=" + session.isConnected() + ", queueSize=" + messages.size());

            if (!session.isConnected()) {
                System.out.println("❌ Session not connected!");
                return;
            }
            if (messages.size() >= 500) {
                System.out.println("❌ Queue full!");
                return;
            }

            messages.add(msg);
            System.out.println("✅ HANDLER: Message added to queue");
        } catch (Exception e) {
            System.out.println("❌ addMessage exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void close() {
        if (messages != null) {
            messages.clear();
        }
    }

    public void dispose() {
        this.session = null;
        this.messages = null;
    }
}
