package com.cubes.learningcubes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
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
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class CategoryDetailActivity extends Activity {

	private CubesDbHelper db;
	private SearchResultsListAdapter adapter;
	private ArrayList<Lesson> lessons;
	private final String TAG = "CategoryDetailActivity";
	private Random random;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//reuse layout from search results
		setContentView(R.layout.activity_search_results);
		// Show the Up button in the action bar.
		setupActionBar();
		db = CubesDbHelper.getInstance(this);
		random = new Random();
		lessons = new ArrayList<Lesson>();
		adapter = new SearchResultsListAdapter(this, lessons);
		ListView lv = (ListView)findViewById(R.id.search_results_list);
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int arg2,
					long arg3) {
				long id = (Long)v.getTag();
				Intent i = new Intent(CategoryDetailActivity.this, LessonDetailActivity.class);
				i.putExtra("remoteId", id);
				startActivity(i);
			}

		});
		lessons.clear();
		Intent intent = getIntent();
        String categoryName = intent.getStringExtra("categoryName");
        getActionBar().setTitle("Browse category: " + categoryName);
       
        int categoryId = intent.getIntExtra("remoteCategoryId", 0);
        if (categoryId != 0) {
        	CategorySearchTask task = new CategorySearchTask(this);
            task.execute(categoryId);
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
		getMenuInflater().inflate(R.menu.category_detail, menu);
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
	
	private class CategorySearchTask extends AsyncTask<Integer, Void, JSONObject> {
		private ProgressDialog dialog;
		private Activity activity;
		
		
		public CategorySearchTask(Activity activity) {
	        this.activity = activity;
	        dialog = new ProgressDialog(activity);
	    }
	    
		
		@Override
		protected void onPreExecute() {
	        this.dialog.setMessage("Loading...");
	        this.dialog.show();
	    }
		
		@Override
	    protected void onPostExecute(JSONObject categoryObject) {
	        
			if (categoryObject != null) {
				Log.d(TAG, categoryObject.toString());
			} else {
				Log.d(TAG, "category is null");
			}
			
			if (dialog.isShowing()) {
	            dialog.dismiss();
	        }
	        
			JSONArray itemsList;
			try {
				 itemsList = categoryObject.getJSONArray("lessons");
				 for (int i = 0; i < itemsList.length(); i++) {
		            	
		                try {
		                           
		                	Lesson lesson = new Lesson();
		                	JSONObject object = itemsList.getJSONObject(i);
		                	lesson.lessonName = object.getString("title");
		                	lesson.description = object.getString("description");
		                	lesson.remoteId = object.getLong("id");
		                	String price = object.getString("price");
		                	if (price.isEmpty()) {
		                		lesson.price = 0.0f;
		                	} else {
		                		lesson.price = Float.valueOf(price);
		                	}
		                	adapter.addItem(lesson);
		                	
		                    } catch (JSONException e) {
		                            // TODO Auto-generated catch block
		                            e.printStackTrace();
		                    }
		            }   
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
                   
		}
		
        
        protected JSONObject doInBackground(Integer... args) {
        	
            int categoryId = args[0];
            String url = "http://fuzzylogic.herokuapp.com/categories/" + categoryId + ".json";
            Log.d(TAG, "URL IS: " + url);
            HttpResponse response;
            HttpClient httpclient = new DefaultHttpClient();
            String responseString = "";
            

            try {
            	response = httpclient.execute(new HttpGet(url));
            	if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
            		ByteArrayOutputStream out = new ByteArrayOutputStream();
            		response.getEntity().writeTo(out);
            		out.close();
            		responseString = out.toString();
            		Log.d(TAG, responseString);
	            } else{
	                //Closes the connection.
	                response.getEntity().getContent().close();
	                Log.d(TAG, response.getStatusLine().getReasonPhrase());
	                throw new IOException(response.getStatusLine().getReasonPhrase());
	            }
	        } catch (ClientProtocolException e) {
	            //TODO Handle problems..
	        } catch (IOException e) {
	            //TODO Handle problems..
	        }
	        try {
                JSONObject mCategory = new JSONObject(responseString);
                return mCategory;
	        } catch (JSONException e) {
	                // TODO Auto-generated catch block
	                e.printStackTrace();
	        } 
	        return null;
        }
	}
	
	


}
