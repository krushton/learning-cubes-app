package com.cubes.learningcubes;


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
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class BlockSetDetailActivity extends Activity {

	private long setId;
	private String setName;
	private final String TAG = "Block set detail";
	private CubesDbHelper db;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_block_set_detail);
		// Show the Up button in the action bar.
		
		setupActionBar();
		setId = getIntent().getExtras().getLong("setId");
		db = CubesDbHelper.getInstance(this);
		
		BlockSet blockSet = db.getBlockSetById(setId);
		
		final ListView lv = (ListView)findViewById(R.id.blocks_in_set_list);
		final Block[] values = blockSet.asArray();
		lv.setAdapter(new BlockListAdapter(this, values));
		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent intent = new Intent(BlockSetDetailActivity.this, ScanActivity.class);
				
				long blockId = (Long)view.getTag();
				Log.d(TAG, "BLOCK ID: " + blockId);
				Block block = values[position];
				
				Log.d(TAG, "BLOCK IS NULL: " + String.valueOf(block == null));
				
				intent.putExtra("blockId", block.id);
				intent.putExtra("setId", block.blockSetId);
				intent.putExtra("value", block.text);
				intent.putExtra("setName","");
				intent.putExtra("mode", "mapping");

				startActivity(intent);
			}
			
		});
		
		
		TextView blockSetNameText = (TextView)findViewById(R.id.block_set_name_detail);
		blockSetNameText.setText(blockSet.name);
		setName = blockSet.name;
		
		TextView numBlocksText = (TextView)findViewById(R.id.number_of_blocks_detail);
		numBlocksText.setText(blockSet.size() + " blocks"); 
	}

	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setTitle("");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.block_set_detail, menu);
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
	

	private class BlockListAdapter extends ArrayAdapter<Block>{

	          private final Context context;
	          private final Block[] values;

	          public BlockListAdapter(Context context, Block[] set) {
	                  
	          //call the super class constructor and provide the ID of the resource to use instead of the default list view item
	            super(context, R.layout.skinny_block_list_item, set);
	            this.context = context;
	            this.values = set;
	          }
	          
	          //this method is called once for each item in the list
	          @Override
	          public View getView(int position, View convertView, ViewGroup parent) {

	            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	            View listItem = inflater.inflate(R.layout.skinny_block_list_item, parent, false);
	          
	            TextView text = (TextView)listItem.findViewById(R.id.block_text);
	            text.setText(values[position].text);
	            
	            TextView idText = (TextView)listItem.findViewById(R.id.block_id);
	            String id = values[position].tagId;
	            if (id == null || id.equals("")) {
	            	idText.setText(getResources().getString(R.string.unmapped));
	            } else {
	            	idText.setText(values[position].tagId);
	            }
		        
	            listItem.setTag(values[position].id);
	           
	            return listItem;
	          
	          }
	          
	}
}
