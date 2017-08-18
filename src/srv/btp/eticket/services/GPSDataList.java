package srv.btp.eticket.services;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import srv.btp.eticket.FormObjectTransfer;
import srv.btp.eticket.R;
import srv.btp.eticket.crud.CRUD_Route_Back_Table;
import srv.btp.eticket.crud.CRUD_Route_Table;
import srv.btp.eticket.crud.Datafield_Route;
import android.content.ContentValues;
import android.database.sqlite.SQLiteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class GPSDataList {

	/***
	 * Sampling
	 * 
	 * {\ 0, //null rambutan 8000, //rambutan bekasi 5000, //bekasi cisarua
	 * 10000,//cisarua garut 5000, //garut sukabumi 8000, //sukabumi sumedang
	 * 8000, //sumedang tegalega 5000, //tegalega padalarang 5000, //padalarang
	 * kopo 5000, //kopo buahbatu 10000, //buahbatu cielunyi 15000, //cileunyi
	 * tasikamalaya 0, //tasikmalaya, null };
	 */

	// variabel helper
	private String[] kotaListForward;
	private int listSizeForward;
	private int hargaParsialForward[];
	private double[] lat_kota_forward;
	private double[] long_kota_forward;

	private String[] kotaListReverse;
	private int listSizeReverse;
	private int[] hargaParsialReverse;
	private double[] lat_kota_rev;
	private double[] long_kota_rev;

	// properti publik
	public String[] kotaList;
	public int listSize;
	public int hargaParsial[];
	public boolean ReverseStatus = false;

	public double[] lat_kota;
	public double[] long_kota;

	// Layanan
	ServerDatabaseService sdd;
	String ServiceBaseURL = PreferenceManager.getDefaultSharedPreferences(
			FormObjectTransfer.main_activity.getBaseContext()).getString(
			"service_address",
			FormObjectTransfer.main_activity.getResources().getString(
					R.string.default_service));

	public void SetTrack(boolean isReversed) {
		if (isReversed == false) {
			kotaList = kotaListForward;
			listSize = listSizeForward;
			hargaParsial = hargaParsialForward;
			lat_kota = lat_kota_forward;
			long_kota = long_kota_forward;
		} else {
			kotaList = kotaListReverse;
			listSize = listSizeReverse;
			hargaParsial = hargaParsialReverse;
			lat_kota = lat_kota_rev;
			long_kota = long_kota_rev;
		}
		ReverseStatus = isReversed;
	}

	public void getDataFromJSON() {
		/*
		 * Disini program akan melakukan get data 3x. Yaitu : - Cek versioning.
		 * Apabila lebih tinggi, diupdate. Apabila tidak, langsung load dari
		 * SQLite. - Download Update. Jika diupdate, maka data akan disimpan ke
		 * SQLite, jika tidak, skip. - Download trayek reverse. Jika diupdate,
		 * data reverse juga disimpan ke SQLite, jika tidak, skip.
		 * 
		 * Requires : ServerDatabaseService.java GSON library
		 */

		// session 1
		sdd = new ServerDatabaseService();
		ServerDatabaseService.TRAJECTORY_LOCATION = Integer
				.parseInt(PreferenceManager.getDefaultSharedPreferences(
						FormObjectTransfer.main_activity
								.getApplicationContext()).getString(
						"route_list", "-1"));
		String URL_LIST_SERVICE[] = {
				ServerDatabaseService.URL_SERVICE_VERSION_CHECK,
				ServerDatabaseService.URL_SERVICE_FORWARD,
				ServerDatabaseService.URL_SERVICE_REVERSE,
				ServerDatabaseService.URL_SERVICE_PRICE_FORWARD
						+ String.valueOf(ServerDatabaseService.TRAJECTORY_LOCATION + 0),
				ServerDatabaseService.URL_SERVICE_PRICE_REVERSE
						+ String.valueOf(ServerDatabaseService.TRAJECTORY_LOCATION + 1) };
		Timer td = new Timer(true);
		td.schedule(TaskUpdate, Calendar.getInstance().getTime(), 1000);
		try {
			sdd.execute(URL_LIST_SERVICE);
		} catch (Exception e) {
			td.cancel();
			generateData();
		}

	}

	protected TimerTask TaskUpdate = new TimerTask() {

		@Override
		public void run() {
			if (ServerDatabaseService.isDone) {
				this.cancel();
				FormObjectTransfer.main_activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (ServerDatabaseService.isFail)
							Toast.makeText(
									FormObjectTransfer.main_activity
											.getBaseContext(),
									"Ada masalah pada jaringan. Mengambil data trayek dari update terakhir.",
									Toast.LENGTH_LONG).show();
						try{
							FormObjectTransfer.gdl.generateData();
						}catch(Exception e){
							FormObjectTransfer.main_activity.CallError();
						}

					}
				});
			}

		}
	};

	public void generateData() {
		/*
		 * Tugas dari fungsi ini adalah menggenerate data dari SQLite ke array
		 * string. Fungsi SQLite sudah didefine di masing-masing kelas.
		 * 
		 * Setelah itu, data akan masuk ke data forward berikut reverse.
		 */

		// initialisasi
		CRUD_Route_Table crud_forward = new CRUD_Route_Table(
				FormObjectTransfer.main_activity.getBaseContext());
		CRUD_Route_Back_Table crud_reverse = new CRUD_Route_Back_Table(
				FormObjectTransfer.main_activity.getBaseContext());

		// Membersihkan data dari trayek tak terpakai
		try {
			crud_forward.getWritableDatabase().delete(
					CRUD_Route_Table.TABLE_NAME,
					CRUD_Route_Table.KEY_LEFTPRICE + " = 0 AND "
							+ CRUD_Route_Table.KEY_RIGHTPRICE + " = 0", null);
			crud_reverse.getWritableDatabase().delete(
					CRUD_Route_Back_Table.TABLE_NAME,
					CRUD_Route_Back_Table.KEY_LEFTPRICE + " = 0 AND "
							+ CRUD_Route_Back_Table.KEY_RIGHTPRICE + " = 0",
					null);
			
			//proofing data terakhir ROUTE KEY untuk kedua tabel
			ContentValues c = new ContentValues();
			int lastIndex = crud_forward.countEntries();
			c.put(CRUD_Route_Table.KEY_ROUTE_PRIORITY, lastIndex);
			crud_forward.getWritableDatabase().update(CRUD_Route_Table.TABLE_NAME, c, CRUD_Route_Table.KEY_ID+ "="+lastIndex, null);
			c = new ContentValues();
			lastIndex = crud_reverse.countEntries();
			c.put(CRUD_Route_Back_Table.KEY_ROUTE_PRIORITY, lastIndex);
			crud_reverse.getWritableDatabase().update(CRUD_Route_Back_Table.TABLE_NAME, c, CRUD_Route_Back_Table.KEY_ID+ "="+lastIndex, null);
			
		} catch (SQLiteException sqle) {

		}
		
		// prosesi data forward
		ArrayList<Datafield_Route> datafield = (ArrayList<Datafield_Route>) crud_forward
				.getAllEntries();
		int counter = 0;
		int previousrightprice = 0;
		int size = datafield.size();
		kotaListForward = new String[size];
		hargaParsialForward = new int[size + 2];
		long_kota_forward = new double[size];
		lat_kota_forward = new double[size];

		// initialisasi format data
		hargaParsialForward[0] = 0;

		for (Datafield_Route dr : datafield) {
			int numbering = counter+1;//(int) dr.get_ID();
			kotaListForward[numbering - 1] = dr.get_nama();
			if (numbering - 1 == 0) {
				hargaParsialForward[numbering - 1] = 0;
				previousrightprice = dr.get_rightprice();
			} else {
				hargaParsialForward[numbering - 1] = dr.get_leftprice()
						+ previousrightprice;
				previousrightprice = dr.get_rightprice();
			}
			long_kota_forward[numbering - 1] = dr.get_longitude();
			lat_kota_forward[numbering - 1] = dr.get_latitude();

			System.out.println(counter + " forward "
					+ hargaParsialForward[counter] + "");
			counter++;
		}
		hargaParsialForward[counter] = 0;
		listSizeForward = datafield.size();
		Log.d("TESTVALUE", kotaListForward.length + " datalength = "
				+ listSizeForward);

		// Mulai prosesi data route reverse
		ArrayList<Datafield_Route> datafield_reversed = (ArrayList<Datafield_Route>) crud_reverse
				.getAllEntries();

		int previousleftprice = 0;
		size = datafield_reversed.size();
		kotaListReverse = new String[size];
		hargaParsialReverse = new int[size + 2];
		long_kota_rev = new double[size];
		lat_kota_rev = new double[size];

		counter = size - 1;
		// initialisasi format data
		hargaParsialReverse[counter] = 0;

		for (Datafield_Route dr : datafield_reversed) {
			int numbering = counter;//size - (int) dr.get_ID();
			kotaListReverse[numbering] = dr.get_nama();
			if (numbering == size) {
				// hargaParsialReverse[numbering]=0;
				previousleftprice = dr.get_leftprice();
			} else {
				hargaParsialReverse[numbering + 1] = dr.get_rightprice()
						+ previousleftprice;
				previousleftprice = dr.get_leftprice();
			}
			long_kota_rev[numbering] = dr.get_longitude();
			lat_kota_rev[numbering] = dr.get_latitude();

			counter--;

		}

		for (int a = 0; a < hargaParsialReverse.length; a++) {
			System.out.println(a + " ref " + hargaParsialForward[a] + "");
		}
		/*
		 * //well, anti bug proceed :v int hrgTmp[] = new int[datafield.size()];
		 * for(int a=0;a<datafield.size();a++){ hrgTmp[a] =
		 * hargaParsialReverse[a]; } for(int a=datafield)
		 */

		listSizeReverse = datafield_reversed.size();
		Log.d("TESTVALUE", kotaListReverse.length + " datalength_REVERSED = "
				+ listSizeReverse);

		// PREPURRR
		String valueIntended = PreferenceManager.getDefaultSharedPreferences(
				FormObjectTransfer.main_activity.getBaseContext()).getString(
				"trajectory_direction",
				FormObjectTransfer.main_activity.getResources().getStringArray(
						R.array.direction_entry)[0]);
		if (valueIntended.equals("maju")) {
			SetTrack(false);
			Log.d("VALUE_INTENDED", "maju");
		} else {
			SetTrack(true);
			Log.d("VALUE_INTENDED", "terbalik");

		}

		FormObjectTransfer.main_activity.PrepareCityList();
	}

	public int getNearestCity(double latSrc, double longSrc) {
		double lastShortestDistance = 999999.;
		int lastNearestCity = 0;
		for (int a = 0; a < listSize; a++) {
			Log.w("getNearestCity", long_kota[a] + "," + lat_kota[a]
					+ " compared to " + longSrc + "," + latSrc);
			double dist = Distance.calculateDistanceCoordinates(longSrc,
					latSrc, long_kota[a], lat_kota[a]);
			if (dist < lastShortestDistance) {
				lastNearestCity = a + 1;
				lastShortestDistance = dist;
			}
		}
		Log.w("getNearestCity","Nearest city is : " + lastNearestCity);
		return lastNearestCity;
	}
}
