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
    private val LANGUAGE_SP = "language_sp"

    fun init(context: Context) {
        sharedPreferences = context.getSharedPreferences("vpn_settings", Context.MODE_PRIVATE)
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
