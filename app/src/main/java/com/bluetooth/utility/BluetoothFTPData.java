package com.bluetooth.utility;

import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.ArrayList;


public class BluetoothFTPData {
	
	public ArrayList<BluetoothFTPFileInfo> mFileList;
	
	
	public BluetoothFTPData(){
		
		mFileList = new ArrayList<BluetoothFTPFileInfo>();
	}

	
	public String getMimeType(String filename){
		
		FileNameMap filemap = URLConnection.getFileNameMap();		
		
		return filemap.getContentTypeFor(filename);
		
	}
	
	public ArrayList<BluetoothFTPFileInfo> getFileList(){
		
		return mFileList;
	}
}
