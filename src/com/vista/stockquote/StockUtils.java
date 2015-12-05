package com.vista.stockquote;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class StockUtils {
	public static final String RESPONSE_TIMEOUT = "TIMEOUT";
	public static final String RESPONSE_INVALID = "INVALID";
	public static final String EMPTY = "";

	private static final String COMMA_DELIMETER = ",";	
	private static final String STOCK_URL = "http://hq.sinajs.cn/list=";
	private static final String TIME_CHART_URL = "http://image.sinajs.cn/newchart/min/n/";
	private static final String GIF = ".gif";
	
	private static final String MARKET_SH = "SH";
	private static final String K_CHART_URL = "http://hqpick.eastmoney.com/EM_Quote2010PictureProducter/Index.aspx?ImageType=KXL&ID={ID}&EF=&Formula=BOLL&UnitWidth={UnitWidth}&FA=true&BA=&type={type}";	
	private static final String K_PARM_ID = "{ID}";
	private static final String K_PARM_UNIT_WIDTH = "{UnitWidth}";
	private static final String K_PARM_TYPE = "{type}";
	
	private static final int TIMEOUT_VALUE = 15000;
	private static final String ENCODING_GB2312 = "gb2312";
	private static final String REG_SH_SYMBOL = "^sh[6|0][0-9]{5}$";
	private static final String REG_SZ_SYMBOL = "^sz[3|0][0-9]{5}$";

	private static final String STOCK_INFO_VAR_STR = "var hq_str_";
	private static final String STOCK_INFO_EQ = "=\"";
	private static final String STOK_INFO_END = "\";";
	
	// sh600332="°×ÔÆÉ½,24.05,23.83,24.33,24.64,24.03,24.33,24.34,10040014,244571608,1000,24.33,300,24.32,1200,24.31,50400,24.30,1500,24.29,7300,24.34,21714,24.35,4390,24.36,2200,24.37,11560,24.38,2014-05-09,15:03:34,00";

	public static String getURLText(String urlPath) {
		String text = null;
		HttpURLConnection connection = null;
		InputStream inputStream = null;
		try {
			URL url = new URL(urlPath);
			connection = (HttpURLConnection) url.openConnection();
			connection.setConnectTimeout(TIMEOUT_VALUE);
			int reponseCode = connection.getResponseCode();
			if (reponseCode == HttpURLConnection.HTTP_OK) {
				inputStream = connection.getInputStream();
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, ENCODING_GB2312));
				text = bufferedReader.readLine();
				if (text == null || text.length() < 50) { // TODO
					return RESPONSE_INVALID;
				}
				text = text.replace(STOCK_INFO_VAR_STR, EMPTY);
				text = text.replace(STOCK_INFO_EQ, COMMA_DELIMETER);
				text = text.replace(STOK_INFO_END, EMPTY);
			} else {
				Log.e(MainActivity.TAG, "Connection response code:" + reponseCode);
			}
		} catch (MalformedURLException e) {
			Log.e(MainActivity.TAG, Log.getStackTraceString(e));
			text = RESPONSE_TIMEOUT;
		} catch (IOException e) {
			Log.e(MainActivity.TAG, Log.getStackTraceString(e));
			text = RESPONSE_TIMEOUT;
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					Log.e(MainActivity.TAG, Log.getStackTraceString(e));
				}
			}
			if (connection != null) {
				connection.disconnect();
			}
		}
		return text;
	}

	public static String[] splitStockText(String stockText) {
		if (stockText == null) {
			return null;
		}
		StringTokenizer st = new StringTokenizer(stockText, COMMA_DELIMETER);
		String[] stockInfo = new String[st.countTokens()];
		int index = 0;
		while (st.hasMoreTokens()) {
			stockInfo[index++] = st.nextToken();
		}
		return stockInfo;
	}

	public static String constructStockURL(String stockSymbol) { // TODO private
		StringBuffer stockURL = new StringBuffer(STOCK_URL);
		stockURL.append(stockSymbol);
		return stockURL.toString();
	}

	public static boolean isValidSymbol(String stockSymbol) {
		if (stockSymbol == null) {
			return false;
		}

		return Pattern.matches(REG_SH_SYMBOL, stockSymbol) || Pattern.matches(REG_SZ_SYMBOL, stockSymbol);
	}

	public static Bitmap getTimeChartGif(String stockSymbol) {
		String urlPath = TIME_CHART_URL + stockSymbol + GIF;
		return getUrlGif(urlPath);
	}
	
	public static Bitmap getKChartGif(String stockSymbol, String unitWidth, String type) {
		String urlPath = K_CHART_URL;
		String market = stockSymbol.substring(0, 2);
		String stockId = stockSymbol.substring(2);
		if (MARKET_SH.equalsIgnoreCase(market)) {
			stockId += "1";
		} else {
			stockId += "2";
		}
		urlPath = urlPath.replace(K_PARM_ID, stockId);
		urlPath = urlPath.replace(K_PARM_UNIT_WIDTH, unitWidth);
		urlPath = urlPath.replace(K_PARM_TYPE, type);
		
		return getUrlGif(urlPath);
	}
	
	private static Bitmap getUrlGif(String urlPath) {
		Bitmap bitmap = null;
		HttpURLConnection connection = null;
		InputStream inputStream = null;
		try {
			URL url = new URL(urlPath);
			connection = (HttpURLConnection) url.openConnection();
			connection.setDoInput(true);
			connection.setConnectTimeout(TIMEOUT_VALUE);
			int reponseCode = connection.getResponseCode();
			if (reponseCode == HttpURLConnection.HTTP_OK) {
				inputStream = connection.getInputStream();
				bitmap = BitmapFactory.decodeStream(inputStream);
			} else {
				Log.e(MainActivity.TAG, "Connection response code:" + reponseCode);
			}
		} catch (MalformedURLException e) {
			Log.e(MainActivity.TAG, Log.getStackTraceString(e));
		} catch (IOException e) {
			Log.e(MainActivity.TAG, Log.getStackTraceString(e));
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					Log.e(MainActivity.TAG, Log.getStackTraceString(e));
				}
			}
			if (connection != null) {
				connection.disconnect();
			}
		}
		return bitmap;
	}

}
