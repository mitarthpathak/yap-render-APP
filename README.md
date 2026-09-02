# Yap & Render — Mobile App

**You Speak. We Sign.**

The Android companion app for **Yap & Render** — an AI-powered platform that converts spoken audio into **Indian Sign Language (ISL)** in real time through an animated sign interpreter. Built as part of our **Smart India Hackathon (SIH) 2026** submission.

> 🌐 Web version: [yap-render](https://github.com/mitarthpathak/yap-render) — live at [yap-render.vercel.app](https://yap-render.vercel.app)

---

## 📖 About

Millions of Deaf and hard-of-hearing individuals in India rely on Indian Sign Language, but most digital content and everyday conversations remain audio/text-first. **Yap & Render** bridges that gap by turning spoken language into an animated ISL interpretation on the fly — no interpreter required.

This repository holds the **native Android app**, which brings the same speech → sign pipeline to mobile, so translation can happen anywhere, without needing a browser tab open.

## ⚙️ How It Works

```
🎙️ Speech Input → 📝 Speech-to-Text → 🧠 AI Context Understanding → 🤟 ISL Gloss Translation → 🎬 Animated Sign Interpreter
```

1. **Speech Input** — captures live audio from the device mic
2. **Speech-to-Text** — transcribes spoken audio
3. **AI Context Understanding** — powered by the **Gemini API**, converts natural language into ISL grammar/gloss
4. **Sign Rendering** — plays back the corresponding animated signs to the user

## ✨ Features

- 🎙️ Real-time speech capture and transcription
- 🧠 AI-driven context understanding for accurate ISL gloss conversion
- 🤟 Animated sign interpreter output
- 📱 Native Android experience — works independent of the web app

## 🛠️ Tech Stack

- **Language:** Kotlin
- **Platform:** Native Android
- **Build System:** Gradle (Kotlin DSL)
- **AI/Context Layer:** Google Gemini API
- Scaffolded via [Google AI Studio](https://ai.studio)

## 🚀 Getting Started

### Prerequisites

- [Android Studio](https://developer.android.com/studio) (latest stable)
- A [Gemini API key](https://ai.google.dev/)

### Setup

1. Clone the repo
   ```bash
   git clone https://github.com/mitarthpathak/yap-render-APP.git
   ```
2. Open the project folder in **Android Studio** and let it sync/resolve dependencies.
3. Create a `.env` file in the project root and add your Gemini key (see `.env.example`):
   ```
   GEMINI_API_KEY=your_api_key_here
   ```
4. Run the app on an emulator or a physical device.

## 🏆 Built For

**Smart India Hackathon (SIH) 2026** — Team Yap & Render

## 📄 License

TBD
