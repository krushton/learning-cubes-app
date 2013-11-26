package com.cubes.learningcubes;

import java.util.Date;
import java.util.HashMap;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup.LayoutParams;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.support.v4.app.NavUtils;

public class SessionDetailActivity extends Activity {
	
	private TableLayout tableLayout;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_session_detail);
		// Show the Up button in the action bar.
		setupActionBar();
		tableLayout = (TableLayout)findViewById(R.id.session_table);
		int lessonId = getIntent().getExtras().getInt("lessonId");
		HashMap<String, Integer> questions = Database.sessions[0].log;

		for (String key : questions.keySet()) {
			
			TableRow tr = new TableRow(this);
			TextView q = new TextView(this);
			q.setText(key);
			tr.addView(q);
			
			TextView c = new TextView(this);
			if (questions.get(key) == 1){
				c.setTextColor(getResources().getColor(R.color.green));
				c.setText("Correct");
			} else {
				c.setTextColor(getResources().getColor(R.color.red));
				c.setText("Incorrect");
			}
			tr.addView(c);
			tableLayout.addView(tr);
			
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
		getMenuInflater().inflate(R.menu.session_detail, menu);
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
