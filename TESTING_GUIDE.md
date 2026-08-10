# How to Test the Mess Management API

A step-by-step guide to run the app and exercise every endpoint — using **Postman**, **your browser**, or **curl**. No authentication is required (deferred for the MVP).

---

## Part 1 — Start the app

### 1. Open a terminal in the project folder
```
d:\Projects\mess-management
```

### 2. Run the app
```
gradlew.bat bootRun
```
Wait until you see:
```
Grails application running at http://localhost:8080 in environment: development
```
The app connects to **Neon PostgreSQL** and auto-seeds demo data on first boot (see *Seed data* below). Leave this terminal running — the app stays live until you press **Ctrl+C**.

> **Tip:** To wipe and re-seed a clean database, stop the app, set `dbCreate: create` under `environments.development.dataSource` in `grails-app/conf/application.yml`, start once, then set it back to `update`. In `update` mode your test data survives restarts.

### 3. Quick smoke test (browser)
Open this in any browser:
```
http://localhost:8080/api/members
```
You should see a JSON array of 4 members. If so, the API is live. ✅

---

## Part 2 — Testing with Postman (recommended)

### Import the ready-made collection
1. Open Postman → **Import** (top-left).
2. Select the file **`postman_collection.json`** from `d:\Projects\mess-management`.
3. A collection named **“Mess Management API”** appears with every endpoint grouped by resource.

### Set the variables (once)
The collection uses variables so you never retype IDs. Click the collection name → **Variables** tab:

| Variable | Default | Meaning |
|----------|---------|---------|
| `baseUrl` | `http://localhost:8080/api` | API root |
| `monthId` | `9` | The seeded August 2026 month |
| `memberId` | `5` | Rahman (first seeded member) |
| `mealId` | `96` | A sample meal row |
| `date` | `2026-08-05` | A date with meal data |

> These match the seed data. If you create your own month/members, update the variables to the new IDs (every create response includes the new `id`).

### Run requests
Expand a folder (e.g. **Dashboard & Reports**) → click a request → **Send**. Start with **Dashboard summary** — it returns computed totals immediately from seed data.

---

## Part 3 — The IDs you'll need (from seed data)

Because Users, Members, and Months share one ID sequence, the seeded IDs are:

| Entity | Name | ID |
|--------|------|-----|
| Month | August 2026 | **9** |
| Member | Rahman (manager) | **5** |
| Member | Karim | 6 |
| Member | Salim | 7 |
| Member | Faruk | 8 |

> Always confirm with `GET /api/members` and `GET /api/months` — IDs can differ if the DB was re-seeded.

---

## Part 4 — Guided walkthrough (copy-paste curl)

Run these in order. Each builds on the seed data. On Windows use **Git Bash** or **PowerShell** (curl works in both; PowerShell may need `curl.exe`).

### A. Read the seeded state
```bash
# List members (note the IDs)
curl http://localhost:8080/api/members

# List months (grab the month id, e.g. 9)
curl http://localhost:8080/api/months

# Dashboard — the headline numbers
curl http://localhost:8080/api/dashboard/9
```
**Expect** the dashboard to show:
```json
{ "totalMeals": 307.5, "totalBazar": 6500.00, "mealRate": 21.14,
  "totalExpenses": 5000.00, "totalDeposits": 12000.00, "expenseSharePerMember": 1250.00, ... }
```
`mealRate = totalBazar ÷ totalMeals = 6500 ÷ 307.5 ≈ 21.14`. ✅

### B. Full reports
```bash
# Monthly report — per-member balances + rolled-up totals
curl http://localhost:8080/api/reports/monthly/9

# Per-member breakdown (member 5, month 9)
curl http://localhost:8080/api/reports/member/5/9

# Daily report for one date
curl http://localhost:8080/api/reports/daily/9/2026-08-05
```

### C. Create a member
```bash
curl -X POST http://localhost:8080/api/members \
  -H "Content-Type: application/json" \
  -d '{"name":"Jamil","phone":"+8801511112222","joinDate":"2026-08-01"}'
```
**Expect** `201 Created` and the new member with its `id`.

