package com.cubes.learningcubes;

import java.util.ArrayList;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class Session {
	
	private final static String TAG = "SessionData";
	DateTime datetime;
	long utcDate;
    int sessionLength; //length of the session in seconds
    int numberCorrect; //how many of the attempted questions were correct
    int numberTried; //how many questions were attempted
    String lessonName; //name of the lesson
    long lessonId;	//id of the lesson
    long id; //id of the session
    float score;
    ArrayList<LogItem> sessionLog;
    
    public Session(long dateInMillis, int sessionLength, int numberCorrect, int numberTried, String lessonName, 
    		long lessonId, long sessionId, ArrayList<LogItem> sessionLog) {
	
    	this.utcDate = dateInMillis;
    	convertDate(dateInMillis);
    	this.sessionLength = sessionLength;
    	this.numberCorrect = numberCorrect;
    	this.numberTried = numberTried;
    	this.lessonName = lessonName;
    	this.lessonId = lessonId;
    	this.id = sessionId;
    	this.score = (float)numberCorrect/(float)numberTried;
    	if (sessionLog == null) {
    		this.sessionLog = new ArrayList<LogItem>();
    	} else {
    		this.sessionLog = sessionLog;
    	}
    }
    
    public Session(long dateInMillis, int sessionLength, String lessonName, long lessonId, ArrayList<LogItem> sessionLog) {
    	this(dateInMillis, sessionLength, 0, 0, lessonName, lessonId, 0, sessionLog);
    }
    
    
    public int getScore() {
    	return (int)(score * 100);
    }
    
    public String getSummary() {
    	int[] values = splitToComponentTimes(sessionLength);
    	String numCorrect =  " (" + numberCorrect + "/" + numberTried + " questions)";
    	if (values[0] == 0) {
    		return combine(values, ":", false) + numCorrect;
    	} else {
    		return combine(values, ":", true) + numCorrect;
    	}
    	
    }
   
    public String getDate() {
    	DateTimeFormatter fmt = DateTimeFormat.forPattern("MM/dd");
    	return fmt.print(datetime);
    }
    
    public int getScoreDrawable() {
    	int percent = getScore();
    	if (percent > 80) {
    		return R.drawable.score_green;
    	}
    	if (percent > 60) {
    		return R.drawable.score_yellow;
    	}
    	return R.drawable.score_red;
    }
    
    private int[] splitToComponentTimes(long val)
    {
        int hours = (int) val / 3600;
        int remainder = (int) val - hours * 3600;
        int mins = remainder / 60;
        remainder = remainder - mins * 60;
        int secs = remainder;

        int[] ints = {hours , mins , secs};
        return ints;
    }
    
    private String combine(int[] s, String delim, boolean includeHours)
    {
      if (s.length == 0) {
        return "";
      }
      StringBuilder out=new StringBuilder();
      int start;
      if (includeHours) {
    	  out.append(s[0]);
    	  start = 1;
      } else {
    	  out.append(s[1]);
    	  start = 2;
      }
      for (int x=start;x<s.length;++x) {
        out.append(delim).append(s[x]);
      }
      return out.toString();
    }
    
 
    @SuppressWarnings("deprecation")
	private void convertDate(long millisecs) {
      DateTime dt = new DateTime(millisecs);
      this.datetime = dt;
    }
   


}
