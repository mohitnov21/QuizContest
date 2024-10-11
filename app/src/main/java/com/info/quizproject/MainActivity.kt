package com.info.quizproject

import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.info.quizproject.databinding.ActivityMainBinding
import com.info.quizproject.dataclass.QuestionData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var viewModel: MainViewModel? = null
    var quizData: QuestionData? = null
    private var currentQuestionIndex = 0  // Track the current question index
    private var countDownTimer: CountDownTimer? = null
    private var selectedOptionId: Int = -1  // To store the selected option's ID
    private var score = 0  // Track the total score (number of correct answers)
    private var totalQuestions = 0  // Track the total score (number of correct answers)
    private var selectedOptionIndex: Int = -1 // To store the selected option index
    private var selectedOptionView: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        viewModel = ViewModelProvider(this, ViewModelFactory(this)).get(MainViewModel::class.java)

        //binding.setViewModel(viewModel)
        binding!!.lifecycleOwner = this
        viewModel!!.getQuizData().observe(this, Observer { questions ->
            questions?.let {
                // Process your quiz data here
                totalQuestions = it.questions.size
                /*    Toast.makeText(
                        this,
                        "Quiz Data Loaded: ${it.questions.size} questions",
                        Toast.LENGTH_SHORT
                    ).show()*/
                quizData = it
            }
        })

        // Observe timer text changes
        viewModel!!.getTimerText().observe(
            this
        ) { timerText: String? ->
            binding!!.timerTextView.setText(timerText)
            binding.challengeInitialView.countdownTimer.text = timerText
        }
        changeVisibleView(0)
        binding.scheduleView.saveTv.setOnClickListener {
            // Get values from EditTexts
            val hourFirstStr: String = binding.scheduleView.hourFirst.getText().toString()
            val hourSecondStr: String = binding.scheduleView.hourSecond.getText().toString()
            val minuteFirstStr: String = binding.scheduleView.minuteFirst.getText().toString()
            val minuteSecondStr: String = binding.scheduleView.minuteSecond.getText().toString()
            val secondFirstStr: String = binding.scheduleView.secondFirst.getText().toString()
            val secondSecondStr: String = binding.scheduleView.secondSecond.getText().toString()

            // Parse input values
            val hourFirst = (if (hourFirstStr.isEmpty()) "0" else hourFirstStr).toInt()
            val hourSecond =
                (if (hourSecondStr.isEmpty()) "0" else hourSecondStr).toInt()
            val minuteFirst =
                (if (minuteFirstStr.isEmpty()) "0" else minuteFirstStr).toInt()
            val minuteSecond =
                (if (minuteSecondStr.isEmpty()) "0" else minuteSecondStr).toInt()
            val secondFirst =
                (if (secondFirstStr.isEmpty()) "0" else secondFirstStr).toInt()
            val secondSecond =
                (if (secondSecondStr.isEmpty()) "0" else secondSecondStr).toInt()

            // Calculate total time in milliseconds
            val totalHours = hourFirst * 10 + hourSecond
            val totalMinutes = minuteFirst * 10 + minuteSecond
            val totalSeconds = secondFirst * 10 + secondSecond

            val totalMilliseconds =
                ((totalHours * 3600 + totalMinutes * 60 + totalSeconds) * 1000).toLong()

            // Start the timer
            viewModel!!.startTimer(totalMilliseconds, {
                showTimeWarningToast()  // Show the toast warning
            }, {
                onTimerFinish() // Show the toast for timer finish
            })
        }
    }

    fun changeVisibleView(pos: Int) {
        when (pos) {
            0 -> {
                binding.scheduleView.root.visibility = View.VISIBLE
                binding.challengeStartView.root.visibility = View.GONE
                binding.challengeInitialView.root.visibility = View.GONE
                binding.gameOver.root.visibility = View.GONE
            }

            1 -> {
                binding.scheduleView.root.visibility = View.GONE
                binding.challengeStartView.root.visibility = View.GONE
                binding.challengeInitialView.root.visibility = View.VISIBLE
                binding.gameOver.root.visibility = View.GONE
            }

            2 -> {
                binding.scheduleView.root.visibility = View.GONE
                binding.challengeStartView.root.visibility = View.VISIBLE
                binding.challengeInitialView.root.visibility = View.GONE
                binding.gameOver.root.visibility = View.GONE
            }

            3 -> {
                binding.scheduleView.root.visibility = View.GONE
                binding.challengeStartView.root.visibility = View.GONE
                binding.challengeInitialView.root.visibility = View.GONE
                binding.gameOver.root.visibility = View.VISIBLE

            }
        }
    }

    fun onTimerFinish() {
        //  Toast.makeText(this, "Timer Finished!", Toast.LENGTH_SHORT).show()
        changeVisibleView(2)
        displayQuestion()

    }

    private fun loadNextQuestion() {
        // Increment the question index
        resetQuestionView()
        enableDisableOptionsClick(true)

        currentQuestionIndex++

        // If there are more questions, display the next one
        if (currentQuestionIndex < quizData?.questions?.size ?: 0) {
            displayQuestion()
        } else {
            // All questions have been answered
            showFinalScore()
        }
    }

    private fun showFinalScore() {

        // Optionally, you can also show this score in the UI (e.g., in a TextView or dialog)
        changeVisibleView(3)
        CoroutineScope(Dispatchers.Default).launch {
            delay(1000) // Wait for 1 second
            withContext(Dispatchers.Main) {
                binding.gameOver.gameOverTv.visibility = View.GONE
                binding.gameOver.scoreView.visibility = View.VISIBLE
                binding.gameOver.finalScoreTv.text = "$score/$totalQuestions"
            }
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

    private fun displayQuestion() {

        val question = quizData?.questions?.getOrNull(currentQuestionIndex)

        if (question != null) {
            // Set question number
            binding.challengeStartView.questionNumberTv.text = (currentQuestionIndex + 1).toString()

            // Set flag image
            val drawableId = getFlagResource(question.country_code)
            binding.challengeStartView.countryFlag.setImageResource(drawableId)

            // Set question text
            binding.challengeStartView.ques.text = question.countries[0].country_name
            binding.challengeStartView.ques2.text = question.countries[1].country_name
            binding.challengeStartView.ques3.text = question.countries[2].country_name
            binding.challengeStartView.ques4.text = question.countries[3].country_name
            setOptionClickListeners()
            // Start the timer for the displayed question
            startQuestionTimer()
        } else {
            //   Toast.makeText(this@MainActivity, "Quiz finished!", Toast.LENGTH_SHORT).show()
            // Handle the end of the quiz here (e.g., show results or reset the quiz)
            changeVisibleView(3)
            CoroutineScope(Dispatchers.Default).launch {
                delay(1000) // Wait for 1 second
                withContext(Dispatchers.Main) {
                    binding.gameOver.gameOverTv.visibility = View.GONE
                    binding.gameOver.scoreView.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun showTimeWarningToast() {
        // Toast.makeText(this, "Less than 20 seconds remaining!", Toast.LENGTH_SHORT).show()
        changeVisibleView(1)

    }

    var timerQuestionRemainingSeconds = 0
    private fun startQuestionTimer()= with(binding.challengeStartView) {
        // If a timer is already running, cancel it
        countDownTimer?.cancel()

        // Create a new timer that runs for 10 seconds
        countDownTimer = object : CountDownTimer(10000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                // Update the timer UI, e.g., display remaining time to the user (optional)
                val secondsRemaining = (millisUntilFinished / 1000).toInt()
                timerQuestionRemainingSeconds = secondsRemaining
                val minutes = secondsRemaining / 60
                val seconds = secondsRemaining % 60

                binding.timerTextView.text = String.format("%02d:%02d", minutes, seconds)
            }

            override fun onFinish() {
                // Handle what happens when the timer finishes
                //   Toast.makeText(this@MainActivity, "Time's up!", Toast.LENGTH_SHORT).show()
                if (selectedOptionIndex == -1) {
                    // You can handle no selection here (e.g., treat as wrong or skip)
                    Toast.makeText(
                        this@MainActivity,
                        "No option selected, moving to the next question.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    // Call checkAnswer with the selected option
                    val optionsView = listOf(option1, option2, option3, option4)

                    checkAnswer(selectedOptionView!!, selectedOptionIndex, optionsView.get(selectedOptionIndex))
                }
                CoroutineScope(Dispatchers.Main).launch {
                    enableDisableOptionsClick(false)

                    delay(10000) // Wait for 10 seconds (10000 milliseconds)
                    loadNextQuestion() // After waiting, load the next question
                }
            }
        }.start()
    }

    private fun highlightSelectedOption(view: TextView) {
        // Reset background of all options before highlighting the selected one
        //  resetQuestionView()
        enableDisableOptionsClick(false)
        view.setTextColor(Color.WHITE)

        // Highlight the selected option
        view.setBackgroundResource(R.drawable.selectedoption)
    }

    private fun setOptionClickListeners() = with(binding.challengeStartView) {

        ques.setOnClickListener {
            selectedOptionIndex = 0
            selectedOptionView = it
            highlightSelectedOption(ques)
        }
        ques2.setOnClickListener {
            selectedOptionIndex = 1
            selectedOptionView = it
            highlightSelectedOption(ques2)
        }
        ques3.setOnClickListener {
            selectedOptionIndex = 2
            selectedOptionView = it
            highlightSelectedOption(ques3)
        }
        ques4.setOnClickListener {
            selectedOptionIndex = 3
            selectedOptionView = it
            highlightSelectedOption(ques4)
        }
    }

    private fun checkAnswer(view: View, selectedOptionIndex: Int, optionView: TextView) =
        with(binding.challengeStartView) {
            val question = quizData?.questions?.getOrNull(currentQuestionIndex)
            Log.e("checkAnswer", "index $selectedOptionIndex")

            question?.run {
                // Check if the selected option's ID matches the correct answer ID
                val selectedCountry = countries[selectedOptionIndex]
                if (selectedCountry.id == answer_id) {
                    // Correct answer
                    //    Toast.makeText(this@MainActivity, "Correct!", Toast.LENGTH_SHORT).show()
                    view.setBackgroundResource(R.drawable.right_answer_bg) // Optional: change background for correct answer
                    score++  // Increment score for a correct answer
                    highlightCorrectAnswer(selectedOptionIndex)
                } else {
                    view.setBackgroundResource(R.drawable.wrong_answer_bg) // Optional: change background for wrong answer
                    setWrongOptionTextView(optionView)
                    val correctIndex = countries.indexOfFirst { it.id == answer_id }
                    highlightCorrectAnswer(correctIndex) // Highlight the correct view
                }

                // Wait for 1 second before moving to the next question
                /* Handler().postDelayed({
                     resetQuestionView()

                 }, 1000)*/
            }
        }

    private fun enableDisableOptionsClick(isEnable: Boolean) = with(binding.challengeStartView)
    {
        ques.isClickable = isEnable
        ques2.isClickable = isEnable
        ques3.isClickable = isEnable
        ques4.isClickable = isEnable
    }

    private fun highlightCorrectAnswer(correctIndex: Int) = with(binding.challengeStartView) {
        when (correctIndex) {
            0 -> {
                ques.setBackgroundResource(R.drawable.right_answer_bg)
                setCorrectOptionTextView(option1)
                ques.setTextColor(ContextCompat.getColor(this@MainActivity,R.color.options_text_color_not_selected))

            }

            1 -> {
                ques2.setBackgroundResource(R.drawable.right_answer_bg)
                setCorrectOptionTextView(option2)
                ques2.setTextColor(ContextCompat.getColor(this@MainActivity,R.color.options_text_color_not_selected))
            }

            2 -> {
                ques3.setBackgroundResource(R.drawable.right_answer_bg)
                setCorrectOptionTextView(option3)
                ques3.setTextColor(ContextCompat.getColor(this@MainActivity,R.color.options_text_color_not_selected))
            }

            3 -> {
                ques4.setBackgroundResource(R.drawable.right_answer_bg)
                setCorrectOptionTextView(option4)
                ques4.setTextColor(ContextCompat.getColor(this@MainActivity,R.color.options_text_color_not_selected))
            }
        }
    }

    fun resetQuestionView() = with(binding.challengeStartView) {
        val quesOptions = listOf(ques,ques2,ques3,ques4)
        quesOptions.forEach { option->
            option.setBackgroundResource(R.drawable.default_ques_bg)
            option.setTextColor(ContextCompat.getColor(this@MainActivity,R.color.options_text_color_not_selected))
        }
//        ques.setBackgroundResource(R.drawable.default_ques_bg)
//        ques2.setBackgroundResource(R.drawable.default_ques_bg)
//        ques3.setBackgroundResource(R.drawable.default_ques_bg)
//        ques4.setBackgroundResource(R.drawable.default_ques_bg)

        selectedOptionIndex = -1
        selectedOptionView = null

        val optionsView = listOf(option1, option2, option3, option4)
        optionsView.forEach { v ->
            v.visibility = View.INVISIBLE
            v.text = ""
        }
    }

    fun setCorrectOptionTextView(v: TextView) {
        v.apply {
            visibility = View.VISIBLE
            text = "CORRECT"
            setTextColor(Color.GREEN)
        }

    }

    fun setWrongOptionTextView(v: TextView) {
        v.apply {
            visibility = View.VISIBLE
            text = "WRONG"
            setTextColor(Color.RED)
        }

    }
}
