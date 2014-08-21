package com.denisigo.moments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends FragmentActivity {

	// Request code for add moment activity
	private static final int RC_ADD_MOMENT = 0;

	MainFragment mFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// If we are being created for the first time,
		// create and add fragment
		if (savedInstanceState == null) {
			mFragment = new MainFragment();
			getSupportFragmentManager().beginTransaction()
					.add(R.id.frame, mFragment).commit();
		}
	}

	/**
	 * Open add moment activity
	 */
	private void addMoment() {
		startActivityForResult(new Intent(this, AddMomentActivity.class),
				RC_ADD_MOMENT);
	}

	/**
	 * Handle add moment activity result
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// If moment was successfully added, notify fragment to refresh moments
		if (requestCode == RC_ADD_MOMENT && resultCode == Activity.RESULT_OK) {
			showToast(R.string.moment_has_been_added);
			
			mFragment.onRefreshMoments();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.add_moment:
			addMoment();
			return true;
		case R.id.refresh_moments:
			mFragment.onRefreshMoments();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	/**
	 * Helper method to show toasts
	 * 
	 * @param textResId
	 *            resource id for text
	 * @return Toast instance for any case
	 */
	private Toast showToast(int textResId) {
		Toast toast = Toast.makeText(this, textResId, Toast.LENGTH_SHORT);
		toast.show();

		return toast;
	}
}
