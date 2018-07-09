package com.android.popularmovies.listMoviesHelper;

import android.view.View;
import android.widget.ImageView;

public interface MovieItemClickListener {

    void onItemSelected(View view, ImageView imageView, int position);
}
