package com.ewan.triviaapp.activities

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Button
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ewan.triviaapp.R
import com.ewan.triviaapp.adapters.CategoryAdapter
import com.ewan.triviaapp.models.TriviaCategory
import com.ewan.triviaapp.models.TriviaConstants
import com.ewan.triviaapp.models.TriviaResponse
import com.ewan.triviaapp.models.UserProfile
import com.ewan.triviaapp.network.TriviaApiService
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class StartActivity : AppCompatActivity() {

    private var selectedCategory: TriviaCategory? = null
    private var isHardcoreUnlocked = false
    private val gson = Gson()
    private var username = "user"
    private lateinit var txtStreak: TextView
    private lateinit var txtGems: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_select)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        username = intent.getStringExtra("username") ?: return

        loadUserProfile(username)

        txtStreak = findViewById(R.id.txtStreak)
        txtGems = findViewById(R.id.txtCoins)

        val sharedPref = getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val streak = sharedPref.getInt("$username:currentStreak", 0)
        val gems = sharedPref.getInt("$username:currentGems", 0)

        txtStreak.text = getString(R.string.streakShowcase, streak)
        txtGems.text = getString(R.string.gemsShowcase, gems)

        findViewById<Button>(R.id.btnEasy).setOnClickListener { showQuizOptionsDialog("easy") }
        findViewById<Button>(R.id.btnNormal).setOnClickListener { showQuizOptionsDialog("medium") }
        findViewById<Button>(R.id.btnHard).setOnClickListener { showQuizOptionsDialog("hard") }
        findViewById<Button>(R.id.btnCreateCustom).setOnClickListener{
            val intent = Intent(this, CreateTriviaActivity::class.java)
            startActivity(intent)
        }
        findViewById<Button>(R.id.btnPlayCustom).setOnClickListener{
            openFilePicker()
        }

        isHardcoreUnlocked = sharedPref.getBoolean("$username:hardcoreUnlocked", false)
        val hardcoreButton = findViewById<Button>(R.id.btnHardcore)
        val hardcoreDescription = findViewById<TextView>(R.id.txtHardcoreDescription)

        hardcoreButton.isEnabled = isHardcoreUnlocked
        hardcoreButton.alpha = if (isHardcoreUnlocked) 1.0f else 0.5f
        hardcoreDescription.text = if (isHardcoreUnlocked) getString(R.string.hardcoreDesc) else getString(R.string.hardcoreLockedDesc)
        hardcoreButton.setOnClickListener {
            if (isHardcoreUnlocked) {
                showHardcoreDialog()
            }
        }
    }

    /**
     * Load the user profile
     */
    private fun loadUserProfile(username: String) {
        val sharedPref = getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val userProfileJson = sharedPref.getString("$username:profile", null)
        if (userProfileJson != null) {
            val userProfile = gson.fromJson(userProfileJson, UserProfile::class.java)
            isHardcoreUnlocked = userProfile.hardcoreUnlocked
        } else {
            Log.e("StartActivity", "User profile not found for username: $username")
        }
    }

    /**
     * Show the quiz options dialog
     */
    private fun showQuizOptionsDialog(difficulty: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_quiz_options, null)
        val rgQuestionType = dialogView.findViewById<RadioGroup>(R.id.rgQuestionType)
        val etNumberOfQuestions = dialogView.findViewById<android.widget.EditText>(R.id.etNumberOfQuestions)
        val rvCategories = dialogView.findViewById<RecyclerView>(R.id.rvCategories)
        val btnConfirm = dialogView.findViewById<Button>(R.id.btnConfirm)

        val categories = getCategories()

        val adapter = CategoryAdapter(categories) { category ->
            selectedCategory = category
            Log.d("CategorySelection", "Selected category: ${category.name}")
        }
        rvCategories.layoutManager = LinearLayoutManager(this)
        rvCategories.adapter = adapter

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        /**
         * Set the click listener for the confirm button
         */
        btnConfirm.setOnClickListener {
            val selectedQuestionType = when (rgQuestionType.checkedRadioButtonId) {
                R.id.rbTrueFalse -> "boolean"
                R.id.rbMultipleChoice -> "multiple"
                else -> "multiple"
            }
            val numberOfQuestions = etNumberOfQuestions.text.toString().toIntOrNull() ?: 10

            if (selectedCategory == null) {
                Log.e("QuizOptions", "No category selected")
                return@setOnClickListener
            }

            val categoryId = TriviaConstants.categoryIdMap[selectedCategory!!.name] ?: 9

            fetchQuestions(numberOfQuestions, categoryId, difficulty, selectedQuestionType)
            dialog.dismiss()
        }

        dialog.show()
    }

    /**
     * Show the hardcore dialog
     */
    private fun showHardcoreDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_hardcore_options, null)
        val rvCategories = dialogView.findViewById<RecyclerView>(R.id.rvCategories)
        val btnConfirm = dialogView.findViewById<Button>(R.id.btnConfirm)

        val categories = getCategories()

        val adapter = CategoryAdapter(categories) { category ->
            selectedCategory = category
            Log.d("CategorySelection", "Selected category: ${category.name}")
        }
        rvCategories.layoutManager = LinearLayoutManager(this)
        rvCategories.adapter = adapter

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        btnConfirm.setOnClickListener {
            if (selectedCategory == null) {
                Log.e("HardcoreOptions", "No category selected")
                return@setOnClickListener
            }

            val categoryId = TriviaConstants.categoryIdMap[selectedCategory!!.name] ?: 9
            fetchQuestions(10, categoryId, "hard", "multiple", hardcoreMode = true)
            dialog.dismiss()
        }

        dialog.show()
    }

    /**
     * Fetch the questions from the API
     */
    private fun fetchQuestions(
        amount: Int,
        category: Int,
        difficulty: String,
        type: String,
        hardcoreMode: Boolean = false
    ) {
        /**
         * Make the API call
         */
        TriviaApiService.api.getQuestions(amount, category, difficulty, type)
            .enqueue(object : Callback<TriviaResponse> {
                override fun onResponse(call: Call<TriviaResponse>, response: Response<TriviaResponse>) {
                    if (response.isSuccessful) {
                        val questions = response.body()?.results ?: emptyList()
                        if (questions.isNotEmpty()) {
                            val intent = Intent(this@StartActivity, TriviaActivity::class.java)
                            intent.putParcelableArrayListExtra("questions", ArrayList(questions))
                            intent.putExtra("difficulty", difficulty)
                            intent.putExtra("selectedCategoryName", category)
                            intent.putExtra("hardcoreMode", hardcoreMode) // Pass Hardcore flag
                            intent.putExtra("username", username)
                            startActivity(intent)
                        } else {
                            Log.e("TriviaAPI", "No questions retrieved.")
                        }
                    } else {
                        Log.e("TriviaAPI", "Error: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<TriviaResponse>, t: Throwable) {
                    Log.e("TriviaAPI", "Failed to fetch questions: ${t.message}")
                }
            })
    }

    /**
     * Get the list of categories
     */
    private fun getCategories(): List<TriviaCategory> {
        return listOf(
            TriviaCategory("General Knowledge", "\uD83C\uDF10"),
            TriviaCategory("Books", "\uD83D\uDCDA"),
            TriviaCategory("Film", "\uD83C\uDFAC"),
            TriviaCategory("Music", "\uD83C\uDFB5"),
            TriviaCategory("Musicals & Theatres", "\uD83C\uDFAD"),
            TriviaCategory("Television", "\uD83D\uDCFA"),
            TriviaCategory("Video Games", "\uD83C\uDFAE"),
            TriviaCategory("Board Games", "\uD83C\uDFB2"),
            TriviaCategory("Science & Nature", "\uD83D\uDD2C"),
            TriviaCategory("Computers", "\uD83D\uDCBB"),
            TriviaCategory("Mathematics", "\uD83D\uDCCA"),
            TriviaCategory("Mythology", "\uD83C\uDF07"),
            TriviaCategory("Sports", "\u26BD"),
            TriviaCategory("Geography", "\uD83C\uDF0D"),
            TriviaCategory("History", "\uD83D\uDD16"),
            TriviaCategory("Politics", "\uD83D\uDCDA"),
            TriviaCategory("Art", "\uD83C\uDFA8"),
            TriviaCategory("Celebrities", "\uD83C\uDF1F"),
            TriviaCategory("Animals", "\uD83D\uDC3E"),
            TriviaCategory("Vehicles", "\uD83D\uDE95"),
            TriviaCategory("Comics", "\uD83C\uDF08"),
            TriviaCategory("Gadgets", "\uD83D\uDD27"),
            TriviaCategory("Japanese Anime & Manga", "\uD83D\uDC09"),
            TriviaCategory("Cartoon & Animations", "\uD83C\uDF83")
        )
    }

    /**
     * Updates streak and gems when activity is resumed
     */
    override fun onResume() {
        super.onResume()
        updateStreakAndGems()
    }

    /**
     * Updates the streak and gems
     */
    private fun updateStreakAndGems() {
        val username = intent.getStringExtra("username") ?: return

        val sharedPref = getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val streak = sharedPref.getInt("$username:currentStreak", 0)
        val gems = sharedPref.getInt("$username:currentGems", 0)

        txtStreak.text = getString(R.string.streakShowcase, streak)
        txtGems.text = getString(R.string.gemsShowcase, gems)
    }

    /**
     * Open the file picker
     */
    private val filePickerLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                handleFileSelection(uri)
            } else {
                Toast.makeText(this, "No file selected.", Toast.LENGTH_SHORT).show()
            }
        }

    /**
     * Open the file picker
     */
    private fun openFilePicker() {
        filePickerLauncher.launch("application/json")
    }

    /**
     * Handle the file selection
     */
    private fun handleFileSelection(uri: Uri) {
        val fileName = getFileName(uri)
        if (fileName != null) {
            Toast.makeText(this, "Selected file: $fileName", Toast.LENGTH_SHORT).show()
        }

        // Pass the file URI to CustomTriviaActivity
        val intent = Intent(this, CustomTriviaActivity::class.java).apply {
            putExtra("fileUri", uri.toString())
            putExtra("username", username)
        }
        startActivity(intent)
    }

    /**
     * Get the file name from the URI
     */
    private fun getFileName(uri: Uri): String? {
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                return it.getString(it.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
            }
        }
        return null
    }


}
