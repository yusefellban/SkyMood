package iti.yousef.skymood.data.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

/**
 * Interface to abstract network availability checks.
 * This allows for easy mocking in unit tests.
 */
interface NetworkHandler {
    fun isNetworkAvailable(): Boolean
}

/**
 * Real implementation of NetworkHandler using Android's ConnectivityManager.
 */
class AndroidNetworkHandler(private val context: Context) : NetworkHandler {
    override fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
