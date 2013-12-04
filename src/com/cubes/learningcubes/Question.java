package com.cubes.learningcubes;

public class Question {
	
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
}
