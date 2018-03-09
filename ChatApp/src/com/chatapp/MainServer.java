package com.company;

import com.company.ServerThings.Server;

import java.io.IOException;

public class MainServer {

    public static void main(String[] args) {
        Server server = new Server(2000, 8);
        try {
            server.launch();
        } catch (IOException e) {
            server.terminate();
        }
    }
}
