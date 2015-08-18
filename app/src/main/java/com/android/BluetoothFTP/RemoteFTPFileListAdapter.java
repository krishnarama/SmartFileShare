package com.android.BluetoothFTP;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bluetooth.utility.BluetoothFTPFileInfo;

import org.apache.commons.net.ftp.FTPFile;

import java.util.ArrayList;

public class RemoteFTPFileListAdapter extends BaseAdapter{

	public final String LOGTAG = "RemoteFileList";
	public Activity mAppContext = null;
	private ArrayList<FTPFile> mFilelist;

	public RemoteFTPFileListAdapter(Activity applicationContext) {
		
		mAppContext = applicationContext;
	}

	public void setData(ArrayList<FTPFile> filelist)
	{
		mFilelist = filelist;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mFilelist.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mFilelist.get(position).getName();
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
		tvTextView.setText(mFilelist.get(position).getName());

		TextView sizeview = (TextView)v.findViewById(R.id.length);
		sizeview.setText(String.valueOf(mFilelist.get(position).getSize()/1024)+" kb");

		TextView lastmodified = (TextView)v.findViewById(R.id.lastmodified);
		lastmodified.setVisibility(View.GONE);
		/*SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		lastmodified.setText("Last Modified:" +dateFormat.format(mFilelist.get(position).);*/
		
		if(mFilelist.get(position).isFile() == false)
		{
			Log.v(LOGTAG, "file: "+mFilelist.get(position).getName()+" pos: "+position);
			ImageView iv = (ImageView)v.findViewById(R.id.filelisticon);
			iv.setBackgroundResource(R.drawable.folder_final);
		}
		else
		{
			Log.v(LOGTAG, "nofile: "+mFilelist.get(position).getName()+" pos: "+position);
			ImageView iv = (ImageView)v.findViewById(R.id.filelisticon);
			iv.setBackgroundResource(R.drawable.file_final);
		}
		return v;
	}
	
}