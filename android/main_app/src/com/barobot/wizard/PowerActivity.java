package com.barobot.wizard;

import java.io.File;

import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.barobot.R;
import com.barobot.common.constant.Constant;
import com.barobot.common.interfaces.serial.Wire;
import com.barobot.gui.database.BarobotData;
import com.barobot.hardware.Arduino;
import com.barobot.hardware.devices.BarobotConnector;
import com.barobot.other.Android;
import com.barobot.wizard.helpers.CheckboxValueAdapter;
import com.barobot.wizard.helpers.SystemTestItem;
import com.barobot.wizard.helpers.SystemUnitTest;

public class PowerActivity extends BlankWizardActivity {
	private SystemUnitTest result_list	= new SystemUnitTest();
	private CheckboxValueAdapter kva;
	private TextView wizard_power_hint_content;
    ListView result_box;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wizard_power);
		result_box					= (ListView) findViewById(R.id.power_list);
		wizard_power_hint_content	= (TextView) findViewById(R.id.wizard_power_hint_content);	
		loadTests();
		kva							= new CheckboxValueAdapter(this, result_list);
		result_box.setAdapter(kva);
		enableTimer( 1000, 2000 );
	}

	@Override
	protected void onResume() {
		super.onResume();		
		kva.notifyDataSetChanged();
	}

	private void loadTests(){
		result_list.put( new SystemTestItem<Boolean>("Arduino driver exists", R.string.wizard_text_power_restart ){
			@Override
			public Boolean read() {
                return Arduino.getInstance() != null;
            }
		});
		result_list.put( new SystemTestItem<Boolean>( R.string.wizard_text_power_arduino, R.string.wizard_text_power_restart ){
			@Override
			public Boolean read() {
				if( Arduino.getInstance() != null ){
					return Arduino.getInstance().barobot != null;
				}else{
					return false;
				}
            }
		});
		result_list.put( new SystemTestItem<Boolean>( R.string.wizard_text_power_queue , R.string.wizard_text_power_restart){
			@Override
			public Boolean read() {
				BarobotConnector barobot	= Arduino.getInstance().barobot;
				return barobot!= null && barobot.main_queue != null;
            }

		});
		result_list.put( new SystemTestItem<Integer>(R.string.wizard_text_android_version){
			@Override
			public Integer read() {
				return Constant.ANDROID_APP_VERSION;
            }
		});
		/*
		result_list.put( new SystemTestItem<Boolean>("JAVA MainBoard exists"){
			@Override
			public Boolean read() {
				BarobotConnector barobot	= getBarobot();
				return barobot!= null && barobot.mb != null;
            }
            @Override
			public int getSolutionId() {		
				 return R.string.wizard_text_power_restart;
            }
		});*/

		if( Arduino.getInstance() != null && Arduino.getInstance().barobot != null ){
			final BarobotConnector barobot	= Arduino.getInstance().barobot;
			final Wire connection			= Arduino.getInstance().getConnection();
			result_list.put( new SystemTestItem<Boolean>("Connection exists", R.string.wizard_text_replug){
				@Override
				public Boolean read() {
					return connection != null;
	            }
			});

			if( connection != null ){
				/*
				result_list.put( new SystemTestItem<String>("Connection Provider"){
					@Override
					public String read() {
						return connection.getName();
		            }
					@Override
					public boolean check() {
						String val2 = value;
						if( val2.equals("")){
							return false;
						}
		                return true;
		            }
				});
				result_list.put( new SystemTestItem<Boolean>("USB drivers started"){
					@Override
					public Boolean read() {
						return connection.canConnect();
		            }
					@Override
					public boolean check() {
		                return this.value;
		            }
				});	*/
				result_list.put( new SystemTestItem<Boolean>("USB is connected", R.string.wizard_text_replug){
					@Override
					public Boolean read() {
						return connection.isConnected();
		            }
					@Override
					public boolean check() {
		                return this.value.booleanValue();
		            }
				});
			}

			result_list.put( new SystemTestItem<Boolean>("DC power is plugged", R.string.wizard_text_plug_dc){
				@Override
				public Boolean read() {
					return barobot.state.getInt("DC_PLUGGED", 0) > 0;
	            }
			});

			result_list.put( new SystemTestItem<Boolean>("Robot was connected", R.string.wizard_text_turn_on ){
				@Override
				public Boolean read() {
					return barobot.robot_id_ready;
	            }
				@Override
				public boolean check() {
	                return this.value.booleanValue();
	            }
			});

			result_list.put( new SystemTestItem<Boolean>("Tablet is online (for software updates)", R.string.wizard_text_connect_wifi ){
				@Override
				public Boolean read() {
					return Android.isOnline(PowerActivity.this) > 0;
	            }
				@Override
				public boolean check() {
	                return this.value.booleanValue();
	            }
			});
			result_list.put( new SystemTestItem<String>("Internet Adress"){
				@Override
				public String read() {
					return Android.getLocalIpAddress();
	            }
				@Override
				public boolean check() {
					return !value.equals("");
	            }
			});
			/*
			result_list.put( new SystemTestItem<Boolean>("Queue is busy"){
				@Override
				public Boolean read() {
					return barobot.main_queue.isBusy();
	            }
				@Override
				public boolean check() {
	                return !this.value.booleanValue();
	            }
			});
			result_list.put( new SystemTestItem<Boolean>("Barobot is available"){
				@Override
				public Boolean read() {
					return barobot.isAvailable();
	            }
				@Override
				public boolean check() {
	                return this.value.booleanValue();
	            }
			});*/
			result_list.put( new SystemTestItem<Boolean>("Application folder exists", R.string.wizard_text_power_restart ){
				@Override
				public Boolean read() {
					File dir				= new File(Environment.getExternalStorageDirectory(), "Barobot");
					return dir.exists();
	            }
				@Override
				public boolean check() {
	                return this.value.booleanValue();
	            }
			});
			/*
			result_list.put( new SystemTestItem<Boolean>("Database folder exists"){
				@Override
				public Boolean read() {
					try {
						String appPath2				= PowerActivity.this.getPackageManager().getPackageInfo(PowerActivity.this.getPackageName(), 0).applicationInfo.dataDir;
						String dbFolderPath 		= appPath2+"/databases";
						File fDbFolderPath			= new File(dbFolderPath);
						return fDbFolderPath.exists();		
					} catch (NameNotFoundException e) {
					}
					return false;
	            }
				@Override
				public boolean check() {
	                return this.value.booleanValue();
	            }
			});	*/
			result_list.put( new SystemTestItem<Boolean>("Drink database exists", R.string.wizard_text_power_restart ){
				@Override
				public Boolean read() {
					try {
						String appPath2				= PowerActivity.this.getPackageManager().getPackageInfo(PowerActivity.this.getPackageName(), 0).applicationInfo.dataDir;
						String dbFolderPath 		= appPath2+"/databases";
						String dbFilePath 			= dbFolderPath + "/" + BarobotData.DATABASE_NAME;
						File fDbFilePath			= new File(dbFilePath);
						return fDbFilePath.exists();	
					} catch (NameNotFoundException e) {
					}
					return false;
	            }
				@Override
				public boolean check() {
	                return this.value.booleanValue();
	            }
			});	
	/*
			int TABLET_TEMPERATURE		= barobot.state.getInt("TABLET_TEMPERATURE", 0);
			float cpuUsage				= Android.readCpuUsage();
			int[] memUsage				= Android.readMemUsage();
			boolean use_beta			= barobot.use_beta
*/
		}
	}

	public void onOptionsButtonClicked(View view)
	{
		switch(view.getId()){
			case R.id.wizard_power_next:
				boolean allOk = result_list.checkAllOk();
				if(allOk){
					goNext();
				}else{
					Android.alertMessage( PowerActivity.this, "Not all tests are cleared");
				}
				break;
			default:
				super.onOptionsButtonClicked(view);
				break;
		}
	}

	public void onTick(){
		PowerActivity.this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				int hint = result_list.getHintId();
				if(hint >0 ){
					wizard_power_hint_content.setText(hint);
				}else{
					wizard_power_hint_content.setText(R.string.wizard_power_test_completed);
				}
				kva.notifyDataSetChanged();
			}
		});
	}
}
