package com.example.showsrecommendation.di

import com.example.showsrecommendation.models.GridUiState
import com.example.showsrecommendation.network.MovieApi
import com.example.showsrecommendation.repository.MovieRepository
import com.example.showsrecommendation.util.Constants.Companion.API_BASE_URL
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

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
    fun provideGridUiState() : GridUiState {
        return GridUiState()
    }
}