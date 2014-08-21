package com.denisigo.moments.api;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.denisigo.moments.Moment;

/**
 * API client for accessing API server.
 */
public class Api {

	// Base URL for our API endpoints.
	private static final String API_URL = "https://moments-application.appspot.com/api/v1/";

	private ApiFetcher mApiFetcher;

	public Api() {
		// Instantiate API fetcher
		mApiFetcher = new ApiFetcher();
	}

	/**
	 * Destructor. Should be called when API client is no more needed.
	 */
	public void close() {
		mApiFetcher = null;
	}

	/**
	 * Posts a new moment to the API. Synchronous! Should be wrapped with
	 * AsyncTask or so.
	 * 
	 * @param moment
	 *            Moment instance
	 * @throws IOException
	 */
	public void postMoment(Moment moment) throws IOException {

		// Generate API endpoint URL
		String urlstring = API_URL + "moments";

		URL url = null;
		try {
			url = new URL(urlstring);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		// Prepare JSON data string
		JSONObject data = new JSONObject();
		try {
			data.put("text", moment.getText());
			data.put("author_name", moment.getAuthorName());
		} catch (JSONException e) {
			e.printStackTrace();
		}

		// Fetch the API endpoint
		ApiFetcher.Result result = mApiFetcher.post(url, data.toString());

		// Handle possible error response
		handleError(result);
	}

	/**
	 * Gets moments from cursor with some limit. Synchronous implementation,
	 * should be wrapped with AsyncTask or so.
	 * 
	 * @param cursor
	 *            String for cursor
	 * @param limit
	 *            int for limit of moments
	 * @return Moments collection instance
	 * @throws IOException
	 */
	public Moments getMoments(String cursor, int limit) throws IOException {

		// Generate API endpoint URL
		String urlstring = API_URL + "moments" + "?";
		if (cursor != null)
			urlstring += "&cursor=" + cursor;
		urlstring += "&limit=" + Integer.toString(limit);

		URL url = null;
		try {
			url = new URL(urlstring);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		return getMoments(url);
	}
	
	/**
	 * Gets moments posted after some time with some limit. Synchronous implementation,
	 * should be wrapped with AsyncTask or so.
	 * 
	 * @param fromTime
	 *            Date instance
	 * @param limit
	 *            int for limit of moments
	 * @return Moments collection instance
	 * @throws IOException
	 */
	public Moments getMoments(Date fromTime, int limit) throws IOException {

		// Convert date to kind of ISO 8601 string
		String _fromTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
		.format(fromTime);
		
		// Generate API endpoint URL
		String urlstring = API_URL + "moments" + "?";
		urlstring += "&from_time=" + _fromTime;
		urlstring += "&limit=" + Integer.toString(limit);

		URL url = null;
		try {
			url = new URL(urlstring);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		return getMoments(url);
	}
	
	
	/**
	 * Universal method for getting Moments by any parameters
	 * 
	 * @param url URL instance for API call
	 * @return Moments instance
	 * @throws IOException
	 */
	private Moments getMoments(URL url) throws IOException {

		// Fetch the API endpoint
		ApiFetcher.Result result = mApiFetcher.get(url);

		// Handle possible error response
		handleError(result);

		// Parse result as JSON and create our Moment objects
		JSONObject jObject;
		try {
			jObject = new JSONObject(result.getContent());
			String new_cursor = jObject.getString("cursor");

			if (new_cursor == "null")
				new_cursor = null;

			JSONArray jMoments = jObject.getJSONArray("moments");
			Moment[] moments = new Moment[jMoments.length()];

			for (int i = 0; i < jMoments.length(); i++) {
				JSONObject jMoment = jMoments.getJSONObject(i);

				// Trying to parse date
				Date added = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
						.parse(jMoment.getString("added"));
				
				moments[i] = new Moment(jMoment.getLong("id"),
						jMoment.getString("text"),
						jMoment.getString("author_name"),
						added);
			}

			boolean isMore = moments != null && new_cursor != null;

			return new Moments(moments, isMore, new_cursor);

		} catch (JSONException e) {
			e.printStackTrace();
			throw new IOException("Unable to parse API response.");
		} catch (ParseException e) {
			e.printStackTrace();
			throw new IOException("Unable to parse API response.");
		}
	}

	/**
	 * Checks whether there is error status code and throws exception with
	 * appropriate text.
	 * 
	 * @param result
	 *            of request to the API
	 * @throws IOException
	 */
	private void handleError(ApiFetcher.Result result) throws IOException {
		if (result.getStatusCode() != HttpURLConnection.HTTP_OK) {
			// Get error message from content
			String errorMessage = parseError(result.getContent());
			if (errorMessage != null)
				errorMessage = ", " + errorMessage;

			throw new IOException("Error fetching API endpoint ("
					+ Integer.toString(result.getStatusCode()) + errorMessage
					+ ")");
		}
	}

	/**
	 * Parses error message from response.
	 * 
	 * @param content
	 *            of the response.
	 * @return
	 */
	private String parseError(String content) {
		String message = "";
		try {
			JSONObject jObject = new JSONObject(content);
			jObject = jObject.getJSONObject("error");
			message = jObject.getString("message");
		} catch (JSONException e) {
		}
		return message;
	}
}
