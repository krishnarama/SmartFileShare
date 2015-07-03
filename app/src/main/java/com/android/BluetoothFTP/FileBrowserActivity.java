package com.android.BluetoothFTP;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.EmptyStackException;

import com.bluetooth.utility.BluetoothFTPFileInfo;
import com.example.javax.obex.ClientOperation;
import com.example.javax.obex.HeaderSet;
import com.example.javax.obex.ResponseCodes;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ContextMenu;
import android.view.DragEvent;
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

public class FileBrowserActivity extends Activity implements OnItemClickListener, OnClickListener {
	
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
    private File remoteCurrentDir;
    private File[] localFileList;
    private File[] remoteFileList;
    private ListView localFileListView;
    private ListView remoteFileListView;
    private FileListAdapter localFileAdapt;
    private FileListAdapter remoteFileAdapt;
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
	private LinearLayout mDestLayout;

	private View mVerticalBar;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setBackgroundDrawable(new ColorDrawable(android.R.color.holo_orange_light));
        setContentView(R.layout.main_test);
		getActionBar().setBackgroundDrawable(new ColorDrawable(android.R.color.holo_orange_light));
        Log.v(LOGTAG, "onCreate");
    	SDCardPath = Environment.getExternalStorageDirectory().getPath();
    	localCurrentDir = Environment.getExternalStorageDirectory();
    	srcLinearLayout = (LinearLayout)findViewById(R.id.srcSubLayout);
    	dstLinearLayout = (LinearLayout)findViewById(R.id.dstSubLayout);
		mVerticalBar = (View)findViewById(R.id.verticalbar);
		mDestLayout = (LinearLayout)findViewById(R.id.dstLayout);
		mDestLayout.setVisibility(View.GONE);
		mVerticalBar.setVisibility(View.GONE);
		//dstLinearLayout.setVisibility(View.INVISIBLE);
    	mBTService = BluetoothService.getBluetoothService(getApplicationContext());
    	mBTService.ftpHandler = FileReceiveHandler;
    	localCurrentFilePath = SDCardPath;
		sdCard = new File(localCurrentFilePath);
		remoteFileList = localFileList = sdCard.listFiles();

