package com.cubes.learningcubes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;


public class StatisticsActivity extends FragmentActivity {
	
	
	private final String TAG = "StatisticsActivity";
	private final static String USER_AGENT_STRING = "CubesApp";
	private static ProgressDialog dialog;
	static final int NUM_ITEMS = 2;
	MyAdapter mAdapter;
    ViewPager mPager;

	private static CubesDbHelper db;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().requestFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		// Show the Up button in the action bar.
		setupActionBar();
		setContentView(R.layout.activity_statistics);
		mAdapter = new MyAdapter(getSupportFragmentManager());
		db = CubesDbHelper.getInstance(this);
        mPager = (ViewPager)findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);
        mPager.setCurrentItem(0);
	  
	}
	
	public ProgressDialog getDialog() {
		return dialog;
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
		getMenuInflater().inflate(R.menu.statistics, menu);
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
	
	
		
	 public static class MyAdapter extends FragmentPagerAdapter {
	        public MyAdapter(FragmentManager fm) {
	            super(fm);
	        }

		        @Override
		        public int getCount() {
		            return NUM_ITEMS;
		        }

		        @Override
		        public Fragment getItem(int position) {
		        	String url = "";
		        	switch(position) {
		        		case 0:
		        			url = "file:///android_asset/page-one.html";
		        			break;
		        		case 1:
		        		default:
		        			url = "file:///android_asset/page-two.html";
		        			break;
		        	}
		            return ScreenSlidePageFragment.newInstance(url);
		        }
		    }

		
		public static class ScreenSlidePageFragment extends Fragment {
			
			private ProgressDialog dialog;

			String mUrl;
			static ScreenSlidePageFragment newInstance(String url) {
				ScreenSlidePageFragment f = new ScreenSlidePageFragment();

	            Bundle args = new Bundle();
	            args.putString("url", url);
	            f.setArguments(args);
	            
	            return f;
	        }
			@Override
	        public void onCreate(Bundle savedInstanceState) {
	            super.onCreate(savedInstanceState);
	            mUrl = getArguments() != null ? getArguments().getString("url") : "";
	        }
			 
		    @SuppressLint("NewApi")
			@Override
		    public View onCreateView(LayoutInflater inflater, ViewGroup container,
		            Bundle savedInstanceState) {
		        ViewGroup rootView = (ViewGroup) inflater.inflate(
		                R.layout.web_view_fragment, container, false);
		        WebView webView = (WebView)rootView.findViewById(R.id.web_view);
		        webView.setWebViewClient(new WebViewClient() {
			    	public void onPageFinished(WebView view, String url) {                  
			            if (dialog.isShowing()) {
			                dialog.dismiss();
			            }
			        }
			    });
			    webView.addJavascriptInterface(new WebAppInterface(getActivity(), db) , "Android");
			    dialog = new ProgressDialog(this.getActivity());
			    dialog.setMessage("Crunching some numbers...");
			    dialog.setCanceledOnTouchOutside(false);
			    dialog.show();
			    
			    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			    	webView.setWebContentsDebuggingEnabled(true);
			    }
			    
			    webView.getSettings().setJavaScriptEnabled(true);
			    webView.clearCache(true);
			    webView.getSettings().setUserAgentString(USER_AGENT_STRING);
			    webView.loadUrl(mUrl);
		        return rootView;
		    }
		}

}
