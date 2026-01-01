# FinChat RAG 🤖💰

A hybrid application combining a **Python-based RAG (Retrieval-Augmented Generation) backend** with a **Native Android (Kotlin) client**. This project is designed to provide intelligent financial insights by querying a vector database.

## 📂 Project Structure

This repository is a monorepo containing both the backend and mobile client:

- **Root (`/`)**: Python RAG Backend & Vector Database.
- **`FinChat/`**: Native Android Application (Kotlin).

## 🚀 Tech Stack

### Backend (Python)
- **Language:** Python 3.x
- **Vector Database:** ChromaDB (Persisted in `./chroma_db`)
- **Orchestration:** LangChain (Assumed based on RAG structure)
- **Entry Point:** `main.py`

### Mobile (Android)
- **Language:** Kotlin
- **Build System:** Gradle (Kotlin DSL)
- **Architecture:** MVVM / Clean Architecture (Recommended)
- **Networking:** Retrofit / Ktor (to communicate with the Python backend)

---

## 🛠️ Setup & Installation

### 1. Backend Setup (Python)

Ensure you have Python installed. It is recommended to use a virtual environment.

```bash
# 1. Create a virtual environment
python -m venv .venv

# 2. Activate the environment
# Windows:
.venv\Scripts\activate
# Mac/Linux:
source .venv/bin/activate

# 3. Install dependencies
pip install -r requirements.txt

# 4. Environment Variables
# Create a .env file in the root directory and add your API keys (e.g., OpenAI, etc.)
# OPENAI_API_KEY=your_key_here
