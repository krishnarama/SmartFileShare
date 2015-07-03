package com.bluetooth.ftp;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import com.example.javax.obex.ClientOperation;
import com.example.javax.obex.ObexTransport;
import com.example.javax.obex.ClientSession;
import com.example.javax.obex.HeaderSet;
import com.example.javax.obex.ResponseCodes;
import com.android.BluetoothFTP.BluetoothService;
import com.android.BluetoothFTP.BluetoothService.FileInfo;
import com.bluetooth.utility.BluetoothFTPData;
import com.bluetooth.utility.BluetoothFTPFileInfo;
import com.bluetooth.utility.XmlParser;
import android.app.ProgressDialog;
import android.content.Context;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class BluetoothFtpClientObexSession implements BluetoothFtpObexSession{

	private static final String TAG = "BtFTP ObexClient";
    private static final boolean D = Constants.DEBUG;
    private static final boolean V = Constants.VERBOSE;
    private ObexTransport mTransport;
    private ClientSession mCs;
    private boolean mConnected = false;
    private Context mContext;
    private ClientOperation getOperation = null;
    private OutputStream outputStream = null;
    private InputStream inputStream = null;
    private Context aContext;
    private BluetoothService mBTService;
   
    
    public BluetoothFtpClientObexSession(Context context, ObexTransport transport) {
        if (transport == null) {
            throw new NullPointerException("transport is null");
        }
        mContext = context;
        mTransport = transport;
       
        mBTService = BluetoothService.getBluetoothService(context);
        
    }

    public void start() {
    	Log.d(TAG, "Start!");
        connect();

    }
    
    public ArrayList<BluetoothFTPFileInfo> BrowseFiles(BluetoothFTPData ftpData){
    	return getFolderList(ftpData);
    }

    private ArrayList<BluetoothFTPFileInfo> getFolderList(BluetoothFTPData ftpData) {
		
    	boolean error = false;  
    	HeaderSet request;
    	request = new HeaderSet();
        request.setHeader(HeaderSet.TYPE, "x-obex/folder-listing");

        try {
			getOperation = (ClientOperation)mCs.get(request);
		} catch (IOException e) {
			
			 Log.e(TAG, "Error when get HeaderSet ");
			 error = true;
			e.printStackTrace();
		}
		if (!error) {
            try {
                 Log.v(TAG, "openOutputStream ");
                //outputStream = getOperation.openOutputStream();
                inputStream = getOperation.openInputStream();
                Log.v("log", "response code is "+getOperation.getResponseCode());
                if(getOperation.getResponseCode() == ResponseCodes.OBEX_HTTP_OK || 
                		getOperation.getResponseCode() == ResponseCodes.OBEX_HTTP_CONTINUE){
                XmlParser xp = new XmlParser(ftpData);
				xp.Parse(inputStream);					
                }
				
            } catch (IOException e) {
                Log.e(TAG, "Error when openOutputStream");
                error = true;
            }
        }
		
		
		try {
			if(inputStream != null){
				inputStream.close();
			}
			if(outputStream != null){
				outputStream.close();
			}
			getOperation.close();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		
		return ftpData.getFileList();
	}

    public void GetFile(String fileName, String filePath, String MimeType, Context ActivityContext){
    	
    	if((fileName == null) || (MimeType == null)){
    		throw new NullPointerException("filename and Mimetype can not be null");
    	}
    	aContext = ActivityContext;
    	recieveFile(fileName,filePath,MimeType);
    }
    
    public void PutFile(FileInfo fileInfo, Context ActivityContext){
    	
    	if((fileInfo == null)){
    		throw new NullPointerException("filename and Mimetype can not be null");
    	}
    	aContext = ActivityContext;
    	uploadFile(fileInfo.FileName,fileInfo.length,fileInfo.iStream);
    }
    
    private void uploadFile(String fileName, int length, InputStream istream) {
		
    	new upLoadFileTask(fileName,length,istream).execute(fileName);
		
	}

	private void recieveFile(String Filename, String filePath, final String fileType)
    {
    	
    	new ReceiveFileTask(Filename).execute(Filename,fileType,filePath);
    }
    
    class ReceiveFileTask extends AsyncTask<String, Integer, Long>{
    		
    	String Filename;
    	String fileType;
    	long pos = 0;
    	ProgressDialog progressDialog;
    	private ClientOperation getOp = null;
    	OutputStream outputstream;
    	InputStream inputStream ;
    	
    	public ReceiveFileTask(String filename) {
			Filename = filename;
		}
		@Override
		protected Long doInBackground(String... params) {
			
			HeaderSet request;
	    	request = new HeaderSet();
	    	
	    	try {
				mCs.ensureOpen();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				connect();
			}
	    	Filename = params[0];
	    	fileType = params[1];
	        request.setHeader(HeaderSet.NAME, Filename.trim());
	        //request.setHeader(HeaderSet.TYPE, fileType.trim());
	        boolean error = false;
	       // Log.v("log", "filename is :"+ Filename + fileType);
	        try {
	        	getOp = (ClientOperation)mCs.get(request);
	        	Log.v("log","response "+getOp.getResponseCode());
	        	//outputstream = getOp.openOutputStream();
	        	inputStream = getOp.openInputStream();
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				 Log.e(TAG, "Error when get HeaderSet ");
				 error = true;
				e.printStackTrace();
			}
			try {
				Log.v("log","response "+getOp.getResponseCode());
				if(getOp.getResponseCode() == ResponseCodes.OBEX_HTTP_OK ||
						getOp.getResponseCode() == ResponseCodes.OBEX_HTTP_CONTINUE)
				{
					long len = getOp.getLength();
					
					byte[] b = new byte[getOp.getMaxPacketSize()];
					final File f = new File(params[2] + "/"+ Filename);
					FileOutputStream fos = new FileOutputStream(f,true);
					Log.v("receive File", "file len is: " + len);
					while( pos < len ){
						
						int readLen = inputStream.read(b, 0, getOp.getMaxPacketSize());
						pos = pos + readLen;
						fos.write(b, 0, readLen);
						Log.v("log", "readLen is: " + readLen);
						Log.v("log", "pos is: " + pos);
						publishProgress((int)(((float)pos/len)* 100) );
						Log.v("log","progress :" + (int)(((float)pos/len)* 100));
					}
					
					if(pos == len){
						MediaScannerConnectionClient mediascanner = new MediaScannerConnectionClient() {
							
							private MediaScannerConnection msc = null;
							{
								msc = new MediaScannerConnection(mContext,this);
								msc.connect();
							}

							public void onScanCompleted(String path, Uri uri) {
								msc.disconnect();
								
							}
							

							public void onMediaScannerConnected() {
								Log.v("log", f.getPath());
								msc.scanFile(f.getPath(),fileType );
								
							}
						};
						
						
					}
					
					
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally{
				try {
					if(inputStream != null)
						inputStream.close();
					getOp.close();
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return pos;
		}

		@Override
		protected void onPostExecute(Long result) {
			
			progressDialog.dismiss();
			Toast.makeText(mContext, "Received file: "+Filename, Toast.LENGTH_SHORT).show();
			aContext = null;
			
			Message msg = mBTService.ftpHandler.obtainMessage();
			msg.what = 1;
			msg.sendToTarget();
		}

		@Override
		protected void onPreExecute() {

			progressDialog = new ProgressDialog(aContext);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			progressDialog.setMessage("Receiving " + Filename + "...");
			progressDialog.setCancelable(false);
			progressDialog.show();
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			progressDialog.setProgress(values[0]);
		}
    	
	}
    
    class upLoadFileTask extends AsyncTask<String,Integer,Long>{

    	private String filename;
    	private int len;
    	ClientOperation putOperation = null;
    	OutputStream outputStream = null;
    	InputStream inputStream = null,inStream = null;
    	boolean error = false;
    	int responseCode;
    	ProgressDialog progressDialog = new ProgressDialog(aContext);
    	
    	public upLoadFileTask(String fileName, int Length, InputStream iStream) {
			filename = fileName;
			len = Length;
			inputStream = iStream;
		}
    	
    	@Override
		protected void onPostExecute(Long result) {
			
			progressDialog.dismiss();
			Toast.makeText(mContext, "Uploaded file: "+filename, Toast.LENGTH_SHORT).show();
			aContext = null;
			
			Message msg = mBTService.ftpHandler.obtainMessage();
			msg.what = 2;
			msg.sendToTarget();
		}
    	
		@Override
		protected Long doInBackground(String... params) {
			
			HeaderSet reqHeader;
			reqHeader = new HeaderSet();
			
			
			
			reqHeader.setHeader(HeaderSet.NAME, filename);
			reqHeader.setHeader(HeaderSet.LENGTH, (long)len);
			Log.v("log", "mimetype is: "+ mBTService.getMimeType(filename));
			reqHeader.setHeader(HeaderSet.TYPE, mBTService.getMimeType(filename));
			
		
		try{
			try {
				putOperation = (ClientOperation)mCs.put(reqHeader);
				outputStream = putOperation.openOutputStream();
				inStream = putOperation.openInputStream();
				
			} catch (IOException e) {
				error = true;
				e.printStackTrace();
			}
			
			if(!error){
				  int position = 0;
	              int readLength = 0;
	              boolean okToProceed = false;
	              long timestamp = 0;
	              int outputBufferSize = putOperation.getMaxPacketSize();
	              byte[] buffer = new byte[outputBufferSize];
	              BufferedInputStream a = new BufferedInputStream(inputStream, 0x4000);
	              
	              if (position != len) {
                      try {
						readLength = a.read(buffer, 0, outputBufferSize);
						Log.v("log", "readlen is " + readLength);
						outputStream.write(buffer, 0, readLength);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	              }
	          
                  position += readLength;
                  
                  
                  try {
					if (putOperation.getResponseCode() == ResponseCodes.OBEX_HTTP_CONTINUE
					          || putOperation.getResponseCode() == ResponseCodes.OBEX_HTTP_OK) {
					      Log.v(TAG, "Remote accept");
					      okToProceed = true;
					     
					  } else {
					      Log.v(TAG, "Remote reject, Response code is " + putOperation.getResponseCode());
					  }
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
                  
                  while (okToProceed && (position != len)) {
                      {
                          if (V) timestamp = System.currentTimeMillis();

                          try {
							readLength = a.read(buffer, 0, outputBufferSize);
							publishProgress((int)(((float)position/len)* 100) );
							outputStream.write(buffer, 0, readLength);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
                         

                          /* check remote abort */
                          try {
							responseCode = putOperation.getResponseCode();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
                          if (V) Log.v(TAG, "Response code is " + responseCode);
                          if (responseCode != ResponseCodes.OBEX_HTTP_CONTINUE
                                  && responseCode != ResponseCodes.OBEX_HTTP_OK) {
                              /* abort happens */
                              okToProceed = false;
                          } else {
                              position += readLength;
                              if (V) {
                                  Log.v(TAG, "Sending file position = " + position
                                          + " readLength " + readLength + " bytes took "
                                          + (System.currentTimeMillis() - timestamp) + " ms");
                              }
                              
                          }
                      }
                  }
                  
                  if (responseCode == ResponseCodes.OBEX_HTTP_FORBIDDEN
                          || responseCode == ResponseCodes.OBEX_HTTP_NOT_ACCEPTABLE) {
                      Log.i(TAG, "Remote reject file " + filename + " length "
                              + len);
                      
                  } else if (responseCode == ResponseCodes.OBEX_HTTP_UNSUPPORTED_TYPE) {
                      Log.i(TAG, "Remote reject file type ");
                      
                  } else if ( position == len) {
                      Log.i(TAG, "SendFile finished send out file " + filename
                              + " length " + len);
                      try {
						outputStream.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
                  } else {
                      error = true;
                      
                      try {
						putOperation.abort();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
                      
                  }
              }
		}finally {
            try {
                inputStream.close();
                if (!error) {
                    responseCode = putOperation.getResponseCode();
                    if (responseCode != -1) {
                        if (V) Log.v(TAG, "Get response code " + responseCode);
                        if (responseCode != ResponseCodes.OBEX_HTTP_OK) {
                            Log.i(TAG, "Response error code is " + responseCode);
                            
                        }
                    } else {
                        // responseCode is -1, which means connection error
                        error = true;
                    }
                }

                if (inStream != null) {
                	inStream.close();
                }
                if (outputStream != null){
                	outputStream.close();
                }
                if (putOperation != null) {
                    putOperation.close();
                }
            } catch (IOException e) {
                Log.e(TAG, "Error when closing stream after send");
            }
        }
		
	
		return (long) len;
    	
    }
		@Override
		protected void onPreExecute() {

			
			progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			//progressDialog.setpr
			progressDialog.setMessage("Uploading " + filename + "...");
			progressDialog.setCancelable(false);
			progressDialog.show();
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			progressDialog.setProgress(values[0]);
		}
    }
    public boolean NavigateForward(String path){
    	if(path == null)
    		throw new NullPointerException("Path can not be null");
    	
    	return setPath(path, false, false);
    }
    
    public boolean NavigateBackward(){
    	return setPath(null, true, false);
    }
    
    public boolean CreateFolder(String path){
    	
    	return setPath(path,false,true);
    }
       public boolean deleteFile(String fileName,long len){
    	   InputStream in = null;
    	   OutputStream out = null;
    	   ClientOperation putOperation = null;
    	   
    	   try {
			mCs.ensureOpen();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			connect();
		}
    	if(fileName!= null){
	    	HeaderSet hs = new HeaderSet();
	    	
	    	hs.setHeader(HeaderSet.NAME, fileName);
	    	hs.setHeader(HeaderSet.LENGTH, len);
	    	
	    	try {
				HeaderSet rHs = mCs.delete(hs);
	    		 //putOperation = (ClientOperation)mCs.put(hs);
	    		//in = putOperation.openInputStream();
	    		//out = putOperation.openOutputStream();
	    		int rsp =rHs.responseCode;
	    		Log.v("deleteRemoteFile", "delete : " + rsp);
				if( rsp == ResponseCodes.OBEX_HTTP_OK){
					Log.v("deleteRemoteFile", "delete success: " + fileName);
					return true;
				}
			} catch (IOException e) {
				Log.v("deleteRemoteFile", "delete failure: " + fileName);
				
				e.printStackTrace();
			}finally{
				/*try {
					//putOperation.close();
					//in.close();
					//out.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}*/
			}
    	}
    	return false;
    }
	private boolean setPath(String name, boolean backup, boolean create) {
		
		//Assert.assertNotNull(name);
		//connect();
		try {
			mCs.ensureOpen();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			connect();
		}
		HeaderSet hs = new HeaderSet();
		
		if(name != null) // forward
		{
			hs.setHeader(HeaderSet.NAME, name);
		}
						
		try {
			HeaderSet rHs = mCs.setPath(hs, backup, create);
			Log.v("setpath", "setpath command:  " + rHs.responseCode);
			if(rHs.responseCode == ResponseCodes.OBEX_HTTP_OK){
				Log.v("setpath", "setpath forward success " + name);
				return true;
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return false;
}

	public void stop() {
        if (D) Log.d(TAG, "Stop!");
        disconnect();
    }

     private void disconnect() {
         try {
             if (mCs != null) {
                 mCs.disconnect(null);
             }
             mCs = null;
             if (D) Log.d(TAG, "OBEX session disconnected");
         } catch (IOException e) {
             Log.w(TAG, "OBEX session disconnect error" + e);
         }
         try {
             if (mCs != null) {
                 if (D) Log.d(TAG, "OBEX session close mCs");
                 mCs.close();
                 if (D) Log.d(TAG, "OBEX session closed");
                 }
         } catch (IOException e) {
             Log.w(TAG, "OBEX session close error" + e);
         }
         if (mTransport != null) {
             try {
                 mTransport.close();
             } catch (IOException e) {
                 Log.e(TAG, "mTransport.close error");
             }

         }
     }

     private void connect() {
         if (D) Log.d(TAG, "Create ClientSession with transport " + mTransport.toString());
         try {
             mCs = new ClientSession(mTransport);
            
             mConnected = true;
             Log.v(TAG, "Create ClientSession with transport " + mTransport.toString());
         } catch (IOException e1) {
             Log.e(TAG, "OBEX session create error");
         }
         if (mConnected) {
             mConnected = false;
             HeaderSet hs = new HeaderSet();
             byte[] FTP_UUID = {(byte) 0xF9,(byte) 0xEC,0x7B,(byte) 0xC4,(byte) 0x95,0x3C,0x11,(byte) 0xD2,(byte) 0x98,0x4E,0x52,0x54,0x00,(byte) 0xDC,(byte) 0x9E,0x09};
             hs.setHeader(HeaderSet.TARGET, FTP_UUID);
             
             byte[] FTP_WHO = {'F','T','P'};
             hs.setHeader(HeaderSet.WHO, FTP_WHO);
             try {
                 mCs.connect(hs);
                  Log.v(TAG, "OBEX session created");
                 mConnected = true;
             } catch (IOException e) {
                 Log.e(TAG, "OBEX session connect error");
                 e.printStackTrace();
             }
         }
        
     }


	public void unblock() {
		// TODO Auto-generated method stub
		
	}
}