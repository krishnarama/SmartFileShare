/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.BluetoothFTP;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

import com.bluetooth.ftp.BluetoothFtpClientObexSession;
import com.bluetooth.ftp.BluetoothFtpRfcommTransport;
import com.bluetooth.utility.BluetoothFTPData;
import com.bluetooth.utility.BluetoothFTPFileInfo;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread that listens for
 * incoming connections, a thread for connecting with a device, and a
 * thread for performing data transmissions when connected.
 */
public class BluetoothService {
    // Debugging
	private static BluetoothService mBTservice;
    private static final String TAG = "MyBluetoothService";
    private static final boolean D = true;

    // Name for the SDP record when creating server socket
    private static final String NAME_SECURE = "BluetoothSecure";
    private static final String NAME_INSECURE = "BluetoothInsecure";
    private BluetoothFtpClientObexSession ftpClientSession;
    private BluetoothFtpRfcommTransport ftpRfcommTransport;
    // Unique UUID for this application
    private static final UUID MY_UUID_SECURE =
    	//UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    	UUID.fromString("00001106-0000-1000-8000-00805f9b34fb");
        //UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    private static final UUID MY_UUID_INSECURE =
        //UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");
    	UUID.fromString("00001106-0000-1000-8000-00805f9b34fb");

    // Member fields
    private final BluetoothAdapter  mAdapter;
    public 		  Handler 			ftpHandler;
    private 	  AcceptThread 		mSecureAcceptThread;
    private 	  AcceptThread 		mInsecureAcceptThread;
    private 	  ConnectThread 	mConnectThread;
    private 	  ConnectedThread 	mConnectedThread;
    private 	  BluetoothFTPData 	mFtpData;
    private		  BluetoothDevice   mConnectedDevice;
    private 	  int 				mState;
    			  Context 			mcontext;
    public 		  FileInfo 			fileInfo;
    public BluetoothSocket mmSocket;
    
    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device
    
   // public static final String BTSETTING_ACTION 		= "com.mindtree.rds.BTSettings";
   // public static final String BTMENU_ACTION 			= "com.mindtree.rds.BluetoothMenuActivity";
    public static final String ACTION_BTSERVICE_STATE_CHANGE 	= "com.mindtree.rds.action.STATE_CHANGE";
    public static final String ACTION_BTSERVICE_READ			= "com.mindtree.rds.action.MSG_READ";
    public static final String ACTION_BTSERVICE_WRITE 			= "com.mindtree.rds.action.MSG_WRITE";
    public static final String ACTION_BTSERVICE_DEVICE_NAME 	= "com.mindtree.rds.action.DEVICE_NAME";
    public static final String ACTION_BTSERVICE_TOAST 			= "com.mindtree.rds.action.MSG_TOAST";
    public static final String ACTION_BTMSG_TYPE 				= "com.mindtree.rds.action.MSG_TYPE";
    
    public static final String EXTRA_BTSERVICE_STATE_CHANGE 	= "com.mindtree.rds.extra.STATE_CHANGE";
    public static final String EXTRA_BTSERVICE_READ				= "com.mindtree.rds.extra.MSG_READ";
    public static final String EXTRA_BTSERVICE_WRITE 			= "com.mindtree.rds.extra.MSG_WRITE";
    public static final String EXTRA_BTSERVICE_DEVICE_NAME 		= "com.mindtree.rds.extra.DEVICE_NAME";
    public static final String EXTRA_BTSERVICE_TOAST 			= "com.mindtree.rds.extra.MSG_TOAST";
    public static final String EXTRA_BTMSG_TYPE 				= "com.mindtree.rds.extra.MSG_TYPE";
    
    
    
    
    public class FileInfo{
    	
    	public String       FileName;
    	public int	   	     length;
    	public OutputStream oStream;
    	public InputStream  iStream;
    }
    /**
     * Constructor. Prepares a new BluetoothChat session.
     * @param context  The UI Activity Context
     * @param handler  A Handler to send messages back to the UI Activity
     */
    private BluetoothService(Context context) {
    	mcontext = context;
    	mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;

    }
    
    public BluetoothAdapter getBluetoothAdapter(){
    	
    	if(mAdapter != null)
    		return mAdapter;
    	else
    		return null;
    }

    public BluetoothDevice getConnectedDevice(){
    	
    	if(mConnectedDevice != null)
    		return mConnectedDevice;
    	else
    		return null;
    }
    public static BluetoothService getBluetoothService(Context context) {
    	if(mBTservice == null)
    	{
    		mBTservice = new BluetoothService(context);
    	}
    	return mBTservice;
    }
    /**
     * Set the current state of the chat connection
     * @param state  An integer defining the current connection state
     */
    private synchronized void setState(int state) {
        if (D) Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;

        // Give the new state to the Handler so the UI Activity can update
        //mHandler.obtainMessage(BTSettings.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
        mcontext.sendBroadcast(new Intent(ACTION_BTSERVICE_STATE_CHANGE)
        .putExtra(EXTRA_BTSERVICE_STATE_CHANGE, state));
    }

