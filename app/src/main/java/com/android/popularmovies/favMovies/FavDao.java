package com.android.popularmovies.favMovies;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface FavDao {


    @Query("SELECT * FROM favMovies ORDER BY movieId ASC")
    LiveData<List<FavEntry>> loadAllLiveData();

    @Insert
    void insertTask(FavEntry favEntry);


    /**Source:  https://stackoverflow.com/a/47554641/7504259
     * Date:    Nov. 29, 2017
     * Name:    Maragues*/
    @Query("DELETE FROM favMovies WHERE movieId = :movie_id")
    int deleteById(String movie_id);


}
