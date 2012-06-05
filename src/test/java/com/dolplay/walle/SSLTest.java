package com.dolplay.walle;

import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;

public class SSLTest {

	private static WalleHttpClient client;

	@BeforeClass
	public static void setUpBeforeClass() {
		client = new WalleHttpClient();
		client.setTimeOut(30000);
		//		client.setProxy("192.168.2.61", 8080);
	}

	@Test
	public void testHttpGetWithRightUrl() {
		String url = "https://ajax.googleapis.com/ajax/libs/jquery/1.4.2/jquery.min.js";
		boolean isSuccess = client.httpGet(url);
		assertTrue(isSuccess);
	}

}