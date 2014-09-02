package com.github.aaric.download.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * DownloadUtils2.
 * 
 * @author Aaric
 * 
 */
public final class DownloadUtils2 {

	/**
	 * The private constructor.
	 */
	private DownloadUtils2() {
		super();
	}

	/**
	 * Get record path prefix.
	 * 
	 * @param resourceURL
	 *            The resource URL.
	 * @param threadIndex
	 *            The thread index.
	 * @return
	 */
	private static String getRecordPathPrefix(String resourceURL,
			int threadIndex) {
		String path = null;
		if (null != resourceURL && !"".equals(resourceURL.trim())) {
			path = resourceURL.trim().replaceAll(":", "_").replaceAll("/", "_")
					.replaceAll("\\.", "_");
			path += "_";
			path += threadIndex;
		}
		return path;
	}

	/**
	 * Get an object of HttpURLConnection.
	 * 
	 * @param resourceURL
	 *            The resource URL.
	 * @return
	 * @throws IOException
	 */
	private static HttpURLConnection getHttpURLConnectionByGET(
			String resourceURL) throws IOException {
		HttpURLConnection conn = null;
		if (null != resourceURL && !"".equals(resourceURL.trim())) {
			URL url = new URL(resourceURL);
			conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(5000);
			conn.setRequestMethod("GET");
		}
		return conn;
	}

	/**
	 * Download the file from server.
	 * 
	 * @param resUrl
	 *            The file to be download.
	 * @param storagePath
	 *            The path of storage.
	 * @param threadNumber
	 *            The number of thread.
	 */
	public static void download(String resUrl, String storagePath,
			int threadNumber) {
		System.out.println("Downloading...");
		try {
			HttpURLConnection conn = getHttpURLConnectionByGET(resUrl);
			if (null != conn) {
				int code = conn.getResponseCode();
				// HTTP Status-Code 200: OK.--All Resource.
				if (HttpURLConnection.HTTP_OK == code) {
					int length = conn.getContentLength();
					System.out.println("length--> " + length);
					int startSize, endSize;
					int blockSize = length / threadNumber;
					for (int threadIndex = 1; threadIndex <= threadNumber; threadIndex++) {
						startSize = (threadIndex - 1) * blockSize;
						if (threadIndex == threadNumber) {
							endSize = length;
						} else {
							endSize = threadIndex * blockSize - 1;
						}
						System.out.println(threadIndex + "--> startSize: "
								+ startSize + ", endSize: " + endSize);

						// Download.
						new DownloadThread(resUrl, storagePath, threadIndex,
								startSize, endSize).start();

					}

				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Download Thread.
	 * 
	 * @author Aaric
	 * 
	 */
	public static class DownloadThread extends Thread {

		/**
		 * The URL where to download.
		 */
		private String url;
		/**
		 * The path to storage.
		 */
		private String path;
		/**
		 * The thread index.
		 */
		private int threadIndex;
		/**
		 * The download size from start.
		 */
		private int startSize;
		/**
		 * The download size to end.
		 */
		private int endSize;

		/**
		 * Constructor.
		 * 
		 * @param url
		 *            The URL where to download.
		 * @param path
		 *            The path to storage.
		 * @param threadIndex
		 *            The thread index.
		 * @param startSize
		 *            The download size from start.
		 * @param endSize
		 *            The download size to end.
		 */
		public DownloadThread(String url, String path, int threadIndex,
				int startSize, int endSize) {
			super();
			this.url = url;
			this.path = path;
			this.threadIndex = threadIndex;
			this.startSize = startSize;
			this.endSize = endSize;
		}

		@Override
		public void run() {
			File recordFile = null;
			try {
				int start = startSize;
				String record = getRecordPathPrefix(url, threadIndex);
				recordFile = new File(record);
				System.out.println("file" + threadIndex + ": "
						+ recordFile.getAbsolutePath());
				// Read record file.
				if (recordFile.exists() && 0 < recordFile.length()) {
					FileInputStream fis = new FileInputStream(recordFile);
					byte[] temp = new byte[1024];
					int len = fis.read(temp);
					start = Integer.parseInt(new String(temp, 0, len));
					System.out.println(threadIndex + ": start-->" + start
							+ ", end-->" + endSize);
					fis.close();

					/**
					 * If file has been download, you can delete record file.
					 */
					if (start >= endSize) {
						recordFile.delete();
						return;
					}

				}

				HttpURLConnection conn = getHttpURLConnectionByGET(url);
				conn.setRequestProperty("Range", "bytes=" + start + "-"
						+ endSize);
				int code = conn.getResponseCode();
				System.out.println(threadIndex + "-->" + code);
				// HTTP Status-Code 206: Partial Content.--Section Resource.
				if (HttpURLConnection.HTTP_PARTIAL == code) {
					InputStream is = conn.getInputStream();
					RandomAccessFile raf = new RandomAccessFile(path, "rwd");
					raf.seek(start);
					int len = 0;
					byte[] buffer = new byte[1024];
					while ((len = is.read(buffer)) != -1) {
						RandomAccessFile rafRecord = new RandomAccessFile(
								record, "rwd");
						raf.write(buffer, 0, len);
						start += len;
						System.out.println(threadIndex + "--" + start);
						rafRecord.write(("" + start).getBytes());
						rafRecord.close();
					}
					is.close();
					raf.close();

					// Output.
					System.out.println("Thread Index " + threadIndex
							+ " has been download...");

					// Delete record file.
					if (start >= endSize) {
						recordFile.delete();
					}

				} else {
					// Trace.
					System.out.println("Thread Index " + threadIndex
							+ " download failure...");

				}

			} catch (Exception e) {
				e.printStackTrace();
			}

		}

	}

}
