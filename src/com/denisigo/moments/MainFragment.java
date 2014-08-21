package com.denisigo.moments;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.denisigo.moments.api.Api;
import com.denisigo.moments.api.Moments;

/**
 * Main fragment which handles moments list
 *
 */
public class MainFragment extends Fragment {
	
	private static final String LOG_TAG = MainFragment.class.getSimpleName();

	// Amount of moments loaded at once
	private static final int LIMIT = 10;

	MomentsAdapter mAdapter;
	List<Moment> mMoments;
	ListView mLvMoments;

	// Cursor to load new bunch of moments from
	private String mCursor;
	// If there are more moments on the server?
	private boolean mIsMore = true;
	// Special moment that is being inserted at the top or bottom
	// of the list and is interpreted as a loader item by the adapter
	// (this is the most optimal approach I've came up with =))
	private Moment mLoader;

	// Add moment task
	AsyncTask<GetMomentsTaskArgs, ?, ?> mTask;

	/**
	 * When fragment is about to be created
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// We don't want the fragment to be killed
		// when activity is recreated
		setRetainInstance(true);

		// Create special loader moment
		mLoader = new Moment("[[LOADER]]", "");

		// Create list for moments and ListView adapter
		mMoments = new ArrayList<Moment>(0);
		mAdapter = new MomentsAdapter();

		mTask = null;

		// Start loading first moments
		loadMoments();
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
	 * When fragment's view is about to be created
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_main, container, false);

		mLvMoments = (ListView) view.findViewById(R.id.moments);
		mLvMoments.setAdapter(mAdapter);

		// Set onScroll listener to be able to load more messages when
		// the user reaches the bottom of the list
		mLvMoments.setOnScrollListener(new AbsListView.OnScrollListener() {
			@Override
			public void onScroll(AbsListView lw, final int firstVisibleItem,
					final int visibleItemCount, final int totalItemCount) {
				if (totalItemCount > 0
						&& (firstVisibleItem + visibleItemCount) == totalItemCount) {
					loadMoments();
				}
			}

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
			}
		});

		return view;
	}

	/**
	 * Add special loader moment at the bottom of the list
	 */
	private void showLoaderAtTheBottom() {
		mMoments.add(mLoader);
		mAdapter.notifyDataSetChanged();
	}

	/**
	 * Add special loader moment at the top of the list
	 */
	private void showLoaderAtTheTop() {
		mMoments.add(0, mLoader);
		mAdapter.notifyDataSetChanged();
	}

	/**
	 * Remove loader of any type
	 */
	private void hideLoader() {
		mMoments.remove(mLoader);
		mAdapter.notifyDataSetChanged();
	}

	/**
	 * Loads older moments using current cursor
	 */
	private void loadMoments() {

		// There is not sense to load if there is nothing
		if (!mIsMore)
			return;

		// Don't launch another task if there is one running
		if (mTask != null)
			return;

		// launch the task with current cursor and limit
		mTask = new GetMomentsTask();
		mTask.execute(new GetMomentsTaskArgs(mCursor, LIMIT));
	}

	/**
	 * Loads new moments using added time of the most recent moment we have
	 */
	private void loadNewMoments() {
		if (mMoments.size() == 0)
			return;

		// Don't launch another task if there is one running
		if (mTask != null)
			return;

		// launch the task with the added time of the newest moment we have
		mTask = new GetNewMomentsTask();
		mTask.execute(new GetMomentsTaskArgs(mMoments.get(0).getAdded(), LIMIT));
	}

	/**
	 * Callback method from the host activity to refresh moments
	 */
	public void onRefreshMoments() {
		if (mMoments.size() == 0)
			loadMoments();
		else
			loadNewMoments();
	}

	/**
	 * Our own adapter for the ListView. We need it in order to use custom item
	 * views
	 */
	private class MomentsAdapter extends BaseAdapter {

		private LayoutInflater mInflater;

		public MomentsAdapter() {
			mInflater = getActivity().getLayoutInflater();
		}

		@Override
		public int getCount() {
			return mMoments.size();
		}

