package com.example.guniattendance.utils

import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET
import android.net.NetworkRequest
import androidx.lifecycle.LiveData
import javax.inject.Inject


class LiveNetworkMonitor @Inject constructor(private val connectivityManager: ConnectivityManager):LiveData<Boolean>() {
    private lateinit var networkCallback: NetworkCallback
    private val networksThatHaveInternet:MutableSet<Network> = HashSet()

    init{
        checkIfOneNetworkHasInternet()
    }

    private fun checkIfOneNetworkHasInternet() {
        postValue(networksThatHaveInternet.size>0)
    }

    override fun onActive() {
        networkCallback=createNetworkCallback()

        val networkRequest=NetworkRequest.Builder()
            .addCapability(NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(networkRequest,networkCallback)
    }

    override fun onInactive() {
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }
    private fun createNetworkCallback() = object :ConnectivityManager.NetworkCallback(){
        override fun onAvailable(network: Network) {
            println("Device got connection to this network: $network")

            val networdCapability=connectivityManager.getNetworkCapabilities(network)
            val hasInternet = networdCapability?.hasCapability(NET_CAPABILITY_INTERNET)

            if(hasInternet==true)
            {
                networksThatHaveInternet.add(network)
            }

            checkIfOneNetworkHasInternet()
        }

        override fun onLost(network: Network) {
            println("Device lost connection to this network: $network")
            networksThatHaveInternet.remove(network)
            checkIfOneNetworkHasInternet()
        }
    }

}