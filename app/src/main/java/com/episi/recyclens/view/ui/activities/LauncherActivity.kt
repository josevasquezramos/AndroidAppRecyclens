package com.episi.recyclens.view.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.episi.recyclens.network.FirebaseAuthentication

class LauncherActivity(
    private val repository: FirebaseAuthentication = FirebaseAuthentication()
) : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (repository.isUserLoggedIn()) {
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            startActivity(Intent(this, AuthActivity::class.java))
        }

        finish()
    }
}
