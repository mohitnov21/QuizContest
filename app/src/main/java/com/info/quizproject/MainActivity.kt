package com.info.quizproject

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.info.quizproject.databinding.ActivityMainBinding
import com.info.quizproject.dataclass.QuestionData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader
import java.util.Calendar

class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: FlagsViewModel
    private lateinit var binding: ActivityMainBinding
    private var hour: Int = 0
    private var minutes: Int = 0
    private var seconds: Int = 0
    private var timer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.lifecycleOwner = this
        viewModel = ViewModelProvider(this).get(FlagsViewModel::class.java)

        changeVisibleView(0)

        // Observe the questions
        viewModel.fetchQuestions()

        // Observe current question changes
       /* viewModel.currentQuestion.observe(this, Observer { question ->
            // Display flag and country code based on current question
            if (question != null) {
                binding.flagImageView.setImageResource(getFlagResource(question.country_code))
                binding.countryCodeTextView.text = question.country_code
            }
        })
*/
        // Save button listener to set challenge time
        binding.scheduleView.saveTv.setOnClickListener {
            val time = System.currentTimeMillis() + 20 * 1000 // Example: set for 20 seconds later
            viewModel.startChallengeAt(time)
            scheduleChallenge()
        }
    }

    private fun getFlagResource(countryCode: String): Int {
        // Map the country code to corresponding flag resource
        return when (countryCode) {
            "NZ" -> R.drawable.england
            "AW" -> R.drawable.nz
            "EC" -> R.drawable.ecuador__ec_
            "PY" -> R.drawable.paraguay__py_
            "KG" -> R.drawable.kyrgyzstan__kg_
            "PM" -> R.drawable.saint_pierre_and_miquelon__pm_
            "TM" -> R.drawable.turkmenistan__tm_
            "JP" -> R.drawable.japan__jp_
            "GA" -> R.drawable.gabon__ga_
            "MQ" -> R.drawable.martinique__mq_
            "BZ" -> R.drawable.belize__bz_
            "CZ" -> R.drawable.czech_republic__cz_
            "AE" -> R.drawable.united_arab_emirates
            "JE" -> R.drawable.jersey
            "LS" -> R.drawable.lesotho
            // Add other mappings as needed
            else -> R.drawable.england // Fallback
        }
    }

    fun changeVisibleView(pos: Int) {
        when (pos) {
            0 -> {
                binding.scheduleView.root.visibility = View.VISIBLE
                binding.challengeStartView.root.visibility = View.GONE
                binding.challengeInitialView.root.visibility = View.GONE
            }
            1 -> {
                binding.scheduleView.root.visibility = View.GONE
                binding.challengeStartView.root.visibility = View.GONE
                binding.challengeInitialView.root.visibility = View.VISIBLE
            }
            2 -> {
                binding.scheduleView.root.visibility = View.GONE
                binding.challengeStartView.root.visibility = View.VISIBLE
                binding.challengeInitialView.root.visibility = View.GONE
            }
        }
    }

    private fun scheduleChallenge() = with(binding.scheduleView) {
        // Retrieve values from the EditTexts
        val totalHour = hourFirst.text.toString() + hourSecond.text.toString()
        val totalMinutes = minuteFirst.text.toString() + minuteSecond.text.toString()
        val totalSeconds = secondFirst.text.toString() + secondSecond.text.toString()
        hour = totalHour.toIntOrNull() ?: 0
        minutes = totalMinutes.toIntOrNull() ?: 0
        seconds = totalSeconds.toIntOrNull() ?: 0
        Log.e("schedule time", "$hour $minutes $seconds")

        // Set the target time using Calendar
        val currentTime = Calendar.getInstance()
        val targetTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minutes)
            set(Calendar.SECOND, seconds)
        }

        // Calculate the difference in milliseconds
        val timeDifference = targetTime.timeInMillis - currentTime.timeInMillis

        // If time is in the past, schedule for the next day
        if (timeDifference < 0) {
            targetTime.add(Calendar.DAY_OF_MONTH, 1) // Set to the next day
        }

        // Start a countdown until the challenge begins
        startCountdown(targetTime.timeInMillis - currentTime.timeInMillis)
    }

    // Function to start the countdown
    private fun startCountdown(timeInMillis: Long) {
        if (timer != null) {
            timer?.cancel() // Cancel any existing timer
        }

        // Display countdown and start challenge when it reaches zero
        timer = object : CountDownTimer(timeInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                // Update the countdown text (formatted as HH:MM:SS)
                val secondsLeft = millisUntilFinished / 1000
                val hoursCount = secondsLeft / 3600
                val minutesCount = (secondsLeft % 3600) / 60
                val secondsCount = secondsLeft % 60

                // Display the countdown
              //  binding.scheduleTv.text = String.format("%02d:%02d:%02d", hoursCount, minutesCount, secondsCount)

                if (hoursCount < 0) {
                    if (minutesCount < 0) {
                        if (secondsCount < 21) {
                            View.GONE
                            changeVisibleView(1)
                            binding.challengeInitialView.countdownTimer.text=String.format("%02d",secondsCount)
                        }
                    }
                }
            }

            override fun onFinish() {
                // Start the challenge when countdown finishes
                changeVisibleView(2)
                startChallenge()
            }
        }.start()
    }

    // Function to start the challenge
    private fun startChallenge() {
        // TODO: Implement the challenge logic, e.g., showing the first question
     //   binding.scheduleTv.text = "Challenge Started!"
    }

    override fun onPause() {
        super.onPause()
        val sharedPreferences = getSharedPreferences("FlagsChallengePrefs", MODE_PRIVATE)
      /*  with(sharedPreferences.edit()) {
            putLong("SCHEDULED_TIME", viewModel.challengeTime.value ?: 0L)
            putInt("CURRENT_QUESTION_INDEX", viewModel.getCurrentQuestion() ?: 0)
            putLong("REMAINING_TIME", viewModel.remainingTime.value ?: 0L)
            apply()
        }*/
    }

    override fun onResume() {
        super.onResume()
        val sharedPreferences = getSharedPreferences("FlagsChallengePrefs", MODE_PRIVATE)
        val scheduledTime = sharedPreferences.getLong("SCHEDULED_TIME", 0L)
        val currentQuestionIndex = sharedPreferences.getInt("CURRENT_QUESTION_INDEX", 0)
        val remainingTime = sharedPreferences.getLong("REMAINING_TIME", 30L)

       /* if (scheduledTime > System.currentTimeMillis()) {
            startCountdownToChallenge(scheduledTime)
        } else {
            viewModel.startQuestionTimer()
        }*/
    }
}
