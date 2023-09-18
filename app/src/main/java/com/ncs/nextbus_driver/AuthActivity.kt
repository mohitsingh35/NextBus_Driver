package com.ncs.nextbus_driver

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.google.firebase.auth.FirebaseAuth
import com.ncs.nextbus_driver.ui.theme.NextBus_DriverTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AuthActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NextBus_DriverTheme {
                if (FirebaseAuth.getInstance().currentUser?.uid!=null){
                    this.startActivity(Intent(this,MainActivity::class.java))
                }
                else{
                    AuthScreen()
                }

            }
        }
    }
}
