package srv.btp.eticket.services;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import srv.btp.eticket.FormObjectTransfer;
import srv.btp.eticket.R;
import srv.btp.eticket.crud.CRUD_Route_Table;
import srv.btp.eticket.crud.CRUD_Transaction_Queue;
import srv.btp.eticket.crud.Datafield_Route;
import android.annotation.SuppressLint;
import android.preference.PreferenceManager;
import android.util.Log;
import android.os.AsyncTask;

public class QueueService extends AsyncTask<String, Integer, Boolean> {

	@Override
	protected Boolean doInBackground(String... values) {
		if (values.length != 9) {
			Log.v("async : status", "404");
			return Boolean.FALSE;
		} else {
			String id_trayek, dateTime;
			id_trayek = dateTime = null;

			String kota1, kota2;
			int ticket_num, harga;
			int id_bus = 0;
			harga = 0;

			if (values[1] != null){ //ID Trayek
				id_trayek = values[1];
			}else{
				String arah = PreferenceManager.getDefaultSharedPreferences(
						FormObjectTransfer.main_activity.getBaseContext())
						.getString("trajectory_direction", "maju");
				int rt = Integer.parseInt(
						PreferenceManager.getDefaultSharedPreferences(
								FormObjectTransfer.main_activity.getBaseContext())
								.getString("route_list","-1"));
				if(!arah.equals("maju"))rt+=1;
				id_trayek = String.valueOf(rt);
			}
			kota1 = values[2]; //kota1
			kota2 = values[3]; //kota2
			ticket_num = Integer.parseInt(values[6]); //tiket
			float latitude, longitude; 
			if (values[4] != null) { //longitude
				longitude = Float.parseFloat(values[4]);
			} else {
				longitude = PreferenceManager.getDefaultSharedPreferences(
						FormObjectTransfer.main_activity.getBaseContext())
						.getFloat("long", 0);
			}

			if (values[5] != null) { //latitude
				latitude = Float.parseFloat(values[5]);
			} else {
				latitude = PreferenceManager.getDefaultSharedPreferences(
						FormObjectTransfer.main_activity.getBaseContext())
						.getFloat("lat", 0);
			}

			if (values[8] != null) { //date
				dateTime = values[8];
				Log.e("DATE","INSIDE QUEUESERVICE "+dateTime);
			}

			if (values[7] != null) { //price
				harga = Integer.parseInt(values[7]);
			}
			if (values[0] != null) { //id_bus
				id_bus = Integer.parseInt(values[0]);
			}
			int status = postData(id_trayek, kota1, kota2, longitude, latitude,
					ticket_num, dateTime, harga, id_bus);
			Log.v("async : status", status + "");
			if (status == 200)
				return true;
			else
				return false;
		}
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		Log.v("async : progress status", values[0].toString());
	};

	@Override
	protected void onPostExecute(Boolean result) {
		Log.v("async : result", result.toString());
	};

