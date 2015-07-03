package com.bluetooth.utility;

import java.io.IOException;
import java.io.InputStream;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import android.util.Log;



public class XmlParser {

	SAXParser saxparser ;
	BluetoothFTPData mFtpData = null;
	public static final String FOLDER_TAG = "folder";
	public static final String FILE_TAG = "file";
	
	public XmlParser(BluetoothFTPData ftpData) {
		
		mFtpData = ftpData;
	
		try {
			saxparser = SAXParserFactory.newInstance().newSAXParser();
		} catch (ParserConfigurationException e) {
			
			e.printStackTrace();
		} catch (SAXException e) {
			
			e.printStackTrace();
		}
		
		
	}
	
	public void Parse(InputStream inputStream) throws IOException{
		
		if(inputStream == null)
			throw new IOException("Inputstream is null");
		
		
		try {
			saxparser.parse(inputStream, new XmlDefaultHandler());
		
		} catch (SAXException e) {
			
			e.printStackTrace();
		}
		
	}

	class XmlDefaultHandler extends DefaultHandler{
		
		public XmlDefaultHandler() {
			super();
		if(mFtpData.mFileList.size()!= 0){
			mFtpData.mFileList.clear();
			}
			
		}
	
		@Override
		public void characters(char[] ch, int start, int length)
				throws SAXException {
			
			super.characters(ch, start, length);
		}
	
		@Override
		public void startDocument() throws SAXException {
		
			super.startDocument();
			Log.v("log", "inside start document ");
		}
		@Override
		public void endDocument() throws SAXException {
			
			Log.v("log", "inside end document " +mFtpData.mFileList.size());
			for(int i = 0; i < mFtpData.mFileList.size();i++){
				
				Log.v("log", mFtpData.mFileList.get(i).getFileName()
					+	mFtpData.mFileList.get(i).getSize()
					+   mFtpData.mFileList.get(i).isFile());
			}
			
		}
	
		@Override
		public void endElement(String uri, String localName, String qName)
				throws SAXException {
			
			super.endElement(uri, localName, qName);
			Log.v("log", "inside end element" + qName);
			if(qName.equalsIgnoreCase("folder-listing")){
				Log.v("log", "inside end element" + qName);
			}
		}
	
		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			
			Log.v("log", "inside start element");
			
			BluetoothFTPFileInfo fileInfo = null;
			
			if(qName.equalsIgnoreCase(XmlParser.FILE_TAG)){
				fileInfo = new BluetoothFTPFileInfo();
				fileInfo.setFileName(attributes.getValue("name"));
				fileInfo.setSize(Integer.parseInt(attributes.getValue("size")));
				fileInfo.setFile(true);
			}else if(qName.equalsIgnoreCase(XmlParser.FOLDER_TAG)){
					fileInfo = new BluetoothFTPFileInfo();			
					fileInfo.setFileName(attributes.getValue("name"));
					fileInfo.setFile(false);
			}
			
			
			if(fileInfo != null){
				Log.v("log", fileInfo.fileName);
				if(mFtpData.mFileList.add(fileInfo))
					Log.v("log","file list size is: " +mFtpData.mFileList.size() );
			}
		}
		
	}

}