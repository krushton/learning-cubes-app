package com.cubes.learningcubes;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.cubes.learningcubes.DatabaseContract.LessonEntry;
import com.cubes.learningcubes.DatabaseContract.QuestionEntry;
import com.cubes.learningcubes.DatabaseContract.SessionEntry;
import com.cubes.learningcubes.DatabaseContract.SessionLogEntry;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TableRow;
import android.widget.TextView;

public class SessionDetailActivity extends Activity {
	
	private CubesDbHelper db;
	private Session session;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_session_detail);
		// Show the Up button in the action bar.
		setupActionBar();
		db = CubesDbHelper.getInstance(this);
		
		long sessionId = getIntent().getLongExtra("sessionId", 0);
		session = db.getSessionById(sessionId);
		
		TextView sessionDateTextView = (TextView)findViewById(R.id.session_date);
		sessionDateTextView.setText(session.getDate());
		
		TextView sessionLengthTextView = (TextView)findViewById(R.id.session_summary);
		sessionLengthTextView.setText(session.getSummary());
		
		TextView lessonNameTextView = (TextView)findViewById(R.id.session_lesson_name);
		lessonNameTextView.setText(session.lessonName);
		
		ListView lv = (ListView)findViewById(R.id.session_log_list);
		LogItem[] values = convertListToArray(session.sessionLog);
		lv.setAdapter(new LogListAdapter(this, values));
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

	private LogItem[] convertListToArray(ArrayList<LogItem> list) {
		LogItem[] values = new LogItem[list.size()];
		for (int i = 0; i < list.size(); i++) {
			values[i] = list.get(i);
		}
		return values;
	}
	
	public void removeSession(View v) {
		
		new AlertDialog.Builder(SessionDetailActivity.this)
        .setIcon(android.R.drawable.ic_dialog_alert)
        .setTitle(R.string.warning)
        .setMessage(R.string.delete_session_message)
        .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

            	// first have to delete related logs
        		for (LogItem log : session.sessionLog) {
        			db.delete(SessionLogEntry.TABLE_NAME, log.id);
        		}
     
        		//ok now can delete the session
        		db.delete(SessionEntry.TABLE_NAME, session.id);
            
            	Intent intent = new Intent(SessionDetailActivity.this, SessionsActivity.class);
            	startActivity(intent);
            	
            }

        })
        .setNegativeButton(R.string.cancel, null)
        .show();

	}

	private class LogListAdapter extends ArrayAdapter<LogItem>{

          private final Context context;
          private final LogItem[] values;

          public LogListAdapter(Context context, LogItem[] set) {
                  
          //call the super class constructor and provide the ID of the resource to use instead of the default list view item
            super(context, R.layout.skinny_log_list_item, set);
            this.context = context;
            this.values = set;
          }
          
          //this method is called once for each item in the list
          @Override
          public View getView(int position, View convertView, ViewGroup parent) {

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View listItem = inflater.inflate(R.layout.skinny_log_list_item, parent, false);
          
            TextView questionText = (TextView)listItem.findViewById(R.id.log_question_name);
            questionText.setText(values[position].questionText);
            
            ImageView icon = (ImageView)listItem.findViewById(R.id.log_success_icon);
            if (!values[position].questionResult) {
            	icon.setImageDrawable(getResources().getDrawable(R.drawable.ic_x));
            }
            
            listItem.setClickable(false);
            return listItem;
          
          }
	          
	}

}
