package srv.btp.eticket.obj;

import srv.btp.eticket.R;
import android.content.Context;
import android.graphics.Typeface;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

public class Indicator {
	public ImageView img;
	private CharSequence label;
	public ImageView balloon;
	private int isEnabled = 0;
	public TextView txt;
	public TextView num;
	
	public Indicator(CharSequence lbl, Context targetContext){
		img = new ImageView(targetContext);
		
		txt = new TextView(targetContext);
		this.setLabel(lbl);
		txt.setText(lbl);
		
		num = new TextView(targetContext);
		num.setText("0");
		
		balloon = new ImageView(targetContext);
		img.setImageResource(R.drawable.indicator_off);
		balloon.setImageResource(R.drawable.balloon);
		
		img.setScaleType(ScaleType.CENTER_CROP);
		balloon.setScaleType(ScaleType.CENTER_CROP);
		txt.setTextColor(targetContext.getResources().getColor(R.color.white));
		if( targetContext.getResources().getDisplayMetrics().density > 1.0)
			txt.setTextSize(14);
			else txt.setTextSize(22); //22
		txt.setTypeface(Typeface.DEFAULT_BOLD, Typeface.BOLD);
		txt.setGravity(Gravity.CENTER);
		
		num.setTextColor(targetContext.getResources().getColor(R.color.white));
		if( targetContext.getResources().getDisplayMetrics().density > 1.0)
			num.setTextSize(8);
			else num.setTextSize(16); //16
		num.setTypeface(Typeface.DEFAULT_BOLD, Typeface.BOLD);
		num.setGravity(Gravity.CENTER);
		
		img.setVisibility(View.VISIBLE);
		balloon.setVisibility(View.VISIBLE);
		txt.setVisibility(View.VISIBLE);
		num.setVisibility(View.VISIBLE);
		
	}
	
	public void setLabel(CharSequence c){
		label = c;
		txt.setText(c);
	}
	
	public CharSequence getLabel(){
		return label;
	}
	
	public void setEnabled(int active) {
		if (active==0) {
			img.setImageResource(R.drawable.indicator_off);
			balloon.setImageResource(R.drawable.balloon);
		} else if(active==1) {
			img.setImageResource(R.drawable.indicator_on);
			balloon.setImageResource(R.drawable.balloon);
		} else {
			img.setImageResource(R.drawable.indicator_disabled);
			balloon.setImageResource(R.drawable.balloon_disabled);
		}
	}
	
	public int getEnabled(){
		return isEnabled;
	}
	
	

}