	@SuppressLint("DefaultLocale")
	private int postData(String id_trayek, String kota1, String kota2,
			float longitude, float latitude, int jumlah_tiket, String date,
			int harga, int id_bis) {
		// Create a new HttpClient and Post Header
		String jumlah_tiket_copy = jumlah_tiket + "";
		// String harga_copy = harga + "";
		// String total_transaksi_copy =
		// total_transaksi + "";

		String timeNow = date;
		if (date == null) {
			Calendar cal = Calendar.getInstance();
			SimpleDateFormat format1 = new SimpleDateFormat(
					"yy-MM-dd HH-mm-ss", Locale.getDefault());
			timeNow = format1.format(cal.getTime());
			
		}
		Log.e("DATE","INSIDE POSTDATA"+timeNow);
		String URLService = PreferenceManager.getDefaultSharedPreferences(
				FormObjectTransfer.main_activity.getApplicationContext())
				.getString(
						"service_address",
						FormObjectTransfer.main_activity.getResources()
								.getString(R.string.default_service));
		String table_name = "transaksi";
		String target_post = URLService + table_name;
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(target_post);
		int code = -1;
		List<NameValuePair> nameValuePairs = null;

		try {
			// Add your data
			nameValuePairs = new ArrayList<NameValuePair>(2);

			// Mendapatkan daftar id Kota
			CRUD_Route_Table routeTable = new CRUD_Route_Table(
					FormObjectTransfer.main_activity.getBaseContext());
			List<Datafield_Route> dataList = routeTable.getAllEntries();
			FormObjectTransfer.idKota1 = FormObjectTransfer.idKota2 = -1;
			for (Iterator<Datafield_Route> iterator = dataList.iterator(); iterator
					.hasNext();) {
				Datafield_Route datafield_Route = iterator.next();
				Log.v("async : iterator", iterator.toString());
				String debug = String.format("%s %d",
						datafield_Route.get_nama(), datafield_Route.get_ID());
				Log.v("async : data", debug);
				if (!isInteger(kota1)) {
					if (datafield_Route.get_nama().equals(kota1)) {
						FormObjectTransfer.idKota1 = (int) datafield_Route
								.get_ID();
					}
				} else {
					FormObjectTransfer.idKota1 = Integer.parseInt(kota1);
				}

				if (!isInteger(kota2)) {
					if (datafield_Route.get_nama().equals(kota2)) {
						FormObjectTransfer.idKota2 = (int) datafield_Route
								.get_ID();
					}
				} else {
					FormObjectTransfer.idKota2 = Integer.parseInt(kota2);
				}
				if (FormObjectTransfer.idKota1 != -1
						&& FormObjectTransfer.idKota2 != -1) {
					Log.v("Async : idkota result", FormObjectTransfer.idKota1
							+ " " + FormObjectTransfer.idKota2);
					break;
				}
			}
			// end:daftar kota
			int baseTrajectoryValue = 0;
			int baseRouteValue = Integer.parseInt(PreferenceManager
					.getDefaultSharedPreferences(
							FormObjectTransfer.main_activity
									.getApplicationContext()).getString(
							"route_list", "1"));
			if (PreferenceManager
					.getDefaultSharedPreferences(
							FormObjectTransfer.main_activity
									.getApplicationContext())
					.getString("trajectory_direction", "maju").equals("maju")) {
				baseTrajectoryValue = baseRouteValue;
			} else {
				baseTrajectoryValue = baseRouteValue + 1;
			}
			nameValuePairs.add(new BasicNameValuePair(
			// TODO:
			// key 0 harus punya
			// value
			// ID_trayek
					"1", baseTrajectoryValue + ""));
			nameValuePairs.add(new BasicNameValuePair("2",
					FormObjectTransfer.idKota1 + ""));
			nameValuePairs.add(new BasicNameValuePair("3",
					FormObjectTransfer.idKota2 + ""));
			nameValuePairs.add(new BasicNameValuePair("4", longitude + ""));
			nameValuePairs.add(new BasicNameValuePair("5", latitude + ""));
			nameValuePairs.add(new BasicNameValuePair("6", jumlah_tiket_copy));
			nameValuePairs.add(new BasicNameValuePair("8", timeNow));
			nameValuePairs.add(new BasicNameValuePair("7", harga + ""));
			nameValuePairs.add(new BasicNameValuePair("0", id_bis + ""));

			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			Log.v("async : debug", 1 + "");
			Log.v("async : debug", FormObjectTransfer.idKota1 + "");
			Log.v("async : debug", FormObjectTransfer.idKota2 + "");
			Log.v("async : debug", jumlah_tiket_copy);
			Log.v("async : debug", "" + timeNow);
			Log.v("async : target", target_post);

			// Execute HTTP Post Request
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();
			String responseString = EntityUtils.toString(entity, "iso-8859-1");
			Log.v("async : response string", responseString);

			code = response.getStatusLine().getStatusCode();
			return code;
		} catch (ClientProtocolException e) {
			Log.d("error async query client protocol", e.getMessage());
			sqliteBackup(nameValuePairs);
		} catch (IOException e) {
			Log.d("error async query io exception", e.getMessage());
			Log.d("error async server URL", target_post);
			sqliteBackup(nameValuePairs);
		}
		return code;
	}

	private boolean isInteger(String testString) {
		try {
			Integer.parseInt(testString);
			return true;
		} catch (NumberFormatException nfe) {
			Log.v("integer test : ", "not valid integer");
			return false;
		}

	}

	private void sqliteBackup(List<NameValuePair> nameValuePairs) {
		CRUD_Transaction_Queue transactionQueue = new CRUD_Transaction_Queue(
				FormObjectTransfer.current_activity.getApplicationContext());
		try {
			transactionQueue.addTempTransaction(nameValuePairs);
			Log.v("backup", "Add temp transaction success");
		} catch (Exception e) {
			Log.e("backup", "Add temp transaction FAILED");
			e.printStackTrace();
		}
	}
}
