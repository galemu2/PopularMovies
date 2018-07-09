package com.android.popularmovies.reviews;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.android.popularmovies.R;
import java.util.ArrayList;

/** Source: https://guides.codepath.com/android/Using-an-ArrayAdapter-with-ListView */
public class ReviewsAdaptor  extends ArrayAdapter<Reviews>{


    private  final String COMMENT = "COMMENT: ";
    private  final String AUTHOR = "AUTHOR: ";

    public ReviewsAdaptor(@NonNull Context context , ArrayList<Reviews> reviews) {
        super(context, 0, reviews);
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        Reviews reviews = getItem(position);

        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.review_item, parent, false);
        }

        TextView author = convertView.findViewById(R.id.textView_author);
        String auth = AUTHOR + reviews.author;
        author.setText(auth);
        TextView comment = convertView.findViewById(R.id.textVIew_comment);
        String cmnt = COMMENT +reviews.comment;
        comment.setText(cmnt);

        return convertView;
    }

    @Override
    public int getCount() {
        return super.getCount();
    }
}
