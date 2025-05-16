# Coursework for CSC-306: Writing Mobile Apps
## Overview
**Trivia Time!** is an Android app targeting SDK 34, primarily tested on Android API 33 via. Pixel 3 emulator. It is a lightweight Trivia app, that allows users to answer Trivia questions and create their own to share with others.

<p align="center"><img src="https://github.com/user-attachments/assets/b7c40ae3-e925-49a6-aa65-ada3a1bb315b" height="500"/><img src="https://github.com/user-attachments/assets/21be3e49-874a-45d8-b335-475d65ec11c6" height="500"/><img src="https://github.com/user-attachments/assets/23679123-fd6f-40ee-9dc4-c6b23d4dc665" height="500"></p>


## Features of App
- **User registering/sign in**
	- [(Image Preview)](https://github.com/user-attachments/assets/3dcfdb95-958a-4142-8091-43b49fb02cb5)
	- Preferences save to the specific user
	- User details and preferences stored on device
- **Home**
  - [(Image Preview)](https://github.com/user-attachments/assets/ac3ca71f-095a-48b1-ba60-4b1362dba260)
  - Main navigation throughout the app
  - Live countdown until Trivia "resets"
- **Trivia**
  - [(Image Preview)](https://github.com/user-attachments/assets/4621d995-8267-42a7-a10e-982c0c86e302) [(Image Preview 2)](https://github.com/user-attachments/assets/016baf8f-a057-4b61-94a9-cdf5b24216ed)
	- Standard Trivia
		- Pulls from [OpenTDB](https://opentdb.com/) for trivia questions
		- Can choose:
			- Amount of questions 
			- Difficulty (easy, normal, hard) 
				- Different difficulties reward different amounts of 💎 Gems
			- Category. 
			- Type of question (multiple choice, true/false)
		 - Minimum 2 lives, then 2  for every 5 questions
		 - Unlockable "Hardcore" mode; 10 questions, hard mode, 1 life.
	 - Custom Trivia
		 - Users can create their own Trivia games, and can share them natively
		 - Users can import Trivia sent by other people
	 - All Trivia increases the user's 🔥 Streak by 1
	 - All Trivia gets added to the user's history
- **History**
	- [(Image Preview)](https://github.com/user-attachments/assets/ad706583-12fd-4980-a266-244552a4ae6a)
	- All games users play get added to the user's history
	- Tracks the total amount of games they play and their win rate (/100%)
	- Tracks the score they earned on the Trivia, the category, the difficulty, and the date/time of completion
	- Adds a grade determined by the app
		- S -> 90-100%
		 - A -> 80 - 89%
		 - B -> 70 - 79%
		 - C -> 60 - 69%
		 - D -> 0 - 59%
 - **Preferences**
   - [(Image Preview)](https://github.com/user-attachments/assets/8b6a80c4-1cb0-4086-99e6-b2107c5e2b39)
	 - Can toggle push notifications, and the time in which they send
	 - Can change the username of the account
	 - Can delete their account *(unimplemented)*
 - **Collection**
   - [(Image Preview)](https://github.com/user-attachments/assets/f848f5dd-25db-43b2-93f8-1b54e7b42937)
	 - The main view where unlockables are purchased with💎 Gems.
		 - Hardcore mode costs 💎 1000 Gems
		 - User avatars cost 💎 200 Gems
	 - 9 unlockable avatars
		 - [(Image Preview)](https://github.com/user-attachments/assets/58185e42-3f19-4273-8f35-44c5c5b44a21) [(Image Preview 2)](https://github.com/user-attachments/assets/eb3c44ea-9e77-45a5-8c5c-e58629da9e20)

## Libraries Used
- [Retrofit](https://github.com/square/retrofit) (v2.11.0) is a type-safe HTTP client for Android and JAVA.
- [KonfettiXML](https://github.com/DanielMartinus/Konfetti) (v2.1.0) is a Kotlin library that produces a confetti effect whenever the user answers a Trivia question correctly.
- [GSon](https://github.com/google/gson) (v2.11.0) to convert the HTTP request into a JSON representation.
