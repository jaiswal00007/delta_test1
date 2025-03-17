package com.example.delta

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColor
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.delta.ui.theme.color1
import com.example.delta.ui.theme.color2
import com.example.delta.ui.theme.color3
import com.example.delta.ui.theme.color4
import com.example.delta.ui.theme.sapAssistantBg
import com.example.delta.ui.theme.sapPrimary
import com.example.delta.ui.theme.sapUserText
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale



class MainActivity : ComponentActivity() {

    val geminiHelper = GeminiHelper()
    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

            ChatScreen(geminiHelper)

        }

    }




}

@Composable
fun ChatScreen(geminiHelper: GeminiHelper) {
    var messages by remember { mutableStateOf(listOf<Pair<ChatMessage, String>>()) }
    var currentMessage by remember { mutableStateOf("") }
    var isTyping by remember { mutableStateOf(false) }
    var isFirstMessage by remember { mutableStateOf(true) }
    val keyboardController = LocalSoftwareKeyboardController.current
    var showButtons by remember { mutableStateOf(true) }  // State to control visibility of buttons

    // Reusable send message function
    fun sendMessage(message: String) {
        if (message.isNotEmpty()) {
            val timestamp = getCurrentTime()
            messages = messages + (ChatMessage(message, true) to timestamp)
            currentMessage = ""
            isTyping = true
            isFirstMessage = false
            keyboardController?.hide()

            // Hide buttons after first message
            showButtons = false

            // Call Gemini API to get a response
            geminiHelper.getGeminiResponse(message) { response ->
                Log.d("ChatScreen", "Gemini response received: $response")
                val botTimestamp = getCurrentTime()
                messages = messages + (ChatMessage(response, false) to botTimestamp)
                isTyping = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(sapAssistantBg)
    ) {

        // **Conditional Header**
        if (isFirstMessage) {
            LargeHeader()
        }
        else{
            SmallHeader()
        }

        // **New Button Below Topbar**
        if (showButtons) {  // Only show the buttons if showButtons is true
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp), // Add space between the buttons
                verticalAlignment = Alignment.CenterVertically // Vertically align the buttons in the center
            ) {
                Button(
                    onClick = { sendMessage("Hi Delta") }, // Send "Hi Delta" when clicked
                    modifier = Modifier
                        .width(150.dp) // Adjust the width
                        .padding(horizontal = 6.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(12.dp)) // Border radius
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(12.dp),
                            ambientColor = Color.Black.copy(alpha = 0.1f),
                            spotColor = Color.Black.copy(alpha = 0.5f)
                        ), // Adds shadow with light intensity
                    colors = ButtonDefaults.buttonColors(containerColor = sapAssistantBg)
                ) {
                    Text("Hi Delta", color = sapPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { sendMessage("Today's Weather?") }, // Send "Today's Weather?" when clicked
                    modifier = Modifier
                        .width(200.dp) // Adjust the width
                        .padding(horizontal = 6.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(12.dp)) // Border radius
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(12.dp),
                            ambientColor = Color.Black.copy(alpha = 0.1f),
                            spotColor = Color.Black.copy(alpha = 0.5f)
                        ), // Adds shadow with light intensity
                    colors = ButtonDefaults.buttonColors(containerColor = sapAssistantBg)
                ) {
                    Text("Today's Weather?", color = sapPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // **Chat Messages**
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            LazyColumn {
                items(messages) { (message, timestamp) ->
                    ChatMessageItem(message = message, timestamp = timestamp)
                }
                if (isTyping) {
                    item { SAPTypingIndicator() }
                }
            }
        }

        // **Input Field**
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shadowElevation = 4.dp,
            shape = RoundedCornerShape(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(Color.White)
                    .padding(horizontal = 12.dp)
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
                            onClick = { sendMessage(currentMessage) }, // Send message when clicked
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
                    keyboardActions = KeyboardActions(onSend = { sendMessage(currentMessage) }) // Send message on enter
                )
            }
        }
    }

    // **Simulate AI Response**
    LaunchedEffect(isTyping) {
        if (isTyping) {
            delay(1500) // Wait for simulated response

            // Check if no response was added yet (API call might have failed)
            if (messages.lastOrNull()?.first?.isUser == true) {
                val timestamp = getCurrentTime()
                messages = messages + (ChatMessage("Sorry, I couldn't fetch a response.", false) to timestamp)
            }

            isTyping = false
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
        Color(0xFF2196F3), // Dominant Blue
        Color(0xFF2196F3), // Extra Blue
        Color(0xFF00BCD4), // Teal Accent
        Color(0xFF9C27B0)  // Pink/Purple at far end
    )

    Box(
        modifier = modifier
            .fillMaxSize()
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Image(
                painter = painterResource(R.drawable.logo),
                contentDescription = "Delta Logo",
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(32.dp))
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Hey! How can I help you?",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}


@Composable
fun SmallHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(sapPrimary),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 16.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.logo128x128),
                contentDescription = "Logo",
                modifier = Modifier.size(32.dp)
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

@Composable
fun ChatMessageItem(message: ChatMessage, timestamp: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start
    ) {
        if (!message.isUser) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.logo128x128),
                    contentDescription = "Bot Icon",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "BOT $timestamp",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
        } else {
            Text(
                text = "You $timestamp",
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier
                    .padding(end = 10.dp, bottom = 4.dp)
                    .align(Alignment.End)
            )
        }

        Box(
            modifier = Modifier
                .widthIn(max = 325.dp)
                .background(
                    color = if (message.isUser) sapPrimary else sapAssistantBg,
                    shape = RoundedCornerShape(12.dp)
                )
                .border(
                    width = 1.dp,
                    color = if (message.isUser) sapPrimary else Color(0xFFC4DBF5),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                text = message.text,
                color = if (message.isUser) Color.White else sapUserText,
                fontSize = 16.sp,
                lineHeight = 22.sp
            )
        }
    }
}

@Composable
fun SAPTypingIndicator() {
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
        )

        Spacer(modifier = Modifier.width(8.dp))

        repeat(3) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(sapPrimary, shape = CircleShape)
            )
            Spacer(modifier = Modifier.width(4.dp))
        }
    }
}

fun getCurrentTime(): String {
    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
    return sdf.format(Date())
}

data class ChatMessage(val text: String, val isUser: Boolean)


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    val geminiHelper=GeminiHelper()
    ChatScreen(geminiHelper)
}
@Preview(showBackground = true)
@Composable
fun GreetingPreview1() {
    val geminiHelper=GeminiHelper()
    SmallHeader()
}

