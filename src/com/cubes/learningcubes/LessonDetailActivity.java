package com.cubes.learningcubes;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class LessonDetailActivity extends Activity {

	private CubesDbHelper db;
	private Lesson lesson;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lesson);
		// Show the Up button in the action bar.
		setupActionBar();
		
		db = CubesDbHelper.getInstance(this);
		long id = getIntent().getExtras().getLong("lessonId");
		lesson = db.getLessonById(id);
		
		TextView lessonNameTv = (TextView)findViewById(R.id.lesson_name);
		lessonNameTv.setText(lesson.lessonName);
		
		TextView lessonDescriptionTv = (TextView)findViewById(R.id.lesson_description);
		lessonDescriptionTv.setText(lesson.description);
		
		TextView lessonNumberQuestionsTv = (TextView)findViewById(R.id.lesson_number_questions);
		lessonNumberQuestionsTv.setText(lesson.questions.size()+ " questions");
		
		getActionBar().setTitle(lesson.lessonName);
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
		}
		return super.onOptionsItemSelected(item);
	}

}
