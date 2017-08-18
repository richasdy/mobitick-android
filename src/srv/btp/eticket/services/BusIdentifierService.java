package srv.btp.eticket.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

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
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.util.Log;

public class BusIdentifierService extends AsyncTask<String, String, Void> {
	/***
	 * ServerDatabaseService.java
	 * 
	 * Kelas ini dibuat untuk membaca data dari URL Service. Pemanggilannya
	 * cukup mudah, cukup call syntax dan kemudian JSON akan diambil.
	 * 
	 * Kelas ini akan mengambil dokumen daftar rute dan daftar rute balik.
	 * 
	 * Info menyusul
	 */
	// Variable Lists
	String URLService = PreferenceManager.getDefaultSharedPreferences(
			FormObjectTransfer.current_activity.getApplicationContext())
			.getString(
					"service_address",
					FormObjectTransfer.current_activity.getResources()
							.getString(R.string.default_service));

	private ProgressDialog progressDialog = new ProgressDialog(
			(Activity) FormObjectTransfer.current_activity);
	InputStream inputStream = null;
	String result;
	CRUD_Route_Table route = new CRUD_Route_Table(
			FormObjectTransfer.current_activity.getApplicationContext());
	CRUD_Route_Back_Table route_back = new CRUD_Route_Back_Table(
			FormObjectTransfer.current_activity.getApplicationContext());

	boolean isReversed = false;
	boolean isVersionUptoDate = true;
	boolean isPricing = false;
	public ArrayList<ContentValues> list = new ArrayList<ContentValues>();
	
	// FINALS
	public static final String FIELD_ID = "ID";
	public static final String FIELD_PLAT_NO = "plat_nomer";

	public static final String URL_SERVICE_BUS = "bus";

	public static final int MAXIMUM_WAITING_TIME = 180000;

	public static final int CHECK_BUS = 1;

	public static final int MAXIMUM_ARRAY_FIELD = 100;

	public int connStatus = 0;
	public static boolean isDone;
	public static boolean isFail = false;

	public static String message = "Mendownload daftar bis...";
	protected CountDownTimer ctd = new CountDownTimer(MAXIMUM_WAITING_TIME, 200) {
		@Override
		public void onTick(long arg0) {
			Log.d("RouteTickingWaiting", arg0 + " JSON waiting time.");
			progressDialog.setMessage(message
					+ "\nTekan tombol 'Back' untuk batal.");
		}
		@Override
		public void onFinish() {
			Log.e("ctd","Forced Exit");
			progressDialog.cancel();
		}
	};

	private boolean isGetDataFailed;

	protected void onPreExecute() {
		isDone = false;

		// Membersihkan FormObjectTransfer
		progressDialog.setMessage(message);
		progressDialog.setCanceledOnTouchOutside(false);
		progressDialog.setCancelable(true);
		progressDialog.show();
		ctd.start();

		progressDialog.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface arg0) {
				if(!isDone){
					isFail = true;
				}
				isDone = true;
				cancel(true);
				ctd.cancel();
				progressDialog.dismiss();
			}
		});
	}

	@Override
	protected Void doInBackground(String... params) {
		for (String parameter : params) {
			String url_select = URLService + parameter;
			Log.e("URLService", url_select);
			// STATE detection

			if (parameter.equals(URL_SERVICE_BUS)) {
				connStatus = CHECK_BUS;
				message = "Mendapatkan daftar bis...";
				isVersionUptoDate = false; // Agar doInBackground wajib
											// melakukan download versioning.
			}
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
					Log.e("DATA_LENGTH",
							"Length of data=" + httpEntity.getContentLength());
				} catch (UnsupportedEncodingException e1) {
					Log.e("UnsupportedEncodingException", e1.toString());
					e1.printStackTrace();
					isFail = true;
					isDone = true;
				} catch (ClientProtocolException e2) {
					Log.e("ClientProtocolException", e2.toString());
					e2.printStackTrace();
					isFail = true;
					isDone = true;
				} catch (IllegalStateException e3) {
					Log.e("IllegalStateException", e3.toString());
					e3.printStackTrace();
					isFail = true;
					isDone = true;
				} catch (IOException e4) {
					Log.e("IOException", e4.toString());
					e4.printStackTrace();
					isFail = true;
					isDone = true;
				}
				// Convert response to string using String Builder
				try {
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(inputStream, "iso-8859-1"),
							65728);
					StringBuilder sb = new StringBuilder();
					message = "Mengolah data...";
					// progressDialog.setMessage("Memprosesi data...");
					String line = null;

					while ((line = reader.readLine()) != null) {
						Log.d("BufferingData", "lineworks = " + line);
						sb.append(line + "\n");
					}
					inputStream.close();
					Log.d("BufferingData", "Data= " + sb.toString());
					result = sb.toString();
					// result = ;

				} catch (Exception e) {
					Log.e("StringBuilding & BufferedReader",
							"Error converting result " + e.toString());
					isGetDataFailed = true;
				}

				// Langsung proses?
				Log.d("CONNECTION_STATUS",
						"Connection Status = " + String.valueOf(connStatus));
				try {
					if (!isGetDataFailed) {
						JSONArray jArray = new JSONArray(result);
						// Jikala entri data berhasil, maka dipersiapkan
						// cleaning database
						// apabila isi JSON salah, maka otomatis akan terlempar
						// langsung ke catch dan
						// data sementara akan aman tidak diubah/dibersihkan.

						// Mulai prosesi
						for (int i = 0; i < jArray.length(); i++) {
							JSONObject jObject = jArray.getJSONObject(i);
							Log.d("JOBJECT" + i + " " + connStatus, " "
									+ jObject.toString());
							switch (connStatus) {
							case CHECK_BUS:
								ContentValues data = new ContentValues();
								data.put(FIELD_ID, jObject.getInt(FIELD_ID));
								data.put(FIELD_PLAT_NO, jObject.getString(FIELD_PLAT_NO));
								list.add(data);
								break;
							} // end: switch
						} // end:for
					} // end:i
				} catch (JSONException e) {
					isFail = true;
					isDone = true;
					Log.e("JSONException", "Error: " + e.toString());

				} // end: catch (JSONException e)

			}// end: !isVersionUptoDate

		}// end: for
		return null;
	} // end: protected Void doInBackground(String... params)

	protected void onPostExecute(Void v) {
		// parse massive JSON data from result
		// Karena sukses mendownload, lakukan stop cooldown
		isDone = true;
		isFail = false;
		ctd.cancel();
		this.progressDialog.dismiss();
		

	} // end: protected void onPostExecute(Void v)
	
	public CharSequence[] getCharSequenceFromArray(String key){
		CharSequence[] e = new CharSequence[list.size()];
		int ctr = 0;
		for(ContentValues data: list){
			e[ctr] = ""+data.get(key);
			ctr++;
		}
		
		return e;
	}
} // end: class MyAsyncTask extends AsyncTask<String, String, Void>
