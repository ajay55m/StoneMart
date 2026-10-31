# Project Guidelines & Rules

## 1. Database Operations & Data Mutation Policy
- **Mandatory Notification**: Whenever executing any data insertion, update, or deletion in any database—whether via direct SQL queries, backend scripts, CLI `curl` commands, REST API calls (`POST`/`PUT`/`DELETE`), or test data seeding—you **MUST ALWAYS inform the user**.
- **Details Required**:
  - Exact target endpoint or database table.
  - Data payload / records being inserted or modified (e.g., test IDs, dates, parameters).
  - Explicit notification before or immediately upon executing the request.
- **No Silent Mutations**: Never silently insert dummy or test records into the backend or database without the user's prior knowledge or explicit permission.
