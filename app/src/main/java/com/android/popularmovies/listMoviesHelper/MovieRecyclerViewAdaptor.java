package com.android.popularmovies.listMoviesHelper;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.popularmovies.R;
import com.android.popularmovies.favMovies.FavEntry;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.android.popularmovies.MainActivity.POSTER_PATH;

public class MovieRecyclerViewAdaptor extends RecyclerView.Adapter<MovieRecyclerViewAdaptor.MyViewHolder> {

    private static final String TAG = MovieRecyclerViewAdaptor.class.getSimpleName();
    private final Context mContext;
    private final Activity mActivity;
    private final int mWidth;
    private final int mHeight;
    private final int mSpacing;
    private MovieItemClickListener movieItemClickListener;

    //combined URL example >>  http://image.tmdb.org/t/p/w185/nBNZadXqJSdt05SHLqgT0HuC5Gm.jpg
    private static final String BASE_URL = "http://image.tmdb.org/t/p/";
    public static final String[] POSTER_SIZE = {"w92", "w154", "w185", "w342", "w500", "w780", "original"};

    public   List<FavEntry> favMovies = new ArrayList<>();

    public MovieRecyclerViewAdaptor(Context context, Activity activity, MovieItemClickListener movieItemClickListener) {
        this.mContext = context;
        this.mActivity = activity;
        /**Reference:
         * Link: https://stackoverflow.com/a/31377616/7504259
         * Date: Jul 13 '15 at 7:27
         * Name: weigan
         * */
        mWidth = (mContext.getResources().getDisplayMetrics().widthPixels) / 2;
        double RATIO = 1.33;
        mHeight = (int) (RATIO * mWidth);

        mSpacing = (int) context.getResources().getDimension(R.dimen.padding_space);

        this.movieItemClickListener = movieItemClickListener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.list_movies, parent, false);

        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder convertView, int position) {


        Uri uri = null;

        if (MovieQueryTask.movieJsonArray != null) {
            try {
                String path = imagePath(position, POSTER_SIZE[2]);
                uri = Uri.parse(path);

            } catch (JSONException e) {
                e.printStackTrace();

            }
        } else {
            FavEntry favMovie = favMovies.get(position);

            String path = favMovie.getPosterPath();
            uri = Uri.parse(path);

        }
        //url will be loaded into imageView
        Picasso.with(mContext).load(uri).into(convertView.imageView_poster);

        final int pos = position;
        convertView.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                movieItemClickListener.onItemSelected(convertView.view, convertView.imageView_poster, pos);
            }
        });
    }

    public static String imagePath(int position, String posterSize) throws JSONException {

        JSONObject positionObject = MovieQueryTask.movieJsonArray.getJSONObject(position);

        //sample output > /7WsyChQLEftFiDOVTGkv3hFpyyt.jpg
        String imagePath = positionObject.getString(POSTER_PATH);

        return BASE_URL + posterSize + imagePath;
    }

    @Override
    public int getItemCount() {

        if (MovieQueryTask.movieJsonArray == null) {
            if (favMovies == null || favMovies.isEmpty()) {
                return 0;
            }else {
                return favMovies.size();
            }
        }

        return MovieQueryTask.movieJsonArray.length();
    }

    public void showFavMovies(List<FavEntry> favMovies) {

        this.favMovies = favMovies;
        notifyDataSetChanged();

    }


    public class MyViewHolder extends RecyclerView.ViewHolder {

        public ImageView imageView_poster;
        public View view;

        public MyViewHolder(final View itemView) {
            super(itemView);
            this.view = itemView;
            imageView_poster = itemView.findViewById(R.id.image_view_1);


            imageView_poster.setMinimumWidth(mWidth);
            imageView_poster.setMinimumHeight(mHeight);


            imageView_poster.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView_poster.setPadding(mSpacing, mSpacing, mSpacing, mSpacing);


        }
    }

}