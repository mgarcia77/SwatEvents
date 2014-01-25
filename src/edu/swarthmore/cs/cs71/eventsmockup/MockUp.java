package edu.swarthmore.cs.cs71.eventsmockup;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.text.format.Time;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MockUp extends Activity {
	
	private final int NEW_JSON = 0;
	private final int REFRESH_JSON = 1;
	
	private final int PERSONAL_CALENDAR = 0;
	private final int RECOMMENDATION_SETTINGS = 1;
	private final int MAPS_VIEW = 2;
	
	private final int SYNC_ADD = 0;
	private final int SYNC_UNADD = 1;
			
	private ArrayList<String> added_prefs;		// list of keywords for positive recommendation
	private ArrayList<String> blocked_prefs;	// list of keywords for negative recommendation
	
	private final Context activityContext = this;
	
	private String raw_json;
	
	private AllAdapter allAdapter;	// adapter for ListView of all events
	private RecAdapter recAdapter;	// adapter for ListView of recommended events
	
	ArrayList<HashMap<String,String>> list_master;   // all events, no filter: not displayed anywhere
    ArrayList<HashMap<String,String>> list_recommended;  // recommended, shown in rec feed
    ArrayList<HashMap<String,String>> list_all;  // events that are not hidden
    ArrayList<HashMap<String,String>> list_user;  // events that are added to user's calendar
    
	private ListView lv1 = null;	// ListView of recommended events
	private ListView lv2 = null;	// ListView of all events

	// external storage
	private final File jsonStoreFile = new File(Environment.getExternalStorageDirectory(), "my_json");

	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

	    setContentView(R.layout.activity_mock_up);

	    // check for savedInstanceState, load any saved info, and initialize any
	    // lists that can't be loaded
	    list_master = new ArrayList<HashMap<String,String>>();
		list_all = new ArrayList<HashMap<String,String>>();
		list_recommended = new ArrayList<HashMap<String,String>>();
		list_user = new ArrayList<HashMap<String,String>>();
		

	    added_prefs = new ArrayList<String>();
	    blocked_prefs = new ArrayList<String>();
		
		readStoredJSON();
		removeOldEvents();
		if (raw_json == null){
			getJSON(NEW_JSON);
		} else {
			getJSON(REFRESH_JSON);
		}
			    	   
	    lv1 = (ListView) findViewById (R.id.list1);
	    lv1.setItemsCanFocus(true);   	    
	    
	    lv2 = (ListView) findViewById (R.id.list2);
	    lv2.setItemsCanFocus(true);
        
	    
	    // Initialize our Adapters and plug them in to the ListViews
	    allAdapter = new AllAdapter(list_all);
	    recAdapter = new RecAdapter(list_recommended);
	    
	    lv1.setAdapter(recAdapter);
	    lv2.setAdapter(allAdapter);
	    
	    getRecommendedItems();
	    
	} 
	
	public void removeOldEvents(){
		HashMap<String,String> event;
		String today_date = getToday();
		String event_date;
		String event_title;
		int[] indices = new int[list_all.size()];
		int index_counter = 0;
		for (int i = 0; i < list_all.size(); i++){
			event = list_all.get(i);
			event_date = event.get("date");	        
        	event_title = event.get("title");
			if (event_date.compareTo(today_date) < 0){
	        	if (containsEvent(list_master, event_title)){
	        		removeEvent(list_master, event_title);
	        	}
	        	if (containsEvent(list_user, event_title)){
	        		removeEvent(list_user, event_title);
	        	}
	        	indices[index_counter] = i;
	        	index_counter++;
			}	        
		}
		if (list_all.size() > 0){
		for (int j = index_counter; j >= 0; j--){
			list_all.remove(j);
		}
		}
	}
		
	/* onSaveInstanceState
	 * 		Saves instance state
	 * 		Stores all event lists: list_user, list_master, list_all, list_recommended 
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		saveListState();
		outState.putSerializable("user events", list_user);
		outState.putSerializable("master events", list_master);
		outState.putSerializable("all events", list_all);
		outState.putSerializable("recommended events", list_recommended);
		super.onSaveInstanceState(outState);
	}
	

	@Override
	public void onBackPressed() {
		saveListState();
		super.onBackPressed();
	}

	public void saveListState(){
		FileOutputStream fos;
		try {
			// save JSON
			fos = new FileOutputStream(jsonStoreFile);
			fos.write(raw_json.getBytes());
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			// save list_user
			fos = openFileOutput("my_events",Context.MODE_PRIVATE);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(list_user);
			oos.close();
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			// save list_all
			fos = openFileOutput("all_events",Context.MODE_PRIVATE);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(list_all);
			oos.close();
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			// save list_recommended
			fos = openFileOutput("rec_events",Context.MODE_PRIVATE);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(list_recommended);
			oos.close();
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			// save list_master
			fos = openFileOutput("master_events",Context.MODE_PRIVATE);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(list_master);
			oos.close();
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			// save added_prefs
			fos = openFileOutput("added_prefs",Context.MODE_PRIVATE);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(added_prefs);
			oos.close();
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			// save blocked_prefs
			fos = openFileOutput("blocked_prefs",Context.MODE_PRIVATE);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(blocked_prefs);
			oos.close();
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	public void readStoredJSON(){
		try {
			// read JSON
			String json_data = null;
			FileInputStream fis = new FileInputStream(jsonStoreFile);
			DataInputStream in = new DataInputStream(fis);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			while ((strLine = br.readLine()) != null){
				json_data += strLine;
			}
			in.close();
			raw_json = json_data;
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			// read list_user
			FileInputStream fis;
			fis = openFileInput("my_events");
			ObjectInputStream ois = new ObjectInputStream(fis);
			list_user = (ArrayList<HashMap<String,String>>) ois.readObject();
			ois.close();
			fis.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			// read list_all
			FileInputStream fis;
			fis = openFileInput("all_events");
			ObjectInputStream ois = new ObjectInputStream(fis);
			list_all = (ArrayList<HashMap<String,String>>) ois.readObject();
			ois.close();
			fis.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			// read list_recommended
			FileInputStream fis;
			fis = openFileInput("rec_events");
			ObjectInputStream ois = new ObjectInputStream(fis);
			list_recommended = (ArrayList<HashMap<String,String>>) ois.readObject();
			ois.close();
			fis.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			// read list_master
			FileInputStream fis;
			fis = openFileInput("master_events");
			ObjectInputStream ois = new ObjectInputStream(fis);
			list_master = (ArrayList<HashMap<String,String>>) ois.readObject();
			ois.close();
			fis.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			// read added_prefs
			FileInputStream fis;
			fis = openFileInput("added_prefs");
			ObjectInputStream ois = new ObjectInputStream(fis);
			added_prefs = (ArrayList<String>) ois.readObject();
			ois.close();
			fis.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			// read blocked_prefs
			FileInputStream fis;
			fis = openFileInput("blocked_prefs");
			ObjectInputStream ois = new ObjectInputStream(fis);
			blocked_prefs = (ArrayList<String>) ois.readObject();
			ois.close();
			fis.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/* getRecommendedItems
	 * 		Input: none
	 * 		Return: none
	 * 		Clears and refills list_recommended based on preferences.
	 * 		Checks the title and description of each event against added and blocked words
	 * 
	 */
    private void getRecommendedItems() {
    	list_recommended.clear();
    	for (int i=0; i < list_all.size(); i++){
	    	HashMap<String,String> event = list_all.get(i);
	    	String title = event.get("title").toLowerCase();
	    	String descrip = event.get("text").toLowerCase();
	    	
	    	checkInAddedPrefs(event, title, descrip);
	    	
	    }
    	allAdapter.notifyDataSetChanged();
    	recAdapter.notifyDataSetChanged();
	}
    
    public void checkInAddedPrefs(HashMap<String,String> event, String title, String descrip){
    	boolean blocked = false;
    	for (int k = 0; k < added_prefs.size(); k++){
			if (title.contains(added_prefs.get(k)) || descrip.contains(added_prefs.get(k))){
				for (int j = 0; j < blocked_prefs.size(); j++){
		    		if (title.contains(blocked_prefs.get(j))){
		    			blocked = true;
		    		}
		    	}
				if (! list_recommended.contains(event) && !blocked){
    				list_recommended.add(event);
    			}
			}
			blocked = false;
		}
    }


    /*
     * Custom ViewHolder class for ListView items
     */
	class ViewHolder {

        TextView title;
        TextView description;
        Button addButton;
        Button infoButton;
        
     }

	/*
	 * Custom AllAdapter is adapter for all events ListView
	 */
     public class AllAdapter extends BaseAdapter {
         private ArrayList<HashMap<String,String>> list_items;

         public AllAdapter(ArrayList<HashMap<String,String>> list_all) {
             list_items = list_all;
         }

         public int getCount() {
             return list_items.size();
         }

         public HashMap<String, String> getItem(int position) {
             return list_items.get(position);
         }

         public long getItemId(int position) {
             return position;
         }

         /*
          * Inflates and populates each View in ListView
          * Normal items have normal view; items in list_user have grayed-out view; hidden items don't appear
          */
         public View getView(final int position, View convertView, ViewGroup parent) {
             final ViewHolder holder;
             String isAdded = allAdapter.getItem(position).get("is_added");
             LayoutInflater mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
             if (isAdded.equals("true")){
           		 convertView = mInflater.inflate(R.layout.two_gray, null);
           		 
           	// create new holder and bind all values upon each call of getView()
           		holder = new ViewHolder();      
                holder.title=(TextView)convertView
                		.findViewById(R.id.text1_gray); 
                holder.title.setText((CharSequence) (list_items.get(position).get("title")));
                holder.description = (TextView) convertView
                        .findViewById(R.id.text2_gray);
                holder.description.setText((CharSequence) (list_items.get(position).get("text")));
           		holder.addButton = (Button) convertView
                        .findViewById(R.id.gray_add);
           		holder.addButton.setText("X");
           	// on Add button click: remove event from user events (since it was added before)
                holder.addButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                   	 removeEvent(view);
                   }
               });
           	 } else {
           		convertView = mInflater.inflate(R.layout.two, null);
           		
           	// create new holder and bind all values upon each call of getView()
           		holder = new ViewHolder();      
                holder.title=(TextView)convertView
                		.findViewById(R.id.text1); 
                holder.title.setText((CharSequence) (list_items.get(position).get("title")));
                holder.description = (TextView) convertView
                        .findViewById(R.id.text2);
                holder.description.setText((CharSequence) (list_items.get(position).get("text")));
           		holder.addButton = (Button) convertView
                        .findViewById(R.id.add);
                
                // on Add button click: add event to user events
                holder.addButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                   	 addEvent(view);
                   }
               });
           	 }
          
             convertView.setTag(position);
            
             //updates tag of the button view as we scroll
             holder.title.setTag(position);
             holder.description.setTag(position);
             holder.addButton.setTag(position);
             
             holder.title.setFocusable(true);
             holder.description.setFocusable(true);
             holder.title.requestFocus();
             holder.description.requestFocus();

             convertView.setOnClickListener(new OnClickListener(){
            	 @Override
            	 public void onClick(View view){
            		final View v = view;
            		            		
            		final Dialog dialog = new Dialog(activityContext);
            		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
         			dialog.setContentView(R.layout.item_full_info);
         			
         			TextView title = (TextView) dialog.findViewById(R.id.info_title);
         			title.setText(list_items.get(position).get("title"));
         			          
         			TextView date = (TextView) dialog.findViewById(R.id.info_date);
         			date.setText(toHumanDate(list_items.get(position).get("date")));
         			
         			TextView time = (TextView) dialog.findViewById(R.id.info_time);
         			String start_end_time = toHumanTime(list_items.get(position).get("time"));
         			String end_time = toHumanTime(list_items.get(position).get("end_time"));
         			//Log.i("event info",""+end_time);
         			if (! end_time.equals("null")){
         				start_end_time += " - " + end_time;
         			}
         			if (! start_end_time.equals("null")){
         				time.setText(start_end_time);
         			}
         			
         			TextView description = (TextView) dialog.findViewById(R.id.info_description);
         			description.setMovementMethod(new ScrollingMovementMethod());
         			description.setText(holder.description.getText());
         			
         			TextView poster = (TextView) dialog.findViewById(R.id.info_poster);
         			poster.setText(list_items.get(position).get("sponsor"));
         			
         			TextView contact = (TextView) dialog.findViewById(R.id.info_contact);
         			contact.setText(list_items.get(position).get("poster_email"));
         			
         			TextView url = (TextView) dialog.findViewById(R.id.info_url);
         			String url_text = list_items.get(position).get("sponsor_url");
         			if (! url_text.equals("null")){
         				url.setText(url_text);
         			}
         			
         			TextView place = (TextView) dialog.findViewById(R.id.info_place);
         			String place_text = list_items.get(position).get("place");
         			place.setText(place_text);
         			         			 			
         			Button add = (Button) dialog.findViewById(R.id.info_add);
         			if (list_items.get(position).get("is_added").equals("true")){
         				add.setText("Remove event");
         				add.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View view) {
                            	allAdapter.removeEvent(v);
                           	    dialog.dismiss();
                           }
                        });
         			} else {
         				add.setOnClickListener(new OnClickListener() {
         					@Override
         					public void onClick(View view) {
         						allAdapter.addEvent(v);
         						dialog.dismiss();
         					}
         				});
         			}
         			Button cancel = (Button) dialog.findViewById(R.id.info_cancel);
         			cancel.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                        	dialog.dismiss();
                       }
                    });
         			          
         			dialog.show();
            	 }
             });
             
             return convertView;
         }
         
         /*
          * Adds event to list_user
          * Marks event as added (getView will inflate different layout) in list_all and list_recommended
          */
         public void addEvent(View v){
       		Toast.makeText(getApplicationContext(), "Adding event to your calendar", Toast.LENGTH_SHORT).show();
       		int tag = (Integer) v.getTag();
       		HashMap<String,String> item = list_items.get(tag);
       		list_user.add(item);
       		recAdapter.syncItem(list_items.get(tag).get("title"), SYNC_ADD);

       		// remove event from list of events to be shown
       		allAdapter.getItem(tag).put("is_added", "true");
	          	 
	          allAdapter.notifyDataSetChanged();
       	 }
         
         public void removeEvent(View v){
       		Toast.makeText(getApplicationContext(), "Removing event from your calendar", Toast.LENGTH_SHORT).show();
       		int tag = (Integer) v.getTag();
         	HashMap<String,String> item = list_items.get(tag);
         	list_user.remove(item);
         	recAdapter.syncItem(list_items.get(tag).get("title"), SYNC_UNADD);
         	 
         	  // tag event as added to the user's events
         	allAdapter.getItem(tag).put("is_added", "false");
 	          	 
 	         allAdapter.notifyDataSetChanged();
       	}
         
         /*
          * syncItem
          * 	Input: title (String) - name of event to sync
          * 		   type (int) - what action to take (SYNC_ADD, SYNC_UNADD)
          * 	When an event is modified in recAdapter, this method allows allAdapter to sync the event
          * 	and display the same style and information.
          */
         public void syncItem(String title, int type){        	
       		for (int i = 0; i < list_items.size(); i++){
       			if (list_items.get(i).get("title").equals(title)){
       				if (type == SYNC_ADD){
       					allAdapter.getItem(i).put("is_added", "true");
       				} else if (type == SYNC_UNADD) {
       					allAdapter.getItem(i).put("is_added", "false");
       				}
       			}
       		}
       		allAdapter.notifyDataSetChanged();
       	}
    }

     /*
 	 * Custom RecAdapter is adapter for recommended events ListView
 	 */
    public class RecAdapter extends BaseAdapter {
          private ArrayList<HashMap<String,String>> list_items;

          public RecAdapter(ArrayList<HashMap<String,String>> list_recommended) {
             list_items = list_recommended;
          }

          public int getCount() {
              return list_items.size();
          } 

          public HashMap<String, String> getItem(int position) {
              return list_items.get(position);
          }

          public long getItemId(int position) {
              return position;
          }

          /*
           * Inflates and populates each View in ListView
           * Normal items have normal view; items in list_user have grayed-out view; hidden items don't appear
           */
          public View getView(final int position, View convertView, ViewGroup parent) {
        	  final ViewHolder holder;
              String isAdded = recAdapter.getItem(position).get("is_added");
              LayoutInflater mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
              if (isAdded.equals("true")){
            	 convertView = mInflater.inflate(R.layout.two_gray, null);
            		 
            	// create new holder and bind all values upon each call of getView()
            		holder = new ViewHolder();      
                 holder.title=(TextView)convertView
                 		.findViewById(R.id.text1_gray); 
                 holder.title.setText((CharSequence) (list_items.get(position).get("title")));
                 holder.description = (TextView) convertView
                         .findViewById(R.id.text2_gray);
                 holder.description.setText((CharSequence) (list_items.get(position).get("text")));
            	 holder.addButton = (Button) convertView
                         .findViewById(R.id.gray_add);
            	 holder.addButton.setText("X");
            	// on Add button click: remove event from user events (since it was added before)
                 holder.addButton.setOnClickListener(new OnClickListener() {
                     @Override
                     public void onClick(View view) {
                    	 removeEvent(view);
                    }
                });
            } else {
            	convertView = mInflater.inflate(R.layout.two, null);
            		
            	// create new holder and bind all values upon each call of getView()
            	holder = new ViewHolder();      
            	
            	 holder.title=(TextView)convertView
                		.findViewById(R.id.text1);
                 holder.title.setText((CharSequence) (list_items.get(position).get("title")));
                 holder.description = (TextView) convertView
                         .findViewById(R.id.text2);
                 holder.description.setText((CharSequence) (list_items.get(position).get("text")));
            	 holder.addButton = (Button) convertView
                         .findViewById(R.id.add);
                 
                 // on Add button click: add event to user events
                 holder.addButton.setOnClickListener(new OnClickListener() {
                     @Override
                     public void onClick(View view) {
                    	 addEvent(view);
                    }
                });
            	 }
           
              convertView.setTag(position);
             
              //updates tag of the button view as we scroll
              holder.title.setTag(position);
              holder.description.setTag(position);
              holder.addButton.setTag(position);
              
              holder.title.setFocusable(true);
              holder.description.setFocusable(true);
              holder.title.requestFocus();
              holder.description.requestFocus();
              
              convertView.setOnClickListener(new OnClickListener(){
             	 @Override
             	 public void onClick(View view){
             		             		  
                 	 final View v = view;
            		
                 	final Dialog dialog = new Dialog(activityContext);
            		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
         			dialog.setContentView(R.layout.item_full_info);
         			
         			TextView title = (TextView) dialog.findViewById(R.id.info_title);
         			title.setText(list_items.get(position).get("title"));
          
         			TextView date = (TextView) dialog.findViewById(R.id.info_date);
         			date.setText(toHumanDate(list_items.get(position).get("date")));
         			
         			TextView time = (TextView) dialog.findViewById(R.id.info_time);
         			String start_end_time = toHumanTime(list_items.get(position).get("time"));
         			String end_time = toHumanTime(list_items.get(position).get("end_time"));
         			//Log.i("event info",""+end_time);
         			if (! end_time.equals("null")){
         				start_end_time += " - " + end_time;
         			}
         			if (! start_end_time.equals("null")){
         				time.setText(start_end_time);
         			}
         			
         			TextView description = (TextView) dialog.findViewById(R.id.info_description);
         			description.setMovementMethod(new ScrollingMovementMethod());
         			description.setText(holder.description.getText());
         			
         			TextView poster = (TextView) dialog.findViewById(R.id.info_poster);
         			poster.setText(list_items.get(position).get("sponsor"));
         			
         			TextView contact = (TextView) dialog.findViewById(R.id.info_contact);
         			contact.setText(list_items.get(position).get("poster_email"));
         			
         			TextView url = (TextView) dialog.findViewById(R.id.info_url);
         			String url_text = list_items.get(position).get("sponsor_url");
         			if (! url_text.equals("null")){
         				url.setText(url_text);
         			}
         			
         			TextView place = (TextView) dialog.findViewById(R.id.info_place);
         			String place_text = list_items.get(position).get("place");
         			place.setText(place_text);
         			
         			
         			Button add = (Button) dialog.findViewById(R.id.info_add);
         			if (list_items.get(position).get("is_added").equals("true")){
         				add.setText("Remove event");
         				add.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View view) {
                            	recAdapter.removeEvent(v);
                           	    dialog.dismiss();
                           }
                        });
         			} else {
         				add.setOnClickListener(new OnClickListener() {
         					@Override
         					public void onClick(View view) {
         						recAdapter.addEvent(v);
         						dialog.dismiss();
         					}
         				});
         			}
         			Button cancel = (Button) dialog.findViewById(R.id.info_cancel);
         			cancel.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                        	dialog.dismiss();
                       }
                    });
         			          
         			dialog.show();
             	 }
              });

              return convertView;
          }
          
          /*
           * Adds event to list_user
           * Marks event as added (getView will inflate different layout) in list_all and list_recommended
           */  
      	public void addEvent(View v){
      		Toast.makeText(getApplicationContext(), "Adding event to your calendar", Toast.LENGTH_SHORT).show();
      		int tag = (Integer) v.getTag();
        	HashMap<String,String> item = list_items.get(tag);
        	list_user.add(item);
        	allAdapter.syncItem(list_items.get(tag).get("title"), SYNC_ADD);
        	 
        	// tag event as added to the user's events
        	recAdapter.getItem(tag).put("is_added", "true");
	          	 
	        recAdapter.notifyDataSetChanged();
      	}
      	
      	public void removeEvent(View v){
      		Toast.makeText(getApplicationContext(), "Removing event from your calendar", Toast.LENGTH_SHORT).show();
      		int tag = (Integer) v.getTag();
        	HashMap<String,String> item = list_items.get(tag);
        	list_user.remove(item);
        	allAdapter.syncItem(list_items.get(tag).get("title"), SYNC_UNADD);
        	 
        	  // tag event as added to the user's events
        	recAdapter.getItem(tag).put("is_added", "false");
	          	 
	         recAdapter.notifyDataSetChanged();
      	}
      	      	
      	/*
         * syncItem
         * 	Input: title (String) - name of event to sync
         * 		   type (int) - what action to take (SYNC_ADD, SYNC_UNADD)
         * 	When an event is modified in allAdapter, this method allows recAdapter to sync the event
         * 	and display the same style and information.
         */
      	public void syncItem(String title, int type){
      		for (int i = 0; i < list_items.size(); i++){
      			if (list_items.get(i).get("title").equals(title)){
      				if (type == SYNC_ADD){
       					recAdapter.getItem(i).put("is_added", "true");
       				} else if (type == SYNC_UNADD) {
       					recAdapter.getItem(i).put("is_added", "false");
       				}
      			}
      		}
      		recAdapter.notifyDataSetChanged();
      	}

    }

    
    /*
     * Converts the date stored in an event to a date more naturally readable to humans.
     * 		Input: date (String) - date in the form 'yyyy-mm-dd'
     * 		Return: date in the form '<Month> <Date>' 
     */
    public String toHumanDate(String date){
    	if (date == null || date.length() < 10){
    		return "";
    	}
    	int m = Integer.parseInt(date.substring(5, 7));
    	int d = Integer.parseInt(date.substring(8));
    	String new_date = "";
    	switch (m){
    		case 1:
    			new_date += "Jan.";
    			break;
    		case 2:
    			new_date += "Feb.";
    			break;
    		case 3:
    			new_date += "March";
    			break;
    		case 4:
    			new_date += "April";
    			break;
    		case 5:
    			new_date += "May";
    			break;
    		case 6:
    			new_date += "June";
    			break;
    		case 7:
    			new_date += "July";
    			break;
    		case 8:
    			new_date += "Aug.";
    			break;
    		case 9:
    			new_date += "Sep.";
    			break;
    		case 10:
    			new_date += "Oct.";
    			break;
    		case 11:
    			new_date += "Nov.";
    			break;
    		case 12:
    			new_date += "Dec.";
    			break;
    		default:
    			break;
    	}
    	new_date += " " + d;
    	return new_date;
    }
    
    /*
     * Converts the time stored in an event to a time more naturally readable to humans.
     * 		Input: time (String) - time in the form 'hh:mm:ss' (24 hour time)
     * 		Return: time in the form 'hh:mm <am/pm>'
     */
    public String toHumanTime(String time){
    	if (time == null || time.length() < 8){
    		return "";
    	}
    	int h = Integer.parseInt(time.substring(0,2));
    	int m = Integer.parseInt(time.substring(3,5));
    	String new_time = "";
    	String am_pm = "";
    	if (h == 0){
    		new_time += "12:";
    		am_pm = "am";
    	} else if (h < 12){
    		new_time += h + ":";
    		am_pm = "am";
    	} else if (h == 12){
    		new_time += "12:";
    		am_pm = "pm";
    	} else {
    		new_time += (h-12) + ":";
    		am_pm = "pm";
    	}
    	if (m < 10){
    		new_time += "0" + m;
    	} else {
    		new_time += m;
    	}
    	new_time += " " + am_pm;
    	return new_time;
    }
    
    /*
     * Starts an intent to begin the user events activity
     */
	public void changeView(View v){
		Intent i = new Intent(this, PersonalCalendar.class);
		i.putExtra("user events", list_user);
		startActivityForResult(i, PERSONAL_CALENDAR);
		// pass to personal calendar view
	}
	
	/*
	 * Starts an intent to begin the mapping activity
	 */
	public void whatsNearMe(View v){
		Toast.makeText(this, "Mapping what's near you", Toast.LENGTH_SHORT).show();
		Intent i = new Intent(this, MapView.class);
		i.putExtra("all events", list_master);
		i.putExtra("rec events", list_recommended);
		startActivityForResult(i, MAPS_VIEW);
		// pass to maps view
	}
	
	/*
	 * onActivityResult
	 * 		Inputs: requestCode (int) - code input from this activity when the just-ended activity was started
	 * 				resultCode (int) - is RESULT_OK on successful completion
	 * 				data (Intent) - any data passed back from the just-ended activity
	 * 	On returning from user events activity (requestCode PERSONAL_CALENDAR):
	 * 		Update list_user to reflect what the user has modified
	 * 		Re-add any events that the user has removed from list_user (ArrayList deleted)
	 * 	On returning from modifying recommendations settings (requestCode RECOMMENDATON_SETTINGS)
	 * 		Update added_prefs and blocked_prefs to reflect user's new preferences
	 * 		Get recommended events again based on new preferences
	 * 	On returning from maps activity (requestCode MAPS_VIEW)
	 * 		Adds events to list_user and updates allAdapter and recAdapter as appropriate
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		// returned from personal calendar activity
		if (requestCode == PERSONAL_CALENDAR && resultCode == RESULT_OK){
			Log.i("onActivityResult","returned from personal calendar");
			list_user = (ArrayList<HashMap<String, String>>) data.getSerializableExtra("user events");
			ArrayList<String> deleted = (ArrayList<String>) data.getSerializableExtra("deleted events");

			for (int i = 0; i < deleted.size(); i++) {
				String title = deleted.get(i);				
				for (int j = 0; j < list_all.size(); j++){
					HashMap<String,String> item = list_all.get(j);
	      			if (item.get("title").equals(title)){	      				
	      				item.put("is_added", "false");
	      				allAdapter.syncItem(title, SYNC_UNADD);
	      				recAdapter.syncItem(title, SYNC_UNADD);
	      			}
	      		}
			}
			allAdapter.notifyDataSetChanged();
			recAdapter.notifyDataSetChanged();
			
		} else if (requestCode == RECOMMENDATION_SETTINGS && resultCode == RESULT_OK){
			Log.i("onActivityResult","returned from recommendations settings");
			added_prefs = (ArrayList<String>) data.getSerializableExtra("added prefs");
			blocked_prefs = (ArrayList<String>) data.getSerializableExtra("blocked prefs");
			getRecommendedItems();
			
		} else if (requestCode == MAPS_VIEW && resultCode == RESULT_OK){
			Log.i("onActivityResult","returned from maps view");
			ArrayList<String> added = (ArrayList<String>) data.getSerializableExtra("added events");
			for (int i = 0; i < added.size(); i++){
				String title = added.get(i);
				for (int j = 0; j < list_all.size(); j++){
					HashMap<String,String> item = list_all.get(j);
	      			if (item.get("title").equals(title)){
	      				list_user.add(item);
	      				allAdapter.syncItem(title, SYNC_ADD);
	      				recAdapter.syncItem(title, SYNC_ADD);
	      			}
	      		}
			}
		}
	}
	
	/*
	 * getJSON
	 * 		Input: type (int) - how to get events
	 * 					NEW_JSON - clear old data and start over; REFRESH_JSON - add new events to existing data
	 * 	Reads JSON from the RSD site and tries to parse it. 
	 */
	public void getJSON(int type){
		try{
		DefaultHttpClient httpclient = new DefaultHttpClient(new BasicHttpParams());
		HttpPost httppost = new HttpPost("http://rsd.sccs.swarthmore.edu/json/events.json");
		httppost.setHeader("Content-type", "application/json");

		InputStream inputStream = null;
		String result = null;
		HttpResponse response = httpclient.execute(httppost);           
		HttpEntity entity = response.getEntity();

		inputStream = entity.getContent();
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
		StringBuilder sb = new StringBuilder();

		String line = null;
		while ((line = reader.readLine()) != null)
		{
		    sb.append(line + "\n");
		}
		result = sb.toString();
		raw_json = result;
		parseJSON(result, type);
		Log.i("getJSON success?","" + result);
		} catch (Exception e){
			Log.i("getJSON exception",""+e);
		}
	}

	/*
	 * parseJSON
	 * 		Input: result (String) - retrieved JSON to parse
	 * 			   type (int) - how to get events
	 * 					NEW_JSON - clear old data and start over; REFRESH_JSON - add new events to existing data
	 * 	Parses JSON result and adds to list_all and list_master as appropriate.
	 */
	private void parseJSON(String result, int type) {
		try {
			if (type == NEW_JSON){
				list_all.clear();
				list_master.clear();
			}
			JSONArray events = new JSONArray(result);
			
			for(int i = 0; i < events.length(); i++){
		        JSONObject event = events.getJSONObject(i);
		        
	        	String event_title = event.getString("title");
		        if (!containsEvent(list_all, event_title)){
		        	HashMap<String, String> map = makeEventMap(event);
		        	if (! containsEvent(list_master,event_title)){
		               	list_master.add(map);
		            }
		        	list_all.add(map);
		        }
		                        
			}
			
			Collections.sort(list_all, new EventComparator());
			Collections.sort(list_master, new EventComparator());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}
	
	public String getToday(){
		Time today = new Time(Time.getCurrentTimezone());
		today.setToNow();
		int month = today.month + 1;
		String m;
		if (month < 10){
			m = "0" + month;
		} else {
			m = "" + month;
		}
		int day = today.monthDay;
		String d;
		if (day < 10){
			d = "0" + day;
		} else {
			d = "" + day;
		}
		String date = today.year + "-" + m + "-" + d;
		return date;
	}
	
	public boolean containsEvent(ArrayList<HashMap<String,String>> list, String title){
		HashMap<String,String> item;
		for (int i = 0; i < list.size(); i++){
			item = list.get(i);
			if (item.get("title").equals(title)){
				return true;
			}
		}
		return false;
	}
	
	public boolean removeEvent(ArrayList<HashMap<String,String>> list, String title){
		HashMap<String,String> item;
		for (int i = 0; i < list.size(); i++){
			item = list.get(i);
			if (item.get("title").equals(title)){
				list.remove(i);
				return true;
			}
		}
		return false;
	}
	
	public HashMap<String,String> makeEventMap(JSONObject event) throws JSONException{
		// Storing each json item in variable
        String title = event.getString("title");
        String slug = event.getString("slug");
        String text = event.getString("text");
        String sponsor = event.getString("sponsor");
        String sponsor_url = event.getString("sponsor_url");
        String poster_email = event.getString("poster_email");
        String date_start = event.getString("date_start");
        String date_end = event.getString("date_end");
        String date = event.getString("date");
        String time = event.getString("time");
        String end_time = event.getString("end_time");
        String place = event.getString("place");
 
        // creating new HashMap
        HashMap<String, String> map = new HashMap<String, String>();

        // adding each child node to HashMap key => value
        map.put("title", title);
        map.put("slug", slug);
        map.put("text", text);
        map.put("sponsor", sponsor);
        map.put("sponsor_url", sponsor_url);
        map.put("poster_email", poster_email);
        map.put("date_start", date_start);
        map.put("date_end", date_end);
        map.put("date", date);
        map.put("time", time);
        map.put("end_time", end_time);
        map.put("place", place);
        map.put("is_added", "false");
        
        return map;
	}

	/*
	 * Inflates the options menu
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.activity_mock_up, menu);
	    return true;
	}
	
	/*
	 * Handles selection in options menu
	 * Recommendations: Starts recommendation settings activity
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
		switch (item.getItemId()){
		case R.id.recommendations:
			Intent i = new Intent(this, RecommendationSettings.class);
			i.putExtra("added prefs", added_prefs);
			i.putExtra("blocked prefs", blocked_prefs);
			startActivityForResult(i,RECOMMENDATION_SETTINGS);
		}
		return true;
	}
	
}

/*
 * Custom EventComparator allows for sorting of events
 */
class EventComparator implements Comparator<HashMap<String,String>>{
	
	public int compare(HashMap<String,String> e1, HashMap<String,String> e2){
		String date1 = e1.get("date") + e1.get("time");
		String date2 = e2.get("date") + e2.get("time");
		
		return date1.compareTo(date2);
	}
}