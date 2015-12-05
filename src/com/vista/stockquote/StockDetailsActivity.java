package com.vista.stockquote;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

public class StockDetailsActivity extends ActionBarActivity {
	private String stockSymbol;
	private SharedPreferences myStocks;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_stock_details);

		stockSymbol = getIntent().getStringExtra(MainActivity.STOCK_SYMBOL);

		TextView stockNameTextView = (TextView) findViewById(R.id.stockNameTextView);
		myStocks = getSharedPreferences(MainActivity.MY_STOCK, MODE_PRIVATE);
		stockNameTextView.setText(myStocks.getString(stockSymbol, null) + " " + stockSymbol);

		refreshNative();
	}

	// MUST be public
	public void refresh(View v) {
		refreshNative();
	}

	private void refreshNative() {
		setProgressBarIndeterminateVisibility(true);
		new MyAsyncTask().execute(stockSymbol);
	}

	public void delete(View v) {
		AlertDialog.Builder alertDialogBulder = new AlertDialog.Builder(StockDetailsActivity.this);
		alertDialogBulder.setTitle(R.string.confirm_to_delete_sotck);
		alertDialogBulder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		alertDialogBulder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				SharedPreferences.Editor preferencesEditor = myStocks.edit();
				preferencesEditor.remove(stockSymbol);
				preferencesEditor.apply();
				Intent intent = new Intent(StockDetailsActivity.this, MainActivity.class);
				startActivity(intent);
			}
		});
		AlertDialog alertDialog = alertDialogBulder.create();
		alertDialog.show();
	}

	private class MyAsyncTask extends AsyncTask<String, String, String> {
		@Override
		protected String doInBackground(String... params) {
			String stockURL = StockUtils.constructStockURL(params[0]);
			String result = StockUtils.getURLText(stockURL);
			if (result == StockUtils.RESPONSE_TIMEOUT) {
				return null;
			}
			return result;
		}

		@Override
		protected void onPostExecute(String stockText) {
			String[] stockInfo = StockUtils.splitStockText(stockText);
			if (stockInfo == null) {
				AlertDialog.Builder alertDialogBulder = new AlertDialog.Builder(StockDetailsActivity.this);
				alertDialogBulder.setTitle(R.string.timeout);
				alertDialogBulder.setPositiveButton(R.string.ok, null);
				AlertDialog alertDialog = alertDialogBulder.create();
				alertDialog.show();
				setProgressBarIndeterminateVisibility(false);
				return;
			}

			TextView currentPriceTextView = (TextView) findViewById(R.id.currentPriceTextView);
			double currentPrice = Double.parseDouble(stockInfo[4]);
			currentPriceTextView.setText(String.format("%.02f", currentPrice));

			TextView yesterdayPriceTextView = (TextView) findViewById(R.id.yesterdayPriceTextView);
			double yesterdayPrice = Double.parseDouble(stockInfo[3]);
			yesterdayPriceTextView.setText(String.format("%.02f", yesterdayPrice));

			TextView openTextView = (TextView) findViewById(R.id.openTextView);
			double openPrice = Double.parseDouble(stockInfo[2]);
			openTextView.setText(String.format("%.02f", openPrice));

			TextView highestTextView = (TextView) findViewById(R.id.highestTextView);
			double highestPrice = Double.parseDouble(stockInfo[5]);
			highestTextView.setText(String.format("%.02f", highestPrice));

			TextView lowestTextView = (TextView) findViewById(R.id.lowestTextView);
			double lowPrice = Double.parseDouble(stockInfo[6]);
			lowestTextView.setText(String.format("%.02f", lowPrice));

			double diff = currentPrice - yesterdayPrice;
			TextView raiseDiffTextView = (TextView) findViewById(R.id.raiseDiffTextView);
			raiseDiffTextView.setText(String.format("%.02f", diff));

			TextView raisePercentageTextView = (TextView) findViewById(R.id.raisePercentageTextView);
			raisePercentageTextView.setText(String.format("%.02f", (diff / yesterdayPrice) * 100) + "%");

			// TextView dealVolTextView = (TextView)
			// findViewById(R.id.dealVolTextView);
			// long dealVol = (long) (Double.parseDouble(stockInfo[9]) / 100);
			// dealVolTextView.setText(String.format("%1$9d", dealVol));
			//
			// TextView dealAmountTextView = (TextView)
			// findViewById(R.id.dealAmountTextView);
			// long dealAmount = Math.round(Double.parseDouble(stockInfo[10]) /
			// 10000);
			// dealAmountTextView.setText(String.format("%1$9d", dealAmount));

			TextView sell5VolTextView = (TextView) findViewById(R.id.sell5VolTextView);
			long sell5Vol = Math.round(Double.parseDouble(stockInfo[29]) / 100);
			sell5VolTextView.setText(String.format("%1$9d", sell5Vol));

			TextView sell5TextView = (TextView) findViewById(R.id.sell5TextView);
			double sell5Price = Double.parseDouble(stockInfo[30]);
			sell5TextView.setText(String.format("%.02f", sell5Price));

			TextView sell4VolTextView = (TextView) findViewById(R.id.sell4VolTextView);
			long sell4Vol = Math.round(Double.parseDouble(stockInfo[27]) / 100);
			sell4VolTextView.setText(String.format("%1$9d", sell4Vol));

			TextView sell4TextView = (TextView) findViewById(R.id.sell4TextView);
			double sell4Price = Double.parseDouble(stockInfo[28]);
			sell4TextView.setText(String.format("%.02f", sell4Price));

			TextView sell3VolTextView = (TextView) findViewById(R.id.sell3VolTextView);
			long sell3Vol = Math.round(Double.parseDouble(stockInfo[25]) / 100);
			sell3VolTextView.setText(String.format("%1$9d", sell3Vol));

			TextView sell3TextView = (TextView) findViewById(R.id.sell3TextView);
			double sell3Price = Double.parseDouble(stockInfo[26]);
			sell3TextView.setText(String.format("%.02f", sell3Price));

			TextView sell2VolTextView = (TextView) findViewById(R.id.sell2VolTextView);
			long sell2Vol = Math.round(Double.parseDouble(stockInfo[23]) / 100);
			sell2VolTextView.setText(String.format("%1$9d", sell2Vol));

			TextView sell2TextView = (TextView) findViewById(R.id.sell2TextView);
			double sell2Price = Double.parseDouble(stockInfo[24]);
			sell2TextView.setText(String.format("%.02f", sell2Price));

			TextView sell1VolTextView = (TextView) findViewById(R.id.sell1VolTextView);
			long sell1Vol = Math.round(Double.parseDouble(stockInfo[21]) / 100);
			sell1VolTextView.setText(String.format("%1$9d", sell1Vol));

			TextView sell1TextView = (TextView) findViewById(R.id.sell1TextView);
			double sell1Price = Double.parseDouble(stockInfo[22]);
			sell1TextView.setText(String.format("%.02f", sell1Price));

			TextView buy1VolTextView = (TextView) findViewById(R.id.buy1VolTextView);
			long buy1Vol = Math.round(Double.parseDouble(stockInfo[11]) / 100);
			buy1VolTextView.setText(String.format("%1$9d", buy1Vol));

			TextView buy1TextView = (TextView) findViewById(R.id.buy1TextView);
			double buy1Price = Double.parseDouble(stockInfo[12]);
			buy1TextView.setText(String.format("%.02f", buy1Price));

			TextView buy2VolTextView = (TextView) findViewById(R.id.buy2VolTextView);
			long buy2Vol = Math.round(Double.parseDouble(stockInfo[13]) / 100);
			buy2VolTextView.setText(String.format("%1$9d", buy2Vol));

			TextView buy2TextView = (TextView) findViewById(R.id.buy2TextView);
			double buy2Price = Double.parseDouble(stockInfo[14]);
			buy2TextView.setText(String.format("%.02f", buy2Price));

			TextView buy3VolTextView = (TextView) findViewById(R.id.buy3VolTextView);
			long buy3Vol = Math.round(Double.parseDouble(stockInfo[15]) / 100);
			buy3VolTextView.setText(String.format("%1$9d", buy3Vol));

			TextView buy3TextView = (TextView) findViewById(R.id.buy3TextView);
			double buy3Price = Double.parseDouble(stockInfo[16]);
			buy3TextView.setText(String.format("%.02f", buy3Price));

			TextView buy4VolTextView = (TextView) findViewById(R.id.buy4VolTextView);
			long buy4Vol = Math.round(Double.parseDouble(stockInfo[17]) / 100);
			buy4VolTextView.setText(String.format("%1$9d", buy4Vol));

			TextView buy4TextView = (TextView) findViewById(R.id.buy4TextView);
			double buy4Price = Double.parseDouble(stockInfo[18]);
			buy4TextView.setText(String.format("%.02f", buy4Price));

			TextView buy5VolTextView = (TextView) findViewById(R.id.buy5VolTextView);
			long buy5Vol = Math.round(Double.parseDouble(stockInfo[19]) / 100);
			buy5VolTextView.setText(String.format("%1$9d", buy5Vol));

			TextView buy5TextView = (TextView) findViewById(R.id.buy5TextView);
			double buy5Price = Double.parseDouble(stockInfo[20]);
			buy5TextView.setText(String.format("%.02f", buy5Price));

			int white = getResources().getColor(R.color.white);
			int red = getResources().getColor(R.color.red);
			int green = getResources().getColor(R.color.green);

			int color = white;
			if (diff > 0) {
				color = red;
			} else if (diff < 0) {
				color = green;
			}

			currentPriceTextView.setTextColor(color);
			yesterdayPriceTextView.setTextColor(white);
			openTextView.setTextColor(getColor(openPrice, yesterdayPrice));
			raisePercentageTextView.setTextColor(color);
			raiseDiffTextView.setTextColor(color);
			currentPriceTextView.setTextColor(color);
			lowestTextView.setTextColor(getColor(lowPrice, yesterdayPrice));
			highestTextView.setTextColor(getColor(highestPrice, yesterdayPrice));
			// dealAmountTextView.setTextColor(white);
			// dealVolTextView.setTextColor(white);
			sell5TextView.setTextColor(getColor(sell5Price, yesterdayPrice));
			sell5VolTextView.setTextColor(getColor(sell5Price, yesterdayPrice));
			sell4TextView.setTextColor(getColor(sell4Price, yesterdayPrice));
			sell4VolTextView.setTextColor(getColor(sell4Price, yesterdayPrice));
			sell3TextView.setTextColor(getColor(sell3Price, yesterdayPrice));
			sell3VolTextView.setTextColor(getColor(sell3Price, yesterdayPrice));
			sell2TextView.setTextColor(getColor(sell2Price, yesterdayPrice));
			sell2VolTextView.setTextColor(getColor(sell2Price, yesterdayPrice));
			sell1TextView.setTextColor(getColor(sell1Price, yesterdayPrice));
			sell1VolTextView.setTextColor(getColor(sell1Price, yesterdayPrice));
			buy1TextView.setTextColor(getColor(buy1Price, yesterdayPrice));
			buy1VolTextView.setTextColor(getColor(buy1Price, yesterdayPrice));
			buy2TextView.setTextColor(getColor(buy2Price, yesterdayPrice));
			buy2VolTextView.setTextColor(getColor(buy2Price, yesterdayPrice));
			buy3TextView.setTextColor(getColor(buy3Price, yesterdayPrice));
			buy3VolTextView.setTextColor(getColor(buy3Price, yesterdayPrice));
			buy4TextView.setTextColor(getColor(buy4Price, yesterdayPrice));
			buy4VolTextView.setTextColor(getColor(buy4Price, yesterdayPrice));
			buy5TextView.setTextColor(getColor(buy5Price, yesterdayPrice));
			buy5VolTextView.setTextColor(getColor(buy5Price, yesterdayPrice));

			setProgressBarIndeterminateVisibility(false);
		}
	}

	private int getColor(double price, double basePrice) {
		int white = getResources().getColor(R.color.white);
		int red = getResources().getColor(R.color.red);
		int green = getResources().getColor(R.color.green);
		if (price > basePrice) {
			return red;
		} else if (price < basePrice) {
			return green;
		}
		return white;
	}

	public void openTimeChart(View v) {
		Intent intent = new Intent(this, TimeChartActivity.class);
		intent.putExtra(MainActivity.STOCK_SYMBOL, stockSymbol);
		startActivity(intent);
	}

	public void openKChart(View v) {
		Intent intent = new Intent(this, KChartActivity.class);
		intent.putExtra(MainActivity.STOCK_SYMBOL, stockSymbol);
		startActivity(intent);
	}

}
