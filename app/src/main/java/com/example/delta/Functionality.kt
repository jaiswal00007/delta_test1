package com.example.delta

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.ContentResolver
import android.content.IntentFilter
import android.database.Cursor
import android.media.AudioManager
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.provider.ContactsContract

import androidx.core.app.ActivityCompat.startActivityForResult
import android.provider.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Suppress("DEPRECATION")
class Functionality(private val context: Context) {
    var flashLightStatus: Boolean = false
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    // Mapping app names to their correct package names
    private val appPackages = mapOf(
        "youtube" to "com.google.android.youtube",
        "instagram" to "com.instagram.android",
        "whatsapp" to "com.whatsapp",
        "facebook" to "com.facebook.katana",
        "twitter" to "com.twitter.android",
        "snapchat" to "com.snapchat.android",
        "spotify" to "com.spotify.music",
        "chrome" to "com.android.chrome",
        "gmail" to "com.google.android.gm"
    )

    // Mapping apps to their correct website URLs
    private val appUrls = mapOf(
        "youtube" to "https://www.youtube.com",
        "instagram" to "https://www.instagram.com",
        "whatsapp" to "https://web.whatsapp.com",
        "facebook" to "https://www.facebook.com",
        "twitter" to "https://twitter.com",
        "snapchat" to "https://www.snapchat.com",
        "spotify" to "https://www.spotify.com",
        "chrome" to "https://www.google.com",
        "gmail" to "https://mail.google.com"
    )

    fun open_app(appName: String) {
        val packageName = appPackages[appName.lowercase()]
        val url = appUrls[appName.lowercase()]

        if (packageName != null) {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                context.startActivity(intent)
                return
            }
        }

        // If app is not installed, open in browser
        if (url != null) {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(browserIntent)
        } else {
            Toast.makeText(context, "App not found", Toast.LENGTH_SHORT).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun toggleFlashlight(turnOn: Boolean): Int {
        if (flashLightStatus == turnOn) {
            return -1
        } else {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraId = cameraManager.cameraIdList[0]
            cameraManager.setTorchMode(cameraId, turnOn)
            flashLightStatus = turnOn
            return 0
        }

    }
    // extract name from text
    fun extractContactName(input: String): String? {
        val cleanedInput = input.lowercase().trim()
        val pattern = Regex("call(?: (?:my )?(?:friend )?)?(.+)", RegexOption.IGNORE_CASE)

        val match = pattern.find(cleanedInput)
        return match?.groups?.get(1)?.value?.trim()?.replaceFirstChar { it.uppercase() }
    }

    // to make a call by name
    fun callContactByName(str: String): String {
        val contactName = extractContactName(str)

        if (contactName.isNullOrBlank()) {
            return "Could not extract contact name from the command."
        }

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(Manifest.permission.READ_CONTACTS, Manifest.permission.CALL_PHONE),
                1
            )
            return "Permission not granted"
        }

        val phoneNumber = getPhoneNumberFromContact(contactName)

