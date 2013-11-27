package com.cubes.learningcubes;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.NavUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ScanActivity extends Activity {
	
	private NfcAdapter mNfcAdapter;
	private TextView scanResultTextView;
	private final String TAG = "ScanActivity";
	private String setName = "";
	private int setId = -1;
	private SharedPreferences prefs;
	private EditText newValueTextbox;
	private String mode;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scan);
		// Show the Up button in the action bar.
		setupActionBar();
		
		prefs = this.getPreferences(Context.MODE_PRIVATE);
		scanResultTextView = (TextView)findViewById(R.id.result);
		
		RelativeLayout scanLayout = (RelativeLayout)findViewById(R.id.nfc_scan_layout);
		RelativeLayout editLayout = (RelativeLayout)findViewById(R.id.nfc_edit_layout);
		
		SharedPreferences.Editor editor = prefs.edit();
		
		//hack. this activity gets loaded twice for some reason, need to store the id
		if (getIntent().getExtras() != null) {
			mode = getIntent().getExtras().getString("mode");
			Log.d(TAG, " TEST NOISE: " + mode);
			editor.putString("mode", mode);
			editor.commit();
		} else {
			if (prefs.contains("mode")) {
				mode = prefs.getString("mode", "");
			} else {
				mode = "scan";
			}
			
		}
	
		
		if (mode.equals("details")) {
			setId = getIntent().getExtras().getInt("setId", -1);
			if (setId == -1) {
				setId = prefs.getInt("setId", -1);
			}
			getActionBar().setTitle("Edit Block Set");
			
			//todo: make this actually get the set by id
			setName = Database.blockSets[0].name;

			
			editor.putInt("setId", setId);
			editor.putString("mode", "details");
			editor.commit();
			
			editLayout.setVisibility(View.VISIBLE);
			scanLayout.setVisibility(View.GONE);
		} else {
			scanLayout.setVisibility(View.VISIBLE);
			editLayout.setVisibility(View.GONE);
			editor.putString("mode", "scan");
        	getActionBar().setTitle("Quick Scan");
		}
		
		
		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
		
        if (mNfcAdapter == null) {
            Toast.makeText(this, "This device doesn't support NFC.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        if (!mNfcAdapter.isEnabled()) {
        	scanResultTextView.setText("NFC is disabled.");
        } 
        handleIntent(getIntent());
    }
	
	private void handleIntent(Intent intent) {
		
	    String action = intent.getAction();
	    if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage[] msgs;
            String payload ="";
            if (rawMsgs != null) {
                msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                }
                
                for (NdefMessage msg : msgs) {
                	payload += " " + msg.toString();
                }
            } else {
                // Unknown tag type
                Parcelable tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                payload = getTagData(tag);
            }
            
            if (mode.equals("scan")) {
            	scanResultTextView.setText(payload);
            } else {
            	TextView currentValueLabel = (TextView)findViewById(R.id.message_current_value);
            	currentValueLabel.setText("Current value in set \"" + setName + "\"");
        		
        		EditText currentValue = (EditText)findViewById(R.id.current_value);

            	//todo: actually find value of tag based on current block set and input id
        		Log.d(TAG, payload);
            	String value = "A";
        		currentValue.setText(value);
        		
        		newValueTextbox = (EditText)findViewById(R.id.remap_value);
        		final Button saveButton = (Button)findViewById(R.id.save_button);
        		newValueTextbox.addTextChangedListener(new TextWatcher() {

					@Override
					public void afterTextChanged(Editable edit) {
						if (edit.toString().isEmpty()) {
							saveButton.setEnabled(false);
						} else {
							saveButton.setEnabled(true);
						}
						
					}

					@Override
					public void beforeTextChanged(CharSequence s, int start, int count, int after) {
						// TODO Auto-generated method stub	
					}

					@Override
					public void onTextChanged(CharSequence s, int start, int before, int count) {
						// TODO Auto-generated method stub
						
					}
        			
        		});
        		SharedPreferences.Editor editor = prefs.edit();
    			editor.remove("mode");
    			editor.remove("setId");
    			editor.commit();
        		
            }
            
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
		getMenuInflater().inflate(R.menu.scan, menu);
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

	public void saveMapping(View v) {
		if (!newValueTextbox.getEditableText().toString().equals("")) {
			String newVal = newValueTextbox.getText().toString();
			//todo: remap
			Toast.makeText(this, "Value saved succesfully.", Toast.LENGTH_SHORT).show();
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


