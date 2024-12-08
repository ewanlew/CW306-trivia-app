package com.ewan.triviaapp.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ewan.triviaapp.R
import com.ewan.triviaapp.models.TriviaQuestion

class TriviaActivity : AppCompatActivity() {

    private lateinit var txtQuestion: TextView
    private lateinit var txtQuestionNo: TextView
    private lateinit var txtCoinsEarned: TextView
    private lateinit var linLayAnswers: RadioGroup
    private lateinit var btnConfirmAnswer: View
    private lateinit var imgLife1: View
    private lateinit var imgLife2: View

    private var questions: List<TriviaQuestion> = listOf()
    private var currentQuestionIndex = 0
    private var lives = 2
    private var coins = 0
    private var coinValue = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trivia_questions)

        // Initialize views
        txtQuestion = findViewById(R.id.txtQuestion)
        txtQuestionNo = findViewById(R.id.txtQuestionNo)
        txtCoinsEarned = findViewById(R.id.txtCoinsEarned)
        linLayAnswers = findViewById(R.id.linLayAnswers)
        btnConfirmAnswer = findViewById(R.id.btnConfirmAnswer)
        imgLife1 = findViewById(R.id.imgLife1)
        imgLife2 = findViewById(R.id.imgLife2)

        // Retrieve data
        questions = intent.getParcelableArrayListExtra<TriviaQuestion>("questions") ?: listOf()
        val difficulty = intent.getStringExtra("difficulty") ?: "easy"

        // Determine coin value based on difficulty
        coinValue = when (difficulty) {
            "easy" -> 50
            "medium" -> 100
            "hard" -> 150
            else -> 50
        }

        if (questions.isEmpty()) {
            Toast.makeText(this, "No questions available.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Load the first question
        loadQuestion()

        btnConfirmAnswer.setOnClickListener {
            checkAnswer()
        }
    }

    private fun loadQuestion() {
        if (currentQuestionIndex >= questions.size) {
            // End of quiz
            Toast.makeText(this, "Quiz complete! Total coins: $coins", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        val question = questions[currentQuestionIndex]

        // Set question number and text
        txtQuestionNo.text = getString(R.string.question_number, currentQuestionIndex + 1)
        txtQuestion.text = question.question

        // Clear previous answers
        linLayAnswers.removeAllViews()

        // Dynamically load answers
        if (question.type == "boolean") {
            val trueOption = RadioButton(this).apply {
                text = "True"
                id = View.generateViewId()
            }
            val falseOption = RadioButton(this).apply {
                text = "False"
                id = View.generateViewId()
            }
            linLayAnswers.addView(trueOption)
            linLayAnswers.addView(falseOption)
        } else { // Multiple choice
            val allAnswers = question.incorrect_answers.toMutableList().apply {
                add(question.correct_answer)
                shuffle()
            }
            allAnswers.forEach { answer ->
                val radioButton = RadioButton(this).apply {
                    text = answer
                    id = View.generateViewId()
                }
                linLayAnswers.addView(radioButton)
            }
        }
    }

    private fun checkAnswer() {
        val selectedOptionId = linLayAnswers.checkedRadioButtonId
        if (selectedOptionId == -1) {
            Toast.makeText(this, "Please select an answer.", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedOption = findViewById<RadioButton>(selectedOptionId).text.toString()
        val correctAnswer = questions[currentQuestionIndex].correct_answer

        if (selectedOption == correctAnswer) {
            coins += coinValue
            txtCoinsEarned.text = getString(R.string.coins_earned, coins)
            Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show()
        } else {
            lives--
            Toast.makeText(this, "Incorrect!", Toast.LENGTH_SHORT).show()
            when (lives) {
                1 -> imgLife2.visibility = View.GONE
                0 -> {
                    imgLife1.visibility = View.GONE
                    Toast.makeText(this, "Game Over!", Toast.LENGTH_LONG).show()
                    finish()
                    return
                }
            }
        }

        currentQuestionIndex++
        loadQuestion()
    }
}
