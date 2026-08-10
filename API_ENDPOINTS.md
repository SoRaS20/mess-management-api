# Mess Management API — Endpoint Reference

> **Base URL:** `http://localhost:8080/api`
> **Content-Type:** `application/json`

---

## Members

### List all members
```
GET /api/members
```
| Param | Type | Source | Description |
|-------|------|--------|-------------|
| `max` | int | query | Page size (default 10) |
| `offset` | int | query | Skip N rows |

**Response 200:**
```json
[
  { "id": 1, "name": "Rahman", "phone": "+8801712345678", "joinDate": "2026-01-01", "active": true, "userId": 2 }
]
```

---

### Get member by ID
```
GET /api/members/{id}
```

---

### Create member
```
POST /api/members
```
| Field | Type | Required | Notes |
|-------|------|----------|-------|
| `name` | string | yes | 2–100 chars |
| `phone` | string | no | 6–20 chars, digits/+/−/space |
| `joinDate` | string | yes | `yyyy-MM-dd` |
| `active` | boolean | no | default `true` |
| `user.id` | long | no | link to User |

```json
{ "name": "Karim", "phone": "+8801812345678", "joinDate": "2026-01-01" }
```

---

### Update member
```
PUT /api/members/{id}
```
Same fields as create (any subset).

---

### Delete member
```
DELETE /api/members/{id}
```
**Response:** `204 No Content`

---

### Toggle active status
```
PUT /api/members/{id}/toggleActive
```
No body required. Flips `active` between `true` ↔ `false`.

```json
{ "id": 3, "name": "Faruk", "active": false, ... }
```

---

## Months

### List all months
```
GET /api/months
```

**Response 200:**
```json
[
  { "id": 1, "year": 2026, "monthNo": 8, "closed": false, "managerId": 1, "managerName": "Rahman" }
]
```

---

### Get month by ID
```
GET /api/months/{id}
```

---

### Create month
```
POST /api/months
```
| Field | Type | Required | Notes |
|-------|------|----------|-------|
| `year` | int | yes | 2000–2100 |
| `monthNo` | int | yes | 1–12 |

```json
{ "year": 2026, "monthNo": 8 }
```
> Duplicate (year, monthNo) returns existing month, not an error.

---

### Update month
```
PUT /api/months/{id}
```

---

### Delete month
```
DELETE /api/months/{id}
```

---

### Close month
```
PUT /api/months/{id}/close
```
No body. Sets `closed: true`. After closing, all writes to child records (meals, bazar, expenses, deposits, rent) for this month return **422** with `month.closed` error.

---

### Reopen month
```
PUT /api/months/{id}/reopen
```
No body. Sets `closed: false`.

---

### Set manager
```
PUT /api/months/{id}/manager/{memberId}
```
No body. Assigns the member as this month's manager.

---

## Meals

### Generate default meals (bulk create)
```
POST /api/meals/generate/{monthId}
```
No body. Creates one all-ON meal row for every **active** member × every day of the month. Idempotent — skips existing `(member, recordDate)` pairs.

**Response 201:**
```json
{ "created": 124 }
```

---

### List meals for a month
```
GET /api/meals/byMonth/{monthId}
```

**Response 200:**
```json
[
  {
    "id": 1, "memberId": 1, "memberName": "Rahman", "monthId": 1,
    "recordDate": "2026-08-01", "breakfastOn": true, "lunchOn": true,
    "dinnerOn": true, "dailyCount": 2.5
  }
]
```

---

### Get meals for a specific date
```
GET /api/meals/byDate/{monthId}/{date}
```
| Param | Type | Format |
|-------|------|--------|
| `date` | string | `yyyy-MM-dd` |

Example: `GET /api/meals/byDate/1/2026-08-10`

---

### Toggle a meal slot (on/off)
```
PUT /api/meals/{id}/toggle
```
| Field | Type | Required | Notes |
|-------|------|----------|-------|
| `slot` | string | yes | `"breakfast"`, `"lunch"`, or `"dinner"` |
| `on` | boolean | yes | `true` = ON, `false` = OFF |

```json
{ "slot": "dinner", "on": false }
```

---

### Get single meal
```
GET /api/meals/{id}
```

---

### Admin-correct meal flags (via standard update)
```
PUT /api/meals/{id}
```
| Field | Type | Notes |
|-------|------|-------|
| `breakfastOn` | boolean | optional |
| `lunchOn` | boolean | optional |
| `dinnerOn` | boolean | optional |

