package srv.btp.eticket.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import srv.btp.eticket.FormObjectTransfer;
import srv.btp.eticket.R;
import srv.btp.eticket.crud.CRUD_Route_Back_Table;
import srv.btp.eticket.crud.CRUD_Route_Table;
import srv.btp.eticket.crud.Datafield_Route;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class ServerDatabaseService extends AsyncTask<String, String, Void> {
	/***
	 * ServerDatabaseService.java
	 * 
	 * Kelas ini dibuat untuk membaca data dari URL Service. 
	 * Pemanggilannya cukup mudah, cukup call syntax dan kemudian JSON 
	 * akan diambil.
	 * 
	 * Kelas ini akan mengambil dokumen daftar rute dan daftar rute balik.
	 * 
	 * Info menyusul
	 */
	//Variable Lists
	String URLService = PreferenceManager.getDefaultSharedPreferences(
			FormObjectTransfer.main_activity.getApplicationContext())
			.getString("service_address",
					FormObjectTransfer.main_activity.getResources().getString(
							R.string.default_service));
	
	
	private ProgressDialog progressDialog = new ProgressDialog(
			(Activity) FormObjectTransfer.main_activity);
	InputStream inputStream = null;
	String result;
	CRUD_Route_Table route = new CRUD_Route_Table(FormObjectTransfer.main_activity.getApplicationContext());
	CRUD_Route_Back_Table route_back = new CRUD_Route_Back_Table(FormObjectTransfer.main_activity.getApplicationContext());
	
	boolean isReversed = false;
	boolean isVersionUptoDate = false;
	boolean isPricing = false;
	
	
	//FINALS
	public static final String FIELD_ID = "ID";
	public static final String FIELD_NAMA = "nama_lokasi";
	public static final String FIELD_LAT = "latitude";
	public static final String FIELD_LONG = "longitude";
	
	public static final String FIELD_ID_SRC = "ID_lokasi_asal";
	public static final String FIELD_ID_DST = "ID_lokasi_tujuan";
	public static final String FIELD_PRICE = "harga";
	public static final String FIELD_URUTAN_TRAYEK = "urutan_trayek";
	
	
	public static final String URL_SERVICE_FORWARD = "lokasi?1";
	public static final String URL_SERVICE_REVERSE = "lokasi?2";
	
	public static int TRAJECTORY_LOCATION = 0; //variably changes
	public static final String URL_SERVICE_PRICE_FORWARD = "harga_lokasi_trayek/";
	public static final String URL_SERVICE_PRICE_REVERSE = "harga_lokasi_trayek/";
	public static final String URL_SERVICE_VERSION_CHECK = "t_version";
	
	public static final String URL_SERVICE_TRAJECTORY = "trayek";
	
	public static final int MAXIMUM_WAITING_TIME = 180000;
	
	public static final int CHECK_STATE_VERSION = 1;
	public static final int CHECK_ROUTE_FORWARD = 2;
	public static final int CHECK_ROUTE_REVERSE = 3;
	public static final int CHECK_PRICE_FORWARD = 4;
	public static final int CHECK_PRICE_REVERSE = 5;
	public static final int CHECK_UNKNOWN = 0;
	
	public int connStatus = 0;
	public static boolean isDone;
	public static boolean isFail = false;

	public static String message = "Mendownload beberapa informasi data...";
	protected CountDownTimer ctd = new CountDownTimer(MAXIMUM_WAITING_TIME,200) {
		@Override public void onTick(long arg0) {
			Log.d("ServerTickingWaiting",arg0+ " JSON waiting time." );
			progressDialog.setMessage(message+"\nTekan tombol 'Back' untuk batal.");
		}
		
		@Override
		public void onFinish() {
			progressDialog.cancel();
		}
	};


	private boolean isGetDataFailed;
	
	protected void onPreExecute() {
		isDone = false;
		
		progressDialog.setMessage(message);
		progressDialog.setCanceledOnTouchOutside(false);
		progressDialog.setCancelable(true);
		progressDialog.show();
		
		//Initializer value
		ctd.start();
		
		progressDialog.setOnCancelListener(new OnCancelListener() {
			@Override public void onCancel(DialogInterface arg0) {
				Toast.makeText(FormObjectTransfer.main_activity.getBaseContext(), 
						"Proses dibatalkan. Menggunakan data dari yang ada.", Toast.LENGTH_SHORT).show();
				progressDialog.dismiss();
				isDone = true;
				cancel(true);
				ctd.cancel();
				
			}
		});
	}

	@Override
	protected Void doInBackground(String... params) {
		for (String parameter : params) {
			String url_select = URLService + parameter;
			Log.e("URLService", url_select);
			// STATE detection
			
			if (parameter.equals(URL_SERVICE_FORWARD)) {
				connStatus = CHECK_ROUTE_FORWARD;
				message="Mendownload informasi trayek...";
				// progressDialog.setMessage("Mendownload informasi trayek...");
				
			} else if (parameter.equals(URL_SERVICE_REVERSE)) {
				connStatus = CHECK_ROUTE_REVERSE;
				message="Mendownload informasi trayek arah balik...";
				// progressDialog.setMessage("Mendownload informasi trayek arah balik...");
			} else if (parameter.equals(URL_SERVICE_VERSION_CHECK)) {
				connStatus = CHECK_STATE_VERSION;
				message="Memeriksa update versi trayek...";
				// progressDialog.setMessage("Memeriksa update versi trayek...");
				isVersionUptoDate = false; // Agar doInBackground wajib
											// melakukan download versioning.
			} else if (parameter.equals(URL_SERVICE_PRICE_FORWARD + String.valueOf(TRAJECTORY_LOCATION + 0))) {
				connStatus = CHECK_PRICE_FORWARD;
				message="Mendownload daftar harga trayek...";
				// progressDialog.setMessage("Mendownload daftar harga trayek...");
			} else if (parameter.equals(URL_SERVICE_PRICE_REVERSE + String.valueOf(TRAJECTORY_LOCATION + 1))) {
				connStatus = CHECK_PRICE_REVERSE;
				message="Mendownload daftar harga trayek arah balik...";
				// progressDialog.setMessage("Mendownload daftar harga trayek arah balik...");
			}

			//ArrayList<NameValuePair> param = new ArrayList<NameValuePair>();
			isGetDataFailed = false;
			if (!isVersionUptoDate) { // ketika versi telah terupdate, perlu
										// adanya pencegahan download.
				try {
					// Set up HTTP post
					// HttpClient is more then less deprecated. Need to change
					// to URLConnection
					 HttpClient httpclient = new DefaultHttpClient();
					 HttpGet httpget = new HttpGet(url_select);
					 HttpResponse response = httpclient.execute(httpget);
					 HttpEntity httpEntity = response.getEntity();
					
					inputStream = httpEntity.getContent();
					Log.e("DATA_LENGTH", "Length of data=" + httpEntity.getContentLength());
				} catch (UnsupportedEncodingException e1) {
					Log.e("UnsupportedEncodingException", e1.toString());
					e1.printStackTrace();
					isFail = true;
				} catch (ClientProtocolException e2) {
					Log.e("ClientProtocolException", e2.toString());
					e2.printStackTrace();
					isFail = true;
				} catch (IllegalStateException e3) {
					Log.e("IllegalStateException", e3.toString());
					e3.printStackTrace();
					isFail = true;
				} catch (IOException e4) {
					Log.e("IOException", e4.toString());
					e4.printStackTrace();
					isFail = true;
				}
				// Convert response to string using String Builder
				try {
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(inputStream, "iso-8859-1"), 65728);
					StringBuilder sb = new StringBuilder();
					message="Mengolah data...";
					// progressDialog.setMessage("Memprosesi data...");
					String line = null;

					while ((line = reader.readLine()) != null) {
						Log.d("BufferingData", "lineworks = " + line);
						sb.append(line + "\n");
					}
					inputStream.close();
					Log.d("BufferingData","Data= " + sb.toString());
					result = sb.toString();
					// result = ;

				} catch (Exception e) {
					Log.e("StringBuilding & BufferedReader",
							"Error converting result " + e.toString());
					isGetDataFailed = true;
				}

				// Langsung proses?
				Log.d("CONNECTION_STATUS", "Connection Status = "+ String.valueOf(connStatus));
				try {
					if(!isGetDataFailed){
					JSONArray jArray = new JSONArray(result);
					//Jikala entri data berhasil, maka dipersiapkan cleaning database
					//apabila isi JSON salah, maka otomatis akan terlempar langsung ke catch dan
					//data sementara akan aman tidak diubah/dibersihkan.
					
					if(connStatus == CHECK_ROUTE_FORWARD){
						route.onUpgrade(route.getWritableDatabase(), 1, 1);
					}else if(connStatus == CHECK_ROUTE_REVERSE){
						route_back.onUpgrade(
								route_back.getWritableDatabase(), 1, 1);
					}
					
					
					//Mulai prosesi 
					for (int i = 0; i < jArray.length(); i++) {
						JSONObject jObject = jArray.getJSONObject(i);
						Log.d("JOBJECT" + i +  " " + connStatus,  " " + jObject.toString());
						switch (connStatus) {
						case CHECK_STATE_VERSION:
							int originValue = Integer
									.parseInt(PreferenceManager
											.getDefaultSharedPreferences(
													FormObjectTransfer.main_activity
															.getBaseContext())
											.getString("unique_key", "0"));
							int downloadedValue = jObject.getInt("version");
							Log.d("STATE"+i, "version:"+downloadedValue + " internal:"+originValue);
							if (downloadedValue > originValue) {
								
								isVersionUptoDate = false;
								PreferenceManager.getDefaultSharedPreferences(
										FormObjectTransfer.main_activity.getBaseContext())
										.edit().putString("unique_key", String.valueOf(downloadedValue) ).commit();
							} else {
								isVersionUptoDate = true; // membuat update
															// tidak terjadi.
							}
							break;

						/*
						 * Untuk CHECK PRICE dibutuhkan sesuatu pemrosesan
						 * lanjut agar data left price sama right pricenya masuk
						 * ke database yang benar, bukan membuat tabel baru.
						 */
						case CHECK_PRICE_FORWARD:
							if (isVersionUptoDate)
								break;
							int id_src_fwd = jObject.getInt(FIELD_ID_SRC);
							int id_dst_fwd = jObject.getInt(FIELD_ID_DST);
							int price_fwd = jObject.getInt(FIELD_PRICE);
							int urutan_fwd = jObject.getInt(FIELD_URUTAN_TRAYEK);
							
							ContentValues c = new ContentValues();
							c.put(CRUD_Route_Table.KEY_LEFTPRICE, price_fwd / 2); 
							c.put(CRUD_Route_Table.KEY_ROUTE_PRIORITY, urutan_fwd);
							route.getWritableDatabase().update(
									CRUD_Route_Table.TABLE_NAME, // nama tabel
									c, // konten yang diupdate
									CRUD_Route_Table.KEY_ID + " = " + id_dst_fwd, null
									);
							
							c = new ContentValues();
							c.put(CRUD_Route_Table.KEY_RIGHTPRICE, price_fwd / 2); 
							route.getWritableDatabase().update(
									CRUD_Route_Table.TABLE_NAME, // nama tabel
									c, // konten yang diupdate
									CRUD_Route_Table.KEY_ID + " = "+ id_src_fwd, null
									);
							
							Log.d("CHECK_PRICE_FORWARD step-" + i, "host:" + id_src_fwd + " dest:"+ id_dst_fwd + " price:" + price_fwd);
							break;
						case CHECK_PRICE_REVERSE:
							if (isVersionUptoDate)
								break;
							int id_src_rev = jObject.getInt(FIELD_ID_SRC);
							int id_dst_rev = jObject.getInt(FIELD_ID_DST);
							int price_rev = jObject.getInt(FIELD_PRICE);
							int urutan_rev = jObject.getInt(FIELD_URUTAN_TRAYEK);

							ContentValues cc = new ContentValues();
							cc.put(CRUD_Route_Back_Table.KEY_LEFTPRICE,
									price_rev / 2); // insert ke id_dst
							cc.put(CRUD_Route_Back_Table.KEY_ROUTE_PRIORITY, urutan_rev);
							route_back.getWritableDatabase().update(
									CRUD_Route_Back_Table.TABLE_NAME, // nama
																		// tabel
									cc, // konten yang diupdate
									CRUD_Route_Back_Table.KEY_ID + " = " + id_dst_rev, null
									);
							
							cc = new ContentValues();
							cc.put(CRUD_Route_Back_Table.KEY_RIGHTPRICE,
									price_rev / 2); // insert ke id src
							route_back.getWritableDatabase().update(
									CRUD_Route_Back_Table.TABLE_NAME, // nama
																		// tabel
									cc, // konten yang diupdate
									CRUD_Route_Back_Table.KEY_ID + " = " + id_src_rev, null 
									);
							Log.d("CHECK_PRICE_REVERSE step-" + i, "host:" + id_src_rev + " dest:"+ id_dst_rev + " price:" + price_rev);
							break;
							
						/*
						 * Check Route adalah pengolahan JSON menjadi entri data.
						 * 
						 * 
						 */
						case CHECK_ROUTE_FORWARD:
							if (isVersionUptoDate)
								break;
							

							Log.d("JOBJECT" + i +  " " + connStatus,  " " + jObject.toString());
							
							int id_forward = jObject.getInt(FIELD_ID);
							String nama_forward = jObject.getString(FIELD_NAMA);
							double latd_forward = jObject.getDouble(FIELD_LAT);
							double longd_forward= jObject.getDouble(FIELD_LONG);
							Datafield_Route dr_forward = new Datafield_Route(
									id_forward, nama_forward, 0, 0,
									latd_forward, longd_forward,0);
							route.addEntry(dr_forward);
							
							Log.d("CHECK_ROUTE_FORWARD step-"+i,dr_forward.toString());
							break;

						case CHECK_ROUTE_REVERSE:
							if (isVersionUptoDate)
								break;

							int id_reverse = jObject.getInt(FIELD_ID);
							String nama_reverse = jObject.getString(FIELD_NAMA);
							double latd_reverse = jObject.getDouble(FIELD_LAT);
							double longd_reverse = jObject
									.getDouble(FIELD_LONG);
							Datafield_Route dr_reverse = new Datafield_Route(
									id_reverse, nama_reverse, 0, 0,
									latd_reverse, longd_reverse, 0);
							route_back.addEntry(dr_reverse);
							
							Log.d("CHECK_ROUTE_REVERSE step-"+i,dr_reverse.toString());
							break;
						}//end: switch

					} // end: for
					}
				} catch (JSONException e) {
					isFail = true;
					Log.e("JSONException", "Error: " + e.toString());

				} // end: catch (JSONException e)

			}// end: !isVersionUptoDate

		}// end: for
		return null;
	} // end: protected Void doInBackground(String... params)

	protected void onPostExecute(Void v) {
		// parse massive JSON data from result
		// Karena sukses mendownload, lakukan stop cooldown
		ctd.cancel();
		this.progressDialog.dismiss();
		isDone = true;

	} //end: protected void onPostExecute(Void v)
} //end: class MyAsyncTask extends AsyncTask<String, String, Void>