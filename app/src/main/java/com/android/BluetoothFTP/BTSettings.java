package com.android.BluetoothFTP;


import java.util.ArrayList;





import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;



public class BTSettings extends PreferenceActivity{
	
	private static final String KEY_BT_CHECKBOX = "bt_checkbox";
    private static final String KEY_BT_DISCOVERABLE = "bt_discoverable";
    private static final String KEY_BT_DEVICE_LIST = "bt_device_list";
    private static final String KEY_BT_NAME = "bt_name";
    private static final String KEY_BT_SCAN = "bt_scan";
	private static final int REQUEST_ENABLE_BT = 0;
	private static final int REQUEST_ENABLE_DISCOVERABLE = 1;
	
	public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
   
    BluetoothAdapter mBluetoothAdapter;
    private CheckBoxPreference mCheckBox = null;
    private CheckBoxPreference mDiscCheckBox = null;
    private ProgressCategory mDeviceList;
    private IntentFilter mStateIntentFilter;
    private IntentFilter mDiscIntentFilter;
    private IntentFilter mBTServiceIntentFilter;
    private IntentFilter mScanfilter;
    private Handler mUiHandler;
    private long endTimestamp;
    private ArrayList<BluetoothDevice> mArrayAdapter = null;
    
    private BluetoothService mBluetoothService;
    private BluetoothDevicePreference mSelectedDevice;
   
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
            handleStateChanged(state);
        }

		private void handleStateChanged(int state) {
			Log.v("BTSettings", "State: "+state);	 
			switch (state) {
	            case BluetoothAdapter.STATE_TURNING_ON:
	                mCheckBox.setSummary(R.string.bluetooth_starting);
	                mCheckBox.setEnabled(false);
	                break;
	            case BluetoothAdapter.STATE_ON:
	                mCheckBox.setChecked(true);
	                mCheckBox.setSummary(null);
	                mCheckBox.setEnabled(true);
	                if (mBluetoothService != null) {
	                    // Only if the state is STATE_NONE, do we know that we haven't started already
	                    if (mBluetoothService.getState() == BluetoothService.STATE_NONE) {
	                      // Start the Bluetooth services
	                    	mBluetoothService.start();
	                    }
	                }
	                break;
	            case BluetoothAdapter.STATE_TURNING_OFF:
	            	Log.v("BTSettings", "STATE_TURNING_OFF");	 
	                mCheckBox.setSummary(R.string.bluetooth_stopping);
	                mCheckBox.setEnabled(false);
	                break;
	            case BluetoothAdapter.STATE_OFF:
	                mCheckBox.setChecked(false);
	                mCheckBox.setSummary(R.string.bluetooth_quick_toggle_summary);
	                mCheckBox.setEnabled(true);
	                break;
	            default:
	                mCheckBox.setChecked(false);
	                mCheckBox.setSummary(R.string.bluetooth_error);
	                mCheckBox.setEnabled(true);
	                Log.v("Myapp", "error");
	        }
	    }
		
    };
    
    private final BroadcastReceiver mDiscReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (BluetoothAdapter.ACTION_SCAN_MODE_CHANGED.equals(intent.getAction())) {
            	Log.v("myapp", "in discover receiver");
                int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE,
                        BluetoothAdapter.ERROR);
                Log.v("myapp", "in discover receiver" + mode);
                if (mode != BluetoothAdapter.ERROR) {
                    handleModeChanged(mode);
                }
            }
        }
    };
    
 // Create a BroadcastReceiver for ACTION_FOUND
    private final BroadcastReceiver mScanReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Add the name and address to an array adapter to show in a ListView
                mArrayAdapter.add(device);
                BluetoothDevicePreference preference = new BluetoothDevicePreference(getApplicationContext(), device);
                mDeviceList.addPreference(preference);
            }
            else if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action))
            {
            	mDeviceList.setProgress(true);
            }
            else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
            	mDeviceList.setProgress(false);
            }
        }
    };
    
		private void handleModeChanged(int mode) {
			if (mode == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
				Log.v("myapp", "in handlemode changed");
				mDiscCheckBox.setChecked(true);
	            updateCountdownSummary();

	        } else {
	        	mDiscCheckBox.setChecked(false);
	        }
			
		}

		private void updateCountdownSummary() {
			int mode = mBluetoothAdapter.getScanMode();
	        if (mode != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
	            return;
	        }
	        Log.v("myapp", "in updateCountdownsummary");
	        long currentTimestamp = System.currentTimeMillis();
	        long endTimestamp = this.endTimestamp;// mLocalManager.getSharedPreferences().getLong(
	                //SHARED_PREFERENCES_KEY_DISCOVERABLE_END_TIMESTAMP, 0);

	        if (currentTimestamp > endTimestamp) {
	            // We're still in discoverable mode, but maybe there isn't a timeout.
	        	mDiscCheckBox.setSummaryOn("something went wrong");
	            return;
	        }

	        String formattedTimeLeft = String.valueOf((endTimestamp - currentTimestamp) / 1000);

	        mDiscCheckBox.setSummaryOn(formattedTimeLeft+" time");

	        synchronized (this) {
	            mUiHandler.removeCallbacks(mUpdateCountdownSummaryRunnable);
	            mUiHandler.postDelayed(mUpdateCountdownSummaryRunnable, 1000);
	        }
			
		}
   
    
    private final Runnable mUpdateCountdownSummaryRunnable = new Runnable() {
        public void run() {
            updateCountdownSummary();
        }
    };
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.xml.btsettings);
		
		mCheckBox = (CheckBoxPreference) findPreference(KEY_BT_CHECKBOX);
		mDiscCheckBox = (CheckBoxPreference) findPreference(KEY_BT_DISCOVERABLE);
		mDeviceList = (ProgressCategory)findPreference(KEY_BT_DEVICE_LIST);
		
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		
		mStateIntentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
		mDiscIntentFilter = new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
		mScanfilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		mScanfilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		mScanfilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		
		{
			mBTServiceIntentFilter = new IntentFilter();
			mBTServiceIntentFilter.addAction(BluetoothService.ACTION_BTSERVICE_DEVICE_NAME);
			mBTServiceIntentFilter.addAction(BluetoothService.ACTION_BTSERVICE_READ);
			mBTServiceIntentFilter.addAction(BluetoothService.ACTION_BTSERVICE_STATE_CHANGE);
			mBTServiceIntentFilter.addAction(BluetoothService.ACTION_BTSERVICE_TOAST);
		}
		
		
		if (mBluetoothAdapter.isEnabled()) {
			mCheckBox.setChecked(true);
		}else{
			mCheckBox.setChecked(false);
			mCheckBox.setSummary(R.string.bluetooth_quick_toggle_summary);
		}

		mUiHandler = new Handler();
		mArrayAdapter = new ArrayList<BluetoothDevice>(); 
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		
		// Initialize the BluetoothChatService to perform bluetooth connections
		mBluetoothService = BluetoothService.getBluetoothService(getApplicationContext());
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();	
		this.registerReceiver(mReceiver, mStateIntentFilter);
		this.registerReceiver(mDiscReceiver,mDiscIntentFilter);
		this.registerReceiver(mScanReceiver, mScanfilter);
		this.registerReceiver(mBTServiceReceiver,mBTServiceIntentFilter);
		
        
	}
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		this.unregisterReceiver(mReceiver);
		this.unregisterReceiver(mDiscReceiver);
		
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		this.unregisterReceiver(mScanReceiver);
		this.unregisterReceiver(mBTServiceReceiver);
	}
	
	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {
		Log.v("Myapp", "Inside preference main tree click");
		if(KEY_BT_CHECKBOX.equals(preference.getKey())){
			Log.v("Myapp", "Inside preference tree click");
			if (!mBluetoothAdapter.isEnabled()) {
				Log.v("Myapp", "enable bt");
			    //Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			    //startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
				mBluetoothAdapter.enable();
			}
			else{
				mDeviceList.removeAll();
				mBluetoothAdapter.disable();
			}
				
		}
		else if(KEY_BT_DISCOVERABLE.equals(preference.getKey())){
			Log.v("myapp", "in discover preference");
			 endTimestamp = System.currentTimeMillis() + 300 * 1000;
			Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
			startActivityForResult(discoverableIntent,REQUEST_ENABLE_DISCOVERABLE);
		}
		
		else if (KEY_BT_SCAN.equals(preference.getKey())) {
			if(!mBluetoothAdapter.isDiscovering()){
				mDeviceList.removeAll();
				mBluetoothAdapter.startDiscovery();
			}
                      
        }
		
		if(preference instanceof BluetoothDevicePreference){
			if(mBluetoothAdapter.isDiscovering()){
				mBluetoothAdapter.cancelDiscovery();
			}
			mSelectedDevice = (BluetoothDevicePreference) preference;
			mBluetoothService.connect(((BluetoothDevicePreference) preference).getCachedDevice(), true);
			
		}
		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}
	

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		switch(requestCode){
		case REQUEST_ENABLE_DISCOVERABLE:
			if(resultCode == 300)
			{
				handleModeChanged(BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE);
			}
			
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	BroadcastReceiver mBTServiceReceiver = new BroadcastReceiver(){

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			Log.v("mBTServiceReceiver", "Reached onReceive fun");
			if(intent.getAction().equals(BluetoothService.ACTION_BTSERVICE_STATE_CHANGE))
			{
				Log.v("mBTServiceReceiver", "ACTION_BTSERVICE_STATE_CHANGE");
				//if(intent.getStringExtra(BluetoothService.BTMSG_TYPE).equals(BluetoothService.BTSERVICE_STATE_CHANGE))
				{
					int state = intent.getIntExtra(BluetoothService.EXTRA_BTSERVICE_STATE_CHANGE, 0);
					switch (state) {
					case BluetoothService.STATE_CONNECTED:
						mSelectedDevice.setSummary("Connected");
	                	Intent i = new Intent(getApplicationContext(),BluetoothRemoteFileBrowserActivity.class);
	                	startActivity(i);
	                    break;
	                case BluetoothService.STATE_CONNECTING:
	                	mSelectedDevice.setSummary("Connecting..");
	                    break;
	                case BluetoothService.STATE_LISTEN:
	                case BluetoothService.STATE_NONE:
	                    break;
					}
				}
			}
			//else if(intent.getStringExtra(BluetoothService.BTMSG_TYPE).equals(BluetoothService.BTSERVICE_DEVICE_NAME))
			else if(intent.getAction().equals(BluetoothService.ACTION_BTSERVICE_DEVICE_NAME))
			{
				Log.v("mBTServiceReceiver", "ACTION_BTSERVICE_DEVICE_NAME");
			}
			//else if(intent.getStringExtra(BluetoothService.BTMSG_TYPE).equals(BluetoothService.BTSERVICE_TOAST))
			else if(intent.getAction().equals(BluetoothService.ACTION_BTSERVICE_TOAST))
			{
				Log.v("mBTServiceReceiver", "ACTION_BTSERVICE_TOAST");	
				String msg = intent.getStringExtra(BluetoothService.EXTRA_BTSERVICE_TOAST); 
				Toast.makeText(getApplicationContext(), msg,Toast.LENGTH_SHORT).show();
			}
			//else if(intent.getStringExtra(BluetoothService.BTMSG_TYPE).equals(BluetoothService.BTSERVICE_READ))
			else if(intent.getAction().equals(BluetoothService.ACTION_BTSERVICE_READ))
			{
				Log.v("mBTServiceReceiver", "ACTION_BTSERVICE_READ");		
				byte[] buf = intent.getByteArrayExtra(BluetoothService.EXTRA_BTSERVICE_READ); 
				StringBuffer temp = new StringBuffer();
				temp.append(buf[0]);
				temp.append(buf[1]);
				Toast.makeText(getApplicationContext(), "Read: "+temp, Toast.LENGTH_SHORT).show();
			}
		}
	};

}
