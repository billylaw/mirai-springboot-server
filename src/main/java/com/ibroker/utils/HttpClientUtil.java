package com.ibroker.utils;

import org.apache.http.HttpEntity;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

public class HttpClientUtil {
	private static RequestConfig requestConfig;
	private static PoolingHttpClientConnectionManager cm;
	private static CloseableHttpClient httpClient = null;
	private static String EMPTY_STR = "";
	private static String UTF_8 = "UTF-8";

	private static void init(){
		if (requestConfig == null) {
			synchronized (RequestConfig.class) {
				if (requestConfig == null) {
					requestConfig = RequestConfig.custom()
					.setSocketTimeout(3000)
					.setConnectTimeout(3000)
					.setRedirectsEnabled(false)
					.build();
				}
			}
		}

		if(cm == null){
			synchronized (PoolingHttpClientConnectionManager.class) {
				if(cm == null){
					cm = new PoolingHttpClientConnectionManager();
					cm.setMaxTotal(1500);
					cm.setDefaultMaxPerRoute(150);
		            cm.closeExpiredConnections();
		            cm.closeIdleConnections(2000, TimeUnit.MILLISECONDS);
				}
			}
		}
	}


	private static CloseableHttpClient getHttpClient(){
		init();
		if (httpClient == null) {
			synchronized (CloseableHttpClient.class) {
				if(httpClient == null){
					httpClient = HttpClients.custom()
							.setConnectionManager(cm)
							.setDefaultRequestConfig(requestConfig)
							.build();
				}
			}
		}
		return httpClient;
	}


	public static String httpGetRequest(String url){
		HttpGet httpGet = new HttpGet(url);
		return getResult(httpGet);
	}

	public  static class SSLClient extends DefaultHttpClient {
		public SSLClient() throws Exception{
			super();
			SSLContext ctx = SSLContext.getInstance("TLS");
			X509TrustManager tm = new X509TrustManager() {
				@Override
				public void checkClientTrusted(X509Certificate[] chain,
											   String authType) throws CertificateException {
				}
				@Override
				public void checkServerTrusted(X509Certificate[] chain,
											   String authType) throws CertificateException {
				}
				@Override
				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}
			};
			ctx.init(null, new TrustManager[]{tm}, null);
			SSLSocketFactory ssf = new SSLSocketFactory(ctx);
			ssf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			ClientConnectionManager ccm = this.getConnectionManager();
			SchemeRegistry sr = ccm.getSchemeRegistry();
			sr.register(new Scheme("https", ssf,443));
		}
	}
	public static String httpPostBody(String url, String json) throws Exception {
		CloseableHttpClient httpClient = new SSLClient();
		try{
			HttpPost httpPost = new HttpPost(url);
			StringEntity stringEntity = new StringEntity(json, Charset.forName("UTF-8"));
			stringEntity.setContentEncoding("UTF-8");
			httpPost.setEntity(stringEntity);
			httpPost.setHeader("Content-Type", "application/json; charset=utf-8");
			httpPost.setHeader("Accept", "application/json");
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			return httpClient.execute(httpPost, responseHandler);
			//return getResult(httpPost);
		}catch(Exception e){
			throw new Exception(e);
		}
	}


	private static String getResult(HttpRequestBase request){
		CloseableHttpClient httpClient = getHttpClient();
		CloseableHttpResponse response = null;
		try {
			response = httpClient.execute(request);
			HttpEntity entity = response.getEntity();
			if(entity!=null){
				String result = EntityUtils.toString(entity,"utf-8");
				EntityUtils.consume(entity);
				response.close();
				return result;
			}
		} catch(Exception e) {
			request.abort();
		} finally {
			request.releaseConnection();
			if (null != response){
				try {
					response.close();
				} catch (IOException e) {
				}
			}
		}
		return EMPTY_STR;
	}
}
