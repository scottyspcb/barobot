package com.barobot.isp;

import com.barobot.common.Initiator;
import com.barobot.common.interfaces.CanLog;
import com.barobot.common.interfaces.serial.IspCommunicator;
import com.barobot.isp.enums.Board;
import com.barobot.isp.enums.UploadErrors;
import com.barobot.isp.programmer.AvrConf;
import com.barobot.isp.programmer.AvrMem;
import com.barobot.isp.programmer.IntelHexFileToBuf;
import com.barobot.isp.programmer.Stk500;
import com.barobot.isp.programmer.Stk500V2;
import com.barobot.isp.programmer.UploadProtocol;
import com.barobot.isp.programmer.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public final class Uploader {
    public static final String TAG = Uploader.class.getSimpleName();
    private static final boolean DEBUG_SHOW = true;
    private static final boolean DEBUG_SHOW_HEXDUMP = true;
    private UploadProtocol      mProg;
    private IntelHexFileToBuf   mIntelHex;
    private AvrConf             mAVRConf;
    private AvrMem              mAVRMem;
    private Board				mBoard;
    private Thread				mUploadThread;
    private UploadCallBack		mCallBack;
    private InputStream			mFileStream;
    private static final Object LOCK = new Object();
    protected IspCommunicator mSerial;
	private CanLog logger;

    public void upload() throws RuntimeException {
        if (mCallBack == null) {
        	return;
        }
        mUploadThread = new UploadThread();
        mUploadThread.start();
    }
	public void setBoard(Board mSelectedBoard) {
        mBoard      = mSelectedBoard;
	}
	public void setHex(InputStream fileStream) {
		mFileStream = fileStream;
	}
	public void setHex( String filePath) {
	    if (mCallBack == null) {
	    	return;
	    }
        if(filePath == null) {
            mCallBack.onError(UploadErrors.FILE_OPEN);
            return;
        }
        File file = new File(filePath);
        if(!file.exists() || !file.isFile() || !file.canRead()) {
            mCallBack.onError(UploadErrors.FILE_OPEN);
            return;
        }
        InputStream is;
        try {
            is = new FileInputStream(filePath);
        } catch(Exception e) {
            mCallBack.onError(UploadErrors.FILE_OPEN); 
            return;
        }
        this.mFileStream = is;
	}
	public void setCallBack(UploadCallBack mUploadCallback) {
		this.mCallBack   = mUploadCallback;
	}
	public void setSerial(IspCommunicator mSerial) {
		this.mSerial = mSerial;
	}
    public boolean close() throws RuntimeException {
        synchronized (LOCK) {
            if(mSerial == null) return true;
            if(mSerial.close()) {
                mSerial = null;
                return true;
            } else {
                return false;
            }
        }
    }
	private boolean uploadCode() {
        if(this.mBoard == null) {
            this.mCallBack.onError(UploadErrors.AVR_CHIPTYPE);
            return false;
        }
        boolean ret = false;
        this.mCallBack.onPreUpload();
        ret =  uploadCode2();
        this.mCallBack.onPostUpload(ret);
        return ret;
    }
    public void cancelUpload() {
        if(this.mUploadThread != null){
        	this.mUploadThread.interrupt();
        }
    }
    public boolean uploadCode2() {
    	logger = Initiator.logger;

        if (mBoard.uploadProtocol == Board.UploadProtocols.STK500) {
            mProg = new Stk500( logger );
        } else if(mBoard.uploadProtocol == Board.UploadProtocols.STK500V2) {
            mProg = new Stk500V2( logger );
        } else {
        	mCallBack.onError(UploadErrors.AVR_CHIPTYPE);
            return false;
        }

        mProg.setSerial(mSerial);
        mProg.setCallback(mCallBack);

        try {
            mAVRConf = new AvrConf(mBoard);
            mAVRMem = new AvrMem(mAVRConf);
        } catch(Exception e) {
            mCallBack.onError(UploadErrors.AVR_CHIPTYPE);
            return false;
        }
        try {
            getFileToBuf(mFileStream);
        } catch(FileNotFoundException  e) {
            mCallBack.onError(UploadErrors.HEX_FILE_OPEN1);
        } catch(IOException e) {   
            mCallBack.onError(UploadErrors.HEX_FILE_OPEN2);
        } catch(Exception e) {
            mCallBack.onError(UploadErrors.HEX_FILE_OPEN3);
            return false;
        }
        mProg.setConfig(mAVRConf, mAVRMem);
        int res = mProg.open();
        if(res == -1){
        	return false;
        }
        int initOK = mProg.initialize();
        if(initOK < 0) {
        	Initiator.logger.e(TAG,"initialization failed ("+initOK+")");
            mCallBack.onError(UploadErrors.CHIP_INIT);
            return false;
        }
        int sigOK = mProg.check_sig_bytes();
        if( sigOK != 0) {
        	Initiator.logger.e(TAG,"check signature failed ("+sigOK+")");
            mCallBack.onError(UploadErrors.SIGNATURE);
            return false;
        }
        int writeOK = mProg.paged_write();
        if(writeOK == 0) { return false; } // canceled
        if(writeOK < 0) {
        	Initiator.logger.e(TAG,"paged write failed ("+initOK+")");
            mCallBack.onError(UploadErrors.PAGE_WRITE);
            return false;
        }
        mProg.disable();
        return true;
    }

    private void getFileToBuf(InputStream hexFile) throws FileNotFoundException, IOException, Exception {
        mIntelHex = new IntelHexFileToBuf();
        mIntelHex.parse(hexFile);
        long byteLength = mIntelHex.getByteLength();
        mAVRMem.buf = new byte[(int)byteLength];
        mIntelHex.getHexData(mAVRMem.buf);
        mIntelHex = null;
        if(DEBUG_SHOW_HEXDUMP) {
        	Utils.dumpHex( byteLength, mAVRMem.buf );
        }
    }
    
   private class UploadThread extends Thread {
        public void run() {
            synchronized (LOCK) {
                if(mSerial.isConnected()){
                } else {
                	 if(!mSerial.open()) {
                         if(DEBUG_SHOW) { 
                        	 Initiator.logger.d(TAG, "upload : cannot mSerial.open"); 
                         }
                         return;
                     }
                     if(DEBUG_SHOW) { 
                    	 Initiator.logger.d(TAG, "upload : open successful");
                     }
                }
                mSerial.clearBuffer();
                uploadCode();
                mSerial.clearBuffer();
            }
		}
    } 
}

