package srv.btp.eticket;

import srv.btp.eticket.obj.CityList;
import srv.btp.eticket.obj.Indicator;
import srv.btp.eticket.services.BluetoothPrintService;
import srv.btp.eticket.services.GPSDataList;
import srv.btp.eticket.services.GPSLocationService;
import srv.btp.eticket.services.StatusBarService;
import srv.btp.eticket.util.SystemUiHider;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.Rect;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
@SuppressLint("NewApi")
public class Form_Main extends Activity {

        //constant related
        
        // public related value
        public int tmpInt;
        public int city_position; //Bukan Indeks
        public int city_max_position; //Bukan Indeks
        public int city_real_position = 1; //Berubah mengikuti GPS

        // !region Form Objects
        private Button button_action[][] = new Button[3][3];
        private Button button_left;
        private Button button_right;

        
        private Button button_pref;
        
        private Intent intentPrint;
        private Intent intentPref;
        

        private Indicator[] indicators;
        private CityList[] city_list;
        public CityList[] city_display;

        private RelativeLayout top_layout;
        @SuppressWarnings("unused")
		private RelativeLayout mid_layout;
        public HorizontalScrollView top_scroll;
        
        private ImageView BT_Indicator;
        private ImageView GPS_Indicator;
        private ImageView line;
        private ImageView Direction_Indicator;
        //Service Objects
        BluetoothPrintService btx;
        GPSLocationService gls;
        GPSDataList gdl;
        

        // listeners
        private OnTouchListener button_touch_controls;
        private OnClickListener button_click_controls;

        private OnTouchListener arrow_touch_controls;
        private OnClickListener arrow_click_controls;

        private CountDownTimer cd;

        //DEBUGITEMS
        public Button dbg_btnLeft;
        public Button dbg_btnRight;
        public TextView dbg_txtLog;
        // !endregion
		private boolean isNotInitialized = true;

        /***
         * Listener Section disini berisi daftar Listener atas objek-objek Form
         * untuk membaca inputan ato respon unit.
         */

        @Override
        protected void onDestroy(){
                super.onDestroy();
        }
        @Override
        protected void onCreate(Bundle savedInstanceState) {
                /**
                 * Ketika program ini muncul, baris-baris disini yang akan dieksekusi
                 * pertama kali.
                 */

                // baris wajib :D
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_form_main);
                this.setFinishOnTouchOutside(false);
                overridePendingTransition(R.anim.push_up_in, R.anim.push_up_out);

                // jangan lupa selalu informasikan ke FormObjectTransfer.java
                FormObjectTransfer.main_activity = this;
                FormObjectTransfer.current_activity = this;

                // Deklarasi variabel variabel objek
                for (int a = 0; a < 3; a++) {
                        button_action[a] = new Button[3];
                }

                /*
                 * Indexing untuk button_action : [0,0] [1,0] [2,0] [0,1] [1,1] [2,1]
                 * [0,2] [1,2] [2,2]
                 */
                
                button_action[0][0] = (Button) this.findViewById(R.id.btnTopLeft); //0,0 = 1
                button_action[1][0] = (Button) this.findViewById(R.id.btnTopTop);  //1,0 = 2
                button_action[2][0] = (Button) this.findViewById(R.id.btnTopRight);//2,0 = 3
                button_action[0][1] = (Button) this.findViewById(R.id.btnMidLeft); //0,1 = 4
                button_action[1][1] = (Button) this.findViewById(R.id.btnMidMid);  //1,1 = 5
                button_action[2][1] = (Button) this.findViewById(R.id.btnMidRight);//2,1 = 6
                button_action[0][2] = (Button) this.findViewById(R.id.btnBotLeft); //0,2 = 7
                button_action[1][2] = (Button) this.findViewById(R.id.btnBotMId);  //1,2 = 8
                button_action[2][2] = (Button) this.findViewById(R.id.btnBotRight);//2,2 = 9

                registerOnTouchAndClick();
                for (int aa = 0; aa < 3; aa++) {
                        for (int ab = 0; ab < 3; ab++) {
                                button_action[aa][ab].setOnTouchListener(button_touch_controls);
                                button_action[aa][ab].setOnClickListener(button_click_controls);
                        }
                }
                button_left = (Button) this.findViewById(R.id.btnLeftSide);
                button_left.setOnTouchListener(arrow_touch_controls);
                button_left.setOnClickListener(arrow_click_controls);

