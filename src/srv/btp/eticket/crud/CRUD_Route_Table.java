package srv.btp.eticket.crud;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class CRUD_Route_Table extends SQLiteOpenHelper {
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
	public static final String DATABASE_NAME = "route_table";
	public static final String TABLE_NAME = "route_forward";
	
	//entries
	public static final String KEY_ID = "id"; // nomor prioritas
	public static final String KEY_NAMA = "nama"; //nama kota
	public static final String KEY_LEFTPRICE = "leftprice"; //harga kiri
	public static final String KEY_RIGHTPRICE = "rightprice"; //harga kanan
	public static final String KEY_LATITUDE = "latitude"; //nomor latitude
	public static final String KEY_LONGITUDE = "longitude"; //nomor longitude
	public static final String KEY_ROUTE_PRIORITY = "urutan_trayek";
	
	
	public CRUD_Route_Table(Context context){
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	public CRUD_Route_Table(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
	}
	@Override
	public void onCreate(SQLiteDatabase db) {
		String SQL_CREATION = "CREATE TABLE " + TABLE_NAME + "("
				+ KEY_ID + " INTEGER," + KEY_NAMA + " TEXT," 
				+ KEY_LEFTPRICE + " INTEGER," + KEY_RIGHTPRICE + " INTEGER," 
				+ KEY_LATITUDE + " NUMBER,"
				+ KEY_LONGITUDE + " NUMBER,"
				+ KEY_ROUTE_PRIORITY + " NUMBER"
				+ ")";
		db.execSQL(SQL_CREATION);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
		onCreate(db);
	}
	
	public void addEntry(Datafield_Route t){
		Log.d("SQLiteCRUD","Add new entry of "+ t.toString());
		SQLiteDatabase db = this.getWritableDatabase();
		
		ContentValues val = new ContentValues();
		val.put(KEY_ID, t.get_ID());
		val.put(KEY_NAMA,t.get_nama());
		val.put(KEY_LEFTPRICE, t.get_leftprice());
		val.put(KEY_RIGHTPRICE, t.get_rightprice());
		val.put(KEY_LATITUDE, t.get_latitude());
		val.put(KEY_LONGITUDE, t.get_longitude());
		val.put(KEY_ROUTE_PRIORITY, t.get_urutan_lokasi());
		
		db.insert(TABLE_NAME, null, val);
		db.close();
	}
	
	public List<Datafield_Route> getAllEntries() {
		Log.d("SQLiteCRUD","Get All entries");
		List<Datafield_Route> l = new ArrayList<Datafield_Route>();
		String query = "SELECT * FROM " + TABLE_NAME + " ORDER BY " + KEY_ROUTE_PRIORITY;
		
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor c = db.rawQuery(query, null);
		
		//proceeed
		if(c.moveToFirst()){
			do{
				Datafield_Route t = new Datafield_Route();
				t.set_ID(Integer.parseInt(c.getString(0)));
				t.set_nama(c.getString(1));
				t.set_leftprice(Integer.parseInt(c.getString(2)));
				t.set_rightprice(Integer.parseInt(c.getString(3)));
				t.set_latitude(Double.parseDouble(c.getString(4)));
				t.set_longitude(Double.parseDouble(c.getString(5)));
				t.set_urutan_lokasi(Integer.parseInt(c.getString(6)));
				
				l.add(t);
			} while (c.moveToNext());
		}
		
		return l;
	}
	
	public Datafield_Route getEntry(int id){
		Log.d("SQLiteCRUD","Lookup entry from " + id);
		SQLiteDatabase db = this.getReadableDatabase();
		
		Cursor c = db.query(
				TABLE_NAME, 
				new String[] {KEY_ID, KEY_NAMA, KEY_LEFTPRICE, KEY_RIGHTPRICE, KEY_LATITUDE, KEY_LONGITUDE,KEY_ROUTE_PRIORITY},
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
		Log.d("SQLiteCRUD","Counting Entries...");
		String query = "SELECT * FROM " + TABLE_NAME;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor c = db.rawQuery(query, null);
		int count = c.getCount();
		c.close();
		
		return count;
	}
	
	public int updateEntry(Datafield_Route t){
		Log.d("SQLiteCRUD","Update Entry of "+ t.toString());
		SQLiteDatabase db = this.getWritableDatabase();
		
		ContentValues val = new ContentValues();
		val.put(KEY_ID, t.get_ID());
		val.put(KEY_NAMA,t.get_nama());
		val.put(KEY_LEFTPRICE, t.get_leftprice());
		val.put(KEY_RIGHTPRICE, t.get_rightprice());
		val.put(KEY_LATITUDE, t.get_latitude());
		val.put(KEY_LONGITUDE, t.get_longitude());
		val.put(KEY_ROUTE_PRIORITY, t.get_urutan_lokasi());
		
		
		return db.update(TABLE_NAME, val, KEY_ID + " = ?",
				new String[]{ String.valueOf(t.get_ID() ) }
		);
	}
	
	public void deleteEntry(Datafield_Route t){
		Log.d("SQLiteCRUD","Delete entry of "+ t.toString());
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_NAME, KEY_ID + " = ?", 
				new String[] { String.valueOf(t.get_ID())
				});
		db.close();
		}
	
}

