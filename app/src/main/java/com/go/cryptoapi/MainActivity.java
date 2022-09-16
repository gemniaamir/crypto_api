package com.go.cryptoapi;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.hamzaahmedkhan.counteranimationtextview.CountAnimationTextView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.snackbar.Snackbar;
import com.go.cryptoapi.adapter.CoinAdapter;
import com.go.cryptoapi.model.CoinModel;

import com.go.cryptoapi.model.CryptoHistoryModel;
import com.go.cryptoapi.model.CurrencyDataModel;
import com.go.cryptoapi.model.CurrencyNameModel;
import com.go.cryptoapi.model.CurrencyRateModel;
import com.go.cryptoapi.model.SelectedCurrencyModel;
import com.yarolegovich.discretescrollview.DiscreteScrollView;
import com.yarolegovich.discretescrollview.transform.ScaleTransformer;

import org.eazegraph.lib.charts.ValueLineChart;
import org.eazegraph.lib.models.LegendModel;
import org.eazegraph.lib.models.ValueLinePoint;
import org.eazegraph.lib.models.ValueLineSeries;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MainActivity extends AppCompatActivity implements
        DiscreteScrollView.ScrollStateChangeListener<CoinAdapter.ViewHolder>,
        DiscreteScrollView.OnItemChangedListener<CoinAdapter.ViewHolder>{

    private List<CoinModel> coinModels = new ArrayList<>();

    private DiscreteScrollView currencyPicker;
    private ValueLineChart lineChart;

    String message = "";

    RelativeLayout loadingView;
    CoordinatorLayout rootView;
    CountAnimationTextView priceUSD;
    AppCompatSpinner currencySpinner;
    TextView dollarSign;
    View bottomSheet;
    AppCompatButton expandBottomView, buy;
    TextView marketCapitalUSD, volumeUSD24HR, title, symbol, rating, changePercent,
            marketPrice, availableSupplyValue, totalSupplyValue;
    ProgressBar totalIssuedSupplyProgress, availableSupplyProgress;

    private BottomSheetBehavior mBottomSheetBehavior;

    CurrencyRateModel currencyRates;
    ArrayList<CurrencyNameModel> currencyNames = new ArrayList<>();
    ArrayList<CurrencyDataModel> currencyDataModels = new ArrayList<>();
    ArrayList<CryptoHistoryModel> history = new ArrayList<>();
    String selectedCurrencyId = "";
    Long maxProgress = Long.valueOf(0), availableProgress = Long.valueOf(0);
    DecimalFormat format = new DecimalFormat("###,###,###,###");
    DecimalFormat df = new DecimalFormat("#");
    Double seletedCoinRate = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        format.setMaximumFractionDigits(0);
        df.setMaximumFractionDigits(0);

        loadingView = findViewById(R.id.loadingView);
        dollarSign = findViewById(R.id.dollarSign);
        lineChart = findViewById(R.id.lineChart);
        rootView = findViewById(R.id.rootView);
        marketCapitalUSD = findViewById(R.id.marketCapitalUSD);
        currencySpinner = findViewById(R.id.currencySpinner);
        bottomSheet = findViewById(R.id.bottom_sheet);
        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        expandBottomView = findViewById(R.id.showDetail);
        priceUSD = findViewById(R.id.priceUSD);
        volumeUSD24HR = findViewById(R.id.volumeUSD24HR);
        buy = findViewById(R.id.buy);
        title = findViewById(R.id.title);
        symbol = findViewById(R.id.symbol);
        rating = findViewById(R.id.rating);
        changePercent = findViewById(R.id.changePercent);
        marketPrice = findViewById(R.id.marketPrice);
        availableSupplyProgress = findViewById(R.id.availableSupplyProgress);
        totalIssuedSupplyProgress = findViewById(R.id.totalIssuedSupplyProgress);
        availableSupplyValue = findViewById(R.id.availableSupplyValue);
        totalSupplyValue = findViewById(R.id.totalSupplyValue);

        // parse currency names and symbols from local json file
        parseLocalJsonData();

        if (isNetworkAvailable()){
            GetCurrencyRates task = new GetCurrencyRates();
            task.execute();
        }else {
            Snackbar.make(MainActivity.this, rootView, "Internet not available",Snackbar.LENGTH_SHORT).show();
        }

        expandBottomView.setOnClickListener(view -> mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED));

        buy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.coinbase.com/"));
                startActivity(browserIntent);
            }
        });
    }

    @Override
    public void onCurrentItemChanged(@Nullable CoinAdapter.ViewHolder holder, int position) {
        //viewHolder will never be null, because we never remove items from adapter's list
        if (holder != null) {
            holder.showText();
        }
    }

    // Horizontal Scroll Picker Scrolling Listeners - STARTS //

    @Override
    public void onScrollStart(@NonNull CoinAdapter.ViewHolder holder, int position) {
        holder.hideText();
    }

    @Override
    public void onScroll(
            float position,
            int currentIndex, int newIndex,
            @Nullable CoinAdapter.ViewHolder currentHolder,
            @Nullable CoinAdapter.ViewHolder newHolder) {}

    @Override
    public void onScrollEnd(@NonNull CoinAdapter.ViewHolder holder, int position) {
        if (currencyDataModels.size() > 0){
            selectedCurrencyId = currencyDataModels.get(position).getId();

            // Load ui view details
            try {
                title.setText(currencyDataModels.get(position).getName());
                symbol.setText(currencyDataModels.get(position).getSymbol());
                rating.setText(currencyDataModels.get(position).getRank());
                if (Float.parseFloat(currencyDataModels.get(position).getChangePercent24Hr()) < 0){
                    changePercent.setTextColor(getResources().getColor(R.color.orange));
                    changePercent.setText(String.format("%.2f", Double.parseDouble(currencyDataModels.get(position).getChangePercent24Hr())) + "%");
                }else {
                    changePercent.setTextColor(getResources().getColor(R.color.success));
                    changePercent.setText((String.format("%.2f", Double.parseDouble(currencyDataModels.get(position).getChangePercent24Hr())) + "%"));
                }
                Double priceValue = Double.parseDouble(currencyDataModels.get(position).getPriceUsd());
                if (priceValue < 1){
                    priceUSD.setText(String.format("%.5f", priceValue.floatValue()));
                }else {
                    priceUSD.setDecimalFormat(new DecimalFormat("###,###,###"))
                            .setAnimationDuration(3000)
                            .countAnimation(0, priceValue.intValue());
                }
                Float volumeValue = Float.parseFloat(currencyDataModels.get(position).getVolumeUsd24Hr());
                Float marketCapitalValue = Float.parseFloat(currencyDataModels.get(position).getMarketCapUsd());
                volumeUSD24HR.setText(format.format(volumeValue));
                marketCapitalUSD.setText(format.format(marketCapitalValue));
                Double totSupVal = Double.parseDouble(df.format(Float.parseFloat(currencyDataModels.get(position).getMaxSupply())));
                Double availSupVal = Double.parseDouble(df.format(Float.parseFloat(currencyDataModels.get(position).getSupply())));
                totalSupplyValue.setText(df.format(totSupVal.doubleValue()));
                availableSupplyValue.setText(df.format(availSupVal.doubleValue()));
                maxProgress = Long.parseLong((totalSupplyValue.getText().toString()));
                availableProgress = Long.parseLong((availableSupplyValue.getText().toString()));
                totalSupplyValue.setText(format.format(totSupVal.doubleValue()));
                availableSupplyValue.setText(format.format(availSupVal.doubleValue()));
                seletedCoinRate = Double.parseDouble(currencyDataModels.get(position).getPriceUsd());

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Double percentage = ((double)availableProgress/(double)maxProgress) * 100.0;
                    availableSupplyProgress.setProgress(percentage.intValue(), true);
                    totalIssuedSupplyProgress.setProgress(100, true);
                }else {
                    Double percentage = ((double)availableProgress/(double)maxProgress) * 100.0;
                    availableSupplyProgress.setProgress(percentage.intValue());
                    totalIssuedSupplyProgress.setProgress(100);
                }

                loadConvertedMarketPrice();

            }catch (Exception ex){
                Log.w("UI Loading exception: ", ex.getMessage());
            }

            if (isNetworkAvailable()){
                GetCryptoHistory task = new GetCryptoHistory();
                task.execute();
            }else {
                Snackbar.make(MainActivity.this, rootView, "Internet not available",Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    // Horizontal Scroll Picker Scrolling Listeners - ENDS //

    private class GetCurrencyRates extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            message = "";
            bottomSheet.setVisibility(View.GONE);
            loadingView.setVisibility(View.VISIBLE);
            expandBottomView.setVisibility(View.INVISIBLE);
            dollarSign.setVisibility(View.INVISIBLE);
        }

        @Override
        protected Void doInBackground(Void... arg) {

            try {
                OkHttpClient client = new OkHttpClient();

                Request request = new Request.Builder()
                        .url(GlobalVariable.CURRENCY_API_URL + GlobalVariable.CURRENCY_API_KEY +"&symbols="
                                + URLEncoder.encode("GBP,EUR,USD,PKR,INR,CNY,AUD", "UTF-8"))
                        .get()
                        .build();
                Response response = client.newCall(request).execute();
                ResponseBody responseBody = response.peekBody(Long.MAX_VALUE);
                parseJSONStringToJSONObject(responseBody.string(), "CurrencyRates");

            } catch (Exception e) {
                e.printStackTrace();
                message = e.getMessage();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void a) {
            if (!isFinishing()){
                expandBottomView.setVisibility(View.VISIBLE);
                dollarSign.setVisibility(View.VISIBLE);
                loadingView.setVisibility(View.GONE);
                bottomSheet.setVisibility(View.VISIBLE);
                if (!message.equals("")) {
                    // Show some message from server
                    Snackbar.make(MainActivity.this, rootView, message,Snackbar.LENGTH_LONG)
                            .setAction("Close", view -> {})
                            .setActionTextColor(getResources().getColor(android.R.color.holo_red_light))
                            .show();
                } else {
                    // Success
                    if (currencyRates.getRates().size() > 0){
                        loadCurrencySpinner(currencyRates.getRates());

                        if (isNetworkAvailable()){
                            GetCryptoAssets task = new GetCryptoAssets();
                            task.execute();
                        }else {
                            Snackbar.make(MainActivity.this, rootView, "Internet not available",Snackbar.LENGTH_SHORT).show();
                        }

                    }
                }
            }
        }
    }

    private class GetCryptoAssets extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            message = "";
            bottomSheet.setVisibility(View.GONE);
            loadingView.setVisibility(View.VISIBLE);
            expandBottomView.setVisibility(View.INVISIBLE);
            dollarSign.setVisibility(View.INVISIBLE);
        }

        @Override
        protected Void doInBackground(Void... arg) {

            try {
                OkHttpClient client = new OkHttpClient();

                Request request = new Request.Builder()
                        .url("https://api.coincap.io/v2/assets")
                        .get()
                        .build();

                Response response = client.newCall(request).execute();
                String networkResp = response.body().string();
                parseJSONStringToJSONObject(networkResp, "Assets");

            } catch (Exception e) {
                e.printStackTrace();
                message = e.getMessage();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void a) {
            if (!isFinishing()){
                expandBottomView.setVisibility(View.VISIBLE);
                loadingView.setVisibility(View.GONE);
                dollarSign.setVisibility(View.VISIBLE);
                bottomSheet.setVisibility(View.VISIBLE);
                if (!message.equals("")) {
                    // Show some message from server
                    Snackbar.make(MainActivity.this, rootView, message,Snackbar.LENGTH_LONG)
                            .setAction("Close", view -> {

                            })
                            .setActionTextColor(getResources().getColor(android.R.color.holo_red_light))
                            .show();
                } else {
                    // Success - load ui views
                    loadHorizontalPicker(currencyDataModels);

                    if (isNetworkAvailable()){
                        GetCryptoHistory task = new GetCryptoHistory();
                        task.execute();
                    }else {
                        Snackbar.make(MainActivity.this, rootView, "Internet not available",Snackbar.LENGTH_SHORT).show();
                    }

                }
            }
        }
    }

    private class GetCryptoHistory extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            message = "";
            bottomSheet.setVisibility(View.GONE);
            dollarSign.setVisibility(View.INVISIBLE);
            expandBottomView.setVisibility(View.INVISIBLE);
            loadingView.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... arg) {

            try {
                OkHttpClient client = new OkHttpClient();

                Request request = new Request.Builder()
                        .url("https://api.coincap.io/v2/assets/"+selectedCurrencyId+"/history?interval=d1")
                        .addHeader("Authorization", "Bearer 42342ec5-6ab3-4eb5-b917-dbf2758dc09f")
                        .get()
                        .build();
                Response response = client.newCall(request).execute();
                ResponseBody responseBody = response.peekBody(Long.MAX_VALUE);
                parseJSONStringToJSONObject(responseBody.string(), "History");

            } catch (Exception e) {
                e.printStackTrace();
                message = e.getMessage();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void a) {
            if (!isFinishing()){
                loadingView.setVisibility(View.GONE);
                dollarSign.setVisibility(View.VISIBLE);
                expandBottomView.setVisibility(View.VISIBLE);
                bottomSheet.setVisibility(View.VISIBLE);
                if (!message.equals("")) {
                    // Show some message from server
                    Snackbar.make(MainActivity.this, rootView, message,Snackbar.LENGTH_LONG)
                            .setAction("Close", view -> {})
                            .setActionTextColor(getResources().getColor(android.R.color.holo_red_light))
                            .show();
                } else {
                    // Success
                    if (history.size() > 0){
                        loadLineChart(history);

                    }
                }
            }
        }
    }

    private void loadBottomSheetStateListener(){
        mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        break;
                    case BottomSheetBehavior.STATE_HIDDEN:
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {}
        });
    }

    private void loadLineChart(ArrayList<CryptoHistoryModel> historyList){

        lineChart.clearChart();

        ValueLineSeries series = new ValueLineSeries();
        series.setColor(R.color.lineChartColorValue);

        for (int i = 0 ; i < historyList.size() ; i++){
            series.addPoint(new ValueLinePoint(historyList.get(i).getDate(), Float.parseFloat(historyList.get(i).getPriceUsd())));
        }

        lineChart.addSeries(series);
        lineChart.startAnimation();
        ArrayList<LegendModel> legendModels = new ArrayList<>();
        legendModels.add(new LegendModel("Name"));
        lineChart.addLegend(legendModels);
    }

    private void loadHorizontalPicker(ArrayList<CurrencyDataModel> currencyModels){
        if (currencyModels.size() > 30){

            selectedCurrencyId = currencyModels.get(0).getId();

            // Load ui view details
            try {
                title.setText(currencyDataModels.get(0).getName());
                symbol.setText(currencyDataModels.get(0).getSymbol());
                rating.setText(currencyDataModels.get(0).getRank());
                if (Float.parseFloat(currencyDataModels.get(0).getChangePercent24Hr()) < 0){
                    changePercent.setTextColor(getResources().getColor(R.color.orange));
                    changePercent.setText(String.format("%.2f", Double.parseDouble(currencyDataModels.get(0).getChangePercent24Hr())) + "%");
                }else {
                    changePercent.setTextColor(getResources().getColor(R.color.success));
                    changePercent.setText((String.format("%.2f", Double.parseDouble(currencyDataModels.get(0).getChangePercent24Hr())) + "%"));
                }
                Double priceValue = Double.parseDouble(currencyDataModels.get(0).getPriceUsd());
                priceUSD.setDecimalFormat(new DecimalFormat("###,###,###"))
                        .setAnimationDuration(4000)
                        .countAnimation(0, priceValue.intValue());
                Float volumeValue = Float.parseFloat(currencyDataModels.get(0).getVolumeUsd24Hr());
                Float marketCapitalValue = Float.parseFloat(currencyDataModels.get(0).getMarketCapUsd());
                volumeUSD24HR.setText(format.format(volumeValue));
                marketCapitalUSD.setText(format.format(marketCapitalValue));
                Double totSupVal = Double.parseDouble(df.format(Float.parseFloat(currencyDataModels.get(0).getMaxSupply())));
                Double availSupVal = Double.parseDouble(df.format(Float.parseFloat(currencyDataModels.get(0).getSupply())));
                totalSupplyValue.setText(df.format(totSupVal.doubleValue()));
                availableSupplyValue.setText(df.format(availSupVal.doubleValue()));
                maxProgress = Long.parseLong((totalSupplyValue.getText().toString()));
                availableProgress = Long.parseLong((availableSupplyValue.getText().toString()));
                totalSupplyValue.setText(format.format(totSupVal.doubleValue()));
                availableSupplyValue.setText(format.format(availSupVal.doubleValue()));
                seletedCoinRate = Double.parseDouble(currencyDataModels.get(0).getPriceUsd());

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Double percentage = ((double)availableProgress/(double)maxProgress) * 100.0;
                    availableSupplyProgress.setProgress(percentage.intValue(), true);
                    totalIssuedSupplyProgress.setProgress(100, true);
                }else {
                    Double percentage = ((double)availableProgress/(double)maxProgress) * 100.0;
                    availableSupplyProgress.setProgress(percentage.intValue());
                    totalIssuedSupplyProgress.setProgress(100);
                }

                loadConvertedMarketPrice();

            }catch (Exception ex){
                Log.w("UI Loading exception: ", ex.getMessage());
            }

            for (int i = 0 ; i < 31 ; i++){
                coinModels.add(new CoinModel(currencyModels.get(i).getName(),
                        currencyModels.get(i).getSymbol().toLowerCase(),
                        currencyModels.get(i).getRank()));
            }

            currencyPicker = findViewById(R.id.coinPicker);
            currencyPicker.setSlideOnFling(true);
            currencyPicker.setAdapter(new CoinAdapter(coinModels, MainActivity.this));
            currencyPicker.addOnItemChangedListener(this);
            currencyPicker.addScrollStateChangeListener(this);
            currencyPicker.scrollToPosition(0);
            currencyPicker.setItemTransitionTimeMillis(150);
            currencyPicker.setItemTransformer(new ScaleTransformer.Builder()
                    .setMinScale(0.8f)
                    .build());
        }
    }

    private void loadConvertedMarketPrice() {
        double dollarRate = 0.0, selectedCurrencyRate = 0.0;
        String[] temp = currencySpinner.getSelectedItem().toString().split("-");
        if (temp.length > 0){
            for ( int j = 0 ; j < currencyRates.getRates().size(); j++){
                if (temp[0].replace(" ", "").equals(currencyRates.getRates().get(j).getSymbol())){
                    selectedCurrencyRate = Double.parseDouble(currencyRates.getRates().get(j).getRate());
                }
                if (currencyRates.getRates().get(j).getSymbol().equals("USD")){
                    dollarRate = Double.parseDouble(currencyRates.getRates().get(j).getRate());
                }
            }
        }
        // Convert Crypto Currency rate to selected currency
        Double convertedPrice = (selectedCurrencyRate/dollarRate) * seletedCoinRate;
        marketPrice.setText(String.format("%.2f", convertedPrice.floatValue()));
    }

    private void loadCurrencySpinner(ArrayList<SelectedCurrencyModel> rates){
        String[] currencyName = new String[rates.size()];

        for (int i = 0 ; i < rates.size() ; i++){
            for (int j = 0 ; j < currencyNames.size() ; j++){
                if (currencyNames.get(j).getSymbol().equals(rates.get(i).getSymbol())){
                    currencyName[i] = rates.get(i).getSymbol() + " - " + currencyNames.get(j).getName();
                    break;
                }
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                R.layout.spinner_item, currencyName);

        currencySpinner.setAdapter(adapter);
        adapter.setDropDownViewResource(R.layout.spin_dialog_item);
        currencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
              loadConvertedMarketPrice();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });
    }

    private void parseJSONStringToJSONObject(String networkResp, String methodName) {
        if (methodName.equals("Assets")){
            try {
                JSONObject jsonObject = new JSONObject(networkResp);
                JSONArray jsonArray = jsonObject.getJSONArray("data");
                for ( int i = 0 ;  i < jsonArray.length() ; i++){
                    JSONObject json = jsonArray.getJSONObject(i);
                    currencyDataModels.add(new CurrencyDataModel(
                            json.getString("id"),
                            json.getString("rank"),
                            json.getString("symbol"),
                            json.getString("name"),
                            json.getString("supply"),
                            json.getString("maxSupply").equals("null")? "1": json.getString("maxSupply"),
                            json.getString("marketCapUsd"),
                            json.getString("volumeUsd24Hr"),
                            json.getString("priceUsd"),
                            json.getString("changePercent24Hr"),
                            json.getString("vwap24Hr"),
                            json.getString("explorer")
                    ));
                }
            } catch (JSONException e) {
                message = e.getMessage();
                e.printStackTrace();
            }

        }
        if (methodName.equals("CurrencyRates")){
            try {
                JSONObject jsonObject = new JSONObject(networkResp);

                // Accessing rates object
                JSONObject jsonObject1 = jsonObject.getJSONObject("rates");
                Iterator<String> keys = jsonObject1.keys();
                ArrayList<SelectedCurrencyModel> rates = new ArrayList<>();

                while(keys.hasNext()) {
                    String key = keys.next();
                    if (jsonObject1.get(key) instanceof String) {
                        rates.add(new SelectedCurrencyModel(key, jsonObject1.getString(key)));
                    }
                };

                currencyRates = new CurrencyRateModel(
                        jsonObject.getString("date"),
                        jsonObject.getString("base"),
                        rates);

            } catch (JSONException e) {
                message = e.getMessage();
                e.printStackTrace();
            }
        }
        if (methodName.equals("History")){
            try {
                JSONObject jsonObject = new JSONObject(networkResp);
                JSONArray jsonArray = jsonObject.getJSONArray("data");
                if (jsonArray.length() > 0){
                    history.clear();
                    for ( int i = 0 ;  i < jsonArray.length() ; i++){
                        JSONObject json = jsonArray.getJSONObject(i);
                        history.add(new CryptoHistoryModel(
                                json.getString("priceUsd"),
                                json.getInt("time"),
                                json.getString("date").replace("T00:00:00.000Z", "")
                        ));
                    }
                }

            } catch (JSONException e) {
                message = e.getMessage();
                e.printStackTrace();
            }

        }
    }

    private void parseLocalJsonData(){
        try {
            JSONObject jsonObject = new JSONObject(loadJSONFromAsset());
            JSONArray jsonArray = jsonObject.getJSONArray("data");

            for (int i = 0 ; i < jsonArray.length(); i++){
                JSONObject json = jsonArray.getJSONObject(i);
                currencyNames.add(new CurrencyNameModel(json.getString("name"),
                        json.getString("id")));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = getAssets().open("currencyNames.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    // Check if internet is available before sending request to server for fetching data
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }
}