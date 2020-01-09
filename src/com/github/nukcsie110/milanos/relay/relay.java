package com.github.nukcsie110.milanos.relay;

import com.github.nukcsie110.milanos.common.RelayInfo;
import com.sun.org.apache.bcel.internal.generic.Select;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

import javax.swing.event.CaretListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.*;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;

public class relay {

    private static String HS_address = "127.0.0.1";
    private static int HS_port = 8500;
    public ECPublicKey myPublicKey;
    private ECPrivateKey myPrivateKey;
    private byte[] mySEKey;
    private static int port = 5509;

    //分成連進來的client(ReadPkt、decrypted) 跟 要連出去的client(WritePkt、encrypted)
    class Clients{
        public SocketChannel in,out;
        //預設都是連進來的channel
        boolean act = false;

        public Clients(SocketChannel s) throws IOException{
            in = s;
            in.configureBlocking(false);
        }
        //連進來的
        public void inClients(Selector selector,SelectionKey sk){
            try {
                ByteBuffer pkt = ByteBuffer.allocate(1024);
                if(in.read(pkt) < 1){
                    return;
                }

                byte[] ip = new byte[16];
                pkt.get(ip,16,16);
                byte[] portOut = new byte[2];
                ByteBuffer toInt = ByteBuffer.wrap(portOut);
                pkt.get(portOut,32,2);
                InetAddress next = InetAddress.getByAddress(ip);
                pkt.flip();
                byte[] nextPkt = new byte[1024];
                pkt.wrap(nextPkt);
                out = SocketChannel.open(new InetSocketAddress(next,toInt.getInt()));
                ByteBuffer outPkt = ByteBuffer.allocate(1024);
                outPkt.get(nextPkt,35,1024);
                out.write(outPkt);
                out.register(selector,SelectionKey.OP_READ);
            }
            catch (Exception e){
                e.printStackTrace();
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
            ObjectOutputStream os= new ObjectOutputStream(HS_socket.getOutputStream());
            os.writeObject(myPK);
            os.close();
            HS_socket.close();
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }

    ArrayList<Clients> clientsGroup = new ArrayList<Clients>();

    public relay() throws IOException{
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
                    try {
                        if (readyChannel.isAcceptable()) {
                            ServerSocketChannel s = (ServerSocketChannel) readyChannel.channel();
                            SocketChannel incoming = s.accept();
                            System.out.println("Connected from : " + incoming);
                            if(incoming == null) continue;
                            addCs(incoming);
                            incoming.register(selector, SelectionKey.OP_READ);
                        } else if (readyChannel.isReadable()) {
                            for(int i = 0; i < clientsGroup.size();i++){
                                Clients client = clientsGroup.get(i);
                                if(readyChannel.channel() == client.in){
                                    client.inClients(selector,readyChannel);
                                }
                                else if(readyChannel.channel() == client.out){
                                    client.outClients(selector,readyChannel);
                                }
                            }
                        }
                    } catch (IOException e) {

                    }
                }
            }
        }catch (Exception ex){
            ex.printStackTrace();
            return;
        }
    }

    public static void main(String[] arg) throws Exception {
        relay r1 = new relay();
//        relay r2 = new relay();
//        relay r3 = new relay();
    }
}
