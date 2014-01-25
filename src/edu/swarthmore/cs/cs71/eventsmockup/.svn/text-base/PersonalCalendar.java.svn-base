package edu.swarthmore.cs.cs71.eventsmockup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class PersonalCalendar extends Activity {

	ListView lv;
	private MyAdapter myAdapter;
	
	ArrayList<HashMap<String, String>> list;  // list of all user events
	ArrayList<String> list_deleted;		// list of titles of all events that user has removed from user events
	
	private final Context activityContext = this;
	
	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_personal_calendar);
		
		list = (ArrayList<HashMap<String, String>>) getIntent().getSerializableExtra("user events");
		Collections.sort(list, new EventComparator());
		list_deleted = new ArrayList<String>();
			    
	    myAdapter = new MyAdapter(list);
	    
		lv = (ListView) findViewById(R.id.list_personal_events);
		lv.setAdapter(myAdapter);
		
	}
	
	/*
     * Custom ViewHolder class for ListView items
     */
	class ViewHolder {
        TextView title;
        TextView description;
        Button deleteButton;
        Button infoButton;
     }

	/*
	 * Custom MyAdapter is adapter for user events ListView
	 */
     public class MyAdapter extends BaseAdapter {
         private ArrayList<HashMap<String,String>> list_items;

         public MyAdapter(ArrayList<HashMap<String,String>> list) {
             list_items = list;
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

         public View getView(final int position, View convertView, ViewGroup parent) {
             final ViewHolder holder;
             if (convertView == null) {
	           	 LayoutInflater mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                 convertView = mInflater.inflate(R.layout.custom_personal_event, null);
             }
          
             // create new holder and bind all values upon each call of getView()
             holder = new ViewHolder();      
             holder.title=(TextView)convertView
             		.findViewById(R.id.text1); 
             holder.title.setText((CharSequence) (list_items.get(position).get("title")));
             holder.description = (TextView) convertView
                     .findViewById(R.id.text2);
             holder.description.setText((CharSequence) (list_items.get(position).get("text")));             
             holder.deleteButton = (Button) convertView
                    .findViewById(R.id.delete);
             convertView.setTag(position);
            
             //updates tag of the button view as we scroll
             holder.title.setTag(position);
             holder.description.setTag(position);
             holder.deleteButton.setTag(position);

          
            
            // on Delete button click: remove event from user events
            holder.deleteButton.setOnClickListener(new OnClickListener() {

                 @Override
                 public void onClick(View view) {
                	 deleteEvent(view);
                 }
             });
                         
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
         			
         			
         			Button delete = (Button) dialog.findViewById(R.id.info_add);
         			delete.setText("Delete event");
         			delete.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                        	myAdapter.deleteEvent(v);
                       	    dialog.dismiss();
                       }
                    });
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
          * Removes event from user events
          * Adds event to list_deleted so it will no longer be marked as added in the main activity
          */
       	public void deleteEvent(View v){       		
       		int tag = (Integer) v.getTag();
         	 HashMap<String,String> item = list_items.get(tag);
         	 list_deleted.add(item.get("title"));
         	 list_items.remove(tag);
         	 
 	         myAdapter.notifyDataSetChanged();
       	}
    }
	
     /*
      * Begins return intent with extras of user events list and list of events removed from user events
      */
	public void changeView(View v){		
		Intent returnIntent = new Intent();
		returnIntent.putExtra("user events",list);
		returnIntent.putExtra("deleted events", list_deleted);
		setResult(RESULT_OK,returnIntent);     
		finish();
	}

	@Override
	public void onBackPressed() {
		changeView(this.findViewById(R.id.switch_to_main_stream));
		super.onBackPressed();
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
	
	
}
