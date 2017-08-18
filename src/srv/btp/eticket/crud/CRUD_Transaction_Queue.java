package srv.btp.eticket.crud;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;

import srv.btp.eticket.services.QueueService;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class CRUD_Transaction_Queue extends SQLiteOpenHelper {
	/***
	 * DatabaseVersioning.java
	 * 
	 * Kelas ini dibuat untuk melakukan versioning check pada semua SQLite
	 * yang ada di aplikasi, menyangkut info lokasi kota, pricing, dan lainnya.
	 * 
	 * 
	 * Info menyusul
	 */


	private static final int DATABASE_VERSION = 1;
	
	public static final String DATABASE_NAME = "transaction_system";
	public static final String TABLE_NAME = "transaction";
	public static final String TEMP_TABLE = "temp_transaction";
	
	//entries obsolete
	private static final String KEY_ID = "id"; // nomor prioritas
	private static final String KEY_NAMA = "nama"; //nama kota
	private static final String KEY_LEFTPRICE = "leftprice"; //harga kiri
	private static final String KEY_RIGHTPRICE = "rightprice"; //harga kanan
	private static final String KEY_LATITUDE = "latitude"; //nomor latitude
	private static final String KEY_LONGITUDE = "longitude"; //nomor longitude
	
	public static final String ID_TRAYEK = "ID_trayek"; //id trayek
	public static final String KOTA_1 = "kota1"; //id kota asal
	public static final String KOTA_2 = "kota2"; //id kota tujuan
	public static final String LONGITUDE = "long"; //id kota tujuan
	public static final String LATITUDE = "lat"; //id kota tujuan
	public static final String JUMLAH_TIKET = "tiket"; //id kota tujuan
	public static final String DATE_TIME = "date_transaction"; //id kota tujuan
	public static final String PRICE = "harga";
	public static final String ID_BIS = "id_bis";
	
	
	public CRUD_Transaction_Queue(Context context){
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	public CRUD_Transaction_Queue(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
	}
	@Override
	public void onCreate(SQLiteDatabase db) {
		String /*SQL_CREATION = "CREATE TABLE " + TABLE_NAME + "("
				+ KEY_ID + " INTEGER," + KEY_NAMA + " TEXT," 
				+ KEY_LEFTPRICE + " INTEGER," + KEY_RIGHTPRICE + " INTEGER," 
				+ KEY_LATITUDE + " NUMBER,"
				+ KEY_LONGITUDE + " NUMBER"
				+ ")";		
		Log.v("Query ", SQL_CREATION);
		db.execSQL(SQL_CREATION);
		*/
		//create temp table for queueing
		SQL_CREATION = "CREATE TABLE " + TEMP_TABLE + "("
				+ ID_BIS + " NUMBER, "		//1
				+ ID_TRAYEK + " INTEGER, " 	//2
				+ KOTA_1 + " NUMBER, " 		//3
				+ KOTA_2 + " NUMBER, " 		//4
				+ LONGITUDE + " REAL, " 		//5
				+ LATITUDE + " REAL, "		//6
				+ JUMLAH_TIKET + " NUMBER, "//7
				+ PRICE + " NUMBER, " 		//8
				+ DATE_TIME + " TEXT "		//9

				+ ")";
		Log.v("Query ", SQL_CREATION);
		db.execSQL(SQL_CREATION);
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TEMP_TABLE);
		onCreate(db);
	}
	
	@Deprecated public void addEntry(Datafield_Route t){
		Log.d("SQLiteCRUD","Add new entry of "+ t.toString());
		SQLiteDatabase db = this.getWritableDatabase();
		
		ContentValues val = new ContentValues();
		val.put(KEY_ID, t.get_ID());
		val.put(KEY_NAMA,t.get_nama());
		val.put(KEY_LEFTPRICE, t.get_leftprice());
		val.put(KEY_RIGHTPRICE, t.get_rightprice());
		val.put(KEY_LATITUDE, t.get_latitude());
		val.put(KEY_LONGITUDE, t.get_longitude());
		//val.put(key, value)
		
		db.insert(TABLE_NAME, null, val);
		db.close();
	}
	
	public void addTempTransaction(List<NameValuePair> nameValuePairs){

		Log.d("SQLiteCRUD","Add new entry of temp transaction");
		SQLiteDatabase db = this.getWritableDatabase();
		Log.e("DATE","INSIDE tempTransaction " + ((NameValuePair)nameValuePairs.get(6)).getValue());
		ContentValues val = new ContentValues();
		val.put(ID_BIS, ((NameValuePair)nameValuePairs.get(8)).getValue());
		val.put(ID_TRAYEK, ((NameValuePair)nameValuePairs.get(0)).getValue());
		val.put(KOTA_1,((NameValuePair)nameValuePairs.get(1)).getValue());
		val.put(KOTA_2, ((NameValuePair)nameValuePairs.get(2)).getValue());
		val.put(LONGITUDE, ((NameValuePair)nameValuePairs.get(3)).getValue());
		val.put(LATITUDE, ((NameValuePair)nameValuePairs.get(4)).getValue());
		val.put(JUMLAH_TIKET, ((NameValuePair)nameValuePairs.get(5)).getValue());
		val.put(PRICE, ((NameValuePair)nameValuePairs.get(7)).getValue());
		val.put(DATE_TIME, ((NameValuePair)nameValuePairs.get(6)).getValue());

		
		db.insert(TEMP_TABLE, null, val); 
		db.close();
	}
	
	public List<ContentValues> getAllEntries() {
		Log.d("SQLiteCRUD","Get All entries");
		List<ContentValues> l = new ArrayList<ContentValues>();
		String query = "SELECT * FROM " + TEMP_TABLE;
		
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor c = db.rawQuery(query, null);
		
		//proceeed
		if(c.moveToFirst()){
			do{
				ContentValues t = new ContentValues();
				t.put(ID_BIS, c.getString(0));
				t.put(ID_TRAYEK,c.getString(1));
				t.put(KOTA_1,c.getString(2));
				t.put(KOTA_2,c.getString(3));
				t.put(LONGITUDE,c.getString(4));
				t.put(LATITUDE,c.getString(5));
				t.put(JUMLAH_TIKET,c.getString(6));
				t.put(PRICE, c.getString(7));
				t.put(DATE_TIME, c.getString(8));


				l.add(t);
			} while (c.moveToNext());
		}
		
		return l;
	}
	
	@Deprecated public Datafield_Route getEntry(int id){
		Log.d("SQLiteCRUD","Lookup entry from " + id);
		SQLiteDatabase db = this.getReadableDatabase();
		
		Cursor c = db.query(
				TABLE_NAME, 
				new String[] {KEY_ID, KEY_NAMA, KEY_LEFTPRICE, KEY_RIGHTPRICE, KEY_LATITUDE, KEY_LONGITUDE},
				KEY_ID + "=?", 
				new String[] {String.valueOf(id) },
				null,
				null,
				null, 
				null
				);
		
		if(c!= null){
			c.moveToFirst();
		}
		
		Datafield_Route t = new Datafield_Route(
				Integer.parseInt(c.getString(0)), //ID
				c.getString(1), //Nama
				Integer.parseInt(c.getString(2)), //leftprice
				Integer.parseInt(c.getString(3)), //rightprice
				Double.parseDouble(c.getString(4)),
				Double.parseDouble(c.getString(5)),
				Integer.parseInt(c.getString(6))
				);
		return t;
	}

	public int countEntries(){
		Log.d("QueueEntries","Counting Entries...");
		String query = "SELECT * FROM " + TEMP_TABLE;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor c = db.rawQuery(query, null);
		int number = c.getCount();
		c.close();
		
		return number;
	}
	
	@Deprecated public int updateEntry(Datafield_Route t){
		Log.d("SQLiteCRUD","Update Entry of "+ t.toString());
		SQLiteDatabase db = this.getWritableDatabase();
		
		ContentValues val = new ContentValues();
		val.put(KEY_ID, t.get_ID());
		val.put(KEY_NAMA,t.get_nama());
		val.put(KEY_LEFTPRICE, t.get_leftprice());
		val.put(KEY_RIGHTPRICE, t.get_rightprice());
		val.put(KEY_LATITUDE, t.get_latitude());
		val.put(KEY_LONGITUDE, t.get_longitude());
		
		
		return db.update(TABLE_NAME, val, KEY_ID + " = ?",
				new String[]{ String.valueOf(t.get_ID() ) }
		);
	}
	
	public void deleteEntry(int t){
		Log.d("SQLiteCRUD","Delete entry of "+ t);
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TEMP_TABLE, KEY_ID + " = ?", 
				new String[] { String.valueOf(t)
				});
		db.close();
		}
	public static void SyncData(Context context){
		// TODO:prosesi data yang ada di tabel
		// ingat, data setelah dikirim harus di CLEAR! XD
		CRUD_Transaction_Queue queue = new CRUD_Transaction_Queue(
				context);
		if(queue.countEntries()==0)return;
		List<ContentValues> tmpData = queue.getAllEntries();
		// CLEAR!
		queue.onUpgrade(queue.getWritableDatabase(), 1, 1);
		// buat data
		for (ContentValues c : tmpData) {
			Log.e("DATE",c.getAsString(queue.DATE_TIME));
			
			// Loop isi tmpData lalu dibuatkan String[] untuk di
			// queueService.execute()
			String[] data = new String[9];
			data[0] = c.getAsString(queue.ID_BIS);
			data[1] = c.getAsString(queue.ID_TRAYEK);
			data[2] = c.getAsString(queue.KOTA_1);
			data[3] = c.getAsString(queue.KOTA_2);
			data[4] = c.getAsString(queue.LONGITUDE);
			data[5] = c.getAsString(queue.LATITUDE);
			data[6] = c.getAsString(queue.JUMLAH_TIKET);
			data[7] = c.getAsString(queue.PRICE);
			data[8] = c.getAsString(queue.DATE_TIME);

			
			// TODO: Post-data
			QueueService queueService = new QueueService();
			queueService.execute(data);
		}
	}
	
}

