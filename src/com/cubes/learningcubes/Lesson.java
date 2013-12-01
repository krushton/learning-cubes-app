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
	boolean enabled;
	ArrayList<Question> questions;
	static int LESSON_ENABLED = 1;
	static int LESSON_DISABLED = 0;
	static String CATEGORY_SPELLING = "Spelling";
	static String CATEGORY_READING = "Reading";
	static String CATEGORY_SCIENCE = "Science";
	static String CATEGORY_GEOGRAPHY = "Geography";
	static String CATEGORY_MATH = "Math";
	static String CATEGORY_HISTORY = "History";
	static String CATEGORY_MUSIC = "Music";
	static String CATEGORY_ART = "Art";
	static String CATEGORY_GOVERNMENT = "Government";
	private Random random;
	public Lesson(String lessonName, String description, String category, int enableInt, long id, long remoteId, long blockSetId, ArrayList<Question> questions) {
		this.lessonName = lessonName;
		this.category = category;
		this.id = id;
		this.remoteId = remoteId;
		this.description = description;
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
	
	public Lesson(String lessonName, String description, String category, int enableInt, long remoteId, long blockSetId, ArrayList<Question> questions) {
		this(lessonName, description, category, enableInt, 0, remoteId, blockSetId, null);
	}

	public Question getRandomQuestion() {
		int index = random.nextInt(questions.size());
		return questions.get(index);
	}
	
	public void setBlockSet(int blockSetId) {
		this.blockSetId = blockSetId;
	}

}
