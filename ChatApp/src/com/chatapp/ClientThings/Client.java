package com.company.ClientThings;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client {
    private Socket tcpSocket = null;
    private DatagramSocket udpSocket = null;
    private MulticastSocket multicastSocket = null;
    
    private BufferedReader reader = null;
    private PrintWriter writer = null;
    
    private String serverAddressName;
    private String multicastAddressName = "230.1.1.1";
    private InetAddress serverInetAddress;
    private InetAddress multicastInetAddress;
    private int serverPort;
    private int multicastPort = 6666;

    private int userID;
    private int bufferLength = 8192;

    private ExecutorService executorService;

    private String[] asciis;

    public Client(String serverAddress, int serverPort){
        this.serverAddressName = serverAddress;
        this.serverPort = serverPort;
        executorService = Executors.newFixedThreadPool(3);
    }
    public void launch() throws IOException {
        launchUDPconnection();
        launchTCPconnection();
        launchMulticastConnection();
        setAsciis();
        setID();
        sendYourIDViaTcp();
        listenForTcp();
        listenForUdp();
        listenForMulticast();
        waitForOutput();

    }

    private void listenForMulticast() {
        Runnable runnable = () -> {
            byte[] receiveBufor = new byte[bufferLength];
            DatagramPacket receivePacket = new DatagramPacket(receiveBufor, receiveBufor.length);
            while(true){
                try {
                    multicastSocket.receive(receivePacket);
                    String message = new String(receivePacket.getData());
                    String[] tokens = message.split(":");
                    if (Integer.parseInt(tokens[0]) != userID)
                        System.out.println("User " + tokens[0] + " says: " + tokens[1] + " via multicast.");
                } catch (IOException e) {
                    break;
                }
            }
            terminate(4);
        };
        executorService.execute(runnable);
    }

    private void launchMulticastConnection() throws IOException {
        multicastSocket = new MulticastSocket(multicastPort);
        multicastInetAddress = InetAddress.getByName(multicastAddressName);
        multicastSocket.joinGroup(multicastInetAddress);
        System.out.println("Multicast group joined.");

    }
    
    private void setAsciis() {
        asciis = new String[3];
        asciis[0] = "\n(\\(\n( – -)\n((‘) (’)\n";
        asciis[1] = "\n(”)….(”)\n( ‘ o ‘ )\n(”)–(”)\n(””’)-(””’)\n";
        asciis[2] = "\n───────────────────────────────────────\n"
                  + " ───▐▀▄───────▄▀▌───▄▄▄▄▄▄▄─────────────\n"
                  + " ───▌▒▒▀▄▄▄▄▄▀▒▒▐▄▀▀▒██▒██▒▀▀▄──────────\n"
                  + " ──▐▒▒▒▒▀▒▀▒▀▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▀▄────────\n"
                  + " ──▌▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▄▒▒▒▒▒▒▒▒▒▒▒▒▀▄──────\n"
                  + " ▀█▒▒▒█▌▒▒█▒▒▐█▒▒▒▀▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▌─────\n"
                  + " ▀▌▒▒▒▒▒▒▀▒▀▒▒▒▒▒▒▀▀▒▒▒▒▒▒▒▒▒▒▒▒▒▒▐───▄▄\n"
                  + " ▐▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▌▄█▒█\n"
                  + " ▐▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒█▒█▀─\n"
                  + " ▐▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒█▀───\n"
                  + " ▐▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▌────\n"
                  + " ─ ▌▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▐─────\n"
                  + " ─▐▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▌─────\n"
                  + " ──▌▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▐──────\n"
                  + " ──▐▄▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▄▌──────\n"
                  + " ────▀▄▄▀▀▀▀▀▄▄▀▀▀▀▀▀▀▄▄▀▀▀▀▀▄▄▀────────\n";
    }

    private void listenForUdp() {
        Runnable runnable = () -> {
            byte[] receiveBufor = new byte[bufferLength];
            DatagramPacket receivePacket = new DatagramPacket(receiveBufor, receiveBufor.length);
            while(true){
                try {
                    udpSocket.receive(receivePacket);
                    String message = new String(receivePacket.getData());
                    System.out.println(message + " via UDP.");
                } catch (IOException e) {
                    break;
                }
            }
            terminate(3);

        };
        executorService.execute(runnable);
    }

    private void listenForTcp() {
        Runnable runnable = () -> {
            while (true) {
                String message = null;
                try {
                    message = reader.readLine();
                    System.out.println(message + " via TCP.");
                } catch (IOException e) {
                    break;
                }
            }
            terminate(2);
        };
        executorService.execute(runnable);
    }

    private void waitForOutput() throws IOException {
        String line;
        String message;
        Scanner scanner = new Scanner(System.in);

        while(true){
            line = scanner.nextLine();

            if (line.startsWith("U:")){
                message = line.substring(2);

                if (message.length() > 1) {
                    String ascii = line.substring(2,4);
                    if (ascii.matches("A\\d")) {
                        int index = Integer.parseInt(ascii.substring(1));
                        if (index < asciis.length) message = asciis[index];
                    }
                }
                byte[] buffer = message.getBytes();
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, serverInetAddress, serverPort);
                if (!udpSocket.isClosed()) udpSocket.send(packet);
                else System.out.println("UDP connection has been closed");
            }
            else if (line.startsWith("M:")) {
                message = userID + ":" + line.substring(2);
                byte[] buffer = message.getBytes();
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, multicastInetAddress, multicastPort);
                if (!multicastSocket.isClosed()) multicastSocket.send(packet);
                else System.out.println("Multicast connection has been closed");
            }
            else {
                if (line.equals("STOP")) break;
                else writer.println(line);
            }
        }
        terminate(0);

    }

    private void terminate(int code) {
        udpSocket.close();
        try {
            tcpSocket.close();
            multicastSocket.leaveGroup(multicastInetAddress);
        } catch (IOException e) {
            e.printStackTrace();
        }
        multicastSocket.close();
        System.out.println("Connection closed.");
        executorService.shutdown();
        System.exit(code);
    }


    private void setID() {
        byte[] receiveBufor = new byte[bufferLength];
        DatagramPacket receivePacket = new DatagramPacket(receiveBufor, receiveBufor.length);
        try {
            udpSocket.receive(receivePacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String message = new String(receivePacket.getData());
        String[] tokens = message.split(";");
        userID = Integer.parseInt(tokens[0]);
        System.out.println(tokens[1] + " via UDP.");
    }

    private void sendYourIDViaTcp() {
        System.out.println("Server has given ID: You are user " + userID + ".");
        writer.println("ID:" + userID);
    }

    private void launchUDPconnection() throws IOException {
        udpSocket = new DatagramSocket();
        serverInetAddress = InetAddress.getByName(serverAddressName);
        String message = "Hello";
        byte[] sendBuffer = message.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, serverInetAddress, serverPort);
        udpSocket.send(sendPacket);
        
    }

    private void launchTCPconnection() throws IOException {
        tcpSocket = new Socket(serverAddressName, serverPort);
        reader = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream()));
        writer = new PrintWriter(tcpSocket.getOutputStream(), true);
    }
}
