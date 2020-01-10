package com.github.nukcsie110.milanos.relay;

import com.github.nukcsie110.milanos.common.*;
import com.github.nukcsie110.milanos.entrypoint.main;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import org.bouncycastle.jce.interfaces.ECPublicKey;
import org.bouncycastle.jce.interfaces.ECPrivateKey;

public class relay {

    private static String HS_address = "127.0.0.1";
    private static int HS_port = 8500;
    public ECPublicKey myPublicKey;
    private ECPrivateKey myPrivateKey;
    private byte[] mySEKey;
    private int port;

    //分成連進來的client(ReadPkt、decrypted) 跟 要連出去的client(WritePkt、encrypted)
    class Clients{
        public SocketChannel in,out;
        //預設都是連進來的channel
        boolean act;

        public Clients(SocketChannel s) throws IOException{
            in = s;
            act = false;
        }
        //連進來的
        public void inClients(Selector selector,SelectionKey sk) throws IOException{
            if(!act) {
                ByteBuffer pkt = ByteBuffer.allocate(23);
                if(in.read(pkt) < 1){
                    return;
                }
                pkt.flip();
                System.out.println(pkt.remaining());

                byte[] cid = new byte[16];
                byte[] ip = new byte[4];
                short portOut = 0;
                pkt.get(cid).get(ip);
                portOut = pkt.getShort();
                pkt.get(); //TTE (discarded)

                InetAddress next = InetAddress.getByAddress(ip);
                //byte[] nextPkt = new byte[1024];
//                while ((pkt.get()) != 0){
//                    pkt.get(nextPkt,22,1002);
//                }
                System.out.println("Connected to: "+new InetSocketAddress(next, portOut));
                out = SocketChannel.open(new InetSocketAddress(next,portOut));
                //ByteBuffer outPkt = ByteBuffer.allocate(1024);
                //outPkt.get(nextPkt);

                if (!out.isConnected())
                    throw new IOException("connect failed");

                out.configureBlocking(false);
                out.register(selector, SelectionKey.OP_READ);

                act = true;
            } else {
                ByteBuffer buf = ByteBuffer.allocate(1024);
                if (in.read(buf) == -1)
                    throw new IOException("disconnected");
                buf.flip();
                out.write(buf);
            }
        }

        public void outClients(Selector selector,SelectionKey sk) throws IOException{
            ByteBuffer outcome = ByteBuffer.allocate(1024);
            if(in.read(outcome) == -1){
                throw new IOException();
            }
            outcome.flip();
            in.write(outcome);
        }
    }

    private Clients addCs(SocketChannel s){
        Clients c;
        try{
            c = new Clients(s);
        }catch(IOException e){
            return null;
        }
        clientsGroup.add(c);
        return c;
    }

    private void heartBeat(ECPublicKey myPK) {
        try {
            Socket HS_socket = new Socket(HS_address, HS_port);
            HS_socket.setSoTimeout(5000);

            DataOutputStream cmd = new DataOutputStream(HS_socket.getOutputStream());
            cmd.writeByte(0x01); //Post

            Socket socket = new Socket();
            socket.connect(new InetSocketAddress("google.com", 80)); //In order to get outbound ip address

            RelayInfo myInfo = new RelayInfo();
            myInfo.address = new InetSocketAddress(socket.getLocalAddress(), port);
            myInfo.publicKey = myPublicKey;
            ObjectOutputStream os= new ObjectOutputStream(HS_socket.getOutputStream());
            os.writeObject(myInfo);

            socket.close();
            cmd.close();
            os.close();
            HS_socket.close();
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }

    ArrayList<Clients> clientsGroup = new ArrayList<Clients>();

    public relay(int _port) throws IOException{
        port = _port;
        KeyGenerator Sets = new KeyGenerator();
        myPublicKey = Sets.getPublicKey();
        myPrivateKey = Sets.getPrivateKey();
        heartBeat(myPublicKey);

        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        ServerSocket server = serverChannel.socket();
        InetSocketAddress address = new InetSocketAddress(port);
        server.bind(address);
        System.out.println("listening on port : " + address.getPort());
        serverChannel.configureBlocking(false);
        Selector selector = Selector.open();
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        try {
            while (true) {
                selector.select(1000);

                Set<SelectionKey> readys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = readys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey readyChannel = iterator.next();
                    iterator.remove();
                    if (!readyChannel.isValid())
                        continue;

                    if (readyChannel.isAcceptable()) {
                        ServerSocketChannel s = (ServerSocketChannel) readyChannel.channel();
                        SocketChannel incoming = s.accept();
                        incoming.configureBlocking(false);
                        System.out.println("Connected from : " + incoming);
                        if (incoming == null)
                            continue;
                        addCs(incoming);
                        incoming.register(selector, SelectionKey.OP_READ);
                    } else if (readyChannel.isReadable()) {
                        for (int i = 0; i < clientsGroup.size(); i++) {
                            Clients client = clientsGroup.get(i);
                            if (readyChannel.channel() == client.in) {
                                client.inClients(selector, readyChannel);
                            } else if (readyChannel.channel() == client.out) {
                                client.outClients(selector, readyChannel);
                            }
                        }
                    }
                }
            }
        }catch (Exception ex){
            ex.printStackTrace();
            return;
        }
    }

    public static void main(String[] arg) throws Exception {
        int port = Integer.parseInt(arg[0]);
        relay r1 = new relay(port);
//        relay r2 = new relay();
//        relay r3 = new relay();
    }
}
