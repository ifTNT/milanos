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
    private static String HS_address = "192.168.43.176";
    private static int HS_port = 8500;
    private static ArrayList<RelayInfo> relay_list = new ArrayList<RelayInfo>();
    public static int DEFAULT_PORT = 8591;    //設定初始自己的port

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 3];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 3] = HEX_ARRAY[v >>> 4];
            hexChars[j * 3 + 1] = HEX_ARRAY[v & 0x0F];
            hexChars[j * 3 + 2] = ' ';
        }
        return new String(hexChars);
    }

    public static void main(String[] args) throws IOException {
//        RelayInfo test_relay = new RelayInfo();
//        InetSocketAddress sa = new InetSocketAddress(InetAddress.getLocalHost(),90);
//        test_relay.address = sa;
//        relay_list.add(test_relay);
        new main();
    }

    private static byte[] generateByteSeed(int size) {
        Random random = new Random();
        byte[] rtVal = new byte[size];
        random.nextBytes(rtVal);
        return rtVal;
    }

    private static void HS_get_relay_list() throws IOException {      //CONNECTION TO heartbeat
        try {
            Socket HS_socket = new Socket(HS_address, HS_port);
            HS_socket.setSoTimeout(5000);
            DataOutputStream out = new DataOutputStream(HS_socket.getOutputStream());
            out.writeByte(0x00);

            ObjectInputStream in = new ObjectInputStream(HS_socket.getInputStream());
            relay_list = (ArrayList<RelayInfo>) in.readObject();
            System.out.println("relay_list size : "+relay_list.size());
            for(RelayInfo i:relay_list){
                System.out.println(i.address.getPort());
            }
            out.close();
            in.close();
            HS_socket.close();

        } catch (IOException | ClassNotFoundException ex) {
            System.out.println(ex);
        }
    }

    private static class header_combine {
        private static int[] rand_array = new int[3];    //決定要走哪三個relay
        private static byte[] CID = new byte[16];
        private static byte[] TTL = new byte[1];
        public static int get_three(){
            return  rand_array[0];
        }
        public static byte[] make_header(byte[]  adr,byte[] port){
            ByteBuffer header = ByteBuffer.allocate(23);
            header.put(CID);
            header.put(adr);
            header.put(port);
            header.put(TTL);
            header.flip();
            byte[] rtVal = new byte[header.remaining()];
            header.get(rtVal);
            //System.out.println("header :"+rtVal);
            return rtVal;
        }


        public static byte[] APPEND_3HEADER(InetAddress Des_IP,short des_port) {
            ByteBuffer output = ByteBuffer.allocate(1024);
            ArrayList<byte[]> SEK_list = new ArrayList<byte[]>(3);    //存放的3把SEK
            //生random出來
            int now_size = relay_list.size();
            for (int a = 0; a < rand_array.length; ++a) {    //生3個random出來 放在rand_array
                rand_array[a] = (int) (Math.random() * now_size);
            }
            for (int i = 0; i < 3; i++) {    //生成三把SEK
                byte[] sek = generateByteSeed(256);
                SEK_list.add(i, sek);
            }
            //開始包header
            for (int i = 0; i < 2; i++) {
                RelayInfo relay = relay_list.get(rand_array[i]);
                //System.out.println(temp);
                short relay_port = (short)relay.address.getPort();
                InetAddress relay_address = relay.address.getAddress();
                byte[] b_address = relay_address.getAddress();    //IPv4 32-bits
                byte[] b_port = ByteBuffer.allocate(2).putShort(relay_port).array();    //port 16-bytes
                byte[] bf = make_header(b_address,b_port);
                //System.out.println("bf :"+bf);
                output.put(bf);
                //call iftnt function

            }
            byte[] des_b_address = Des_IP.getAddress();
            byte[] des_b_port = ByteBuffer.allocate(2).putShort(des_port).array();
            byte[] bf = make_header(des_b_address,des_b_port);
            output.put(bf);
            output.flip();
            byte[] rtVal = new byte[output.remaining()];
            output.get(rtVal);
            return rtVal;
        }
    }

    //reference
    class SocksClient {
        SocketChannel client, remote;
        boolean connected;
        int state = 0;

        SocksClient(SocketChannel c) throws IOException {
            client = c;
            client.configureBlocking(false);

        }
        public void newRemoteData(Selector selector, SelectionKey sk) throws IOException {
            ByteBuffer buf = ByteBuffer.allocate(1024);
            //System.out.println("in remote");
            if (remote.read(buf) < 1)
                return;
//            while ((buf.get()) != 0){
//                buf.get(payload,4,500);
//            }
//
            buf.flip();
            byte[] test = new byte[buf.remaining()];
            buf.get(test);

            System.out.println("buf.remaining : "  + bytesToHex(test));
            client.write(ByteBuffer.wrap(test));
        }

        public void newClientData(Selector selector, SelectionKey sk) throws IOException {
            if (!connected) {
                ByteBuffer inbuf = ByteBuffer.allocate(4096);
                if (client.read(inbuf) < 1)
                    return;
                inbuf.flip();
                // read socks header
                int ver = inbuf.get() & 0xFF;
                if (ver != 4) {
                    throw new IOException("incorrect version" + ver);
                }
                int cmd = inbuf.get();
                // check supported command
                if (cmd != 1) {
                    throw new IOException("incorrect version");
                }

                final short des_port = inbuf.getShort();    //
                System.out.println("des_port : " + des_port);
                final byte ip[] = new byte[4];
                // fetch IP
                inbuf.get(ip);
                System.out.println();
                System.out.println("IP "+InetAddress.getByAddress(ip));
                InetAddress remoteAddr = InetAddress.getByAddress(ip);
//                byte[] payload = new byte[512];
//                while ((inbuf.get()) != 0){
//                    inbuf.get(payload,4,500);
//                } ; // username

                // hostname provided, not IP
                if (ip[0] == 0 && ip[1] == 0 && ip[2] == 0 && ip[3] != 0) { // host provided
                    String host = "";
                    byte b;
                    while ((b = inbuf.get()) != 0) {
                        host += b;
                    }
                    remoteAddr = InetAddress.getByName(host);
                }    //get des IP


                //ByteBuffer Send_header = ByteBuffer.allocate(1024);    //要送出的header
                byte[] combinedHeader = header_combine.APPEND_3HEADER(remoteAddr,des_port);
                ByteBuffer Send_header = ByteBuffer.wrap(combinedHeader);
                Send_header.put(combinedHeader);
                Send_header.flip();
                int first_relay_index = header_combine.get_three();    //取得要連線的first_relay
                remote = SocketChannel.open(new InetSocketAddress(relay_list.get(first_relay_index).address.getAddress(), relay_list.get(first_relay_index).address.getPort()));
                System.out.println("toRelay");

                //ByteBuffer out_payload = ByteBuffer.wrap(payload);    //要傳出的payload

                //f(state == 0) {
                    remote.write(Send_header);
                //    state = 1;
                    System.out.println("HEARED SEND DONE" + Send_header);
               // }

//                out_payload.put(payload);
//                out_payload.flip();
                //remote.write(out_payload);
                //System.out.println("Payload send done" + bytesToHex(payload));
//                if (!remote.isConnected())
//                    throw new IOException("connect failed");

                ByteBuffer responseToClient = ByteBuffer.allocate(8);
                responseToClient.put(new Integer(0).byteValue());
                responseToClient.put(new Integer(90).byteValue());
                responseToClient.putShort(des_port);
                responseToClient.put(ip);

                responseToClient.flip();
                client.write(responseToClient);


                remote.configureBlocking(false);
                remote.register(selector, SelectionKey.OP_READ);

                connected = true;
            } else {
                ByteBuffer buf = ByteBuffer.allocate(1024);
                if (client.read(buf) < 1){
                    return;
                }

                buf.flip();

                byte[] test = new byte[buf.remaining()];
                buf.get(test);

                System.out.println("buf.remaining : "  + bytesToHex(test));

                remote.write(ByteBuffer.wrap(test));
            }
        }
    }

    private static ArrayList<SocksClient> clients = new ArrayList<SocksClient>();

    // utility function
        public SocksClient addClient(SocketChannel s) {
        SocksClient cl;
        try {
            cl = new SocksClient(s);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        clients.add(cl);
        return cl;
    }

    public main() throws IOException {
        ServerSocketChannel socks = ServerSocketChannel.open();
        socks.socket().bind(new InetSocketAddress(DEFAULT_PORT));
        socks.configureBlocking(false);
        Selector select = Selector.open();
        socks.register(select, SelectionKey.OP_ACCEPT);

        int lastClients = clients.size();
        // select loop
        HS_get_relay_list();
        while (true) {

            select.select(1000);

            Set keys = select.selectedKeys();
            Iterator iterator = keys.iterator();
            while (iterator.hasNext()) {
                SelectionKey k = (SelectionKey) iterator.next();

                if (!k.isValid())
                    continue;

                // new connection?
                if (k.isAcceptable() && k.channel() == socks) {
//                    System.out.println("isAcceptable");
                    // server socket
                    SocketChannel csock = socks.accept();
                    if (csock == null)
                        continue;
                    addClient(csock);
                    csock.register(select, SelectionKey.OP_READ);

                } else if (k.isReadable()) {
//                    System.out.println("isreadtable");
//                    System.out.println(k.channel());
                    // new data on a client/remote socket
                    for (int i = 0; i < clients.size(); i++) {
                        SocksClient cl = clients.get(i);
                        try {
                            if (k.channel() == cl.client) // from client (e.g. socks client)
                                cl.newClientData(select, k);
                            else if (k.channel() == cl.remote) {  // from server client is connected to (e.g. website)
                                cl.newRemoteData(select, k);
                            }
                        } catch (IOException e) { // error occurred - remove client
                            cl.client.close();
                            if (cl.remote != null)
                                cl.remote.close();
                            k.cancel();
                            clients.remove(cl);
                        }

                    }
                }
            }


            if (clients.size() != lastClients) {
                System.out.println("now client size :"+ clients.size());
                lastClients = clients.size();
            }
        }
    }


}

