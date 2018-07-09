package com.android.popularmovies;

import android.app.ActivityOptions;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.popularmovies.favMovies.AppDatabase;
import com.android.popularmovies.favMovies.FavEntry;
import com.android.popularmovies.listMoviesHelper.MovieItemClickListener;
import com.android.popularmovies.listMoviesHelper.MovieQueryTask;
import com.android.popularmovies.listMoviesHelper.MovieRecyclerViewAdaptor;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.android.popularmovies.DetailsActivity.NO_NETWORK;
import static com.android.popularmovies.listMoviesHelper.MovieQueryTask.getNetworkStatus;

public class MainActivity extends AppCompatActivity implements MovieItemClickListener {

    public static final String TITLE = "title";
    public static final String VOTE_AVERAGE = "vote_average";
    public static final String RELEASE_DATE = "release_date";
    public static final String OVERVIEW = "overview";
    public static final String POSTER_PATH = "poster_path";
    public static final String ID_MOVIE = "id";

    //http://api.themoviedb.org/3/movie/popular?api_key=
    public static final String BASE_URL = "http://api.themoviedb.org/3/movie/";
    public static final String QUERY_API_KEY = "?api_key=";
    //https://api.themoviedb.org/3/movie/{movie_id}/videos?api_key=<<api_key>>&language=en-US

    private static final String TAG = MainActivity.class.getSimpleName();

    //get the results array with the movie details
    public static final String RESULTS = "results";

    private static Toast mToast;

    private static OkHttpClient client;

    private Toolbar mToolbar;
    private String topRated_toolBar, popular_toolBar, favorite_toolBar;

    public static RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private TextView mTextView;
    public static MovieRecyclerViewAdaptor myAdaptor;

    private static String queryUrl;
    private static String sortingOption = null;

     //http://api.themoviedb.org/3/movie/popular?api_key=[YOUR_API_KEY]
    public static final String API_KEY = "[YOUR_API_KEY]";
    private List<FavEntry> favMoviesList = new ArrayList<>();

    private int selected = -1;
    private static final String CURRENT_LIST_SELECTION = "the_view_selected should-be";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mToolbar = findViewById(R.id.toolBar);
        setSupportActionBar(mToolbar);
        mTextView = findViewById(R.id.textView);
        client = new OkHttpClient();

        mRecyclerView = findViewById(R.id.grid_view);

        /** Source: https://stackoverflow.com/a/50075019/7504259
         * Date: Apr 28, 2018
         * Name: raevilman*/
        layoutManager = new GridLayoutManager(this, 2);
        mRecyclerView.setLayoutManager(layoutManager);

        sortingOption = getResources().getString(R.string.top_rating);

        popular_toolBar = getResources().getString(R.string.popular_movie_label);
        topRated_toolBar = getResources().getString(R.string.top_rated_label);
        favorite_toolBar = getResources().getString(R.string.favorite_movie_label);

        mTextView.setText(topRated_toolBar);

        myAdaptor = new MovieRecyclerViewAdaptor(this, MainActivity.this, this);
        mRecyclerView.setAdapter(myAdaptor);

        if (savedInstanceState != null) {
            this.selected = savedInstanceState.getInt(CURRENT_LIST_SELECTION);
        }

        switch (selected) {
            case 1:
                listFavoriteMovies();
                break;
            case 2:
                String titleTopRated = topRated_toolBar;
                int idTopRated = R.string.top_rating;
                getListOfMovies(this, titleTopRated, idTopRated);
                break;

            default:
                String titlePopular = popular_toolBar;
                int idPopular = R.string.popular_movies;
                getListOfMovies(this, titlePopular, idPopular);

        }


