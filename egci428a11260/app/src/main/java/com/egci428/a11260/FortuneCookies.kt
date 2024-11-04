package com.egci428.a11260

import android.content.Context
import android.content.Intent
import android.graphics.Color
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
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class FortuneCookies : AppCompatActivity() {

    private var loadedStatus: String = ""

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
        val dateTimeTV = findViewById<TextView>(R.id.dateTimeTV)
        var dateTime = "Date:"

        circleBTN.text = "Make a Wish"

        circleBTN.setOnClickListener {
            if (circleBTN.text == "Make a Wish") {

                // PREPARE JSONURL
                val num = kotlin.random.Random.nextInt(0, 9).toString()
                val jsonURL = "https://egci428-d78f6-default-rtdb.firebaseio.com/fortunecookies/$num.json"

                // TOAST WAITING
                Toast.makeText(this, "Waiting...", Toast.LENGTH_SHORT).show()

                // COOKIE OPENED IMAGE
                cookiesIV.setImageResource(R.drawable.opened_cookie)

                // REQUEST
                val request = Request.Builder().url(jsonURL).build()

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        runOnUiThread {
                            Toast.makeText(this@FortuneCookies, "Failed to load message", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onResponse(call: Call, response: Response) {
                        val timestamp = System.currentTimeMillis()
                        dateTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(timestamp))
                        dateTimeTV.text = dateTime

                        response.body?.let { responseBody ->
                            val jsonString = responseBody.string()
                            val jsonObject = JSONObject(jsonString)
                            var loadedMessage = jsonObject.optString("message", "No message found")
                            loadedStatus = jsonObject.optString("status", "No status found")

                            runOnUiThread {
                                resultTV.text = loadedMessage

                                when (loadedStatus) {
                                    "positive" -> resultTV.setTextColor(Color.BLUE)
                                    "negative" -> resultTV.setTextColor(Color.parseColor("#FFA500"))
                                    else -> resultTV.setTextColor(Color.BLACK)
                                }

                                // CHANGE CIRCLE BUTTON TO SAVE
                                circleBTN.text = "Save"
                            }
                        }
                    }
                })

            } else if (circleBTN.text == "Save") {
                val fortuneMessage = resultTV.text.toString()
                val timestamp = dateTimeTV.text.toString()
                val status = loadedStatus

                // Create JSON object with message, timestamp, and status
                val fortuneJson = JSONObject().apply {
                    put("message", fortuneMessage)
                    put("timestamp", timestamp)
                    put("status", status)
                }

                // Save to file
                saveFortune(fortuneJson)

                // Navigate back to MainActivity
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }


    private fun saveFortune(fortuneJson: JSONObject) {
        val file = File(filesDir, "result.txt")
        file.appendText(fortuneJson.toString() + "\n")
    }
}