    	loadLocalFiles(1);
		loadLocalFiles(2);
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
		tvMyDevice.setVisibility(View.GONE);
    	//mBluetoothDevice = mBTService.getConnectedDevice();
    	tvRemoteDevice = (TextView)findViewById(R.id.tvRemoteDevice);
    	//tvRemoteDevice.setText("Remote Device:" + mBluetoothDevice.getName());
		tvRemoteDevice.setVisibility(View.GONE);
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
	private String mCurrentSelFile;
    private void loadLocalFiles(int id) {
		if(id == 1) {
			srcMtFolder = new TextView(this);
			//srcLinearLayout.addView(srcMtFolder);

			srcMtFolder.setGravity(Gravity.AXIS_Y_SHIFT);
			if (localFileList.length == 0) {

				srcMtFolder.setText(FOLDERMT);
				srcMtFolder.setGravity(Gravity.CENTER);

			} else {
				//localFileList = (ListView)findViewById(R.id.srcListview);
				localFileListView = new ListView(this);
				localFileListView.setId(1);
				srcLinearLayout.addView(localFileListView);
				localFileAdapt = new FileListAdapter(this);
				localFileAdapt.setData(localFileList);
				localFileListView.setAdapter(localFileAdapt);
				localFileListView.setOnItemClickListener(this);

			}

			//registerForContextMenu(localFileListView);

			localFileListView.setOnItemLongClickListener(new OnItemLongClickListener() {


				public boolean onItemLongClick(AdapterView<?> arg0, final View view,
											   final int position, long arg3) {

					if(mIsSplitViewOn) {
						ClipData data = ClipData.newPlainText("FileName", localFileList[position].getName());
						dstLinearLayout.setOnDragListener(new FTPDragListener(FTP_PUT, position, dstLinearLayout));
						srcLinearLayout.setOnDragListener(null);
						view.startDrag(data, new MyShadowBuilder(view), null, 0);
					}else {
						 new AlertDialog.Builder(FileBrowserActivity.this)
								.setTitle("Copy?")
								.setMessage("Do you want to copy?")
								.setPositiveButton("yes", new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog, int which) {
										mIsSplitViewOn = true;
										mCurrentSelFile = localFileList[position].getPath();
										mDestLayout.setVisibility(View.VISIBLE);
										mVerticalBar.setVisibility(View.VISIBLE);
										loadLocalFiles(2);
									}
								}).
								 setNegativeButton("No",new DialogInterface.OnClickListener() {

							 @Override
							 public void onClick(DialogInterface dialog, int which) {

							 }
						 }).show();
				}
					return true;
				}

			});

		}
		else{
			dstMtFolder = new TextView(this);
			//dstLinearLayout.addView(dstMtFolder);
			//mBTService.setPathRoot();
			//remoteFileList = remoteCurrentDir.listFiles();//mBTService.getFolderList();
			remoteCurrentFilePath = REMOTE_ROOT_PATH;
			{
				Log.v(LOGTAG, "remote list");
				//localFileList = (ListView)findViewById(R.id.srcListview);
				if (remoteFileList.length == 0) {

					dstMtFolder.setText(FOLDERMT);
					dstMtFolder.setGravity(Gravity.CENTER);

				}
				dstLinearLayout.setBackgroundResource(0);
				dstLinearLayout.setBackground(null);
				if(remoteFileListView == null) {
					remoteFileListView = new ListView(this);
					remoteFileListView.setId(2);
					dstLinearLayout.addView(remoteFileListView);
					remoteFileAdapt = new FileListAdapter(this);
					remoteFileAdapt.setData(remoteFileList);
					remoteFileListView.setAdapter(remoteFileAdapt);
					remoteFileListView.setOnItemClickListener(this);
				}
			}

			//registerForContextMenu(remoteFileListView);

			remoteFileListView.setOnItemLongClickListener(new OnItemLongClickListener() {


				public boolean onItemLongClick(AdapterView<?> arg0, View view,
											   int position, long arg3) {

					//ClipData data = ClipData.newPlainText("FileName", remoteFileList[position].getName());
					ClipData data = ClipData.newPlainText("FileName", remoteFileList[position].getName());
					FTPDragListener listener = new FTPDragListener(FTP_GET,0,srcLinearLayout);
					srcLinearLayout.setOnDragListener(listener);
					dstLinearLayout.setOnDragListener(null);
					view.startDrag(data, new MyShadowBuilder(view), null, 0);
					return true;
				}

			});

		}
	}



    class FTPDragListener implements View.OnDragListener{
    	int DragOperation;
    	int Position;
    	LinearLayout ll;
    	public FTPDragListener(int Operation,int position, LinearLayout l) {
			DragOperation = Operation;
			Position = position;
			ll = l;
		}

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

						new FileCopyTask(FileBrowserActivity.this).execute(new String[]{localFileList[Position].getPath(),remoteCurrentDir.getPath()});
					ll.setBackgroundResource(R.drawable.highlight_remover);
				}
				return (true);
				//break;
			case DragEvent.ACTION_DRAG_EXITED:
				Log.v("log", "drag exited");
				//ll.setBackgroundResource(R.drawable.highlight_remover);
				//ll.setBackgroundColor(Color.BLACK);
				break;
				
			}
			return true;
		}
    	
    }

	public void copyDirectory(File sourceLocation , File targetLocation)
			throws IOException {

		if (sourceLocation.isDirectory()) {
			if (!targetLocation.exists()) {
				targetLocation.mkdir();
			}

			String[] children = sourceLocation.list();
			for (int i=0; i<children.length; i++) {
				copyDirectory(new File(sourceLocation, children[i]),
						new File(targetLocation, children[i]));
			}
		} else {

			InputStream in = new FileInputStream(sourceLocation);
			OutputStream out = new FileOutputStream(targetLocation);

			// Copy the bits from instream to outstream
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
		}
	}

	class FileCopyTask extends AsyncTask<String, String, Long> {

		String Filename;
		String fileType;
		long pos = 0;
		Context mContext;
		ProgressDialog progressDialog;

		OutputStream outputstream;
		InputStream inputStream;

		public FileCopyTask(Context context) {
			mContext = context;
		}

		@Override
		protected Long doInBackground(String... params) {

			FileChannel inputChannel = null;
			FileChannel outputChannel = null;

			InputStream input = null;
			OutputStream output = null;


			try {
				Log.v("myapp", "File name is:" + params[0] + "  " + "destination::" + params[1]);
				/*inputChannel = new FileInputStream(params[0]).getChannel();
				outputChannel = new FileInputStream(params[1]).getChannel();
				outputChannel.transferFrom(inputChannel, 0, inputChannel.size());*/

				File inputfile = new File(params[0]);
				File outputfile = new File(params[1]);
				if(inputfile.isDirectory()){

					File newoutputfile = new File(outputfile.getPath()+"/"+inputfile.getName());
					Log.v("myapp","outputfolder name is::"+newoutputfile.getPath());
					copyDirectory(inputfile, newoutputfile);
					return (long)0;
				}
				input = new FileInputStream(inputfile);
				output = new FileOutputStream(new File(params[1]+"/"+inputfile.getName()));
				byte[] buf = new byte[1024];
				int bytesRead;
				while ((bytesRead = input.read(buf)) > 0) {
					output.write(buf, 0, bytesRead);
					//publishProgress(String.valueOf(((float)bytesRead/size)* 100) );
				}
			}catch (FileNotFoundException e){
				e.printStackTrace();
			}catch (IOException e){
				e.printStackTrace();
			}finally {
				try {
					/*if(inputChannel != null)
					inputChannel.close();
					if(outputChannel != null)
					outputChannel.close();*/

					if(input != null)
						input.close();

					if(output != null)
						output.close();


				}catch (IOException e){

				}
			}

			return (long)0;
		}

		@Override
		protected void onPostExecute(Long result) {

			remoteFileList = remoteCurrentDir.listFiles();//mBTService.getFolderList();
			remoteFileAdapt.setData(remoteFileList);
			remoteFileAdapt.notifyDataSetChanged();
			progressDialog.dismiss();
			mContext = null;


		}

		@Override
		protected void onPreExecute() {

			progressDialog = new ProgressDialog(mContext);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progressDialog.setMessage("Copying in progress... " );
			progressDialog.setCancelable(false);
			progressDialog.show();

			/*progressDialog = new ProgressDialog(mContext);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			progressDialog.setMessage("Copying " + Filename + "...");
			progressDialog.setCancelable(false);
			progressDialog.show();*/
		}


		protected void onProgressUpdate(Integer... values) {

			progressDialog.setProgress(values[0]);
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
    
   // private void loadRemoteFiles()
    /*{
    	dstMtFolder = new TextView(this);
    	dstLinearLayout.addView(dstMtFolder);
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
    		remoteFileListView.setId(2);
    		dstLinearLayout.addView(remoteFileListView);
        	remoteFileAdapt = new RemoteFileListAdapter(this);
        	remoteFileAdapt.setData(remoteFileList);
        	remoteFileListView.setAdapter(remoteFileAdapt);
        	remoteFileListView.setOnItemClickListener(this);
    	}
    	
    	//registerForContextMenu(remoteFileListView);
    	
    	remoteFileListView.setOnItemLongClickListener(new OnItemLongClickListener() {


			public boolean onItemLongClick(AdapterView<?> arg0, View view,
					int position, long arg3) {
				
				ClipData data = ClipData.newPlainText("FileName", remoteFileList.get(position).getFileName());
				FTPDragListener listener = new FTPDragListener(FTP_GET,0,srcLinearLayout);
				srcLinearLayout.setOnDragListener(listener);
				dstLinearLayout.setOnDragListener(null);
				view.startDrag(data, new MyShadowBuilder(view), null, 0);
				return true;
			}   	   	
    	
		});
    	
    }*/
    private boolean mIsSplitViewOn = false;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	// TODO Auto-generated method stub
    	//return super.onCreateOptionsMenu(menu);
    	//menu.add(0, 1, 0, "Grid View");
		menu.clear();
		menu.add(0, 1, 0, "Browser Remote Files");
		if(mIsSplitViewOn)
			menu.add(0,2,1,"Split View Off");
		else
			menu.add(0,2,1,"Split View ON");
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
    			//loadRemoteFiles();
				//loadLocalFiles(2);
				Intent intent = new Intent(getApplicationContext(),BTSettings.class);
				startActivity(intent);
				finish();
    		}
    		break;

			case 2:
			{
				Log.v(LOGTAG, "Menu item 'split view' selected");
				if(!mIsSplitViewOn) {
					mIsSplitViewOn = true;
					mDestLayout.setVisibility(View.VISIBLE);
					mVerticalBar.setVisibility(View.VISIBLE);
				}else{
					mIsSplitViewOn = false;
					mDestLayout.setVisibility(View.GONE);
					mVerticalBar.setVisibility(View.GONE);
					mDestLayout.destroyDrawingCache();
				}
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

	private void updateRemoteFileList(final int position){

		File[] tempFileList = null;
		if(remoteFileList[position].isDirectory() ==  true)
		{
			Log.v(LOGTAG, "isDirectory == true");
			File tempFile = remoteFileList[position];
			if(tempFile.canRead())
			{
				remoteCurrentDir = tempFile;
				remoteCurrentFilePath = tempFile.getAbsolutePath();
				tvDstPath.setText(remoteCurrentFilePath);
				//srcPath.setMovementMethod(new ScrollingMovementMethod());
				tempFileList = remoteFileList[position].listFiles();
				Log.v(LOGTAG, "No of files: " + tempFileList.length);
				if(tempFileList.length == 0)
				{
					remoteFileListView.setVisibility(View.GONE);
					if(dstMtFolder == null)
					{
						//srcMtFolder = new TextView(this);
						//srcLinearLayout.addView(srcMtFolder);
					}
					else if(!dstMtFolder.isShown())
					{
						//srcLinearLayout.addView(srcMtFolder);
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
			else
			{
				Log.v(LOGTAG, "Folder: "+ tempFile.getName()+ " is not readable!!" );
			}
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

	public void onItemClick(AdapterView<?> parent, View view, final int position,
			long id) {
		
		
		Log.v(LOGTAG, "onItemClick: pos: " + position);
		int viewId = parent.getId();
		if(viewId == LOCAL_LISTID)
		{
			updateLocalFileList(position);
		}
		else
		{
			updateRemoteFileList(position);
		}
	}

	private void deleteRemoteFile(String fileName,long len) {
		
		mBTService.deleteRemoteFile(fileName, len);
	}
	private void updateLocalFileListonBack(){
		
		File tempParent = localCurrentDir.getParentFile();
		String tempPath = tempParent.getAbsolutePath();
		if(!tempPath.equals("/"))
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

	private void updateRemoteFileListonBack(){
	try {
		File tempParent = remoteCurrentDir.getParentFile();
		String tempPath = tempParent.getAbsolutePath();
		if (!tempPath.equals("/")) {
			remoteCurrentDir = tempParent;
			remoteCurrentFilePath = tempParent.getAbsolutePath();
			tvDstPath.setText(remoteCurrentFilePath);
			File[] tempFiles = remoteCurrentDir.listFiles();
			if (!remoteFileListView.isShown()) {
				dstMtFolder.setVisibility(View.GONE);
				remoteFileListView.setVisibility(View.VISIBLE);
			}
			remoteFileList = tempFiles;
			remoteFileAdapt.setData(tempFiles);
			remoteFileAdapt.notifyDataSetChanged();
		} else {
			Toast.makeText(getApplicationContext(), "Not allowed", Toast.LENGTH_SHORT).show();
		}
	}catch (Exception e){
		e.printStackTrace();
		Toast.makeText(getApplicationContext(), "Not allowed", Toast.LENGTH_SHORT).show();
	}
	}

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

				updateRemoteFileListonBack();
				//mBTService.setPathBackward();
				/*String pathString[] = remoteCurrentFilePath.split("/");
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
				Log.v("log", "Path is: " + remoteCurrentFilePath);
				File[] tempFileList = remoteCurrentDir.listFiles();;//mBTService.getFolderList();
				if(!remoteFileListView.isShown())
				{
					dstMtFolder.setVisibility(View.GONE);
					remoteFileListView.setVisibility(View.VISIBLE);
				}
				remoteFileList = tempFileList;
				remoteFileAdapt.setData(tempFileList);
				remoteFileAdapt.notifyDataSetChanged();
*/
			}
			break;
			case R.id.rmtfolder:
				if(mCurrentSelFile != null)
				{
					new FileCopyTask(FileBrowserActivity.this).execute(mCurrentSelFile,remoteCurrentDir.getPath());
				}
				break;
		case R.id.locfolder:

			AlertDialog.Builder dialog = new AlertDialog.Builder(this);
			dialog.setTitle("Enter Folder Name");
			final EditText input = new EditText(this);
			dialog.setView(input);
			dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {


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
                            	 File[] tempFileList = remoteCurrentDir.listFiles();//mBTService.getFolderList();
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
				remoteFileList = remoteCurrentDir.listFiles();//mBTService.getFolderList();
				remoteFileAdapt.setData(remoteFileList);
				remoteFileAdapt.notifyDataSetChanged();
				break;
			}
		}
		
	};
	
	  

}