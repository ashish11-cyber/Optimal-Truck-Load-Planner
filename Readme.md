
What we will evaluate
1. Correctness on 10 hidden test cases (including n=22 orders, hazmat isolation,
time-window conflicts, etc.)
2. Performance (< 800 ms on n=22 on our judge machine)
3. API design, validation, error handling
4. Code structure & readability
5. Dockerfile quality and README clarity
How to submit
1. Create a public GitHub repository and share the github link
2. Your repository must contain:
○ Complete source code
○ Dockerfile (multi-stage preferred)
○ docker-compose.yml that starts only your service (no DB needed)
○ README.md with exact instructions (see below)

README.md

# SmartLoad Optimization API
## How to run
```bash
git clone <your-repo>
cd <folder>
docker compose up --build
# → Service will be available at http://localhost:8080
