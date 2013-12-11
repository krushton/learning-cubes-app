package com.cubes.learningcubes;


import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.cubes.learningcubes.DatabaseContract.BlockEntry;
import com.cubes.learningcubes.DatabaseContract.BlockSetEntry;
import com.cubes.learningcubes.DatabaseContract.LessonEntry;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;


public class BlockSetDownloadService extends IntentService {
	
    private CubesDbHelper db;
    private final String TAG = "BlockSetDownloadService";
    private BlockSet set;
    private Handler handler = new Handler();
    private Timer timer = new Timer();
    private boolean blockSetHasAudio = false;
    private List<Block> blocks;
    
    public BlockSetDownloadService(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}
    public BlockSetDownloadService() {
    	super("BlockSetDownloadService");
    }

	@Override
    protected void onHandleIntent(Intent intent) {
        // Gets data from the incoming Intent
        db = CubesDbHelper.getInstance(this);
        long blockSetId = intent.getLongExtra("blockSetId", 0);
        
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            // We cant write to SD card
        	sayAToast("Cannot access SD card :(");
        	stopSelf();
        } 
        
        
        if (blockSetId > 0) {
        	sayAToast("Starting blockset download...");
        	set = db.getBlockSetById(blockSetId);
        	blocks = new ArrayList<Block>();
        	addBlocksFromWeb();
        	if (blockSetHasAudio) {
        		downloadBlockAudioFiles();
        	}
        	
        }
	}
	
	
    
   private void addBlocksFromWeb() {
	    
       String url = "http://fuzzylogic.herokuapp.com/block_sets/" + set.remoteId + ".json";
       HttpResponse response;
       HttpClient httpclient = new DefaultHttpClient();
       String responseString = "";
       
       try {
       	response = httpclient.execute(new HttpGet(url));
       	if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
       		ByteArrayOutputStream out = new ByteArrayOutputStream();
       		response.getEntity().writeTo(out);
       		out.close();
       		responseString = out.toString();
       		Log.d(TAG, responseString);
           } else{
               //Closes the connection.
               response.getEntity().getContent().close();
               Log.d(TAG, response.getStatusLine().getReasonPhrase());
               throw new IOException(response.getStatusLine().getReasonPhrase());
           }
       } catch (ClientProtocolException e) {
           //TODO Handle problems..
       } catch (IOException e) {
           //TODO Handle problems..
       }
       try {
           JSONObject mBlockSet = new JSONObject(responseString);
           if (mBlockSet.has("blocks")) {
        	   JSONArray blockListJson = mBlockSet.getJSONArray("blocks");
        	   boolean exists = false;
        	   for (int i = 0; i < blockListJson.length(); i++) {
        		   JSONObject bObj = blockListJson.getJSONObject(i);
        		   String text = bObj.getString("text");
        		   String remoteUrl = null;
        		   if (bObj.has("url")) {
        			   exists = true;
        			   remoteUrl = bObj.getString("url");
        		   }
        		   
        		   Block newBlock = new Block(text, null, set.id, null, remoteUrl);
        		   long id = db.addBlock(newBlock);
        		   newBlock.id = id;
        		   blocks.add(newBlock);
        	   }
        	   blockSetHasAudio = exists;
   
           }
           
       } catch (JSONException e) {
               // TODO Auto-generated catch block
               e.printStackTrace();
       } 
   }
   
   private void downloadBlockAudioFiles() {
	   for (int i = 0; i < blocks.size(); i++) {
		   ContentValues values = new ContentValues();
		   Block currentBlock = blocks.get(i);
		   String fileName = "set-" + set.id + "-block-" + currentBlock.id + ".mp3";
		   String urlResult = downloadAudioFile(currentBlock.remoteUrl, fileName);
		   values.put(BlockEntry.BLOCK_LOCAL_URL, urlResult);
		   db.updateValues(BlockEntry.TABLE_NAME, currentBlock.id, values);
	   }
	   sayAToast("Block set \"" + set.name + "\" downloaded successfully.");
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