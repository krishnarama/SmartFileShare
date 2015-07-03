package com.bluetooth.ftp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.bluetooth.BluetoothSocket;
import com.example.javax.obex.ObexTransport;

public class BluetoothFtpRfcommTransport implements ObexTransport {

    private final BluetoothSocket mSocket;

    public BluetoothFtpRfcommTransport(BluetoothSocket socket) {
        super();
        this.mSocket = socket;
    }

    public void close() throws IOException {
        mSocket.close();
    }

    public DataInputStream openDataInputStream() throws IOException {
        return new DataInputStream(openInputStream());
    }

    public DataOutputStream openDataOutputStream() throws IOException {
        return new DataOutputStream(openOutputStream());
    }

    public InputStream openInputStream() throws IOException {
        return mSocket.getInputStream();
    }

    public OutputStream openOutputStream() throws IOException {
        return mSocket.getOutputStream();
    }

    public void connect() throws IOException {
    }

    public void create() throws IOException {
    }

    public void disconnect() throws IOException {
    }

    public void listen() throws IOException {
    }

    public boolean isConnected() throws IOException {
        //return mSocket.isConnected();
        // TODO: add implementation
        return true;
    }

    public String getRemoteAddress() {
        if (mSocket == null)
            return null;
        return mSocket.getRemoteDevice().getAddress();
    }

}
