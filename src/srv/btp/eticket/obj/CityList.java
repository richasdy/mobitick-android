package srv.btp.eticket.obj;

import android.graphics.Point;

/***
 * CityList.java
 * 
 * Kelas ini dibuat untuk membuat package data dari KOTA.
 * CRUD tidak dibuat disini.
 * 
 * Info menyusul
 */
public class CityList {
	public String Nama;
	public String ID_Kota;
	public int TarifKiri;
	public int TarifKanan;
	public int Priority;
	public Point coordinate;
	public boolean isNotPassed = true;
	public CityList(){
		super();
		
	}
	
}
