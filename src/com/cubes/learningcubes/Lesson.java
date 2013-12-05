package com.cubes.learningcubes;

import java.util.ArrayList;
import java.util.Random;

/* Model class representing a lesson */

public class Lesson {
	private final String TAG = "LessonData";
	
	String lessonName;
	String description;
	long id;
	long blockSetId;
	long remoteId;
	String category;
	long categoryId;
	boolean enabled;
	ArrayList<Question> questions;
	static int LESSON_ENABLED = 1;
	static int LESSON_DISABLED = 0;
	float price; 
			
	private Random random;
	public Lesson(String lessonName, String description, String category, long categoryId, int enableInt, 
			long id, long remoteId, long blockSetId, ArrayList<Question> questions, float price) {
		this.lessonName = lessonName;
		this.category = category;
		this.categoryId = categoryId;
		this.id = id;
		this.remoteId = remoteId;
		this.description = description;
		this.price = price;
		this.blockSetId = blockSetId;
		if (questions == null) {
			this.questions = new ArrayList<Question>();
		} else {
			this.questions = questions;
		}
		if (enableInt == LESSON_ENABLED) {
			this.enabled = true;
		} else {
			this.enabled = false;
		}
		random = new Random();
	}
	
	public Lesson(String lessonName, String description, String category, 
			long categoryId, int enableInt, long remoteId, long blockSetId, 
			ArrayList<Question> questions, float price) {
		this(lessonName, description, category, categoryId, enableInt, 0, remoteId, blockSetId, null, price);
	}

	public Question getRandomQuestion() {
		int index = random.nextInt(questions.size());
		return questions.get(index);
	}
	
	public Question getQuestion(int index) {
		return questions.get(index);
	}
	
	public void setBlockSet(int blockSetId) {
		this.blockSetId = blockSetId;
	}
	
	public String getPrice(){
		if (price == 0.0) {
			return "Free";
		} else {
			return String.valueOf(price);
		}
	}

}
