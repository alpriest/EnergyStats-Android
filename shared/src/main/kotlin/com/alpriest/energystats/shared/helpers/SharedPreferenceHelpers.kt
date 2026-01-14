package com.alpriest.energystats.shared.helpers

import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

fun <T> preference(
    sharedPreferences: SharedPreferences,
    key: String,
    defaultValue: T
): ReadWriteProperty<Any, T> = object : ReadWriteProperty<Any, T> {
    override fun getValue(thisRef: Any, property: KProperty<*>): T {
        return findPreference(key, defaultValue)
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        setPreference(key, value)
    }

    private fun findPreference(key: String, defaultValue: T): T {
        with(sharedPreferences) {
            val result: Any? = when (defaultValue) {
                is Boolean -> getBoolean(key, defaultValue)
                is String -> getString(key, defaultValue)
                is String? -> getString(key, defaultValue)
                is Double -> (getString(key, defaultValue.toString()) ?: defaultValue.toString()).toDouble()
                is Int -> getInt(key, defaultValue)
                else -> throw IllegalArgumentException("Unsupported preference type.")
            }

            @Suppress("UNCHECKED_CAST")
            return result as T
        }
    }

    private fun setPreference(key: String, value: T) {
        sharedPreferences.edit(commit = true) {
            when (value) {
                is Boolean -> putBoolean(key, value)
                is String -> putString(key, value)
                is String? -> putString(key, value)
                is Double -> putString(key, value.toString())
                is Int -> putInt(key, value)
                else -> throw IllegalArgumentException("Unsupported preference type.")
            }
        }
    }
}

// Special delegate for Nullable Doubles
fun nullableDoublePreference(
    sharedPreferences: SharedPreferences,
    key: String
): ReadWriteProperty<Any, Double?> = object : ReadWriteProperty<Any, Double?> {
    override fun getValue(thisRef: Any, property: KProperty<*>): Double? {
        return sharedPreferences.getString(key, null)?.toDoubleOrNull()
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: Double?) {
        sharedPreferences.edit(commit = true) {
            if (value == null) {
                remove(key)
            } else {
                putString(key, value.toString())
            }
        }
    }
}

fun <T> enumIntPreference(
    sharedPreferences: SharedPreferences,
    key: String,
    defaultValue: T,
    readStorage: (Int) -> T,
    writeStorage: (T) -> Int
): ReadWriteProperty<Any, T> = object : ReadWriteProperty<Any, T> {
    override fun getValue(thisRef: Any, property: KProperty<*>): T {
        with (sharedPreferences) {
            return readStorage(getInt(key, writeStorage(defaultValue)))
        }
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        with (sharedPreferences) {
            edit(commit = true) {
                putInt(key, writeStorage(value))
            }
        }
    }
}

fun <T> jsonNullablePreference(
    sharedPreferences: SharedPreferences,
    key: String,
    typeToken: TypeToken<T>,
    gson: Gson = Gson()
): ReadWriteProperty<Any, T?> = object : ReadWriteProperty<Any, T?> {

    private val type = typeToken.type

    override fun getValue(thisRef: Any, property: KProperty<*>): T? {
        val stored = sharedPreferences.getString(key, null) ?: return null
        return gson.fromJson(stored, type)
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T?) {
        sharedPreferences.edit(commit = true) {
            if (value == null) {
                remove(key)
            } else {
                putString(key, gson.toJson(value))
            }
        }
    }
}

fun <T> jsonPreference(
    sharedPreferences: SharedPreferences,
    key: String,
    defaultValue: T,
    typeToken: TypeToken<T>,
    gson: Gson = Gson()
): ReadWriteProperty<Any, T> = object : ReadWriteProperty<Any, T> {

    private val type = typeToken.type

    override fun getValue(thisRef: Any, property: KProperty<*>): T {
        val stored = sharedPreferences.getString(key, null) ?: return defaultValue
        return gson.fromJson(stored, type)
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        sharedPreferences.edit(commit = true) {
            putString(key, gson.toJson(value))
        }
    }
}