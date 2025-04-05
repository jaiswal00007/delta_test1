package com.example.delta

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.delta.ui.theme.botBubbleColor
import com.example.delta.ui.theme.botTextColor
import com.example.delta.ui.theme.color1
import com.example.delta.ui.theme.color2
import com.example.delta.ui.theme.color3
import com.example.delta.ui.theme.color4
import com.example.delta.ui.theme.sapAssistantBg
import com.example.delta.ui.theme.sapPrimary
import com.yourapp.delta.WeatherData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale



class MainActivity : ComponentActivity() {

    val geminiHelper = GeminiHelper()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        val response = ResponseHandler(this)
        val weatherFetcher = WeatherData(this)
        enableEdgeToEdge()

        setContent {

            ChatScreen(geminiHelper,response,weatherFetcher)

        }

    }




}

@Composable
fun ChatScreen(geminiHelper: GeminiHelper, response: ResponseHandler,weatherFetcher: WeatherData) {
    var messages by remember { mutableStateOf(listOf<Pair<ChatMessage, String>>()) }
    var currentMessage by remember { mutableStateOf("") }
    var isTyping by remember { mutableStateOf(false) }
    var isFirstMessage by remember { mutableStateOf(true) }
    val keyboardController = LocalSoftwareKeyboardController.current
    var showButtons by remember { mutableStateOf(true) }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }
    fun sendMessage(message: String) {
        if (message.isNotEmpty()) {
            val timestamp = getCurrentTime()
            messages = messages + (ChatMessage(message, true) to timestamp)
            currentMessage = ""
            isTyping = true
            isFirstMessage = false
            keyboardController?.hide()
            showButtons = false

            CoroutineScope(Dispatchers.Main).launch {
                if (message.contains("weather", ignoreCase = true)) {
                    weatherFetcher.fetchCurrentWeather { response ->
                        if (response != null) {
                            val city = response.name
                            val temp = response.main.temp
                            val desc = response.weather.firstOrNull()?.description ?: "N/A"
                            val humidity = response.main.humidity
                            val windSpeed = response.wind.speed
                            val feelsLike = response.main.feels_like
                            val iconCode = response.weather.firstOrNull()?.icon ?: "01d"

                            val weatherMessage = ChatMessage(
                                isUser = false,
                                messageType = MessageType.WEATHER,
                                weatherData = WeatherData(
                                    city = city,
                                    temperature = (Math.round(temp * 10f)) / 10f.toDouble(),
                                    weatherDescription = desc,
                                    humidity = humidity,
                                    windSpeed = (Math.round(windSpeed * 10f)) / 10f.toDouble(),
                                    feelsLike = (Math.round(feelsLike * 10f)) / 10f.toDouble(),
                                    iconCode = iconCode
                                )
                            )

                            CoroutineScope(Dispatchers.Main).launch {
                                delay(2000)
                                messages = messages + (weatherMessage to getCurrentTime())
                                isTyping = false
                            }
                        } else {
                            Log.e("Weather", "Failed to fetch weather.")
                            val botReply = "Sorry, I couldn't fetch the weather information at the moment."
                            messages = messages + (ChatMessage(botReply, false) to getCurrentTime())
                            isTyping = false
                        }
                    }
                } else {
                    val historyForContext = messages.map { (msg, _) ->
                        mapOf(
                            "role" to if (msg.isUser) "user" else "assistant",
                            "content" to msg.text
                        )
                    }.takeLast(10)
                    response.getResponse(message, historyForContext) { botReply ->
                        val botTimestamp = getCurrentTime()
                        CoroutineScope(Dispatchers.Main).launch {
                            delay(2000)
                            messages = (messages + (ChatMessage(botReply, false) to botTimestamp)).takeLast(10)
                            isTyping = false
                        }
                    }
                }
            }



//            geminiHelper.getGeminiResponse(message) { response ->
//                Log.d("ChatScreen", "Gemini response received: $response")
//                val botTimestamp = getCurrentTime()
//                messages = messages + (ChatMessage(response, false) to botTimestamp)
//                isTyping = false
//            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(sapAssistantBg)
            .imePadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            if (isFirstMessage) {
                LargeHeader()
            } else {
                SmallHeader()
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                .padding(bottom = 90.dp),
            reverseLayout = false
            )
            {
                if (showButtons) {
                    item {
                        Column(
                            modifier = Modifier
                                .padding(vertical = 12.dp)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Button(
                                    onClick = { sendMessage("Hi Delta") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                                ) {
                                    Text("👋 Hi Delta", color = sapPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = { sendMessage("Today's Weather?") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                                ) {
                                    Text("☀️ Weather", color = sapPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                items(messages) { (message, timestamp) ->
                    ChatMessageItem(message = message, timestamp = timestamp)
                }
                if (isTyping) {
                    item { SAPTypingIndicator() }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
        }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(8.dp),
            shadowElevation = 4.dp,
            shape = RoundedCornerShape(24.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = currentMessage,
                    onValueChange = { currentMessage = it },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp),
                    placeholder = { Text("Ask me anything...") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    trailingIcon = {
                        IconButton(
                            onClick = { sendMessage(currentMessage) },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Send",
                                tint = sapPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { sendMessage(currentMessage) })
                )
            }
        }
    }

}

@Composable
fun SmoothGradientBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition()

    // Animate offset to move gradient left to right and back
    val shift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 800f, // shift across screen width
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 10000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val gradientColors = listOf(
        color1, // Dominant Blue
        color2, // Extra Blue
        color3, // Teal Accent  
        color4  // Pink/Purple at far end
    )

    Box(
        modifier = modifier

            .background(
                Brush.linearGradient(
                    colors = gradientColors,
                    start = Offset(0f + shift, 0f),
                    end = Offset(1000f + shift, 1000f)
                )
            )
    ) {
        content()
    }
}



@Composable
fun LargeHeader() {

    SmoothGradientBackground(
        modifier = Modifier
            .fillMaxWidth()
            .height(600.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
        ) {
            // Top Row Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 16.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.logo128x128),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(16.dp))
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Delta",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Beta",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                )
            }

            // Center Content
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                Image(
                    painter = painterResource(R.drawable.logo),
                    contentDescription = "Delta Logo",
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(32.dp))
                )

                Spacer(modifier = Modifier.height(16.dp))

                TypingText(
                    fullText = "Hey! How can I help you?",
                    typingSpeed = 60L,
                    loop = true,
                    textStyle = TextStyle(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }
        }
    }
}


@Composable
fun TypingText(
    fullText: String,
    typingSpeed: Long = 50L,
    cursorBlinkSpeed: Int = 500,
    loop: Boolean = false,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = TextStyle(fontSize = 20.sp, color = Color.White)
) {
    var displayedText by remember { mutableStateOf("") }

    // Typing effect
    LaunchedEffect(fullText, loop) {
        do {
            displayedText = ""
            for (char in fullText) {
                displayedText += char
                delay(typingSpeed)
            }
            if (loop) {
                delay(1500L) // pause before restarting
            }
        } while (loop)
    }

    Text(
        text = displayedText ,
        modifier = modifier,
        style = textStyle
    )
}



@Composable
fun SmallHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
    ) {
        SmoothGradientBackground(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 16.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.logo128x128),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(16.dp))
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Delta",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Beta",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                )
            }
        }
    }
}

