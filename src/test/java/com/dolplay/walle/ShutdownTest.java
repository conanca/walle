package com.dolplay.walle;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class ShutdownTest {
	private WalleHttpClient client;

	@Before
	public void setUp() throws Exception {
		client = new WalleHttpClient();
		client.setTimeOut(30000);
		//		client.setProxy("192.168.2.61", 8080);
	}

	@Test
	public void testShutdown() {
		String url = "http://baidu.com";
		boolean isSuccess1 = client.httpGet(url);
		client.shutdown();
		client = new WalleHttpClient();
		client.setTimeOut(30000);
		//		client.setProxy("192.168.2.61", 8080);
		boolean isSuccess2 = client.httpGet(url);
		assertTrue(isSuccess1 && isSuccess2);
	}
}
