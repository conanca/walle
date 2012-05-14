package com.dolplay.walle;


import org.apache.http.client.HttpClient;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;

public class HttpClientProvider {
	/**
	 * max total connection
	 */
	private static final int MAXTOTALCONNECTION = 200;
	/**
	 * default max connection per route
	 */
	private static final int DEFAULTMAXCONNECTIONPERROUTE = 20;
	
	private static ThreadSafeClientConnManager cm;
	
	public static HttpClient creatHttpClient() {
		if(null==cm){
			SchemeRegistry schemeRegistry = new SchemeRegistry();
			schemeRegistry.register(
			         new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
			schemeRegistry.register(
			         new Scheme("https", 443, SSLSocketFactory.getSocketFactory()));
			cm = new ThreadSafeClientConnManager(schemeRegistry);
			cm.setMaxTotal(MAXTOTALCONNECTION);
			cm.setDefaultMaxPerRoute(DEFAULTMAXCONNECTIONPERROUTE);		 
		}
		HttpClient httpClient = new DefaultHttpClient(cm);
		return httpClient;
	}
}
