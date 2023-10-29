package com.example.showsrecommendation.di

import android.app.Application
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.showsrecommendation.models.MainUiState
import com.example.showsrecommendation.network.MovieApi
import com.example.showsrecommendation.repository.MovieRepository
import com.example.showsrecommendation.util.Constants.Companion.API_BASE_URL
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.scopes.ViewModelScoped
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {


    // exoplayer unused
    @Provides
    @Singleton
    fun provideExoPlayer(app: Application): Player {
        return ExoPlayer.Builder(app)
            .build()
    }

    @Provides
    @Singleton
    fun provideMovieApi(moshi: Moshi): MovieApi {
        return Retrofit.Builder()
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .baseUrl(API_BASE_URL)
            .build()
            .create(MovieApi::class.java)
    }

    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }
    @Provides
    @Singleton
    fun provideRepository(movieApi: MovieApi): MovieRepository {
        return MovieRepository(movieApi)
    }

    @Provides
    @Singleton
    // provides the Ui state for the viewmodel/UI
    fun provideMainUiState() : MainUiState {
        return MainUiState()
    }
}