package edu.swarthmore.cs.cs71.eventsmockup;

import java.util.ArrayList;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import edu.swarthmore.cs.cs71.eventsmockup.MockUp.ViewHolder;

public class RecommendationSettings extends Activity {
	
	private KeywordAdapter keywordAdapter;
	private KeywordAdapter hidewordAdapter;

	private ListView lv_keywords;
	private ListView lv_hidewords;
	
	ArrayList<String> added_prefs;
	ArrayList<String> blocked_prefs;
	
	EditText add_pref_text;
	EditText add_blocked_text;
	
	Button add_pref_button;	
	Button block_button;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_recommendation_settings);
		
		// unchecked casts since we can guarantee passing ArrayLists
		added_prefs = (ArrayList<String>) getIntent().getSerializableExtra("added prefs");
		blocked_prefs = (ArrayList<String>) getIntent().getSerializableExtra("blocked prefs");
		
		lv_keywords = (ListView) findViewById (R.id.list_keywords);
		lv_hidewords = (ListView) findViewById (R.id.list_hidewords);
		
		keywordAdapter = new KeywordAdapter(added_prefs);
		hidewordAdapter = new KeywordAdapter(blocked_prefs);

		lv_keywords.setAdapter(keywordAdapter);
		lv_hidewords.setAdapter(hidewordAdapter);

		add_pref_text = (EditText) this.findViewById(R.id.add_pref_text);
		add_pref_button = (Button) this.findViewById(R.id.add_pref_button);
		
		block_button = (Button) this.findViewById(R.id.block_button);
		add_blocked_text = (EditText) this.findViewById(R.id.add_blocked_text);

	}

	@Override
	public void onBackPressed() {
		Intent returnIntent = new Intent();
		returnIntent.putExtra("added prefs",added_prefs);
		returnIntent.putExtra("blocked prefs", blocked_prefs);
		setResult(RESULT_OK,returnIntent);     
		finish();
	}

	public void addPref(View v){
		String key = add_pref_text.getText().toString().toLowerCase();
		if(! added_prefs.contains(key)){
			added_prefs.add(key);
		}
		add_pref_text.setText("");
		add_pref_text.setHint(R.string.add_pref);
		keywordAdapter.notifyDataSetChanged();
	}

	public void addBlocked(View v){
		String key = add_blocked_text.getText().toString().toLowerCase();
		if(! blocked_prefs.contains(key)){
			blocked_prefs.add(key);
		}
		add_blocked_text.setText("");
		add_blocked_text.setHint(R.string.add_blocked);
		hidewordAdapter.notifyDataSetChanged();
		
	}
	
	public void remove(View v){
		int tag = (Integer) v.getTag();
		if (v.getParent().getParent() == lv_keywords){
			if (! added_prefs.isEmpty()){
				added_prefs.remove(tag);
			}
			keywordAdapter.notifyDataSetChanged();
		} else {
			if (! blocked_prefs.isEmpty()){
				blocked_prefs.remove(tag);
			}
			hidewordAdapter.notifyDataSetChanged();
		}
	}

	public class KeywordAdapter extends BaseAdapter {
        private ArrayList<String> list_keywords;

        public KeywordAdapter(ArrayList<String> list_all) {
            list_keywords = list_all;
        }

        public int getCount() {
            return list_keywords.size();
        }

        public String getItem(int position) {
            return list_keywords.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            if (convertView == null) {
	           	 LayoutInflater mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	           	 convertView = mInflater.inflate(R.layout.keyword,null);
            }

            TextView keyword = (TextView) convertView.findViewById(R.id.keyword);
            keyword.setText((CharSequence) (list_keywords.get(position)));
            keyword.setTag(position);
            Button removeButton = (Button) convertView.findViewById(R.id.delete_pref_button);
            removeButton.setTag(position);
            convertView.setTag(position);
           
            keyword.setFocusable(true);
            removeButton.setFocusable(true);
            
            return convertView;
        }        
   }
}