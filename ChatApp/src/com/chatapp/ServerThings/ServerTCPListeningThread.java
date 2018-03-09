package com.company.ServerThings;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class ServerTCPListeningThread implements Runnable{
    private int id;

    private Socket socket;
    private List<PrintWriter> writers;

    private BufferedReader reader;
    private PrintWriter writer;

    public ServerTCPListeningThread(Socket socket, List<PrintWriter> writers) {
        this.socket = socket;
        this.writers = writers;
    }

    @Override
    public void run() {
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String messageFromUser;
        String messageToRest;

        while(true){
            try {
                messageFromUser = reader.readLine();

                messageToRest = "User " + id + " says: " + messageFromUser;

                if (messageFromUser.startsWith("ID:")) {
                    writers.add(writer);
                    id = Integer.parseInt(messageFromUser.substring(3));
                    writer.println("Server says: you are connected to chat");
                } else {
                    System.out.println(messageToRest + " via TCP.");
                    for (PrintWriter pw : writers) {
                        if (!pw.equals(writer)) pw.println(messageToRest);
                    }
                }
            } catch (IOException | NullPointerException e) {
                System.out.println("User " + id + " left the chat.");
                break;
            }

        }
    }
}
