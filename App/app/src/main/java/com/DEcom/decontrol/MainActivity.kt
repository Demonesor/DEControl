package com.DEcom.decontrol

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                TokenClientUI()
            }
        }
    }
}

@Composable
fun TokenClientUI() {
    var token by remember { mutableStateOf("") }
    var response by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🔐 Введіть токен", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = token,
            onValueChange = { token = it },
            label = { Text("Token") },
            singleLine = true
        )

        Button(
            onClick = {
                loading = true
                response = ""
                GlobalScope.launch(Dispatchers.IO) {
                    val result = connectToServer(token)
                    withContext(Dispatchers.Main) {
                        response = result ?: "Помилка підключення"
                        loading = false
                    }
                }
            },
            enabled = !loading
        ) {
            Text(if (loading) "З'єднання..." else "Підтвердити")
        }

        if (response.isNotEmpty()) {
            Text("✅ Відповідь: $response")
        }

        TextButton(onClick = { /* TODO: профі-режим */ }) {
            Text("Профі режим ⚙️")
        }
    }
}

fun connectToServer(token: String): String? {
    try {
        val parts = token.split(":")
        if (parts.size < 3) return null

        val ip = parts[2]  // IP з токену
        val port = 7546
        val socket = Socket(ip, port)

        val out = PrintWriter(socket.getOutputStream(), true)
        val input = BufferedReader(InputStreamReader(socket.getInputStream()))

        out.println(token)
        val response = input.readLine()

        socket.close()
        return response
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}
