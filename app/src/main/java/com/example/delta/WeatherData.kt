package com.yourapp.delta

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.util.Log
import com.example.delta.BuildConfig
import com.example.delta.WeatherResponse
import com.google.android.gms.location.LocationServices
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query



class WeatherData(private val context: Context) {

    private val apiKey = BuildConfig.OPENWEATHER_API_KEY // 🔐 Replace with your OpenWeatherMap API key
    private val baseUrl = "https://api.openweathermap.org/data/2.5/"
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    interface WeatherApi {
        @GET("weather")
        fun getCurrentWeather(
            @Query("lat") lat: Double,
            @Query("lon") lon: Double,
            @Query("appid") apiKey: String,
            @Query("units") units: String = "metric"
        ): Call<WeatherResponse>
    }

    private val api: WeatherApi = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(WeatherApi::class.java)

    @SuppressLint("MissingPermission")
    fun fetchCurrentWeather(onResult: (WeatherResponse?) -> Unit) {
        val locationRequest = com.google.android.gms.location.LocationRequest.create().apply {
            interval = 0
            fastestInterval = 0
            numUpdates = 1
            priority = com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        val locationCallback = object : com.google.android.gms.location.LocationCallback() {
            override fun onLocationResult(locationResult: com.google.android.gms.location.LocationResult) {
                val location = locationResult.lastLocation
                if (location != null) {
                    val lat = location.latitude
                    val lon = location.longitude
                    Log.d("WeatherFetcher", "Location (live): $lat, $lon")

                    api.getCurrentWeather(lat, lon, apiKey).enqueue(object : Callback<WeatherResponse> {
                        override fun onResponse(
                            call: Call<WeatherResponse>,
                            response: Response<WeatherResponse>
                        ) {
                            if (response.isSuccessful) {
                                onResult(response.body())
                            } else {
                                Log.e("WeatherFetcher", "API error: ${response.errorBody()}")
                                onResult(null)
                            }
                        }

                        override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                            Log.e("WeatherFetcher", "Network error: ${t.message}")
                            onResult(null)
                        }
                    })
                } else {
                    Log.e("WeatherFetcher", "Live location is null")
                    onResult(null)
                }

                fusedLocationClient.removeLocationUpdates(this) // stop after 1 fetch
            }
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }


}

