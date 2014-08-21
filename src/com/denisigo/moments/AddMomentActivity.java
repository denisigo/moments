package com.denisigo.moments;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

/**
 * Host activity for add moment dialog.
 */
public class AddMomentActivity extends FragmentActivity implements
		AddMomentFragment.OnMomentAddedListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_moment);

		// If we are being created for the first time,
		// create and add fragment
		if (savedInstanceState == null) {
			AddMomentFragment fragment = new AddMomentFragment();
			getSupportFragmentManager().beginTransaction()
					.add(R.id.frame, fragment).commit();
		}
	}

	/**
	 * Fragment callback. If moment was added successfully, close activity with OK result
	 */
	@Override
	public void onMomentAdded() {
		setResult(RESULT_OK);
		finish();
	}
}
