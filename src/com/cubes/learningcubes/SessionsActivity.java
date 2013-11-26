package com.cubes.learningcubes;

import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class SessionsActivity extends Activity {
	
	private final String TAG = "Sessions Activity";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sessions);
		// Show the Up button in the action bar.
		setupActionBar();
		ListView lv = (ListView)findViewById(R.id.sessions_list);
		lv.setAdapter(new SessionsListAdapter(this, Database.sessions));
		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Log.d(TAG, "CLICK");
				
				Session data = (Session)parent.getItemAtPosition(position);
				Intent intent = new Intent(SessionsActivity.this, SessionDetailActivity.class);
				intent.putExtra("sessionId", data.id);
				startActivity(intent);

			}
				
		});
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
		getMenuInflater().inflate(R.menu.sessions, menu);
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
	
	private class SessionsListAdapter extends ArrayAdapter<Session>{

        private final Context context;
        private final Session[] values;

        public SessionsListAdapter(Context context, Session[] values) {
                
        //call the super class constructor and provide the ID of the resource to use instead of the default list view item
          super(context, R.layout.session_list_item, values);
          this.context = context;
          this.values = values;
        }
        
        //this method is called once for each item in the list
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

          LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
          View listItem = inflater.inflate(R.layout.session_list_item, parent, false);
          
          //date of session
          TextView dateText = (TextView) listItem.findViewById(R.id.session_date);
          dateText.setText(values[position].getDate());
          
          //name of session
          TextView lessonText = (TextView) listItem.findViewById(R.id.lesson_name);
          lessonText.setText(values[position].lessonName);
          
          //how long and how many questions answered
          TextView timeText = (TextView) listItem.findViewById(R.id.session_time);
          timeText.setText(values[position].getSummary());
          
          //score circle
          View circle = listItem.findViewById(R.id.score_circle);
          int drawableId = values[position].getScoreDrawable();
          circle.setBackgroundResource(drawableId);
          
          TextView scoreNumber = (TextView)listItem.findViewById(R.id.score_number);
          scoreNumber.setText(values[position].getScore()+"");

          listItem.setTag(values[position].id);
          return listItem;
        
        }
        
	}

}
