package com.github.nukcsie110.milanos.relay;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.*;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class relay {

    public byte[] myPublicKey;
    private byte[] myPrivateKey;
    private byte[] mySEKey;
    private static int port = 5509;

    public relay() {
        KeyGenerator Sets = new KeyGenerator();
        myPublicKey = Sets.getPublicKey();
        myPrivateKey = Sets.getPrivateKey();
        heartBeat(myPublicKey);
    }

    private void forwarding(byte[] data){

    }

    private void heartBeat(byte[] myPK){

    }

    public static void main(String arg[]){
        relay r = new relay();

        ServerSocketChannel serverChannel;
        Selector selector;
        try {
            serverChannel = ServerSocketChannel.open();
            ServerSocket server = serverChannel.socket();
            InetSocketAddress address = new InetSocketAddress(port);
            server.bind(address);
            serverChannel.configureBlocking(false);
            selector = Selector.open();
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        }
        catch (IOException e){
            e.printStackTrace();
            return;
        }

        try{
            selector.select();
        }catch (IOException e){
            e.printStackTrace();
            return;
        }

        Set<SelectionKey> readys = selector.selectedKeys();
        Iterator<SelectionKey> iterator = readys.iterator();
        while(iterator.hasNext()){
            SelectionKey readyChannel = iterator.next();
            iterator.remove();
            try {
                if(readyChannel.isAcceptable()){
                    ServerSocketChannel s = (ServerSocketChannel) readyChannel.channel();
                    SocketChannel incoming = s.accept();
                    System.out.println("Connected from : "+incoming);
                    incoming.configureBlocking(false);
                    SelectionKey connetChannel = incoming.register(selector,SelectionKey.OP_READ | SelectionKey.OP_WRITE);

                    ByteBuffer pktBuffer = ByteBuffer.allocate(1024 * 1024);
                    connetChannel.attach(pktBuffer);
                }
                else if(readyChannel.isReadable()){

                }
                else if(readyChannel.isWritable()){

                }
            }catch (IOException e){
                readyChannel.cancel();
                try{
                    readyChannel.channel().close();
                }catch (IOException ex){
                }
            }
        }
    }

}