                button_right = (Button) this.findViewById(R.id.btnRightSide);
                button_right.setOnTouchListener(arrow_touch_controls);
                button_right.setOnClickListener(arrow_click_controls);

                // layout set
                top_layout = (RelativeLayout) findViewById(R.id.top_linear);
                mid_layout = (RelativeLayout) findViewById(R.id.relInside);
                top_scroll = (HorizontalScrollView) findViewById(R.id.scrollMap);
                
                //preferences
                button_pref = (Button)this.findViewById(R.id.btn_config);
                this.PreparePreferences();
                
                //indicator set
                BT_Indicator = (ImageView)findViewById(R.id.img_indicator_bt);
                GPS_Indicator = (ImageView)findViewById(R.id.img_indicator_gps);
                Direction_Indicator = (ImageView)findViewById(R.id.img_direction);
                
                String valueIntended = PreferenceManager.getDefaultSharedPreferences(
						getBaseContext()).getString(
								"trajectory_direction", 
								"");
				Log.d("Traject",valueIntended);
				if(valueIntended.equals("maju")){
					Direction_Indicator.setImageResource(R.drawable.direction_maju);
				}else{
					Direction_Indicator.setImageResource(R.drawable.direction_balik);
				}
                BT_Indicator.setOnClickListener(bt_manual_reconnector);
                GPS_Indicator.setOnClickListener(gps_manual_reconnector);
                
                
                //service initialization
                btx = new BluetoothPrintService(this,BT_Indicator);
                gls = new GPSLocationService(GPS_Indicator);
                gdl = new GPSDataList();
                
                FormObjectTransfer.gdl = this.gdl;
                
                //FIX:DEBUG SET
                //Disini terdapat percontohan fungsi memindahkan indikator
                //
                dbg_btnLeft = (Button)this.findViewById(R.id.dbg_btnLeft);
                dbg_btnRight = (Button)this.findViewById(R.id.dbg_btnRight);
                dbg_txtLog = (TextView)this.findViewById(R.id.txtDebugLog);
                
                dbg_btnLeft.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                        	// translate to actual GPS location
            				Location location = new Location(GPSLocationService.GPS_MOCK_PROVIDER);
            				int mocked = PreferenceManager.getDefaultSharedPreferences(getBaseContext())
            						.getInt("mocked", 2) - 1;
            				location.setLatitude(gdl.lat_kota[mocked-1]);
            				location.setLongitude(gdl.long_kota[mocked-1]);
            				location.setTime(System.currentTimeMillis());
            				location.setAccuracy(0);
            				location.setElapsedRealtimeNanos(System.currentTimeMillis());

            				// show debug message in log
            				Log.d(GPSLocationService.LOG_TAG, "mock Index=" + mocked + " " + location.toString());
            				Log.d(GPSLocationService.LOG_TAG, "longitude="+ gdl.long_kota[mocked-1] );
            				Log.d(GPSLocationService.LOG_TAG, "latitude="+ gdl.lat_kota[mocked-1] );

            				// provide the new location
            				LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            				locationManager.setTestProviderLocation(GPSLocationService.GPS_MOCK_PROVIDER, location);
                        	
            				PreferenceManager.getDefaultSharedPreferences(getBaseContext())
            					.edit().putInt("mocked", mocked).commit();
            				
