package srv.btp.eticket;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import srv.btp.eticket.crud.CRUD_Route_Back_Table;
import srv.btp.eticket.crud.CRUD_Route_Table;
import srv.btp.eticket.services.BluetoothPrintService;
import srv.btp.eticket.services.BusIdentifierService;
import srv.btp.eticket.services.RouteService;
import srv.btp.eticket.services.ServerDatabaseService;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.webkit.WebView.FindListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class AppPreferences extends PreferenceActivity {
	/**
	 * Determines whether to always show the simplified settings UI, where
	 * settings are presented in a single list. When false, settings are shown
	 * as a master/detail two-pane view on tablets. When true, a single pane is
	 * shown on tablets.
	 */
	private static final boolean ALWAYS_SIMPLE_PREFS = true;
	
	private CharSequence[] btListAddress;
	private CharSequence[] btListNames;
	private ListPreference bluetoothList;

	private ListPreference route_list;
	private ListPreference plat_bis = (ListPreference) findPreference("plat_bis");
	private CharSequence[] routeListCode;
	private CharSequence[] routeListName;
	
	private BluetoothPrintService btx;

	private RouteService rd;
	
	protected static boolean isRouteClicked = false;
	protected static boolean isRestart = false;
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// Must Have
		super.onCreate(savedInstanceState);
        FormObjectTransfer.current_activity = this;
		overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
		plat_bis = (ListPreference) findPreference("plat_bis");
		setupSimplePreferencesScreen();

		// pemeriksaan asal dari intent
		try {
			if (this.getIntent().getBooleanExtra("fromParent", false)) {
				findPreference("pref_quit").setEnabled(true);
			} else {
				/*
				 * findPreference("pref_quit").setEnabled(false);
				 * findPreference("pref_quit").setSelectable(false);
				 * findPreference("pref_quit").setTitle("");
				 * findPreference("pref_quit").setSummary("");
				 */

			}
		} catch (NullPointerException e) {
			findPreference("pref_quit").setEnabled(false);
			findPreference("pref_quit").setSelectable(false);
			findPreference("pref_quit").setTitle("");
			findPreference("pref_quit").setSummary("");
		}

		//registrasi plat_bis
		
		//registrasi klik Route
		findPreference("route_list").setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference arg0) {
				isRouteClicked = true;
				return false;
			}
		});
		
		// Preference for Bluetooth List
		bluetoothList = (ListPreference) findPreference("bluetooth_list");
		btx = new BluetoothPrintService(this, null);
		if (btx.FindPrinters() == 0) {
			Log.d("BTX", btx.toString() + " address access : "
					+ btx.getBtAddr().size());
			btListAddress = new CharSequence[btx.getBtAddr().size()];
			btListNames = new CharSequence[btx.getBtAddr().size()];
			Log.d("BTX", btListAddress.toString() + " " + btListNames
					+ " access address");
			int a = 0;
			for (String s : btx.getBtAddr()) {
				// Log.d("BTX",s + " get address");
				btListNames[a] = s.substring(0, s.indexOf("|"));
				btListAddress[a] = s.substring(s.indexOf("|") + 1, s.length());
				Log.d("BTXData", btListAddress[a] + " ... " + btListNames[a]);
				a++;
			}
			bluetoothList.setEntries(btListNames);
			bluetoothList.setEntryValues(btListAddress);
			Log.d("BTX", bluetoothList.toString() + " post address");
			bluetoothList
					.setOnPreferenceClickListener(new OnPreferenceClickListener() {
						@Override
						public boolean onPreferenceClick(Preference arg0) {
							// debug
							for (int aa = 0; aa < btListNames.length; aa++) {
								Log.d("PrefBTXData",
										bluetoothList.getEntries()[aa]
												+ " = "
												+ bluetoothList
														.getEntryValues()[aa]);
							}
							bluetoothList.setEntries(btListNames);
							bluetoothList.setEntryValues(btListAddress);
							return false;
						}
					});

		} else {
			bluetoothList.setEnabled(false);
		}

		// setting nilai summary-summary
		findPreference("input_password").setSummary("****");
		CallPassword();

		// Preference for Route List
		route_list = (ListPreference) findPreference("route_list");
		// Ambil Rute
		rd = new RouteService();
		String URL_LIST_SERVICE[] = { RouteService.URL_SERVICE_TRAJECTORY };
		Timer td = new Timer(true);
		td.schedule(TaskUpdate, Calendar.getInstance().getTime(), 1000);
		try {
			rd.execute(URL_LIST_SERVICE);
		} catch (Exception e) {
			td.cancel();
			route_list.setEnabled(false);
		}
		
		
	}
	protected TimerTask TaskUpdate = new TimerTask() {
		@Override
		public void run() {
			if(RouteService.isDone){
				this.cancel();
				FormObjectTransfer.current_activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if(RouteService.isFail){
							Toast.makeText(FormObjectTransfer.current_activity.getBaseContext(), "Ada masalah pada jaringan. Rute untuk sementara tidak dapat diganti", Toast.LENGTH_LONG).show();
							route_list.setEnabled(false);
						}else if(RouteService.isDone){
							route_list.setEnabled(true);
							route_list.setEntries(FormObjectTransfer.routeName);
							route_list.setEntryValues(FormObjectTransfer.routeID);
						}

					}
				});
			}
		}
	};

	TimerTask Task = new TimerTask() {
		@Override
		public void run() {
			plat_bis = (ListPreference) findPreference("plat_bis");
			if(BusIdentifierService.isDone){
				this.cancel();
				FormObjectTransfer.current_activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if(BusIdentifierService.isFail){
							Toast.makeText(FormObjectTransfer.current_activity.getBaseContext(), "Ada masalah pada jaringan. Tidak dapat mengganti plat bus.", Toast.LENGTH_LONG).show();
							plat_bis.setEnabled(false);
						}else if(BusIdentifierService.isDone){
							plat_bis.setEnabled(true);
							try{
								plat_bis.setEntries(bus.getCharSequenceFromArray(bus.FIELD_PLAT_NO));
								plat_bis.setEntryValues(bus.getCharSequenceFromArray(bus.FIELD_ID));
								plat_bis.setSummary(bus.getCharSequenceFromArray(bus.FIELD_PLAT_NO)[Integer.parseInt(
								                                                                    PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("plat_bis", "0")
								                                                                    )-1]
								                                                                    		);
							PreferenceManager.getDefaultSharedPreferences(
									plat_bis.getContext())
									.edit()
									.putString("plat_bis_hidden", plat_bis.getSummary()+"" )
									.commit();
							}
							catch(Exception e){
								e.printStackTrace();
								plat_bis.setEnabled(false);
								route_list.setEnabled(false);
							}
						}

					}
				});
			}
		}
	};

	protected BusIdentifierService bus;


	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
		//LEMPAR SEMUA VALUE! >_<
	}
	/**
	 * Shows the simplified settings UI if the device configuration if the
	 * device configuration dictates that a simplified, single-pane UI should be
	 * shown.
	 */
	@SuppressWarnings("deprecation")
	private void setupSimplePreferencesScreen() {
		if (!isSimplePreferences(this)) {
			return;
		}

		// In the simplified UI, fragments are not used at all and we instead
		// use the older PreferenceActivity APIs.

		// Add 'general' preferences.
		addPreferencesFromResource(R.xml.pref_general);

		// Bind the summaries of EditText/List/Dialog/Ringtone preferences to
		// their values. When their values change, their summaries are updated
		// to reflect the new value, per the Android Design guidelines.
		bindPreferenceSummaryToValue(findPreference("plat_bis"));
		bindPreferenceSummaryToValue(findPreference("unique_key"));
		bindPreferenceSummaryToValue(findPreference("bluetooth_list"));
		bindPreferenceSummaryToValue(findPreference("input_password"));
		bindPreferenceSummaryToValue(findPreference("route_list"));
		
		
		//Tambahan fitur keluar
		Preference p = findPreference("pref_quit");
		p.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference arg0) {
				CallExit();
				return false;
			}
		});
		Preference pp = findPreference("pref_about");
		pp.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference arg0) {
				CallAbout();
				return false;
			}
		});
		
		Preference prefRestart = findPreference("pref_restart");
		prefRestart.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference arg0) {
				isRestart = true;
				CallExit();
				return false;
			}
		});
	}



