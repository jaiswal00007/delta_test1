package com.example.delta

data class WeatherResponse(
    val name: String,
    val weather: List<Weather>,
    val main: Main,
    val wind: Wind
)

data class Weather(
    val main: String,
    val description: String,
    val icon: String
)

data class Main(
    val temp: Float,
    val feels_like: Float,
    val humidity: Int,
    val pressure: Int
)
enum class MessageType {
    TEXT,
    WEATHER
}

data class ChatMessage(
    val text: String = "",
    val isUser: Boolean,
    val messageType: MessageType = MessageType.TEXT,
    val weatherData: WeatherData? = null
)

data class WeatherData(
    val city: String,
    val temperature: Double,
    val weatherDescription: String,
    val humidity: Int,
    val windSpeed: Double,
    val feelsLike: Double,
    val iconCode: String
)


data class Wind(
    val speed: Float
)
