package com.dolplay.walle;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

/**
 * 一个简单的Http客户端的单例类,尽可能的模拟浏览器的动作,包括：执行post和get,获取header和cookie
 * @author Conanca
 *
 */
public class WalleHttpClient {
	private static final Log log = LogFactory.getLog(WalleHttpClient.class);
	private static final int DEFAULTTIMEOUT = 5000;
	private static final String DEFAULTUSERAGENT = UserAgent.IE8;
	private static final String DEFAULTREQUENCODING = "UTF-8";
	private static final String DEFAULTRESPENCODING = "UTF-8";

	static {
		System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
	}

	private DefaultHttpClient httpclient;
	
	private HttpHost proxy;
	private int timeOut;
	private String userAgent;
	private String requEncoding;
	private String respEncoding;
	
	private HttpGet currentHttpGet;
	private HttpPost currentHttpPost;
	
	private int currentStatusCode;
	private String currentRedirectUrl;
	private List<Cookie> currentCookies;
	private List<Header> currentHeaders;

	/**
	 * 构造一个无代理单线程的 WalleBrowser
	 */
	public WalleHttpClient() {
		this(null);
	}

	/**
	 * 构造一个WalleBrowser,并制定代理服务器
	 * @param proxy
	 */
	public WalleHttpClient(HttpHost proxy) {
		this(DEFAULTTIMEOUT, DEFAULTUSERAGENT, DEFAULTREQUENCODING, DEFAULTRESPENCODING, proxy);
	}

	public WalleHttpClient(int timeOut, String userAgent, String requEncoding, String respEncoding, HttpHost proxy) {
		super();
		this.timeOut = timeOut;
		this.userAgent = userAgent;
		this.requEncoding = requEncoding;
		this.respEncoding = respEncoding;
		this.proxy = proxy;
		initHttpClient();
	}

	/**
	 * 初始化httpclient
	 */
	public void initHttpClient() {
		httpclient = new DefaultHttpClient();
		HttpParams params = httpclient.getParams();
		params.setParameter(CoreConnectionPNames.SO_TIMEOUT, timeOut);
		HttpProtocolParams.setContentCharset(params, respEncoding);
		HttpProtocolParams.setHttpElementCharset(params, respEncoding);
		HttpProtocolParams.setUserAgent(params, userAgent);
		HttpProtocolParams.setUseExpectContinue(params, false);
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		if (proxy != null) {
			log.info("using proxy");
			params.setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
		}
	}

	/**
	 * 更新当前header
	 * @param response
	 */
	private void updateCurrentHeaders(HttpResponse response) {
		// 获取当前响应header
		HeaderIterator headerIterator = response.headerIterator();
		currentHeaders = new ArrayList<Header>();
		while (headerIterator.hasNext()) {
			currentHeaders.add(headerIterator.nextHeader());
		}
	}

	/**
	 * 更新当前cookie
	 */
	private void updateCurrentCookies() {
		// 获取当前cookies
		currentCookies = httpclient.getCookieStore().getCookies();
	}

	/**
	 * 执行Http请求
	 * @param request
	 * @return
	 */
	private HttpEntity excuteRequest(HttpUriRequest request) {
		HttpResponse response;
		HttpEntity resEntity = null;
		try {
			// 执行请求
			response = httpclient.execute(request);
			// 更新当前Header
			updateCurrentHeaders(response);
			// 更新当前cookie
			updateCurrentCookies();
			// 获取响应状态码
			currentStatusCode = response.getStatusLine().getStatusCode();
			log.info("Response status code:" + currentStatusCode);
			// 如果响应状态码表示需要重定向，更新currentRedirectUrl的值；否则获取响应实体
			switch (currentStatusCode) {
			case HttpStatus.SC_MULTIPLE_CHOICES:
			case HttpStatus.SC_MOVED_PERMANENTLY:
			case HttpStatus.SC_MOVED_TEMPORARILY:
			case HttpStatus.SC_SEE_OTHER:
			case HttpStatus.SC_NOT_MODIFIED:
			case HttpStatus.SC_USE_PROXY:
			case HttpStatus.SC_TEMPORARY_REDIRECT:
				Header location = getCurrentHeader("location");
				if (location == null) {
					location = getCurrentHeader("Location");
				}
				if (location != null) {
					currentRedirectUrl = location.getValue();
				} else {
					currentRedirectUrl = null;
				}
				log.info("Get redirect url:" + currentRedirectUrl);
				break;
			default:
				resEntity = response.getEntity();
				currentRedirectUrl = null;
			}

		} catch (Exception e) {
			currentStatusCode = 0;
			log.error("excute request exception", e);
		}
		log.info("Request end");
		return resEntity;
	}

	/**
	 * 传一个HttpEntity,执行post请求
	 * @param url
	 * @param reqEntity
	 * @return
	 */
	private HttpEntity httpPost(String url, HttpEntity reqEntity) {
		// 先终止上次的请求
		abortRequest();
		currentHttpPost = new HttpPost(url);
		// TODO 应该重用链接
		//		try {
		//			currentHttpPost.setURI(new URI(url));
		//		} catch (URISyntaxException e) {
		//			e.printStackTrace();
		//		}
		// TODO reqEntity为空会咋样？
		currentHttpPost.setEntity(reqEntity);
		// 执行post请求
		log.info("executing request " + currentHttpPost.getRequestLine());
		return excuteRequest(currentHttpPost);
	}

