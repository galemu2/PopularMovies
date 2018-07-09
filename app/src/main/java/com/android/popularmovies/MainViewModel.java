package com.android.popularmovies;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import com.android.popularmovies.favMovies.AppDatabase;
import com.android.popularmovies.favMovies.FavEntry;

import java.util.List;

public class MainViewModel extends AndroidViewModel {

    private static final String TAG  = MainViewModel.class.getSimpleName();
    private LiveData<List<FavEntry>> favs;

    public MainViewModel(@NonNull Application application) {
        super(application);
        AppDatabase database  = AppDatabase.getInstance(this.getApplication());

         favs = database.favDao().loadAllLiveData();
    }

    public LiveData<List<FavEntry>> getFavs() {
        return favs;
    }
}
