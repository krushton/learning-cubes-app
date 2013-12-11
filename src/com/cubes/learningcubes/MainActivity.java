package com.cubes.learningcubes;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends Activity {
	private String[] listItems = { "Session History", "Statistics", "Block Sets", "Lessons", "Quick Scan", "Start Learning Service" };

	private boolean serviceIsRunning = false;
	private final String SERVICE_KEY = "serviceIsRunning";
	private SharedPreferences prefs;
	private CheckServiceRunningTask task;
	private ToggleButton serviceToggleButton;
	private final String TAG = "MainActivity";
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
               
        prefs = getPreferences(Activity.MODE_PRIVATE);
        if (prefs.contains("serviceRunning")) {
        	serviceIsRunning = prefs.getBoolean(SERVICE_KEY, false);
        }
        
        ListView lv = (ListView)findViewById(R.id.list);
        lv.setAdapter(new MainListAdapter(this, listItems));
        lv.setOnItemClickListener(new OnItemClickListener() {

	        public void onItemClick(final AdapterView<?> parentView, View view, int index, long id) {
	            Intent intent;
	        	switch(index) {
		            case 0:
		            	intent = new Intent(MainActivity.this, SessionsActivity.class);
		            	startActivity(intent);
		            	break;
		            case 1:
		            	intent = new Intent(MainActivity.this, StatisticsActivity.class);
		            	startActivity(intent);
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
		            	//does nothing now that we have a service
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
    
    private class CheckServiceRunningTask extends TimerTask {

		@Override
		public void run() {
			if (prefs.contains("serviceRunning")) {
				boolean running = prefs.getBoolean("serviceRunning", false);
				serviceToggleButton.setActivated(running); 
				if (!running) {
					task.cancel();
				
				}
			}
			
		}
    	
    }
    
    private class MainListAdapter extends ArrayAdapter<String>{

	    private final Context context;
	    private final String[] values;

	    public MainListAdapter(Context context, String[] items) {
	            
	    //call the super class constructor and provide the ID of the resource to use instead of the default list view item
	      super(context, R.layout.blockset_list_item, items);
	      this.context = context;
	      this.values = items;
	    }
	    
	    //this method is called once for each item in the list
	    @Override
	    public View getView(int position, View convertView, ViewGroup parent) {

	      LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	      View listItem = inflater.inflate(R.layout.main_list_item, parent, false);
	    
	      TextView actionName = (TextView)listItem.findViewById(R.id.action_name);
	      actionName.setText(values[position]);
	      
	      ToggleButton tb = (ToggleButton)listItem.findViewById(R.id.service_toggle);
	      
	      if (String.valueOf(values[position]).contains("Learning Service")) {

	          tb.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						
						serviceToggleButton = (ToggleButton)v;
						Intent intent = new Intent(MainActivity.this, LearningService.class);
						final SharedPreferences.Editor editor = prefs.edit();
						
						if (serviceIsRunning) {
							Log.d(TAG, "STOPPING SERVICE");
							editor.putBoolean(SERVICE_KEY, false);
							serviceIsRunning = false;
							stopService(intent);
						} else {
							
							BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
							if (mBluetoothAdapter == null) {
							    Toast.makeText(MainActivity.this, "This device does not support Bluetooth,",
							    		Toast.LENGTH_LONG).show();
							} else {
							    if (!mBluetoothAdapter.isEnabled()) {
							    	Intent enableBluetooth = new Intent(
						                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
						            startActivityForResult(enableBluetooth, 1);
							    } else {

									editor.putBoolean(SERVICE_KEY, true);
									serviceIsRunning = true;
									Timer timer = new Timer();
									task = new CheckServiceRunningTask();
									timer.schedule(task, 1000, 1000);
									startService(intent);
							    }
							    
							}
							
						}
					}
	          });
	          
	          tb.setActivated(serviceIsRunning);
	          
	    	 
	      } else {
	    	  //hide toggle button
	    	  tb.setVisibility(View.GONE);
	      }
	     
	      return listItem;
	    
	    }
	   
    }
  
}


