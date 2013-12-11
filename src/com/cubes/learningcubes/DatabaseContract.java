package com.cubes.learningcubes;

import android.provider.BaseColumns;


public final class DatabaseContract {

    public DatabaseContract() {}

    /* Inner class that defines the table contents */
    public static abstract class LessonEntry implements BaseColumns {
        public static final String TABLE_NAME = "lesson";
        public static final String LESSON_REMOTE_ID = "remoteid";
        public static final String LESSON_NAME = "name";
        public static final String LESSON_CATEGORY_ID = "categoryid";
        public static final String LESSON_DESCRIPTION = "description";
        public static final String LESSON_ENABLED = "lessonenabled";
        public static final String LESSON_BLOCK_SET_ID = "blocksetid";
        public static final String LESSON_CATEGORY = "lessoncategory";
        public static final String LESSON_RATING = "rating";
        public static final String LESSON_AUTHOR = "author";
        public static final String START_SOUND_REMOTE_URL = "startsoundremoteurl";
        public static final String START_SOUND_LOCAL_URL = "startsoundlocalurl";
        public static final String END_SOUND_REMOTE_URL = "endsoundremoteurl";
        public static final String END_SOUND_LOCAL_URL = "endsoundlocalurl";
        public static final String CORRECT_SOUND_REMOTE_URL = "correctsoundremoteurl";
        public static final String CORRECT_SOUND_LOCAL_URL = "correctsoundlocalurl";
        public static final String INCORRECT_SOUND_REMOTE_URL = "incorrectsoundremoteurl";
        public static final String INCORRECT_SOUND_LOCAL_URL = "incorrectsoundlocalurl";
        public static final String PRICE = "price";
        public static final String LESSON_DOWNLOAD_STATUS = "lessondownloadstatus";
        
    }
    
    public static abstract class QuestionEntry implements BaseColumns {
        public static final String TABLE_NAME = "question";
        public static final String QUESTION_TEXT = "text";
        public static final String QUESTION_ANSWER = "answer";
        public static final String QUESTION_REMOTE_URL = "remoteurl";
        public static final String QUESTION_LOCAL_URL = "local";
        public static final String QUESTION_LESSON_ID = "lessonid";
    }
    
    
    public static abstract class SessionLogEntry implements BaseColumns {
        public static final String TABLE_NAME = "log";
        public static final String LOG_SESSION_ID = "sessionid";
        public static final String LOG_QUESTION_ID = "questionid";
        public static final String LOG_LESSON_ID = "lessonid";
        public static final String LOG_QUESTION_RESULT = "sessionresult";
    }
    
    public static abstract class SessionEntry implements BaseColumns {
        public static final String TABLE_NAME = "session";
        public static final String SESSION_DATE = "sessiondate";
        public static final String SESSON_LESSON_ID = "sessionlessonid";
        public static final String SESSION_LENGTH = "sessionlength";
    }
    
    public static abstract class BlockSetEntry implements BaseColumns {
        public static final String TABLE_NAME = "blockset";
        public static final String BLOCK_SET_NAME = "name";
        public static final String BLOCK_SET_ENABLED = "enabled";
        public static final String BLOCK_SET_REMOTE_ID = "blocksetremoteid";
    }
    
    public static abstract class BlockEntry implements BaseColumns {
        public static final String TABLE_NAME = "block";
        public static final String BLOCK_TEXT = "text";
        public static final String BLOCK_RFID_TAG = "tag";
        public static final String BLOCK_BLOCKSET_ID = "blocksetid";
        public static final String BLOCK_REMOTE_URL = "blockremoteurl";
        public static final String BLOCK_LOCAL_URL = "blocklocalurl";
    }
    
    public static abstract class CategoryEntry implements BaseColumns {
        public static final String TABLE_NAME = "category";
        public static final String CATEGORY_NAME = "name";
    }
}