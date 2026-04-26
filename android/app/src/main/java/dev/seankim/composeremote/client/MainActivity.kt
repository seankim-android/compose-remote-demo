package dev.seankim.composeremote.client

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import dev.seankim.composeremote.client.ui.theme.ComposeRemoteDemoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ComposeRemoteDemoTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    RemoteScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}
