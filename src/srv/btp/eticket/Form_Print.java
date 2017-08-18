package srv.btp.eticket;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import srv.btp.eticket.crud.CRUD_Transaction_Queue;
import srv.btp.eticket.services.BluetoothPrintService;
import srv.btp.eticket.services.QueueService;
import srv.btp.eticket.util.SystemUiHider;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class Form_Print extends Activity {

	// !region hardcore constants
	/***
	 * Hardcoded constants
	 */
	private static final int MAX_VALUES_BOUND = 45;
	private static final int WARN_VALUES_BOUND = 8;

	// !endregion

	// !region Form Objects
	private Button button_action[][] = new Button[3][4];
	private Button button_print;
	private OnTouchListener button_touch_controls;
	private OnClickListener button_click_controls;
	private TextView txtPrintNumber;
	private TextView txtIndicator;

	private Form_Print thisForm = this;
	// !endregion

	// !region Value Calculations
	private int ticket_num = 0;

	// !endregion

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		/**
		 * Ketika program ini muncul, baris-baris disini yang akan dieksekusi
		 * pertama kali.
		 */

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_layout_print);
		overridePendingTransition(R.anim.push_up_in, R.anim.push_up_out);

		// jangan lupa selalu informasikan ke FormObjectTransfer.java
		FormObjectTransfer.current_activity = this;

		// Deklarasi variabel variabel objek
		for (int a = 0; a < 3; a++) {
			button_action[a] = new Button[4];
		}

		/***
		 * Indexing untuk button_action : [0,0] [1,0] [2,0] [0,1] [1,1] [2,1]
		 * [0,2] [1,2] [2,2] [0,3] [1,3] [2,3]
		 */

		button_action[0][0] = (Button) this.findViewById(R.id.btnOne);
		button_action[1][0] = (Button) this.findViewById(R.id.btnTwo);
		button_action[2][0] = (Button) this.findViewById(R.id.btnThree);
		button_action[0][1] = (Button) this.findViewById(R.id.btnFour);
		button_action[1][1] = (Button) this.findViewById(R.id.btnFive);
		button_action[2][1] = (Button) this.findViewById(R.id.btnSix);
		button_action[0][2] = (Button) this.findViewById(R.id.btnSeven);
		button_action[1][2] = (Button) this.findViewById(R.id.btnEight);
		button_action[2][2] = (Button) this.findViewById(R.id.btnNine);
		button_action[0][3] = (Button) this.findViewById(R.id.btnCancel);
		button_action[1][3] = (Button) this.findViewById(R.id.btnZero);
		button_action[2][3] = (Button) this.findViewById(R.id.btnBackspace);

		button_print = (Button) this.findViewById(R.id.btnPrint);

		registerOnTouchAndClick();
		for (int aa = 0; aa < 3; aa++) {
			for (int ab = 0; ab < 4; ab++) {
				button_action[aa][ab].setOnTouchListener(button_touch_controls);
				button_action[aa][ab].setOnClickListener(button_click_controls);
			}
		}

		txtPrintNumber = (TextView) findViewById(R.id.txtNumbers);
		txtPrintNumber.setText(String.valueOf(ticket_num));

		txtIndicator = (TextView) findViewById(R.id.txtIndicator);

		button_print.setOnTouchListener(button_touch_controls);
		button_print.setOnClickListener(button_click_controls);
	}

	@Override
	public void onBackPressed() {
		finish(); // go back to the previous Activity
		overridePendingTransition(R.anim.push_down_in, R.anim.push_down_out);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Trigger the initial hide() shortly after the activity has been
		// created, to briefly hint to the user that UI controls
		// are available.
		txtIndicator.setText(FormObjectTransfer.Kota1 + " > "
				+ FormObjectTransfer.Kota2 + " : Rp."
				+ FormObjectTransfer.harga);
	}

	/***
	 * Listener Section Disini berisi daftar Listener atas objek-objek Form
	 * untuk membaca inputan ato respon unit.
	 */

	@SuppressWarnings("static-access")
	private void registerOnTouchAndClick() {
		button_touch_controls = new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				/***
				 * Fungsi ini akan membuat simulasi "tombol ditekan"
				 */
				if (v == button_action[0][3]) { // Tombol "CANCEL"
					v.setBackgroundResource(R.drawable.button_digit_cancel_press);

				} else if (v == button_action[2][3]) { // Tombol "del"
					v.setBackgroundResource(R.drawable.button_digit_press);
				} else if (v == button_print) {
					v.setBackgroundResource(R.drawable.button_pressed);
				} else {
					v.setBackgroundResource(R.drawable.button_digit_press);

				}
				return false;
			};
		};
		button_click_controls = new OnClickListener() {
			@SuppressLint("DefaultLocale")
			@Override
			public void onClick(View v) {
				/***
				 * Fungsi ini akan mengembalikan warna button ke bentuk semula.
				 * serta mengeksekusi fungsi-fungsi tiap button
				 */
				if (v == button_action[0][3]) {
					v.setBackgroundResource(R.drawable.button_digit_cancel);
					onBackPressed(); // Menjalankan fungsi activity out

				} else if (v == button_action[2][3]) {
					v.setBackgroundResource(R.drawable.button_digit);

					ProceedNumber(Button2Int((Button) v));
				} else if (v == button_print) {
					v.setBackgroundResource(R.drawable.button);
					/*
					 * Di bagian sini, terjadi mekanisme : - Logging data
					 * customer - Print Data Text - dll.
					 */
					if (ticket_num > 0) {
						/*
						 * Summon Button sudah terinclude dengan print data.
						 * Namun, agar terjamin, cek Bluetooth harus dipastikan
						 * terlebih dahulu.
						 */
						if (FormObjectTransfer.bxl.BT_STATE == BluetoothPrintService.STATE_CONNECTED) {
							if (ticket_num >= WARN_VALUES_BOUND) {
								AlertDialog.Builder builder;
								builder = new AlertDialog.Builder(thisForm);
								builder.setTitle("Peringatan");
								builder.setMessage("Anda akan mencetak jumlah tiket yang cukup besar. Yakin ingin melanjutkan?\nJumlah tiket:"
										+ ticket_num);
								builder.setPositiveButton("Ya",
										new DialogInterface.OnClickListener() {
											@Override
											public void onClick(
													DialogInterface arg0,
													int arg1) {
												FormObjectTransfer.main_activity
														.SummonButton(
																FormObjectTransfer.Kota1,
																FormObjectTransfer.Kota2,
																ticket_num,
																FormObjectTransfer.harga,
																FormObjectTransfer.harga
																		* ticket_num);
												onBackPressed();

											}
										});
								builder.setNegativeButton("Tidak",
										new DialogInterface.OnClickListener() {
											@Override
											public void onClick(
													DialogInterface arg0,
													int arg1) {
												Toast.makeText(
														getApplicationContext(),
														"Dibatalkan.",
														Toast.LENGTH_SHORT)
														.show();
											}
										});
								AlertDialog alert = builder.create();
								alert.show();
							} else {
								FormObjectTransfer.main_activity.SummonButton(
										FormObjectTransfer.Kota1,
										FormObjectTransfer.Kota2, ticket_num,
										FormObjectTransfer.harga,
										FormObjectTransfer.harga * ticket_num);
								onBackPressed();
							}

							// Setelah ini harusnya dilakukan pencatatan histori
							// pembelian
							// INSERT QUERY PELAPORAN
							// !region AsyncTask Kirim Data
							QueueService asyncQuery = new QueueService();
							// eksekusi data
							Calendar cal = Calendar.getInstance();
							SimpleDateFormat format1 = new SimpleDateFormat(
									"yyyy-MM-dd HH:mm:ss", Locale.getDefault());
							String timeNow = format1.format(cal.getTime());
							Log.e("DATE",timeNow);
							String id_bis = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
									.getString("plat_bis", "-1");
							String[] execution = new String[] { 
									""+id_bis, //tanggalan//id bis
									null, //ID trayek
									(String) FormObjectTransfer.Kota1, //kota1
									(String) FormObjectTransfer.Kota2, //kota2
									null, //long
									null, //lat
									ticket_num + "", //tiketnum
									""+FormObjectTransfer.harga * ticket_num, //pricetotal
									timeNow //tanggalan
							};
							asyncQuery.execute(execution);

							//dan melakukan sync data >_<
							CRUD_Transaction_Queue.SyncData(getApplicationContext());
							// !endregion

						} else {
							FormObjectTransfer.bxl.ConnectPrinter();
							Toast.makeText(
									getApplicationContext(),
									"Bluetooth tidak sedang dalam keadaan tersambung.",
									Toast.LENGTH_SHORT).show();
						}
					} else {
						Toast.makeText(getApplicationContext(),
								"Jumlah tiket yang anda masukkan tidak benar.",
								Toast.LENGTH_SHORT).show();
					}
				} else {
					v.setBackgroundResource(R.drawable.button_digit);
					ProceedNumber(Button2Int((Button) v));
				}
			}
		};

	}

	private int Button2Int(Button b) {
		if (b.getId() == R.id.btnOne)
			return 1;
		if (b.getId() == R.id.btnTwo)
			return 2;
		if (b.getId() == R.id.btnThree)
			return 3;
		if (b.getId() == R.id.btnFour)
			return 4;
		if (b.getId() == R.id.btnFive)
			return 5;
		if (b.getId() == R.id.btnSix)
			return 6;
		if (b.getId() == R.id.btnSeven)
			return 7;
		if (b.getId() == R.id.btnEight)
			return 8;
		if (b.getId() == R.id.btnNine)
			return 9;
		if (b.getId() == R.id.btnZero)
			return 0;
		return -1;
	}

	private void ProceedNumber(int addition) {
		/***
		 * Fungsi ini bertujuan untuk memprosesi nilai input yang ditekan. Jadi
		 * contoh :
		 * 
		 * ticket_num = 3 <tombol 5 ditekan> ticket_num = 35
		 * 
		 * nilai input -1 adalah delete. sehingga dari 35 mundur menjadi 3.
		 * 
		 * Batas default nya adalah int MAX_VALUES_BOUND = 99;
		 */

		int curVal = ticket_num;
		Log.d("ProceedNumber", String.valueOf(curVal));
		if (addition != -1) {
			curVal *= 10;
			if (curVal > MAX_VALUES_BOUND) {
				curVal = MAX_VALUES_BOUND;
			} else
				curVal += addition;
		} else {
			curVal /= 10;
		}
		ticket_num = curVal;
		Log.d("ProceedNumberEnd", String.valueOf(curVal));
		txtPrintNumber.setText(String.valueOf(ticket_num));
	}

}
