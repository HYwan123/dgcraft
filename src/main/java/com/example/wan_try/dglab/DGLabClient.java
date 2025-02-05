package com.example.wan_try.dglab;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import net.minecraft.client.player.LocalPlayer;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.BindException;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class DGLabClient<T extends DGLabClient.DGLabContext> extends WebSocketServer implements IDGLabClient<T> {
    private final HashMap<WebSocket,T> map = new HashMap<>();
    private final InetSocketAddress reflectAddress;

    private IDgLabContextFactory<T> factory = null;

    @Override
    public void stop() throws InterruptedException {
        super.stop();
        map.clear();

    }

    public DGLabClient(InetSocketAddress realAddress, InetSocketAddress reflectAddress, IDgLabContextFactory<T> factory){
        super(realAddress);

        if(reflectAddress == null){
            reflectAddress = realAddress;
        }
        if(factory == null){
            factory = new IDgLabContextFactory() {
                @Override
                public DGLabContext createDgLabContext(WebSocket conn, String targetId, String clientId) {
                    return new DGLabContext(conn,targetId,clientId);
                }
            };
        }
        this.factory = factory;
        this.reflectAddress = reflectAddress;
    }

    public DGLabClient(InetSocketAddress realAddress,InetSocketAddress reflectAddress){
        this(realAddress,reflectAddress,null);
    }

    public DGLabClient(InetSocketAddress address){
        this(address,null,null);
    }

    // "https://www.dungeon-lab.com/app-download.php#DGLAB-SOCKET#ws://39.108.168.199:9999/"

    @Override
    public BitMatrix genQrCode(String clientID) throws WriterException {
        String fullUrl = "https://www.dungeon-lab.com/app-download.php#DGLAB-SOCKET" + "#" + "ws://"+this.reflectAddress.getHostString()+":"+this.reflectAddress.getPort() +"/" + clientID;
        System.out.println("QR URL: " + fullUrl);
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(fullUrl, BarcodeFormat.QR_CODE, 25, 25);
        return bitMatrix;
    }

    private String genID(){
        return String.valueOf(new Random().nextInt(114514));
    }
    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        String id = genID();
        System.out.println("New connection opened");
        System.out.println("Connection ID: " + id);
        System.out.println("Remote socket address: " + conn.getRemoteSocketAddress());
        conn.send(DgLabPack.createBindPack(id,"","targetId").toJson());
    }




    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        map.remove(conn);
    }


    @Override
    public void onMessage(WebSocket conn, String message) {
        var pack = DgLabPack.fromJson(message);
        switch (pack.getType()) {
            case Msg -> onMsg(conn, pack);
            case Bind -> onBind(conn, pack);
            case Break -> onBreak(conn, pack);
            case Heartbeat -> onHeartbeat(conn, pack);
            default -> System.out.println("Unknown message type: " + pack.getType());
        }
    }

    private void onMsg(WebSocket conn, DgLabPack pack) {
        // Handle regular messages
        System.out.println("Received message from " + pack.getTargetId() + ": " + pack.getMessage());
        resolveStrength(pack.getMessage(),map.get(conn));
        System.out.println(map.get(conn));
    }

    private void resolveStrength(String rawStrength,DGLabContext context){
        var result = Arrays.stream(Arrays.stream(rawStrength.split("-")).toList().get(1).split("\\+")).toList();
        context.setStrengthAWithOutNotify(Integer.parseInt(result.get(0)));
        context.setStrengthBWithOutNotify(Integer.parseInt(result.get(1)));
        context.setStrengthALimit(Integer.parseInt(result.get(2)));
        context.setStrengthBLimit(Integer.parseInt(result.get(3)));

    }

    @Override
    public List<T> getContext(String id){
        return map.values().stream().filter((x)-> Objects.equals(x.getClientId(), id)).toList();
    }


    private void onBind(WebSocket conn, DgLabPack pack) {
        // Handle binding of client ID to connection
        String clientId = pack.getClientId();
        System.out.println("Binding client ID: " + clientId);
        T context = this.factory.createDgLabContext(conn, pack.getTargetId(),clientId);
        map.put(conn, context);
//        if(Objects.equals(pack.getClientId(), Minecraft.getInstance().player.getStringUUID())){
//           if(Minecraft.getInstance().level.isClientSide()){
//               if(context instanceof MinecraftDgLabContext context1) {
//                   QrCodeHandler.getQrCodeScreen().setContext(Arrays.stream(new MinecraftDgLabContext[]{context1}).toList());
//               }
//           }
//        }
        pack.setMessage("200");
        conn.send(pack.toJson());
    }

    private void onBreak(WebSocket conn, DgLabPack pack) {
        // Handle break/disconnect requests
        map.remove(conn);
        System.out.println("Break request from " + pack.getClientId());
    }

    private void onHeartbeat(WebSocket conn, DgLabPack pack) {
        // Handle heartbeat messages
        System.out.println("Heartbeat from " + pack.getClientId());
    }




    @Override
    public void onError(WebSocket conn, Exception ex) {
        map.remove(conn);
//        if(ex instanceof BindException){
//
//        }

    }

    @Override
    public void onStart() {
        System.out.println("started");
    }


    public static class DGLabContext {
        private final AtomicInteger strengthA = new AtomicInteger(0);
        private final AtomicInteger strengthB = new AtomicInteger(0);

        public DGLabContext() {

        }

        public void disconnect(){
            this.conn.close();
        }

        public void setStrengthALimit(int strengthALimit) {
            this.strengthALimit = strengthALimit;
        }

        public void setStrengthBLimit(int strengthBLimit) {
            this.strengthBLimit = strengthBLimit;
        }

        private int strengthALimit = 100;


        private int strengthBLimit = 100;

        public int getStrengthA() {
            return strengthA.get();
        }
        private String targetId;

        public String getTargetId() {
            return targetId;
        }

        public String getClientId() {
            return clientId;
        }

        public WebSocket getConn() {
            return conn;
        }

        private String clientId;
        private WebSocket conn;
        public DGLabContext(WebSocket conn,String targetId,String clientId){
            this.conn = conn;
            this.targetId = targetId;
            this.clientId = clientId;
        }

        public void notifyChange(){
            // Format: strength-channel+mode+value
            // Channel: 1=A, 2=B
            // Mode: 0=decrease, 1=increase, 2=set absolute
            StringBuilder command = new StringBuilder("strength-");

            // Send A channel strength
            command.append("1+2+").append(strengthA);
            this.conn.send(DgLabPack.createMessagePack(clientId, this.targetId, command.toString()).toJson());

            // Send B channel strength
            command = new StringBuilder("strength-");
            command.append("2+2+").append(strengthB);
            this.conn.send(DgLabPack.createMessagePack(clientId, this.targetId, command.toString()).toJson());
            //发送一个数据同步包给这个Context对应的玩家
        }
        public void addStrengthChange(int addValue){
            // Format: strength-channel+mode+value
            // Channel: 1=A, 2=B
            // Mode: 0=decrease, 1=increase, 2=set absolute
            StringBuilder command = new StringBuilder("strength-");

            // Send A channel strength
            command.append("1+1+").append(addValue);
            this.conn.send(DgLabPack.createMessagePack(clientId, this.targetId, command.toString()).toJson());

            // Send B channel strength
            command = new StringBuilder("strength-");
            command.append("2+1+").append(addValue);
            this.conn.send(DgLabPack.createMessagePack(clientId, this.targetId, command.toString()).toJson());
        }
        public void minusStrengthChange(int minusValue){
            // Format: strength-channel+mode+value
            // Channel: 1=A, 2=B
            // Mode: 0=decrease, 1=increase, 2=set absolute
            StringBuilder command = new StringBuilder("strength-");

            // Send A channel strength
            command.append("1+0+").append(minusValue);
            this.conn.send(DgLabPack.createMessagePack(clientId, this.targetId, command.toString()).toJson());

            // Send B channel strength
            command = new StringBuilder("strength-");
            command.append("2+0+").append(minusValue);
            this.conn.send(DgLabPack.createMessagePack(clientId, this.targetId, command.toString()).toJson());
        }


        public void sendWaveForm(String channel, WaveSequence sequence) {
            conn.send(DgLabPack.createMessagePack(clientId, targetId, sequence.toMessage(channel)).toJson());
        }


        public void clearWaveForm(String channel){

            conn.send(DgLabPack.createMessagePack(clientId, targetId, "clear-" + (channel.equals("A")?"1":"2")).toJson());
        }

        protected void setStrengthAWithOutNotify(int strengthA){
            this.strengthA.set(strengthA);
        }
        protected void setStrengthBWithOutNotify(int strengthB){
            this.strengthB.set(strengthB);
        }
        public synchronized void setStrengthA(int strengthA) {
            if (strengthA >= 0 && strengthA <= strengthALimit) {
                this.strengthA.set(strengthA);
                notifyChange();
            }
        }

        public synchronized int getStrengthB() {
            return strengthB.get();
        }

        public synchronized void setStrengthB(int strengthB) {
            if (strengthB >= 0 && strengthB <= strengthBLimit) {
                this.strengthB.set(strengthB);
                notifyChange();
            }
        }

        public int getStrengthALimit() {
            return strengthALimit;
        }

        public int getStrengthBLimit() {
            return strengthBLimit;
        }

        @Override
        public String toString() {
            return "DGLabContext{" +
                    "strengthA=" + strengthA +
                    ", strengthB=" + strengthB +
                    ", strengthALimit=" + strengthALimit +
                    ", strengthBLimit=" + strengthBLimit +
                    '}';
        }
    }

    // Constants
}
