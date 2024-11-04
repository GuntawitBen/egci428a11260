package com.egci428.a11260

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import android.widget.ImageButton
import android.widget.ListView
import android.widget.SimpleAdapter
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileNotFoundException

class MainActivity : AppCompatActivity() {

    private lateinit var fortuneCookies: ArrayList<Map<String, String>>
    private lateinit var adapter: FortuneCookieAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val listView = findViewById<ListView>(R.id.listView)
        fortuneCookies = loadFortunes()

        adapter = FortuneCookieAdapter(this, fortuneCookies) {
            // This lambda will be executed after an item is removed
            saveFortunes(fortuneCookies) // Save updated fortunes to result.txt
        }
        listView.adapter = adapter

        val addBTN = findViewById<ImageButton>(R.id.addBTN)
        addBTN.setOnClickListener {
            startActivity(Intent(this, FortuneCookies::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        fortuneCookies.clear()
        fortuneCookies.addAll(loadFortunes())
        adapter.notifyDataSetChanged()
    }

    private fun saveFortunes(fortuneList: ArrayList<Map<String, String>>) {
        val file = File(filesDir, "result.txt")
        file.writeText("") // Clear the file before saving
        fortuneList.forEach { fortune ->
            file.appendText(JSONObject(fortune).toString() + "\n")
        }
    }

    private fun loadFortunes(): ArrayList<Map<String, String>> {
        val fortunes = ArrayList<Map<String, String>>()
        try {
            openFileInput("result.txt")?.bufferedReader()?.useLines { lines ->
                lines.forEach { line ->
                    try {
                        val jsonObject = JSONObject(line)
                        val message = jsonObject.getString("message")
                        val timestamp = jsonObject.getString("timestamp")
                        val status = jsonObject.optString("status", "neutral")

                        fortunes.add(mapOf("message" to message, "timestamp" to timestamp, "status" to status))
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            }
        } catch (e: FileNotFoundException) {
            Log.e("MainActivity", "result.txt not found")
        }
        return fortunes
    }

}


class FortuneCookieAdapter(
    private val context: Context,
    private val data: ArrayList<Map<String, String>>,
    private val saveCallback: () -> Unit
) : BaseAdapter() {

    override fun getCount(): Int = data.size

    override fun getItem(position: Int): Map<String, String> = data[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.row, parent, false)
        val resultTV = view.findViewById<TextView>(R.id.resultTV)
        val timeStampTV = view.findViewById<TextView>(R.id.timeStampTV)

        val fortuneData = getItem(position)
        val message = fortuneData["message"] ?: ""
        val timestamp = fortuneData["timestamp"] ?: ""
        val status = fortuneData["status"]

        resultTV.text = message
        timeStampTV.text = timestamp

        // Set text color based on status
        when (status) {
            "positive" -> resultTV.setTextColor(Color.BLUE)
            "negative" -> resultTV.setTextColor(Color.parseColor("#FFA500"))
            else -> resultTV.setTextColor(Color.BLACK)
        }

        view.setOnClickListener {
            // Animate the view before removing it
            view.animate().setDuration(150).alpha(0F).withEndAction {
                // Remove item from the list
                removeItem(position)
                notifyDataSetChanged()
                // Reset the view alpha
                view.alpha = 1.0F

                saveCallback()
            }
        }

        return view
    }

    private fun removeItem(position: Int) {
        data.removeAt(position)
        notifyDataSetChanged()
    }
}


