package com.barobot.wizard;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.barobot.R;

public class LedActivity extends BlankWizardActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wizard_led);
	}

	public void onOptionsButtonClicked(View view)
	{
		switch(view.getId()){
			case R.id.wizard_led_start:
				barobot.lightManager.turnOffLeds(barobot.main_queue);
				barobot.lightManager.carret_color(barobot.main_queue, 255, 255, 255);
				checkCarriageIsWhite();
				break;
			default:
				super.onOptionsButtonClicked(view);
				break;
		}
	}

	private void checkCarriageIsWhite() {
	     AlertDialog.Builder builder = new AlertDialog.Builder(this);
	     builder.setMessage(R.string.wizard_led_carriage_lights)
            .setCancelable(false)
            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                     checkTopIsWhite();
                }
            })
            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                     dialog.cancel();
                     carriageIsNotWhite();
                }
            });
	    final AlertDialog alert = builder.create();
		runOnUiThread(new Runnable() {
			  public void run() {
				  alert.show();
			  }
		});	     
	}

	private void checkTopIsWhite(){
		barobot.lightManager.turnOffLeds(barobot.main_queue);
		barobot.lightManager.setAllLeds(barobot.main_queue, "ff", 255, 255, 255, 255);
	    AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    builder.setMessage(R.string.wizard_led_all_lights)
	           .setCancelable(false)
	           .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int id) {
	                    topIsWhite();
	               }
	           })
	           .setNegativeButton("No", new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int id) {
	                    dialog.cancel();
	                    topIsNotWhite();
	               }
	           });
	    final AlertDialog alert = builder.create();
		runOnUiThread(new Runnable() {
			  public void run() {
				  alert.show();
			  }
		});
	}
	private void topIsWhite(){
		showResult(R.string.wizard_led_result_ok, true );
	}

	private void topIsNotWhite(){
		showResult(R.string.wizard_led_check_top, false );
	}

	private void carriageIsNotWhite(){
		showResult(R.string.wizard_led_check_carret, false );
	}

	private void showResult(final int text, final boolean success ) {
		runOnUiThread(new Runnable() {
			  public void run() {
				  TextView wizard_led_results			= (TextView) findViewById(R.id.wizard_led_results);
				  wizard_led_results.setText(text);
				  if(success){
					  wizard_led_results.setTextColor(Color.GREEN);
				  }else{
					  wizard_led_results.setTextColor(Color.RED);
				  }
			  }
		});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
}
