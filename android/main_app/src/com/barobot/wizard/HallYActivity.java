package com.barobot.wizard;

import com.barobot.R;
import com.barobot.common.Initiator;
import com.barobot.parser.Queue;
import com.barobot.parser.message.AsyncMessage;
import com.barobot.parser.message.Mainboard;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class HallYActivity extends BlankWizardActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wizard_hall_y);
	}

	public void onOptionsButtonClicked(View view)
	{
		switch(view.getId()){
			case R.id.wizard_hally_start:
				barobot.lightManager.turnOffLeds(barobot.main_queue);
				barobot.lightManager.setAllLeds(barobot.main_queue, "44", 255, 0, 0, 255);
				checkHallY(barobot.main_queue);
				break;
			default:
				super.onOptionsButtonClicked(view);
				break;
		}
	}

	int hally_neutral = 0;
	int hally_front = 0;
	int hally_back = 0;
	String error = "";
	int sensor_max = 1000;
	
	// hally_neutral in <495;520>
	int neutral_min = 495;
	int neutral_max = 520;

	int minDiff1 = 150;		// front - back
	int minDiff2 = 65;		// front - neutral
	int minDiff3 = 65;		// back - neutral

	private void checkHallY(Queue q) {	
		showResult(R.string.wizard_hally_waiting, true );

		int back = barobot.state.getInt("SERVOY_BACK_POS", 1000);
		int front = barobot.state.getInt("SERVOY_FRONT_POS", 1000);
		int neutral	= barobot.state.getInt("SERVOY_BACK_NEUTRAL", 1000);

		//Initiator.logger.w("checkHallY.front", ""+front );
		//Initiator.logger.w("checkHallY.back", ""+back );	
		//Initiator.logger.w("checkHallY.neutral", ""+neutral );

		barobot.y.move(q, neutral, true);
		barobot.lightManager.setAllLeds(barobot.main_queue, "22", 255, 100, 100, 100);
		q.addWait( 500 );

		barobot.y.move(q, back, true);
		q.add( new AsyncMessage( true ){
			@Override
			public Queue run(Mainboard dev, Queue queue) {
				hally_back = barobot.state.getInt("HALLY", 0);
				return null;
			}
		});
		barobot.lightManager.setAllLeds(barobot.main_queue, "22", 255, 0, 100, 100);
		q.addWait( 500 );

		barobot.y.move(q, neutral, true);
		q.add( new AsyncMessage( true ){
			@Override
			public Queue run(Mainboard dev, Queue queue) {
				hally_neutral = barobot.state.getInt("HALLY", 0);
				return null;
			}
		});
		barobot.lightManager.setAllLeds(barobot.main_queue, "22", 255, 0, 100, 0);
		q.addWait( 500 );

		barobot.y.move(q, front, true);
		q.add( new AsyncMessage( true ){
			@Override
			public Queue run(Mainboard dev, Queue queue) {
				hally_front = barobot.state.getInt("HALLY", 0);
				return null;
			}
		});
		barobot.lightManager.setAllLeds(barobot.main_queue, "22", 255, 0, 100, 100);
		q.addWait( 500 );

		barobot.y.move(q, back, true);
		q.add( new AsyncMessage( true ){
			@Override
			public Queue run(Mainboard dev, Queue queue) {
				int hally_back2 = barobot.state.getInt("HALLY", 0);
				int diff = Math.abs(hally_back2 - hally_back);
				if( diff < 5){
					error = "Difference to big";
				}
				return null;
			}
		});
		barobot.lightManager.setAllLeds(barobot.main_queue, "22", 255, 0, 100, 200);	
		q.addWait( 500 );
		
		barobot.y.move(q, front, true);
		q.add( new AsyncMessage( true ){
			@Override
			public Queue run(Mainboard dev, Queue queue) {
				int hally_front2 = barobot.state.getInt("HALLY", 0);
				int diff = Math.abs(hally_front2 - hally_back);
				if( diff < 5){
					error = "Difference to big";
				}
				return null;
			}
		});
		barobot.lightManager.setAllLeds(barobot.main_queue, "22", 255, 0, 100, 100);
		q.addWait( 500 );
		q.add( new AsyncMessage( true ){
			@Override
			public Queue run(Mainboard dev, Queue queue) {

				boolean ok = false;
				int diff1 = Math.abs(hally_front - hally_back);
				int diff2 = Math.abs(hally_neutral - hally_front);
				int diff3 = Math.abs(hally_neutral - hally_back);			

				Initiator.logger.w("moveZUp.hally_front", ""+hally_front);
				Initiator.logger.w("moveZUp.hally_neutral", ""+hally_neutral);
				Initiator.logger.w("moveZUp.hally_back", ""+hally_back);

				Initiator.logger.w("moveZUp.diff1", ""+diff1);
				Initiator.logger.w("moveZUp.diff2", ""+diff2);
				Initiator.logger.w("moveZUp.diff3", ""+diff3);

				if( hally_front > sensor_max || hally_neutral > sensor_max || hally_back > sensor_max ){
					showResult(R.string.wizard_hally_nosensor, false );

				}else if(diff1 < 10 && diff2 < 10  && diff3 < 10 ){		// no sensor or servo can't move
					showResult(R.string.wizard_hally_sensor_pernament, false );
				}else if( hally_neutral < neutral_min || hally_neutral > neutral_max ){	// neutral error
					showResult(R.string.wizard_hally_neutral_error, false );

				}else if(  diff1 < minDiff2 && diff2 > minDiff2 && diff3 > minDiff3 ){		// magnets exist but on the same side
					showResult(R.string.wizard_hally_inverted, true );

				}else if( diff1 > minDiff2 && diff2 > minDiff2 && diff3 > minDiff3 ){
					showResult(R.string.wizard_hally_success, true );
					ok = true;

				}else{
					if( diff2 < minDiff2 ){		// front error
						showResult(R.string.wizard_hally_front_error, false );
					}
					if( diff3 < minDiff3 ){		// baack error
						showResult(R.string.wizard_hally_back_error, false );
					}
				}
				if(ok){
					barobot.state.set("HALLY_FRONT", hally_front );
					barobot.state.set("HALLY_BACK", hally_back );
					barobot.state.set("HALLY_NEUTRAL", hally_neutral );
				}
				return null;
			}
		});
		barobot.lightManager.setAllLeds(barobot.main_queue, "22", 255, 0, 255, 0);
	}

	private void showResult(final int text, final boolean success ) {
		runOnUiThread(new Runnable() {
			  public void run() {
				  TextView wizard_hally_results			= (TextView) findViewById(R.id.wizard_hally_results);
				  wizard_hally_results.setText(text);
				  if(success){
					  wizard_hally_results.setTextColor(Color.GREEN);
				  }else{
					  wizard_hally_results.setTextColor(Color.RED);
				  }
			  }
		});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		barobot.lightManager.turnOffLeds(barobot.main_queue);
	}
}
