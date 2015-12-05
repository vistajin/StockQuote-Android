package com.vista.stockquote;

import java.util.Arrays;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity {

	public static final String STOCK_SYMBOL = "com.vista.stockquote.STOCK_SYMBOL";
	public static final String MY_STOCK = "com.vista.stockquote.MY_STOCK";
	public static final String TAG = "STOCK";

	private SharedPreferences myStocks;
	private TableLayout stockScrollView;
	private EditText stockSymbolEditText;
	private Button addStockButton;
	private Button clearListButton;
	private Button refreshListButton;

	private boolean timeoutAlert = false;
	private boolean[] loadedIndicator; 

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);  
		setContentView(R.layout.activity_main);

		myStocks = getSharedPreferences(MY_STOCK, MODE_PRIVATE);		

		stockScrollView = (TableLayout) findViewById(R.id.stockScrollView);
		stockSymbolEditText = (EditText) findViewById(R.id.stockSymbolEditText);
		addStockButton = (Button) findViewById(R.id.addStockButton);
		clearListButton = (Button) findViewById(R.id.clearListButton);
		refreshListButton = (Button) findViewById(R.id.refreshListButton);

		addStockButton.setOnClickListener(addStockButtonListener);
		clearListButton.setOnClickListener(clearListButtonListener);
		refreshListButton.setOnClickListener(refreshListButtonListener);

		refreshStockList();
	}

	private void hideKeyBoard() {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(stockSymbolEditText.getWindowToken(), 0);
	}

	private void refreshStockList() {
		setProgressBarIndeterminateVisibility(true);
		String[] stocks = myStocks.getAll().keySet().toArray(new String[0]);
		// Arrays.sort(stocks, String.CASE_INSENSITIVE_ORDER);
		int len = stocks.length;
		stockScrollView.removeAllViews();
		Arrays.sort(stocks, String.CASE_INSENSITIVE_ORDER);
		loadedIndicator = new boolean[len];
		for (int i = 0; i < len; i++) {
			appendStock(stocks[i]);
			loadedIndicator[i] = false;
		}
	}

	private void appendStock(String stockSymbol) {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View stockRow = inflater.inflate(R.layout.stock_row, null);
		TextView stockSymbolTextView = (TextView) stockRow.findViewById(R.id.stockSymbolTextView);
		stockSymbolTextView.setText(stockSymbol);
		TextView stockNameTextView = (TextView) stockRow.findViewById(R.id.stockNameTextView);
		stockNameTextView.setText(myStocks.getString(stockSymbol, null));

		// clear price / percentage
		TextView priceTextView = (TextView) stockRow.findViewById(R.id.priceTextView);
		TextView percentageTextView = (TextView) stockRow.findViewById(R.id.percentageTextView);
		priceTextView.setText("");
		percentageTextView.setText("");
		new MyAsyncTask().execute(stockSymbol);

		stockRow.setOnClickListener(onClickStockRowListerner);
		this.stockScrollView.addView(stockRow, stockScrollView.getChildCount());
	}

	// go into stock details
	private OnClickListener onClickStockRowListerner = new OnClickListener() {
		@Override
		public void onClick(View stockRow) {
			TextView stockSymbolTextView = (TextView) stockRow.findViewById(R.id.stockSymbolTextView);
			// stockSymbolTextView.setBackgroundColor(getResources().getColor(R.color.aero_blue));
			// SystemClock.sleep(500);
			// stockSymbolTextView.setBackgroundColor(getResources().getColor(R.color.black));
			String stockSymbol = stockSymbolTextView.getText().toString();
			Intent intent = new Intent(MainActivity.this, StockDetailsActivity.class);
			intent.putExtra(STOCK_SYMBOL, stockSymbol);
			startActivity(intent);
		}
	};

	private class MyAsyncTask extends AsyncTask<String, String, String> {
		@Override
		protected String doInBackground(String... params) {
			String stockURL = StockUtils.constructStockURL(params[0]);
			String result = StockUtils.getURLText(stockURL);
			if (result == StockUtils.RESPONSE_INVALID) {
				result = params[0];
			} else if (result == StockUtils.RESPONSE_TIMEOUT) {
				result = null;
			}
			return result;
		}

		@Override
		protected void onPostExecute(String stockText) {
			if (stockText == null) { // timeout
				if (timeoutAlert == false) {
					AlertDialog.Builder alertDialogBulder = new AlertDialog.Builder(MainActivity.this);
					alertDialogBulder.setTitle(R.string.timeout);
					alertDialogBulder.setPositiveButton(R.string.ok, null);
					AlertDialog alertDialog = alertDialogBulder.create();
					alertDialog.show();
					timeoutAlert = true;
					setProgressBarIndeterminateVisibility(false);
				}
				return;
			}
			boolean bRemoveInvalidStock = false;
			String[] stockInfo = StockUtils.splitStockText(stockText);
			if (stockText.length() < 50) { // TODO
				AlertDialog.Builder alertDialogBulder = new AlertDialog.Builder(MainActivity.this);
				alertDialogBulder.setTitle(R.string.invalid_stock_symbol);
				alertDialogBulder.setMessage(stockText);
				alertDialogBulder.setPositiveButton(R.string.ok, null);
				AlertDialog alertDialog = alertDialogBulder.create();
				alertDialog.show();
				bRemoveInvalidStock = true;
			}

			int stockNum = stockScrollView.getChildCount();
			for (int i = 0; i < stockNum; i++) {
				View stockRow = stockScrollView.getChildAt(i);

				TextView stockSymbolTextView = (TextView) stockRow.findViewById(R.id.stockSymbolTextView);
				String stockSymbol = stockSymbolTextView.getText().toString();
				if (bRemoveInvalidStock) {
					if (stockSymbol.equals(stockText)) {
						stockScrollView.removeView(stockRow);
						SharedPreferences myStocks = getSharedPreferences(MainActivity.MY_STOCK, MODE_PRIVATE);
						SharedPreferences.Editor preferencesEditor = myStocks.edit();
						preferencesEditor.remove(stockSymbol);
						preferencesEditor.apply();
						return;
					}
				} else if (stockSymbol.equals(stockInfo[0])) {
					TextView stockNameTextView = (TextView) stockRow.findViewById(R.id.stockNameTextView);
					stockNameTextView.setText(stockInfo[1]);
					
					// update stock name to shared preferences
					SharedPreferences.Editor preferencesEditor = myStocks.edit();
					preferencesEditor.putString(stockInfo[0], stockInfo[1]);
					preferencesEditor.apply();

					TextView priceTextView = (TextView) stockRow.findViewById(R.id.priceTextView);

					TextView percentageTextView = (TextView) stockRow.findViewById(R.id.percentageTextView);
					double yesterdayPrice = Double.parseDouble(stockInfo[3]);
					double todayPrice = Double.parseDouble(stockInfo[4]);
					double diff = todayPrice - yesterdayPrice;

					priceTextView.setText(String.format("%.02f", todayPrice));
					percentageTextView.setText(String.format("%.02f", diff / yesterdayPrice * 100) + "%");

					int color = getResources().getColor(R.color.white);
					if (diff > 0) {
						color = getResources().getColor(R.color.red);
					} else if (diff < 0) {
						color = getResources().getColor(R.color.green);
					}
					stockSymbolTextView.setTextColor(color);
					stockNameTextView.setTextColor(color);
					priceTextView.setTextColor(color);
					percentageTextView.setTextColor(color);
					loadedIndicator[i] = true;
					boolean allLoaded = true;
					for (int j = 0 ;j < stockNum; j++) {
						if (loadedIndicator[j] == false) {
							allLoaded = false;
						}
					}
					if (allLoaded) {
						setProgressBarIndeterminateVisibility(false);
					}

					return;
				}
			}

		}
	}

	private void saveStock(String stockSymbol) {
		String stock = myStocks.getString(stockSymbol, null);
		if (stock == null) { // add when doesn't exist
			SharedPreferences.Editor preferencesEditor = myStocks.edit();
			preferencesEditor.putString(stockSymbol, stockSymbol);
			preferencesEditor.apply();
			refreshStockList();
		} // if exist, do nothing
	}

	private OnClickListener addStockButtonListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			String stockSymbol = stockSymbolEditText.getText().toString().trim();
			if (StockUtils.isValidSymbol(stockSymbol)) {
				saveStock(stockSymbol);
				hideKeyBoard();				
			} else {
				AlertDialog.Builder alertDialogBulder = new AlertDialog.Builder(MainActivity.this);
				alertDialogBulder.setTitle(R.string.invalid_stock_symbol);
				alertDialogBulder.setPositiveButton(R.string.ok, null);
				AlertDialog alertDialog = alertDialogBulder.create();
				alertDialog.show();
			}
			stockSymbolEditText.setText("");
		}
	};

	private OnClickListener clearListButtonListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			AlertDialog.Builder alertDialogBulder = new AlertDialog.Builder(MainActivity.this);
			alertDialogBulder.setTitle(R.string.confirm_to_clear_sotck_list);
			alertDialogBulder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			alertDialogBulder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					SharedPreferences.Editor preferencesEditor = myStocks.edit();
					preferencesEditor.clear();
					preferencesEditor.apply();
					stockScrollView.removeAllViews();
					dialog.dismiss();
				}
			});
			AlertDialog alertDialog = alertDialogBulder.create();
			alertDialog.show();
		}
	};

	private OnClickListener refreshListButtonListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			timeoutAlert = false;
			refreshStockList();
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	// @Override
	// public boolean onOptionsItemSelected(MenuItem item) {
	// Handle action bar item clicks here. The action bar will
	// automatically handle clicks on the Home/Up button, so long
	// as you specify a parent activity in AndroidManifest.xml.
	// int id = item.getItemId();
	// if (id == R.id.action_settings) {
	// return true;
	// }
	// return super.onOptionsItemSelected(item);
	// }

	/**
	 * A placeholder fragment containing a simple view.
	 */
	// public static class PlaceholderFragment extends Fragment {
	//
	// public PlaceholderFragment() {
	// }
	//
	// @Override
	// public View onCreateView(LayoutInflater inflater, ViewGroup container,
	// Bundle savedInstanceState) {
	// View rootView = inflater.inflate(R.layout.fragment_main, container,
	// false);
	// return rootView;
	// }
	// }

	// private static Bitmap getHttpPic(String path) {
	// Bitmap bitmap = null;
	// HttpGet httpGet = new HttpGet(path);
	// DefaultHttpClient httpClient = new DefaultHttpClient();
	// try {
	// HttpResponse httpResponse = httpClient.execute(httpGet);
	// int reponseCode = httpResponse.getStatusLine().getStatusCode();
	// if (reponseCode == HttpStatus.SC_OK) {
	// InputStream inputStream = httpResponse.getEntity().getContent();
	// bitmap = BitmapFactory.decodeStream(inputStream);
	// inputStream.close();
	// }
	// } catch (ClientProtocolException e) {
	// e.printStackTrace();
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// return bitmap;
	// }

	// public static String getHttpText(String path) {
	// String text = null;
	// try {
	// URL url = new URL(path);
	// HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	// int reponseCode = connection.getResponseCode();
	// if (reponseCode == HttpURLConnection.HTTP_OK) {
	// InputStream inputStream = connection.getInputStream();
	// byte[] buffer = new byte[2048];
	// int readBytes = 0;
	// StringBuilder stringBuilder = new StringBuilder();
	// while((readBytes = inputStream.read(buffer)) > 0){
	// stringBuilder.append(new String(buffer, 0, readBytes));
	// }
	// text = stringBuilder.toString();
	// inputStream.close();
	// }
	// } catch (ClientProtocolException e) {
	// Log.e(TAG, e.getMessage());
	// } catch (IOException e) {
	// Log.e(TAG, e.getMessage());
	// }
	// return text;
	// }

}
