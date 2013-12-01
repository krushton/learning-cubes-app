package com.cubes.learningcubes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;


public class StatisticsActivity extends Activity {
	
	
	private final String TAG = "StatisticsActivity";
	private final String USER_AGENT_STRING = "CubesApp";
	private ProgressDialog dialog;
	private CubesDbHelper db;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().requestFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		// Show the Up button in the action bar.
		setupActionBar();
		setContentView(R.layout.activity_statistics);
		db = CubesDbHelper.getInstance(this);
		dialog = new ProgressDialog(this);
	    WebView webView = (WebView)findViewById(R.id.dashboard_webview);
	    webView.setWebViewClient(new WebViewClient() {
	    	public void onPageFinished(WebView view, String url) {                  
	            if (dialog.isShowing()) {
	                dialog.dismiss();
	            }
	        }
	    });
	    webView.addJavascriptInterface(new WebAppInterface(this), "Android");

	    dialog.setMessage("Crunching some numbers...");
	    dialog.setCanceledOnTouchOutside(false);
	    dialog.show();
	    
	    webView.getSettings().setJavaScriptEnabled(true);
	    webView.clearCache(true);
	    webView.getSettings().setUserAgentString(USER_AGENT_STRING);
	    webView.loadUrl("file:///android_asset/statistics.html");
	    
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
		getMenuInflater().inflate(R.menu.statistics, menu);
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
	
	private class WebAppInterface {
	    Context mContext;

	    /** Instantiate the interface and set the context */
	    WebAppInterface(Context c) {
	        mContext = c;
	    }

	    /** Show a toast from the web page */
	    @JavascriptInterface
	    public void showToast(String toast) {
	        Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
	    }
	    
	    @JavascriptInterface
	    public String getCategoryStatistics(String dateRange) {
			Set<String> categories = db.getCategoryNames();
			HashMap<String, String> results = new HashMap<String, String>();

	    	for (String category : categories) {
	    		
	    		float totalScore = 0.0f;
	    		int totalLength = 0;

	    		ArrayList<Session> sessions = db.getSessionsByCategory(category);
	    		for (Session session : sessions) {
	    			totalScore += session.score;
	    			totalLength += session.sessionLength;
	    		}
	    	
	    		float averageScore = totalScore/(float)sessions.size();
	    		int averageLength = totalLength/sessions.size();
	    		
	    		results.put(category, averageScore + "|" + averageLength + "|" + totalScore + "|" + totalLength);
	    		
	    	}
	    	JSONObject object = new JSONObject(results);
	    	return object.toString();
	    }
		
		@JavascriptInterface
	    public String getLessonStatistics(String dateRange) {
			
			ArrayList<Lesson> lessons = db.getLessons();
			ArrayList<String> lessonNames = new ArrayList<String>();
			ArrayList<Float> averageScores = new ArrayList<Float>();
			ArrayList<Integer> totalLengths = new ArrayList<Integer>();
			ArrayList<Integer> averageLengths = new ArrayList<Integer>();
			ArrayList<Integer> totalSessions = new ArrayList<Integer>();
			
			HashMap<String, JSONArray> results = new HashMap<String, JSONArray>();

	    	for (Lesson lesson : lessons) {
	    		
	    		lessonNames.add(lesson.lessonName);
	    		float totalScore = 0.0f;
	    		int totalLength = 0;

	    		ArrayList<Session> sessions = db.getSessionsForLesson(lesson.id);
	    		for (Session session : sessions) {
	    			totalScore += session.score;
	    			totalLength += session.sessionLength;
	    		}
	    		
	    		float averageScore = totalScore/(float)sessions.size();
	    		int averageLength = totalLength/sessions.size();
	    		averageScores.add(averageScore);
	    		averageLengths.add(averageLength);
	    		totalLengths.add(totalLength);
	    		totalSessions.add(sessions.size());
	    	}
	    	JSONObject object = new JSONObject();
	    	try {
				object.put("lessonNames", new JSONArray(lessonNames));
				object.put("averageScore", new JSONArray(averageScores));
				object.put("totalLength", new JSONArray(totalLengths));
				object.put("averageLength", new JSONArray(averageLengths));
				object.put("totalSessions", new JSONArray(totalSessions));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	Log.d(TAG, object.toString());
	    	return object.toString();
	    }
	}

}