	/**
	 * 传一个输入流,执行post请求
	 * @param url
	 * @param in
	 * @return
	 */
	private HttpEntity excuteHttpPost(String url, InputStream in, String contentType) {
		InputStreamEntity inEntity = new InputStreamEntity(in, -1);
		inEntity.setContentType(contentType);
		inEntity.setChunked(true);
		// 执行post请求
		return httpPost(url, inEntity);
	}

	/**
	 * 传表单参数Map,执行post请求
	 * @param url
	 * @param formparamMap
	 * @param requEncoding
	 * @return
	 */
	private HttpEntity excuteHttpPost(String url, Map<String, String> formparamMap) {
		// 设置post请求的表单参数
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		for (String paramName : formparamMap.keySet()) {
			formparams.add(new BasicNameValuePair(paramName, formparamMap.get(paramName)));
		}
		UrlEncodedFormEntity formEntity = HttpEntityHelper.makeUrlEncodedFormEntity(formparams, requEncoding);
		// 执行post请求
		return httpPost(url, formEntity);
	}

	/**
	 * 传表单参数Map,执行post请求(用于参数值含多选框或checkbox的)
	 * @param url
	 * @param formparamMap
	 * @param requEncoding
	 * @return
	 */
	private HttpEntity excuteHttpPost2(String url, Map<String, List<String>> formparamMap) {
		// 设置post请求的表单参数
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		for (String paramName : formparamMap.keySet()) {
			List<String> valueList = formparamMap.get(paramName);
			for (String value : valueList) {
				formparams.add(new BasicNameValuePair(paramName, value));
			}
		}
		UrlEncodedFormEntity formEntity = HttpEntityHelper.makeUrlEncodedFormEntity(formparams, requEncoding);
		// 执行post请求
		return httpPost(url, formEntity);
	}

	/**
	 * POST方式访问一个url,并提交一个输入流
	 * @param url
	 * @param in
	 * @param contentType
	 * @return 是否成功
	 */
	public boolean httpPost(String url, InputStream in, String contentType) {
		HttpEntity httpEntity = excuteHttpPost(url, in, contentType);
		if (httpEntity == null) {
			return false;
		}
		HttpEntityHelper.consumeResponseEntity(httpEntity);
		return true;
	}

	/**
	 * 传一个输入流,执行post请求,返回响应的String内容
	 * 可用于访问Web Service
	 * @param url
	 * @param reqEntity
	 * @param respEncoding
	 * @return
	 */
	public String httpPostResp(String url, InputStream in, String contentType) {
		return HttpEntityHelper.entity2String(excuteHttpPost(url, in, contentType), respEncoding);
	}

	/**
	 * POST方式访问一个url,并提交参数
	 * @param url
	 * @param formparamMap
	 * @param requEncoding
	 */
	public boolean httpPost(String url, Map<String, String> formparamMap) {
		HttpEntity httpEntity = excuteHttpPost(url, formparamMap);
		if (httpEntity == null) {
			return false;
		}
		HttpEntityHelper.consumeResponseEntity(httpEntity);
		return true;
	}

	/**
	 * 传表单参数Map,执行post请求,返回响应的String内容
	 * @param url
	 * @param formparamMap
	 * @param requEncoding
	 * @param respEncoding
	 * @return
	 */
	public String httpPostResp(String url, Map<String, String> formparamMap, String requEncoding) {
		return HttpEntityHelper.entity2String(excuteHttpPost(url, formparamMap), respEncoding);
	}

	/**
	 * POST方式访问一个url,并提交参数(用于参数值含多选框或checkbox的)
	 * @param url
	 * @param formparamMap
	 * @param requEncoding
	 */
	public boolean httpPost2(String url, Map<String, List<String>> formparamMap) {
		HttpEntity httpEntity = excuteHttpPost2(url, formparamMap);
		if (httpEntity == null) {
			return false;
		}
		HttpEntityHelper.consumeResponseEntity(httpEntity);
		return true;
	}

	/**
	 * 传表单参数Map,执行post请求,返回响应的String内容(用于表单参数含多选框或checkbox的)
	 * @param url
	 * @param formparamMap
	 * @param requEncoding
	 * @param respEncoding
	 * @return
	 */
	public String httpPostResp2(String url, Map<String, List<String>> formparamMap) {
		return HttpEntityHelper.entity2String(excuteHttpPost2(url, formparamMap), respEncoding);
	}

	/**
	 * 执行get请求
	 * @param url
	 * @return
	 */
	private HttpEntity excuteHttpGet(String url) {
		// 先终止上次的请求
		abortRequest();
		// TODO 是否应该重用这个对象呢
		currentHttpGet = new HttpGet(url);
		// 执行get请求
		log.info("executing request " + currentHttpGet.getRequestLine());
		return excuteRequest(currentHttpGet);
	}

