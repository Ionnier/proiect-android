package com.ionnier.pdma

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ionnier.pdma.data.Languages
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

object Settings {
    private lateinit var sharedPreferences: SharedPreferences
    private const val LANGUAGE_SP = "language_sp"
    private const val RANDOM_COLORS = "random_colors_sp"
    private const val GOAL_CALORIES = "calories_sp"

    fun init(context: Context) {
        sharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    }

    var random_colors: Boolean
        get() = sharedPreferences.getBoolean(RANDOM_COLORS, false)
        set(value) {
            sharedPreferences.edit().putBoolean(RANDOM_COLORS, value).apply()
        }

    var goal_calories: Int
        get() = sharedPreferences.getInt(GOAL_CALORIES, 0)
        set(value) {
            sharedPreferences.edit().putInt(GOAL_CALORIES, value).apply()
        }

    fun setPreferedLanguage(language: Languages){
        sharedPreferences.edit().putString(LANGUAGE_SP, Json.encodeToString(language)).apply()
    }

    fun getPreferedLanguage(): Languages? {
        val asd = sharedPreferences.getString(LANGUAGE_SP, null) ?: return null
        return try {
            Json.decodeFromString<Languages>(asd)
        } catch (e: java.lang.Exception) {
            null
        }
    }
}

