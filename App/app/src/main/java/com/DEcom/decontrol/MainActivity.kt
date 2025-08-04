package com.DEcom.decontrol

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.DEcom.decontrol.ui.theme.DEControlTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.io.IOException

class MainActivity : ComponentActivity() {
    private val port = 7462
    private var token by mutableStateOf("")
    private var serverIp by mutableStateOf("127.0.0.1")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DEControlTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    RemoteControlUI()
                }
            }
        }
    }

    @Composable
    fun RemoteControlUI() {
        var tokenInput by remember { mutableStateOf(TextFieldValue()) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("PC Remote Controller", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = tokenInput,
                onValueChange = {
                    tokenInput = it
                    token = it.text
                    parseIpFromToken(token)
                },
                label = { Text("Введіть токен") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(onClick = { sendRequest("status", "GET") }, modifier = Modifier.fillMaxWidth()) {
                Text("Статус")
            }

            Button(onClick = { sendRequest("lock") }, modifier = Modifier.fillMaxWidth()) {
                Text("Заблокувати")
            }

            Button(onClick = { sendRequest("reboot") }, modifier = Modifier.fillMaxWidth()) {
                Text("Перезавантажити")
            }

            Button(onClick = { sendRequest("shutdown") }, modifier = Modifier.fillMaxWidth()) {
                Text("Вимкнути")
            }
        }
    }

    private fun parseIpFromToken(token: String) {
        try {
            val ipPart = token.split(":").getOrNull(2) ?: return
            serverIp = "192.168.${ipPart[0]}.${ipPart.substring(1)}"
        } catch (_: Exception) {
            serverIp = "127.0.0.1"
        }
    }

    private fun sendRequest(endpoint: String, method: String = "POST") {
        val url = "http://$serverIp:$port/$endpoint"
        val client = OkHttpClient()
        val requestBuilder = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $token")

        val request = if (method == "GET") {
            requestBuilder.get().build()
        } else {
            requestBuilder.post(RequestBody.create(null, ByteArray(0))).build()
        }

        CoroutineScope(Dispatchers.IO).launch {
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "Помилка: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    val body = response.body?.string()
                    runOnUiThread {
                        Toast.makeText(
                            this@MainActivity,
                            "[${endpoint.uppercase()}] → ${response.code}\n${body}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            })
        }
    }
}
