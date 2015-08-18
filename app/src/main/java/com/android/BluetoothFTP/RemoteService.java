package com.android.BluetoothFTP;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

/**
 * Created by siva.r on 7/16/2015.
 */
public class RemoteService extends Service {

    private FTPClientUtils mFTPClientUtils;
    @Override
    public void onCreate() {
        super.onCreate();
        mFTPClientUtils = new FTPClientUtils();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    public class LocalBinder extends Binder {

        public RemoteService getRemoteService(){
            return RemoteService.this;
        }
    }

    public FTPClientUtils getFTPClient(){
      return  mFTPClientUtils;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new LocalBinder();
    }
}
