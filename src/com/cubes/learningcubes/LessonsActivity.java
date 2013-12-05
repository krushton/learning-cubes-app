package com.cubes.learningcubes;

import java.util.ArrayList;

import android.app.ActionBar;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.ToggleButton;

public class LessonsActivity extends Activity {
	
	private LessonsListAdapter adapter;
	private final String TAG = "Lesson activity";
	private CubesDbHelper db;
	private ActionMode mActionMode; 
	private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

	    @Override
	    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
	        // Inflate a menu resource providing context menu items
	        MenuInflater inflater = mode.getMenuInflater();
	        inflater.inflate(R.menu.lesson_context_menu, menu);
	        return true;
	    }

	    @Override
	    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
	        return false; // Return false if nothing is done
	    }

	    // Called when the user selects a contextual menu item
	    @Override
	    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
	        switch (item.getItemId()) {
	            case R.id.action_delete:
	            	//todo: handle delete of item from 'database'
	                mode.finish(); // Action picked, so close the CAB
	                return true;
	            default:
	                return false;
	        }
	    }

	    // Called when the user exits the action mode
	    @Override
	    public void onDestroyActionMode(ActionMode mode) {
	        mActionMode = null;
	    }
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lessons);

		db = CubesDbHelper.getInstance(this);
		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
		// Show the Up button in the action bar.
		actionBar.setDisplayHomeAsUpEnabled(true);
		adapter = new LessonsListAdapter(this, convertListToArray(db.getLessons()));
		
        
		ListView lv = (ListView)findViewById(R.id.lessons_list);
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int arg2,
					long arg3) {
				long id = (Long)v.getTag();
				Intent i = new Intent(LessonsActivity.this, LessonDetailActivity.class);
				i.putExtra("lessonId", id);
				startActivity(i);
			}

      });
      
        lv.setOnItemLongClickListener(new OnItemLongClickListener() {


			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View v,
					int arg2, long arg3) {
				Log.d(TAG, "LONG ITEM CLICK");
				if (mActionMode != null) {
		            return false;
		        }				
		        mActionMode = LessonsActivity.this.startActionMode(mActionModeCallback);
		        v.setSelected(true);
				return true;
			}

		
	});
		

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.lessons, menu);
		
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
		searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
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
	
	private Lesson[] convertListToArray(ArrayList<Lesson> list) {
		Lesson[] values = new Lesson[list.size()];
		for (int i = 0; i < list.size(); i++) {
			values[i] = list.get(i);
		}
		return values;
	}
	
	
	private class LessonsListAdapter extends ArrayAdapter<Lesson>{

	    private final Context context;
	    private final Lesson[] values;

	    public LessonsListAdapter(Context context, Lesson[] lessons) {
	            
	    //call the super class constructor and provide the ID of the resource to use instead of the default list view item
	      super(context, R.layout.blockset_list_item, lessons);
	      this.context = context;
	      this.values = lessons;
	    }
	    
	    //this method is called once for each item in the list
	    @Override
	    public View getView(int position, View convertView, ViewGroup parent) {

	      LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	      View listItem = inflater.inflate(R.layout.lesson_list_item, parent, false);
	    
	      listItem.setTag(values[position].id);
	      TextView lessonName = (TextView)listItem.findViewById(R.id.lesson_name);
	      lessonName.setText(values[position].lessonName);
	      
	      TextView description = (TextView)listItem.findViewById(R.id.lesson_description);
	      description.setText(values[position].description);
	     
	      
          ToggleButton tb = (ToggleButton)listItem.findViewById(R.id.lesson_toggle);
          tb.setChecked(values[position].enabled);

          tb.setTag(values[position].id);
          tb.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					long id = (Long)v.getTag();
					setListItemEnabled(id);
				}
  
          });
          
	      return listItem;
	    
	    }
	    public void setListItemEnabled(long id){
	      	  for (Lesson lesson : values) {
	      		  if (lesson.id == id) {
	      			lesson.enabled = true;
	      			  db.changeEnabledLesson(lesson.id);
	      		  } else {
	      			lesson.enabled = false;
	      		  }
	      	  }
	      	  this.notifyDataSetChanged();
	        }
	}

}