    /**
     * Return the current connection state. */
    public synchronized int getState() {
        return mState;
    }

    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume() */
    public synchronized void start() {
        if (D) Log.d(TAG, "start");

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

       // setState(STATE_LISTEN);
        setState(STATE_NONE);

        // Start the thread to listen on a BluetoothServerSocket
        /*if (mSecureAcceptThread == null) {
            mSecureAcceptThread = new AcceptThread(true);
            mSecureAcceptThread.start();
        }*/
        
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     * @param device  The BluetoothDevice to connect
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    public synchronized void connect(BluetoothDevice device, boolean secure) {
        if (D) Log.d(TAG, "connect to: " + device);

        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device, secure);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     * @param socket  The BluetoothSocket on which the connection was made
     * @param device  The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice
            device, final String socketType) {
        if (D) Log.d(TAG, "connected, Socket Type:" + socketType);

        // Cancel the thread that completed the connection
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        // Cancel the accept thread because we only want to connect to one device
        if (mSecureAcceptThread != null) {
            mSecureAcceptThread.cancel();
            mSecureAcceptThread = null;
        }
//        if (mInsecureAcceptThread != null) {
//            mInsecureAcceptThread.cancel();
//            mInsecureAcceptThread = null;
//        }

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket, socketType);
        mConnectedThread.start();

        // Send the name of the connected device back to the UI Activity
        /*Message msg = mHandler.obtainMessage(BTSettings.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(BTSettings.DEVICE_NAME, device.getName());
        msg.setData(bundle);
        mHandler.sendMessage(msg);*/
        mcontext.sendBroadcast(new Intent(ACTION_BTSERVICE_DEVICE_NAME)
        .putExtra(EXTRA_BTSERVICE_DEVICE_NAME, device.getName()));

