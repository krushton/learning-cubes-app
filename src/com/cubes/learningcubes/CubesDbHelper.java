package com.cubes.learningcubes;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Random;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.cubes.learningcubes.DatabaseContract.BlockEntry;
import com.cubes.learningcubes.DatabaseContract.BlockSetEntry;
import com.cubes.learningcubes.DatabaseContract.LessonEntry;
import com.cubes.learningcubes.DatabaseContract.QuestionEntry;
import com.cubes.learningcubes.DatabaseContract.SessionEntry;
import com.cubes.learningcubes.DatabaseContract.SessionLogEntry;

public class CubesDbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 19;
    public static final String DATABASE_NAME = "Cubes.db";
    private SQLiteDatabase db;
    private Random random;
    private final String TAG = "CubesDbHelper";
    private static CubesDbHelper sInstance = null;

    
    public static CubesDbHelper getInstance(Context context) {
        
        // Use the application context, which will ensure that you 
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
          sInstance = new CubesDbHelper(context.getApplicationContext());
        }
        return sInstance;
      }
         
      /**
       * Constructor should be private to prevent direct instantiation.
       * make call to static factory method "getInstance()" instead.
       */
      private CubesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        random = new Random();
      }
    
    
    public void onCreate(SQLiteDatabase db) {
        this.db = db;
        db.execSQL(Queries.CREATE_BLOCK_SET_TABLE);
        db.execSQL(Queries.CREATE_BLOCK_TABLE);
        db.execSQL(Queries.CREATE_LESSON_TABLE);
        db.execSQL(Queries.CREATE_SESSION_TABLE);
        db.execSQL(Queries.CREATE_QUESTION_TABLE);
        db.execSQL(Queries.CREATE_LOG_TABLE);
        populateDatabase();
    }
    
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(Queries.DELETE_LOG_TABLE);
        db.execSQL(Queries.DELETE_QUESTION_TABLE);
        db.execSQL(Queries.DELETE_SESSION_TABLE);
        db.execSQL(Queries.DELETE_LESSON_TABLE);
        db.execSQL(Queries.DELETE_BLOCK_TABLE);
        db.execSQL(Queries.DELETE_BLOCK_SET_TABLE);
        onCreate(db);
    }
    
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
    
    public void delete(String tableName, int rowId) {
    	getDbIfNecessary();
    	db.delete(tableName, "_id = " + rowId, null);
    	db.close();
    }
    
    public Block getBlockById(long rowId) {
    	getDbIfNecessary();
    	Cursor q = db.query(BlockEntry.TABLE_NAME, null, BlockEntry._ID + " = " + rowId, null, null, null, null);
    	q.moveToFirst();
    	return getBlockFromCursor(q);
    }
    
    public Question getQuestionById(long rowId) {
    	getDbIfNecessary();
    	String condition = QuestionEntry._ID + " = " + rowId;
    	Cursor q = db.query(QuestionEntry.TABLE_NAME, null, condition, null, null, null, null);
    	q.moveToFirst();
    	return getQuestionFromCursor(q);
    }
    
    private Block getBlockFromCursor(Cursor q) {
    	getDbIfNecessary();
    	long rowId = q.getInt(q.getColumnIndex(BlockEntry._ID));
    	String text = q.getString(q.getColumnIndex(BlockEntry.BLOCK_TEXT));
    	String tag = q.getString(q.getColumnIndex(BlockEntry.BLOCK_RFID_TAG));
    	int blockSetId = q.getInt(q.getColumnIndex(BlockEntry.BLOCK_BLOCKSET_ID));
    	return new Block(text, tag, blockSetId, rowId);
    }
    
    public BlockSet getBlockSetById(long rowId) {
    	getDbIfNecessary();
    	Cursor q = db.query(BlockSetEntry.TABLE_NAME, null, BlockSetEntry._ID + " = " + rowId, null, null, null, null);
    	q.moveToFirst();
    	return getBlockSetFromCursor(q);
    }
    
    private BlockSet getBlockSetFromCursor(Cursor q) {
    	String name = q.getString(q.getColumnIndex(BlockSetEntry.BLOCK_SET_NAME));
    	long rowId = q.getInt(q.getColumnIndex(BlockSetEntry._ID));
    	int enabledInt = q.getInt(q.getColumnIndex(BlockSetEntry.BLOCK_SET_ENABLED));
    	boolean enabled = false;
    	if (enabledInt == BlockSet.ENABLED) {
    		enabled = true;
    	} 
    	Cursor c = db.query(BlockEntry.TABLE_NAME, null, BlockEntry.BLOCK_BLOCKSET_ID + " = " + rowId, null, null, null, null);
    	ArrayList<Block> blocks = new ArrayList<Block>();
    	
    	c.moveToFirst();
        while(!c.isAfterLast()) {
        	Block block = getBlockFromCursor(c);
        	blocks.add(block);
        	c.moveToNext();
    	}
    	return new BlockSet(name, enabled, blocks, rowId);
    }
    
    public ArrayList<Lesson> getLessons() {
    	getDbIfNecessary();
    	Cursor c = db.query(LessonEntry.TABLE_NAME, null, null, null, null, null, null);
    	ArrayList<Lesson> lessons = new ArrayList<Lesson>();
    	c.moveToFirst();
        while(!c.isAfterLast()) {
    		Lesson lesson = getLessonFromCursor(c);
    		lessons.add(lesson);
    		c.moveToNext();
    	}
    	return lessons;
    }
    
    public ArrayList<Session> getSessions() {
    	getDbIfNecessary();
    	Cursor c = db.query(SessionEntry.TABLE_NAME, null, null, null, null, null, SessionEntry.SESSION_DATE + " ASC");
    	ArrayList<Session> sessions = new ArrayList<Session>();
    	c.moveToFirst();
    	while(!c.isAfterLast()) {
    		Session session = getSessionFromCursor(c);
    		sessions.add(session);
    		c.moveToNext();
    	}
    	return sessions;
    }
    
    public Lesson getActiveLesson() {
    	getDbIfNecessary();
    	Cursor q = db.query(LessonEntry.TABLE_NAME, null,  LessonEntry.LESSON_ENABLED + " = " + Lesson.LESSON_ENABLED, null, null, null, null);
    	q.moveToFirst();
    	return getLessonFromCursor(q);
    }
    
    public Lesson getLessonById(long rowId) {
    	getDbIfNecessary();
    	Cursor q = db.query(LessonEntry.TABLE_NAME, null,  LessonEntry._ID + " = " + rowId, null, null, null, null);
    	q.moveToFirst();
    	return getLessonFromCursor(q);
    }
    
    public void changeEnabledBlockSet(long rowId) {
    	getDbIfNecessary();
    	Cursor q = db.query(BlockSetEntry.TABLE_NAME, null, null, null, null, null, null);
    	q.moveToFirst();
    	while(!q.isAfterLast()) {
    		long id = q.getLong(q.getColumnIndex(BlockSetEntry._ID));
    		ContentValues values = new ContentValues();
    		if (id == rowId) {
    			values.put(BlockSetEntry.BLOCK_SET_ENABLED, BlockSet.ENABLED);
    		} else {
    			values.put(BlockSetEntry.BLOCK_SET_ENABLED, BlockSet.DISABLED);
    		}
    		db.update(BlockSetEntry.TABLE_NAME, values, BlockSetEntry._ID + " = " + id, null);
    		q.moveToNext();
    	}
    	db.close();
    }
    
    public BlockSet getActiveBlockSet() {
    	getDbIfNecessary();
    	Cursor q = db.query(BlockSetEntry.TABLE_NAME, null, BlockSetEntry.BLOCK_SET_ENABLED + " = " + BlockSet.ENABLED, null, null, null, null);
    	q.moveToFirst();
    	return getBlockSetFromCursor(q);
    }
    
    public Block getBlockByTagValue(String rfidTag) {
    	BlockSet blockSet = getActiveBlockSet();
    	for (Block b : blockSet.set) {
    		if (b.tagId == rfidTag) {
    			return b;
    		}
    	}
    	return null;
    }
    
    public void remapBlock(long id, String newValue) {
    	Log.d(TAG, "ID : " + id);
    	Log.d(TAG, "VALUE : " + newValue);
    	getDbIfNecessary();
    	ContentValues values = new ContentValues();
    	values.put(BlockEntry.BLOCK_TEXT, newValue);
		db.update(BlockEntry.TABLE_NAME, values, BlockEntry._ID + " = " + id, null);
    }
    
    private Lesson getLessonFromCursor(Cursor q) {
    	String name = q.getString(q.getColumnIndex(LessonEntry.LESSON_NAME));
    	String description = q.getString(q.getColumnIndex(LessonEntry.LESSON_DESCRIPTION));
    	long remoteId = q.getInt(q.getColumnIndex(LessonEntry.LESSON_REMOTE_ID));
    	long rowId = q.getInt(q.getColumnIndex(LessonEntry._ID));
    	long setId = q.getInt(q.getColumnIndex(LessonEntry.LESSON_BLOCK_SET_ID));
    	int enabled = q.getInt(q.getColumnIndex(LessonEntry.LESSON_ENABLED));
    	Cursor c = db.query(QuestionEntry.TABLE_NAME, null, QuestionEntry.QUESTION_LESSON_ID + " = " + rowId, null, null, null, null);
    	ArrayList<Question> questions = new ArrayList<Question>();
    	
    	c.moveToFirst();
    	while(!c.isAfterLast()) {
        	Question question = getQuestionFromCursor(c);
        	questions.add(question);
        	c.moveToNext();
    	}
    	return new Lesson(name, description, enabled, rowId, remoteId, setId, questions);
    }
    
    private Question getQuestionFromCursor(Cursor c) {    	
    	int colIndex = c.getColumnIndex(QuestionEntry._ID);
    	int id = c.getInt(colIndex);
    	int lessonId = c.getInt(c.getColumnIndex(QuestionEntry.QUESTION_LESSON_ID));
		String text = c.getString(c.getColumnIndex(QuestionEntry.QUESTION_TEXT));
    	String answer = c.getString(c.getColumnIndex(QuestionEntry.QUESTION_ANSWER));
    	return new Question(text, answer, id, lessonId);
    }
    
    public Session getSessionById(long rowId) {
    	db = this.getWritableDatabase();
    	Cursor q = db.query(SessionEntry.TABLE_NAME, null,  SessionEntry._ID + " = " + rowId, null, null, null, null);
    	q.moveToFirst();
    	return getSessionFromCursor(q);
    }
    
    private LogItem getLogFromCursor(Cursor c) {
    	long id = c.getInt(c.getColumnIndex(SessionLogEntry._ID));
    	long sessionId = c.getInt(c.getColumnIndex(SessionLogEntry.LOG_SESSION_ID));
    	long lessonId = c.getInt(c.getColumnIndex(SessionLogEntry.LOG_LESSON_ID));
		long questionId = c.getInt(c.getColumnIndex(SessionLogEntry.LOG_QUESTION_ID));

    	Cursor q = db.query(QuestionEntry.TABLE_NAME, null,  "_id = " + questionId, null, null, null, null);
    	q.moveToFirst();
    	String questionText = q.getString(q.getColumnIndex(QuestionEntry.QUESTION_TEXT));
    	
    	int result = c.getInt(c.getColumnIndex(SessionLogEntry.LOG_QUESTION_RESULT));
    	boolean res = false;
    	if (result == LogItem.QUESTION_CORRECT){
  			res = true;
  		} 
    	return new LogItem(id, sessionId, questionId, lessonId, questionText, res);
    }
    
    private Session getSessionFromCursor(Cursor q) {
    	int dateInMillis = q.getInt(q.getColumnIndex(SessionEntry.SESSION_DATE));
    	long rowId = q.getInt(q.getColumnIndex(SessionEntry._ID));
    	int lengthInSeconds= q.getInt(q.getColumnIndex(SessionEntry.SESSION_LENGTH));
    	long lessonId = q.getLong(q.getColumnIndex(SessionEntry.SESSON_LESSON_ID));

    	Cursor l = db.query(LessonEntry.TABLE_NAME, null, LessonEntry._ID + " = " + lessonId, null, null, null, null);
    	l.moveToFirst();
    	
    	String lessonName = l.getString(l.getColumnIndex(LessonEntry.LESSON_NAME));
    	
    	Cursor c = db.query(SessionLogEntry.TABLE_NAME, null, SessionLogEntry.LOG_SESSION_ID + " = " + rowId, null, null, null, null);
    	ArrayList<LogItem> logs = new ArrayList<LogItem>();
    	
    	int numCorrect = 0;
    	int numTried = 0;
    	
    	c.moveToFirst();
    	while(!c.isAfterLast()) {
    		LogItem log = getLogFromCursor(c);
        	if (log.questionResult) {
      			numCorrect += 1;
      		} 
      		numTried += 1;
			logs.add(log);
			c.moveToNext();
        
    	}
    	return new Session(dateInMillis,lengthInSeconds, numCorrect, numTried, lessonName, lessonId, rowId, logs );
    }
    
    
    public ArrayList<BlockSet> getBlockSets() {
    	getDbIfNecessary();
    	Cursor c = db.query(BlockSetEntry.TABLE_NAME, null, null, null, null, null, null);
    	ArrayList<BlockSet> sets = new ArrayList<BlockSet>();
    	
    	c.moveToFirst();
    	while(!c.isAfterLast()) {
    		BlockSet set = getBlockSetFromCursor(c);
    		sets.add(set);
    		c.moveToNext();
    	}
    	return sets;
    }
    
    public long addLesson(Lesson lesson) {
    	getDbIfNecessary();
    	ContentValues values = new ContentValues();
    	values.put(LessonEntry.LESSON_NAME, lesson.lessonName);
    	values.put(LessonEntry.LESSON_DESCRIPTION, lesson.description);
    	values.put(LessonEntry.LESSON_BLOCK_SET_ID, lesson.blockSetId);
    	values.put(LessonEntry.LESSON_REMOTE_ID, lesson.remoteId);
    	if (lesson.enabled) {
    		values.put(LessonEntry.LESSON_ENABLED, Lesson.LESSON_ENABLED);
    	} else {
    		values.put(LessonEntry.LESSON_ENABLED, Lesson.LESSON_DISABLED);
    	}
    	return db.insert(LessonEntry.TABLE_NAME, null, values);
    }
    
    public long addBlock(Block block) {
    	getDbIfNecessary();
    	ContentValues values = new ContentValues();
    	values.put(BlockEntry.BLOCK_TEXT, block.text);
    	values.put(BlockEntry.BLOCK_RFID_TAG, block.tagId);
    	values.put(BlockEntry.BLOCK_BLOCKSET_ID, block.blockSetId);    	
    	return db.insert(BlockEntry.TABLE_NAME, null, values);
    }
    
    public long addSession(Session session) {
    	getDbIfNecessary();
    	ContentValues values = new ContentValues();
    	values.put(SessionEntry.SESSION_LENGTH, session.sessionLength);
    	values.put(SessionEntry.SESSION_DATE, session.calendar.getTimeInMillis());  	 
    	values.put(SessionEntry.SESSON_LESSON_ID, session.lessonId);
    	return db.insert(SessionEntry.TABLE_NAME, null, values);
    }
   
    public long addBlockSet(BlockSet set) {
    	getDbIfNecessary();
    	ContentValues values = new ContentValues();
    	Log.d(TAG, "NOW ADDING TO DB: " + set.name);
    	values.put(BlockSetEntry.BLOCK_SET_NAME, set.name);
    	if (set.enabled) {
    		values.put(BlockSetEntry.BLOCK_SET_ENABLED, BlockSet.ENABLED);
    	} else {
    		values.put(BlockSetEntry.BLOCK_SET_ENABLED, BlockSet.DISABLED);
    	}
    	long result = db.insert(BlockSetEntry.TABLE_NAME, null, values);
    	Cursor c = db.query(BlockSetEntry.TABLE_NAME, null, BlockSetEntry._ID + " = " + result, null, null, null, null);
    	c.moveToFirst();
    	Log.d(TAG, "NOW NAME IS : " + c.getString(c.getColumnIndex(BlockSetEntry.BLOCK_SET_NAME)));
    	return result;
    }
    
    public long addSessionLog(LogItem log) {
    	getDbIfNecessary();
    	ContentValues values = new ContentValues();
    	values.put(SessionLogEntry.LOG_QUESTION_ID, log.questionId);
    	values.put(SessionLogEntry.LOG_LESSON_ID, log.lessonId);
    	if (log.questionResult) {
    		values.put(SessionLogEntry.LOG_QUESTION_RESULT, log.QUESTION_CORRECT);
    	} else {
    		values.put(SessionLogEntry.LOG_QUESTION_RESULT, log.QUESTION_INCORRECT);
    	}
    	values.put(SessionLogEntry.LOG_SESSION_ID, log.sessionId);
    	return db.insert(SessionLogEntry.TABLE_NAME, null, values);
    }
    
    public long addQuestion(Question question) {
    	getDbIfNecessary();
    	ContentValues values = new ContentValues();
    	values.put(QuestionEntry.QUESTION_TEXT, question.text);
    	values.put(QuestionEntry.QUESTION_ANSWER, question.answer);
    	values.put(QuestionEntry.QUESTION_LESSON_ID, question.lessonId);
    	return db.insert(QuestionEntry.TABLE_NAME, null, values);
    }
    
 
    private void populateDatabase() {
    	
    	BlockSet alphaSet = new BlockSet("Alphabet Letters", true, null);
    	final long alphaBlockSetId = addBlockSet(alphaSet);
    	Log.d(TAG, "NEW BLOCK SET ID: " + alphaBlockSetId);
    	
    	for(char letter = 'A'; letter <= 'Z'; letter++) {
    	    String l = String.valueOf(letter);
    	    Block b = new Block(l, null, alphaBlockSetId);
    	    addBlock(b);
    	}
    	
    	BlockSet numberSet = new BlockSet("Math Symbols", false, null);
    	final long numberSetId = addBlockSet(numberSet);
    	Log.d(TAG, "NEW BLOCK SET ID: " + numberSetId);
    	
    	for(int i = 0; i <= 9; i++) {
    	    String num = String.valueOf(i);
    	    Block b = new Block(num, null, numberSetId);
    	    addBlock(b);
    	}

    	for (char c : new char[]{'+', '-', '*', '/'}) {
    		String symb = String.valueOf(c);
    	    Block b = new Block(symb, null, numberSetId);
    	    addBlock(b);
    	}
    	
    	String spellingLessonName = "Spelling animals";
    	Lesson spellingLesson = new Lesson(spellingLessonName, "Practice spelling with animal name words", 
    			Lesson.LESSON_ENABLED, -1, alphaBlockSetId, null);
    	final long spellingLessonId = addLesson(spellingLesson);

    	HashMap<String,String> questions = new HashMap<String, String>();
    	questions.put("How do you spell cat?", "{c}{a}{t}");
		questions.put("How do you spell dog?", "{d}{o}{g}");
		questions.put("How do you spell fish?", "{f}{i}{s}{h}");
		questions.put("How do you spell frog?", "{f}{r}{o}{g}");
		questions.put("How do you spell horse?", "{h}{o}{r}{s}{e}");
		questions.put("How do you spell goat?", "{g}{o}{a}{t}");
		questions.put("How do you spell pig?", "{p}{i}{g}");
		questions.put("How do you spell mouse?", "{m}{o}{u}{s}{e}");
		questions.put("How do you spell rooster?", "{r}{o}{o}{s}{t}{e}{r}");
		questions.put("How do you spell lizard?", "{l}{i}{z}{a}{r}{d}");
		questions.put("How do you spell eagle?", "{e}{a}{g}{l}{e}");
		questions.put("How do you spell antelope?", "{a}{n}{t}{e}{l}{o}{p}{e}");	
		
		ArrayList<Long> letterIds = new ArrayList<Long>();

		for (String key : questions.keySet()) {
			Question q = new Question(key, questions.get(key), spellingLessonId);
			long id = addQuestion(q);
			letterIds.add(id);
		}
		
		String mathLessonName = "Basic math";
		Lesson mathLesson = new Lesson(mathLessonName, "Short lesson giving practice adding single digit numbers", 
				Lesson.LESSON_DISABLED, -1, numberSetId, null);
		final long mathLessonId = addLesson(mathLesson);
		
		HashMap<String,String> moreQuestions = new HashMap<String, String>();
		moreQuestions.put("What is 2 plus 2?", "{4}");
		moreQuestions.put("What is 6 plus 3?", "{9}");
		moreQuestions.put("What is 3 plus 1?", "{4}");
		moreQuestions.put("What is 1 plus 1?", "{2}");
		moreQuestions.put("What is 5 plus 1?", "{6}");
		moreQuestions.put("What is 3 plus 2?", "{5}");
		moreQuestions.put("What is 0 plus 1?", "{1}");
		moreQuestions.put("What is 1 plus 8?", "{9}");
		moreQuestions.put("What is 2 plus 4?", "{6}");
		moreQuestions.put("What is 6 plus 1?", "{7}");
		moreQuestions.put("What is 3 plus 3?", "{6}");
		moreQuestions.put("What is 3 plus 6?", "{9}");
		moreQuestions.put("What is 7 plus 1?", "{8}");
		moreQuestions.put("What is 4 plus 0?", "{4}");
		moreQuestions.put("What is 0 plus 0?", "{0}");
		moreQuestions.put("What is 4 plus 4?", "{8}");
		
		ArrayList<Long> numberIds = new ArrayList<Long>();
		
		for (String key : moreQuestions.keySet()) {
			Question q = new Question(key, questions.get(key), mathLessonId);
			long id = addQuestion(q);
			numberIds.add(id);
		}
		
		Calendar twentyFifth = Calendar.getInstance();
		twentyFifth.set(Calendar.DATE, 25);
		twentyFifth.set(Calendar.YEAR, 2013);
		twentyFifth.set(Calendar.MONTH, Calendar.NOVEMBER);
		
		Calendar twentySixth = Calendar.getInstance();
		twentySixth.set(Calendar.DATE, 26);
		twentySixth.set(Calendar.YEAR, 2013);
		twentySixth.set(Calendar.MONTH, Calendar.NOVEMBER);
		
		Calendar twentySeventh = Calendar.getInstance();
		twentySeventh.set(Calendar.DATE, 27);
		twentySeventh.set(Calendar.YEAR, 2013);
		twentySeventh.set(Calendar.MONTH, Calendar.NOVEMBER);
		
		Session spellingSessionFirst = new Session(twentyFifth.getTimeInMillis(), 2500, spellingLessonName, spellingLessonId, null);
		long spellingSessionFirstId = addSession(spellingSessionFirst);
		
		Session mathSessionFirst = new Session(twentySixth.getTimeInMillis(), 3000, mathLessonName, mathLessonId, null);
		long mathSessionFirstId = addSession(mathSessionFirst);
		
		Session spellingSessionSecond = new Session(twentyFifth.getTimeInMillis(), 1200, spellingLessonName, spellingLessonId, null);
		long spellingSessionSecondId = addSession(spellingSessionSecond);
		
		Session mathSessionSecond = new Session(twentySeventh.getTimeInMillis(), 1900, mathLessonName, mathLessonId, null);
		long mathSessionSecondId = addSession(mathSessionSecond);
		
		for (long questionId : letterIds) {
			String text = getQuestionById(questionId).text;
			
			LogItem log = new LogItem(spellingSessionFirstId, questionId, spellingLessonId, text, getRandomBoolean());
			LogItem log2 = new LogItem(spellingSessionSecondId, questionId, spellingLessonId, text, getRandomBoolean());
			addSessionLog(log);
			addSessionLog(log2);
		}

		for (long questionId : numberIds) {
			String text = getQuestionById(questionId).text;
			
			LogItem log = new LogItem(mathSessionFirstId, questionId, mathLessonId, text, getRandomBoolean());
			LogItem log2 = new LogItem(mathSessionSecondId, questionId, mathLessonId, text, getRandomBoolean());
			addSessionLog(log);
			addSessionLog(log2);
		}
		
    }
    
    private void getDbIfNecessary() {
    	if (db == null) {
    		db = this.getWritableDatabase();
    	}
    }
    
    private boolean getRandomBoolean() {
    	int randInt = random.nextInt(7);
		if (randInt > 4) {
			return false;
		}
		return true;
    }
}