        setUpViewModel();
    }

    /** */
    @NonNull
    private String getQueryUrl(String soring) {
        return BASE_URL + soring + QUERY_API_KEY + API_KEY;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int selected = item.getItemId();

        if (selected == R.id.favorite_movies) {
            this.selected = 1;

            listFavoriteMovies();
        } else if (selected == R.id.top_rated) {
            this.selected = 2;
            getListOfMovies(this, topRated_toolBar, R.string.top_rating);

        } else if (selected == R.id.popular_movies) {
            this.selected = -1;
            getListOfMovies(this, popular_toolBar, R.string.popular_movies);

        }


        return super.onOptionsItemSelected(item);
    }

    private void listFavoriteMovies() {
        mTextView.setText(favorite_toolBar);
        MovieQueryTask.movieJsonArray = null;
        myAdaptor.showFavMovies(favMoviesList);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(CURRENT_LIST_SELECTION, this.selected);

        super.onSaveInstanceState(outState);
    }

    private void setUpViewModel() {

        MainViewModel viewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        viewModel.getFavs().observe(this, new Observer<List<FavEntry>>() {
            @Override
            public void onChanged(@Nullable List<FavEntry> favEntries) {

                favMoviesList = favEntries;

                myAdaptor.showFavMovies(favMoviesList);

            }
        });
    }

    /*Helper method that query the list of movies */
    private void getListOfMovies(Context c, String toolBar, int id) {
        if (sortingOption.equals(getResources().getString(id)) //R.string.popular_movies
                && MovieQueryTask.movieJsonArray != null) {
            return;
        }

        sortingOption = getResources().getString(id);

        queryUrl = getQueryUrl(sortingOption);

        if (getNetworkStatus(c)) {
            new MovieQueryTask().execute(queryUrl);

        } else {

            mToastShort(NO_NETWORK, c);

        } //popular_toolBar
        mTextView.setText(toolBar);
        mRecyclerView.scrollToPosition(0);
        myAdaptor.notifyDataSetChanged();
    }

    /**
     * Reference:
     * 1. https://square.github.io/okhttp/
     * 2. https://developers.themoviedb.org/3/movies/get-popular-movies
     */
    public static String runner(String url) throws IOException {

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();


        Response response = client.newCall(request).execute();

        return response.body() != null ? response.body().string() : null;
    }

    /**
     * Helper method to get the movie details array in Json format
     */
    public static JSONArray getJsonArrayFromString(String JSONString) {

        JSONObject object;
        JSONArray jsonArray = null;

        try {
            object = new JSONObject(JSONString);
            jsonArray = object.getJSONArray(RESULTS);

        } catch (JSONException e) {
            e.printStackTrace();
        }


        return jsonArray;
    }

    public static void mToastShort(String text, Context context) {
        if (mToast != null) mToast.cancel();
        mToast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
        mToast.show();

    }

    @Override
    public void onItemSelected(View view, ImageView imageView, int position) {

        if (MovieQueryTask.movieJsonArray != null) {
            JSONObject jsonObject;
            String imageUrl = null;
            String movieTitle = null;
            String voterRating = null;
            String releaseDate = null;
            String overview = null;
            String id = null;

            try {
                jsonObject = MovieQueryTask.movieJsonArray.getJSONObject(position);

                imageUrl = MovieRecyclerViewAdaptor.imagePath(position, MovieRecyclerViewAdaptor.POSTER_SIZE[2]);

                movieTitle = jsonObject.getString(TITLE);
                voterRating = jsonObject.getString(VOTE_AVERAGE);
                releaseDate = jsonObject.getString(RELEASE_DATE);
                overview = jsonObject.getString(OVERVIEW);
                //movie id
                id = jsonObject.getString(ID_MOVIE);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            mToastShort(movieTitle, this);

            Intent intent = new Intent(this, DetailsActivity.class);
            intent.putExtra(DetailsActivity.IMAGE_URL, imageUrl);
            intent.putExtra(DetailsActivity.MOVIE_TITLE, movieTitle);
            intent.putExtra(DetailsActivity.VOTER_RATING, voterRating);
            intent.putExtra(DetailsActivity.RELEASE_DATE, releaseDate);
            intent.putExtra(DetailsActivity.OVERVIEW, overview);
            intent.putExtra(DetailsActivity.MOVIE_ID, id);

            startDetailsActivity(view, intent);
        } else {

            favMovieItemSelected(view, position);
        }
    }

    private void favMovieItemSelected(final View view, final int position) {

        final Intent intent = new Intent(this, DetailsActivity.class);

        AppExecutor.getsInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {

                //get movie title, id, and poster path from database
                String movieId = favMoviesList.get(position).getMovieId();
                String posterPath = favMoviesList.get(position).getPosterPath();
                String movieTitle = favMoviesList.get(position).getMovieTitle();
                intent.putExtra(DetailsActivity.MOVIE_TITLE, movieTitle);

                intent.putExtra(DetailsActivity.MOVIE_ID, movieId);
                intent.putExtra(DetailsActivity.IMAGE_URL, posterPath);

                String urlForQuery = queryMovieFromID(movieId);

                if (getNetworkStatus(MainActivity.this)) {

                    String queryResult;
                    try {
                        //must run on background thread
                        queryResult = runner(urlForQuery);

                        JSONObject jsonObject = new JSONObject(queryResult);

                        //String movieTitle = jsonObject.getString(TITLE);
                        String voterRating = jsonObject.getString(VOTE_AVERAGE);
                        String releaseDate = jsonObject.getString(RELEASE_DATE);
                        String overview = jsonObject.getString(OVERVIEW);


                        intent.putExtra(DetailsActivity.VOTER_RATING, voterRating);
                        intent.putExtra(DetailsActivity.RELEASE_DATE, releaseDate);
                        intent.putExtra(DetailsActivity.OVERVIEW, overview);

                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        startDetailsActivity(view, intent);
                    }
                });
            }
        });
    }

    private String queryMovieFromID(String movieId) {
        return BASE_URL + movieId + QUERY_API_KEY + API_KEY + DetailsActivity.LANGUAGE_EN_US;
    }

    /**
     * helper method to animate start of detail activity
     */
    private void startDetailsActivity(View view, Intent intent) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            String transitionName = this.getResources().getString(R.string.transition_name);
            Pair<View, String> pair = new Pair<>(view, transitionName);
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this, pair);
            this.startActivity(intent, options.toBundle());
        } else {
            this.startActivity(intent);
        }
    }
}
