package com.cubes.learningcubes;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.cubes.learningcubes.DatabaseContract.LessonEntry;
import com.cubes.learningcubes.DatabaseContract.QuestionEntry;

public class LessonDownloadService extends IntentService {
	
    private CubesDbHelper db;
    private Lesson lesson;
    private final String TAG = "LessonDownloadService";
    private Handler handler = new Handler();
    private Timer timer = new Timer();
    
    public LessonDownloadService(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}
    public LessonDownloadService() {
    	super("LessonDownloadService");
    }

	@Override
    protected void onHandleIntent(Intent intent) {
        // Gets data from the incoming Intent
        db = CubesDbHelper.getInstance(this);
        long lessonId = intent.getLongExtra("lessonId", 0);
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            // We cant write to SD card
        	sayAToast("Cannot access SD card :(");
        	stopSelf();
        } else {
        	sayAToast("Starting download... the lesson will appear in My Lessons when the download is complete.");
        }
        
        if (lessonId > 0) {
        	lesson = db.getLessonById(lessonId);
        	downloadLessonFiles();
        	downloadQuestionFiles();
        	updateLessonDownloadStatus();
        }
	}
	
	private void downloadLessonFiles() {
		
    	String[] urlsToDownload = { 
    		lesson.correctSoundRemoteUrl, lesson.incorrectSoundRemoteUrl, 
    		lesson.startSoundRemoteUrl, lesson.endSoundRemoteUrl
    	};
    	
    	ContentValues values = new ContentValues();
    	for (int i = 0; i < urlsToDownload.length; i++) {
    		String path = downloadAudioFile(urlsToDownload[i], lesson.id + "-" +  i + ".mp3");
    		if (path != null) {
    			switch(i) {
    				case 0:
    					values.put(LessonEntry.CORRECT_SOUND_LOCAL_URL, path);
    					break;
    				case 1:
    					values.put(LessonEntry.INCORRECT_SOUND_LOCAL_URL, path);
    					break;
    				case 2:
    					values.put(LessonEntry.START_SOUND_LOCAL_URL, path);
    					break;
    				case 3:
    					values.put(LessonEntry.END_SOUND_LOCAL_URL, path);
    					break;
    			}
    		}
    	}
    	if (values.size() > 0) {
    		db.updateValues(LessonEntry.TABLE_NAME, lesson.id, values);
    	}
    	
   }
   private void downloadQuestionFiles() {
	   ArrayList<Question> questions = lesson.questions;
	   for (int i = 0; i < questions.size(); i++) {
		   Question q = questions.get(i);
		   
		   if (q.remoteUrl != null && !q.remoteUrl.isEmpty()) {
			   String url = downloadAudioFile(q.remoteUrl, lesson.id + "-q" + i + ".mp3");
			   if (url != null) {
				   ContentValues values = new ContentValues();
				   values.put(QuestionEntry.QUESTION_LOCAL_URL, url);
				   db.updateValues(QuestionEntry.TABLE_NAME, q.id, values);
			   }
		   }
	   }
   }
   
   private void updateLessonDownloadStatus() {
	   ContentValues values = new ContentValues();
	   values.put(LessonEntry.LESSON_DOWNLOAD_STATUS, 1);
	   db.updateValues(LessonEntry.TABLE_NAME, lesson.id, values);
	   sayAToast("Lesson " + lesson.lessonName + " downloaded successfully.");
   }
   
   private String downloadAudioFile(String urlString, String fileName ) {
	   Log.d(TAG, urlString + " and " + fileName);
	   File sdCard = Environment.getExternalStorageDirectory();
	   File dir = new File(sdCard.getAbsolutePath() + "/fuzzylogic");
	   dir.mkdirs();
	   File file = new File(dir, fileName);
		
       int count;
       URL url;
	   try {
			url = new URL(urlString);
			URLConnection connection = url.openConnection();
           connection.connect();
           InputStream input = new BufferedInputStream(url.openStream());
           OutputStream output = new FileOutputStream(file);
           byte data[] = new byte[1024];
           while ((count = input.read(data)) != -1) {
               output.write(data, 0, count);
           }
           output.flush();
           output.close();
           input.close();
           return file.getAbsolutePath();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 
	   return null;
   }
   
   private void sayAToast(final String message) {
	   timer.schedule(new TimerTask() {
		   public void run() {
	           handler.post(new Runnable() {
	              public void run() {
	                 Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
	              }
	           });
		   }
	   }, 0);
	  
   }
	
}