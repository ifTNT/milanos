package com.github.nukcsie110.milanos.hs;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.net.InetSocketAddress;
import java.util.Iterator;
import java.util.Set;
import java.nio.channels.SocketChannel;
import java.nio.ByteBuffer;

public class main {
    private Selector selector;
    private InetSocketAddress listenAddress;
    private final static int PORT=8500;
    public String[][] relayifo=new String[10][2];

    public main(String address, int port) {
        
    }

    public static void main(String[] args) throws Exception {
        new main("localhost", 8500).startServer();
    }

    private void startServer() throws IOException {
        this.selector=Selector.open();
        ServerSocketChannel serverChannel=ServerSocketChannel.open();
        serverChannel.configureBlocking(false);

        serverChannel.socket().bind(listenAddress);
        serverChannel.register(this.selector,SelectionKey.OP_ACCEPT);


        while(true){
            int ReadyCount=selector.select();
            if (ReadyCount == 0) {
                continue;
            }
            Set<SelectionKey>ReadyKeys=selector.selectedKeys();
            Iterator iterator=ReadyKeys.iterator();
            while (iterator.hasNext()){
                SelectionKey Key= (SelectionKey) iterator.next();
                iterator.remove();
                if(!Key.isValid()){
                    continue;
                }
                if(Key.isAcceptable()){
                    this.accept(Key);
                }
                else if(Key.isReadable()){
                    this.read(Key);
                }
                else if(Key.isWritable()){
                    //data for client
                }
            }
        }
    }

    private  void accept(SelectionKey key) throws IOException{
        ServerSocketChannel Serverchannel= (ServerSocketChannel) key.channel();
        SocketChannel channel = Serverchannel.accept();
        channel.configureBlocking(false);
        Socket socket=channel.socket();
        SocketAddress RemoteAddr=socket.getRemoteSocketAddress();
        channel.register(this.selector,SelectionKey.OP_READ);
    }

    private void read(SelectionKey key) throws IOException{
        SocketChannel channel= (SocketChannel) key.channel();
        ByteBuffer buffer=ByteBuffer.allocate(1024);
        int read=0;
        read=channel.read(buffer);
        if(read==0){
            Socket socket=channel.socket();
            SocketAddress RemoteAddr=socket.getRemoteSocketAddress();
            channel.close();
            return;
        }
        String data = new String(buffer.array()).trim();
    }
}
