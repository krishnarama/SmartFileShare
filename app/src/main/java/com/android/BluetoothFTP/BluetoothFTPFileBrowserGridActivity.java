package com.android.BluetoothFTP;

import java.io.File;

import com.android.BluetoothFTP.R.layout;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

public class BluetoothFTPFileBrowserGridActivity extends Activity {

	private String SDCardPath;
	private File[] fileList;
	private static final String LOGTAG = "FTPFileGridBrowser";
	private GridView localFileGrid;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.filegridview);
		Log.v(LOGTAG, "onCreate");
		SDCardPath = Environment.getExternalStorageDirectory().getPath();
    	loadLocalFiles();
    	localFileGrid = (GridView)findViewById(R.id.srcGridView);
    	localFileGrid.setAdapter(new LocalGridAdapter(fileList));
	}
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		Log.v(LOGTAG, "onStart");
	}
	
    private void loadLocalFiles() {
		// TODO Auto-generated method stub
    	Log.v(LOGTAG, "loadLocalFiles");
		File sdCard = new File(SDCardPath);
		fileList = sdCard.listFiles();
	}
    
    class LocalGridAdapter extends BaseAdapter{

    	File[] mfileList;
    	
    	LocalGridAdapter(File[] fileList)
    	{
    		mfileList = fileList;
    	}
    	
    	@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mfileList.length;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return mfileList[position].getName();
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			View v = convertView;
			if(v == null)
			{
				LayoutInflater li = getLayoutInflater();
				v = li.inflate(R.layout.filegridentry, null);
			}
			
			TextView tvFileName = (TextView)v.findViewById(R.id.gridFileName);
			tvFileName.setText(mfileList[position].getName());
			return v;
		}
    	
    }
}