//////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////
//HARDCODED
//////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////
	
//!region hardcoded
	/** {@inheritDoc} */
	@Override
	public boolean onIsMultiPane() {
		return isXLargeTablet(this) && !isSimplePreferences(this);
	}

	/**
	 * Helper method to determine if the device has an extra-large screen. For
	 * example, 10" tablets are extra-large.
	 */
	private static boolean isXLargeTablet(Context context) {
		return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
	}

	/**
	 * Determines whether the simplified settings UI should be shown. This is
	 * true if this is forced via {@link #ALWAYS_SIMPLE_PREFS}, or the device
	 * doesn't have newer APIs like {@link PreferenceFragment}, or the device
	 * doesn't have an extra-large screen. In these cases, a single-pane
	 * "simplified" settings UI should be shown.
	 */
	private static boolean isSimplePreferences(Context context) {
		return ALWAYS_SIMPLE_PREFS;
	}

	/**
	 * A preference value change listener that updates the preference's summary
	 * to reflect its new value.
	 */
	private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object value) {
			String stringValue = value.toString();

			if (preference instanceof ListPreference) {
				// For list preferences, look up the correct display value in
				// the preference's 'entries' list.
				ListPreference listPreference = (ListPreference) preference;
				//if(!preference.getKey().equals("plat_bis")){
					int index = listPreference.findIndexOfValue(stringValue);
					// Set the summary to reflect the new value.
					preference
						.setSummary(index >= 0 ? listPreference.getEntries()[index]
								: null);
				//}else{
					
				//}
				if(preference.getKey().equals("route_list") && isRouteClicked == true){
					Log.e("UNIQUE_FORCE_CHANGE","CHANGE to 0");
					PreferenceManager.getDefaultSharedPreferences(
							FormObjectTransfer.current_activity.getApplicationContext())
							.edit()
							.putString("unique_key", "0")
							.commit();
					
					CRUD_Route_Table e = new CRUD_Route_Table(FormObjectTransfer.current_activity.getApplicationContext());
					e.onUpgrade(e.getWritableDatabase(), 1, 1);
					CRUD_Route_Back_Table ed = new CRUD_Route_Back_Table(FormObjectTransfer.current_activity.getApplicationContext());
					ed.onUpgrade(e.getWritableDatabase(), 1, 1);
					
					String txt = "Harap muat ulang (restart) program menggunakan menu 'Restart' untuk menerima efek perubahan.";
					Toast.makeText(FormObjectTransfer.current_activity.getApplicationContext(), txt, Toast.LENGTH_LONG).show();
				
					isRouteClicked = false;	
				}
				
				if(preference.getKey().equals("plat_bis")){
					String summary=PreferenceManager.getDefaultSharedPreferences(
							preference.getContext()).getString("plat_bis_hidden", "");
					PreferenceManager.getDefaultSharedPreferences(
								preference.getContext())
								.edit()
								.putString("plat_bis_hidden", summary+"" )
								.commit();
					}
				
			} else {
				// For all other preferences, set the summary to the value's
				// simple string representation.
				preference.setSummary(stringValue);
			}
			
			/*PreferenceManager.getDefaultSharedPreferences(
					preference.getContext())
					.edit().putString(preference.getKey(), preference.getSummary().toString() ).commit();
			*/
			
			//Value update
			/*if( preference.getTitle().equals("Alamat Layanan") || 
					preference.getTitle().equals("Versi Trayek") || 
					preference.getTitle().equals("Arah Trayek")){
				preference.setSummary(stringValue);
			}*/
			if(isRouteClicked){
				}
			return true;
		}
	};

	/**
	 * Binds a preference's summary to its value. More specifically, when the
	 * preference's value is changed, its summary (line of text below the
	 * preference title) is updated to reflect the value. The summary is also
	 * immediately updated upon calling this method. The exact display format is
	 * dependent on the type of preference.
	 * 
	 * @see #sBindPreferenceSummaryToValueListener
	 */
	private static void bindPreferenceSummaryToValue(Preference preference) {
		// Set the listener to watch for value changes.
		preference
				.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
		
		// Trigger the listener immediately with the preference's
		// current value.
		sBindPreferenceSummaryToValueListener.onPreferenceChange(
				preference,
				PreferenceManager.getDefaultSharedPreferences(
						preference.getContext()).getString(preference.getKey(),
						""));
	}
