package com.denisigo.moments.api;

import com.denisigo.moments.Moment;

/*
 * Container for getMoments result.
 */
public class Moments {
	private Moment[] mMoments;
	private boolean mIsMore;
	private String mCursor;

	public Moments(Moment[] moments, boolean isMore, String cursor) {
		this.setMoments(moments);
		this.setCursor(cursor);
		this.setIsMore(isMore);
	}

	public Moment[] getMoments() {
		return mMoments;
	}

	private void setMoments(Moment[] moments) {
		mMoments = moments;
	}

	public String getCursor() {
		return mCursor;
	}

	private void setCursor(String cursor) {
		mCursor = cursor;
	}
	
	public boolean isMore() {
		return mIsMore;
	}

	private void setIsMore(boolean mIsMore) {
		this.mIsMore = mIsMore;
	}
}