```json
{ "breakfastOn": false, "lunchOn": true, "dinnerOn": true }
```

---

### Delete meal
```
DELETE /api/meals/{id}
```

---

## Bazar

### List bazar for a month
```
GET /api/bazar/byMonth/{monthId}
```

**Response 200:**
```json
[
  {
    "id": 1, "memberId": 1, "memberName": "Rahman", "monthId": 1,
    "amount": 2200.00, "description": "Rice, oil, spices", "bazarDate": "2026-08-01"
  }
]
```

---

### Create bazar
```
POST /api/bazar
```
| Field | Type | Required | Notes |
|-------|------|----------|-------|
| `member.id` | long | yes | who shopped |
| `month.id` | long | yes | billing month |
| `amount` | decimal | yes | ≥ 0, scale 2 |
| `description` | string | no | max 255 |
| `bazarDate` | string | yes | `yyyy-MM-dd` |

```json
{ "member": { "id": 1 }, "month": { "id": 1 }, "amount": 2200, "description": "Rice, oil", "bazarDate": "2026-08-01" }
```

---

### Update bazar
```
PUT /api/bazar/{id}
```

---

### Delete bazar
```
DELETE /api/bazar/{id}
```

---

## Expenses

### List expenses for a month
```
GET /api/expenses/byMonth/{monthId}
```

**Response 200:**
```json
[
  {
    "id": 1, "monthId": 1, "amount": 2500.00, "description": "Gas bill",
    "category": "gas", "expenseDate": "2026-08-05", "paidById": 1, "paidByName": "Rahman"
  }
]
```

---

### Create expense
```
POST /api/expenses
```
| Field | Type | Required | Notes |
|-------|------|----------|-------|
| `month.id` | long | yes | billing month |
| `amount` | decimal | yes | ≥ 0, scale 2 |
| `description` | string | no | max 255 |
| `category` | string | yes | `gas` / `electricity` / `water` / `internet` / `other` |
| `expenseDate` | string | yes | `yyyy-MM-dd` |
| `paidBy.id` | long | no | who paid |

```json
{ "month": { "id": 1 }, "amount": 2500, "description": "Gas bill", "category": "gas", "expenseDate": "2026-08-05", "paidBy": { "id": 1 } }
```

---

### Update / Delete expense
```
PUT /api/expenses/{id}
DELETE /api/expenses/{id}
```

---

## Deposits

### List deposits for a month
```
GET /api/deposits/byMonth/{monthId}
```

**Response 200:**
```json
[
  {
    "id": 1, "memberId": 1, "memberName": "Rahman", "monthId": 1,
    "amount": 3000.00, "depositDate": "2026-08-01", "description": "August deposit"
  }
]
```

---

### Create deposit
```
POST /api/deposits
```
| Field | Type | Required | Notes |
|-------|------|----------|-------|
| `member.id` | long | yes | who deposited |
| `month.id` | long | yes | billing month |
| `amount` | decimal | yes | ≥ 0, scale 2 |
| `depositDate` | string | yes | `yyyy-MM-dd` |
| `description` | string | no | max 255 |

```json
{ "member": { "id": 1 }, "month": { "id": 1 }, "amount": 3000, "depositDate": "2026-08-01", "description": "August deposit" }
```

---

### Update / Delete deposit
```
PUT /api/deposits/{id}
DELETE /api/deposits/{id}
```

---

## Rents

### List rents for a month
```
GET /api/rents/byMonth/{monthId}
```

**Response 200:**
```json
[
  { "id": 1, "memberId": 1, "memberName": "Rahman", "monthId": 1, "amount": 1500.00 }
]
```

---

### Create / Update rent (upsert)
```
POST /api/rents          (create — unique on member+month)
PUT /api/rents/{id}      (update)
```
| Field | Type | Required | Notes |
|-------|------|----------|-------|
| `member.id` | long | yes | |
| `month.id` | long | yes | |
| `amount` | decimal | yes | ≥ 0, scale 2 |

```json
{ "member": { "id": 1 }, "month": { "id": 1 }, "amount": 1500 }
```

---

### Delete rent
```
DELETE /api/rents/{id}
```

---

## Dashboard

### Monthly summary
```
GET /api/dashboard/{monthId}
```

