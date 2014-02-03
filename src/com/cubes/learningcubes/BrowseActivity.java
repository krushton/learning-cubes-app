package com.cubes.learningcubes;

import android.os.Bundle;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.support.v4.app.NavUtils;

public class BrowseActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_browse);
		// Show the Up button in the action bar.
		setupActionBar();
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
		getMenuInflater().inflate(R.menu.browse, menu);
		
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
	
	public void handleClick(View v) {
		String q = "";
		Intent intent = new Intent(this, CategoryDetailActivity.class);
		switch(v.getId()) {
			case R.id.spelling:
				q += "Spelling";
				intent.putExtra("remoteCategoryId", 1);
				intent.putExtra("categoryName", q);
				startActivity(intent);
				break;
			case R.id.history:
				q += "History";
				break;
			case R.id.math:
				q += "Math";
				intent.putExtra("remoteCategoryId", 2);
				intent.putExtra("categoryName", q);
				startActivity(intent);
				break;
			case R.id.government:
				q += "Civics";
				break;
			case R.id.art:
				q += "Art";
				break;
			case R.id.science:
				q += "Science";
				intent.putExtra("remoteCategoryId", 4);
				intent.putExtra("categoryName", q);
				startActivity(intent);
				break;
			case R.id.music:
				q += "Music";
				break;
			case R.id.geography:
				q += "World";
				break;
			case R.id.reading:
				q += "Reading";
				break;
		}
	}
 

}