        setState(STATE_CONNECTED);
    }

    /**
     * Stop all threads
     */
    public synchronized void stop() {
        if (D) Log.d(TAG, "stop");

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if (mSecureAcceptThread != null) {
            mSecureAcceptThread.cancel();
            mSecureAcceptThread = null;
        }

//        if (mInsecureAcceptThread != null) {
//            mInsecureAcceptThread.cancel();
//            mInsecureAcceptThread = null;
//        }
        setState(STATE_NONE);
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
       // r.write(out);
    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed() {
        // Send a failure message back to the Activity
       /* Message msg = mHandler.obtainMessage(BTSettings.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(BTSettings.TOAST, "Unable to connect device");
        msg.setData(bundle);
        mHandler.sendMessage(msg);*/
    	 mcontext.sendBroadcast(new Intent(ACTION_BTSERVICE_TOAST)
         .putExtra(EXTRA_BTSERVICE_TOAST, "Unable to connect device"));
        // Start the service over to restart listening mode
        BluetoothService.this.start();
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() {
        // Send a failure message back to the Activity
        /*Message msg = mHandler.obtainMessage(BTSettings.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(BTSettings.TOAST, "Device connection was lost");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
*/
    	mcontext.sendBroadcast(new Intent(ACTION_BTSERVICE_TOAST)
          .putExtra(EXTRA_BTSERVICE_TOAST, "Device connection was lost"));
    	// Start the service over to restart listening mode
        BluetoothService.this.start();
    }

    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    private class AcceptThread extends Thread {
        // The local server socket
        private final BluetoothServerSocket mmServerSocket;
        private String mSocketType;

        public AcceptThread(boolean secure) {
            BluetoothServerSocket tmp = null;
            mSocketType = secure ? "Secure":"Insecure";

            // Create a new listening server socket
             try {
                if (secure) {
                	 Log.v("AcceptThread", "secure: "+secure);
                    //tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME_SECURE,
					tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME_SECURE,
                        MY_UUID_SECURE);
					 Log.v("AcceptThread", "tmp: "+tmp);
                } else {
                   
                }
            } catch (IOException e) {
                Log.e(TAG, "Socket Type: " + mSocketType + "listen() failed", e);
            } 
            mmServerSocket = tmp;
        }

        public void run() {
            if (D) Log.d(TAG, "Socket Type: " + mSocketType +
                    "BEGIN mAcceptThread" + this);
            setName("AcceptThread" + mSocketType);

            BluetoothSocket socket = null;

            // Listen to the server socket if we're not connected
            while (mState != STATE_CONNECTED) {
                try {
                    // This is a blocking call and will only return on a
                    // successful connection or an exception
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "Socket Type: " + mSocketType + "accept() failed", e);
                    break;
                }

                // If a connection was accepted
                if (socket != null) {
                    synchronized (BluetoothService.this) {
                        switch (mState) {
                        case STATE_LISTEN:
                        case STATE_CONNECTING:
                            // Situation normal. Start the connected thread.
                            connected(socket, socket.getRemoteDevice(),
                                    mSocketType);
                            break;
                        case STATE_NONE:
                        case STATE_CONNECTED:
                            // Either not ready or already connected. Terminate new socket.
                            try {
                                socket.close();
                            } catch (IOException e) {
                                Log.e(TAG, "Could not close unwanted socket", e);
                            }
                            break;
                        }
                    }
                }
            }
            if (D) Log.i(TAG, "END mAcceptThread, socket Type: " + mSocketType);

        }

        public void cancel() {
            if (D) Log.d(TAG, "Socket Type" + mSocketType + "cancel " + this);
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Socket Type" + mSocketType + "close() of server failed", e);
            }
        }
    }


    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {
       
        //private final BluetoothDevice mmDevice;
        private String mSocketType;

        public ConnectThread(BluetoothDevice device, boolean secure) {
            mConnectedDevice = device;
            BluetoothSocket tmp = null;
            mmSocket = null;
            mSocketType = secure ? "Secure" : "Insecure";

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                //if (secure) 
                	{
                    /* tmp = device.createRfcommSocketToServiceRecord(
                            MY_UUID_SECURE); */
                		//tmp = device.createRfcommSocketToServiceRecord(MY_UUID_SECURE);
                		tmp = device.createInsecureRfcommSocketToServiceRecord(MY_UUID_SECURE);
                } 
            } catch (IOException e) {
                Log.e(TAG, "Socket Type: " + mSocketType + "create() failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectThread SocketType:" + mSocketType);
            setName("ConnectThread" + mSocketType);

            // Always cancel discovery because it will slow down a connection
            mAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();
                
                
            } catch (IOException e) {
            	 Log.e(TAG, "connect() failed: "+ e.getMessage());
            	 e.printStackTrace();
                // Close the socket
                try {
                    mmSocket.close();
                    mConnectedDevice = null;
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() " + mSocketType +
                            " socket during connection failure", e2);
                }
                connectionFailed();
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (BluetoothService.this) {
                mConnectThread = null;
            }

            // Start the connected thread
            connected(mmSocket, mConnectedDevice, mSocketType);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect " + mSocketType + " socket failed", e);
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
       

        public ConnectedThread(BluetoothSocket socket, String socketType) {
            Log.d(TAG, "create ConnectedThread: " + socketType);
            mmSocket = socket;
            
            
        }

        public void run() {
        	boolean interrupted = false;
            Log.i(TAG, "BEGIN mConnectedThread");  
            
            try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				interrupted = true;
				e.printStackTrace();
			}
            if(!interrupted){
            	Log.i(TAG, "starting clientsession");  
            	ftpRfcommTransport = new BluetoothFtpRfcommTransport(mmSocket);
	            ftpClientSession   = new BluetoothFtpClientObexSession(mcontext, ftpRfcommTransport);            
	            mFtpData		   = new BluetoothFTPData(); 
	            ftpClientSession.start();
            }
            
            }        

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }
    
    
    public ArrayList<BluetoothFTPFileInfo> getFolderList()
    {
    	ftpClientSession.BrowseFiles(mFtpData);
    	return mFtpData.getFileList();
    }
    
    public String getMimeType(String filename)
    {
    	return mFtpData.getMimeType(filename);
    }
    
    public boolean setPathRoot()
    {
    	return ftpClientSession.NavigateForward("");
    }
    
    public boolean setPathForward(String path)
    {
    	return ftpClientSession.NavigateForward(path);
    }
    
    public boolean CreateNewFolder(String path){
    	
    	boolean flag = false;
    	flag =  ftpClientSession.CreateFolder(path);
    	if(flag)
    		setPathBackward();
    	return flag;
    }
    public boolean setPathBackward()
    {
    	return ftpClientSession.NavigateBackward();
    }
    
    public void GetFile(String fileName,String filePath, Context context)
    {
    	ftpClientSession.GetFile(fileName, filePath, mFtpData.getMimeType(fileName),context);
    }
    
    public void PutFile(FileInfo fileInfo,Context context){
    	
    	ftpClientSession.PutFile(fileInfo, context);
    }
public boolean deleteRemoteFile(String fileName,long len){
    	
    	return ftpClientSession.deleteFile(fileName,len);
    }
}
