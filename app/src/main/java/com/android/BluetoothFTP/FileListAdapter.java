package com.android.BluetoothFTP;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class FileListAdapter extends BaseAdapter{
	
	public final String LOGTAG = "LocalFileList";
	private Activity mAppContext = null;

	private File[] mFilelist;
	private ArrayList<Object> tmpArraylist; 
	public FileListAdapter(Activity activityContext) {
		mAppContext = activityContext;
	}

	public void setData(File[] fileList)
	{
		mFilelist = fileList;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mFilelist.length;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mFilelist[position].getName();
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
			LayoutInflater li = mAppContext.getLayoutInflater();
			v = li.inflate(R.layout.filelistentry, null);
		}
		TextView tvTextView = (TextView)v.findViewById(R.id.tvfileName);
		tvTextView.setText(mFilelist[position].getName());

		TextView sizeview = (TextView)v.findViewById(R.id.length);
		sizeview.setText(String.valueOf(mFilelist[position].length()/1024)+" kb");

		TextView lastmodified = (TextView)v.findViewById(R.id.lastmodified);
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		lastmodified.setText("Last Modified:" +dateFormat.format(mFilelist[position].lastModified()));

		
		if(mFilelist[position].isDirectory() == true)
		{
			Log.v(LOGTAG, "file: "+mFilelist[position].getName()+" pos: "+position);
			ImageView iv = (ImageView)v.findViewById(R.id.filelisticon);
			iv.setBackgroundResource(R.drawable.folder_final);
		}
		else
		{
			Log.v(LOGTAG, "nofile: "+mFilelist[position].getName()+" pos: "+position);
			ImageView iv = (ImageView)v.findViewById(R.id.filelisticon);
			iv.setBackgroundResource(R.drawable.file_final);
		}
		return v;
	}
	
}