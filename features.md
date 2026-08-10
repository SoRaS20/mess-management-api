# Grails REST API — MVP Backend Requirements

Build a simple **Grails REST API** for the Mess Management System. Authentication and role-based access will be added later.

### Core Features

* **Members**

  * Add, update, delete, list members
  * Member status

* **Monthly Management**

  * Create/open month
  * Set manager
  * Set member rent
  * Close month
  * View previous months

* **Meals**

  * Add/update meals
  * Meal ON/OFF
  * Admin meal correction
  * Monthly meal summary

* **Bazar**

  * Add/update/delete bazar
  * View bazar records

* **Expenses**

  * Add/update/delete expenses
  * View monthly expenses

* **Deposits**

  * Add deposits
  * View deposit history
  * Calculate balance

* **Rent**

  * Set monthly rent
  * View member rent

* **Dashboard**

  * Total meals
  * Meal rate
  * Total bazar
  * Total expenses
  * Total deposits
  * Member balances

* **Reports**

  * Monthly summary
  * Meal summary
  * Expense summary
  * Member balance

### Domain Classes

```text
User
Member
Month
Meal
Bazar
Expense
Deposit
Rent
```

### API Endpoints

```text
/api/members
/api/months
/api/meals
/api/bazar
/api/expenses
/api/deposits
/api/rents
/api/dashboard
/api/reports
```

### Excluded for Now

* Authentication
* Authorization / RBAC
* Notifications
* PDF/Excel export
* Advanced charts
* Multi-mess
* Backup/restore
* PWA/offline support

**Goal:** Keep the Grails backend small and REST-focused so authentication and authorization can be added later without complicating the MVP.
