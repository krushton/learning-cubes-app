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
	int rating;
	String author;
	String startSoundRemoteUrl;
	String startSoundLocalUrl;
	String endSoundLocalUrl;
	String endSoundRemoteUrl;
	String correctSoundLocalUrl;
	String correctSoundRemoteUrl;
	String incorrectSoundRemoteUrl;
	String incorrectSoundLocalUrl;
	
	private Random random;
	
	public Lesson() {
		questions = new ArrayList<Question>();
	}
	
	public Lesson(String lessonName, String description, String category, long categoryId, int enableInt, 
			long id, long remoteId, long blockSetId, ArrayList<Question> questions, float price, 
			int rating, String author, 
			String startSoundRemoteUrl, String startSoundLocalUrl,
			String endSoundRemoteUrl, String endSoundLocalUrl,
			String correctSoundRemoteUrl, String correctSoundLocalUrl,
			String incorrectSoundRemoteUrl, String incorrectSoundLocalUrl
			) {
		this.lessonName = lessonName;
		this.category = category;
		this.categoryId = categoryId;
		this.id = id;
		this.remoteId = remoteId;
		this.description = description;
		this.price = price;
		this.blockSetId = blockSetId;
		this.rating = rating;
		this.author = author;
		this.startSoundRemoteUrl = startSoundRemoteUrl;
		this.startSoundLocalUrl = startSoundLocalUrl;
		this.endSoundLocalUrl = endSoundLocalUrl;
		this.endSoundRemoteUrl = endSoundRemoteUrl;
		this.correctSoundRemoteUrl = correctSoundRemoteUrl;
		this.correctSoundLocalUrl = correctSoundLocalUrl;
		this.incorrectSoundRemoteUrl = incorrectSoundRemoteUrl;
		this.incorrectSoundLocalUrl = incorrectSoundLocalUrl;
		
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
			ArrayList<Question> questions, float price,
			int rating, String author) {
		this(lessonName, description, category, 
			 categoryId, enableInt, 0, remoteId, blockSetId, 
			 null, price, rating, author,
			 "", "", "", "", "", "", "", "");
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
	
	public Question[] questionsAsArray() {
		Question[] list = new Question[questions.size()];
		for (int i = 0; i < questions.size(); i++) {
			list[i] = questions.get(i);
		}
		return list;
	}
	
	public String getPrice(){
		if (price == 0.0) {
			return "Free";
		} else {
			return String.valueOf(price);
		}
	}

}
