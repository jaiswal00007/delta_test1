package com.example.delta

import android.content.Context
import android.util.Log
import kotlin.random.Random

class ResponseHandler(private val context: Context) {
    val functionality=Functionality(context)
    val greetResponses = listOf(
        "Hey there! 😊 How can I help you today?",
        "Hi! 👋 What can I do for you?",
        "Hello! I'm Delta, your assistant. Ask me anything!",
        "Hey! Ready when you are 😄",
        "Hi! Need help with something?",
        "Hello! Always happy to chat!",
        "Yo! 😎 What’s on your mind?",
        "Hi! Let’s get things done 💪",
        "Hey there! Got any questions?",
        "Hiya! I'm here to assist you anytime!"
    )


    fun detectIntent(userInput: String): String {
        val commands = mapOf(
            "youtube" to listOf("open youtube", "launch youtube", "start youtube", "run youtube"),
            "instagram" to listOf(
                "open instagram",
                "launch instagram",
                "start instagram",
                "run instagram"
            ),
            "whatsapp" to listOf(
                "open whatsapp",
                "launch whatsapp",
                "start whatsapp",
                "run whatsapp"
            ),
            "facebook" to listOf(
                "open facebook",
                "launch facebook",
                "start facebook",
                "run facebook"
            ),
            "twitter" to listOf("open twitter", "launch twitter", "start twitter", "run twitter"),
            "snapchat" to listOf(
                "open snapchat",
                "launch snapchat",
                "start snapchat",
                "run snapchat"
            ),
            "spotify" to listOf("open spotify", "launch spotify", "start spotify", "run spotify"),
            "chrome" to listOf("open chrome", "launch chrome", "start chrome", "run chrome"),
            "gmail" to listOf("open gmail", "launch gmail", "start gmail", "run gmail"),
            "flashlight_on" to listOf("turn on flashlight", "enable flashlight", "flashlight on"),
            "flashlight_off" to listOf(
                "turn off flashlight",
                "disable flashlight",
                "flashlight off"
            ),
            "greet" to listOf("hi", "hello", "hey", "wassup"),
            "set_alarm" to listOf("set an alarm", "create an alarm", "schedule an alarm"),
            "stop_alarm" to listOf("stop alarm", "turn off alarm", "disable alarm"),
            "about_bot" to listOf("who are you", "what is your name", "introduce yourself"),
            "developed_by" to listOf(
                "developed you", "created you", "about developer", "made this assistant",
                "behind this ai", "built this assistant?", "who designed you", "who programmed you"
            ),
            "about_bot" to listOf(
                "who are you", "your name", "about yourself", "introduce yourself",
                "who you are?", "what do people call you?", "who am i talking to?", "your identity"
            ),
            "navigation" to listOf(
                "open maps", "launch maps", "start google maps", "navigate to home"
            ),
            "music_control" to listOf(
                "play music", "pause music", "resume music", "next song", "previous song"
            ),
            "weather" to listOf(
                "weather", "today's weather"
            ),
            "time" to listOf(
                "time", "current time"
            ),
            "phone_control" to listOf(
                "increase volume", "decrease volume", "mute the phone", "turn on do not disturb"
            ),
            "calls_messages" to listOf(
                "call", "send a message to John", "text dad saying I’ll be late"
            ),
            "wifi_bluetooth" to listOf(
                "turn on WiFi", "turn off WiFi", "enable Bluetooth", "disable Bluetooth"
            ),
            "device_status" to listOf(
                "how much battery is left?",
                "what's my battery percentage?",
                "is my phone charging?"
            )
        )
        val lowerInput = userInput.lowercase()
        for ((intent, phrases) in commands) {
            if (phrases.any { phrase -> lowerInput.contains(phrase) }) {
                return intent
            }
        }
        return "unknown_intent"

    }

    fun getResponse(query: String): String {
        val currentIntent = detectIntent(query)
        if (currentIntent.isNotEmpty()) {
            if (currentIntent == "greet") {
                return greetResponses[Random.nextInt(0, greetResponses.count())]
            }
            else if(currentIntent == "youtube"){
                return "Opening YouTube"
            }
            else if(currentIntent == "instagram"){
                return "Opening Instagram"
            }
            else if(currentIntent == "whatsapp"){
                return "Opening WhatsApp"
            }
            else if(currentIntent == "facebook"){
                return "Opening Facebook"
            }
            else if(currentIntent == "twitter"){
                return "Opening Twitter"
            }
            else if(currentIntent == "snapchat"){
                return "Opening Snapchat"
            }
            else if(currentIntent == "spotify"){
                return "Opening Spotify"
            }
            else if(currentIntent == "chrome"){
                return "Opening Chrome"
            }
            else if(currentIntent == "gmail"){
                return "Opening Gmail"
            }
            else if(currentIntent == "flashlight_on"){
                return "Turning on the flashlight"
            }
            else if(currentIntent == "flashlight_off") {
                return "Turning off the flashlight"
            }
            else if(currentIntent == "navigation"){
                return "Opening Google Maps"
            }
            else if(currentIntent == "music_control") {
                return "Controlling music playback"
            }



        }

        return "Invalid Response"
    }




}
//fun main(){
//    val get=ResponseHandler()
//    println(get.detectIntent("turn on flashlight"))
//}