**Response 200:**
```json
{
  "monthId": 9,
  "year": 2026,
  "monthNo": 8,
  "closed": false,
  "memberCount": 4,
  "totalMeals": 307.5,
  "totalBazar": 6500.00,
  "mealRate": 21.14,
  "totalExpenses": 5000.00,
  "totalDeposits": 12000.00,
  "expenseSharePerMember": 1250.00
}
```
> `mealRate = totalBazar ÷ totalMeals` (guarded for zero)
> `expenseSharePerMember = totalExpenses ÷ active memberCount` (guarded for zero)

---

## Reports

### Monthly report (full breakdown)
```
GET /api/reports/monthly/{monthId}
```

**Response 200:**
```json
{
  "month": { "id": 1, "year": 2026, "monthNo": 8, "closed": false },
  "summary": {
    "totalMeals": 286.5, "totalBazar": 6500.00, "mealRate": 22.69,
    "totalExpenses": 5000.00, "totalDeposits": 12000.00,
    "memberCount": 4, "expenseSharePerMember": 1250.00
  },
  "members": [
    {
      "memberId": 1, "memberName": "Rahman",
      "meals": 72.0, "mealRate": 22.69, "mealCost": 1633.68,
      "expenseShare": 1250.00, "rent": 1500.00, "deposit": 3000.00, "balance": -1383.68
    }
  ],
  "totals": {
    "deposits": 12000.00, "mealCost": 6500.00,
    "expenses": 5000.00, "rent": 6000.00, "netBalance": -5500.00
  }
}
```

---

### Daily report
```
GET /api/reports/daily/{monthId}/{date}
```
| Param | Type | Format |
|-------|------|--------|
| `date` | string | `yyyy-MM-dd` |

**Response 200:**
```json
{
  "date": "2026-08-10",
  "monthId": 1,
  "members": [
    { "memberId": 1, "memberName": "Rahman", "breakfastOn": true, "lunchOn": true, "dinnerOn": false, "dailyCount": 1.5 }
  ],
  "dayTotals": { "totalMeals": 8.0, "bazarThatDay": 1500.00, "expensesThatDay": 0 }
}
```

---

### Per-member report
```
GET /api/reports/member/{memberId}/{monthId}
```

**Response 200:**
```json
{
  "member": { "id": 1, "name": "Rahman" },
  "month": { "id": 1, "year": 2026, "monthNo": 8 },
  "meals": {
    "totalCount": 72.0,
    "byDay": [
      { "date": "2026-08-01", "dailyCount": 2.5 },
      { "date": "2026-08-02", "dailyCount": 2.0 }
    ]
  },
  "deposits": [
    { "date": "2026-08-01", "amount": 3000.00 }
  ],
  "rent": 1500.00,
  "mealRate": 22.69,
  "mealCost": 1633.68,
  "expenseShare": 1250.00,
  "totalDeposit": 3000.00,
  "balance": -1383.68
}
```

---

## Error Responses

All validation errors return **422 Unprocessable Entity** using the [vnd.error](https://github.com/blongden/vnd.error) format:

```json
{
  "message": "Property [name] of class [mess.management.Member] cannot be blank",
  "path": "/api/members",
  "_links": { "self": { "href": "http://localhost:8080/api/members" } }
}
```

**Closed-month write attempt:**
```json
{ "message": "Cannot modify records for a closed month." }
```

---

## Recommended Testing Order

1. `POST /api/months` — create August 2026
2. `POST /api/members` — add a few members
3. `PUT /api/months/{id}/manager/{memberId}` — assign manager
4. `POST /api/meals/generate/{monthId}` — bulk create meals
5. `PUT /api/meals/{id}/toggle` — toggle a few OFF
6. `POST /api/bazar` — add bazar entries
7. `POST /api/expenses` — add expenses
8. `POST /api/deposits` — add deposits
9. `POST /api/rents` — set rent per member
10. `GET /api/dashboard/{monthId}` — verify mealRate
11. `GET /api/reports/monthly/{monthId}` — full report
12. `GET /api/reports/member/{memberId}/{monthId}` — per-member breakdown
13. `GET /api/reports/daily/{monthId}/{date}` — daily view
14. `PUT /api/months/{id}/close` — close month
15. `POST /api/bazar` → **422** — confirm closed-month guard works

---

## Dev Seed Data

When running in development mode (`bootRun`), the app auto-seeds:
- **1 admin** (`admin` / `admin123`)
- **4 members** (Rahman, Karim, Salim, Faruk)
- **1 open month** (2026/8, manager = Rahman)
- Rent 1500 each, several bazar/expense/deposit rows
- All meals generated, a few toggled OFF

You can skip steps 1–9 and go straight to dashboard/reports.
