package com.cubes.learningcubes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

public class WebAppInterface {
    Context mContext;
    CubesDbHelper db;
    private String TAG = "Web app interface";
    
    /** Instantiate the interface and set the context */
    WebAppInterface(Context c, CubesDbHelper db) {
        mContext = c;
        this.db = db;
    }

    /** Show a toast from the web page */
    @JavascriptInterface
    public void showToast(String toast) {
        Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
    }
  /*  
    @JavascriptInterface
    public String getDataOverTime(String dateRange) {
    	List<DateData> results = new ArrayList<DateData>();
    	DateTime today = new DateTime();
    	for (int i = 0; i < 14; i++) {
    		DateTime current = today.minusDays(i);
    		DateTimeFormatter fmt = DateTimeFormat.forPattern("MM/dd");
    		String dateString = fmt.print(current);
    		ArrayList<Session> sessions = db.getSessionsByDate(current);
    	}
    	return "";
    }
    */
    @JavascriptInterface
    public String getCategoryStatistics(String dateRange) {
    	
    	
		ArrayList<Category> categories = db.getCategories();
		ArrayList<String> categoryNames = new ArrayList<String>();
		ArrayList<Float> averageScores = new ArrayList<Float>();
		ArrayList<Integer> totalLengths = new ArrayList<Integer>();
		ArrayList<Integer> averageLengths = new ArrayList<Integer>();
		ArrayList<Integer> totalSessions = new ArrayList<Integer>();
		
    	for (Category category : categories) {
    		
    		if (category.lessons.size() == 0) {    		
    			continue;
    		} else {
    			Log.d(TAG, category.name + " HAS STUFF IN IT");
    		}
    		
    		categoryNames.add(category.name);
    		float totalScore = 0.0f;
    		int totalLength = 0;

    		ArrayList<Session> sessions =  db.getSessionsByCategory(category.id);
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
			object.put("categoryNames", new JSONArray(categoryNames));
			object.put("averageScore", new JSONArray(averageScores));
			
			object.put("totalLength", new JSONArray(totalLengths));
			object.put("averageLength", new JSONArray(averageLengths));
			object.put("totalSessions", new JSONArray(totalSessions));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	Log.d(TAG, "CATEGORY DATA");
    	Log.d(TAG, object.toString());
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
    		float averageScore = 0.0f;
    		int averageLength = 0;

    		ArrayList<Session> sessions = db.getSessionsForLesson(lesson.id);
    		if (sessions.size() > 0) {
    			for (Session session : sessions) {
        			totalScore += session.score;
        			totalLength += session.sessionLength;
        		}
        		
        		averageScore = totalScore/(float)sessions.size();
        		averageLength = totalLength/sessions.size();
    		}
    		
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