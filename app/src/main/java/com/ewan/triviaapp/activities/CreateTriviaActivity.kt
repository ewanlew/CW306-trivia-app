package com.ewan.triviaapp.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.ewan.triviaapp.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import nl.dionsegijn.konfetti.xml.BuildConfig
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class CreateTriviaActivity : AppCompatActivity() {

    private lateinit var editTxtTriviaName: EditText
    private lateinit var editTxtQuestionTitle: EditText
    private lateinit var rdoGrpQuestionType: RadioGroup
    private lateinit var rdoMultipleChoice: RadioButton
    private lateinit var rdoTrueFalse: RadioButton
    private lateinit var questionNoTextView: TextView
    private lateinit var btnNext: FloatingActionButton
    private lateinit var btnPrevious: FloatingActionButton
    private lateinit var btnFinish: Button

    private lateinit var linearOptionsContainer: LinearLayout
    private lateinit var rdoGrpAnswers: RadioGroup
    private lateinit var editTxtOption1: EditText
    private lateinit var editTxtOption2: EditText
    private lateinit var editTxtOption3: EditText
    private lateinit var editTxtOption4: EditText
    private lateinit var rdoOption1: RadioButton
    private lateinit var rdoOption2: RadioButton
    private lateinit var rdoOption3: RadioButton
    private lateinit var rdoOption4: RadioButton

    private val questions = mutableListOf<Question>()
    private var currentQuestionIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_trivia)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        editTxtTriviaName = findViewById(R.id.editTxtTriviaName)
        editTxtQuestionTitle = findViewById(R.id.editTxtQuestionTitle)
        rdoGrpQuestionType = findViewById(R.id.rdoGrpQuestionType)
        rdoMultipleChoice = findViewById(R.id.rdoMultipleChoice)
        rdoTrueFalse = findViewById(R.id.rdoTrueFalse)
        questionNoTextView = findViewById(R.id.tvQuestionNo)
        btnNext = findViewById(R.id.btnNext)
        btnPrevious = findViewById(R.id.btnPrevious)
        btnFinish = findViewById(R.id.btnFinish)

        linearOptionsContainer = findViewById(R.id.linearOptionsContainer)
        rdoGrpAnswers = findViewById(R.id.rdoGrpAnswers)
        editTxtOption1 = findViewById(R.id.editTxtOption1)
        editTxtOption2 = findViewById(R.id.editTxtOption2)
        editTxtOption3 = findViewById(R.id.editTxtOption3)
        editTxtOption4 = findViewById(R.id.editTxtOption4)
        rdoOption1 = findViewById(R.id.rdoOption1)
        rdoOption2 = findViewById(R.id.rdoOption2)
        rdoOption3 = findViewById(R.id.rdoOption3)
        rdoOption4 = findViewById(R.id.rdoOption4)

        rdoGrpQuestionType.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.rdoTrueFalse) {
                setupTrueFalseView()
            } else {
                setupMultipleChoiceView()
            }
        }

        btnNext.setOnClickListener { saveAndNextQuestion() }
        btnPrevious.setOnClickListener { goToPreviousQuestion() }
        btnFinish.setOnClickListener { saveTrivia() }

        setupMultipleChoiceView()
        updateQuestionView()
    }

    /**
     * Save the current question and move to the next one
     */
    private fun saveAndNextQuestion() {
        if (saveCurrentQuestion()) {
            currentQuestionIndex++
            if (currentQuestionIndex >= questions.size) {
                questions.add(Question())
            }
            updateQuestionView()
        }
    }

    /**
     * Go to the previous question
     */
    private fun goToPreviousQuestion() {
        if (currentQuestionIndex > 0) {
            currentQuestionIndex--
            updateQuestionView()
        }
    }

    /**
     * Update the question view based on the current question index
     */
    private fun updateQuestionView() {
        val isFirstQuestion = currentQuestionIndex == 0
        btnPrevious.visibility = if (isFirstQuestion) View.INVISIBLE else View.VISIBLE

        val currentQuestion = if (currentQuestionIndex < questions.size) questions[currentQuestionIndex] else Question()
        questionNoTextView.text = getString(R.string.question_number, currentQuestionIndex + 1)

        editTxtQuestionTitle.setText(currentQuestion.title)
        if (currentQuestion.isMultipleChoice) {
            rdoMultipleChoice.isChecked = true
            setupMultipleChoiceView()
            updateMultipleChoiceOptions(currentQuestion.options)
        } else {
            rdoTrueFalse.isChecked = true
            setupTrueFalseView()
        }
    }

    /**
     * Save the current question and return true if successful
     */
    private fun saveCurrentQuestion(): Boolean {
        val title = editTxtQuestionTitle.text.toString().trim()
        if (title.isEmpty()) {
            Toast.makeText(this, "Question title cannot be empty.", Toast.LENGTH_SHORT).show()
            return false
        }

        val question = if (currentQuestionIndex < questions.size) questions[currentQuestionIndex] else Question()
        question.title = title
        question.isMultipleChoice = rdoMultipleChoice.isChecked

        if (question.isMultipleChoice) {
            val options = getMultipleChoiceOptions()
            if (options.size < 4 || options.any { it.isEmpty() }) {
                Toast.makeText(this, "All 4 options must be provided for multiple-choice questions.", Toast.LENGTH_SHORT).show()
                return false
            }
            question.options = options
            question.correctAnswer = options[getCorrectOptionIndex()]
        } else {
            question.correctAnswer = if (rdoOption1.isChecked) "True" else "False"
        }

        if (currentQuestionIndex >= questions.size) {
            questions.add(question)
        } else {
            questions[currentQuestionIndex] = question
        }

        return true
    }

    /**
     * Get the multiple-choice options from the EditText fields
     */
    private fun getMultipleChoiceOptions(): List<String> {
        return listOf(
            editTxtOption1.text.toString().trim(),
            editTxtOption2.text.toString().trim(),
            editTxtOption3.text.toString().trim(),
            editTxtOption4.text.toString().trim()
        )
    }

    /**
     * Get the correct option index based on the selected RadioButton
     */
    private fun getCorrectOptionIndex(): Int {
        return when {
            rdoOption1.isChecked -> 0
            rdoOption2.isChecked -> 1
            rdoOption3.isChecked -> 2
            rdoOption4.isChecked -> 3
            else -> 0
        }
    }

    /**
     * Set up the view for the True/False question type
     */
    private fun setupTrueFalseView() {
        editTxtOption1.visibility = View.GONE
        editTxtOption2.visibility = View.GONE
        editTxtOption3.visibility = View.GONE
        editTxtOption4.visibility = View.GONE

        rdoOption1.visibility = View.VISIBLE
        rdoOption2.visibility = View.VISIBLE
        rdoOption3.visibility = View.GONE
        rdoOption4.visibility = View.GONE
        rdoOption1.text = getString(R.string.trueOption)
        rdoOption2.text = getString(R.string.falseOption)
    }

    /**
     * Set up the view for the Multiple-Choice question type
     */
    private fun setupMultipleChoiceView() {
        linearOptionsContainer.visibility = View.VISIBLE
        rdoGrpAnswers.clearCheck()

        rdoOption1.text = ""
        rdoOption2.text = ""
        rdoOption3.text = ""
        rdoOption4.text = ""

        editTxtOption1.visibility = View.VISIBLE
        editTxtOption2.visibility = View.VISIBLE
        editTxtOption3.visibility = View.VISIBLE
        editTxtOption4.visibility = View.VISIBLE

        rdoOption1.visibility = View.VISIBLE
        rdoOption2.visibility = View.VISIBLE
        rdoOption3.visibility = View.VISIBLE
        rdoOption4.visibility = View.VISIBLE
    }

    /**
     * Update the multiple-choice options in the EditText fields
     */
    private fun updateMultipleChoiceOptions(options: List<String>) {
        editTxtOption1.setText(options.getOrNull(0) ?: "")
        editTxtOption2.setText(options.getOrNull(1) ?: "")
        editTxtOption3.setText(options.getOrNull(2) ?: "")
        editTxtOption4.setText(options.getOrNull(3) ?: "")
    }

    /**
     * Save the trivia to a JSON file
     */
    private fun saveTrivia() {
        if (!saveCurrentQuestion()) return

        val triviaName = editTxtTriviaName.text.toString().trim()
        if (triviaName.isEmpty()) {
            Toast.makeText(this, "Trivia name cannot be empty.", Toast.LENGTH_SHORT).show()
            return
        }

        /**
         * Create a JSON object representing the trivia
         */
        val triviaJson = JSONObject().apply {
            put("response_code", 0)
            put("results", JSONArray().apply {
                for (question in questions) {
                    put(JSONObject().apply {
                        put("type", if (question.isMultipleChoice) "multiple" else "boolean")
                        put("difficulty", "custom")
                        put("category", triviaName)
                        put("question", question.title)
                        put("correct_answer", question.correctAnswer)
                        put("incorrect_answers", JSONArray().apply {
                            if (question.isMultipleChoice) {
                                question.options.filter { it != question.correctAnswer }.forEach { put(it) }
                            }
                        })
                    })
                }
            })
        }

        /**
         * Save the JSON to a file
         */
        val triviaDir = File(getExternalFilesDir(null), "Trivia")
        if (!triviaDir.exists()) {
            triviaDir.mkdirs()
        }

        val file = File(triviaDir, "$triviaName.json")
        file.writeText(triviaJson.toString())
        Log.d("CreateTriviaActivity", "Trivia JSON saved to: ${file.absolutePath}")

        Toast.makeText(this, "Trivia saved to: ${file.absolutePath}", Toast.LENGTH_LONG).show()

        showShareDialog(file)
    }


    /**
     * Show a dialog to share the trivia
     */
    private fun showShareDialog(file: File) {
        val dialog = android.app.AlertDialog.Builder(this)
            .setTitle("Share Trivia")
            .setMessage("Trivia saved successfully! Would you like to share it with others?")
            .setPositiveButton("Yes") { _, _ ->
                shareTrivia(file)
                finishActivityWithToast()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
                finishActivityWithToast()
            }
            .create()

        dialog.show()
    }

    /**
     * Finish the activity and show a toast message
     */
    private fun finishActivityWithToast() {
        finish()
    }

    /**
     * Share the trivia file
     */
    private fun shareTrivia(file: File) {
        try {
            val fileUri = androidx.core.content.FileProvider.getUriForFile(
                this,
                "com.ewan.triviaapp.provider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_STREAM, fileUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivity(Intent.createChooser(shareIntent, "Share Trivia"))
        } catch (e: IllegalArgumentException) {
            Log.e("CreateTriviaActivity", "Failed to share trivia file: ${e.message}")
            Toast.makeText(this, "Unable to share the trivia file.", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Represents a question in the trivia
     */
    data class Question(
        var title: String = "",
        var isMultipleChoice: Boolean = true,
        var options: List<String> = listOf(),
        var correctAnswer: String = ""
    )
}
