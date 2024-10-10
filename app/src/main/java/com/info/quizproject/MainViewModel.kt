package com.info.quizproject

import android.app.Application
import android.content.Context
import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.info.quizproject.dataclass.Question
import com.info.quizproject.dataclass.QuestionData
import java.io.IOException

class MainViewModel(private val context: Context) : ViewModel() {
    private val isScheduleVisible = MutableLiveData<Boolean>()
    private val isChallengeInitialVisible = MutableLiveData<Boolean>()
    private val isChallengeStartVisible = MutableLiveData<Boolean>()
    private val timerText = MutableLiveData<String>()

    private var countDownTimer: CountDownTimer? = null
    private var isTimerRunning = false
    private val quizData = MutableLiveData<QuestionData>() // Expecting a list

    init {
        loadQuizData() // Load quiz data during initialization
    }

    fun getQuizData(): LiveData<QuestionData> {
        return quizData
    }


    private fun loadQuizData() {
        // Load JSON data from the assets file
        val jsonString: String = try {
            context.assets.open("questions.json").bufferedReader().use { it.readText() }
        } catch (e: IOException) {
            Log.e("MainViewModel", "Error reading JSON from assets", e)
            return
        }

        // Use Gson to parse the JSON as a list
        val gson = Gson()
        val quizDataType = object : TypeToken<QuestionData>() {}.type
        try {
            val data: QuestionData = gson.fromJson(jsonString, quizDataType)
            quizData.value = data // Set the parsed data to LiveData
            Log.d("QuizData", "Loaded JSON: ${jsonString}")

        } catch (e: Exception) {
            Log.e("MainViewModel", "Error parsing JSON", e)
        }
    }

    fun getIsScheduleVisible(): LiveData<Boolean> {
        return isScheduleVisible
    }

    fun getIsChallengeInitialVisible(): LiveData<Boolean> {
        return isChallengeInitialVisible
    }

    fun getIsChallengeStartVisible(): LiveData<Boolean> {
        return isChallengeStartVisible
    }

    fun getTimerText(): LiveData<String> {
        return timerText
    }

    fun initialize() {
        isScheduleVisible.value = true
        isChallengeInitialVisible.value = false
        isChallengeStartVisible.value = false
        timerText.value = "00:00"
    }

    fun startTimer(totalMilliseconds: Long, onTimeWarning: () -> Unit, onTimerFinish: () -> Unit) {
        if (isTimerRunning) {
            // If a timer is already running, cancel it
            countDownTimer?.cancel()
        }

        countDownTimer = object : CountDownTimer(totalMilliseconds, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = (millisUntilFinished / 1000).toInt()
                val minutes = secondsRemaining / 60
                val seconds = secondsRemaining % 60
                timerText.value = String.format("%02d:%02d", minutes, seconds)

                // Check if the remaining time is less than 20 seconds
                if (secondsRemaining < 20) {
                    onTimeWarning() // Call the callback to show the toast
                }
            }

            override fun onFinish() {
                isTimerRunning = false
                timerText.value = "00:00"
                // Call the callback for timer finish
                onTimerFinish() // Notify that the timer has finished
                Log.e("MainViewModel", "Timer finished")
            }
        }.start()

        isTimerRunning = true
    }

    fun stopTimer() {
        if (isTimerRunning) {
            countDownTimer?.cancel()
            isTimerRunning = false
        }
    }
}
