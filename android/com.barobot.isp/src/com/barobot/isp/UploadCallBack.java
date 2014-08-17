package com.barobot.isp;

import com.barobot.common.interfaces.serial.IspCommunicator;
import com.barobot.isp.enums.UploadErrors;
public interface UploadCallBack{
    void onPreUpload();
    void onUploading(int value);
    void onPostUpload(boolean success);
    void onCancel();
    void onError(UploadErrors err);
	void resetDevice(boolean reset, IspCommunicator mComm );
}