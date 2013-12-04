package com.cubes.learningcubes;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.joda.time.DateTime;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.cubes.learningcubes.DatabaseContract.BlockEntry;
import com.cubes.learningcubes.DatabaseContract.BlockSetEntry;
import com.cubes.learningcubes.DatabaseContract.CategoryEntry;
import com.cubes.learningcubes.DatabaseContract.LessonEntry;
import com.cubes.learningcubes.DatabaseContract.QuestionEntry;
import com.cubes.learningcubes.DatabaseContract.SessionEntry;
import com.cubes.learningcubes.DatabaseContract.SessionLogEntry;

public class CubesDbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 34;
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

        db.execSQL(Queries.CREATE_CATEGORY_TABLE);
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
        db.execSQL(Queries.DELETE_CATEGORY_TABLE);
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
    
    public ArrayList<Lesson> getLessonsByCategory(long id) {
    	getDbIfNecessary();
    	Cursor c = db.query(LessonEntry.TABLE_NAME, null, LessonEntry.LESSON_CATEGORY_ID + " = " + id, null, null, null, null);
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
    
    public ArrayList<Session> getSessionsByDate(DateTime date) {
    	getDbIfNecessary();
    	Cursor c = db.query(SessionEntry.TABLE_NAME, null, SessionEntry.SESSION_DATE + " = " + date.getMillis(), null, null, null, SessionEntry.SESSION_DATE + " ASC");
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
    
    public void changeEnabledLesson(long rowId) {
    	getDbIfNecessary();
    	Cursor q = db.query(LessonEntry.TABLE_NAME, null, null, null, null, null, null);
    	q.moveToFirst();
    	while(!q.isAfterLast()) {
    		long id = q.getLong(q.getColumnIndex(LessonEntry._ID));
    		ContentValues values = new ContentValues();
    		if (id == rowId) {
    			values.put(LessonEntry.LESSON_ENABLED, Lesson.LESSON_ENABLED);
    		} else {
    			values.put(LessonEntry.LESSON_ENABLED, Lesson.LESSON_DISABLED);
    		}
    		db.update(LessonEntry.TABLE_NAME, values, LessonEntry._ID + " = " + id, null);
    		q.moveToNext();
    	}
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
    	
    	String category = q.getString(q.getColumnIndex(LessonEntry.LESSON_CATEGORY));
    	long remoteId = q.getInt(q.getColumnIndex(LessonEntry.LESSON_REMOTE_ID));
    	long rowId = q.getInt(q.getColumnIndex(LessonEntry._ID));
    	long setId = q.getInt(q.getColumnIndex(LessonEntry.LESSON_BLOCK_SET_ID));
    	long categoryId = q.getInt(q.getColumnIndex(LessonEntry.LESSON_CATEGORY_ID));
    	int enabled = q.getInt(q.getColumnIndex(LessonEntry.LESSON_ENABLED));
    	float price = q.getFloat(q.getColumnIndex(LessonEntry.PRICE));
    	Cursor c = db.query(QuestionEntry.TABLE_NAME, null, QuestionEntry.QUESTION_LESSON_ID + " = " + rowId, null, null, null, null);
    	ArrayList<Question> questions = new ArrayList<Question>();
    	
    	c.moveToFirst();
    	while(!c.isAfterLast()) {
    		
        	Question question = getQuestionFromCursor(c);
        	questions.add(question);
        	c.moveToNext();
    	}
    	return new Lesson(name, description, category, categoryId, enabled, rowId, remoteId, setId, questions, price);
    }
    
    private Question getQuestionFromCursor(Cursor c) {    	
    	int colIndex = c.getColumnIndex(QuestionEntry._ID);
    	int id = c.getInt(colIndex);
    	int lessonId = c.getInt(c.getColumnIndex(QuestionEntry.QUESTION_LESSON_ID));
		String remoteUrl = c.getString(c.getColumnIndex(QuestionEntry.QUESTION_REMOTE_URL));
		String localUrl = c.getString(c.getColumnIndex(QuestionEntry.QUESTION_LOCAL_URL));
		String text = c.getString(c.getColumnIndex(QuestionEntry.QUESTION_TEXT));
    	String answer = c.getString(c.getColumnIndex(QuestionEntry.QUESTION_ANSWER));
    	return new Question(text, answer, id, lessonId, remoteUrl, localUrl);
    }
    
    public Session getSessionById(long rowId) {
    	db = this.getWritableDatabase();
    	Cursor q = db.query(SessionEntry.TABLE_NAME, null,  SessionEntry._ID + " = " + rowId, null, null, null, null);
    	q.moveToFirst();
    	return getSessionFromCursor(q);
    }
    
    public ArrayList<Session> getSessionsForLesson(long lessonId) {
    	db = this.getWritableDatabase();
    	Cursor q = db.query(SessionEntry.TABLE_NAME, null,  SessionEntry.SESSON_LESSON_ID + " = " + lessonId, null, null, null, null);
    	q.moveToFirst();
    	ArrayList<Session> sessions = new ArrayList();
       	while(!q.isAfterLast()) {
        	Session session = getSessionFromCursor(q);
        	sessions.add(session);
        	q.moveToNext();
    	}
    	return sessions;
    }
    
    public ArrayList<Category> getCategories() {
    	db = this.getWritableDatabase();
    	Cursor q = db.query(CategoryEntry.TABLE_NAME, null, null, null, null, null, null);
    	q.moveToFirst();
    	ArrayList<Category> results = new ArrayList<Category>();
		while(!q.isAfterLast()) {
        	Category category = getCategoryFromCursor(q);
        	results.add(category);
        	q.moveToNext();
    	}
		return results;
    }
    
    public Category getCategoryById(long id) {
    	db = this.getWritableDatabase();
    	Cursor q = db.query(CategoryEntry.TABLE_NAME, null, CategoryEntry._ID + " = " + id, null, null, null, null);
    	q.moveToFirst();
    	return getCategoryFromCursor(q);
    }
    
    public Category getCategoryFromCursor(Cursor c) {
    	long id = c.getInt(c.getColumnIndex(CategoryEntry._ID));
    	String name = c.getString(c.getColumnIndex(CategoryEntry.CATEGORY_NAME));
    	ArrayList<Lesson> lessons = getLessonsByCategory(id);
    	return new Category(name, id, lessons);
    }
    
    public ArrayList<Session> getSessionsByCategory(long categoryId) {
    	db = this.getWritableDatabase();
    	ArrayList<Lesson> lessons = getLessonsByCategory(categoryId);
    	ArrayList<Session> sessions = new ArrayList<Session>();
    	for (Lesson lesson : lessons) {
    		Cursor q = db.query(SessionEntry.TABLE_NAME, null,  SessionEntry.SESSON_LESSON_ID + " = " + lesson.id, null, null, null, null);
    		q.moveToFirst();
    		while(!q.isAfterLast()) {
            	Session session = getSessionFromCursor(q);
            	sessions.add(session);
            	q.moveToNext();
        	}
    	}
    	return sessions;
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
    	long dateInMillis = q.getLong(q.getColumnIndex(SessionEntry.SESSION_DATE));
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
    
    public long addCategory(Category category) {
    	getDbIfNecessary();
    	ContentValues values = new ContentValues();
    	values.put(CategoryEntry.CATEGORY_NAME, category.name);
    	return db.insert(CategoryEntry.TABLE_NAME, null, values);
    }
    
    public long addLesson(Lesson lesson) {
    	getDbIfNecessary();
    	ContentValues values = new ContentValues();
    	values.put(LessonEntry.LESSON_NAME, lesson.lessonName);
    	values.put(LessonEntry.LESSON_DESCRIPTION, lesson.description);
    	values.put(LessonEntry.LESSON_CATEGORY, lesson.category);
    	values.put(LessonEntry.LESSON_CATEGORY_ID, lesson.categoryId);
    	values.put(LessonEntry.LESSON_BLOCK_SET_ID, lesson.blockSetId);
    	values.put(LessonEntry.LESSON_REMOTE_ID, lesson.remoteId);
    	values.put(LessonEntry.PRICE, lesson.price);
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
    	values.put(SessionEntry.SESSION_DATE, session.datetime.getMillis());  	 
    	values.put(SessionEntry.SESSON_LESSON_ID, session.lessonId);
    	return db.insert(SessionEntry.TABLE_NAME, null, values);
    }
   
    public long addBlockSet(BlockSet set) {
    	getDbIfNecessary();
    	ContentValues values = new ContentValues();
    	values.put(BlockSetEntry.BLOCK_SET_NAME, set.name);
    	if (set.enabled) {
    		values.put(BlockSetEntry.BLOCK_SET_ENABLED, BlockSet.ENABLED);
    	} else {
    		values.put(BlockSetEntry.BLOCK_SET_ENABLED, BlockSet.DISABLED);
    	}
    	long result = db.insert(BlockSetEntry.TABLE_NAME, null, values);
    	Cursor c = db.query(BlockSetEntry.TABLE_NAME, null, BlockSetEntry._ID + " = " + result, null, null, null, null);
    	c.moveToFirst();
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
    	values.put(QuestionEntry.QUESTION_LOCAL_URL, question.localUrl);
    	values.put(QuestionEntry.QUESTION_REMOTE_URL, question.remoteUrl);
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
    	
    	String[] categoryNames =  { "Spelling", "Art", "Science", "Music", "History", "Government", "Math",
    			"Reading", "Geography" };
    	
    	Category spellingCategory = null;
    	Category mathCategory = null;
    	
    	for (int i = 0; i < categoryNames.length; i++) {
    		Category category = new Category(categoryNames[i]);
    		long id = addCategory(category);
    		category.id = id;
    		if (categoryNames[i].equals("Spelling")) {
    			spellingCategory = category;
    		}
    		else if (categoryNames[i].equals("Math")) {
    			mathCategory = category;
    		}
    	}
    	
    	String spellingLessonName = "Spelling animals";
    	Lesson spellingLesson = new Lesson(spellingLessonName, "Practice spelling with animal name words", 
    			spellingCategory.name, spellingCategory.id, Lesson.LESSON_ENABLED, -1, alphaBlockSetId, null, 0.0f);
    	final long spellingLessonId = addLesson(spellingLesson);

    	HashMap<String,String> questions = new HashMap<String, String>();
    	questions.put("How do you spell cat?", "c|a|t");
		questions.put("How do you spell dog?", "d|o|g");
		questions.put("How do you spell fish?", "f|i|s|h");
		questions.put("How do you spell frog?", "f|r|o|g");
		questions.put("How do you spell horse?", "h|o|r|s|e");
		questions.put("How do you spell goat?", "g|o|a|t");
		questions.put("How do you spell pig?", "p|i|g");
		questions.put("How do you spell mouse?", "m|o|u|s|e");
		questions.put("How do you spell rooster?", "r|o|o|s|t|e|r");
		questions.put("How do you spell lizard?", "l|i|z|a|r|d");
		questions.put("How do you spell eagle?", "e|a|g|l|e");
		questions.put("How do you spell antelope?", "a|n|t|e|l|o|p|e");	
		
		ArrayList<Long> letterIds = new ArrayList<Long>();

		for (String key : questions.keySet()) {
			Question q = new Question(key, questions.get(key), spellingLessonId, "", "");
			long id = addQuestion(q);
			letterIds.add(id);
		}
		
		String spellVerbs = "Spelling Verbs";
		Lesson spellingPlaceLesson = new Lesson(spellVerbs, "Beginning spelling of verb words",
				spellingCategory.name, spellingCategory.id, Lesson.LESSON_DISABLED, -1, alphaBlockSetId, null, 0.0f);
		
		final long spellingLesson2Id = addLesson(spellingPlaceLesson);
		
		HashMap<String,String> spellQuestions = new HashMap<String, String>();
		spellQuestions.put("How do you spell run?", "r|u|n");
		spellQuestions.put("How do you spell play?", "p|l|a|y");
		spellQuestions.put("How do you spell dance?", "d|a|n|c|e");
		spellQuestions.put("How do you spell sing?", "s|i|n|g");
		spellQuestions.put("How do you spell hug?", "h|u|g");
		spellQuestions.put("How do you spell eat?", "e|a|t");
		spellQuestions.put("How do you spell sleep?", "s|l|e|e|p");
		spellQuestions.put("How do you spell read?", "r|e|a|d");
		spellQuestions.put("How do you spell swim?", "s|w|i|m");
		spellQuestions.put("How do you spell wash?", "w|a|s|h");
		spellQuestions.put("How do you spell brush?", "b|r|u|s|h");
		spellQuestions.put("How do you spell jump?", "j|u|m|p");
		spellQuestions.put("How do you spell walk?", "w|a|k|k");
		spellQuestions.put("How do you spell skip?", "s|k|i|p");
		spellQuestions.put("How do you spell cook?", "c|o|o|k");
		spellQuestions.put("How do you spell drive?", "d|r|i|v|e");
		ArrayList<Long> spellVerbIds = new ArrayList<Long>();
		
		for (String key : spellQuestions.keySet()) {
			Question q = new Question(key, questions.get(key), spellingLesson2Id, "", "");
			long id = addQuestion(q);
			spellVerbIds.add(id);
		}
		
		
		String mathLessonName = "Basic addition";
		Lesson mathLesson = new Lesson(mathLessonName, "Practice adding single digit numbers", 
			 mathCategory.name, mathCategory.id, Lesson.LESSON_DISABLED, -1, numberSetId, null, 0.0f);
		final long mathLessonId = addLesson(mathLesson);
		
		HashMap<String,String> moreQuestions = new HashMap<String, String>();
		moreQuestions.put("What is 2 plus 2?", "4");
		moreQuestions.put("What is 6 plus 3?", "9");
		moreQuestions.put("What is 3 plus 1?", "4");
		moreQuestions.put("What is 1 plus 1?", "2");
		moreQuestions.put("What is 5 plus 1?", "6");
		moreQuestions.put("What is 3 plus 2?", "5");
		moreQuestions.put("What is 0 plus 1?", "1");
		moreQuestions.put("What is 1 plus 8?", "9");
		moreQuestions.put("What is 2 plus 4?", "6");
		moreQuestions.put("What is 6 plus 1?", "7");
		moreQuestions.put("What is 3 plus 3?", "6");
		moreQuestions.put("What is 3 plus 6?", "9");
		moreQuestions.put("What is 7 plus 1?", "8");
		moreQuestions.put("What is 4 plus 0?", "4");
		moreQuestions.put("What is 0 plus 0?", "0");
		moreQuestions.put("What is 4 plus 4?", "8");
		
		ArrayList<Long> numberIds = new ArrayList<Long>();
		
		for (String key : moreQuestions.keySet()) {
			Question q = new Question(key, questions.get(key), mathLessonId, "", "");
			long id = addQuestion(q);
			numberIds.add(id);
		}
		
		DateTime first = DateTime.now().minusDays(13);		
		DateTime second = DateTime.now().minusDays(10);
		DateTime third = DateTime.now().minusDays(9);
		DateTime fourth = DateTime.now().minusDays(6);
		DateTime fifth = DateTime.now().minusDays(2);
		
		Session spellingSessionFirst = new Session(first.getMillis(), 2500, spellingLessonName, spellingLessonId, null);
		long spellingSessionFirstId = addSession(spellingSessionFirst);
		
		Session mathSessionFirst = new Session(first.getMillis(), 3000, mathLessonName, mathLessonId, null);
		long mathSessionFirstId = addSession(mathSessionFirst);
		
		Session spellingSessionSecond = new Session(second.getMillis(), 1200, spellingLessonName, spellingLessonId, null);
		long spellingSessionSecondId = addSession(spellingSessionSecond);
		
		Session mathSessionSecond = new Session(third.getMillis(), 1900, mathLessonName, mathLessonId, null);
		long mathSessionSecondId = addSession(mathSessionSecond);
		
		Session spellingVerbSession = new Session(third.getMillis(), 1200, spellVerbs, spellingLesson2Id, null);
		long spellingVerbSessionId = addSession(spellingVerbSession);
		
		Session spellingVerbSecondSession = new Session(fourth.getMillis(), 2100, spellVerbs, spellingLesson2Id, null);
		long spellinVerbSessionSecondId = addSession(spellingVerbSecondSession);
		
		Session spellingVerbSessionThird = new Session(fifth.getMillis(), 2100, spellVerbs, spellingLesson2Id, null);
		long spellingVerbSessionThirdId = addSession(spellingVerbSessionThird);
		
		for (long questionId : letterIds) {
			String text = getQuestionById(questionId).text;
			
			LogItem log = new LogItem(spellingSessionFirstId, questionId, spellingLessonId, text, getRandomBoolean());
			LogItem log2 = new LogItem(spellingSessionSecondId, questionId, spellingLessonId, text, getRandomBoolean());
			addSessionLog(log);
			addSessionLog(log2);
		}
		
		for (long questionId : spellVerbIds) {
			String text = getQuestionById(questionId).text;
			
			LogItem log = new LogItem(spellingVerbSessionId, questionId, spellingLesson2Id, text, getRandomBoolean());
			LogItem log2 = new LogItem(spellinVerbSessionSecondId, questionId, spellingLesson2Id, text, getRandomBoolean());
			LogItem log3 = new LogItem(spellingVerbSessionThirdId, questionId, spellingLesson2Id, text, getRandomBoolean());

			addSessionLog(log);
			addSessionLog(log2);
			addSessionLog(log3);
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