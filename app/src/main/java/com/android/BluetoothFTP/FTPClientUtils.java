package com.android.BluetoothFTP;

import android.util.Log;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.IOException;
import java.net.InetAddress;

/**
 * Created by siva.r on 7/16/2015.
 */
public class FTPClientUtils {

    private FTPClient mFtpClient;
    public FTPClientUtils(){
        mFtpClient = new FTPClient();

    }

    public void connectServer(final String host, final int port){
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    mFtpClient.connect(InetAddress.getByName(host), port);
                    int replyCode = mFtpClient.getReplyCode();
                    if (!FTPReply.isPositiveCompletion(replyCode)) {
                        Log.v("FTPClientUtils","Operation failed. Server reply code: " + replyCode);
                        return;
                    }
                    Log.v("FTPClientUtils","Operation failed. Server reply code: " + replyCode);
                    login("user","pwd");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void login(String username, String password){
        try {
            boolean success = mFtpClient.login(username, password);
            Log.v("FTPClientUtils","Login: " + success);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public FTPFile[] listFiles(String path){
        try {
           return mFtpClient.listFiles(path);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public FTPFile[] listFiles(){
        try {
          return  mFtpClient.listFiles();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
