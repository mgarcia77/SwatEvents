package edu.swarthmore.cs.cs71.eventsmockup;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.text.format.Time;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MapView extends FragmentActivity implements OnInfoWindowClickListener {

	  private GoogleMap map; 
	  Time today;
	  
	  // LatLng objects for all major locations at Swarthmore
	  static final LatLng AMPHITHEATER = new LatLng(39.904295, -75.356155);
	  static final LatLng APDK = new LatLng(39.903172, -75.350877);
	  static final LatLng BCC = new LatLng(39.906958, -75.351472); 
	  static final LatLng BEARDSLEY = new LatLng(39.906484, -75.354734);
	  static final LatLng BELLTOWER = new LatLng(39.904139, -75.354546); 
	  static final LatLng BENWEST = new LatLng(39.905085, -75.351252); 
	  static final LatLng BOND = new LatLng(39.905423, -75.350743); 
	  static final LatLng CORNELL = new LatLng(39.906213, -75.356145);
	  static final LatLng CRUMHENGE = new LatLng(39.902336, -75.360763); 
	  static final LatLng DANAWELL = new LatLng(39.903369, -75.357437);
	  static final LatLng DU = new LatLng(39.902851, -75.354852); 
	  static final LatLng FIELDHOUSE = new LatLng(39.901225, -75.354256); 
	  static final LatLng HICKS = new LatLng(39.906904, -75.354321);
	  static final LatLng ICC = new LatLng(39.903982, -75.354804); 
	  static final LatLng KITAO = new LatLng(39.902777, -75.355131);
	  static final LatLng KOHLBERG = new LatLng(39.905884, -75.354841);
	  static final LatLng LANGCENTER = new LatLng(39.908023, -75.353961);
	  static final LatLng LANGCONCERT = new LatLng(39.90548, -75.356193);
	  static final LatLng LPAC = new LatLng(39.905377, -75.355356);
	  static final LatLng MARTIN = new LatLng(39.905954, -75.355726);
	  static final LatLng MCCABE = new LatLng(39.905353, -75.352685);
	  static final LatLng MEPHISTOS = new LatLng(39.905888, -75.351381); 
	  static final LatLng MERTZFIELD = new LatLng(39.903731, -75.352304); 
	  static final LatLng ML = new LatLng(39.895616, -75.35394); 
	  static final LatLng MTGHOUSE = new LatLng(39.907283, -75.353237);
	  static final LatLng OFFCAMPUSSTUDY = new LatLng(39.906143, -75.352502); 
	  static final LatLng OLDECLUB = new LatLng(39.902587, -75.355415); 
	  static final LatLng OLDTARBLE = new LatLng(39.904505, -75.351944);
	  static final LatLng PACES = new LatLng(39.904143, -75.355109); 
	  static final LatLng PAPAZIAN = new LatLng(39.907311, -75.353934);
	  static final LatLng PARRISH = new LatLng(39.905213, -75.354192);
	  static final LatLng PCIRCLE = new LatLng(39.905423, -75.353366); 
	  static final LatLng PEARSON = new LatLng(39.906879, -75.353591);
	  static final LatLng PHIPSI = new LatLng(39.902818, -75.354514);
	  static final LatLng PPR = new LatLng(39.899863, -75.351456); 
	  static final LatLng SCHEUER = new LatLng(39.905545, -75.354865);
	  static final LatLng SCICOMMONS = new LatLng(39.906488, -75.355892);
	  static final LatLng SCI101 = new LatLng(39.906631, -75.355595);
	  static final LatLng SCI199 = new LatLng(39.906961, -75.355402);
	  static final LatLng SHARPLES = new LatLng(39.90332, -75.353768);
	  static final LatLng TROTTER = new LatLng(39.906443, -75.353918);
	  static final LatLng UPPERTARBLE = new LatLng(39.904283, -75.354841); 
	  static final LatLng WHARTON = new LatLng(39.903707, -75.356236); 
	  static final LatLng WISTER = new LatLng(39.906114, -75.352052); 
	  static final LatLng WORTH = new LatLng(39.905649, -75.350973); 
	  static final LatLng WRC = new LatLng(39.902616, -75.355726); 
	   	  
	  ArrayList<HashMap<String, String>> list;		// list of all events
	  ArrayList<HashMap<String, String>> list_recommended;	// list of recommended events
	  String time;
	  String next_twelve_hours;
	  HashMap<String,Integer> added_events;  // list of events added to the map; used to get info for pop-up box 
	  ArrayList<String> pos;  // list of tag numbers for events that should be added to list_user in main activity
	
	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map_view);
		
		added_events = new HashMap<String,Integer>();
		pos = new ArrayList<String>();
		
		list = (ArrayList<HashMap<String, String>>) getIntent().getSerializableExtra("all events");
		list_recommended = (ArrayList<HashMap<String, String>>) getIntent().getSerializableExtra("rec events");
		getNow();
		
		map = ((com.google.android.gms.maps.SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
		        .getMap();
		   
		map.setMyLocationEnabled(true);
		  
	}
	
	/*
	 * When activity is launched, should map events happening today and move map to user's location
	 */
	@Override
	protected void onResume() {
		super.onResume();
		
		mapToday();
	    moveMapToMyLocation();
	    
	    map.setOnInfoWindowClickListener(this);
	}

	/*
	 * Begins return intent with extra of events to add to list_user in main activity
	 */
	@Override
	public void onBackPressed() {
		Intent returnIntent = new Intent();
		returnIntent.putExtra("added events", pos);
		setResult(RESULT_OK,returnIntent);     
		finish();
		
	}

	/*
	 * Gets user's location and moves camera to focus on user's location
	 */
	private void moveMapToMyLocation() {
		LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Criteria crit = new Criteria();
		Location loc = lm.getLastKnownLocation(lm.getBestProvider(crit, false));
		
		CameraPosition cp = new CameraPosition.Builder()
			.target(new LatLng(loc.getLatitude(),loc.getLongitude()))
			.zoom(17)
			.build();
		
		CameraUpdate cu = CameraUpdateFactory.newCameraPosition(cp);
		map.moveCamera(cu);
		
	}

	/*
	 * Maps all events that will have started in the next twelve hours and which have not yet ended.
	 * 			e.g. If it is 12pm now, this function will map events that have start times earlier than
	 * 					12am and which have end times later than 12 pm.
	 * Events with no explicit end time are assumed to be 2 hours after the start time.
	 */
	private void mapToday() {
		String end_time;
		for (int i = 0; i < list.size(); i++){
			end_time = list.get(i).get("date");
			String s_time = list.get(i).get("time");
			String e_time = list.get(i).get("end_time");
			if (e_time.equals("null")){
				if (s_time.equals("null")){
					end_time += " 23:59:59";
				} else {
					end_time = plusNHours(end_time, s_time, 2);
				}
			} else if (e_time.equals("00:00:00")){
				end_time += " 23:59:59";
			} else {
				end_time += " " + e_time;
			}

			if (end_time.compareTo(next_twelve_hours) < 0 && end_time.compareTo(time) > 0){
				plotEvent(list.get(i));
				added_events.put(list.get(i).get("title"), i);
			}
		}
	}
	
	/*
	 * plusNHours
	 * 		Inputs: date (String) - a date of the form 'yyyy-mm-dd'
	 * 				s_time (String) - a start time of the form 'hh:mm:ss' in 24 hour time
	 * 				n (int) - how many hours should be added to s_time
	 * 		Return: a new date/time string of the form 'yyyy-mm-dd hh:mm:ss' that represents a date/time
	 * 				n hours later than the given date and s_time
	 */
	private String plusNHours(String date, String s_time, int n){
		String day = date.substring(8, 10).trim();
		String hour = s_time.substring(0, 2).trim();
		String minute = s_time.substring(3,5).trim();
		int h = ((Integer.parseInt(hour) + n) % 24);
		String newHour = h + ":";
		int m = Integer.parseInt(minute);
		String newMin = m + s_time.substring(5);
		if (h < 10){
			newHour = "0" + newHour;
		}
		if (m < 10){
			newMin = "0" + newMin;
		}		
		String newDay = date;
		if (newHour.compareTo(hour) < 0){
			int d = Integer.parseInt(day) + 1;
			if (d < 10){
				newDay = "0" + d;
			}
			newDay = date.substring(0,8) + newDay;
			newDay = checkMonth(newDay);
		}
		return newDay + " " + newHour + newMin;
	}
	
	// takes date string "yyyy-mm-dd"
	public String checkMonth(String date){
		int day = Integer.parseInt(date.substring(8));
		int month = Integer.parseInt(date.substring(5, 7));
		int year = Integer.parseInt(date.substring(0, 4));
		
		if (month == 1){
			if (day == 29){
				if ((year % 4) != 0){
					month++;
				}
			} else if (day > 29){
				month++;
			}
		} else if (month == 3 || month == 5 || month == 8 || month == 10){
			if (day > 30){
				month++;
			}
		} else {
			if (day > 31){
				month++;
			}
		}
		if (month > 11){
			month = 0;
			year++;
		}
		String m;
		String d;
		if (month < 10){
			m = "0" + month;
		} else {
			m = "" + month;
		}
		if (day < 10){
			d = "0" + day;
		} else {
			d = "" + day;
		}
		return year + "-" + m + "-" + d;
	}
	
	/*
	 * plotEvent
	 * 		Input: event (HashMap) - the HashMap representation of one event
	 * Attempts to match the event's place to a LatLng location at Swarthmore. (If an event's place can't be
	 * successfully matched, it maps to PARRISH.)
	 * Creates a marker with the event's info at that location, giving the marker an appropriate color.
	 * 		HUE_GREEN: Current event (i.e. an event that has started but not ended yet). Takes first precedence.
	 * 		HUE_ORANGE: Recommended event (i.e. an event in list_recommended). Takes second precedence.
	 * 		HUE_AZURE: Default color for an event that is not current or recommended.
	 */
	private void plotEvent(HashMap<String,String> event){
		String place = event.get("place").toLowerCase();
		LatLng ll;
		if (place.contains("sci") && place.contains("101")){
			ll = SCI101;
		} else if (place.contains("sci") && place.contains("199")){
			ll = SCI199;
		} else if (place.contains("scheuer") || place.contains("scheur") || place.contains("schuer")){
			ll = SCHEUER;
		} else if (place.contains("parrish")){
			ll = PARRISH;
		} else if (place.contains("lpac") || place.contains("list")){
			ll = LPAC;
		} else if (place.contains("concert") || place.contains("underhill") || (place.contains("lang") && place.contains("music"))){
			ll = LANGCONCERT;
		} else if (place.contains("sci")){
			ll = SCICOMMONS;
		} else if (place.contains("old") && place.contains("tarble")){
			ll = OLDTARBLE;
		} else if (place.contains("beardsley") || place.contains("media")){
			ll = BEARDSLEY;
		} else if (place.contains("cornell")){
			ll = CORNELL;
		} else if (place.contains("hicks") || place.contains("mural")){
			ll = HICKS;
		} else if (place.contains("papazian")){
			ll = PAPAZIAN;
		} else if (place.contains("mccabe")){
			ll = MCCABE;
		} else if (place.contains("kohl")){
			ll = KOHLBERG;
		} else if (place.contains("martin")){
			ll = MARTIN;
		} else if (place.contains("pearson")){
			ll = PEARSON;
		} else if (place.contains("amphitheater")){
			ll = AMPHITHEATER;
		} else if (place.contains("ap") && place.contains("lounge")){
			ll = APDK;
		} else if (place.contains("bcc") || place.contains("black cultural center")){
			ll = BCC;
		} else if (place.contains("bell") && place.contains("tower")){
			ll = BELLTOWER;
		} else if (place.contains("off") && place.contains("campus") && place.contains("study")){
			ll = OFFCAMPUSSTUDY;
		} else if ((place.contains("ben") && place.contains("west")) || place.contains("haverford") || place.contains("bryn") || (place.contains("off") && place.contains("campus"))){
			ll = BENWEST;
		} else if (place.contains("bond")){
			ll = BOND;
		} else if (place.contains("crum")){
			ll = CRUMHENGE;
		} else if (place.contains("dana") || place.contains("hallowell") || place.contains("trailer")){
			ll = DANAWELL;
		} else if (place.contains("mertz") && place.contains("field")){
			ll = MERTZFIELD;
		} else if (place.contains("fieldhouse") || place.contains("athletic") || place.contains("field")){
			ll = FIELDHOUSE;
		} else if (place.contains("icc") || (place.contains("ic") && (place.contains("center") || place.contains("room")))){
			ll = ICC;
		} else if (place.contains("kitao")){
			ll = KITAO;
		} else if (place.contains("mephisto")){
			ll = MEPHISTOS;
		} else if (place.contains("palmer") || place.contains("pitt") || place.contains("roberts") || place.contains("ppr")){
			ll = PPR;
		} else if (place.contains("old") && place.contains("club")){
			ll = OLDECLUB;
		} else if (place.contains("paces")){
			ll = PACES;
		} else if (place.contains("tarble") || place.contains("clothier") || place.contains("essie")){
			ll = UPPERTARBLE;
		} else if (place.contains("wharton")){
			ll = WHARTON;
		} else if (place.contains("wister") || place.contains("arboretum")){
			ll = WISTER;
		} else if (place.contains("worth") || place.contains("willets") || place.contains("lodge")){
			ll = WORTH;
		} else if (place.contains("wrc") || (place.contains("women") && place.contains("resource"))){
			ll = WRC;
		} else if (place.contains("phi") && place.contains("psi")){
			ll = PHIPSI;
		} else if (place.contains("ml") || place.contains("lyon")){
			ll = ML;
		} else if (place.contains("du") || place.contains("frat")){
			ll = DU;
		} else if (place.contains("parrish") && place.contains("circle")){
			ll = PCIRCLE;
		} else {
			ll = PARRISH;
		}
		
		float color;
		if (now(event.get("time"))){
			color = BitmapDescriptorFactory.HUE_GREEN;
		} else if (containsEvent(list_recommended, event.get("title"))){
			color = BitmapDescriptorFactory.HUE_ORANGE;
		} else {
			color = BitmapDescriptorFactory.HUE_AZURE;
		}
		
		@SuppressWarnings("unused")
		Marker e = map.addMarker(new MarkerOptions()
        .position(ll)
        .title(event.get("title"))
        .snippet(event.get("text"))
        .icon(BitmapDescriptorFactory.defaultMarker(color)));
	}
	
	/*
	 * now
	 * 		Input: start_time (String) - the start time for an event, in the form 'hh:mm:ss' in 24 hour time
	 * 		Return: (boolean) true if start_time is earlier than current time
	 * Determines whether the given time is earlier than the current time.
	 */
	public boolean now(String start_time){
		String right_now = today.hour + ":" + today.minute + ":" + "00";
		if (start_time.equals("null")){
			start_time = "00:00:00";
		}
		if (start_time.compareTo(right_now) < 0){
			return true;
		}
		return false;
		
	}
	
	/*
	 * getNow
	 * 	Determines the current time (String time) and the time twelve hours from now (String next_twelve_hours)
	 */
	public void getNow(){
		today = new Time(Time.getCurrentTimezone());
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
		int hour = today.hour;
		String h;
		if (hour < 10){
			h = "0" + hour;
		} else {
			h = "" + hour;
		}
		int minute = today.minute;
		String min;
		if (minute < 10){
			min = "0" + minute;
		} else {
			min = "" + minute;
		}
		time = today.year + "-" + m + "-" + d + " " + h + ":" + min + ":" + "00";
		String d2 = today.year + "-" + m + "-" + d + " ";
		String t = h + ":" + min + ":" + "00";
		next_twelve_hours = plusNHours(d2,t,12);
	}

	/*
	 * When an info window is clicked, creates a dialog displaying more information about the event 
	 */
	@Override
	public void onInfoWindowClick(Marker marker) {
		String title = marker.getTitle();
		final int position = added_events.get(title);
		 
		final Dialog dialog = new Dialog(this);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.item_full_info);
			
		TextView title_box = (TextView) dialog.findViewById(R.id.info_title);
		title_box.setText(list.get(position).get("title"));

		TextView date = (TextView) dialog.findViewById(R.id.info_date);
		date.setText(toHumanDate(list.get(position).get("date")));
		
		TextView time = (TextView) dialog.findViewById(R.id.info_time);
		String start_end_time = toHumanTime(list.get(position).get("time"));
		String end_time = toHumanTime(list.get(position).get("end_time"));
		if (! end_time.equals("null")){
			start_end_time += " - " + end_time;
		}
		if (! start_end_time.equals("null")){
			time.setText(start_end_time);
		}
		
		TextView description = (TextView) dialog.findViewById(R.id.info_description);
		description.setMovementMethod(new ScrollingMovementMethod());
		description.setText(list.get(position).get("text"));
		
		TextView poster = (TextView) dialog.findViewById(R.id.info_poster);
		poster.setText(list.get(position).get("sponsor"));
		
		TextView contact = (TextView) dialog.findViewById(R.id.info_contact);
		contact.setText(list.get(position).get("poster_email"));
		
		TextView url = (TextView) dialog.findViewById(R.id.info_url);
		String url_text = list.get(position).get("sponsor_url");
		if (! url_text.equals("null")){
			url.setText(url_text);
		}
		
		TextView place = (TextView) dialog.findViewById(R.id.info_place);
		String place_text = list.get(position).get("place");
		place.setText(place_text);
		
		Button add = (Button) dialog.findViewById(R.id.info_add);
		add.setOnClickListener(new OnClickListener() {
           @Override
           public void onClick(View view) {
        	   if (!pos.contains(list.get(position).get("title"))){
        	   pos.add(list.get(position).get("title"));
        	   Toast.makeText(getApplicationContext(), "Adding event to your calendar", Toast.LENGTH_SHORT).show();
        	   }
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

}
