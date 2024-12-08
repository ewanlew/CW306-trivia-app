package com.ewan.triviaapp.activities

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Html
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
    private lateinit var txtGemsEarned: TextView
    private lateinit var linLayAnswers: RadioGroup
    private lateinit var btnConfirmAnswer: View
    private lateinit var txtLivesCount: TextView

    private var questions: List<TriviaQuestion> = listOf()
    private var currentQuestionIndex = 0
    private var lives = 0
    private var gems = 0
    private var gemValue = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trivia_questions)

        txtQuestion = findViewById(R.id.txtQuestion)
        txtQuestionNo = findViewById(R.id.txtQuestionNo)
        txtGemsEarned = findViewById(R.id.txtCoinsEarned)
        linLayAnswers = findViewById(R.id.linLayAnswers)
        btnConfirmAnswer = findViewById(R.id.btnConfirmAnswer)
        txtLivesCount = findViewById(R.id.txtLivesCount)

        // Retrieve data
        questions = intent.getParcelableArrayListExtra<TriviaQuestion>("questions") ?: listOf()
        val difficulty = intent.getStringExtra("difficulty") ?: "easy"

        // Determine gem value based on difficulty
        gemValue = when (difficulty) {
            "easy" -> 50
            "medium" -> 100
            "hard" -> 150
            else -> 50
        }

        // Calculate lives based on the number of questions
        lives = calculateLives(questions.size)
        updateLivesDisplay()

        // Set initial gem count to 0
        txtGemsEarned.text = getString(R.string.gems_earned, 0)

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

    private fun calculateLives(numQuestions: Int): Int {
        return (numQuestions / 5) + 1 // Calculate lives for every 5 questions
    }

    private fun updateLivesDisplay() {
        txtLivesCount.text = "x$lives"
    }

    private fun loadQuestion() {
        if (currentQuestionIndex >= questions.size) {
            // End of quiz
            Toast.makeText(this, "Quiz complete! Total gems: $gems", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        val question = questions[currentQuestionIndex]

        // Set question number and text
        txtQuestionNo.text = getString(R.string.question_number, currentQuestionIndex + 1)
        txtQuestion.text = Html.fromHtml(question.question) // Decode special characters

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
                    text = Html.fromHtml(answer) // Decode special characters
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

        val selectedOption = findViewById<RadioButton>(selectedOptionId)
        val correctAnswer = Html.fromHtml(questions[currentQuestionIndex].correct_answer).toString() // Decode special characters

        if (selectedOption.text.toString() == correctAnswer) {
            gems += gemValue
            txtGemsEarned.text = getString(R.string.gems_earned, gems)
            Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show()
            currentQuestionIndex++
            loadQuestion()
        } else {
            // Highlight the incorrect answer
            lives--
            selectedOption.setTextColor(Color.RED)
            selectedOption.setTypeface(null, Typeface.BOLD)
            selectedOption.paintFlags = selectedOption.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG

            updateLivesDisplay()

            Toast.makeText(this, "Incorrect! Try again.", Toast.LENGTH_SHORT).show()

            if (lives <= 0) {
                Toast.makeText(this, "Game Over!", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }
}
