package com.vista.stockquote;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.ImageView;

public class KChartActivity extends ActionBarActivity {
	private static final String DEFAULT_UNIT_WIDTH = "6";
	private static final String K_DAILY = StockUtils.EMPTY;
	private static final String K_WEEKLY = "W";
	private static final String K_MONTHLY = "M";

	private String stockSymbol;
	private String unitWidth;
	private String type;
	private Bitmap bitmap;
	private ImageView kChart;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_kchart);

		kChart = (ImageView) findViewById(R.id.kChart);
		stockSymbol = getIntent().getStringExtra(MainActivity.STOCK_SYMBOL);
		dailyKChart(findViewById(R.layout.activity_kchart));		
	}
	
	public void dailyKChart(View v) {
		unitWidth = DEFAULT_UNIT_WIDTH;
		type = K_DAILY;
		new MyAsyncTask().execute();
	}
	
	public void weeklyKChart(View v) {
		unitWidth = DEFAULT_UNIT_WIDTH;
		type = K_WEEKLY;
		new MyAsyncTask().execute();
	}
	
	public void monthlyKChart(View v) {
		unitWidth = DEFAULT_UNIT_WIDTH;
		type = K_MONTHLY;
		new MyAsyncTask().execute();
	}
	
	public void expandKChart(View v) {
		int iUnitWidth = Integer.parseInt(unitWidth);
		if (iUnitWidth == 0) {
			return;
		}
		iUnitWidth--;
		unitWidth = Integer.toString(iUnitWidth);
		new MyAsyncTask().execute();
	}
	
	public void shortenKChart(View v) {
		int iUnitWidth = Integer.parseInt(unitWidth);
		if (iUnitWidth == 8) {
			return;
		}
		iUnitWidth++;
		unitWidth = Integer.toString(iUnitWidth);
		new MyAsyncTask().execute();
	}

	private class MyAsyncTask extends AsyncTask<String, String, String> {
		@Override
		protected String doInBackground(String... params) {
			bitmap = StockUtils.getKChartGif(stockSymbol, unitWidth, type);
			return null;
		}

		@Override
		protected void onPostExecute(String result) {			
			kChart.setImageBitmap(bitmap);
		}
	}

}
