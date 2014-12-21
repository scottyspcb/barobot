package com.barobot.wizard;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.barobot.R;
import com.barobot.android.Android;
import com.barobot.hardware.Arduino;
import com.barobot.hardware.devices.BarobotConnector;
import com.barobot.parser.Queue;
import com.barobot.parser.message.AsyncMessage;
import com.barobot.parser.message.Mainboard;
import com.barobot.parser.utils.Decoder;
import com.barobot.wizard.helpers.CheckboxValueAdapter;
import com.barobot.wizard.helpers.SystemTestItem;
import com.barobot.wizard.helpers.SystemUnitTest;

public class SensorsActivity extends BlankWizardActivity {
	private SystemUnitTest result_list	= new SystemUnitTest();
	private CheckboxValueAdapter kva;
    ListView result_box;
	protected boolean finished	= false;
	protected boolean connected	= false;
	protected boolean refreshPossible = true;
	private TextView wizard_sensors_hint_content;
	private Object lock = new Object();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wizard_sensors);
		result_box		= (ListView) findViewById(R.id.sensor_list);
		wizard_sensors_hint_content	= (TextView) findViewById(R.id.wizard_sensors_hint_content);		
		loadTests();
		kva				= new CheckboxValueAdapter(this, result_list);
		result_box.setAdapter(kva);
		enableTimer( 1000, 2000 );
	}

	@Override
	protected void onResume() {
		super.onResume();
		synchronized( this.lock ){
			kva.notifyDataSetChanged();
		}
	}

	private void sendCommands(BarobotConnector barobot) {
		synchronized( this.lock ){
			Queue q	= new Queue();
			q.add( new AsyncMessage( true ) {
				@Override
				public String getName() {
					return "Check robot response";
				}
				@Override
				public Queue run(Mainboard dev, Queue queue) {
					finished = false;
					return null;
				}
			});
			q.add( "PING", "RPONG" );
			q.add( new AsyncMessage( true ) {
				@Override
				public String getName() {
					return "Check robot response";
				}
				@Override
				public Queue run(Mainboard dev, Queue queue) {
					connected = true;
					finished = false;
					return null;
				}
			});
			barobot.lightManager.setAllLeds(q, "0", 0, 0, 0, 0 );
			barobot.readHardwareRobotId(q);
			q.add("x", true);
			q.add("y", true);
			q.add("z", true);
			q.addWithDefaultReader("S");
			q.add("A0", true);					// hall x
			q.add("A1", true);					// hall y
			q.add("A2", true);					// load cell
			barobot.lightManager.carret_color(q, 255, 255, 255 );
			barobot.lightManager.setAllLeds(q, "ff", 255, 255, 255, 255 );
			q.add( new AsyncMessage( true ) {
				@Override
				public String getName() {
					return "All ok";
				}
				@Override
				public Queue run(Mainboard dev, Queue queue) {
					finished = true;
					SensorsActivity.this.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							kva.notifyDataSetChanged();
						}
					});
					return null;
				}
			});
			barobot.main_queue.add(q);
		}
	}

	public void onTick(){
		// if is connected AND is not busy AND not uploading firmware
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				int hint = result_list.getHintId();
				if(hint >0 ){
					wizard_sensors_hint_content.setText(hint);
				}else{
					wizard_sensors_hint_content.setText(R.string.wizard_sensors_test_completed);
				}
				if( Arduino.getInstance().getConnection().isConnected() ){
					synchronized( lock ){
						if( barobot != null && !barobot.main_queue.isBusy() ){
							refreshPossible	= true;
							sendCommands(barobot);
						}else{
							refreshPossible	= false;
							kva.notifyDataSetChanged();
						}
					}
				}
			}
		});
	}

	public void onOptionsButtonClicked(View view)
	{
		switch(view.getId()){
			case R.id.wizard_sensor_next:
				boolean allOk = result_list.checkAllOk();
				if(allOk){
					goNext();
				}else{
					Android.alertMessage( SensorsActivity.this, "Not all tests are cleared");
				}
				break;
			default:
				super.onOptionsButtonClicked(view);
				break;
		}
	}
	private void loadTests() {
		sendCommands(barobot);
		result_list.put( new SystemTestItem<Boolean>("Barobot is connected"){
			@Override
			public Boolean read() {
				return connected;
            }
		});
		result_list.put( new SystemTestItem<Boolean>("All questions to robot were answered", R.string.wizard_text_sensors_refresh ){
			@Override
			public Boolean read() {
				return finished;
            }
		});
		result_list.put( new SystemTestItem<Boolean>("Command Queue swamped", R.string.wizard_text_sensors_refresh ){
			@Override
			public Boolean read() {
				return barobot.main_queue.isBusy();
            }
			@Override
			public boolean check() {
                return !this.value.booleanValue();
            }
		});
		result_list.put( new SystemTestItem<Boolean>("Auto refreshing is possible", R.string.wizard_text_sensors_refresh){
			@Override
			public Boolean read() {
				return refreshPossible;
            }
			@Override
			public boolean check() {
                return value.booleanValue();
            }
		});

		result_list.put( new SystemTestItem<Long>("Last Barobot-Android handshake (miliseconds ago)"){
			long now = 0;
			@Override
			public Long read() {
				now = Decoder.getTimestamp();
				return now- barobot.lastSeenRobotTimestamp;
            }
			@Override
			public boolean check() {
				return (value < 30000);		// more than 30 s
            }
		});
		result_list.put( new SystemTestItem<Long>("Robots last valid response (miliseconds ago)"){
			long now = 0;
			@Override
			public Long read() {
				now = Decoder.getTimestamp();
				return now- barobot.mb.last_response;
            }
			@Override
			public boolean check() {
				return ( value < 30000);		// more than 30 s
            }
		});			

		result_list.put( new SystemTestItem<Integer>("Barobot ID", R.string.wizard_text_sensors_upload_firmware){
			@Override
			public Integer read() {
				return barobot.state.getInt("ROBOT_ID", 0);
            }
			@Override
			public boolean check() {
                if( this.value > 0){
                	return true;
                }
                return false;
            }
		});
		result_list.put( new SystemTestItem<Integer>("ROBOT FIRMWARE VERSION", R.string.wizard_text_sensors_upload_firmware){
			@Override
			public Integer read() {
				return  barobot.state.getInt("ARDUINO_VERSION", 0);
            }
			@Override
			public boolean check() {
                return (value > 0);
            }
		});
		result_list.put( new SystemTestItem<Integer>("MICROPROCESSOR TEMPERATURE", R.string.wizard_text_sensors_temperature){
			@Override
			public Integer read() {
				return  barobot.state.getInt("TEMPERATURE", 0);
            }
			@Override
			public boolean check() {
				return (value > 0 && value < 130 );
            }
		});
		result_list.put( new SystemTestItem<Integer>("MICROPROCESSOR STARTS"){
			@Override
			public Integer read() {
				return  barobot.state.getInt("ARDUINO_STARTS", 0);
            }
			@Override
			public boolean check() {
				return (value > 0);
            }
		});	
		result_list.put( new SystemTestItem<Integer>("HALL X: left-right sensor", R.string.wizard_text_sensors_hallx ){
			@Override
			public Integer read() {
				return barobot.state.getInt("HALLX", 0);
            }
			@Override
			public boolean check() {
                if( value > 1 && value < 1000 ){
                	return true;
                }
                return false;
            }
		});
		result_list.put( new SystemTestItem<Integer>("HALL Y: front-back sensor", R.string.wizard_text_sensors_hally ){
			@Override
			public Integer read() {
				return barobot.state.getInt("HALLY", 0);
            }
			@Override
			public boolean check() {
                if( value > 1 && value < 1000 ){
                	return true;
                }
                return false;
            }
		});

		result_list.put( new SystemTestItem<Integer>("LOAD CELL - weigh sensor", R.string.wizard_text_sensors_weight ){
			@Override
			public Integer read() {
				return barobot.state.getInt("LAST_WEIGHT", 0);
            }
			@Override
			public boolean check() {
                if( value > 1 && value < 1020 ){
                	return true;
                }
                return false;
            }
		});
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		barobot.lightManager.turnOffLeds(barobot.main_queue);
	}
}
