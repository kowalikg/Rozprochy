package com.company.ClientThings;

import java.net.InetAddress;

public class ClientInfo {
    private InetAddress address;
    private int port;
    private int id;

    public InetAddress getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public ClientInfo(InetAddress address, int port){
        this.address = address;
        this.port = port;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass())
            return false;
        ClientInfo other = (ClientInfo) obj;
        return this.address.equals(other.address) && this.port == other.port;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
