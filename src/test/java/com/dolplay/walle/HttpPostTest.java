package com.dolplay.walle;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

public class HttpPostTest {

	private static WalleHttpClient client;
	public static final String itEyeUserName = "walletest";
	public static final String itEyeUserPass = "walle123";

	@BeforeClass
	public static void setUpBeforeClass() {
		client = new WalleHttpClient();
		client.setTimeOut(30000);
		//		client.setProxy("192.168.2.61", 8080);
	}

	@Test
	public void testAccessWebServiceSOAP11() {
		InputStream in = getClass().getResourceAsStream("/soap11.xml");
		String a = client.httpPostResp("http://webservice.webxml.com.cn/WebServices/qqOnlineWebService.asmx", in,
				"text/xml; charset=utf-8");
		System.out.println(a);
		assertNotNull(a);
	}

	@Test
	public void testAccessWebServiceSOAP12() {
		InputStream in = getClass().getResourceAsStream("/soap12.xml");
		String a = client.httpPostResp("http://webservice.webxml.com.cn/WebServices/qqOnlineWebService.asmx", in,
				"application/soap+xml; charset=utf-8");
		System.out.println(a);
		assertNotNull(a);
	}

	@Test
	public void testHttpPostWithForm() {
		String loginPage = client.httpGetResp("http://www.iteye.com/login");
		int index = loginPage.indexOf("<input name=\"authenticity_token\" type=\"hidden\" value=\"");
		String token = loginPage.substring(index + 54, index + 54 + 44);
		Map<String, String> form = new HashMap<String, String>();
		form.put("authenticity_token", token);
		form.put("name", itEyeUserName);
		form.put("password", itEyeUserPass);
		form.put("remember_me", "1");
		form.put("button", "登　录");
		client.httpPost("http://www.iteye.com/login", form);
		String resp = client.httpGetResp(client.getCurrentRedirectUrl());
		// 若重定向至的iteye首页中含字符 “欢迎<用户名>”则表示登录成功
		assertTrue(resp.contains("欢迎" + itEyeUserName));
	}

	@Test
	public void testHttpPostWithNull() {
		String url = "http://baidu.com";
		boolean isSuccess = client.httpPost(url, null);
		assertTrue(isSuccess);
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