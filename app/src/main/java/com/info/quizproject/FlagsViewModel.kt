package com.info.quizproject

import android.app.Application
import android.os.CountDownTimer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.info.quizproject.dataclass.Question
import java.io.BufferedReader
import java.io.InputStreamReader

class FlagsViewModel(application: Application) : AndroidViewModel(application) {
    private val questionList = MutableLiveData<List<Question>>()
    private val currentQuestion = MutableLiveData<Question>()
    private val challengeTime = MutableLiveData<Long>()
    private val remainingTime = MutableLiveData<Long>()

    // Timer variables
    private var questionTimer: CountDownTimer? = null
    private var intervalTimer: CountDownTimer? = null

    fun fetchQuestions() {
        // Read from assets and parse JSON
        val jsonString = loadJSONFromAsset("questions.json")
        val questionType = object : TypeToken<List<Question>>() {}.type
        val questions = Gson().fromJson<List<Question>>(jsonString, questionType)

        // Populate the questionList LiveData
        questionList.value = questions
        if (questions.isNotEmpty()) {
            setCurrentQuestion(questions[0]) // Set the first question initially
        }
    }

    private fun loadJSONFromAsset(filename: String): String {
        val inputStream = getApplication<Application>().assets.open(filename)
        val reader = BufferedReader(InputStreamReader(inputStream))
        return reader.use { it.readText() }
    }

    private fun setCurrentQuestion(question: Question) {
        currentQuestion.value = question
    }

    fun getCurrentQuestion() = currentQuestion

    fun startChallengeAt(timeInMillis: Long) {
        challengeTime.value = timeInMillis
        // Setup Countdown Timer to start challenge
    }

    fun startQuestionTimer() {
        questionTimer = object : CountDownTimer(30_000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                remainingTime.value = millisUntilFinished / 1000
            }

            override fun onFinish() {
                // Handle the end of the question (show correct answer, etc.)
            }
        }.start()
    }

    fun scheduleNextQuestion() {
        intervalTimer = object : CountDownTimer(10_000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                // Interval countdown
            }

            override fun onFinish() {
                // Move to next question
                questionList.value?.let { questions ->
                    val currentIndex = questions.indexOf(currentQuestion.value)
                    if (currentIndex < questions.size - 1) {
                        setCurrentQuestion(questions[currentIndex + 1])
                        startQuestionTimer()
                    } else {
                        // Handle end of quiz
                    }
                }
            }
        }.start()
    }

    override fun onCleared() {
        super.onCleared()
        questionTimer?.cancel()
        intervalTimer?.cancel()
    }
}
