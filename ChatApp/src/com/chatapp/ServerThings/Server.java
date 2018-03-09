package com.company.ServerThings;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private int portNumber;

    private ExecutorService executorService;
    private List<PrintWriter> printWriters;

    private ServerSocket tcpSocket;
    private DatagramSocket udpSocket;

    public Server(int portNumber, int maxUsersViaTCP){
        this.portNumber = portNumber;
        executorService = Executors.newFixedThreadPool(maxUsersViaTCP + 2);
        printWriters = new CopyOnWriteArrayList<>();
    }
    public void launch() throws IOException {
        launchUdp();
        launchTcp();
    }

    private void launchTcp() throws IOException {
        tcpSocket = new ServerSocket(portNumber);

        System.out.println("Server established TCP on port " + portNumber);
        Runnable runnable = () -> {
            while (true) {
                Socket socket = null;
                try {
                    socket = tcpSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                executorService.execute(new ServerTCPListeningThread(socket, printWriters));
            }
        };
        executorService.execute(runnable);
    }

    private void launchUdp() throws SocketException {
        udpSocket = new DatagramSocket(portNumber);
        System.out.println("Server established UDP on port " + portNumber);
        executorService.execute(new ServerUDPListeningThread(udpSocket));
    }

    public void terminate() {
        executorService.shutdown();
        try {
            tcpSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        udpSocket.close();
        System.exit(1);

    }
}
