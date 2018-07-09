package com.android.popularmovies.reviews;

import android.net.Uri;
import android.os.AsyncTask;
import com.android.popularmovies.MainActivity;
import org.json.JSONArray;
import java.io.IOException;
import static com.android.popularmovies.MainActivity.runner;
public class ReviewsQueryTask extends AsyncTask<String, Void, String> {


    public static JSONArray reviewJsonArray;

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

        reviewJsonArray = MainActivity.getJsonArrayFromString(s);


    }


}
