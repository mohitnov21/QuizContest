package com.info.quizproject

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.info.quizproject.databinding.ActivityMainBinding
import com.info.quizproject.dataclass.QuestionData

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var viewModel: MainViewModel? = null
    var quizData: QuestionData? = null
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
                Toast.makeText(
                    this,
                    "Quiz Data Loaded: ${it.questions.size} questions",
                    Toast.LENGTH_SHORT
                ).show()
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

    fun onTimerFinish() {
        Toast.makeText(this, "Timer Finished!", Toast.LENGTH_SHORT).show()
        changeVisibleView(2)
        setUpQuestionaire()

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

    private fun setUpQuestionaire() = with(binding.challengeStartView) {

        val question = quizData?.questions?.get(0)

        // Set question number

        question?.run {
            questionNumberTv.text = answer_id.toString()

            // Set flag image (assuming you're mapping the country code to a drawable resource)

            val drawableId = countries[0].let { getFlagResource(question.country_code) }
            countryFlag.setImageResource(drawableId)

            // Set question text (assuming the first country name as the question)

            ques.text = countries[0].country_name

            // Set other options (assuming you have more country names)

            ques2.text = countries[1].country_name


            ques3.text = countries[2].country_name


            ques4.text = countries[3].country_name

        }

    }

    private fun showTimeWarningToast() {
        // Toast.makeText(this, "Less than 20 seconds remaining!", Toast.LENGTH_SHORT).show()
        changeVisibleView(1)

    }

}
