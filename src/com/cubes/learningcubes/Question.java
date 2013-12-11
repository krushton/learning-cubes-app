package com.cubes.learningcubes;

import java.util.regex.Pattern;

import android.util.Log;

public class Question {
	
	private final String TAG = "QuestionModel";
	String text;
	String answer;
	long id;
	long lessonId;
	String remoteUrl;
	String localUrl;
	
	public Question(String text, String answer, long id, long lessonId, String remoteUrl, String localUrl) {
		this.text = text;
		this.answer = answer;
		this.id = id;
		this.lessonId = lessonId;
		this.remoteUrl = remoteUrl;
		this.localUrl = localUrl;
	}
	
	public Question(String t, String a, long l, String r, String u) {
		this(t, a, 0, l, r, u);
	}
	
	public Question(String t, String a, long l) {
		this(t, a, 0, null, null);
	}
	
	public String getAnswerWithoutSeparators() { 
		
		if (answer != null) {
			return answer.replace("|", "");
		} else {
			return "";
		}
		
	}
}
