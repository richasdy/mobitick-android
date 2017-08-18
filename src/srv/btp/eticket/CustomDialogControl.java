package srv.btp.eticket;

import android.app.Activity;
import android.app.Dialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

public class CustomDialogControl extends Dialog implements
		android.view.View.OnClickListener {

	public Activity c;
	public Dialog d;
	public Button done;
	public TextView Kota;
	public TextView subtotal;
	public TextView Total;

	public CustomDialogControl(Activity a) {
		super(a);
		// TODO Auto-generated constructor stub
		this.c = a;
		this.setCanceledOnTouchOutside(false);
		this.setCancelable(false);
	}
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.cusdialog);

		getWindow().setBackgroundDrawableResource(R.color.transparent);
		
		done = (Button) findViewById(R.id.btn_done);
		done.setOnClickListener(this);
		
		done.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent mov) {
				// TODO Auto-generated method stub
				v.setBackgroundResource(R.drawable.button_dark_press);
				Button bb = (Button)v;
				bb.setTextColor(Color.WHITE);
				return false;
			}
		});

		Kota = (TextView) findViewById(R.id.txtKota);
		subtotal = (TextView) findViewById(R.id.txtIndicator);
		Total = (TextView) findViewById(R.id.txtTotal);
		
		Kota.setText(FormObjectTransfer.Kota1 + " - " + FormObjectTransfer.Kota2);
		subtotal.setText(FormObjectTransfer.qty + " x @ Rp "+ FormObjectTransfer.harga);
		Total.setText("Total: Rp " + FormObjectTransfer.total);
		
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_done:
			v.setBackgroundResource(R.drawable.button_dark);
			dismiss();
			break;
		default:
			break;
		}
		dismiss();
	}
	
	
}