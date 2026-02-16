# AI SkillBridge 🚀

**AI SkillBridge** is a powerful AI-driven platform designed to bridge the gap between your current skills and your dream career. By analyzing your resume against specific job descriptions, it identifies high-priority skill gaps and generates tailored, time-boxed study plans to get you job-ready.

## ✨ Features

- **AI Gap Analysis**: Advanced extraction of skills from PDF/DOCX resumes and job descriptions using AI.
- **Priority-Based Learning**: Automatically identifies which skills are most critical for your target role.
- **Personalized Study Plans**: Generates custom preparation schedules based on the number of days you have available.
- **Premium Dark Interface**: Sleek, modern UI with glassmorphism and interactive elements.
- **Learning History**: Keep track of all your past analyses and study plans.

## 🛠️ Tech Stack

- **Backend**: Spring Boot 3.2.5, Java 17, Spring Data JPA, Spring AI.
- **Frontend**: HTML5, Vanilla CSS3 (Custom Glassmorphism Design), JavaScript (Async/Await), Thymeleaf.
- **AI Engine**: Gemini 2.0 Flash (via OpenRouter).
- **Utils**: Apache Tika (Document Extraction), FontAwesome (Icons).

## 🚀 Getting Started

### Prerequisites
- JDK 17 or higher
- Maven (or use the provided `./mvnw`)
- An OpenRouter API Key

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/dilipnalage1063/Skill-Gap-Analyzer-Study-Planner.git
   ```

2. Configure your API key in `src/main/resources/application.properties`:
   ```properties
   openrouter.api.key=your_api_key_here
   ```

3. Build and run:
   ```bash
   ./mvnw clean install
   java -jar target/studyplan-0.0.1-SNAPSHOT.jar
   ```

4. Open in browser: `http://localhost:8080`

## 📄 License
This project is for educational purposes. All rights reserved.