//!endregion

	private void CallPassword() {
		final String thePassword =  PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getString("input_password","1234");
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Keamanan Kontrol");
		builder.setMessage("Masukkan password hak akses: ");
		final EditText input = new EditText(AppPreferences.this);  
		final Activity thisAct = this;
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
		                        LinearLayout.LayoutParams.MATCH_PARENT,
		                        LinearLayout.LayoutParams.MATCH_PARENT);
		input.setLayoutParams(lp);
		builder.setView(input);
		builder.setCancelable(false);
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@SuppressWarnings("deprecation")
			
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				arg0.dismiss();
				if(! input.getText().toString().equals(thePassword)){
					CallPassword();
				}
				else{
					findPreference("input_password").setSummary(thePassword);
					bus  = new BusIdentifierService();
					String[] execution = {BusIdentifierService.URL_SERVICE_BUS};
					Timer td = new Timer(true);
					td.schedule(Task, Calendar.getInstance().getTime(), 1000);
					try {
						bus.execute(execution);
					} catch (Exception e) {
						td.cancel();
						plat_bis.setEnabled(false);
						route_list.setEnabled(false);
					}
					
				}
			}
		}
		);
		builder.setNegativeButton("Batal", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface di, int wat) {
				di.cancel();
				thisAct.onBackPressed();
			}
		});

		AlertDialog alert = builder.create();
		alert.setCanceledOnTouchOutside(false);
		alert.show();
	}
	
	private void CallExit(){
		String msg = "";
		AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);
        if(isRestart){
        	msg = "Muat ulang aplikasi?";
        }else{
        	msg = "Keluar dari aplikasi?";
        }
        builder.setTitle("Peringatan");
		builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setPositiveButton("Ya", 
            new DialogInterface.OnClickListener() {
			// Yes
			public void onClick(DialogInterface dialog, int id) {
				if (isRestart) {
					Intent mStartActivity = new Intent(getBaseContext(), Form_Main.class);
					int mPendingIntentId = 45556;
					PendingIntent mPendingIntent = PendingIntent.getActivity(getBaseContext(), mPendingIntentId,    mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
					AlarmManager mgr = (AlarmManager)getBaseContext().getSystemService(Context.ALARM_SERVICE);
					mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 400, mPendingIntent);
					System.exit(0);
				} else {
					System.out.println(" onClick ");

					try {
						FormObjectTransfer.bxl.READY_STATE = false;
						FormObjectTransfer.bxl.DisconnectPrinter();
						FormObjectTransfer.main_activity.finish(); // Hancurkan
																	// main
																	// terlebih
																	// dahulu
						FormObjectTransfer.bxl.sharedCountdown.cancel();
						FormObjectTransfer.main_activity.gls.StopGPS();
					} catch (NullPointerException e) {
						Log.w("EXIT_STATE",
								"Program called from launcher, not activity.");
					}
					FormObjectTransfer.isQuit = true;
					finish(); // Close Application method called
					System.exit(0);
				}

			}
		});
        builder.setNegativeButton("Tidak",
            new DialogInterface.OnClickListener() {
              	//No
                public void onClick(DialogInterface dialog,int id) {
                    dialog.cancel();
                    isRestart = false;
                }
            });

        AlertDialog alert = builder.create();
        alert.show();
	}
	
	private void CallAbout(){
		AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);
        builder.setTitle("Mobile Ticketing v.1.00");
		builder.setMessage("Dibuat oleh Immersa Labs.\n\n" +
						   "Dukungan :\n" + 
						   "The Android Open Source Project - Android API\n" +
						   "Google - Analytics dan Statistics\n" +
						   "Bixolon - Bluetooth Printing Service\n" +
						   "\n"+
						   "2013. Some rights reserved."
				
				);
        builder.setCancelable(false);
        builder.setPositiveButton("OK", 
            new DialogInterface.OnClickListener() {
        	//Yes
                public void onClick(DialogInterface dialog,int id) {
                	dialog.cancel();
                }
            });
        AlertDialog alert = builder.create();
        alert.show();
	}
}