### D. Toggle a meal slot OFF
```bash
# Find a meal id for a date
curl "http://localhost:8080/api/meals/byDate/9/2026-08-05"

# Turn dinner OFF for that meal (use an id from above)
curl -X PUT http://localhost:8080/api/meals/26/toggle \
  -H "Content-Type: application/json" \
  -d '{"slot":"dinner","on":false}'
```
**Expect** the returned meal to show `"dinnerOn": false` and a lower `dailyCount` (breakfast 0.5 + lunch 1 + dinner 1 = 2.5 full; dinner off → 1.5).

### E. Add financial records
```bash
# Bazar (grocery shopping) entry
curl -X POST http://localhost:8080/api/bazar \
  -H "Content-Type: application/json" \
  -d '{"member":{"id":5},"month":{"id":9},"amount":1750,"description":"Fish","bazarDate":"2026-08-20"}'

# Expense (shared bill)
curl -X POST http://localhost:8080/api/expenses \
  -H "Content-Type: application/json" \
  -d '{"month":{"id":9},"amount":900,"category":"electricity","expenseDate":"2026-08-18","paidBy":{"id":5}}'

# Deposit (member pays money in)
curl -X POST http://localhost:8080/api/deposits \
  -H "Content-Type: application/json" \
  -d '{"member":{"id":5},"month":{"id":9},"amount":2000,"depositDate":"2026-08-15"}'
```
Re-run `GET /api/dashboard/9` — the totals now reflect your new rows. ✅

### F. Prove the closed-month guard
```bash
# Close the month
curl -X PUT http://localhost:8080/api/months/9/close

# Try to write to it — should be REJECTED with 422
curl -i -X POST http://localhost:8080/api/bazar \
  -H "Content-Type: application/json" \
  -d '{"member":{"id":5},"month":{"id":9},"amount":500,"bazarDate":"2026-08-25"}'

# Reopen so you can keep testing
curl -X PUT http://localhost:8080/api/months/9/reopen
```
**Expect** the middle call to return:
```
HTTP/1.1 422
{ "message": "Cannot modify records for a closed month." }
```
This confirms the business rule works. ✅

---

## Part 5 — HTTP status codes you'll see

| Code | When |
|------|------|
| `200 OK` | Successful GET / PUT / custom action |
| `201 Created` | Successful POST (new record) |
| `204 No Content` | Successful DELETE |
| `404 Not Found` | Wrong ID, or wrong URL (see note below) |
| `422 Unprocessable Entity` | Validation failed, or closed-month write |
| `500 Internal Server Error` | Unexpected server bug — check the app terminal |

> **Common 404 gotcha:** resource URLs are **plural** — `/api/members`, `/api/months`, `/api/meals`. A singular URL like `/api/member` will not work the way you expect.

---

## Part 6 — Testing methods compared

| Method | Best for | Notes |
|--------|----------|-------|
| **Postman** | Interactive testing, saving requests, editing JSON bodies | Import `postman_collection.json` — everything is pre-built |
| **Browser** | Quick GET checks only | Can't do POST/PUT/DELETE; install a JSON viewer extension for readability |
| **curl** | Scripting, copy-paste from this guide | Works in Git Bash & PowerShell (`curl.exe` on PS) |
| **Swagger UI** | *(not installed)* | Not wired up yet — ask if you'd like `springdoc-openapi` added for an interactive `/swagger-ui` page |

---

## Seed data (auto-loaded in development)

| What | Details |
|------|---------|
| Admin user | `admin` / `admin123` |
| Members (4) | Rahman, Karim, Salim, Faruk |
| Month | August 2026 (id 9), open, manager = Rahman |
| Rent | 1500 each |
| Bazar | 4 entries, total 6500 |
| Expenses | 4 entries, total 5000 |
| Deposits | 4 entries, total 12000 |
| Meals | All members × every day of August, a few toggled OFF |

You can jump straight to dashboards/reports without creating anything.

---

## Full endpoint list

For the complete parameter reference of every endpoint (all fields, types, and example bodies), see **`API_ENDPOINTS.md`** in the project root.
