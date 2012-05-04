package com.dolplay.walle;

import static org.junit.Assert.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

public class WalleHttpClientTest {

	private WalleHttpClient client;
	public static final String itEyeUserName = "你在iteye上的用户名";
	public static final String itEyeUserPass = "你在iteye上的密码";
	
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
	public void testHttpGetHtmlWithRightUrl() {
		String url = "http://baidu.com";
		String content = client.httpGetResp(url, "utf-8");
		assertNotNull(content);
	}

	@Test
	public void testHttpGetDownloadWithRightUrl() {
		boolean isSuccess = client.httpGetDownload(
				"http://mirrors.devlib.org/apache//commons/jelly/binaries/commons-jelly-1.0.zip", "d:\\");
		assertEquals(true, isSuccess);
	}

	@Test
	public void testAccessWebServiceSOAP11() throws IOException {
		InputStream in = new FileInputStream("soap11.xml");
		String a = client.httpPostResp("http://www.webxml.com.cn/webservices/qqOnlineWebService.asmx", in,
				"text/xml; charset=utf-8", "UTF-8");
		assertNotNull(a);
	}

	@Test
	public void testAccessWebServiceSOAP12() throws IOException {
		InputStream in = new FileInputStream("soap12.xml");
		String a = client.httpPostResp("http://www.webxml.com.cn/webservices/qqOnlineWebService.asmx", in,
				"application/soap+xml; charset=utf-8", "UTF-8");
		assertNotNull(a);
	}
	
	@Test
	public void testHttpPostWithForm(){
		String loginPage = client.httpGetResp("http://www.iteye.com/login", "UTF-8");
		int index = loginPage.indexOf("<input name=\"authenticity_token\" type=\"hidden\" value=\"");
		String token = loginPage.substring(index+54, index+54+44);
		Map<String,String> form = new HashMap<String,String>();
		form.put("authenticity_token",token);
		form.put("name", itEyeUserName);
		form.put("password", itEyeUserPass);
		form.put("remember_me", "1");
		form.put("button", "登　录");
		client.httpPost("http://www.iteye.com/login", form, "UTF-8");
		String resp = client.httpGetResp(client.getCurrentRedirectUrl(), "UTF-8");
		// 若重定向至的iteye首页中含字符 “欢迎<用户名>”则表示登录成功
		Assert.assertTrue(resp.contains("欢迎"+itEyeUserName));
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
