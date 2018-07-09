package com.android.popularmovies;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.popularmovies.favMovies.AppDatabase;
import com.android.popularmovies.favMovies.FavEntry;
import com.android.popularmovies.listMoviesHelper.MovieQueryTask;
import com.android.popularmovies.reviews.Reviews;
import com.android.popularmovies.reviews.ReviewsAdaptor;
import com.android.popularmovies.reviews.ReviewsQueryTask;
import com.android.popularmovies.trailers.TrailerItemClickListener;
import com.android.popularmovies.trailers.TrailerQueryTask;
import com.android.popularmovies.trailers.TrailerRecyclerVIewAdaptor;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.android.popularmovies.MainActivity.mToastShort;

public class DetailsActivity extends AppCompatActivity implements TrailerItemClickListener {

    public static final String IMAGE_URL = "image_url";
    public static final String MOVIE_TITLE = "movie-title";
    public static final String VOTER_RATING = "voter-rating";
    public static final String RELEASE_DATE = "release-date";
    public static final String OVERVIEW = "overview_";
    public static final String MOVIE_ID = "the-id-for-the-movie";

    public static final String NO_NETWORK = "Network Connection not available";

    private static final String TAG = DetailsActivity.class.getSimpleName();
    public static final String TRAILER_KEY = "key";
    public static final String YOUTUBE = "https://www.youtube.com/watch?v=";
    public static final String YOUTUBE_APP = "youtube";
    private static final String MOVIE_REVIEWS = "/reviews";
    public static final String AUTHOR = "author";
    public static final String CONTENT = "content";
    private static final String NO_REVIEWS = "No Reviews";
    private TextView textViewTitle, textViewVoterRating, textViewReleaseDate, textViewOverview;

    public static final String OUT_OF_TEN = "/10";

    private ImageView mImageView;
    private Toolbar mToolbar;
    private ImageButton favButton;
    //images to show when movie is selected
    private Drawable drawableDeSelect, drawableSelect;

    private RecyclerView trailersRecyclerView;
    public static TrailerRecyclerVIewAdaptor trailerAdaptor;
    private RecyclerView.LayoutManager layoutManager;

    //MainActivity.BASE_URL+{movie_id}+TRAILER_VIDEO+MainActivity.QUERY_API_KEY+MainActivity.API_KEY+ LANGUAGE_EN_US;
    //https://api.themoviedb.org/3/movie/{movie_id}/videos?api_key=<<api_key>>&language=en-US
    public static final String LANGUAGE_EN_US = "&language=en-US";
    public static final String TRAILER_VIDEO = "/videos";

    //hold the id of the movie
    private String movieID = null;
    //hold the image url, will be saved into database
    private String imageUrl = null;
    //value holds the movie title
    private String title = null;

    private ArrayList<Reviews> reviewsList;
    private ReviewsAdaptor reviewAdaptor;
    private AlertDialog mAlertDialog;

    //Database
    private AppDatabase mDb;
    private static List<FavEntry> favMovieList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        //database instantiation
        mDb = AppDatabase.getInstance(this);

