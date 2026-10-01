# CineMatch

CineMatch is a clean, minimal movie recommendation website built with Java (Spring Boot) on the backend and pure HTML/CSS/JS on the frontend. It uses a dataset of 4,800 movies and recommends films based on genre matching and popularity.

## Features

- **Movie Search**: Instantly search for any movie in the dataset.
- **Top Movies**: See a curated list of the most popular movies on the homepage.
- **Recommendations**: Click on any movie to see details and get a ranked list of similar movies.
- **Real Posters**: Automatically fetches high-quality movie posters from the web.
- **Dynamic Background**: Features a beautiful, slow-moving pastel wave background.

## How It Works (The Logic)

Even though it looks simple, there is some smart logic happening behind the scenes to find the best movies for you:

1. **Searching (KMP Algorithm)**: When you type in the search bar, the system uses a fast text-matching technique to instantly find movie titles without slowing down, even with thousands of movies.
2. **Finding Similarities (Bitmasks)**: Every movie's genres (like Action, Comedy, Drama) are converted into a unique mathematical "fingerprint". This allows the system to instantly compare two movies and see how much their genres overlap.
3. **Ranking (Max-Flow)**: Once it finds movies with similar genres, it uses a network-flow algorithm to figure out the "strongest" connections and ranks the best recommendations at the top.
4. **Final Scoring**: It combines the genre similarity, the movie's rating, and how popular it is to give you a final list of movies you'll actually want to watch.

## How to Run (Step-by-Step)

If you have downloaded this project as a ZIP file from GitHub, follow these exact steps to get it running on your machine:

**Prerequisites:** You must have Java installed on your computer (Java 17 or higher).

1. **Extract the ZIP:** Extract the downloaded project folder and open it.
2. **Open Terminal / Command Prompt:**
   - **Mac/Linux:** Open the `Terminal` app and use the `cd` command to navigate to the extracted folder (e.g., `cd Downloads/cinematch-main`).
   - **Windows:** Open `Command Prompt` or `PowerShell` and navigate to the extracted folder (e.g., `cd Downloads\cinematch-main`).
3. **Run the Server:** Type the following command and press Enter:
   - On **Mac/Linux**: `./mvnw spring-boot:run`
   - On **Windows**: `mvnw.cmd spring-boot:run`
4. **Wait for Startup:** It will download a few dependencies the first time and then say `Started CinematchApplication`.
5. **Open the Website:** Open your web browser (Chrome/Safari/Edge) and go to: [http://localhost:8080](http://localhost:8080)

*Note: The dataset is already included in the `datasets/` folder, so you don't need to configure any paths or databases!*

## Tech Stack

- **Backend**: Java, Spring Boot
- **Frontend**: HTML5, CSS3, Vanilla JavaScript, WebGL (for the animated background)
- **Data**: TMDB 5000 Movies Dataset
