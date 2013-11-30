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
	private Random random;
	boolean enabled;
	ArrayList<Question> questions;
	static int LESSON_ENABLED = 1;
	static int LESSON_DISABLED = 0;
	
	public Lesson(String lessonName, String description, int enableInt, long id, long remoteId, long blockSetId, ArrayList<Question> questions) {
		this.lessonName = lessonName;
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
	
	public Lesson(String lessonName, String description, int enableInt, long remoteId, long blockSetId, ArrayList<Question> questions) {
		this(lessonName, description, enableInt, 0, remoteId, blockSetId, null);
	}

	public Question getRandomQuestion() {
		int index = random.nextInt(questions.size());
		return questions.get(index);
	}
	
	public void setBlockSet(int blockSetId) {
		this.blockSetId = blockSetId;
	}

}
