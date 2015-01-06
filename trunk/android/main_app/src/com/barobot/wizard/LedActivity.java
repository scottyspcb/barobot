package com.barobot.wizard;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.barobot.R;

public class LedActivity extends BlankWizardActivity {

	int mode = 0;
	boolean toggle = false;
	private static int LED_LEFT = 1;
	private static int LED_RIGHT = 2;
	private static int LED_ALL = 4;
	private static int LED_OK = 8;
	private static int LED_ERROR = 1024;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wizard_led);
		enableTimer( 200, 200 );
	}

	public void onOptionsButtonClicked(View view)
	{
		switch(view.getId()){
			case R.id.wizard_led_start:
				checkCarriageLeftIsWhite();
				break;
			default:
				super.onOptionsButtonClicked(view);
				break;
		}
	}

	private void checkCarriageLeftIsWhite() {
		mode = LED_LEFT;
		barobot.lightManager.turnOffLeds(barobot.main_queue);
	    AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    builder.setMessage(R.string.wizard_led_carriage_left_lights)
            .setCancelable(false)
            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                	checkCarriageRightIsWhite();
                }
            })
            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                     dialog.cancel();
                     carriageLeftIsNotWhite();
                }
            });
	    final AlertDialog alert = builder.create();
		runOnUiThread(new Runnable() {
			  public void run() {
				  alert.show();
			  }
		});	     
	}
	private void checkCarriageRightIsWhite() {
		mode = LED_RIGHT;
		barobot.lightManager.turnOffLeds(barobot.main_queue);
	     AlertDialog.Builder builder = new AlertDialog.Builder(this);
	     builder.setMessage(R.string.wizard_led_carriage_right_lights)
           .setCancelable(false)
           .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
               public void onClick(DialogInterface dialog, int id) {
                    checkTopIsWhite();
               }
           })
           .setNegativeButton("No", new DialogInterface.OnClickListener() {
               public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                    carriageRightIsNotWhite();
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
		mode = LED_ALL;
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
		mode = LED_OK;
		showResult(R.string.wizard_led_result_ok, true );
	}

	private void topIsNotWhite(){
		mode = LED_ERROR;
		showResult(R.string.wizard_led_check_top, false );
	}

	private void carriageLeftIsNotWhite(){
		mode = LED_ERROR;
		showResult(R.string.wizard_led_check_carret_left, false );
	}
	private void carriageRightIsNotWhite(){
		mode = LED_ERROR;
		showResult(R.string.wizard_led_check_carret_right, false );
	}

	private void showResult(final int text, final boolean success ) {
		runOnUiThread(new Runnable() {
			  public void run() {
				  setFullScreen();
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
		mode = 0;
		barobot.lightManager.turnOffLeds(barobot.main_queue);
		super.onDestroy();
	}

	public void onTick(){
		if( mode == LED_LEFT ){
			if(toggle){
				barobot.lightManager.turnOffLeds(barobot.main_queue);
			}else{
				barobot.lightManager.carret_color_left(barobot.main_queue, 255, 255, 255);
			}
		}else if( mode == LED_RIGHT ){
			if(toggle){
				barobot.lightManager.turnOffLeds(barobot.main_queue);
			}else{
				barobot.lightManager.carret_color_right(barobot.main_queue, 255, 255, 255);
			}
		}else if( mode == LED_ALL ){	
			if(toggle){
				barobot.lightManager.turnOffLeds(barobot.main_queue);
			}else{
				barobot.lightManager.setAllLeds(barobot.main_queue, "ff", 255, 255, 255, 255);
			}
		}else if( mode == LED_OK ){
			barobot.lightManager.setAllLeds(barobot.main_queue, "22", 255, 0, 255, 0);

		}else if( mode == LED_ERROR ){
			barobot.lightManager.setAllLeds(barobot.main_queue, "11", 255, 255, 0, 0);
		}
		toggle = !toggle;
	}
}
