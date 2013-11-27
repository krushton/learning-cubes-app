package com.cubes.learningcubes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SimpleExpandableListAdapter;
import android.widget.Toast;

public class GameActivity extends Activity implements TextToSpeech.OnInitListener {
	 private BluetoothA2dp bluetoothProfile;
	 private BluetoothAdapter mBluetoothAdapter;
	 private BluetoothDevice device;
	 private TextToSpeech tts;
	 private AudioManager manager;
	 private boolean mConnected = false;
	 private final String SPEAKER_MAC_ADDRESS = "00:5F:2A:A6:73:9C";
	 private final String TAG = "Learning Service";
	 private boolean bluetoothReady = false;
	 
	 private boolean mScanning;
	 private Handler mHandler;

     private static final int REQUEST_ENABLE_BT = 1;
     // Stops scanning after 10 seconds.
     private static final long SCAN_PERIOD = 10000;
	 
	 private final String DEVICE_MAC_ADDRESS = "00:07:80:60:D5:BE";
	 private final String kServiceUUID = "195AE58A-437A-489B-B0CD-B7C9C394BAE4".toLowerCase();

	 private final String readCharacteristicUUID = "21819AB0-C937-4188-B0DB-B9621E1696CD".toLowerCase();
	 private final String writeCharacteristicUUID = "5FC569A0-74A9-4FA4-B8B7-8354C86E45A4".toLowerCase();
	 private BluetoothLeService mBluetoothLeService;
	 private BluetoothGattCharacteristic mNotifyCharacteristic;

	 private final ServiceConnection mServiceConnection = new ServiceConnection() {

	        @Override
	        public void onServiceConnected(ComponentName componentName, IBinder service) {
	            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
	            Log.e(TAG, "HI FROM BLUETOOTH SERVICE");
	            if (!mBluetoothLeService.initialize()) {
	                Log.e(TAG, "Unable to initialize Bluetooth");
	                finish();
	            } else {
	            	Log.d(TAG, "BLUE TOOTH IS GO");
	            }
	            // Automatically connects to the device upon successful start-up initialization.
	            mBluetoothLeService.connect(DEVICE_MAC_ADDRESS);
	        }

	        @Override
	        public void onServiceDisconnected(ComponentName componentName) {
	            mBluetoothLeService = null;
	        }
	    };


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game);
		mHandler = new Handler();
		setupActionBar();
		tts = new TextToSpeech(this, this);
	}
	
	
	public void initialize(View v) {
		//adapter = BluetoothAdapter.getDefaultAdapter();
		//device = adapter.getRemoteDevice(SPEAKER_MAC_ADDRESS);
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) || 
        		!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH) ) {
            Toast.makeText(this, "Bluetooth is not supported.", Toast.LENGTH_SHORT).show();
            finish();
        }
   
	    AudioManager manager = (AudioManager)getSystemService(this.AUDIO_SERVICE);
		manager.setBluetoothScoOn(true);
		Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

		//adapter.startDiscovery();
	}

	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.game, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	 @Override
     public void onInit(int status) {
 
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(Locale.US);
        } 
     }
	 
	@Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(DEVICE_MAC_ADDRESS);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
        if (tts != null) {
			tts.stop();
            tts.shutdown();
        }
    }
	
	private void startLesson() {
		Log.d(TAG, "START LESSON!");
		speak("Hello");
	}
	
	public void speak(String text) {
		Log.d(TAG, text);
		tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
	}
	
	private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    invalidateOptionsMenu();
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        invalidateOptionsMenu();
    }

	 // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        	
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                //updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                Log.d(TAG, "ADDITIONAL SHIT: " + intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            }
        }
    };

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
    
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
        

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();

            if (uuid.equals(kServiceUUID)) {
            	Log.d(TAG, "found service");
            }

            
            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<BluetoothGattCharacteristic>();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                
                uuid = gattCharacteristic.getUuid().toString();
                if (uuid.equals(writeCharacteristicUUID)) {
                	charas.add(gattCharacteristic);
                	Log.d(TAG, "found characteristic: write");
                } else if (uuid.equals(readCharacteristicUUID)) {
                	Log.d(TAG, "found characteristic: read");
                	charas.add(gattCharacteristic);
                }
            }
            
            for (BluetoothGattCharacteristic characteristic : charas) {
            	final int charaProp = characteristic.getProperties();
                if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                    // If there is an active notification on a characteristic, clear
                    // it first so it doesn't update the data field on the user interface.
                    if (mNotifyCharacteristic != null) {
                        mBluetoothLeService.setCharacteristicNotification(
                                mNotifyCharacteristic, false);
                        mNotifyCharacteristic = null;
                    }
                    mBluetoothLeService.readCharacteristic(characteristic);
                }
                if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                    mNotifyCharacteristic = characteristic;
                    mBluetoothLeService.setCharacteristicNotification(
                            characteristic, true);
                }
            }
            
        }

   
    }

	// Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                	
                    final String deviceAddress = device.getAddress();
                    if (deviceAddress.equals(DEVICE_MAC_ADDRESS)) {
                    	
                    }
                    
                }
            });
        }
    };
	/*
	private class SpeakerLocatingReceiver extends BroadcastReceiver {
		
	    public void onReceive(Context context, Intent intent) {
	    	
	        String action = intent.getAction(); //may need to chain this to a recognizing function
	        
	        if (BluetoothDevice.ACTION_FOUND.equals(action)){
	        	Log.d(TAG, "--- device found ---");
	      
	            
	            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
	            Log.d(TAG, device.getAddress());
	            if (device.getAddress().equals("00:5F:2A:A6:73:9C")) {
	            	adapter.cancelDiscovery();
	            	BluetoothSocket socket;
					try {
						socket = device.createInsecureRfcommSocketToServiceRecord(APP_UUID);
		            	socket.connect();
		            	bluetoothReady = true;
					} catch (IOException e) {
						Log.e(TAG, "FAILED");
						// TODO Auto-generated catch block
						Log.e(TAG, e.getMessage());
						e.printStackTrace();
						bluetoothReady = false;
					}
	            	
	            }
	        }
	  
		}
	}
	*/

}
