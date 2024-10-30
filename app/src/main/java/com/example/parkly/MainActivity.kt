package com.example.parkly

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.example.parkly.auth.viewmodel.LoginViewModel
import com.example.parkly.util.intentWithoutBackstack
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    val viewModel: LoginViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        installSplashScreen()
        lifecycleScope.launch {
            viewModel.getCurrentUser()
        }


        //auto login
        if (viewModel.isLoggedIn()) {
            Log.d("status", "onCreateView: logged in and verified")
            intentWithoutBackstack(this, UserActivity::class.java)
        } else {
            setContentView(R.layout.activity_main) //change accordingly for testing
        }


    }
}