	/**
	 * GET方式访问一个Url
	 * @param url
	 */
	public boolean httpGet(String url) {
		HttpEntity httpEntity = excuteHttpGet(url);
		if (httpEntity == null) {
			return false;
		}
		HttpEntityHelper.consumeResponseEntity(httpEntity);
		return true;
	}

	/**
	 * 执行get请求,返回响应的String内容
	 * @param url
	 * @param respEncoding
	 * @return
	 */
	public String httpGetResp(String url) {
		return HttpEntityHelper.entity2String(excuteHttpGet(url), respEncoding);
	}

	/**
	 * 执行get请求，下载文件
	 * TODO 目前形如http://xxx.com/downloadfile?id=123这样的下载的文件有问题，是纯文本的
	 * @param url
	 * @param filePath
	 */
	public String httpGetDownload(String url, String filePath) {
		HttpEntity entity = excuteHttpGet(url);
		String fileName = url.substring(url.lastIndexOf("/") + 1);
		Header contentDisposition = getCurrentHeader("content-disposition");
		if (contentDisposition != null) {
			String contentDispositionStr = getCurrentHeader("content-disposition").getValue();
			fileName = contentDispositionStr.substring(contentDispositionStr.indexOf("filename") + 9);
		}
		if (getCurrentStatusCode() == 200) {
			HttpEntityHelper.downloadFile(entity, filePath + fileName);
			log.info("finished download");
			return fileName;
		} else {
			log.warn("nothing downloaded");
			return null;
		}
	}

	/**
	 * 终止post和get连接
	 */
	private void abortRequest() {
		if (currentHttpGet != null) {
			currentHttpGet.abort();
		}
		if (currentHttpPost != null) {
			currentHttpPost.abort();
		}
	}

	/**
	 * 关闭httpclient
	 */
	public void shutdown() {
		log.info("shuting down...");
		abortRequest();
		httpclient.getConnectionManager().shutdown();
		log.info("shuted down");
	}

	/**
	 * 获取当前设置的代理服务器
	 * @return
	 */
	public HttpHost getProxy() {
		return proxy;
	}

	/**
	 * 设置代理服务器
	 * @param proxy
	 */
	public void setProxy(HttpHost proxy) {
		this.proxy = proxy;
		if (proxy != null) {
			log.info("using proxy");
			httpclient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
		} else {
			httpclient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, null);
			log.info("no longer using proxy");
		}
	}

	/**
	 * 设置代理服务器
	 * @param host
	 * @param port
	 */
	public void setProxy(String host, int port) {
		setProxy(new HttpHost(host, port));
	}

	/**
	 * 获取当前的超时时间
	 * @return
	 */
	public int getTimeOut() {
		return timeOut;
	}

	/**
	 * 设置超时时间
	 * @param timeOut
	 */
	public void setTimeOut(int timeOut) {
		this.timeOut = timeOut;
		httpclient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, timeOut);
	}

	/**
	 * 获取当前cookie，注意此前应执行过post或get
	 * @return
	 */
	public List<Cookie> getCurrentCookies() {
		return currentCookies;
	}

	/**
	 * 根据cookie名获取当前cookie，注意此前应执行过post或get
	 * @param cookieName
	 * @return
	 */
	public Cookie getCurrentCookie(String cookieName) {
		Cookie returnCookie = null;
		for (Cookie cookie : currentCookies) {
			if (cookie.getName() != null && cookie.getName().equals(cookieName)) {
				return returnCookie = cookie;
			}
		}
		return returnCookie;
	}

	/**
	 * 获取当前header，注意此前应执行过post或get
	 * @return
	 */
	public List<Header> getCurrentHeaders() {
		return currentHeaders;
	}

	/**
	 * 根据header名称获取当前header，注意此前应执行过post或get
	 * @param headerName
	 * @return
	 */
	public Header getCurrentHeader(String headerName) {
		Header returnHeader = null;
		if (currentHeaders != null && currentHeaders.size() > 0) {
			for (Header header : currentHeaders) {
				if (header.getName() != null && header.getName().equals(headerName)) {
					return returnHeader = header;
				}
			}
		}
		return returnHeader;
	}

	/**
	 * 获取当前重定向地址
	 * @return
	 */
	public String getCurrentRedirectUrl() {
		return currentRedirectUrl;
	}

	/**
	 * 获取当前状态码
	 * @return
	 */
	public int getCurrentStatusCode() {
		return currentStatusCode;
	}

	public String getRequEncoding() {
		return requEncoding;
	}

	public void setRequEncoding(String requEncoding) {
		this.requEncoding = requEncoding;
	}

	public String getRespEncoding() {
		return respEncoding;
	}

	public void setRespEncoding(String respEncoding) {
		this.respEncoding = respEncoding;
		HttpProtocolParams.setContentCharset(httpclient.getParams(), respEncoding);
		HttpProtocolParams.setHttpElementCharset(httpclient.getParams(), respEncoding);
	}

	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
		HttpProtocolParams.setUserAgent(httpclient.getParams(), userAgent);
	}

}
