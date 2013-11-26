package com.cubes.learningcubes;

import java.util.HashMap;

/* Model class representing a lesson */

public class Lesson {
	private final String TAG = "LessonData";
	
	String lessonName;
	int id;
	int blockSetId;
	HashMap<String, String> questions;
	
	public Lesson(String lessonName, int id, HashMap<String, String> qs) {
		this.lessonName = lessonName;
		this.id = id;
		this.blockSetId = blockSetId;
		if (qs != null) {
			this.questions = qs;
		} else {
			this.questions = new HashMap<String, String>();
		}
	}
	
	public void setBlockSet(int blockSetId) {
		this.blockSetId = blockSetId;
	}
}
