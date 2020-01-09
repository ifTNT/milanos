package com.github.nukcsie110.milanos.hs;

import com.github.nukcsie110.milanos.common.RelayInfo;

import java.io.*;
import java.lang.reflect.Array;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;;


public class main {

    private static ArrayList<RelayInfo> relayInfos = new ArrayList<>();

    public static void main(String[] args) throws IOException {

        int portNumber = 8500;
        //bind server socket to port
        ServerSocket serverSocket = new ServerSocket(portNumber);
        System.out.println("Listening on port "+portNumber);
        try {
            while (true) { //long running server

                /*Wait for the client to make a connection and when it does, create a new socket to handle the request*/
                Socket cs = serverSocket.accept();
                System.out.println("Acceptec connection from "+cs);

                //Handle each connection in a new thread to manage concurrent users
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            DataInputStream r = new DataInputStream(cs.getInputStream());
                            byte cmd = r.readByte();
                            System.out.println(cmd&0xFF);
                            if (cmd == (byte) 0x00) { //Get relay list
                                System.out.println("Received get request");
                                ObjectOutputStream objectOutputStream =
                                        new ObjectOutputStream(cs.getOutputStream());

                                objectOutputStream.writeObject(relayInfos);
                                objectOutputStream.close();
                                System.out.println("Relay list sent. Length:"+relayInfos.size());
                            } else if (cmd == (byte) 0x01) { //Post a RelayInfo to relay list
                                System.out.println("Received post request");
                                ObjectInputStream objectInputStream =
                                        new ObjectInputStream(cs.getInputStream());
                                RelayInfo newRelayInfo = (RelayInfo) objectInputStream.readObject();
                                System.out.println(newRelayInfo);
                                relayInfos.add(newRelayInfo);
                                objectInputStream.close();
                                System.out.println("Added one relay to relay list. Length:"+relayInfos.size());
                            }
                            r.close();
                            cs.close();
                        } catch (IOException | ClassNotFoundException e) {
                            e.printStackTrace();
                        }

                    }
                }).start();
            }
        }catch(IOException e){
            e.printStackTrace();
        }finally {
            serverSocket.close();
        }

    }
}
