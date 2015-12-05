package com.vista.stockquote;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.ImageView;

public class TimeChartActivity extends ActionBarActivity {
	private String stockSymbol;
	private Bitmap bitmap;
	private ImageView timeChart;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_time_chart);

		timeChart = (ImageView) findViewById(R.id.timeChart);
		stockSymbol = getIntent().getStringExtra(MainActivity.STOCK_SYMBOL);
		new MyAsyncTask().execute();
	}

	private class MyAsyncTask extends AsyncTask<String, String, String> {
		@Override
		protected String doInBackground(String... params) {
			bitmap = StockUtils.getTimeChartGif(stockSymbol);
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			timeChart.setImageBitmap(bitmap);
		}
	}

}
