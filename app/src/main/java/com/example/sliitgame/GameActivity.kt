package com.example.sliitgame

import  android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlin.random.Random
import android.content.SharedPreferences

class GameActivity : AppCompatActivity() {
    private lateinit var startButton: Button
    private lateinit var textView: TextView
    private lateinit var textView2: TextView
    private lateinit var textView4: TextView
    private lateinit var textView6: TextView
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var videoView: VideoView
    private var popVideoPlayedCount = 0

    private var isCounting = false
    private var count = 0.0
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_game)

        // Initialize SharedPreferences for storing high score
        sharedPreferences = getSharedPreferences("game_data", Context.MODE_PRIVATE)

        // Initialize views
        videoView = findViewById(R.id.videoView)
        startButton = findViewById(R.id.button)
        textView = findViewById(R.id.textView)
        textView2 = findViewById(R.id.textView2)
        textView4 = findViewById(R.id.textView4)
        textView6 = findViewById(R.id.textView6)
        textView6.text = getHighScore().toString() // Display high score

        // Set up completion listener for the videoView
        videoView.setOnCompletionListener {
            if (it == videoView) {
                if (popVideoPlayedCount < 2) {
                    popVideoPlayedCount++
                    playPopVideo()
                }
            }
        }

        // Set click listener for start button
        startButton.setOnClickListener {
            val currentValue = textView.text.toString().toIntOrNull() ?: 0
            if (currentValue in listOf(10, 20, 30)) {
                if (isCounting) {
                    stopCounting()
                } else {
                    startCounting()
                    playBubbleVideo()
                }
            } else {
                Toast.makeText(this, "Enter a value", Toast.LENGTH_SHORT).show()
            }
        }

        // Set click listeners for decrease buttons
        findViewById<Button>(R.id.button2).setOnClickListener {
            decreaseValueFromTextView4(10)
        }

        findViewById<Button>(R.id.button3).setOnClickListener {
            decreaseValueFromTextView4(20)
        }

        findViewById<Button>(R.id.button4).setOnClickListener {
            decreaseValueFromTextView4(30)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun startCounting() {
        count = 1.0 // Reset count to 1.0
        isCounting = true
        startButton.text = "Stop"
        startButton.setBackgroundColor(ContextCompat.getColor(this, R.color.orange)) // Change button color to orange

        // Random time between 2. 0 and 15.00 seconds
        val randomTime = Random.nextDouble(1.00, 25.00)

        // Start counting loop
        handler.post(object : Runnable {
            override fun run() {
                count += 0.01
                textView2.text = String.format("%.2f", count)
                if (isCounting) {
                    handler.postDelayed(this, 90)
                }
            }
        })

        // Schedule automatic stop after a random time
        handler.postDelayed({
            if (isCounting) {
                stopCounting(autoStop = true)
            }
        }, (randomTime * 1000).toLong())
    }

    @SuppressLint("SetTextI18n")
    private fun stopCounting(autoStop: Boolean = false) {
        isCounting = false
        startButton.text = "Start"
        startButton.setBackgroundColor(ContextCompat.getColor(this, R.color.red))

        // Stop the bubblev video
        videoView.stopPlayback()

        // Process counting result
        if (!autoStop) {
            val multiplier = textView.text.toString().toDouble()
            val addition = (multiplier * count).toInt()
            val currentTotal = textView4.text.toString().toInt()
            val updatedTotal = currentTotal + addition
            textView4.text = updatedTotal.toString()

            // Update high score if the current total is higher
            val highScore = getHighScore()
            if (updatedTotal > highScore) {
                saveHighScore(updatedTotal)
                textView6.text = updatedTotal.toString() // Update textView6 with the new high score
            }
        } else {
            // Play the pop video two times
            playPopVideo()
            playPopVideo()
        }

        textView.text = "0" // Reset textView to 0
        startButton.isEnabled = true // Enable start button
    }

    private fun playBubbleVideo() {
        videoView.setVideoURI(Uri.parse("android.resource://" + packageName + "/" + R.raw.bubblev))
        videoView.setOnPreparedListener {
            it.isLooping = true
            it.start()
        }
        videoView.visibility = View.VISIBLE
    }

    private fun playPopVideo() {
        videoView.setVideoURI(Uri.parse("android.resource://" + packageName + "/" + R.raw.pop))
        videoView.setOnPreparedListener {
            it.start()
        }
        videoView.visibility = View.VISIBLE
    }

    private fun decreaseValueFromTextView4(valueToDecrease: Int) {
        val currentValue = textView4.text.toString().toInt()
        val newValue = currentValue - valueToDecrease
        textView4.text = newValue.toString()
        updateTextView(valueToDecrease.toString())

        // Check if the new value is less than 0 and navigate to GameOverActivity
        if (newValue < 0) {
            val gameOverIntent = Intent(this, GameOver::class.java)
            startActivity(gameOverIntent)
            finish() // Close the current activity
        }
    }

    private fun updateTextView(value: String) {
        textView.text = value
        startButton.isEnabled = value != "0" // Enable start button when textView value is not 0
    }

    fun updateTextView(view: View) {}
    fun onGetStartClicked(view: View) {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun saveHighScore(score: Int) {
        val editor = sharedPreferences.edit()
        editor.putInt("high_score", score)
        editor.apply()
    }

    private fun getHighScore(): Int {
        return sharedPreferences.getInt("high_score", 0)
    }
}
