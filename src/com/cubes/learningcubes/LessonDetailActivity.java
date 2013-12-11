package com.cubes.learningcubes;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.cubes.learningcubes.DatabaseContract.LessonEntry;
import com.cubes.learningcubes.DatabaseContract.QuestionEntry;
import com.cubes.learningcubes.DatabaseContract.SessionEntry;
import com.cubes.learningcubes.DatabaseContract.SessionLogEntry;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;


public class LessonDetailActivity extends Activity {

	private CubesDbHelper db;
	private Lesson lesson;
	private String TAG = "Search Results Activity";
	private LinearLayout layout;
	private LessonQuestionListAdapter adapter;
	private Button downloadButton;
	private Button deleteButton;
	private boolean isAlreadyDownloaded = true;
	private BlockSet blockSet;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lesson);
		// Show the Up button in the action bar.
		setupActionBar();
		
		db = CubesDbHelper.getInstance(this);
		Bundle extras = getIntent().getExtras();
		long localId = 0;
		long remoteId = 0;
		layout = (LinearLayout)findViewById(R.id.lesson_detail_layout);
		downloadButton = (Button)findViewById(R.id.download_lesson_button);
		deleteButton = (Button)findViewById(R.id.delete_lesson_button);
		
		if (extras != null) {
			localId = extras.getLong("localId");
			remoteId = extras.getLong("remoteId");
		}
		if (localId != 0) {
			isAlreadyDownloaded = true;
			lesson = db.getLessonById(localId);
			blockSet = db.getBlockSetById(lesson.blockSetId);
			loadLessonDetails();
		} else {
			isAlreadyDownloaded = false;
			layout.setVisibility(View.GONE);
			LessonLoadTask task = new LessonLoadTask(this);
			task.execute(remoteId);
		}
		
		
	}
	
	private void loadLessonDetails() {
		
		layout.setVisibility(View.VISIBLE);
		getActionBar().setTitle(lesson.lessonName);
		
		TextView lessonNameTv = (TextView)findViewById(R.id.lesson_name);
		lessonNameTv.setText(lesson.lessonName);
		
		TextView lessonDescriptionTv = (TextView)findViewById(R.id.lesson_description);
		lessonDescriptionTv.setText(lesson.description);
		
		TextView lessonNumberQuestionsTv = (TextView)findViewById(R.id.lesson_number_questions);
		lessonNumberQuestionsTv.setText(lesson.questions.size()+ " questions");
		
		TextView lessonCreator = (TextView)findViewById(R.id.lesson_author);
		lessonCreator.setText(lesson.author);
		
		TextView lessonBlockSetTv = (TextView)findViewById(R.id.lesson_blockset_name);
		lessonBlockSetTv.setText(blockSet.name);
	
		
		ListView lv = (ListView)findViewById(R.id.list);
		adapter = new LessonQuestionListAdapter(this, lesson.questionsAsArray());
		lv.setAdapter(adapter);
		
		if (!isAlreadyDownloaded) {
			
			downloadButton.setText(lesson.getPrice());
			downloadButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					
					String message = "";
					if (lesson.price == 0) {
						message = "Please confirm that you wish to download the lesson \"" 
								+ lesson.lessonName + "\"" + ".";
					} else {
						message = "Please confirm that you wish to purchase the lesson "
								+ lesson.lessonName + " for " + lesson.getPrice() + ".";
					}
					
					new AlertDialog.Builder(LessonDetailActivity.this)
			        .setIcon(android.R.drawable.ic_dialog_alert)
			        .setTitle(R.string.confirm)
			        .setMessage(message)
			        .setPositiveButton(R.string.download, new DialogInterface.OnClickListener() {

			            @Override
			            public void onClick(DialogInterface dialog, int which) {
			            	Log.d(TAG, "About to download new lesson");
			            	
			            	//save lesson to db
			            	long savedId = db.addLesson(lesson);
			            	
			            	for (Question q : lesson.questions) {
			            		q.lessonId = savedId;
			            		db.addQuestion(q);
			            	}
			            	if (lesson.isAdvanced) {
			            		lesson.downloadStatus = Lesson.LESSON_AVAILABLE;
			            	}else {
			            		lesson.downloadStatus = Lesson.LESSON_DOWNLOADING;
			            		Intent serviceIntent = new Intent(LessonDetailActivity.this, LessonDownloadService.class);
				            	serviceIntent.putExtra("lessonId", savedId);
				            	startService(serviceIntent);
			            	}
			            	
			            	finish();  	
			            }

			        })
			        .setNegativeButton(R.string.cancel, null)
			        .show();
				}
				
			});
		} else {
			downloadButton.setVisibility(View.GONE);
			deleteButton.setVisibility(View.VISIBLE);
			deleteButton.setOnClickListener(new  OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub

					if (lesson.enabled) {
						new AlertDialog.Builder(LessonDetailActivity.this)
				        .setIcon(android.R.drawable.ic_dialog_alert)
				        .setTitle(R.string.warning)
				        .setMessage(R.string.lesson_enabled_message)
				        .setPositiveButton(R.string.ok, null)
				        .show();
						
					} else {
						new AlertDialog.Builder(LessonDetailActivity.this)
				        .setIcon(android.R.drawable.ic_dialog_alert)
				        .setTitle(R.string.warning)
				        .setMessage(R.string.delete_lesson_message)
				        .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {

				            @Override
				            public void onClick(DialogInterface dialog, int which) {
				            	
				            	//find audio file paths...
				            	String[] paths = { 
				            			lesson.correctSoundLocalUrl,
				            			lesson.incorrectSoundLocalUrl,
				            			lesson.startSoundLocalUrl,
				            			lesson.endSoundLocalUrl
				            	};
				            	
				            	//delete any that exist
				            	for (String path : paths) {
				            		if (path != null) {
				            		
					            		File f = new File(path);
					            		boolean deleted = f.delete();
				            		}
				            	}
				            	
				            	
				            	
				            	//delete related sessions
				            	List<Session> sessions = db.getSessionsForLesson(lesson.id);
				            	for (Session s : sessions) {
				            		
				            		//but first have to delete related logs
				            		for (LogItem log : s.sessionLog) {
				            			db.delete(SessionLogEntry.TABLE_NAME, log.id);
				            		}
				            		//ok now can delete the session
				            		db.delete(SessionEntry.TABLE_NAME, s.id);
				            	
				            	}
				            	
				            	//find questions and delete them and their file paths
				            	for (Question q : lesson.questions) {
				            		Log.d(TAG, "Deleting question: " + q.text);
				            		db.delete(QuestionEntry.TABLE_NAME, q.id);
				            		String filePath = q.localUrl;
				            		File file = new File(filePath);
				            		boolean deleted = file.delete();
				            	}
				            	
				            	//delete lesson
				            	db.delete(LessonEntry.TABLE_NAME, lesson.id);
				            	Intent intent = new Intent(LessonDetailActivity.this, LessonsActivity.class);
				            	startActivity(intent);
				            	
				            }

				        })
				        .setNegativeButton(R.string.cancel, null)
				        .show();
					}
					
					
					
				}
				
			});
		}
		
	}

	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {

		getActionBar().setDisplayHomeAsUpEnabled(true);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.lesson_detail, menu);
		
		SearchManager searchManager =
		           (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		    SearchView searchView =
		            (SearchView) menu.findItem(R.id.search).getActionView();
		    searchView.setSearchableInfo(
		            searchManager.getSearchableInfo(getComponentName()));

		return true;
	}

	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
				NavUtils.navigateUpFromSameTask(this);
				return true;
			case R.id.action_mylessons:
				Intent i = new Intent(this, LessonsActivity.class);
				startActivity(i);
		}
		return super.onOptionsItemSelected(item);
	}
	
	private class LessonQuestionListAdapter extends ArrayAdapter<Question>{

        private final Context context;
        private final Question[] values;

        public LessonQuestionListAdapter(Context context, Question[] set) {
                
        //call the super class constructor and provide the ID of the resource to use instead of the default list view item
          super(context, R.layout.skinny_lesson_list_item, set);
          this.context = context;
          this.values = set;
        }
        
        //this method is called once for each item in the list
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

          LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
          View listItem = inflater.inflate(R.layout.skinny_lesson_list_item, parent, false);
        
          TextView text = (TextView)listItem.findViewById(R.id.question);
          text.setText(values[position].text);
          
          TextView answer = (TextView)listItem.findViewById(R.id.question_answer);
          answer.setText(values[position].getAnswerWithoutSeparators());
         
          return listItem;
        
        }
        
}
	private class LessonLoadTask extends AsyncTask<Long, Void, JSONObject> {
		private ProgressDialog dialog;
		private Activity activity;
	
		public LessonLoadTask(Activity activity) {
	        this.activity = activity;
	        dialog = new ProgressDialog(activity);
	    }
	    
		
		@Override
		protected void onPreExecute() {
	        this.dialog.setMessage("Loading...");
	        this.dialog.show();
	    }
		
		@Override
	    protected void onPostExecute(JSONObject result) {
	        
			if (result != null) {
				lesson = new Lesson();
	        	if (result.has("title")) {
	        		try {
						lesson.lessonName = result.getString("title");
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	        	}
	        	
	        	if (result.has("description")) {
	        		try {
						lesson.description = result.getString("description");
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	        	}
	        	
	        	if (result.has("author")) {
	        		try {
						lesson.author = result.getString("author");
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	        	}
	        	
	        	if (result.has("rating")) {
	        		try {
						lesson.rating = result.getInt("rating");
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	        	}
	        	
	        	if (result.has("block_set")) {
	        		try {
						JSONObject set = result.getJSONObject("block_set");
						String title = set.getString("title");
						long remoteId = set.getLong("id");
						//if (db.getBlockSetByRemoteId(remoteId));
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	        	}
	        	
	        	Boolean isAdvanced = false;
	        	if (result.has("advanced")) {
	        		try {
						isAdvanced = result.getBoolean("advanced");
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	        	}
	        	
	        	if (isAdvanced) {
	        		try {
						lesson.rating = result.getInt("rating");
						lesson.startSoundRemoteUrl = result.getString("start_sound_url");
						lesson.endSoundRemoteUrl = result.getString("end_sound_url");
						lesson.correctSoundRemoteUrl = result.getString("correct_sound_url");
						lesson.incorrectSoundRemoteUrl = result.getString("incorrect_sound_url");
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	        	}
	        	
	        	
	        	if (result.has("price")) {
	        		String price = null;
					try {
						price = result.getString("price");
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	        		if (price.isEmpty()) {
	            		lesson.price = 0.0f;
	            	} else {
	            		lesson.price = Float.valueOf(price);
	            	}
	                
	        	}
	        	
	        	if (result.has("tasks")) {
	        		JSONArray questions = null;
					try {
						questions = result.getJSONArray("tasks");
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	        		if (questions.length() > 0) {
	        			
	        			for (int i = 0; i < questions.length(); i++) {
	        				JSONObject currentQuestion = null;
	    					try {
	    						currentQuestion = questions.getJSONObject(i);
	    					} catch (JSONException e) {
	    						// TODO Auto-generated catch block
	    						e.printStackTrace();
	    					}
	            			String text = "";
	            			String answer = "";
	            			String remoteUrl = "";
	            			if (currentQuestion.has("question")) {
	            				try {
	    							text = currentQuestion.getString("question");
	    						} catch (JSONException e) {
	    							// TODO Auto-generated catch block
	    							e.printStackTrace();
	    						}
	            			}
	            			if (currentQuestion.has("answer")){
	            				try {
	    							answer = currentQuestion.getString("answer");
	    						} catch (JSONException e) {
	    							// TODO Auto-generated catch block
	    							e.printStackTrace();
	    						}
	            			}
	            			
	            			if (currentQuestion.has("audio")){
	            				try {
	    							remoteUrl = currentQuestion.getString("audio");
	    						} catch (JSONException e) {
	    							// TODO Auto-generated catch block
	    							e.printStackTrace();
	    						}
	            			}
	            			
	            			Question question = new Question(text, answer, 0, remoteUrl, "");
	            			lesson.questions.add(question);
	        			}
	        			
	        		}
	        	}
	        	
	        	if (dialog.isShowing()) {
		            dialog.dismiss();
		        }
	        	loadLessonDetails();
        	}
        	
		 }
		
		 protected JSONObject doInBackground(Long... args) {
	        	
	            Long id = args[0];
	            String url = "http://fuzzylogic.herokuapp.com/lessons/" + id + ".json";
	            Log.d(TAG, "URL: " + url);
	            HttpResponse response;
	            HttpClient httpclient = new DefaultHttpClient();
	            String responseString = "";

	            try {
	            	response = httpclient.execute(new HttpGet(url));
	            	if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
	            		ByteArrayOutputStream out = new ByteArrayOutputStream();
	            		response.getEntity().writeTo(out);
	            		out.close();
	            		responseString = out.toString();
	            		Log.d(TAG, "RESPONSE: " + responseString);
		            } else{
		                //Closes the connection.
		                response.getEntity().getContent().close();
		                Log.d(TAG, response.getStatusLine().getReasonPhrase());
		                throw new IOException(response.getStatusLine().getReasonPhrase());
		            }
		        } catch (ClientProtocolException e) {
		            //TODO Handle problems..
		        } catch (IOException e) {
		            //TODO Handle problems..
		        }
		        try {
	                JSONObject mLesson = new JSONObject(responseString);
	                return mLesson;
		        } catch (JSONException e) {
		                // TODO Auto-generated catch block
		                e.printStackTrace();
		        } 
		        return null;
	        }
	}
}
