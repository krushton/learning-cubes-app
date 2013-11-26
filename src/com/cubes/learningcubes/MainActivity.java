package com.cubes.learningcubes;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MainActivity extends ListActivity {
	private String[] listItems = { "Session History", "Statistics", "Block Sets", "Lessons", "Quick Scan", "Start Learning Service" };
	// use this to start and trigger a service
	
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        SharedPreferences prefs = this.getPreferences(Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt("setId", -1);
		editor.commit();
		
		
        setListAdapter(new ArrayAdapter(this, android.R.layout.simple_list_item_1, listItems));
        final ListView v = getListView();
        v.setOnItemClickListener(new OnItemClickListener() {

	        public void onItemClick(final AdapterView<?> parentView, View view, int index, long id) {
	            Intent intent;
	        	switch(index) {
		            case 0:
		            	intent = new Intent(MainActivity.this, SessionsActivity.class);
		            	startActivity(intent);
		            	break;
		            case 1:
		            	//statistics
		            	break;
		            case 2: 
		            	intent = new Intent(MainActivity.this, BlockSetsActivity.class);
		            	startActivity(intent);
		            	break;
		            case 3:
		            	intent = new Intent(MainActivity.this, LessonsActivity.class);
		            	startActivity(intent);
		            	break;
		            case 5:
		            	//start service test
		            	intent = new Intent(MainActivity.this, GameActivity.class);
		            	startActivity(intent);
		            	break;
		            case 4:
		            default:
		            	intent = new Intent(MainActivity.this, ScanActivity.class);
		            	intent.putExtra("mode", "scan");
		            	startActivity(intent);
		            	break;
	            }	
	            
	        }   
        });
        
        getActionBar().setDisplayShowTitleEnabled(false);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
  
}


