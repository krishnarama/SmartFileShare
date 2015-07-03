package com.bluetooth.utility;

public class BluetoothFTPFileInfo {
	
	protected String  fileName;
	protected long    size;
	protected long    date;
	protected String  mimeType;
	protected boolean isFile;
	
	public String getFileName() {
		return fileName;
	}
	protected void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public long getSize() {
		return size;
	}
	protected void setSize(long size) {
		this.size = size;
	}
	protected long getDate() {
		return date;
	}
	protected void setDate(long date) {
		this.date = date;
	}
	protected String getMimeType() {
		return mimeType;
	}
	protected void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}
	public boolean isFile() {
		return isFile;
	}
	protected void setFile(boolean isFile) {
		this.isFile = isFile;
	}
	
}