		@Override
		public Moment getItem(int position) {
			return mMoments.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		/**
		 * Generate view for the item
		 */
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final Moment item = getItem(position);

			if (item == mLoader) {
				convertView = mInflater.inflate(R.layout.listitem_loader,
						parent, false);

			} else {
				// It is good practice to use convertView to save resources,
				// but we must check whether passed convertView is matching
				// the type of our current item. If there is no convertView was
				// passed or it's type doesn't match, we should inflate a new
				// one
				if (convertView == null
						|| convertView.getId() != R.layout.listitem_moment)
					convertView = mInflater.inflate(R.layout.listitem_moment,
							parent, false);

				((TextView) convertView.findViewById(R.id.text)).setText(item
						.getText());

				// We're getting date from server with UTC+00 time zone, so will
				// display appropriately.
				// We'll add time zone support someday...
				String added = new SimpleDateFormat("yyyy-MM-dd HH:mm")
						.format(item.getAdded());
				String authorName = item.getAuthorName();
				if (authorName == null)
					authorName = "Anonymous";
				String info = "Posted on " + added + " by " + authorName;

				((TextView) convertView.findViewById(R.id.info)).setText(info);
			}

			return convertView;
		}
	}

	/**
	 * Arguments object to be passed to the GetMomentsTask
	 */
	private class GetMomentsTaskArgs {
		public String cursor;
		public int limit;
		public Date from_time;

		public GetMomentsTaskArgs(String cursor, int limit) {
			this.cursor = cursor;
			this.limit = limit;
		}

		public GetMomentsTaskArgs(Date from_time, int limit) {
			this.from_time = from_time;
			this.limit = limit;
		}
	}

	/**
	 * AsyncTask for asynchronous getting moments.
	 */
	private class GetMomentsTask extends
			AsyncTask<GetMomentsTaskArgs, Void, Moments> {

		private Exception e;

		/**
		 * Executed in UI thread before doInBackground.
		 */
		@Override
		protected void onPreExecute() {
			showLoaderAtTheBottom();
		}

		/**
		 * Executed in worker thread.
		 */
		@Override
		protected Moments doInBackground(GetMomentsTaskArgs... args) {

			// Instantiate Api object
			Api api = new Api();

			GetMomentsTaskArgs arg = args[0];
			Moments result = null;

			try {
				// Do the Api request
				result = api.getMoments(arg.cursor, arg.limit);
			} catch (IOException e) {
				this.e = e;
				Log.e(LOG_TAG, e.toString());
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
		protected void onPostExecute(Moments result) {
			mTask = null;

			hideLoader();

			if (e == null && result != null) {
				mCursor = result.getCursor();
				mIsMore = result.isMore();
				if (result.getMoments().length > 0) {
					// Add new moments at the bottom and notify adapter
					mMoments.addAll(Arrays.asList(result.getMoments()));
					mAdapter.notifyDataSetChanged();
				}
			} else {
				showConnectionErrorDialog();
			}
		}
	}

	/**
	 * AsyncTask for asynchronous getting new moments.
	 */
	private class GetNewMomentsTask extends
			AsyncTask<GetMomentsTaskArgs, Void, Moments> {

		private Exception e;

		/**
		 * Executed in UI thread before doInBackground.
		 */
		@Override
		protected void onPreExecute() {
			showLoaderAtTheTop();
		}

		/**
		 * Executed in worker thread.
		 */
		@Override
		protected Moments doInBackground(GetMomentsTaskArgs... args) {

			// Instantiate Api object
			Api api = new Api();

			GetMomentsTaskArgs arg = args[0];
			Moments result = null;

			try {
				// Do the Api request
				result = api.getMoments(arg.from_time, arg.limit);
			} catch (IOException e) {
				this.e = e;
				Log.e(LOG_TAG, e.toString());
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
		protected void onPostExecute(Moments result) {
			mTask = null;

			hideLoader();

			if (e == null && result != null) {
				if (result.getMoments().length > 0) {
					// Add new moments at the top and notify adapter
					mMoments.addAll(0, Arrays.asList(result.getMoments()));
					mAdapter.notifyDataSetChanged();
				}
			} else {
				showConnectionErrorDialog();
			}
		}
	}
	
	private void showConnectionErrorDialog(){
		(new ConnectionErrorDialog()).show(getActivity().getSupportFragmentManager(),
				"ddd");
	}
}
