package com.cubes.learningcubes;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.content.SharedPreferences;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.NfcA;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ScanActivity extends Activity {
	
	private NfcAdapter mAdapter;
	private TextView scanResultIdTextView;
	private TextView scanResultValueTextView;
	private final String TAG = "ScanActivity";
	private String setName = "";
	private long setId = 0;
	private long blockId = 0;
	private String mode = "scan";
	private EditText tagIDEditText;
	private CubesDbHelper db;
	PendingIntent pendingIntent;
	private String[][] techListsArray;
	private IntentFilter[] intentFiltersArray;
	private final String MODE_MAPPING = "mapping";
	private final String MODE_SCAN = "scan";
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scan);
		// Show the Up button in the action bar.
		setupActionBar();
		Log.d(TAG, "ON CREATE CALLED");
		db = CubesDbHelper.getInstance(this);
		
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			if (extras.containsKey("mode") && extras.getString("mode").equals(MODE_MAPPING)) {
				mode = MODE_MAPPING;
			}
		} 
		RelativeLayout scanLayout = (RelativeLayout)findViewById(R.id.nfc_scan_layout);
		RelativeLayout editLayout = (RelativeLayout)findViewById(R.id.nfc_edit_layout);
		
		if (mode.equals(MODE_MAPPING)) {
			setId = extras.getLong("setId", 0);
			blockId = extras.getLong("blockId",0);
			String blockValue = extras.getString("value");
			TextView tv = (TextView)findViewById(R.id.message_map_target);
			tv.setText(blockValue);
			setName = extras.getString("setName", "");
			editLayout.setVisibility(View.VISIBLE);
			scanLayout.setVisibility(View.GONE);
			getActionBar().setTitle("Edit Block Set");
		} else {
			scanResultIdTextView = (TextView)findViewById(R.id.result_id);
			scanResultValueTextView = (TextView)findViewById(R.id.result_value);
			scanLayout.setVisibility(View.VISIBLE);
			editLayout.setVisibility(View.GONE);
        	getActionBar().setTitle("Quick Scan");
		}
		
		mAdapter = NfcAdapter.getDefaultAdapter(this);		
        if (mAdapter == null) {
            Toast.makeText(this, "This device doesn't support NFC.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        if (!mAdapter.isEnabled()) {
        	Toast.makeText(this, "NFC is disabled.", Toast.LENGTH_SHORT).show();
        } 
        setupNFC();

	}
	
	private void setupNFC() {
		IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
		IntentFilter tag = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
	    try {
	        ndef.addDataType("*/*");   
	    }
	    catch (MalformedMimeTypeException e) {
	        throw new RuntimeException("fail", e);
	    }
	    intentFiltersArray = new IntentFilter[] {ndef, tag};
	    techListsArray = new String[][] { new String[] { MifareClassic.class.getName(), NfcA.class.getName() } };
		pendingIntent = PendingIntent.getActivity(
			    this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		
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
		getMenuInflater().inflate(R.menu.scan, menu);
		return true;
	}
	
	@Override
	public void onPause() {
	    super.onPause();
	    mAdapter.disableForegroundDispatch(this);
	}   

	@Override
	public void onResume() {
	    super.onResume();
	    mAdapter.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, techListsArray);
	}

	@Override
	public void onNewIntent(Intent intent) {
	    Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        String payload = getTagData(tag).replaceAll(" ", "");
        
        if (mode.equals(MODE_SCAN)) {
        	Block block = db.getBlockByTagValue(payload);
        	Log.d(TAG, "BLOCK IS NULL? " + String.valueOf(block == null));
        	if (block != null) {
        		scanResultValueTextView.setText(block.text);
        	} else {
        		scanResultValueTextView.setText(getResources().getString(R.string.unmapped));
        	}
        	scanResultIdTextView.setText(payload);
        	
        	
        } else {	
    		tagIDEditText = (EditText)findViewById(R.id.block_id);
    		tagIDEditText.setEnabled(false);
    		tagIDEditText.setText(payload);
        }
        
	    Log.d(TAG, "TAG PAYLOAD: " + payload);
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

	public void saveMapping(View v) {
		String boxContents = tagIDEditText.getEditableText().toString();
		Log.d(TAG, boxContents);
		if (!(boxContents == null) && !boxContents.isEmpty()) {
			db.mapBlock(blockId, boxContents);
			Toast.makeText(this, "Value saved succesfully.", Toast.LENGTH_SHORT).show();
			Intent intent = new Intent(ScanActivity.this, BlockSetDetailActivity.class);
			intent.putExtra("setId", setId);
			startActivity(intent);
		} 
	}
	
	private String getTagData(Parcelable p) {
        Tag tag = (Tag) p;
        byte[] id = tag.getId();
        return getHex(id);
    }

    private String getHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = bytes.length - 1; i >= 0; --i) {
            int b = bytes[i] & 0xff;
            if (b < 0x10)
                sb.append('0');
            sb.append(Integer.toHexString(b));
            if (i > 0) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }

}


