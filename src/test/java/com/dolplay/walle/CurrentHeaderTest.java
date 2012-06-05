package com.dolplay.walle;

import static org.junit.Assert.assertNotNull;

import org.junit.BeforeClass;
import org.junit.Test;

public class CurrentHeaderTest {

	private static WalleHttpClient client;

	@BeforeClass
	public static void setUpBeforeClass() {
		client = new WalleHttpClient();
		client.setTimeOut(30000);
		//		client.setProxy("192.168.2.61", 8080);
	}

	@Test
	public void testGetCurrentHeader() {
		String url = "http://baidu.com";
		client.httpGet(url);
		String date = client.getCurrentHeader("Date").getValue();
		System.out.println("current server date : " + date);
		assertNotNull(date);
	}

}