package com.cubes.learningcubes;

import java.util.ArrayList;

public class Category {
	long id;
	String name;
	ArrayList<Lesson> lessons;
	public Category(String name, long id, ArrayList<Lesson> mLessons) {
		this.name = name;
		this.id = id;
		if (mLessons != null) {
			this.lessons = mLessons;
		} else {
			this.lessons = new ArrayList<Lesson>();
		}
	}
	
	public Category(String name) {
		this(name, 0, null);
	}
	
	
}
