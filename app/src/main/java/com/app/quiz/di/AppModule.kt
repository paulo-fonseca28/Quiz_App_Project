package com.app.quiz.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import com.app.quiz.data.db.AppDb
import com.app.quiz.data.db.dao.QuestionDao
import com.app.quiz.data.db.dao.QuizDao
import com.app.quiz.data.db.dao.SessionDao
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // Firebase
    @Provides @Singleton fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()
    @Provides @Singleton fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    // Room
    @Provides @Singleton
    fun provideDb(@ApplicationContext ctx: Context): AppDb =
        Room.databaseBuilder(ctx, AppDb::class.java, "quiz_app.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides @Singleton fun provideSessionDao(db: AppDb): SessionDao = db.sessionDao()
    @Provides @Singleton fun provideQuizDao(db: AppDb): QuizDao = db.quizDao()
    @Provides @Singleton fun provideQuestionDao(db: AppDb): QuestionDao = db.questionDao()

    // DataStore (Preferences)
    @Provides @Singleton
    fun providePrefsDataStore(@ApplicationContext ctx: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            produceFile = { ctx.preferencesDataStoreFile("user_prefs") }
        )
}
