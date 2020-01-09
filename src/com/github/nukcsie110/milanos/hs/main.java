package com.github.nukcsie110.milanos.hs;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;;


public class main {

    public static class relayifo implements Serializable{
        public ArrayList publickey=new ArrayList();
        public ArrayList IPAddr=new ArrayList();
    }
    public static void main(String[] args) throws IOException {

        int portNumber = 8500;
        //bind server socket to port
        ServerSocket serverSocket = new ServerSocket(portNumber);
        try {
            while (true) { //long running server

                /*Wait for the client to make a connection and when it does, create a new socket to handle the request*/
                Socket cs = serverSocket.accept();

                //Handle each connection in a new thread to manage concurrent users
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ObjectOutputStream objectOutputStream=
                                    new ObjectOutputStream(cs.getOutputStream());
                            relayifo relayifo=new relayifo();
                            relayifo.publickey.add("asd");
                            relayifo.IPAddr.add("zxc");
                            objectOutputStream.writeObject(relayifo);
                            objectOutputStream.close();




                            ObjectInputStream objectInputStream=
                                    new ObjectInputStream(cs.getInputStream());
                            relayifo object = (relayifo) objectInputStream.readObject();
                            objectInputStream.close();


                            System.out.println(relayifo.publickey);
                            System.out.println(relayifo.IPAddr);
                            //Process client request and send back response
//                            String request, response;
//                            while ((request = in.readLine()) != null) {
//                                response = processRequest(request);
//                                out.println(response);
//                                if ("Done".equals(request)) {
//                                    break;
//                                }
//                            }
                            cs.close();
                        } catch (IOException | ClassNotFoundException e) {
                            e.printStackTrace();
                        }

                    }
                }).start();
            }
        } finally {
            serverSocket.close();
        }

    }

//    public static String processRequest(String request) {
//        System.out.println("Server receive message from > " + request);
//        return request;
//    }
}
