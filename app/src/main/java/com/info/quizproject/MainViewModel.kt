package com.info.quizproject

import android.os.CountDownTimer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel


class MainViewModel : ViewModel() {
    private val isScheduleVisible = MutableLiveData<Boolean>()
    private val isChallengeInitialVisible = MutableLiveData<Boolean>()
    private val isChallengeStartVisible = MutableLiveData<Boolean>()
    private val timerText = MutableLiveData<String>()

    private var countDownTimer: CountDownTimer? = null
    private var isTimerRunning = false

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

    fun startTimer(totalMilliseconds: Long) {
        if (isTimerRunning) {
            // If a timer is already running, cancel it
            countDownTimer!!.cancel()
        }

        countDownTimer = object : CountDownTimer(totalMilliseconds, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = (millisUntilFinished / 1000).toInt()
                val minutes = secondsRemaining / 60
                val seconds = secondsRemaining % 60
                timerText.value = String.format("%02d:%02d", minutes, seconds)
            }

            override fun onFinish() {
                isTimerRunning = false
                timerText.value = "00:00"
                // Show a toast when the timer finishes
                // You can also trigger an event for the activity to handle the toast.
            }
        }.start()

        isTimerRunning = true
    }

    fun stopTimer() {
        if (isTimerRunning) {
            countDownTimer!!.cancel()
            isTimerRunning = false
        }
    }
}