        mToolbar = findViewById(R.id.toolBar);
        setSupportActionBar(mToolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        drawableSelect = ContextCompat.getDrawable(this, R.drawable.ic_star_white_24dp);
        drawableDeSelect = ContextCompat.getDrawable(this, R.drawable.ic_star_border_white_24dp);
        favButton = findViewById(R.id.image_button);

        favButton.setVisibility(View.VISIBLE);
        favButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                addMovieToFav();
            }
        });

        mImageView = findViewById(R.id.image_view);

        textViewTitle = findViewById(R.id.text_view_title);
        textViewVoterRating = findViewById(R.id.text_view_voter_rating);
        textViewReleaseDate = findViewById(R.id.text_view_release_date);
        textViewOverview = findViewById(R.id.text_View_overview);

        trailersRecyclerView = findViewById(R.id.trailer_recyclerView);
        trailerAdaptor = new TrailerRecyclerVIewAdaptor(this, this);
        layoutManager = new LinearLayoutManager(this);
        trailersRecyclerView.setLayoutManager(layoutManager);
        trailersRecyclerView.setAdapter(trailerAdaptor);

        MainViewModel viewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        populateUi(getIntent(), viewModel);

        String trailerUrl = trailerUrl(movieID);
        String reviewsUrl = reviewsUrl(movieID);

        if (MovieQueryTask.getNetworkStatus(this)) {
            //video query task
            new TrailerQueryTask().execute(trailerUrl);

            //trailer query task
            new ReviewsQueryTask().execute(reviewsUrl);
        } else {
            mToastShort(NO_NETWORK, this);
        }


    }

    private void addMovieToFav() {
        /**add fav movie with
         * @param movieID and
         * @param imageUrl*/
        boolean hasMovies = dbHasMovie(favMovieList, movieID);
        final int[] deleted = {-2};

        if (!hasMovies) {
            AppExecutor.getsInstance().diskIO().execute(new Runnable() {
                @Override
                public void run() {
                    FavEntry favEntry = new FavEntry(title, movieID, imageUrl);
                    mDb.favDao().insertTask(favEntry);
                }
            });

            favButton.setImageDrawable(drawableSelect);

        } else {

            AppExecutor.getsInstance().diskIO().execute(new Runnable() {
                @Override
                public void run() {
                    deleted[0] = mDb.favDao().deleteById(movieID);
                }

            });

            favButton.setImageDrawable(drawableDeSelect);
            MainActivity.mToastShort("deleted", DetailsActivity.this);


        }
    }

    private ArrayList<Reviews> populateReviewList(JSONArray reviewJsonArray) {
        ArrayList<Reviews> reviewsList = new ArrayList<>();
        int length = 0;

        if (reviewJsonArray != null)
            length = reviewJsonArray.length();

        for (int i = 0; i < length; i++) {

            try {
                JSONObject o = (JSONObject) reviewJsonArray.get(i);

                String auth = o.getString(AUTHOR);

                String comment = o.getString(CONTENT);

                reviewsList.add(new Reviews(auth, comment));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return reviewsList;
    }

    private void populateUi(Intent intent, MainViewModel viewModel) {
        imageUrl = intent.getStringExtra(IMAGE_URL);
        Picasso.with(this).load(imageUrl).into(mImageView);

        title = intent.getStringExtra(MOVIE_TITLE);
        textViewTitle.setText(title);

        String voterRating = intent.getStringExtra(VOTER_RATING);
        voterRating = voterRating + OUT_OF_TEN;
        textViewVoterRating.setText(voterRating);

        String releaseDate = intent.getStringExtra(RELEASE_DATE);

        /** Reference: https://stackoverflow.com/a/30754863/7504259
         * Date: Jun 10 '15 at 11:24
         * Name: Ivo Stoyanov
         * */
        Date date = java.sql.Date.valueOf(releaseDate);
        String formattedReleaseDate = DateFormat.getDateInstance().format(date);

        textViewReleaseDate.setText(formattedReleaseDate);

        String overView = intent.getStringExtra(OVERVIEW);
        textViewOverview.setText(overView);

        movieID = intent.getStringExtra(MOVIE_ID);


        viewModel.getFavs().observe(DetailsActivity.this, new Observer<List<FavEntry>>() {
            @Override
            public void onChanged(@Nullable List<FavEntry> entries) {
                favMovieList = entries;
                //if the movie is in the fav list then change the button
                if (dbHasMovie(favMovieList, movieID)) {
                    favButton.setImageDrawable(drawableSelect);
                } else {
                    favButton.setImageDrawable(drawableDeSelect);
                }
            }
        });

    }

    //WARNING: this method must run on a background thread
    public static boolean dbHasMovie(final List<FavEntry> entries, final String movieId) {

         final List<String> movies = new ArrayList<>();
        //List<FavEntry> favEntries = db.favDao().listAllFavMovieEntry();
        for (FavEntry e : entries) {
            movies.add(e.getMovieId());
        }
        return movies.contains(movieId);
    }

    /**
     * Used to generate trailer fetch url as string
     */
    private String trailerUrl(String movieId) {
        //https://api.themoviedb.org/3/movie/{movie_id}/videos?api_key=<<api_key>>&language=en-US
        String s = MainActivity.BASE_URL + movieId + TRAILER_VIDEO + MainActivity.QUERY_API_KEY + MainActivity.API_KEY + LANGUAGE_EN_US;
        return s;
    }

    private String reviewsUrl(String movieId) {

        // https://api.themoviedb.org/3/movie/{movie_id}/reviews?api_key=<<api_key>>&language=en-US
        //19404
        //https://api.themoviedb.org/3/movie/19404/reviews?api_key=7b7e4afa825b051ee3d65cde0556b907&language=en-US
        String s = MainActivity.BASE_URL + movieId + MOVIE_REVIEWS + MainActivity.QUERY_API_KEY + MainActivity.API_KEY + LANGUAGE_EN_US;
        return s;
    }

    @Override
    public void showSelectedTrailer(int position) {

        //https://api.themoviedb.org/3/movie/{movie_id}/videos?api_key=<<api_key>>&language=en-US
        //19404 //movie ID
        //https://api.themoviedb.org/3/movie/19404/videos?api_key=7b7e4afa825b051ee3d65cde0556b907&language=en-US
        //youtube url
        //https://www.youtube.com/watch?v=<<key>>


        String key = null;
        key = getJsonFromJsonArray(position, TRAILER_KEY);

        if (key != null) {
            String youTubeUrlString = YOUTUBE + key;
            Uri youTubeUrl = Uri.parse(youTubeUrlString);
            Intent intent = new Intent(Intent.ACTION_VIEW, youTubeUrl);


            startActivity(intent);
        }
    }

    public static String getJsonFromJsonArray(int position, String name) {
        String key = null;
        try {

            JSONObject s = TrailerQueryTask.trailerJsonArray.getJSONObject(position);
            key = s.getString(name);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return key;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.fav_movie) {
            MainActivity.mToastShort("id found", this);
            return true;
        } else if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Button: Reviews listed in alertDialog
     */
    public void readReviews(View view) {

        reviewsList = new ArrayList<>();
        reviewsList = populateReviewList(ReviewsQueryTask.reviewJsonArray);
        int size = reviewsList.size();


        if (size > 0) {

            reviewAdaptor = new ReviewsAdaptor(this, reviewsList);

            final AlertDialog.Builder builder = new AlertDialog.Builder(DetailsActivity.this);

            builder.setAdapter(reviewAdaptor, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (mAlertDialog.isShowing())
                        mAlertDialog.cancel();
                }
            });

            builder.setView(R.layout.reviews_list);

            builder.setCancelable(true);

            mAlertDialog = builder.create();
            mAlertDialog.show();


        } else {
            MainActivity.mToastShort(NO_REVIEWS, this);
        }

    }
}
