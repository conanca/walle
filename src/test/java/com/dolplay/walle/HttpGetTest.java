package com.dolplay.walle;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.BeforeClass;
import org.junit.Test;

public class HttpGetTest {

	private static WalleHttpClient client;

	@BeforeClass
	public static void setUpBeforeClass() {
		client = new WalleHttpClient();
		client.setTimeOut(30000);
		//		client.setProxy("192.168.2.61", 8080);
	}

	@Test
	public void testHttpGetWithRightUrl() {
		String url = "http://baidu.com";
		boolean isSuccess = client.httpGet(url);
		assertTrue(isSuccess);
	}

	@Test
	public void testHttpGetHtmlWithRightUrl() {
		String url = "http://baidu.com";
		String content = client.httpGetResp(url);
		assertNotNull(content);
	}

	@Test
	public void testHttpGetDownloadWithRightUrl() {
		String fileName = client.httpGetDownload(
				"http://mirrors.devlib.org/apache//commons/jelly/binaries/commons-jelly-1.0.zip", "temp/");
		File f = new File("temp/" + fileName);
		assertTrue(f.exists());
		f.delete();
		new File("temp/").delete();
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