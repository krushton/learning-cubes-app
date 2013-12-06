package com.cubes.learningcubes;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.regex.Pattern;

import org.joda.time.DateTime;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;

public class LearningService extends Service implements TextToSpeech.OnInitListener{

	private TextToSpeech tts;
	private Lesson lesson;
	private CubesDbHelper db;
	private final String TAG = "Learning Service";
	private final String BLUETOOTH_SPEAKER_ADDRESS = "00:06:66:52:08:B5";

	private boolean waitingForAnswer = false;
	private int questionIndex = 0;
	private int numberCorrect = 0;
	private int lastKnownOutputLength = 0;
	
	private QuestionTask questionTask;
	private Question currentQuestion;
	private String[] currentQuestionTags;
	private ArrayList<String> output = new ArrayList<String>();
	
	private UUID mDeviceUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // Standard SPP UUID
	 
	private BluetoothSocket mBTSocket;
	private ReadInput mReadThread = null;

	private boolean mIsUserInitiatedDisconnect = false;
	private boolean bluetoothReady = false;
	private boolean textToSpeechReady = false;

	private BluetoothDevice mDevice;
	private long lastAddTime;
	
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
        
        CheckIfReadyTask task = new CheckIfReadyTask();
        Timer timer = new Timer();
        timer.schedule(task, 0, 3000);
    }
    
    private void speak(String text) {
    	Log.d(TAG, "TEXT TO SPEECH: " + text);
    	tts.speak(text, TextToSpeech.QUEUE_ADD, null);
    }
   
    private void pause(int duration) {
    	tts.playSilence(duration, TextToSpeech.QUEUE_ADD, null);
    }
    
	@Override
	public void onDestroy() {
        super.onDestroy();
        if (mBTSocket != null && bluetoothReady) {
			new DisConnectBT().execute();
        }
       if (tts != null) {
			tts.stop();
            tts.shutdown();
        }
       //save final session time to the database(?)
    }
	
	public void sayStartupLines() {
		//start startup sequence
		speak("The next lesson is " + lesson.lessonName);
		pause(1000);		
	}
	
	public void sayEndingLines() {
		//start startup sequence
		speak("Lesson complete.");
		pause(1000);
		speak("You answered " + numberCorrect + " of " + lesson.questions.size() + " questions correctly");
		pause(1000);
		Timer timer = new Timer();
		CheckIfDoneTask task = new CheckIfDoneTask();
		timer.schedule(task, 1000, 1000);
	}
	
	
	
	 @Override
     public void onInit(int status) {
 
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(Locale.US);          
            textToSpeechReady = true;
        }  else {
        	textToSpeechReady = false;
        }
     }
	 
	 private boolean isSimilarEnough(String testId, String correctId) {

		 testId = testId.toLowerCase().trim();
		 correctId = correctId.toLowerCase();
		 Log.d(TAG, "Now comparing: "+ testId + " and " + correctId);
		 int commonChars = 0;
		 for (int i = 0; i < testId.length(); i++) {
			 if (correctId.contains(""+testId.charAt(i))) {
				 commonChars++;
			 }
		 }
		 float percent = (float)commonChars / (float)correctId.length();
		 Log.d(TAG, "Common characters percentage: " + percent );
		 if (percent > .8) {
			 return true;
		 } 
		 return false;
	 }
	 
	 private void startGame() {
		 Log.d(TAG, "STARTING GAME");
		 Log.d(TAG, "FIRST QUESTION IS " + lesson.questions.get(questionIndex).text);
		 sayStartupLines();
		 currentQuestion = lesson.questions.get(questionIndex);
		 currentQuestionTags = db.getTagsForValues(currentQuestion.answer.split(Pattern.quote("|")));
		 questionTask = new QuestionTask();
		 Timer timer = new Timer();
		 timer.schedule(questionTask, 2000, 2000);
		 mReadThread = new ReadInput();
	 }
	 
	 
	 private class QuestionTask extends TimerTask {
		 public void run() { 
			if (!waitingForAnswer) {
				speak(currentQuestion.text);
				waitingForAnswer = true;
				
			} else {
				Log.d(TAG, "Waiting for answer...");
				
				for (String item : output) {
					Log.d(TAG, "output item: " +item);
				}
				Log.d(TAG, "OUTPUT LENGTH IS " + output.size());
				
				if (output.size() > lastKnownOutputLength) {
					Log.d(TAG, "We see a new block");
					Block block = db.getBlockByTagValue(output.get(output.size()-1));
					if (block != null) {
						speak(block.text);
					} else {
						speak("I do not recognize that block");
					}
					lastKnownOutputLength++;
					
				}
				
				if (output.size() == currentQuestionTags.length) {
					
					boolean correctAnswer = true;
					for (int i = 0; i < output.size(); i++) {
						String tag = output.get(i);
						String tagLookingFor = currentQuestionTags[i];
						if (!isSimilarEnough(tag, tagLookingFor)) {
							correctAnswer = false;
						}
					}
					Log.d(TAG, "Correct answer? " + correctAnswer);
					if (correctAnswer) {
						speak("Great job!");
						numberCorrect++;
					} else {
						speak("Sorry, that is not correct");
					}
					output.clear();
					lastKnownOutputLength = 0;
					if (lesson.questions.size() == questionIndex+1) {
						Log.w(TAG, "LESSON IS OVER");
						sayEndingLines();
						
					} else {
						questionIndex++;
						currentQuestion = lesson.questions.get(questionIndex);
						currentQuestionTags = db.getTagsForValues(currentQuestion.answer.split(Pattern.quote("|")));
						waitingForAnswer= false;
					}
					
				}
				
			}
		 }
	 }
	 
	 private void endGame() {
		 Context ctx = getApplicationContext();
		 SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		 
         if (prefs.contains("serviceRunning")) {
        	 prefs.edit().putBoolean("serviceRunning", false);
         }
         
		 if (mBTSocket != null && bluetoothReady) {
				new DisConnectBT().execute();
	        }
	       if (tts != null) {
				tts.stop();
	            tts.shutdown();
	        }
		 stopSelf();
	 }
	 
	 
	 
	 private class CheckIfReadyTask extends TimerTask {
		 public void run() {
			 if (textToSpeechReady && bluetoothReady) {
				 startGame();
				 this.cancel();
			 }
		 }
	 }
	 
	 private class CheckIfDoneTask extends TimerTask {
		 public void run() {
			 if (!tts.isSpeaking()) {
				 endGame();
				 this.cancel();
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
								if (DateTime.now().getMillis() - lastAddTime < 1500) {
									int length = output.size();
									output.set(length-1, output.get(length-1) + strInput);
								} else {
									output.add(strInput);
									lastAddTime = DateTime.now().getMillis();
								}
								
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
					if (mBTSocket == null || !bluetoothReady) {
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
					bluetoothReady = true;
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
				bluetoothReady = false;
				if (mIsUserInitiatedDisconnect) {
					stopSelf();
				}
			}

		}
	
}
