package com.cubes.learningcubes;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.media.AudioManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class GameActivity extends Activity implements TextToSpeech.OnInitListener {
	 private BluetoothA2dp bluetoothProfile;
	 private BluetoothAdapter adapter;
	 private BluetoothDevice device;
	 private TextToSpeech tts;
	 private AudioManager manager;
	 private final String SPEAKER_MAC_ADDRESS = "00:5F:2A:A6:73:9C";
	 private static final UUID SERVICE_UUID = UUID.fromString("0000110B-0000-1000-8000-00805F9B34FB");
	 private final String TAG = "Learning Service";
	 private boolean bluetoothReady = false;
	 private Timer timer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game);
		setupActionBar();
		tts = new TextToSpeech(this, this);

	}
	
	public void initialize(View v) {
		adapter = BluetoothAdapter.getDefaultAdapter();
		device = adapter.getRemoteDevice(SPEAKER_MAC_ADDRESS);
		/*
		BluetoothSocket socket;
		try {
			socket = device.createInsecureRfcommSocketToServiceRecord(SERVICE_UUID);
        	socket.connect();
        	bluetoothReady = true;
		} catch (IOException e) {
			Log.e(TAG, "FAILED");
			// TODO Auto-generated catch block
			Log.e(TAG, e.getMessage());
			e.printStackTrace();
			bluetoothReady = false;
		}
		
	    
	    timer = new Timer();

	    timer.scheduleAtFixedRate(new CheckBluetoothTask(), 1000, 1000);
	    */
	    AudioManager manager = (AudioManager)getSystemService(this.AUDIO_SERVICE);
		manager.setBluetoothScoOn(true);
	
		startLesson();
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
    public void onDestroy() {
		Log.e(TAG, "--- on destroy called ---");

		if (tts != null) {
			tts.stop();
            tts.shutdown();
        }
		//unregisterReceiver(locateReceiver);
		
        super.onDestroy();
        
    }
	
	private void startLesson() {
		Log.d(TAG, "START LESSON!");
		speak("Hello");
	}
	
	public void speak(String text) {
		Log.d(TAG, text);
		tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
	}
	
	private class CheckBluetoothTask extends TimerTask {

		@Override
		public void run() {
			if (bluetoothReady) {
				this.cancel();
				Log.d(TAG, "bluetooth ready");
				startLesson();
			} else {
				Log.d(TAG, "bluetooth not ready");
			}
		}
		
	}
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
