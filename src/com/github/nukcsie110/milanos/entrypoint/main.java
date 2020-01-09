package com.github.nukcsie110.milanos.entrypoint;
//import com.github.nukcsie110.milanos.common;
import org.omg.CORBA.PRIVATE_MEMBER;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.io.*;
import java.util.ArrayList;
import java.security.*;

public class main {

//    private RelayInfo relay_list[];

    private static void connection_to_HS() throws IOException {      //CONNECTION TO heartbeat
        int port = 8500;
        String hs_address = "127.0.0.1";
        try {
            Socket hs_socket = new Socket(hs_address, port);
            hs_socket.setSoTimeout(5000);
            InputStream input = hs_socket.getInputStream();

        } catch (IOException ex) {
            System.out.println(ex);
        }
    }

    private static void choose_way(){
        KeyGenerator keySets = new KeyGenerator();    //生成三把SEK
        ArrayList<byte[]> SEK_list = new ArrayList<byte[]>(3);
        for(int i=0;i<3;i++){
            byte[] sek = keySets.getKey();
            SEK_list.add(i,sek);
        }




    }

    private static void run_Entry_server(int port){    //啟動server
        System.out.println("Listening for connections on port " + DEFAULT_PORT);
        ServerSocketChannel serverChannel;
        Selector selector;
        try {
            serverChannel = ServerSocketChannel.open();
            ServerSocket ss = serverChannel.socket();
            InetSocketAddress address = new InetSocketAddress(DEFAULT_PORT);
            ss.bind(address);
            serverChannel.configureBlocking(false);
            selector = Selector.open();
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }    //執行server初始化

        while (true) {
            try {
                selector.select();
            } catch (IOException ex) {
                ex.printStackTrace();
                break;
            }
            Set<SelectionKey> readyKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = readyKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();
                try {
                    if (key.isAcceptable()) {
                        ServerSocketChannel server = (ServerSocketChannel) key.channel();
                        SocketChannel client = server.accept();
                        System.out.println("Accepted connection from " + client);
                        client.configureBlocking(false);
                        SelectionKey clientKey = client.register(selector, SelectionKey.OP_WRITE | SelectionKey.OP_READ);

                        ByteBuffer buffer = ByteBuffer.allocate(100);
                        clientKey.attach(buffer);
                    }
                    if (key.isReadable()) {
                        SocketChannel client = (SocketChannel) key.channel();
                        ByteBuffer output = (ByteBuffer) key.attachment();
                        client.read(output);
                    }
                    if (key.isWritable()) {
                        SocketChannel client = (SocketChannel) key.channel();
                        ByteBuffer output = (ByteBuffer) key.attachment();
                        output.flip();
                        client.write(output);
                        output.compact();
                    }
                } catch (IOException ex) {
                    key.cancel();
                    try {
                        key.channel().close();
                    } catch (IOException cex) {
                    }
                }
            }
        }
    }

    public static int DEFAULT_PORT = 8591;    //設定初始port
    public static void main(String[] args) {
        run_Entry_server(DEFAULT_PORT);

    }
}