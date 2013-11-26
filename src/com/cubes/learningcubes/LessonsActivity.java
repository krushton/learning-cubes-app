package com.cubes.learningcubes;

import android.app.ActionBar;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;

public class LessonsActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lessons);

		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
		// Show the Up button in the action bar.
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		for (Lesson l : Database.lessons) {
			addAccordionListItem(l);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.lessons, menu);
		
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
		case R.id.action_browse:
			Intent i = new Intent(this, BrowseActivity.class);
			startActivity(i);
		}
		return super.onOptionsItemSelected(item);
	}

	public void addAccordionListItem(Lesson lesson) {
		
		RelativeLayout listViewLayout = (RelativeLayout)findViewById(R.id.lessonList);
		
		LayoutInflater inflater = getLayoutInflater();
		RelativeLayout titleBar = (RelativeLayout)inflater.inflate(R.id.lesson_title_bar, listViewLayout);
		
		TextView title = (TextView)titleBar.findViewById(R.id.lesson_title_bar_name);
		title.setText(lesson.lessonName);
		
		listViewLayout.addView(titleBar);
		
		RelativeLayout details = (RelativeLayout)inflater.inflate(R.id.lesson_details, listViewLayout);
		TextView name = (TextView)details.findViewById(R.id.lesson_details_name);
		name.setText(lesson.lessonName);
		
		TextView numQuestions = (TextView)details.findViewById(R.id.lesson_details_number_questions);
		name.setText(lesson.questions.size() + " questions");
	}
	


}
