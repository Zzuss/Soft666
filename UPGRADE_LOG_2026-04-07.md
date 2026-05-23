# Upgrade Log (2026-04-07)

## Completed Feature Upgrades

1. TA profile completion and validation hardening
- Added required profile fields for TA workflows: `studentId`, `major`, `year`, `phone`.
- Strengthened validation and error handling in profile update flow to avoid HTTP 500 on input issues.
- Updated current rules:
  - Student ID: exactly 10 digits.
  - Year: exactly 4 digits (e.g., 2024).
  - Phone: normalized for full-width symbols/digits before validation.
- Kept `availableTime` auto-generated from schedule fields (read-only by design).

2. Application lifecycle enhancements
- Added rejection note support end-to-end (`rejectionNote`) with backward-compatible JSON handling.
- MO/Admin can optionally add rejection note when rejecting.
- TA can see rejection note in “My Applications”.
- Added max-length validation for rejection note.

3. Capacity and overload handling
- Enforced job slot limit during approve flow.
- Added overload warning flow for approvals over workload threshold and support for forced approval.
- Improved manage-applications UI with clearer warning text and current approved-load visibility.

4. Admin workload observability and export
- Added workload overload highlighting on admin page (`approved_assignments > 2`).
- Added CSV export endpoint:
  - `GET /admin/workload/export`
  - Columns: `ta_name,email,approved_assignments,is_overloaded,skills`

5. Authentication upgrade: self-service password reset
- Added username + email based password reset flow.
- New auth actions/routes:
  - `GET /auth?action=forgotPassword`
  - `POST /auth` with `action=resetPassword`
- Added forgot-password page and login-page entry point.

6. Job search and listing improvements
- Added/strengthened job filters and UX:
  - skill multi-select filtering
  - lightweight fuzzy keyword matching fallback
  - deadline-priority sorting (nearest first, empty deadline last)
  - filter state feedback + clear filters action
- Added job posted-time visibility for TA browsing:
  - timestamp captured automatically when MO creates job
  - displayed in job list with minute precision (`yyyy-MM-dd HH:mm`)

7. I18n coverage
- Added EN/ZH text keys for all new UI messages and labels introduced above.

