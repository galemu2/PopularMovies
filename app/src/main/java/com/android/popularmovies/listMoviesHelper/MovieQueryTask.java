package com.android.popularmovies.listMoviesHelper;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

import org.json.JSONArray;

import java.io.IOException;

import static com.android.popularmovies.MainActivity.getJsonArrayFromString;
import static com.android.popularmovies.MainActivity.mRecyclerView;
import static com.android.popularmovies.MainActivity.runner;

public class MovieQueryTask extends AsyncTask<String, Void, String> {

    private static final String TAG = MovieQueryTask.class.getSimpleName();
    public static JSONArray movieJsonArray;




    @Override
    protected String doInBackground(String... urls) {
        String url = urls[0];
        String queryResult = null;
        try {
            queryResult = runner(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return queryResult;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);

        movieJsonArray = getJsonArrayFromString(s);

        mRecyclerView.getAdapter().notifyDataSetChanged();
    }

    /**
     * Reference: https://developer.android.com/training/monitoring-device-state/connectivity-monitoring
     */
    public static boolean getNetworkStatus(Context context) {

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (connectivityManager != null) {
            networkInfo = connectivityManager.getActiveNetworkInfo();
        }
        return networkInfo != null && networkInfo.isConnected();
    }
}
