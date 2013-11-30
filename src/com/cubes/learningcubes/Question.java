package com.cubes.learningcubes;

public class Question {
	
	String text;
	String answer;
	long id;
	long lessonId;
	
	public Question(String text, String answer, long id, long lessonId) {
		this.text = text;
		this.answer = answer;
		this.id = id;
		this.lessonId = lessonId;
	}
	
	public Question(String text, String answer, long lessonId) {
		this.text = text;
		this.answer = answer;
		this.lessonId = lessonId;
	}
}
