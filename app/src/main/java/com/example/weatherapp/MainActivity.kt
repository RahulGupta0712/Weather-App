package com.example.weatherapp

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SearchView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.weatherapp.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.TimeZone

// api key : 

class MainActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.group.visibility = View.INVISIBLE
        searchCity()
    }

    private fun searchCity() {
        val searchView = binding.searchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    fetchWeatherData(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })
    }

    private fun fetchWeatherData(city: String) {
        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .build().create(ApiInterface::class.java)

        val response =
            retrofit.getWeatherData(city, "", "metric")

        response.enqueue(object : Callback<WeatherClass> {
            override fun onResponse(call: Call<WeatherClass>, response: Response<WeatherClass>) {
                val responseBody = response.body()
                if (response.isSuccessful && responseBody != null) {
                    val temperature = responseBody.main.temp.toString()
                    val condition = responseBody.weather.firstOrNull()?.main ?: "unknown"
                    val minTemp = responseBody.main.temp_min
                    val maxTemp = responseBody.main.temp_max
                    val humidity = responseBody.main.humidity
                    val windSpeed = responseBody.wind.speed
                    val sunrise = responseBody.sys.sunrise.toLong()
                    val sunset = responseBody.sys.sunset.toLong()
                    val country = responseBody.sys.country
                    val feelsLike = responseBody.main.feels_like
                    val cityName = responseBody.name
                    val updateTime = responseBody.dt.toLong()
                    val visibility = responseBody.visibility

                    binding.cityName.text = getString(R.string.city_and_country, cityName, country)
                    binding.temperature.text = getString(R.string.temp, temperature)
                    binding.weather.text = condition
                    binding.minTemp.text = getString(R.string.min_temp, minTemp.toString())
                    binding.maxTemp.text = getString(R.string.max_temp, maxTemp.toString())
                    binding.day.text = getDay()
                    binding.date.text = getDate()
                    binding.humidity.text = "$humidity %"
                    binding.windSpeed.text = getString(R.string.wind_speed, windSpeed.toString())
                    binding.feelsLike.text = getString(R.string.feels_like, feelsLike.toString())
                    binding.sunrise.text = unixToLocalTime(sunrise)
                    binding.sunset.text = unixToLocalTime(sunset)
                    binding.visibility.text = getVisibility(visibility)
                    binding.updatedTime.text = getString(R.string.last_updated, unixToLocalTime(updateTime))

                    changeWeather(condition)

                    binding.group.visibility = View.VISIBLE
                }
            }

            override fun onFailure(call: Call<WeatherClass>, t: Throwable) {
                Log.d("debug", "Error : ${t.message}")
            }

        })
    }

    private fun getVisibility(visibility: Int): String {
        if(visibility < 1000){
            return "$visibility m"
        }
        else{
            val vis = (visibility.toDouble())/1000.0
            return "$vis km"
        }
    }

    private fun changeWeather(condition: String) {
        when(condition){
            "Sunny", "Clear Sky", "Clear" ->{
                binding.animationView.setAnimation(R.raw.sun)
                binding.root.setBackgroundResource(R.drawable.sunny_bg)
            }

            "Rain","Light Rain", "Moderate Rain", "Drizzle", "Showers", "Heavy Rain" ->{
                binding.animationView.setAnimation(R.raw.rain)
                binding.root.setBackgroundResource(R.drawable.rain_bg)
            }

            "Light Snow", "Moderate Snow", "Heavy Snow", "Blizzard" -> {
                binding.animationView.setAnimation(R.raw.snow)
                binding.root.setBackgroundResource(R.drawable.snow_bg)
            }

            "Clouds", "Partly Clouds", "Overcast", "Mist", "Foggy", "Haze" -> {
                binding.animationView.setAnimation(R.raw.cloud)
                binding.root.setBackgroundResource(R.drawable.cloud_bg)
            }

            else -> {
                binding.animationView.setAnimation(R.raw.sun)
                binding.root.setBackgroundResource(R.drawable.sunny_bg)
            }
        }

        binding.animationView.playAnimation()
    }

    fun unixToLocalTime(unixTime: Long): String {
        val time = Date(unixTime * 1000) // Convert seconds to milliseconds
        val format = SimpleDateFormat("HH:mm")
        format.timeZone = TimeZone.getDefault()
        return format.format(time)
    }

    private fun getDate(): String {
        val date = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("d MMMM, yyyy")
        val formattedDate = date.format(formatter)
        return formattedDate
    }

    private fun getDay(): String {
        val date = LocalDate.now()
        return date.dayOfWeek.toString()
    }
}
