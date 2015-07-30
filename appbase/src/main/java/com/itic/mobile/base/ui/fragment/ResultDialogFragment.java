package com.itic.mobile.base.ui.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class ResultDialogFragment extends DialogFragment {
	
	public static final String MSG = "MSG";
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(getArguments().getString(MSG)).setPositiveButton("确定", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				
			}
		});
		return builder.create();
	}
	
	public static ResultDialogFragment newInstance(String dMsg){
		ResultDialogFragment fragment = new ResultDialogFragment();
		Bundle bundle = new Bundle();
		bundle.putString(MSG, dMsg);		
		fragment.setArguments(bundle);
		return fragment;	
	}
}
