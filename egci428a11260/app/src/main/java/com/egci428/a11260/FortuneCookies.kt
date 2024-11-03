package com.egci428.a11260

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import okhttp3.*
import org.json.JSONObject
import java.io.IOException


class FortuneCookies : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_fortune_cookies)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val backBTN = findViewById<ImageButton>(R.id.backBTN)
        backBTN.setOnClickListener {
            finish()
        }

        val client = OkHttpClient()
        val circleBTN = findViewById<Button>(R.id.circleBTN)
        val cookiesIV = findViewById<ImageView>(R.id.CookiesIV)
        val resultTV = findViewById<TextView>(R.id.resultResultTV)

        circleBTN.setOnClickListener {
            val num = kotlin.random.Random.nextInt(0, 9).toString()
            val jsonURL = "https://egci428-d78f6-default-rtdb.firebaseio.com/fortunecookies/"+num+".json"
            Toast.makeText(this, "Waiting...", Toast.LENGTH_SHORT).show()

            cookiesIV.setImageResource(R.drawable.opened_cookie)
            circleBTN.text = "Save"

            val request = Request.Builder()
                .url(jsonURL)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("Network Error", "Failed to load message: ${e.message}")
                    runOnUiThread {
                        Toast.makeText(this@FortuneCookies, "Failed to load message", Toast.LENGTH_SHORT).show()
                    }
                }


                override fun onResponse(call: Call, response: Response) {
                    response.body?.let { responseBody ->
                        val jsonString = responseBody.string()
                        val jsonObject = JSONObject(jsonString)
                        val message = jsonObject.optString("message", "No message found")

                        runOnUiThread {
                            resultTV.text = message
                        }
                    }
                }
            })


        }
    }
}