                                /*** Obsolete
                                 SetCityEnable(city_real_position-1);
                                CreateCityDisplay(city_display);
                                Log.d("DebugSetCityEnable","toLeft");
                                //Toast.makeText(getBaseContext(), "DEBUG: Move SetCityEnable Left to " + city_real_position, Toast.LENGTH_SHORT).show();        
                                
                                //fixed posisi animasi bergerak
                                final int theconst = 40+ (179 * (city_real_position-1));
                                final int getX = top_scroll.getScrollX();
                                cd = new CountDownTimer(600, 10) {
                                        int a=0;
                                        @Override
                                        public void onTick(long millisUntilFinished) {
                                                Log.d("Timer",""+ ++a + " " + (int)(theconst + ((getX-theconst) * millisUntilFinished/600)));
                                                top_scroll.scrollTo((int)(theconst + ((getX-theconst) * millisUntilFinished/600)),0);
                                        }
                                        @Override
                                        public void onFinish() {
                                                top_scroll.scrollTo(theconst,0);
                                        }
                                };
                                cd.start();
                                */
                        }
                });
                dbg_btnRight.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                        	Location location = new Location(GPSLocationService.GPS_MOCK_PROVIDER);
            				int mocked = PreferenceManager.getDefaultSharedPreferences(getBaseContext())
            						.getInt("mocked", 0) + 1;
            				location.setLatitude(gdl.lat_kota[mocked-1]);
            				location.setLongitude(gdl.long_kota[mocked-1]);
            				location.setTime(System.currentTimeMillis());
            				location.setAccuracy(0);
            				location.setElapsedRealtimeNanos(System.currentTimeMillis());
            				// show debug message in log
            				Log.d(GPSLocationService.LOG_TAG, "mock Index=" + mocked + " " + location.toString());
            				Log.d(GPSLocationService.LOG_TAG, "longitude="+ gdl.long_kota[mocked-1] );
            				Log.d(GPSLocationService.LOG_TAG, "latitude="+ gdl.lat_kota[mocked-1] );

            				// provide the new location
            				LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            				locationManager.setTestProviderLocation(GPSLocationService.GPS_MOCK_PROVIDER, location);
                        	
            				PreferenceManager.getDefaultSharedPreferences(getBaseContext())
            					.edit().putInt("mocked", mocked).commit();    
                        	
                        	/*SetCityEnable(city_real_position+1);
                                CreateCityDisplay(city_display);
                                Log.d("DebugSetCityEnable","toRight");
                                //Toast.makeText(getBaseContext(), "DEBUG: Move SetCityEnable Right to " + city_real_position, Toast.LENGTH_SHORT).show();        
                                final int theconst = 40+ (179 * (city_real_position-1));
                                final int getX = top_scroll.getScrollX();
                                cd = new CountDownTimer(600, 10) {
                                        int a=0;
                                        @Override
                                        public void onTick(long millisUntilFinished) {
                                                Log.d("Timer",""+ ++a + " " + (int)(theconst + ((getX-theconst) * millisUntilFinished/600)));
                                                top_scroll.scrollTo((int)(theconst + ((getX-theconst) * millisUntilFinished/600)),0);
                                        }
                                        @Override
                                        public void onFinish() {
                                                top_scroll.scrollTo(theconst,0);
                                        }
                                };
                                cd.start();
                                */
                        }
                });
                //END DEBUG
                
        }

        private Button Int2Button(int num) {
                switch(num){
                case 1:
                        return button_action[0][0];
                case 2:
                        return button_action[1][0];
                case 3:
                        return button_action[2][0];
                case 4:
                        return button_action[0][1];
                case 5:
                        return button_action[1][1];
                case 6:
                        return button_action[2][1];
                case 7:
                        return button_action[0][2];
                case 8:
                        return button_action[1][2];
                case 9:
                        return button_action[2][2];
                        default:
                                return null;
                }
        }
        private int ButtonID2Int(int button_id){
                switch(button_id){
                case R.id.btnTopLeft:
                        return 1;
                case R.id.btnTopTop:
                        return 2;
                case R.id.btnTopRight:
                        return 3;
                case R.id.btnMidLeft:
                        return 4;
                case R.id.btnMidMid:
                        return 5;
                case R.id.btnMidRight:
                        return 6;
                case R.id.btnBotLeft:
                        return 7;
                case R.id.btnBotMId:
                        return 8;
                case R.id.btnBotRight:
                        return 9;
                        default:
                                return 0;
                }
        }

        @Override
        public void onWindowFocusChanged(boolean hasFocus) {
                super.onWindowFocusChanged(hasFocus);
                /**
                 * Berbagai fungsi ada yang tidak berjalan semestinya di onCreate.
                 * Namun fungsi itu perlu berjalan ketika startup.
                 * Maka dari itu pindahkan fungsi2 tersebut disini.
                 * 
                 * Namun perlu diperhatikan, callback ini selalu dipanggil ketika muncul,
                 * atau ketika activity selanjutnya "pulang" ke activity ini.
                 **/
        }

        
        @Override
        protected void onPostCreate(Bundle savedInstanceState) {
                super.onPostCreate(savedInstanceState);
                //Menyambung ke bluetooth printer
                
                gls.ActivateGPS();
                try{
                	gdl.getDataFromJSON(); //Disini terjadi async task, mohon menunggu.
                }catch(Exception e){
                	CallError();
                }
                //PrepareCityList(); //Pindah jadi di generateData();
        }

        public void CallError() {
        	//TODO: MESSAGE BOX HERE :D
        	String msg = "";
    		AlertDialog.Builder builder;
            builder = new AlertDialog.Builder(this);
            msg = "Program mengalami masalah dalam konfigurasi atau jaringan. Mohon restart aplikasi atau masuk ke bagian Setting untuk mengubah konfigurasi sistem.";
            builder.setTitle("Masalah");
    		builder.setMessage(msg);
            builder.setCancelable(false);
            builder.setPositiveButton("Restart", 
                new DialogInterface.OnClickListener() {
    			// Restart
            	@Override public void onClick(DialogInterface dialog, int id) {
					//Restart APP
                	Intent mStartActivity = new Intent(getBaseContext(), Form_Main.class);
					int mPendingIntentId = 45556;
					PendingIntent mPendingIntent = PendingIntent.getActivity(getBaseContext(), mPendingIntentId,    mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
					AlarmManager mgr = (AlarmManager)getBaseContext().getSystemService(Context.ALARM_SERVICE);
					mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 400, mPendingIntent);
					System.exit(0);
            	}
            });
            builder.setNegativeButton("Settings",
            		new DialogInterface.OnClickListener() {
						@Override public void onClick(DialogInterface dialog, int which) {
							//Jalankan Preferences
                    		Bundle opt = new Bundle();
                    		opt.putBoolean("fromParent", true);
                            button_pref.setBackgroundResource(R.drawable.button_config);
                            intentPref = new Intent(getApplicationContext(), AppPreferences.class);
                            startActivity(intentPref,opt);
                            CallError();
						}
					});
            AlertDialog alert = builder.create();
            alert.show();
            
		}
		protected void postExecution(){
        	btx._setBtAddr(                
                    PreferenceManager.getDefaultSharedPreferences(getBaseContext())
                                    .getString("bluetooth_list", "00:00:00:00:00:00")
                            );
        	Log.d("BluetoothAddressLoader",btx.btSelectedAddr);
        	FormObjectTransfer.bxl = btx;
        	btx.ConnectPrinter();
        }
        
        @Override
        public void onBackPressed() {
                // Mematikan fungsi default onBackPressed
                DoNothing();
                // Animasi
                overridePendingTransition(R.anim.push_down_in, R.anim.push_down_out);
        }

        @Override
        public void onResume() {
                super.onResume();
                FormObjectTransfer.current_activity = this;
        }

        /*
         * Additional Function Disini berisi daftar method atas objek-objek Form
         * untuk tambahan fungsi.
         */
        
        private void registerOnTouchAndClick() {
                button_touch_controls = new OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                                /***
                                 * Fungsi ini akan membuat simulasi "tombol ditekan"
                                 */
                                v.setBackgroundResource(R.drawable.button_pressed);
                                return false;
                        };
                };
                button_click_controls = new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                                /***
                                 * Fungsi ini akan mengembalikan warna button ke bentuk semula.
                                 */
                                Button b = (Button)v;
                                v.setBackgroundResource(R.drawable.button);

                                /***
                                 * Serta melakukan aktivitas apa selanjutnya. Disini contohnya
                                 * adalah lanjut ke activity selanjutnya.
                                 * 
                                 * Jangan lupa menyimpan info button mana yang ditekan agar
                                 * dapat diolah oleh activity selanjutnya :D
                                 * 
                                 */
                                
                                FormObjectTransfer.Kota1 = city_list[city_real_position-1].Nama;
                                FormObjectTransfer.Kota2 = city_list[ButtonID2Int(b.getId())-1 + city_position-1].Nama;
                                FormObjectTransfer.harga = 0;
                                //Tarif Loops
                                for(int a=city_real_position-1;a<city_position-1+ButtonID2Int(b.getId());a++){
                                        if(a==city_real_position-1){
                                                FormObjectTransfer.harga+= city_list[a].TarifKanan;
                                        }else if(a==city_position-2+ButtonID2Int(b.getId())){
                                                FormObjectTransfer.harga+=city_list[a].TarifKiri;
                                        }else
                                        FormObjectTransfer.harga+=city_list[a].TarifKiri + city_list[a].TarifKanan;
                                }
                                
                                //End Tarif
                                
                                
                                //Intent Moving
                                intentPrint = new Intent(getApplicationContext(), Form_Print.class);
                                startActivity(intentPrint);
                                overridePendingTransition(R.anim.push_up_in, R.anim.push_up_out);
                        }
                };
                arrow_touch_controls = new OnTouchListener() {

                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                                if (v == button_right)
                                        v.setBackgroundResource(R.drawable.button_right_pressed);
                                else
                                        v.setBackgroundResource(R.drawable.button_left_pressed);

                                return false;
                        }
                };
                arrow_click_controls = new OnClickListener() {

                        @Override
                        public void onClick(View v) {
                                if (v == button_right){
                                        v.setBackgroundResource(R.drawable.button_right_active);
                                        SwitchCity(1);}
                                else{
                                        v.setBackgroundResource(R.drawable.button_left_active);
                                        SwitchCity(0);}
                                
                        }
                };
        }

        public void SummonButton(CharSequence Kota1, CharSequence Kota2, int qty,
                        int harga, int total) {
                FormObjectTransfer.Kota1 = Kota1;
                FormObjectTransfer.Kota2 = Kota2;
                FormObjectTransfer.qty = qty;
                FormObjectTransfer.harga = harga;
                FormObjectTransfer.total = total;
                //PREPURR to MULTI PRINT
                for(int a = 0;a < qty; a++){
                	String GeneratedID = StatusBarService.GetSerializedID(a+1, 
                    		PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                    		.getString("plat_bis_hidden", getResources().getString(R.string.default_platbis)));
                    
                	btx.PrintText(GeneratedID, Kota1.toString(), Kota2.toString(), qty, harga);
                }
                CustomDialogControl dlg = new CustomDialogControl((Activity) this);
                dlg.show();
        }

        public void CreateIndicator(int num) {
                int left_space = 40; // Default jarak pinggir kiri
                int icon_size = 32; // Ukuran indicator
                int block_indicator_size = 150; //Ukuran per indikator

                // Pembuatan garis indikator
                line = new ImageView(this);
                line.setImageResource(R.drawable.indicator_line);
                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(73
                                * (2 * (num - 1)) + (icon_size * (num - 1)), 8);
                lp.setMargins(left_space + 53 + 24, 98, 0, 0);
                line.setLayoutParams(lp);
                line.setScaleType(ScaleType.CENTER_CROP);
                top_layout.addView(line);

                // Pembuatan Indicator
                indicators = new Indicator[num];
                for (int a = 0; a < num; a++) {
                        indicators[a] = new Indicator("test_Debug",
                                        this.getApplicationContext());

                        // indikator bundar
                        RelativeLayout.LayoutParams indLP = new RelativeLayout.LayoutParams(
                                        icon_size, icon_size);
                        indLP.setMargins(left_space + (73 * (2 * a + 1) + (icon_size * a)),
                                        86, 0, 0);
                        indicators[a].img.setLayoutParams(indLP);
                        top_layout.addView(indicators[a].img);

                        // indikator balon
                        RelativeLayout.LayoutParams balLP = new RelativeLayout.LayoutParams(
                                        block_indicator_size, 68);
                        balLP.setMargins(left_space - 7 + (21 * (2 * a + 1) + (136 * a)),
                                        12, 0, 0);
                        indicators[a].balloon.setLayoutParams(balLP);
                        top_layout.addView(indicators[a].balloon);

                        // indikator text
                        RelativeLayout.LayoutParams txtLP = new RelativeLayout.LayoutParams(
                                        block_indicator_size, icon_size);
                        txtLP.setMargins(balLP.leftMargin
                                        + (indicators[a].txt.getWidth() / 2), balLP.topMargin + 12,
                                        0, 0);
                        indicators[a].txt.setLayoutParams(txtLP);
                        top_layout.addView(indicators[a].txt);
                        
                        // indikator angka
                        RelativeLayout.LayoutParams numLP = new RelativeLayout.LayoutParams(
                                        icon_size, icon_size);
                        numLP.setMargins(left_space + (73 * (2 * a + 1) + (icon_size * a)),
                                        86, 0, 0);
                        indicators[a].num.setLayoutParams(numLP);
                        top_layout.addView(indicators[a].num);

                }

        }

        
        public void PreparePreferences(){
                button_pref.setOnClickListener(new OnClickListener() {
        
                        @Override
                        public void onClick(View arg0) {
                        		Bundle opt = new Bundle();
                        		opt.putBoolean("fromParent", true);
                                button_pref.setBackgroundResource(R.drawable.button_config);
                                intentPref = new Intent(getApplicationContext(), AppPreferences.class);
                                startActivity(intentPref,opt);
                        }
                });
                button_pref.setOnTouchListener(new OnTouchListener() {        
                        @Override
                        public boolean onTouch(View arg0, MotionEvent arg1) {
                                button_pref.setBackgroundResource(R.drawable.button_config_pressed);
                                return false;
                        }
                });
                
        }
        
        public int getTextWidth(String text, Paint paint) {
                Rect bounds = new Rect();
                paint.getTextBounds(text, 0, text.length(), bounds);
                int width = bounds.left + bounds.width();
                return width;
        }

        public int getTextHeight(String text, Paint paint) {
                Rect bounds = new Rect();
                paint.getTextBounds(text, 0, text.length(), bounds);
                int height = bounds.bottom + bounds.height();
                return height;
        }
        
        public void CleanCityList(){
        	//Bersih-bersih indikator perlu dilakukan agar memory bersih
        	
        	//dari indikator
        	try{
		        for(int a=0;a<top_layout.getChildCount();a++){
		        	top_layout.removeView(indicators[a].balloon);
		        	top_layout.removeView(indicators[a].img);
		        	top_layout.removeView(indicators[a].num);
		        	top_layout.removeView(indicators[a].txt);
		        }
        	}catch(IndexOutOfBoundsException e){
        		top_layout.removeAllViewsInLayout();
        		e.printStackTrace();
        	}
        	finally{
        		//garis
        		top_layout.removeView(line);
        	}
        }
        public void PrepareCityList(){
        		String[] namaKota = gdl.kotaList;
                int dataSize = namaKota.length;
                int hargaParsial[] = gdl.hargaParsial;
                Log.e("PrepareCityList","size="+dataSize);
                city_list = new CityList[dataSize];
                city_max_position = dataSize;
                top_layout.setLayoutParams(new FrameLayout.LayoutParams(
                                (180 * dataSize) + 60, -2)); //180, 60, -2 adalah konstanta tetap panjang layout. Untuk saat ini, jangan diubah.
                CreateIndicator(dataSize);
                Log.d("INDICATOR","CreatedIndicator");
                city_display = new CityList[dataSize<9 ? dataSize : 9];
                Log.d("debug", String.valueOf(indicators[0].txt.getLeft()));
                for(int a = 0; a<dataSize;a++){
                        indicators[a].txt.setText(namaKota[a]);
                        indicators[a].num.setText(""+(a+1));
                        city_list[a] = new CityList();
                        city_list[a].Nama = namaKota[a];
                        city_list[a].Priority = a+1;
                        city_list[a].TarifKiri = hargaParsial[a]/2;
                        city_list[a].TarifKanan = hargaParsial[a+1]/2;
                 }
                button_left.setBackgroundResource(R.drawable.button_left_passive);
                button_left.setEnabled(false);
                
                
                //Mempersiapkan default value
                double longi = (double)PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getFloat("long", (float) gdl.long_kota[0]);
                double lati =  (double)PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getFloat("lat", (float) gdl.lat_kota[0]);
                int current_city = FormObjectTransfer.gdl.getNearestCity(lati, longi);
                dbg_txtLog.setText("long:"+ longi + "\n "  +
                				   "lat :"+ lati );
    			
                int initializationValue = current_city;
                Log.e("InitializationValue",initializationValue+"");
                		
                SetCityEnable(initializationValue); 
                city_position = 1;
                for(int a = 0;a<city_display.length;a++){
                        city_display[a] = city_list[a];
                }
                CreateCityDisplay(city_display);

                if (isNotInitialized){
    				postExecution();
    				isNotInitialized = false;
    			}
        }
        
        
	public void CreateCityDisplay(CityList[] cityList) {
		//
		final int MAX_LENGTH_TEXT = 11;
		final int size = 9;
		int indicator = 9;
		if (cityList.length < size) {
			indicator = cityList.length;
			button_right.setBackgroundResource(R.drawable.button_right_passive);
			button_right.setEnabled(false);
		}
		for (int a = 0; a < indicator; a++) {
			Int2Button(a + 1).setText(
					(cityList[a].Nama.length() > MAX_LENGTH_TEXT+ (getResources().getDisplayMetrics().density*2) ? cityList[a].Nama
							.substring(0, (int) (MAX_LENGTH_TEXT+ (getResources().getDisplayMetrics().density*2))) + "..." : cityList[a].Nama));
			if (cityList[a].isNotPassed) {
				Int2Button(a + 1).setBackgroundResource(R.drawable.button);
				Int2Button(a + 1).setEnabled(true);
			} else {
				if (cityList[a].Priority == city_real_position) {
					Int2Button(a + 1).setBackgroundResource(
							R.drawable.button_disabled);
					Int2Button(a + 1).setEnabled(false);
				} else {
					Int2Button(a + 1).setBackgroundResource(
							R.drawable.button_passive);
					Int2Button(a + 1).setEnabled(false);
				}
			}
		}
		for (int a = indicator; a < size; a++) {
			Int2Button(a + 1).setText("-");
			Int2Button(a + 1).setBackgroundResource(R.drawable.button_passive);
			Int2Button(a + 1).setEnabled(false);
		}
	}

        /***
         * Memindahkan Display City ke arah kiri atau kanan.
         * @param whichArrow (0 = Panah kiri, 1 = Panah Kanan)
         */
        public void SwitchCity(int whichArrow) {
                if (whichArrow == 0 && city_position > 9) { // Kiri 
                        city_display = new CityList[9];
                        city_position -= 9;
                        for (int a = city_position; a < city_position + 9; a++) {
                                city_display[a-city_position] = city_list[a-1];
                        }
                        if (!(city_position > 1)) {
                                button_left
                                                .setBackgroundResource(R.drawable.button_left_passive);
                                button_left.setEnabled(false);
                                
                        }
                        button_right.setBackgroundResource(R.drawable.button_right_active);
                        button_right.setEnabled(true);
                        
                } else if (whichArrow == 1 && city_position < city_max_position) { //Kanan
                        Log.d("DEBUG_POSITION", city_position + " " + city_max_position + " real : "+ city_real_position);
                        city_position += 9;
                        city_display = new CityList[city_max_position-city_position<=9?city_max_position-city_position+1 : 9];
                        for (int a = city_position; a < city_position+city_display.length; a++) {
                                city_display[a-city_position] = city_list[a-1];
                        }
                        if (city_max_position - city_position<=9) {
                                button_right
                                                .setBackgroundResource(R.drawable.button_right_passive);
                                button_right.setEnabled(false);              
                        }
                        button_left.setBackgroundResource(R.drawable.button_left_active);
                        button_left.setEnabled(true);
                }
                CreateCityDisplay(city_display);
        }

        public void SetCityEnable(int cityIndex){
                if(cityIndex > city_max_position){
                        //Fail ketika inputan melebihi Index
                        return;
                }
                if(cityIndex == city_max_position){
                        /*
                         * Disini terjadi pertanyaan reset rute ato menyudahi rute,
                         * karena CityEnable sudah tiba di titik poin terakhir.
                         * Tindakan yang perlu dilakukan adalah menanyakan kembali
                         * rute.
                         */
                	AlertDialog.Builder builder = new AlertDialog.Builder(this);
                	builder.setMessage("Bis telah mencapai area titik akhir rute dari trayek. Klik OK untuk memutar balik rute.")
                	       .setTitle("Rute berakhir");
                	builder.setCancelable(false);
                	builder.setPositiveButton("OK", rute_berputar);

                	// 3. Get the AlertDialog from create()
                	AlertDialog dialog = builder.create();
                	dialog.setCanceledOnTouchOutside(false);
                	dialog.setCancelable(false);
                	dialog.show();
                }
                if(cityIndex == 0){
                	for(int a = 0; a< city_max_position;a++){
                		//indicators[a].setEnabled(2);
                		//city_list[a].isNotPassed = false;
                	}
                	return;
                }
                city_real_position = cityIndex;
                for(int a=0;a<cityIndex-1;a++){
                        indicators[a].setEnabled(2);
                city_list[a].isNotPassed = false;
                }
                indicators[cityIndex-1].setEnabled(1);
                city_list[cityIndex-1].isNotPassed = false;
                
                for(int aa=cityIndex;aa<city_max_position;aa++){
                        indicators[cityIndex].setEnabled(0);
                        city_list[cityIndex].isNotPassed = true;
                }

                //CreateCityDisplay(city_display);
        }
        /***
         * Fungsi ini tidak melakukan apa-apa :D
         */
        public void DoNothing(){
                return;
        }
		public boolean checkStatus() {
			
			//emergency recovery status
			if(FormObjectTransfer.isBTConnected && FormObjectTransfer.isGPSConnected){
				EnableAllButtons();
				return true;
			}else{
				DisableAllButtons();
			}
			return false;
		}
		
		public void EnableAllButtons(){
			for(int a = 1; a<= 9;a++){
        		Int2Button(a).setEnabled(true);
        		Int2Button(a).setBackgroundResource(R.drawable.button);
        	}
			if (city_position > 1) {
				button_left.setEnabled(true);
				button_left.setBackgroundResource(R.drawable.button_left_active);
			}
			
			if (city_position < city_max_position) {
				button_right.setEnabled(true);
				button_right.setBackgroundResource(R.drawable.button_right_active);
			}
			if(!isNotInitialized){
                //sedikit perlakuan setelah semua jalan
				
				SetCityEnable(city_real_position);
				CreateCityDisplay(city_display);
			}
		}
		public void DisableAllButtons(){
			for(int a = 1; a<= 9;a++){
        		Int2Button(a).setEnabled(false);
        		Int2Button(a).setBackgroundResource(R.drawable.button_passive);
        	}
			button_left.setEnabled(false);
			button_left.setBackgroundResource(R.drawable.button_left_passive);
			button_right.setEnabled(false);
			button_right.setBackgroundResource(R.drawable.button_right_passive);
		}
		
		protected DialogInterface.OnClickListener rute_berputar = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// Fungsi membalik rute
				String valueIntended = PreferenceManager.getDefaultSharedPreferences(
						getBaseContext()).getString(
								"trajectory_direction", 
								"");
				Log.d("Traject",valueIntended);
				if(valueIntended.equals("maju")){
					//Langsung balikkan data jadi balik
					PreferenceManager.getDefaultSharedPreferences(
							getBaseContext()).edit()
							.putString("trajectory_direction", "balik")
							.commit();
					//dan putar datanya
					gdl.SetTrack(true);
					Direction_Indicator.setImageResource(R.drawable.direction_balik);
					
				}else{
					//Langsung balikkan data jadi balik
					PreferenceManager.getDefaultSharedPreferences(
							getBaseContext()).edit()
							.putString("trajectory_direction", "maju")
							.commit();
					//dan putar datanya
					gdl.SetTrack(false);
					Direction_Indicator.setImageResource(R.drawable.direction_maju);
				}
				//Pertama bersihkan dulu indikator
				CleanCityList();
				PreferenceManager.getDefaultSharedPreferences(getBaseContext())
				.edit().putInt("mocked", 1).commit();
				PrepareCityList();
				top_scroll.scrollTo(0, 0);
				checkStatus();
			}
		};
		
		protected OnClickListener bt_manual_reconnector = new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(!(btx.BT_STATE == BluetoothPrintService.STATE_CONNECTING)){
				Toast.makeText(getBaseContext(), "Menyambung Bluetooth secara manual...", Toast.LENGTH_SHORT).show();
				btx.sharedCountdown.cancel();
				btx.RecreateTimer(BluetoothPrintService.DEFAULT_RECONNECT_FAIL_TIME);
				btx.ConnectPrinter();
				}else{
					Toast.makeText(getBaseContext(), "Menyambung manual tidak memungkinkan, Bluetooth sedang dalam pencarian.", Toast.LENGTH_SHORT).show();
				}
			}
		};
		protected OnClickListener gps_manual_reconnector = new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Toast.makeText(getBaseContext(), "Menyambung GPS secara manual...", Toast.LENGTH_SHORT).show();
				gls.ctd.cancel();
				gls.RecreateTimer();
				gls.ActivateGPS();
			}
		};
		
}