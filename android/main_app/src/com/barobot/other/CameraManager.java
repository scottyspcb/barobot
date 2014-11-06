package com.barobot.other;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.util.Log;
import android.widget.Toast;
import com.barobot.common.Initiator;

public class CameraManager {
	private Camera front_camera;
	private Camera back_camera;
	private int front_cameraId = -1;
	private int back_cameraId = -1;
	Activity bm = null;

	public CameraManager(Activity main) {
		front_cameraId = findCamera( CameraInfo.CAMERA_FACING_FRONT );
	//	back_cameraId = findCamera( CameraInfo.CAMERA_FACING_BACK );
		bm = main;
	}
	public synchronized void doPhoto() {
		if(front_camera!=null){
			front_camera.takePicture(null, null, new PhotoHandler(bm.getApplicationContext(), 1 ));
		}else{
		}
		if(back_camera!=null){
			back_camera.takePicture(null, null, new PhotoHandler(bm.getApplicationContext(), 2) );
		}else{
		}
	}
	public void findCameras() {		// do we have a camera?
		boolean aa = bm.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
		if(!aa) {
	    	Toast.makeText(bm, "No camera on this device", Toast.LENGTH_LONG).show();
	    } else {
	    	Log.d("+front_cameraId", "+" + front_cameraId);

			if (front_cameraId >= -1) {
				try {
					front_camera = Camera.open(front_cameraId);
				} catch (Exception e) {
					Initiator.logger.appendError(e);
				}
			}
			Log.d("+findCameras", "+bb " + back_cameraId);

			if (back_cameraId != -1) {
				back_camera = Camera.open(back_cameraId);
				Log.d("+findCameras", "+bbopen " + back_cameraId);
			}
			if (front_cameraId == -1 && back_cameraId == -1 ) {
				Toast.makeText(bm, "No  camera found.",Toast.LENGTH_LONG).show();
			}
	    }
	  }
	private int findCamera(int cameraFacing) {
	    int numberOfCameras = Camera.getNumberOfCameras();	    // Search for the front facing camera
	    for (int i = 0; i < numberOfCameras; i++) {
	      CameraInfo info = new CameraInfo();
	      Camera.getCameraInfo(i, info);
	      if (info.facing == cameraFacing) {
	    	  return i;
	      }
	    }
	    return -1;
	  }

	public void onDestroy() {
		if (front_camera != null) {
			front_camera.stopPreview();
			front_camera.setPreviewCallback(null);
			front_camera.release();
			front_camera = null;
		}
		if (back_camera != null) {
			back_camera.stopPreview();
			back_camera.setPreviewCallback(null);
			back_camera.release();
			back_camera = null;
		}
	}
}
