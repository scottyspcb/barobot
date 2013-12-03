package com.barobot.gui;

import com.barobot.gui.NoticeDialogListener.ReturnStatus;
import com.barobot.gui.dataobjects.Bottle;
import com.barobot.gui.dataobjects.Engine;
import com.barobot.gui.dataobjects.Liquid;
import com.barobot.R;
import com.barobot.R.id;
import com.barobot.R.layout;
import com.barobot.R.string;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Looper;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.view.LayoutInflater;
import android.view.View;

public class SelectLiquidDialogFragment extends DialogFragment {

	public boolean ShowEmptyButton = true;
	public boolean ShowAddButton = true;
	public static SelectLiquidDialogFragment newInstance()
	{
		SelectLiquidDialogFragment instance = new SelectLiquidDialogFragment();
		
		return instance;
	}
	
	private NoticeDialogListener mListener;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mListener = (NoticeDialogListener) activity;
	}



	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		final ArrayAdapter<Liquid> arrayAdapter = new ArrayAdapter<Liquid>(getActivity(), android.R.layout.select_dialog_singlechoice);
		arrayAdapter.addAll(Engine.GetInstance(getActivity()).getAllLiquids());

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		builder.setTitle(R.string.liquid_select_title);
		builder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				Liquid selectedLiquid = arrayAdapter.getItem(which);

				//Engine.GetInstance(getActivity()).UpdateBottleSlot(currentBottle, new Bottle(selectedLiquid, 0));
				mListener.onDialogEnd(SelectLiquidDialogFragment.this, ReturnStatus.OK, selectedLiquid);
			}
		});
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

				mListener.onDialogEnd(SelectLiquidDialogFragment.this, ReturnStatus.Canceled, null);
			}
		});
		if (ShowEmptyButton) {
			builder.setNeutralButton("Empty slot", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					//Engine.GetInstance(getActivity()).UpdateBottleSlot(currentBottle, null);
					mListener.onDialogEnd(SelectLiquidDialogFragment.this, ReturnStatus.OK, null);
				}
			});
		}
		if (ShowAddButton) {
			builder.setPositiveButton("Add Bottle", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

					LayoutInflater inflater = getActivity().getLayoutInflater();
					final View dialogView = inflater.inflate(R.layout.dialog_add_liquid, null); 

					builder.setView(dialogView)
					.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							mListener.onDialogEnd(SelectLiquidDialogFragment.this, ReturnStatus.Canceled, null);
						}
					})
					.setPositiveButton("Add", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {

							TextView NameView = (TextView) dialogView.findViewById(R.id.liquid_name);
							String name = NameView.getText().toString();

							TextView TypeView = (TextView) dialogView.findViewById(R.id.liquid_type);
							String type = TypeView.getText().toString();

							Liquid liquid = new Liquid(type, name, 0);

							//Engine engine = Engine.GetInstance(getActivity());
							//liquid.id = engine.AddLiquid(liquid);

							//engine.UpdateBottleSlot(currentBottle, new Bottle(liquid, 0));

							mListener.onDialogEnd(SelectLiquidDialogFragment.this, ReturnStatus.NewLiquid, liquid);
						}
					});
					AlertDialog ad = builder.create();
					ad.show();
				}
			});
		}

		return builder.create();
	}
}
