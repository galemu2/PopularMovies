package com.android.popularmovies.favMovies;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "favMovies")
public class FavEntry {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String movieTitle;
    private String movieId;
    private String posterPath;

    @Ignore
    public FavEntry(String movieTitle, String movieId, String posterPath ) {
        this.movieTitle = movieTitle;
        this.movieId = movieId;
        this.posterPath = posterPath;


    }

    public FavEntry(int id, String movieTitle, String movieId, String posterPath ) {

        this.id = id;
        this.movieTitle = movieTitle;
        this.movieId = movieId;
        this.posterPath = posterPath;

    }

    public String getMovieId() {
        return movieId;
    }

    public void setMovieId(String movieId) {
        this.movieId = movieId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public void setMovieTitle(String movieTitle) {
        this.movieTitle = movieTitle;
    }
}
