package com.dolplay.walle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class WalleHttpClientTest {

	private WalleHttpClient client;

	@Before
	public void setUp() throws Exception {
		client = new WalleHttpClient();
		client.setTimeOut(30000);
		//client.setProxy("202.84.17.41", 8080);
	}

	@Test
	public void testHttpGetWithRightUrl() {
		String url = "http://baidu.com";
		boolean isSuccess = client.httpGet(url);
		assertEquals(true, isSuccess);
	}

	@Test
	public void testHttpGetWithWrongUrl() {
		String url = "htxxtp://baidu.com";
		boolean isSuccess = client.httpGet(url);
		assertEquals(false, isSuccess);
	}

	@Test
	public void testHttpGetWithBadUrl() {
		String url = "http://127.0.0.1";
		boolean isSuccess = client.httpGet(url);
		assertEquals(false, isSuccess);
	}

	@Test
	public void testhttpGetHtmlWithRightUrl() {
		String url = "http://baidu.com";
		String content = client.httpGetHtml(url, "utf-8");
		assertNotNull(content);
	}

	@Test
	public void testhttpGetDownloadWithRightUrl() {
		boolean isSuccess = client.httpGetDownload("http://mirrors.devlib.org/apache//commons/jelly/binaries/commons-jelly-1.0.zip", "d:\\");
		assertEquals(true, isSuccess);
	}
	//	@BeforeClass
	//	public static void setUpBeforeClass() throws Exception {
	//	}
	//
	//	@AfterClass
	//	public static void tearDownAfterClass() throws Exception {
	//	}

	//	@After
	//	public void tearDown() throws Exception {
	//	}
}
