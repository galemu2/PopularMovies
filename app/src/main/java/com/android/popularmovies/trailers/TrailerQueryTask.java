package com.android.popularmovies.trailers;

import android.os.AsyncTask;
import com.android.popularmovies.DetailsActivity;
import org.json.JSONArray;
import java.io.IOException;
import static com.android.popularmovies.MainActivity.getJsonArrayFromString;
import static com.android.popularmovies.MainActivity.runner;


public class TrailerQueryTask extends AsyncTask<String, Void, String> {


    public static JSONArray trailerJsonArray;

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
        trailerJsonArray = getJsonArrayFromString(s);

        DetailsActivity.trailerAdaptor.notifyDataSetChanged();
    }


}