@Composable
fun ChatMessageItem(message: ChatMessage, timestamp: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start
    ) {
        // Timestamp and avatar
        if (!message.isUser) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 12.dp, bottom = 4.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.logo128x128),
                    contentDescription = "Bot Icon",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "BOT • $timestamp",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
        } else {
            Text(
                text = "You • $timestamp",
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier
                    .padding(end = 12.dp, bottom = 4.dp)
                    .align(Alignment.End)
            )
        }

        // Message bubble or custom content
        when (message.messageType) {
            MessageType.TEXT -> {
                if (message.isUser) {
                    SmoothGradientBackground(
                        modifier = Modifier
                            .widthIn(max = 325.dp)
                            .clip(RoundedCornerShape(14.dp))
                    ) {
                        Box(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                            Text(
                                text = message.text,
                                color = Color.White,
                                fontSize = 16.sp,
                                lineHeight = 22.sp
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .widthIn(max = 325.dp)
                            .background(
                                color = botBubbleColor,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .border(1.dp, Color(0xFFB6D0E2), RoundedCornerShape(14.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = message.text,
                            color = botTextColor,
                            fontSize = 16.sp,
                            lineHeight = 22.sp
                        )
                    }
                }
            }

            MessageType.WEATHER -> {
                message.weatherData?.let {
                    WeatherCard(
                        city = it.city,
                        temperature = it.temperature,
                        weatherDescription = it.weatherDescription,
                        humidity = it.humidity,
                        windSpeed = it.windSpeed,
                        feelsLike = it.feelsLike,
                        iconCode = it.iconCode
                    )
                }
            }
        }
    }
}





@Composable
fun SAPTypingIndicator() {
    val dotCount = 3
    val dotSize = 8.dp
    val dotSpacing = 4.dp
    val delayPerDot = 300
    val infiniteTransition = rememberInfiniteTransition()
    val alphas = List(dotCount) { index ->
        infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 600, delayMillis = delayPerDot * index),
                repeatMode = RepeatMode.Reverse
            )
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(R.drawable.logo128x128),
            contentDescription = "Typing",
            modifier = Modifier.size(20.dp)
                .clip(RoundedCornerShape(10.dp))
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Dots with animated alpha
        Row(
            horizontalArrangement = Arrangement.spacedBy(dotSpacing)
        ) {
            alphas.forEach { alpha ->
                Box(
                    modifier = Modifier
                        .size(dotSize)
                        .graphicsLayer { this.alpha = alpha.value }
                        .background(sapPrimary, shape = CircleShape)
                )
            }
        }
    }
}


