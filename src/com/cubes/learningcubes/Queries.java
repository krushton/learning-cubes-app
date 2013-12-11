package com.cubes.learningcubes;

import com.cubes.learningcubes.DatabaseContract.BlockEntry;
import com.cubes.learningcubes.DatabaseContract.BlockSetEntry;
import com.cubes.learningcubes.DatabaseContract.CategoryEntry;
import com.cubes.learningcubes.DatabaseContract.LessonEntry;
import com.cubes.learningcubes.DatabaseContract.QuestionEntry;
import com.cubes.learningcubes.DatabaseContract.SessionEntry;
import com.cubes.learningcubes.DatabaseContract.SessionLogEntry;

public class Queries {

    	private static final String TEXT_TYPE = " TEXT";
    	private static final String INT_TYPE = " INTEGER";
    	private static final String FLOAT_TYPE = " REAL";
    	private static final String BLOB_TYPE = " BLOB";
    	private static final String COMMA_SEP = ",";
    
	 	 private static String getForeignKeyAssociation(String columnName, String parentTable, String parentColumnName) {
	    	return String.format("FOREIGN KEY(%s) REFERENCES %s(%s)",
	    			columnName, parentTable, parentColumnName);
	     }
	 	 
	 	static final String CREATE_CATEGORY_TABLE = "CREATE TABLE " + CategoryEntry.TABLE_NAME +
	    		" (" + CategoryEntry._ID + " INTEGER PRIMARY KEY," +
	    		CategoryEntry.CATEGORY_NAME + TEXT_TYPE + " );";

	 	 static final String CREATE_BLOCK_SET_TABLE = "CREATE TABLE " + BlockSetEntry.TABLE_NAME +
		    		" (" + BlockSetEntry._ID + " INTEGER PRIMARY KEY," +
		    		BlockSetEntry.BLOCK_SET_ENABLED + INT_TYPE + COMMA_SEP +
		    		BlockSetEntry.BLOCK_SET_NAME + TEXT_TYPE + " );";
		    
	    static final String CREATE_BLOCK_TABLE = "CREATE TABLE " + BlockEntry.TABLE_NAME +
	    		" (" + BlockEntry._ID + " INTEGER PRIMARY KEY," +
	    		BlockEntry.BLOCK_TEXT + TEXT_TYPE + COMMA_SEP +
	    		BlockEntry.BLOCK_RFID_TAG + TEXT_TYPE + COMMA_SEP +
	    		BlockEntry.BLOCK_BLOCKSET_ID + INT_TYPE + COMMA_SEP + 
	    		BlockEntry.BLOCK_LOCAL_URL + TEXT_TYPE + COMMA_SEP + 
	    		BlockEntry.BLOCK_REMOTE_URL + TEXT_TYPE + COMMA_SEP + 
	    		getForeignKeyAssociation(BlockEntry.BLOCK_BLOCKSET_ID, BlockSetEntry.TABLE_NAME, BlockSetEntry._ID) + " );";
		    
	    static final String CREATE_LESSON_TABLE = "CREATE TABLE " + LessonEntry.TABLE_NAME +
	    		" (" + LessonEntry._ID + " INTEGER PRIMARY KEY," +
	    		LessonEntry.LESSON_BLOCK_SET_ID + INT_TYPE + COMMA_SEP +
	    		LessonEntry.LESSON_DESCRIPTION + TEXT_TYPE + COMMA_SEP +
	    		LessonEntry.LESSON_REMOTE_ID + INT_TYPE + COMMA_SEP +
	    		LessonEntry.LESSON_ENABLED + INT_TYPE + COMMA_SEP +
	    		LessonEntry.PRICE + FLOAT_TYPE + COMMA_SEP +
	    		LessonEntry.LESSON_NAME + TEXT_TYPE + COMMA_SEP + 
	    		LessonEntry.LESSON_CATEGORY + TEXT_TYPE + COMMA_SEP + 
	    		LessonEntry.LESSON_CATEGORY_ID + INT_TYPE + COMMA_SEP +
	    		LessonEntry.LESSON_RATING + INT_TYPE + COMMA_SEP +
	    		LessonEntry.LESSON_AUTHOR + TEXT_TYPE + COMMA_SEP +
	    		LessonEntry.START_SOUND_REMOTE_URL + TEXT_TYPE + COMMA_SEP +
	    		LessonEntry.START_SOUND_LOCAL_URL + TEXT_TYPE + COMMA_SEP +
	    		LessonEntry.END_SOUND_REMOTE_URL + TEXT_TYPE + COMMA_SEP +
	    		LessonEntry.END_SOUND_LOCAL_URL + TEXT_TYPE + COMMA_SEP +
	    		LessonEntry.CORRECT_SOUND_REMOTE_URL + TEXT_TYPE + COMMA_SEP +
	    		LessonEntry.CORRECT_SOUND_LOCAL_URL + TEXT_TYPE + COMMA_SEP +
	    		LessonEntry.INCORRECT_SOUND_REMOTE_URL + TEXT_TYPE + COMMA_SEP +
	    		LessonEntry.INCORRECT_SOUND_LOCAL_URL + TEXT_TYPE + COMMA_SEP +
	    		LessonEntry.LESSON_DOWNLOAD_STATUS + INT_TYPE + COMMA_SEP +
	    		getForeignKeyAssociation(LessonEntry.LESSON_CATEGORY_ID, CategoryEntry.TABLE_NAME, CategoryEntry._ID) +
	    		getForeignKeyAssociation(LessonEntry.LESSON_BLOCK_SET_ID, BlockSetEntry.TABLE_NAME, BlockSetEntry._ID) + " );";   
	    
