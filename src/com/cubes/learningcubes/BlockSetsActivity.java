package com.cubes.learningcubes;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.AdapterView.OnItemClickListener;
import android.support.v4.app.NavUtils;

public class BlockSetsActivity extends Activity {
	
	private String TAG = "Block sets activity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_block_sets);
		// Show the Up button in the action bar.
		setupActionBar();
		ListView lv = (ListView)findViewById(R.id.block_set_list);
		lv.setAdapter(new BlockSetListAdapter(this, Database.blockSets));
		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				
				BlockSet data = (BlockSet)parent.getItemAtPosition(position);
				Intent intent = new Intent(BlockSetsActivity.this, BlockSetDetailActivity.class);
				intent.putExtra("setId", data.id);
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
		getMenuInflater().inflate(R.menu.block_sets, menu);
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
	
	private class BlockSetListAdapter extends ArrayAdapter<BlockSet>{

        private final Context context;
        private final BlockSet[] values;

        public BlockSetListAdapter(Context context, BlockSet[] sets) {
                
        //call the super class constructor and provide the ID of the resource to use instead of the default list view item
          super(context, R.layout.blockset_list_item, sets);
          this.context = context;
          this.values = sets;
        }
        
        //this method is called once for each item in the list
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

          LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
          View listItem = inflater.inflate(R.layout.blockset_list_item, parent, false);
          listItem.setTag(values[position].id);
        
          //score circle
          TextView name = (TextView)listItem.findViewById(R.id.block_set_name);
          name.setText(values[position].name);
          
          ToggleButton tb = (ToggleButton)listItem.findViewById(R.id.blockset_toggle);
          tb.setChecked(values[position].enabled);

          tb.setTag(values[position].id);
          tb.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					int id = (Integer)v.getTag();
					setListItemEnabled(id);
				}
  
          });
                          
          TextView numberOfBlocks = (TextView)listItem.findViewById(R.id.number_of_blocks);
          numberOfBlocks.setText(values[position].size() + " blocks");

          return listItem;
        
        }
        
        public void setListItemEnabled(int id){
      	  for (BlockSet set : values) {
      		  if (set.id == id) {
      			  set.enabled = true;
      		  } else {
      			  set.enabled = false;
      		  }
      	  }
      	  this.notifyDataSetChanged();
        }
        
	}

}