        return if (phoneNumber != null) {
            // Delay and make the call in a coroutine
            CoroutineScope(Dispatchers.Main).launch {
                delay(3000)
                val intent = Intent(Intent.ACTION_CALL)
                intent.data = Uri.parse("tel:$phoneNumber")
                context.startActivity(intent)
            }
            "Calling $contactName..."
        } else {
            "Contact '$contactName' not found in your contacts."
        }
    }


    fun getPhoneNumberFromContact(contactName: String): String? {
        val contentResolver: ContentResolver = context.contentResolver
        val cursor: Cursor? = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
            "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} = ?",
            arrayOf(contactName),
            null
        )

        var phoneNumber: String? = null
        try {
            if (cursor != null && cursor.moveToFirst()) {
                val numberIndex =
                    cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                if (numberIndex != -1) {
                    phoneNumber = cursor.getString(numberIndex)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close() // Ensure the cursor is closed after use
        }

        return phoneNumber
    }

    // to turn on/off wifi
    fun switchnWifi(context: Context, switch: Int): String {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            val wifiManager =
                context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            return if (switch == 1) {
                if (!wifiManager.isWifiEnabled) {
                    wifiManager.isWifiEnabled = true
                    "Wi-Fi turned ON"
                } else {
                    "Wi-Fi is already ON"
                }
            } else {
                if (wifiManager.isWifiEnabled) {
                    wifiManager.isWifiEnabled = false
                    "Wi-Fi turned OFF"
                } else {
                    "Wi-Fi is already OFF"
                }
            }
        } else {
            val intent = Intent(android.provider.Settings.ACTION_WIFI_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
//            return "Cannot toggle Wi-Fi programmatically on Android 10+. Opening Wi-Fi settings..."
            return  "Opening Wi-Fi settings. Please turn it manually."
        }
    }

    // to turn on/off Bluetooth
    fun switchBluetooth(context: Context, switch: Int): String {
        val bluetoothAdapter: BluetoothAdapter? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            bluetoothManager.adapter
        } else {
            BluetoothAdapter.getDefaultAdapter()
        }

        if (bluetoothAdapter == null) {
            return "Bluetooth is not available on this device"
        }

        // 🔐 Request BLUETOOTH_CONNECT permission if required (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
        ) {
            if (context is Activity) {
                ActivityCompat.requestPermissions(
                    context,
                    arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                    1001
                )
                return "Requesting Bluetooth permission..."
            } else {
                return "Bluetooth permission not granted (not an Activity)"
            }
        }

        return when (switch) {
            1 -> {
                if (!bluetoothAdapter.isEnabled) {
                    bluetoothAdapter.enable()
                    "Bluetooth turned ON"
                } else {
                    "Bluetooth is already ON"
                }
            }

            0 -> {
                if (bluetoothAdapter.isEnabled) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        // Android 13+ can't disable Bluetooth programmatically
                        val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                        "Bluetooth can't be turned OFF automatically on Android 13+. Opening settings..."
                    } else {
                        bluetoothAdapter.disable()
                        "Bluetooth turned OFF"
                    }
                } else {
                    "Bluetooth is already OFF"
                }
            }

            else -> "Invalid command"
        }
    }




    // to get battery percentage
    fun getBatteryPercentage(context: Context): Int {
        // Create an intent filter to listen for battery status updates
        val ifilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)

        // Get the battery status from the system
        val batteryStatus: Intent? = context.registerReceiver(null, ifilter)

        // Retrieve battery level and scale
        val level: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1

        // Calculate battery percentage
        return if (level != -1 && scale != -1) {
            (level / scale.toFloat() * 100).toInt()
        } else {
            -1 // If the battery percentage cannot be fetched
        }
    }



    fun adjustVolume(action: Int) {
        when (action) {
            1 -> increaseVolume()
            -1 -> decreaseVolume()
            0 -> muteVolume()
            10 -> unmuteVolume()
        }
    }

    private fun increaseVolume() {
        audioManager.adjustStreamVolume(
            AudioManager.STREAM_MUSIC,
            AudioManager.ADJUST_RAISE,
            AudioManager.FLAG_SHOW_UI
        )
    }

    private fun decreaseVolume() {
        audioManager.adjustStreamVolume(
            AudioManager.STREAM_MUSIC,
            AudioManager.ADJUST_LOWER,
            AudioManager.FLAG_SHOW_UI
        )
    }

    private fun muteVolume() {
        audioManager.adjustStreamVolume(
            AudioManager.STREAM_MUSIC,
            AudioManager.ADJUST_MUTE,
            AudioManager.FLAG_SHOW_UI
        )
    }

    private fun unmuteVolume() {
        audioManager.adjustStreamVolume(
            AudioManager.STREAM_MUSIC,
            AudioManager.ADJUST_UNMUTE,
            AudioManager.FLAG_SHOW_UI
        )
    }

}


