package com.dolplay.walle;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpEntityHelper {
	private static final Logger logger = LoggerFactory.getLogger(HttpEntityHelper.class);

	/**
	 * 消耗给定的响应实体内容
	 * 可以确保连接安全的放回到连接池中，而且可以通过连接管理器对后续的请求重用连接
	 * @param httpEntity
	 */
	protected static void consumeResponseEntity(HttpEntity httpEntity) {
		try {
			EntityUtils.consume(httpEntity);
		} catch (IOException e) {
			logger.error("HttpEntity content consume exception", e);
		}
	}

	/**
	 * 根据指定的NameValuePair列表和编码，生成UrlEncodedFormEntity对象
	 * @param formparams
	 * @param requEncoding
	 * @return
	 */
	protected static UrlEncodedFormEntity makeUrlEncodedFormEntity(List<NameValuePair> formparams, String requEncoding) {
		// 设置post请求编码
		if (requEncoding == null) {
			requEncoding = "UTF-8";
		}
		UrlEncodedFormEntity formEntity = null;
		if (formparams != null) {
			try {
				formEntity = new UrlEncodedFormEntity(formparams, requEncoding);
			} catch (UnsupportedEncodingException e) {
				logger.error("make UrlEncodedFormEntity exception", e);
				return null;
			}
		}
		return formEntity;
	}

	/**
	 * 从HttpEntity获取html内容。注意，此前，post和get连接不能关闭
	 * @param entity
	 * @param respEncoding
	 * @return
	 * @throws ParseException
	 * @throws IOException
	 */
	protected static String entity2String(HttpEntity entity, String respEncoding) {
		if (entity == null) {
			logger.warn("response entity has no content");
			return null;
		}
		if (respEncoding == null) {
			respEncoding = "UTF-8";
		}
		String content = null;
		try {
			content = EntityUtils.toString(entity, respEncoding);
			EntityUtils.consume(entity);
		} catch (Exception e) {
			logger.error("entity to String content exception", e);
		}
		return content;
	}

	/**
	 * 根据HttpEntity内容，下载文件。注意，此前，post和get连接不能关闭
	 * @param entity
	 * @param filePath
	 * @throws IllegalStateException 
	 * @throws IOException
	 */
	protected static boolean downloadFile(HttpEntity entity, String filePath) {
		InputStream input = null;
		FileOutputStream output = null;
		if (entity == null) {
			logger.error("nothing downloaded");
			return false;
		}
		try {
			input = entity.getContent();
			File file = new File(filePath);
			output = FileUtils.openOutputStream(file);
			IOUtils.copy(input, output);
			return true;
		} catch (Exception e) {
			logger.error("download file exception", e);
			return false;
		} finally {
			IOUtils.closeQuietly(input);
			IOUtils.closeQuietly(output);
		}
	}
}