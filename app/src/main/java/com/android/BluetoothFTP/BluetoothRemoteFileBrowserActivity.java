package com.android.BluetoothFTP;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.EmptyStackException;

import com.bluetooth.utility.BluetoothFTPFileInfo;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.DragEvent;

public class BluetoothRemoteFileBrowserActivity extends Activity implements OnItemClickListener, OnClickListener {
	
    private static final String LOGTAG = "FTPFileListBrowser";
    private static final String FOLDERMT = "Folder is empty";
    private static final String REMOTE_ROOT_PATH = "/";
    private static final int LOCAL_LISTID = 1;
    private static final int REMOTE_LISTID = 2;
    public static final int FTP_GET = 1;
    public static final int FTP_PUT = 2;
    private String SDCardPath;
    private String localCurrentFilePath;
    private String remoteCurrentFilePath;
    private File localCurrentDir;
    private BluetoothFTPFileInfo remoteCurrentDir;
    private File[] localFileList;
    private ArrayList<BluetoothFTPFileInfo> remoteFileList;
    private ListView localFileListView;
    private ListView remoteFileListView;
    private FileListAdapter localFileAdapt;
    private RemoteFileListAdapter remoteFileAdapt;
    private TextView tvSrcPath;
    private TextView tvDstPath;
    private TextView tvMyDevice;
    private TextView tvRemoteDevice;
    private ImageView srcParentButton;
    private ImageView dstParentButton;
    private ImageView locNewFolder;
    private ImageView rmtNewFolder;
    private LinearLayout srcLinearLayout;
    private LinearLayout dstLinearLayout;
    private TextView srcMtFolder;
    private TextView dstMtFolder;
    private BluetoothService mBTService;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice  mBluetoothDevice;
    private File sdCard;
    private String newFolderName;
    private String newFileName;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main_test);
        Log.v(LOGTAG, "onCreate");
    	SDCardPath = Environment.getExternalStorageDirectory().getPath();
    	localCurrentDir = Environment.getExternalStorageDirectory();
    	srcLinearLayout = (LinearLayout)findViewById(R.id.srcSubLayout);
    	dstLinearLayout = (LinearLayout)findViewById(R.id.dstSubLayout);
    	mBTService = BluetoothService.getBluetoothService(getApplicationContext());
    	mBTService.ftpHandler = FileReceiveHandler;
    	localCurrentFilePath = SDCardPath;
		sdCard = new File(localCurrentFilePath);
		localFileList = sdCard.listFiles();
		
    	loadLocalFiles();
    	//loadRemoteFiles();
    	tvSrcPath = (TextView)findViewById(R.id.srcCurrentPath);
    	tvSrcPath.setText(localCurrentFilePath);
    	tvDstPath = (TextView)findViewById(R.id.dstCurrentPath);
    	tvDstPath.setText(remoteCurrentFilePath);
    	srcParentButton = (ImageView)findViewById(R.id.upSrcButton);
    	srcParentButton.setOnClickListener(this);
    	dstParentButton = (ImageView)findViewById(R.id.upDstButton);
    	dstParentButton.setOnClickListener(this);
    	locNewFolder = (ImageView)findViewById(R.id.locfolder);
    	locNewFolder.setOnClickListener(this);
    	rmtNewFolder = (ImageView)findViewById(R.id.rmtfolder);
    	rmtNewFolder.setOnClickListener(this);
    	
    	//mBluetoothAdapter = mBTService.getBluetoothAdapter();
    	tvMyDevice = (TextView)findViewById(R.id.tvMyDevice);
    	//tvMyDevice.setText("My Device:" + mBluetoothAdapter.getName());

    	//mBluetoothDevice = mBTService.getConnectedDevice();
    	tvRemoteDevice = (TextView)findViewById(R.id.tvRemoteDevice);
    	//tvRemoteDevice.setText("Remote Device:" + mBluetoothDevice.getName());
    	
    	/*Intent in = new Intent(getApplicationContext(),MyService.class);
    	bindService(in,null,BIND_AUTO_CREATE);*/
    }
    
    @Override
    protected void onStart() {
    	// TODO Auto-generated method stub
    	super.onStart();
    	Log.v(LOGTAG, "onStart");
    }
    
    @Override
    protected void onResume() {
    	// TODO Auto-generated method stub
    	super.onResume();
    	Log.v(LOGTAG, "onResume");
    }

    private void loadLocalFiles() {
		
    	srcMtFolder = new TextView(this);
    	//srcLinearLayout.addView(srcMtFolder);
    	
    	srcMtFolder.setGravity(Gravity.AXIS_Y_SHIFT);
    	if(localFileList.length == 0)
    	{
    		
    		srcMtFolder.setText(FOLDERMT);
    		srcMtFolder.setGravity(Gravity.CENTER);
    		
    	}
    	else{
    		//localFileList = (ListView)findViewById(R.id.srcListview);
    		localFileListView = new ListView(this);
    		localFileListView.setId(LOCAL_LISTID);
    		srcLinearLayout.addView(localFileListView);
    		localFileAdapt = new FileListAdapter(this);
    		localFileAdapt.setData(localFileList);
        	localFileListView.setAdapter(localFileAdapt);
        	localFileListView.setOnItemClickListener(this);
        	
    	}
    	
    	//registerForContextMenu(localFileListView);
    	localFileListView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View view,
					int position, long arg3) {
				
				ClipData data = ClipData.newPlainText("FileName", localFileList[position].getName());
				dstLinearLayout.setOnDragListener(new FTPDragListener(FTP_PUT,position,dstLinearLayout));
				srcLinearLayout.setOnDragListener(null);
				view.startDrag(data, new MyShadowBuilder(view), null, 0);
				return true;
			}   	
    	
		});
    	
    	   	
	}
    
      
    
    class FTPDragListener implements View.OnDragListener {
    	int DragOperation;
    	int Position;
    	LinearLayout ll;
    	public FTPDragListener(int Operation,int position, LinearLayout l) {
			DragOperation = Operation;
			Position = position;
			ll = l;
		}

    	@Override
		public boolean onDrag(View v, DragEvent event) {
			
			switch(event.getAction()){
			case DragEvent.ACTION_DRAG_STARTED:
				Log.v("log", "drag startd");
				
				break;
			case DragEvent.ACTION_DRAG_ENTERED:
				ll.setBackgroundResource(R.drawable.highlighter);
				Log.v("log", "drag entered");
				break;
			case DragEvent.ACTION_DROP:
				Log.v("log", "drag dropped");
				{
					ll.setBackgroundResource(R.drawable.boarder);
					final ClipData data = event.getClipData();
					ClipData.Item item = data.getItemAt(0);
					String text = item.coerceToText(v.getContext()).toString();
					
					if(DragOperation == FTP_GET)
						mBTService.GetFile(text,localCurrentFilePath,BluetoothRemoteFileBrowserActivity.this);
					else{
						//Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
						mBTService.fileInfo = mBTService.new FileInfo();
						mBTService.fileInfo.FileName = text;
						mBTService.fileInfo.length = (int) localFileList[Position].length();
						
						try {
							mBTService.fileInfo.iStream = new FileInputStream(localFileList[Position].getPath().toString());
							Log.v("log", "filename is: "+ localFileList[Position].getPath().toString());
						} catch (FileNotFoundException e) {
							
							e.printStackTrace();
						}
						mBTService.PutFile(mBTService.fileInfo, BluetoothRemoteFileBrowserActivity.this);
					}
						
				}
				return (true);
				//break;
			case DragEvent.ACTION_DRAG_EXITED:
				Log.v("log", "drag exited");
				break;
				
			}
			return true;
		}
    	
    }
   
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
    		ContextMenuInfo menuInfo) {
    	
    	menu.setHeaderIcon(android.R.drawable.dark_header);
    	menu.setHeaderTitle("My List");
    	
    	switch(v.getId()){
    	case LOCAL_LISTID:
    		menu.add("New Folder");
    		break;
    	case REMOTE_LISTID:
    		menu.add("Download File");
    		break;
    	}

    	
    	
    	
    }
    
    private void loadRemoteFiles()
    {
    	dstMtFolder = new TextView(this);
    	//dstLinearLayout.addView(dstMtFolder);
    	mBTService.setPathRoot();
    	remoteFileList = mBTService.getFolderList();
    	remoteCurrentFilePath = REMOTE_ROOT_PATH;
    	if(remoteFileList.size() == 0)
    	{
    		
    		dstMtFolder.setText(FOLDERMT);
    		dstMtFolder.setGravity(Gravity.CENTER_VERTICAL);
    		
    		Log.v(LOGTAG, "remote list0");
    	}
    	else{
    		Log.v(LOGTAG, "remote list");
    		//localFileList = (ListView)findViewById(R.id.srcListview);
    		remoteFileListView = new ListView(this);
    		remoteFileListView.setId(REMOTE_LISTID);
    		dstLinearLayout.addView(remoteFileListView);
        	remoteFileAdapt = new RemoteFileListAdapter(this);
        	remoteFileAdapt.setData(remoteFileList);
        	remoteFileListView.setAdapter(remoteFileAdapt);
        	remoteFileListView.setOnItemClickListener(this);
    	}
    	
    	//registerForContextMenu(remoteFileListView);
    	
    	remoteFileListView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View view,
					int position, long arg3) {
				
				ClipData data = ClipData.newPlainText("FileName", remoteFileList.get(position).getFileName());
				srcLinearLayout.setOnDragListener(new FTPDragListener(FTP_GET,0,srcLinearLayout));
				dstLinearLayout.setOnDragListener(null);
				view.startDrag(data, new MyShadowBuilder(view), null, 0);
				return true;
			}   	   	
    	
		});
    	
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	// TODO Auto-generated method stub
    	//return super.onCreateOptionsMenu(menu);
    	//menu.add(0, 1, 0, "Grid View");
    	menu.add(0, 1, 0, "Browser Remote Files");
    	return true;
    }
	
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {

    	switch(item.getItemId())
    	{
    		/*case 1:
    		{
	    		 Log.v(LOGTAG, "Menu item Grid selected");
	    		 Intent startGrid = new Intent(getApplicationContext(),BluetoothFTPFileBrowserGridActivity.class);
	    		 startActivity(startGrid);
	    		 return true;
    		}*/
    		case 1:
    		{
    			Log.v(LOGTAG, "Menu item 'Browser Remote Files' selected");
    			loadRemoteFiles();
    		}
    		break;
    	}
    	Log.v(LOGTAG, "default menu handle");
    	return super.onMenuItemSelected(featureId, item);
    }
    	


	private void updateLocalFileList(final int position){
		
		File[] tempFileList = null;
		if(localFileList[position].isDirectory() ==  true)
		{
			Log.v(LOGTAG, "isDirectory == true");
			File tempFile = localFileList[position];
			if(tempFile.canRead())
			{
				localCurrentDir = tempFile;
				localCurrentFilePath = tempFile.getAbsolutePath();
				tvSrcPath.setText(localCurrentFilePath);
				//srcPath.setMovementMethod(new ScrollingMovementMethod());
				tempFileList = localFileList[position].listFiles();
				Log.v(LOGTAG, "No of files: "+tempFileList.length);
				if(tempFileList.length == 0)
				{
					localFileListView.setVisibility(View.GONE);
					if(srcMtFolder == null)
					{
						//srcMtFolder = new TextView(this);
						//srcLinearLayout.addView(srcMtFolder);
					}
					else if(!srcMtFolder.isShown())
					{
						//srcLinearLayout.addView(srcMtFolder);
						srcMtFolder.setVisibility(View.VISIBLE);
					}
					srcMtFolder.setText(FOLDERMT);
					srcMtFolder.setGravity(Gravity.CENTER_VERTICAL);
				}
				else
				{
					localFileList = tempFileList;
					localFileAdapt.setData(tempFileList);
					localFileAdapt.notifyDataSetChanged();
				}
				
			}
			else
			{
				Log.v(LOGTAG, "Folder: "+ tempFile.getName()+ " is not readable!!" );
			}
		}else{
			/*// Launch File Dailog
			//final CharSequence[] options = {"Delete File", "Rename File"};
			final CharSequence[] options = {"Delete File"};
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Choose an option");
			builder.setItems(options, new DialogInterface.OnClickListener() {
			    public void onClick(DialogInterface dialog, int item) {
			        Toast.makeText(getApplicationContext(), options[item], Toast.LENGTH_SHORT).show();
			        File tempFile = localFileList[position];
			        switch(item)
			        {
			        case 0: //Delete File
			        	deleteLocalFile(tempFile);
			        	break;
			        case 1: //Rename File
			        	renameLocalFile(tempFile);
			        	break;
			       default:
			    	   break;
			        }
			    }
			});
			AlertDialog alert = builder.create();
			alert.show();
			Log.v(LOGTAG, "File Dailog" );*/
		}
		
		
	}
	private void deleteLocalFile(File file)
	{
		if(file.exists())
		{
			String fileName = file.getName();
			if(file.delete())
			{
				Log.v(LOGTAG, "File :"+fileName+" deleted successfully" );
				 Toast.makeText(getApplicationContext(), "File :"+fileName+" deleted successfully" , Toast.LENGTH_SHORT).show();
				File[] tempFileList = localCurrentDir.listFiles();
				if(tempFileList.length > 0)
				{
					localFileList = tempFileList;
					localFileAdapt.setData(tempFileList);
					localFileAdapt.notifyDataSetChanged();
				}else{
					Log.v(LOGTAG, "Deleted success : folder Empty" );
					localFileListView.setVisibility(View.GONE);
					if(srcMtFolder == null)
					{
						srcMtFolder = new TextView(this);
						srcLinearLayout.addView(srcMtFolder);
					}
					else if(!srcMtFolder.isShown())
					{
						srcMtFolder.setVisibility(View.VISIBLE);
					}
					srcMtFolder.setText(FOLDERMT);
					//srcMtFolder.setGravity(Gravity.CENTER_HORIZONTAL);
				}

			}
			else
			{
				Log.v(LOGTAG, "File :"+fileName+" error in deletion" );
			}
		}
	}
	@Override
	public void onItemClick(AdapterView<?> parent, View view, final int position,
			long id) {
		
		
		Log.v(LOGTAG, "onItemClick: pos: "+position);
		int viewId = parent.getId();
		if(viewId == LOCAL_LISTID)
		{
			updateLocalFileList(position);
		}
		else
		{
			ArrayList<BluetoothFTPFileInfo> tempFileList = null;
			if(remoteFileList.get(position).isFile() ==  false)
			{
				Log.v(LOGTAG, "isRemoteDirectory == true");
				BluetoothFTPFileInfo tempFile = remoteFileList.get(position);
				//if(tempFile.canRead())
				{
					remoteCurrentDir = tempFile;
					remoteCurrentFilePath = remoteCurrentFilePath + tempFile.getFileName() + "/";
					tvDstPath.setText(remoteCurrentFilePath);
					//srcPath.setMovementMethod(new ScrollingMovementMethod());
					mBTService.setPathForward(remoteCurrentDir.getFileName());
					tempFileList = mBTService.getFolderList();
					Log.v(LOGTAG, "No of files: "+tempFileList.size());
					if(tempFileList.size() == 0)
					{
						remoteFileListView.setVisibility(View.GONE);
						if(dstMtFolder == null)
						{
							//dstMtFolder = new TextView(this);
							//dstLinearLayout.addView(dstMtFolder);
						}
						else if(!dstMtFolder.isShown())
						{
							//dstLinearLayout.addView(dstMtFolder);
							dstMtFolder.setVisibility(View.VISIBLE);
						}
						dstMtFolder.setText(FOLDERMT);
						dstMtFolder.setGravity(Gravity.CENTER_VERTICAL);
					}
					else
					{
						remoteFileList = tempFileList;
						remoteFileAdapt.setData(tempFileList);
						remoteFileAdapt.notifyDataSetChanged();
					}
					
				}
			}else{
				//final CharSequence[] options = {"Delete File", "Rename File"};
				final CharSequence[] options = {"Delete File"};
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("Choose an option");
				builder.setItems(options, new DialogInterface.OnClickListener() {
				    public void onClick(DialogInterface dialog, int item) {
				        Toast.makeText(getApplicationContext(), options[item], Toast.LENGTH_SHORT).show();
				        BluetoothFTPFileInfo tempFile = remoteFileList.get(position);
				        switch(item)
				        {
				        case 0: //Delete File
				        	deleteRemoteFile(tempFile.getFileName(),tempFile.getSize());
				        	break;
				        case 1: //Rename File
				        	//renameRemoteFile(tempFile.getFileName());
				        	break;
				       default:
				    	   break;
				        }
				    }


				});
				AlertDialog alert = builder.create();
				alert.show();
				Log.v(LOGTAG, "File Dailog" );
			}
		}
	}

	private void deleteRemoteFile(String fileName,long len) {
		
		mBTService.deleteRemoteFile(fileName,len);
	}
	private void updateLocalFileListonBack(){
		
		File tempParent = localCurrentDir.getParentFile();
		String tempPath = tempParent.getAbsolutePath();
		if(!tempPath.equals("/mnt"))
		{
			localCurrentDir = tempParent;
			localCurrentFilePath = tempParent.getAbsolutePath();
			tvSrcPath.setText(localCurrentFilePath);
			File[] tempFiles = localCurrentDir.listFiles();
			if(!localFileListView.isShown())
			{
				srcMtFolder.setVisibility(View.GONE);
				localFileListView.setVisibility(View.VISIBLE);
			}
			localFileList = tempFiles;
			localFileAdapt.setData(tempFiles);
			localFileAdapt.notifyDataSetChanged();
		}
		else
		{
			Toast.makeText(getApplicationContext(), "Not allowed", Toast.LENGTH_SHORT).show();
		}
	}
	@Override
	public void onClick(View v) {
		final int id = v.getId();
		switch(v.getId())
		{
		case R.id.upSrcButton:
			{
				Log.v(LOGTAG, "upSrcButton is clicked");
				updateLocalFileListonBack();
				
			}
			break;
		case R.id.upDstButton:
			{
				Log.v(LOGTAG, "upDstButton is clicked");
				mBTService.setPathBackward();
				String pathString[] = remoteCurrentFilePath.split("/");
				remoteCurrentFilePath = null;
				int len = pathString.length;
				StringBuilder tempPath = new StringBuilder();
				int i=0;
				while(len - 1 > 0){
					
				tempPath.append(pathString[i] + "/");
				i++;
				len--;
				}
				remoteCurrentFilePath = tempPath.toString();
				tvDstPath.setText(remoteCurrentFilePath);
				Log.v("log", "Path is: " +remoteCurrentFilePath );
				ArrayList<BluetoothFTPFileInfo> tempFileList = mBTService.getFolderList();
				if(!remoteFileListView.isShown())
				{
					dstMtFolder.setVisibility(View.GONE);
					remoteFileListView.setVisibility(View.VISIBLE);
				}
				remoteFileList = tempFileList;
				remoteFileAdapt.setData(tempFileList);
				remoteFileAdapt.notifyDataSetChanged();

			}
			break;
		case R.id.locfolder:
		case R.id.rmtfolder:
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);
			dialog.setTitle("Enter Folder Name");
			final EditText input = new EditText(this);
			dialog.setView(input);
			dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    	
                            newFolderName = input.getText().toString().trim();
                            if( R.id.locfolder == id ){
                            	File file = new File(localCurrentDir,newFolderName);
                            	if(file.mkdir()){
                            		File[] tempFiles = localCurrentDir.listFiles();
                            		if(srcMtFolder.isShown()){
                            			/*localFileListView = new ListView(BluetoothFileBrowserActivity.this);
                                		localFileListView.setId(LOCAL_LISTID);
                                		srcLinearLayout.addView(localFileListView);
                                		localFileAdapt = new FileListAdapter();*/
                                		srcMtFolder.setVisibility(View.INVISIBLE);
                                		localFileListView.setVisibility(View.VISIBLE);
                                		
                            		}
	                            		localFileList = tempFiles;
	                        			localFileAdapt.setData(tempFiles);
	                        			localFileAdapt.notifyDataSetChanged();
                            		
                        			//Toast.makeText(getApplicationContext(), "dir created",Toast.LENGTH_SHORT).show();
                            	}
                            		
                            	
                            }
                            else{
                            	Log.v("log", "inside remote folder create");
                            	if(!mBTService.CreateNewFolder(newFolderName))
                            		Toast.makeText(getApplicationContext(), "permission denier", Toast.LENGTH_SHORT).show();
                            	if(dstMtFolder.isShown()){
                        			
                            		dstMtFolder.setVisibility(View.INVISIBLE);
                            		remoteFileListView.setVisibility(View.VISIBLE);
                            		
                        		}
                            	 ArrayList<BluetoothFTPFileInfo> tempFileList = mBTService.getFolderList();
                            	 remoteFileList = tempFileList;
                				remoteFileAdapt.setData(remoteFileList);
                				remoteFileAdapt.notifyDataSetChanged();
                            	
                            }
                    }

            });

            AlertDialog alert = dialog.create();
            alert.show();
			break;
		}
	}

	
	
	private Handler FileReceiveHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			switch(msg.what){
			
			case 1:
				Log.v("log", "updating list view : " + localCurrentDir);
				//updateLocalFileList();
				File[] tempFiles = localCurrentDir.listFiles();
				localFileList = tempFiles;
				Log.v("log", "localfile list" + tempFiles.length + tempFiles[0].getName());
				localFileAdapt.setData(tempFiles);
				localFileAdapt.notifyDataSetChanged();
				break;
			case 2:
				remoteFileList = mBTService.getFolderList();
				remoteFileAdapt.setData(remoteFileList);
				remoteFileAdapt.notifyDataSetChanged();
				break;
			}
		}
		
	};
	
	  

}