package com.barobot.gui;

import com.barobot.gui.dataobjects.Liquid;

import android.app.DialogFragment;

public interface NoticeDialogListener {
	public enum ReturnStatus
	{
		OK,
		Canceled,
		NewLiquid
	}

	public void onDialogEnd(DialogFragment dialog, ReturnStatus status, Liquid liquid, int volume);
}
