package com.cubes.learningcubes;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class Session {
	
	private final String TAG = "SessionData";
	
	Calendar calendar;	//date of the session
    int sessionLength; //length of the session in seconds
    int numberCorrect; //how many of the attempted questions were correct
    int numberTried; //how many questions were attempted
    String lessonName; //name of the lesson
    int lessonId;	//id of the lesson
    int id; //id of the session
    float score;
    HashMap<String, Integer> log;
    
    public Session(Date date, int sessionLength, int numberCorrect, int numberTried, String lessonName, 
    		int lessonId, int sessionId) {
    	this.calendar = Calendar.getInstance();
    	this.calendar.set(date.getYear(), date.getMonth(), date.getDay());
    	this.sessionLength = sessionLength;
    	this.numberCorrect = numberCorrect;
    	this.numberTried = numberTried;
    	this.lessonName = lessonName;
    	this.lessonId = lessonId;
    	this.id = sessionId;
    	this.score = (float)numberCorrect/(float)numberTried;
    	this.log = new HashMap<String, Integer>();
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
    
    	Calendar cal = Calendar.getInstance();
    	if (cal.get(Calendar.DATE) == calendar.get(Calendar.DATE)) {
    		return "Today";
    	}
    	if (cal.get(Calendar.DATE) - calendar.get(Calendar.DATE) == 1) { //todo: make this account for end/beg of month
    		return "Yesterday";
    	}
    	return calendar.get(Calendar.MONTH) + "/" + calendar.get(Calendar.DATE);
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
    
    private static int[] splitToComponentTimes(int val)
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
      for (int x=start;x<s.length;++x)
        out.append(delim).append(s[x]);
      return out.toString();
    }


}
