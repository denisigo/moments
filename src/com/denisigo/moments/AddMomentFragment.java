package com.denisigo.moments;

import java.io.IOException;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.denisigo.moments.api.Api;

/**
 * Fragment responsible for handling all the stuff of add moment dialog.
 */
public class AddMomentFragment extends Fragment {
	
	private static final String LOG_TAG = AddMomentFragment.class.getSimpleName();

	EditText mEtAuthorName;
	EditText mEtText;
	Button mBtAddMomentButton;

	// Add moment task
	AsyncTask<Moment, ?, ?> mTask;

	/**
	 * IMPORTANT: there is a possibility when the AsyncTask is completed in
	 * between of fragment's detach/attach, so in this case mListener will be
	 * null and it never be notified about the task is completed. This situation
	 * is almost impossible, and I'm leaving handling it up to you ;)
	 */

	// On fragment events listener (usually host activity)
	OnMomentAddedListener mListener;

	/**
	 * Interface to communicate with the host activity.
	 */
	public interface OnMomentAddedListener {
		public void onMomentAdded();
	}

	/**
	 * When fragment is about to be created
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// We don't want the fragment to be killed
		// when activity is recreated
		setRetainInstance(true);
	}

	/**
	 * When fragment is about to be destroyed
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();

		// Trying to cancel running async task if fragment is about
		// to be destroyed - say, when user clicks "back" button
		if (mTask != null) {
			mTask.cancel(true);
			mTask = null;
		}
	}

	/**
	 * When fragment is attached to the activity
	 */
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		// Set the host activity as a listener
		mListener = (OnMomentAddedListener) activity;
	}

	/**
	 * When fragment is detached from the activity
	 */
	@Override
	public void onDetach() {
		super.onDetach();
		// Clear listener since host activity is no more available
		mListener = null;
	}

	/**
	 * When fragment's view is about to be created
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_add_moment, container,
				false);

		mEtAuthorName = (EditText) view.findViewById(R.id.authorName);
		mEtText = (EditText) view.findViewById(R.id.text);
		mBtAddMomentButton = (Button) view.findViewById(R.id.addMomentButton);
		mBtAddMomentButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				onAddMomentButtonClick();
			}
		});

		updateUi();

		return view;
	}

	/**
	 * Handler for Add moment button click
	 * 
	 * @param view
	 */
	private void onAddMomentButtonClick() {

		String authorName = mEtAuthorName.getText().toString().trim();
		String text = mEtText.getText().toString().trim();

		// We allow author name to be empty (anonymous),
		// but not allow text to be empty
		if (text.isEmpty()) {
			mEtText.setError(getString(R.string.text_is_empty));
			return;
		}

		// If everything is ok, create Moment object
		Moment moment = new Moment(text, authorName);
		// and pass it to the AsyncTask to be added asynchronously
		mTask = new AddMomentTask();
		mTask.execute(moment);
	}

	/**
	 * Updates UI
	 */
	private void updateUi() {
		// There could be no view available if updateUi is called
		// between destroyView/createView (view is being destroyed even
		// if setRetainInstance is set to true)
		if (getView() == null)
			return;

		if (mTask == null) {
			mBtAddMomentButton.setEnabled(true);
			mBtAddMomentButton.setText(R.string.add_moment);
		} else {
			mBtAddMomentButton.setEnabled(false);
			mBtAddMomentButton.setText(R.string.adding_moment);
		}
	}

	/**
	 * AsyncTask for asynchronous adding moments.
	 */
	private class AddMomentTask extends AsyncTask<Moment, Void, Boolean> {

		private Exception e;

		/**
		 * Executed in UI thread before doInBackground.
		 */
		@Override
		protected void onPreExecute() {
			updateUi();
		}

		/**
		 * Executed in worker thread.
		 */
		@Override
		protected Boolean doInBackground(Moment... args) {

			// Instantiate Api object
			Api api = new Api();

			Moment moment = args[0];
			boolean result = true;

			try {
				// Do the Api request
				api.postMoment(moment);
			} catch (IOException e) {
				this.e = e;
				result = false;
			}

			// Free Api
			api.close();
			api = null;

			return result;
		}

		/**
		 * Executed in UI thread after doInBackground.
		 */
		@Override
		protected void onPostExecute(Boolean result) {
			mTask = null;
			updateUi();

			// If success, show the toast and notify activity
			if (result) {
				// Notify the activity that we're done.
				if (mListener != null)
					mListener.onMomentAdded();

			} else {
				Log.e(LOG_TAG, e.toString());
				
				showConnectionErrorDialog();
			}
		}
	}
	
	private void showConnectionErrorDialog(){
		(new ConnectionErrorDialog()).show(getActivity().getSupportFragmentManager(),
				"ddd");
	}
}
