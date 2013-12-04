package com.cubes.learningcubes;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import android.app.ProgressDialog;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class LearningService extends Service implements TextToSpeech.OnInitListener{

	private TextToSpeech tts;
	private Lesson lesson;
	private CubesDbHelper db;
	private final String TAG = "Learning Service";
	private final String BLUETOOTH_SPEAKER_ADDRESS = "00:06:66:52:08:B5";

	private boolean textToSpeechReady = false;
	private boolean waitingForAnswer = false;
	private int questionIndex = 0;
	private TimerTask timerTask;
	private Question lastQuestion;
	private ArrayList<String> output = new ArrayList<String>();
	
	private UUID mDeviceUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // Standard SPP UUID
	 
	private BluetoothSocket mBTSocket;
	private ReadInput mReadThread = null;
	private ToggleButton mToggleButton;

	private boolean mIsUserInitiatedDisconnect = false;
	private boolean mIsBluetoothConnected = false;
	private boolean mIsGameRunning = true;

	private BluetoothDevice mDevice;
	
	private BluetoothAdapter mBTAdapter;

	@Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
        tts = new TextToSpeech(this, this);
        super.onCreate();
    }
    


    @Override
    public void onStart(Intent intent, int startId) {
    	db = CubesDbHelper.getInstance(this);
		lesson = db.getActiveLesson();
		tts = new TextToSpeech(this, this);
		
		tts.setPitch(.8f);
		tts.setSpeechRate(.6f);
		mBTAdapter = BluetoothAdapter.getDefaultAdapter();
		
        if (mBTAdapter == null)
        {
            Toast.makeText(this, "Bluetooth cannot be initialized.", Toast.LENGTH_SHORT).show();
            stopSelf();
        }
        
        Set<BluetoothDevice> devices = mBTAdapter.getBondedDevices();
        for (BluetoothDevice device : devices) {
        	if (device.getAddress().equalsIgnoreCase(BLUETOOTH_SPEAKER_ADDRESS)) {
        		mDevice = device;
        	}
        }

        if (mDevice != null) {       	
        	new ConnectBT().execute();
        } else {
        	Log.d(TAG, "Device is null");
        	stopSelf();
        }
    }
    
    private void speak(String text) {
    	tts.speak(text, TextToSpeech.QUEUE_ADD, null);
    }
   
    private void pause(int duration) {
    	tts.playSilence(duration, TextToSpeech.QUEUE_ADD, null);
    }
    
    private boolean checkAnswer(String[] correctAnswer) {
    	Log.d(TAG, correctAnswer+"");
    	Log.d(TAG, output+"");
    	for (int i = 0; i < correctAnswer.length; i++) {
    		if (correctAnswer[i] != output.get(i)) {
    			return false;
    		}
    	}
    	return true;
    }
    
	@Override
	public void onDestroy() {
        super.onDestroy();
        if (mBTSocket != null && mIsBluetoothConnected) {
			new DisConnectBT().execute();
        }
       if (tts != null) {
			tts.stop();
            tts.shutdown();
        }
       timerTask.cancel();
       //save final session time to the database(?)
    }
	
	public void sayStartupLines() {
		//start startup sequence
		pause(1000);
		speak("Let's practice our " + lesson.category);
		pause(1000);
		speak("The next lesson is " + lesson.lessonName);
		pause(1000);
		lastQuestion = lesson.getQuestion(0);
			
	}
	 @Override
     public void onInit(int status) {
 
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(Locale.US);
            
            textToSpeechReady = true;
            sayStartupLines();
            timerTask = new QuestionTask();
    		new Timer().schedule(timerTask, 1000);
    		
        }  else {
        	textToSpeechReady = false;
        }
     }
	
	 private class QuestionTask extends TimerTask {
		    @Override
 			public void run() {
		    	
 				if (textToSpeechReady) {

 					if (waitingForAnswer) {
 						String[] pieces = lastQuestion.answer.split("|");
 						if (pieces.length == output.size()) {
 							boolean correct = checkAnswer(pieces);
 							if (correct) {
 								speak("Great job!");
 							} else {
 								speak("Sorry, that was not correct");
 							}
 							waitingForAnswer = false;
 						}
 						
 					} else {
 						
 						Question q = lesson.getQuestion(questionIndex);
 						lastQuestion = q;
 						speak(q.text);
 						
 						if (questionIndex == lesson.questions.size()){
 							tts.playSilence(1000, TextToSpeech.QUEUE_FLUSH, null);
 							speak("You have finished the lesson. Great job!");
 							stopSelf();
 						} else {
 							waitingForAnswer = true;
 							mReadThread = new ReadInput(); // Kick off input reader
 						}
 						
 						
 					}
 					
 				//}
 				
 			}
		    
		    }	
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
								String fixed = strInput.replace("[", "");
								if (fixed.length() > 0) {
									String result = fixed.substring(0, 7);
									output.add(result);
								}
								this.stop();
							}

						}
					
				} catch (IOException e) {
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
					stopSelf();
				} else {
				//	Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_SHORT).show();
					mIsBluetoothConnected = true;
				}
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
					stopSelf();
				}
			}

		}
	
}