fun getCurrentTime(): String {
    val dateFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    return dateFormat.format(Date())
}

@Composable
fun WeatherCard(
    city: String,
    temperature: Double,
    weatherDescription: String,
    humidity: Int,
    windSpeed: Double,
    feelsLike: Double,
    iconCode: String
) {
    val currentDate = SimpleDateFormat("EEE, MMM d • h:mm a", Locale.getDefault()).format(Date())

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFDAF2FF)),
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = city,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = color2
            )

            Text(
                text = currentDate,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(16.dp))

            AsyncImage(
                model = "https://openweathermap.org/img/wn/${iconCode}@4x.png",
                contentDescription = "Weather Icon",
                modifier = Modifier.size(100.dp)
            )

            Text(
                text = "$temperature°C",
                fontSize = 38.sp,
                fontWeight = FontWeight.Bold,
                color = color2
            )

            Text(
                text = weatherDescription.replaceFirstChar { it.uppercaseChar() },
                fontSize = 18.sp,
                color = Color.DarkGray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                WeatherInfoItem("💧 Humidity", "$humidity%")
                WeatherInfoItem("🌬 Wind", "$windSpeed m/s")
                WeatherInfoItem("🌡 Feels like", "$feelsLike°C")
            }
        }
    }
}

@Composable
fun WeatherInfoItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 14.sp, color = Color.DarkGray)
        Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color.Black)
    }
}

//data class ChatMessage(val text: String, val isUser: Boolean)



@Preview(showBackground = true)
@Composable
fun GreetingPreview1() {
//    val geminiHelper=GeminiHelper()
//    ChatMessageItem(ChatMessage("hello",true),"1233")
//
//    WeatherCard(
//        city = "New York",
//        temperature = 25.5,
//        weatherDescription = "Sunny",
//        humidity = 60,
//        windSpeed = 5.0,
//        feelsLike = 24.0,
//        iconCode = "01d"
//    )
        SAPTypingIndicator()

}

