package com.denisigo.moments.api;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Helper class used to fetch API URLs via GET or POST and send data.
 */
public class ApiFetcher {
	private static final String TAG = "ApiFetcher";
	
	// HTTP timeouts in milliseconds
	private final static int CONNECT_TIMEOUT = 15000;
	private final static int READ_TIMEOUT = 10000;

	private final static String GET = "GET";
	private final static String POST = "POST";

	/*
	 * Container for result. Contains status code and content data of the
	 * response.
	 */
	public static class Result {
		private String mContent;
		private int mStatusCode;

		public Result(int statusCode, String content) {
			this.setContent(content);
			this.setStatusCode(statusCode);
		}

		public String getContent() {
			return mContent;
		}

		private void setContent(String content) {
			mContent = content;
		}

		public int getStatusCode() {
			return mStatusCode;
		}

		private void setStatusCode(int statusCode) {
			mStatusCode = statusCode;
		}
	}

	/*
	 * Fetches given URL using GET.
	 */
	public Result get(URL url) throws IOException {
		return fetch(url, GET, null);
	}
	
	/*
	 * Fetches given URL using POST.
	 */
	public Result post(URL url, String data) throws IOException {
		return fetch(url, POST, data);
	}

	/*
	 * Fetches given URL using method and data.
	 */
	private Result fetch(URL url, String method, String data) throws IOException {

		InputStream is = null;
		HttpURLConnection conn = null;

		try {
			conn = (HttpURLConnection) url.openConnection();
			conn.setReadTimeout(READ_TIMEOUT);
			conn.setConnectTimeout(CONNECT_TIMEOUT);
			conn.setRequestMethod(method);
			conn.setDoInput(true);

			// In case we have POST data
			if (method == POST) {
				conn.setDoOutput(true);
				// Our API uses json and UTF-8, so set appropriate content type
				conn.setRequestProperty("Content-Type",
						"application/json; charset=utf-8");

				// Write data to the output
				if (data != null) {
					OutputStream os = conn.getOutputStream();
					BufferedWriter writer = new BufferedWriter(
							new OutputStreamWriter(os, "UTF-8"));
					writer.write(data);
					writer.flush();
					writer.close();
					os.close();
				} else {
					// on Android 2.2.2 there was 411 (content-length not set)
					// response when POST and no data was posted
					conn.setFixedLengthStreamingMode(0);
				}
			}

			// Read response code and content if present
			int statusCode = conn.getResponseCode();
			String content = null;

			// Content is available in different streams in depend of status 
			if (statusCode == HttpURLConnection.HTTP_OK)
				is = conn.getInputStream();
			else
				is = conn.getErrorStream();
			
			content = readContent(is);

			return new Result(statusCode, content);

		} finally {
			if (is != null)
				is.close();
			
			if (conn != null)
				conn.disconnect();
		}
	}

	/**
	 * Helper method for reading content from InputStream.
	 * @param is InputStream instance
	 * @return String content
	 * @throws IOException
	 */
	private String readContent(InputStream is) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is,
				"UTF-8"));

		StringBuffer content = new StringBuffer("");

		String line;
		while ((line = reader.readLine()) != null)
			content.append(line);

		reader.close();

		return content.toString();
	}
}
