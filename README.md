# AI SkillBridge 🚀

**AI SkillBridge** is a powerful AI-driven platform designed to bridge the gap between your current skills and your dream career. By analyzing your resume against specific job descriptions, it identifies high-priority skill gaps and generates tailored, time-boxed study plans to get you job-ready.

**🔗 Live Demo**: [https://ai-skill-bridge-production.up.railway.app](https://ai-skill-bridge-production.up.railway.app)

## ✨ Features

- **AI Gap Analysis**: Advanced extraction of skills from PDF/DOCX resumes and job descriptions using AI.
- **Priority-Based Learning**: Automatically identifies which skills are most critical for your target role.
- **Personalized Study Plans**: Generates custom preparation schedules based on the number of days you have available.
- **Premium Dark Interface**: Sleek, modern UI with glassmorphism and interactive elements.
- **Learning History**: Keep track of all your past analyses and study plans.

## 🛠️ Tech Stack

- **Backend**: Spring Boot 3.2.5, Java 17, Spring Data JPA, Spring AI, MySQL.
- **Frontend**: HTML5, Vanilla CSS3 (Custom Glassmorphism Design), JavaScript (Async/Await), Thymeleaf.
- **AI Engine**: Gemini 2.0 Flash (via OpenRouter).
- **Utils**: Apache Tika (Document Extraction), FontAwesome (Icons).

## 🚀 Getting Started

### Prerequisites
- JDK 17 or higher
- Maven (or use the provided `./mvnw`)
- An OpenRouter API Key

### Installation

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/dilipnalage1063/AI-Skill-Bridge.git
   ```

2. Set your **Environment Variables**:
   For local development, you can set these in your shell or IDE:
   - `OPENROUTER_API_KEY`: Your OpenRouter API key.
   - `SPRING_DATASOURCE_URL`: (Optional) `jdbc:h2:mem:studydb` if using H2, or your production DB URL.

3. Build and run:
   ```bash
   ./mvnw clean install
   java -jar target/ai-skill-bridge-0.0.1-SNAPSHOT.jar
   ```

4. Open in browser: `http://localhost:8080`

## 🌐 Deployment (Railway / Render / Aiven)

To keep the project live and secure, set the following **Environment Variables** in your hosting dashboard:

| Variable | Description | Example |
| :--- | :--- | :--- |
| `OPENROUTER_API_KEY` | **Required** for AI features | `sk-or-v1-...` |
| `SPRING_DATASOURCE_URL` | Persistent DB URL | `jdbc:postgresql://...` |
| `SPRING_DATASOURCE_USERNAME` | DB Username | `postgres` |
| `SPRING_DATASOURCE_PASSWORD` | DB Password | `your_password` |
| `SPRING_JPA_DATABASE_PLATFORM` | Hibernate Dialect | `org.hibernate.dialect.MySQLDialect` |

> [!TIP]
> Always use environment variables for secrets. Never hardcode API keys in `application.properties`.

## 📄 License
This project is for educational purposes. All rights reserved.
