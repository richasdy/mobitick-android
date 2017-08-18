package srv.btp.eticket.services;

import android.annotation.SuppressLint;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class StatusBarService {
	/***
	 * StatusBarService.java
	 * 
	 * Kelas ini dibuat untuk menyimpan dan mengolah informasi sistem yang 
	 * akan ditaruh di custom statusbar, seperti informasi baterai,
	 * status sambungan bluetooth, jam, dan lainnya.
	 * 
	 * Info menyusul
	 */
	
	@SuppressLint("DefaultLocale")
	public static String GetSerializedID(int nomorTiket, String PlatBis){
		/* Menggunakan kombinasi Platbis, TimeStamp dalam HEXA, dan nomor tiket.
		 * Struktur : 6 Digit Timestap, HEX, Nomor Tiket 2 digit XoR 2 digit pertama plat bis
		 * 6 Digit Plat mentah.
		 * 
		 * Format : 
		 * - (2)notiket
		 * - (6)platbis
		 * - (6)tanggalan
		 * 
		 * Contoh : KS-3688BA-4F1A00
		*/
		//Persiapan timestamp
		Date theNow = new Date();
		Calendar c = Calendar.getInstance();
		long timestamps = 0;
		String HexedTimeStamps;
		theNow = c.getTime();
		timestamps = theNow.getTime() / 1000;
		HexedTimeStamps = Integer.toHexString((int)timestamps);
		
		//Persiapan encoder dari Platbis
		String[] encoder = PlatBis.split(" ");
		//array pertama = kode kota
		//array kedua = kode serial plat
		//array ketiga = kombinasi plat-end
		String topResult = encoder[0].toUpperCase(Locale.ENGLISH) + nomorTiket;
		String MidResult = encoder[1] + encoder[2];
		return topResult + "-" + MidResult + "-" + HexedTimeStamps.toUpperCase(Locale.ENGLISH);
	} //end:GetSerializedID
}
