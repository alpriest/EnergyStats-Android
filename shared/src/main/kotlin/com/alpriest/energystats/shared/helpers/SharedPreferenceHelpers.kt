package com.alpriest.energystats.shared.helpers

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

// Delegate for simple types like String?, Boolean, Double (stored as String)
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
