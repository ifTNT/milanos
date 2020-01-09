package com.github.nukcsie110.milanos.entrypoint;
import com.github.nukcsie110.milanos.common.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.io.*;
import java.util.Iterator;
import java.util.Set;

public class main {
    private static String HS_address = "127.0.0.1";
    private static int HS_port = 8500;
    private static ArrayList<RelayInfo> relay_list = new ArrayList<RelayInfo>();
    public static int DEFAULT_PORT = 8591;    //設定初始自己的port

    public static void main(String[] args) throws IOException {
        run_Entry_server(DEFAULT_PORT);

    }

    private static byte[] generateByteSeed(int size){
        Random random = new Random();
        byte[] rtVal = new byte[size];
        random.nextBytes(rtVal);
        return rtVal;
    }

    private static void HS_get_relay_list() throws IOException {      //CONNECTION TO heartbeat
        try {
            Socket HS_socket = new Socket(HS_address, HS_port);
            HS_socket.setSoTimeout(5000);
            PrintWriter wtr = new PrintWriter(HS_socket.getOutputStream());
            wtr.println("GET / HTTP/1.1");
            wtr.println("");
            wtr.flush();

            ObjectInputStream in = new ObjectInputStream(HS_socket.getInputStream());
            relay_list = (ArrayList<RelayInfo>) in.readObject();
            InputStream input = HS_socket.getInputStream();
        } catch (IOException | ClassNotFoundException ex) {
            System.out.println(ex);
        }
    }

    private static byte[] CH_AND_PA(byte[] payload) {
        byte[] output = new byte[1024];
        int[] rand_array = new int[3];    //決定要走哪三個relay
        ArrayList<byte[]> SEK_list = new ArrayList<byte[]>(3);    //存放的3把key
        //生rand出來
        int now_size = relay_list.size();
        for (int a = 0; a < rand_array.length; ++a) {    //生3個random出來
            rand_array[a] = (int) (Math.random() * now_size);
        }
        for (int i = 0; i < 3; i++) {    //生成三把SEK
            byte[] sek = generateByteSeed(256);
            SEK_list.add(i, sek);
        }
        //開始包header
        byte[] CID = new byte[128];
        byte[] TTL = new byte[8];
        for(int i=0;i<3;i++){
            byte[] header = new byte[184];
            RelayInfo temp = relay_list.get(i);
            int port = temp.address.getPort();
            InetAddress t_addr = temp.address.getAddress();
            byte[] b_address = t_addr.getAddress();
            byte[] b_port = ByteBuffer.allocate(16).putInt(port).array();    //port 16-bytes
            System.arraycopy(CID,0,header,0,CID.length);
            System.arraycopy(b_address,32,header,128,b_address.length);
            System.arraycopy(b_port,16,header,160,b_port.length);
            System.arraycopy(TTL,8,header,176,TTL.length);
            //call詠翔function

        }
        return output;
    }


    private static void run_Entry_server(int port) throws IOException {    //啟動server
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

                        ByteBuffer buffer = ByteBuffer.allocate(1024);
                        clientKey.attach(buffer);
                    }
                    if (key.isReadable()) {
                        SocketChannel client = (SocketChannel) key.channel();
                        ByteBuffer output = (ByteBuffer) key.attachment();
                        client.read(output);


                    }
                } catch (IOException ex) {
                    key.cancel();
                    try {
                        key.channel().close();
                    } catch (IOException ignored) {
                    }
                }
            }
        }
    }
}