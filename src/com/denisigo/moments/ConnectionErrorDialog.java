package com.denisigo.moments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

/**
 * No connection alert dialog
 */
public class ConnectionErrorDialog extends DialogFragment {
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.connection_error)
				.setMessage(R.string.connection_error_message)
				.setNegativeButton(R.string.back,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int id) {
								dismiss();
							}
						})
				.setPositiveButton(R.string.open_settings,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int id) {
								dismiss();
								startActivity(new Intent(
										android.provider.Settings.ACTION_SETTINGS));
							}
						});
		;
		return builder.create();
	}
}