	    static final String CREATE_SESSION_TABLE = "CREATE TABLE " + SessionEntry.TABLE_NAME +
	    		" (" + SessionEntry._ID + " INTEGER PRIMARY KEY," +
	    		SessionEntry.SESSION_LENGTH + INT_TYPE + COMMA_SEP +
	    		SessionEntry.SESSON_LESSON_ID + INT_TYPE + COMMA_SEP +
	    		SessionEntry.SESSION_DATE + INT_TYPE + COMMA_SEP +
	    		getForeignKeyAssociation(SessionEntry.SESSON_LESSON_ID, LessonEntry.TABLE_NAME, LessonEntry._ID) + " );";

	    
	    static final String CREATE_QUESTION_TABLE = "CREATE TABLE " + QuestionEntry.TABLE_NAME +
	    		" (" + QuestionEntry._ID + " INTEGER PRIMARY KEY," +
	    		QuestionEntry.QUESTION_TEXT + TEXT_TYPE + COMMA_SEP +
	    		QuestionEntry.QUESTION_ANSWER + TEXT_TYPE + COMMA_SEP +
	    		QuestionEntry.QUESTION_LOCAL_URL + TEXT_TYPE + COMMA_SEP +
	    		QuestionEntry.QUESTION_REMOTE_URL + TEXT_TYPE + COMMA_SEP +
	    		QuestionEntry.QUESTION_LESSON_ID + INT_TYPE + COMMA_SEP +
	     		getForeignKeyAssociation(QuestionEntry.QUESTION_LESSON_ID, LessonEntry.TABLE_NAME, LessonEntry._ID) + " );";
	    
	    static final String CREATE_LOG_TABLE = "CREATE TABLE " + SessionLogEntry.TABLE_NAME +
	    		" (" + SessionLogEntry._ID + " INTEGER PRIMARY KEY," +
	    		SessionLogEntry.LOG_QUESTION_RESULT + INT_TYPE + COMMA_SEP +
	    		SessionLogEntry.LOG_LESSON_ID + INT_TYPE + COMMA_SEP + 
	    		SessionLogEntry.LOG_QUESTION_ID + INT_TYPE + COMMA_SEP + 
	    		SessionLogEntry.LOG_SESSION_ID + INT_TYPE + COMMA_SEP + 
	    		getForeignKeyAssociation(SessionLogEntry.LOG_SESSION_ID, SessionEntry.TABLE_NAME, SessionEntry._ID) +
	    		getForeignKeyAssociation(SessionLogEntry.LOG_LESSON_ID, LessonEntry.TABLE_NAME, LessonEntry._ID) +
	    		getForeignKeyAssociation(SessionLogEntry.LOG_QUESTION_ID, QuestionEntry.TABLE_NAME, QuestionEntry._ID) + " );";
	   
	   static final String DELETE_LOG_TABLE = "DROP TABLE IF EXISTS " + SessionLogEntry.TABLE_NAME;
	   static final String DELETE_QUESTION_TABLE = "DROP TABLE IF EXISTS " + QuestionEntry.TABLE_NAME;
	   static final String DELETE_SESSION_TABLE = "DROP TABLE IF EXISTS " + SessionEntry.TABLE_NAME;
	   static final String DELETE_LESSON_TABLE = "DROP TABLE IF EXISTS " + LessonEntry.TABLE_NAME;
	   static final String DELETE_BLOCK_TABLE = "DROP TABLE IF EXISTS " + BlockEntry.TABLE_NAME;
	   static final String DELETE_BLOCK_SET_TABLE = "DROP TABLE IF EXISTS " + BlockSetEntry.TABLE_NAME;
	   static final String DELETE_CATEGORY_TABLE = "DROP TABLE IF EXISTS " + CategoryEntry.TABLE_NAME;
	   
	   
	   
}
