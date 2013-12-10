package com.cubes.learningcubes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
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
import android.widget.SearchView;
import android.widget.TextView;

public class SearchResultsActivity extends Activity {

	private SearchResultsListAdapter adapter;
	private final String TAG = "Search results activity";
	private Random random;
	private ArrayList<Lesson> lessons;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search_results);
		// Show the Up button in the action bar.
		setupActionBar();
		lessons = new ArrayList<Lesson>();
		ListView lv = (ListView)findViewById(R.id.search_results_list);
		
		adapter = new SearchResultsListAdapter(this, lessons);
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int arg2,
					long arg3) {
				long id = (Long)v.getTag();
				Intent i = new Intent(SearchResultsActivity.this, LessonDetailActivity.class);
				i.putExtra("remoteId", id);
				startActivity(i);
			}

		});
		random = new Random();
		handleIntent(getIntent());
		
	}

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {

    	lessons.clear();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            SearchResultsTask task = new SearchResultsTask(this);
            task.execute(query);
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
		getMenuInflater().inflate(R.menu.search_results, menu);
		
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
	

	private class SearchResultsTask extends AsyncTask<String, Void, JSONArray> {
		private ProgressDialog dialog;
		private Activity activity;
		
		
		public SearchResultsTask(Activity activity) {
	        this.activity = activity;
	        dialog = new ProgressDialog(activity);
	    }
	    
		
		@Override
		protected void onPreExecute() {
	        this.dialog.setMessage("Loading...");
	        this.dialog.show();
	    }
		
		@Override
	    protected void onPostExecute(JSONArray itemsList) {
	        
			Log.d(TAG, itemsList.toString());
			if (dialog.isShowing()) {
	            dialog.dismiss();
	        }
	        
            for (int i = 0; i < itemsList.length(); i++) {
            	
                try {
                           
                	Lesson lesson = new Lesson();
                	JSONObject object = itemsList.getJSONObject(i);
                	lesson.lessonName = object.getString("title");
                	lesson.description = object.getString("description");
                	String url =  object.getString("url");
                	String[] urlPieces = url.split(Pattern.quote("/"));
                	//hack, we're not sending the ID currently so just stripping it out of the url
                	Long id = Long.valueOf(urlPieces[urlPieces.length-1].replace(".json", ""));
                	lesson.remoteId = id;
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
		}
		
        
        protected JSONArray doInBackground(String... args) {
        	
            String query = args[0];
            String url = "http://lessonbuilder.herokuapp.com/lessons.json?q=" + query;
                
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
                JSONArray mLessons = new JSONArray(responseString);
                return mLessons;
	        } catch (JSONException e) {
	                // TODO Auto-generated catch block
	                e.printStackTrace();
	        } 
	        return null;
        }
	}
	
	private class SearchResultsListAdapter extends BaseAdapter {

	    private final Context context;
	    private final ArrayList<Lesson> values;
	    private LayoutInflater inflater;

	    public SearchResultsListAdapter(Context context, ArrayList<Lesson> values) {
	            
	    //call the super class constructor and provide the ID of the resource to use instead of the default list view item
	      this.context = context;
	      this.values = values;
	      inflater = LayoutInflater.from(context); 
	    }
	    
	    @Override
	    public int getCount() {
	        return values.size();
	    }

	    @Override
	    public long getItemId (int position) {
	        return position;
	    }

	    @Override
	    public Object getItem (int position) {
	        return values.get(position);
	    }
	    
	    public void addItem(Lesson item) {
	    	values.add(item);
	    	this.notifyDataSetChanged();
	    }
	    //this method is called once for each item in the list
	    @Override
	    public View getView(int position, View convertView, ViewGroup parent) {
	    	
	      View listItem = inflater.inflate(R.layout.lesson_web_list_item, parent, false);
	      Lesson lesson = values.get(position);
	      
	      TextView lessonName = (TextView)listItem.findViewById(R.id.web_lesson_name);
	      lessonName.setText(lesson.lessonName);
	      
	      TextView lessonDescription = (TextView)listItem.findViewById(R.id.lesson_description);
	      lessonDescription.setText(values.get(position).description);

	      TextView lessonPrice = (TextView)listItem.findViewById(R.id.lesson_price);
	      lessonPrice.setText(values.get(position).getPrice());
	      
	      listItem.setTag(lesson.remoteId);
	      
	      int starValue = random.nextInt(5);
	      int resId = 0;
	      switch(starValue) {
	      	case 1:
	      		resId = R.drawable.one_star;
	      		break;
	      	case 2:
	      		resId = R.drawable.two_stars;
	      		break;
	      	case 3:
	      		resId = R.drawable.three_stars;
	      		break;
	      	case 4: 
	        default:
	      		resId = R.drawable.four_stars;
	      		break;
	      }
	      
	      ImageView image = (ImageView)listItem.findViewById(R.id.star_rating);
	      image.setImageDrawable(getResources().getDrawable(resId));
	      
	      
	      int numDownloads = random.nextInt(900);
	      TextView numberDownloads = (TextView)listItem.findViewById(R.id.number_downloads);
	      numberDownloads.setText(numDownloads + " downloads");

	      return listItem;
	    
	    }
	
	}
 }


