package com.cubes.learningcubes;

public class LogItem {
	  long sessionId;
	  long questionId;
	  long lessonId;
	  long id;
	  boolean questionResult;
	  String questionText;
	  
	  public LogItem(long id, long sessionId, long questionId, long lessonId, String questionText, boolean questionResult) {
		  this.sessionId = sessionId;
		  this.questionId = questionId;
		  this.lessonId = lessonId;
		  this.questionResult = questionResult;
		  this.questionText = questionText;
	  }

	  public LogItem(long sessionId, long questionId, long lessonId,  String questionText, boolean questionResult) {
		 this(-1, sessionId, questionId, lessonId, questionText, questionResult);
	  }
     
	  public static int QUESTION_CORRECT = 1;
	  public static int QUESTION_INCORRECT = 0;
	  
}
