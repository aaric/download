package com.github.aaric.download;

import com.github.aaric.download.utils.DownloadUtils;

/**
 * Test
 * 
 * @author Aaric
 * 
 */
public class App {

	/**
	 * Main function.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		String resUrl = "http://127.0.0.1:8280/test.exe";
		String storagePath = "test.exe";
		
		// Test DownloadUtils.
		DownloadUtils.download(resUrl, storagePath);
		
		// Test DownloadUtils2.
		// DownloadUtils2.download(resUrl, storagePath, 3);
		
	}

}
