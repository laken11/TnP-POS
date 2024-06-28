package com.sysbeams.thumbandpin

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sysbeams.thumbandpin.api.models.User
import retrofit2.Callback


class SharedPreferencesHelper {
    companion object {
        val PREFERENCE_NAME: String = "Data"
        // Store object in SharedPreferences
        inline fun <reified T> storeObject(context: Context, key: String, obj: T) {
            val sharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            val gson = Gson()
            val json = gson.toJson(obj)
            editor.putString(key, json)
            editor.apply()
        }

        // Retrieve object from SharedPreferences
        inline fun <reified T> retrieveObject(context: Context, key: String): T? {
            val sharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
            val gson = Gson()
            val json = sharedPreferences.getString(key, null) ?: return null
            return gson.fromJson(json, T::class.java)
        }

        inline fun <reified T> storeObjects(context: Context, key: String, obj: List<T>) {
            val sharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            val gson = Gson()
            val json = gson.toJson(obj)
            editor.putString(key, json)
            editor.apply()
        }

        // Retrieve object from SharedPreferences
        inline fun <reified T> retrieveObjects(context: Context, key: String): MutableList<T> {
            val sharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
            val gson = Gson()
            val json = sharedPreferences.getString(key, null) ?: return mutableListOf()
            val typeToken = object : TypeToken<List<T>>() {}.type
            return gson.fromJson(json, typeToken)
        }
    }

}