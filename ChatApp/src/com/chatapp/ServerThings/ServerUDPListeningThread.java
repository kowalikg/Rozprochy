package com.company.ServerThings;

import com.company.ClientThings.ClientInfo;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServerUDPListeningThread implements Runnable {
    private DatagramSocket socket;
    private List<ClientInfo> clientInfos;

    private int bufferLength = 8192;
    private int lastClientID = 1;

    public ServerUDPListeningThread(DatagramSocket socket){
        this.socket = socket;
        this.clientInfos = new CopyOnWriteArrayList<>();
    }

    private void sendMessage(String message, ClientInfo client) throws IOException {
        byte[] sendBuffer = message.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length,
                client.getAddress(), client.getPort());
        socket.send(sendPacket);
    }
    @Override
    public void run() {
        while (true){
            try {
                byte[] receiveBuffer = new byte[bufferLength];
                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                socket.receive(receivePacket);

                String messageFromUser = new String(receivePacket.getData());
                System.out.println("User " + receivePacket.getPort() + " says: " + messageFromUser + " via UDP.");

                ClientInfo currentClientInfo = new ClientInfo(receivePacket.getAddress(), receivePacket.getPort());

                String messageToUser;

                if (!clientInfos.contains(currentClientInfo)){
                    currentClientInfo.setId(lastClientID++);
                    clientInfos.add(currentClientInfo);
                    messageToUser = currentClientInfo.getId() + ";Server says: you are connected to chat";
                    sendMessage(messageToUser, currentClientInfo);
                }
                else {
                    currentClientInfo = getFullUserInfo(currentClientInfo);
                    messageToUser = "User " + currentClientInfo.getId() + " says: " + messageFromUser;
                    for (ClientInfo clientInfo : clientInfos) {
                        if (!clientInfo.equals(currentClientInfo)) {
                            sendMessage(messageToUser, clientInfo);
                        }
                    }
                }
            }
            catch (IOException e) {
                break;
            }

        }
        socket.close();
    }

    private ClientInfo getFullUserInfo(ClientInfo currentClientInfo) {
        for (ClientInfo clientInfo: clientInfos){
            if (clientInfo.equals(currentClientInfo)) return clientInfo;
        }
        return null;
    }

}
