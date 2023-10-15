//package com.alpriest.energystats.widget
//
//import android.content.Context
//import android.util.Log
//import androidx.glance.appwidget.updateAll
//import kotlin.random.Random
//
//class LatestDataRepository private constructor() {
//    var batteryPercentage: Float = 0f
//
//    suspend fun update(context: Context) {
//        batteryPercentage = Random.nextFloat()
//
//        Log.d("AWP", "AWP battery $batteryPercentage")
//
//        BatteryWidget().updateAll(context)
//    }
//
//    companion object {
//        private var instance: LatestDataRepository? = null
//
//        fun getInstance(): LatestDataRepository {
//            if (instance == null) {
//                instance = LatestDataRepository()
//            }
//            return instance!!
//        }
//    }
//}