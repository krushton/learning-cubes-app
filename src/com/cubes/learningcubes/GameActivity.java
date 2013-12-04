package com.cubes.learningcubes;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class GameActivity extends Activity implements TextToSpeech.OnInitListener {
	 private TextToSpeech tts;
	 private final String TAG = "Learning Service";
	 private final String BLUETOOTH_SPEAKER_ADDRESS = "00:06:66:52:08:B5";
	 private CubesDbHelper db;
	 public static final int MESSAGE_READ = 2;
	 private TextView mTxtReceive;
	 private BluetoothAdapter mBTAdapter;
	 
	 private UUID mDeviceUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // Standard SPP UUID
	 
	 private BluetoothSocket mBTSocket;
	 private ReadInput mReadThread = null;
	 private ToggleButton mToggleButton;

	 private boolean mIsUserInitiatedDisconnect = false;
	 private boolean mIsBluetoothConnected = false;
	 private boolean mIsGameRunning = true;

	 private BluetoothDevice mDevice;

	 private ProgressDialog progressDialog;
	 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game);
		setupActionBar();
		mTxtReceive = (TextView) findViewById(R.id.output);
		db = CubesDbHelper.getInstance(this);
		tts = new TextToSpeech(this, this);
		mToggleButton = (ToggleButton)findViewById(R.id.enableButton);
		// The Handler that gets information back from the BluetoothChatService
	}
	
	
	public void initialize(View v) {
		
			mBTAdapter = BluetoothAdapter.getDefaultAdapter();
	        if (mBTAdapter == null)
	        {
	        	/*  Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
	            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT); */
	            
	            Toast.makeText(this, "Bluetooth is not supported.", Toast.LENGTH_SHORT).show();
	            finish();
	        }
	        
	        Set<BluetoothDevice> devices = mBTAdapter.getBondedDevices();
	        for (BluetoothDevice device : devices) {
	        	if (device.getAddress().equalsIgnoreCase(BLUETOOTH_SPEAKER_ADDRESS)) {
	        		mDevice = device;
	        	}
	        }
	        
	       
	        if (mDevice != null) {       	
	        	new ConnectBT().execute();
	        	mIsGameRunning = true;
	        	startLesson();
	        } else {
	        	Log.d(TAG, "Device is null");
	        }

      
	}
	public void clear(View v) {
		mTxtReceive.setText("");
		
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
		if (mBTSocket == null || !mIsBluetoothConnected) {
			
		}
        super.onResume();
      
    }

    @Override
    protected void onPause() {
    	if (mBTSocket != null && mIsBluetoothConnected) {
			new DisConnectBT().execute();
			if (tts != null) {
				tts.stop();
				tts.shutdown();
			}
            
		}
    	mIsGameRunning = false;
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
       if (tts != null) {
			tts.stop();
            tts.shutdown();
        }
       mIsGameRunning = false;
    }
	
	private void startLesson() {
		if (mIsGameRunning) {
			final Lesson lesson = db.getActiveLesson();
			
			Timer timer = new Timer();
			TimerTask myTimerTask = new TimerTask() {
	
				@Override
				public void run() {
					Question q = lesson.getRandomQuestion();
					speak(q.text);
				}
				
			};
			timer.schedule(myTimerTask, 1000, 5000);
		} 
			
		
	}
	
	public void speak(String text) {
		Log.d(TAG, text);
		tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
	}
	
	private class ReadInput implements Runnable {

		private boolean bStop = false;
		private Thread t;

		public ReadInput() {
			t = new Thread(this, "Input Thread");
			t.start();
		}

		public boolean isRunning() {
			return t.isAlive();
		}

		@Override
		public void run() {
			InputStream inputStream;

			try {
				inputStream = mBTSocket.getInputStream();
				while (!bStop) {
					byte[] buffer = new byte[256];
					if (inputStream.available() > 0) {
						inputStream.read(buffer);
						int i = 0;
						/*
						 * This is needed because new String(buffer) is taking the entire buffer i.e. 256 chars on Android 2.3.4 http://stackoverflow.com/a/8843462/1287554
						 */
						for (i = 0; i < buffer.length && buffer[i] != 0; i++) {
						}
						final String strInput = new String(buffer, 0, i);

						/*
						 * If checked then receive text, better design would probably be to stop thread if unchecked and free resources, but this is a quick fix
						 */
							mTxtReceive.post(new Runnable() {
								@Override
								public void run() {
									mTxtReceive.append(strInput);
									//Uncomment below for testing
									//mTxtReceive.append("Chars: " + strInput.length() + " Lines: " + mTxtReceive.getLineCount() + "\n");
								
								}
							});
						}

					}
					Thread.sleep(500);
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		public void stop() {
			bStop = true;
		}

	}
	
	private class ConnectBT extends AsyncTask<Void, Void, Void> {
		private boolean mConnectSuccessful = true;

		@Override
		protected void onPreExecute() {
			progressDialog = ProgressDialog.show(GameActivity.this, "Hold on", "Connecting");// http://stackoverflow.com/a/11130220/1287554
		}

		@Override
		protected Void doInBackground(Void... devices) {

			try {
				if (mBTSocket == null || !mIsBluetoothConnected) {
					mBTSocket = mDevice.createInsecureRfcommSocketToServiceRecord(mDeviceUUID);
					BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
					mBTSocket.connect();
				}
			} catch (IOException e) {
				// Unable to connect to device
				e.printStackTrace();
				mConnectSuccessful = false;
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			if (!mConnectSuccessful) {
				Toast.makeText(getApplicationContext(), "Could not connect to device. Is it a Serial device? Also check if the UUID is correct in the settings", Toast.LENGTH_LONG).show();
				finish();
			} else {
			//	Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_SHORT).show();
				mIsBluetoothConnected = true;
				mReadThread = new ReadInput(); // Kick off input reader
			}

			progressDialog.dismiss();
		}

	}


	private class DisConnectBT extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
		}

		@Override
		protected Void doInBackground(Void... params) {

			if (mReadThread != null) {
				mReadThread.stop();
				while (mReadThread.isRunning())
					; // Wait until it stops
				mReadThread = null;

			}

			try {
				mBTSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			mIsBluetoothConnected = false;
			if (mIsUserInitiatedDisconnect) {
				finish();
			}
		}

	}
}
