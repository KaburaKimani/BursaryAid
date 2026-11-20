# Admin review module (Member 4)

Lets an administrator review submitted bursary applications, check eligibility, and then approve (with an award), reject, or ask the student for more information.

## Status flow

SUBMITTED -> UNDER_REVIEW (admin taps "Start review") -> APPROVED or REJECTED
UNDER_REVIEW -> NEEDS_INFORMATION -> student resubmits as SUBMITTED

Decisions are final. DRAFT applications never appear for the administrator.

## Eligibility rules (logic/EligibilityRules.kt)

Explicit criteria, no scoring. Every check must pass:

- Status is SUBMITTED or UNDER_REVIEW.
- Submitted on or before the bursary deadline (submission time is compared, not the "open" flag).
- Year of study is within AppConstants.MIN_YEAR_OF_STUDY..MAX_YEAR_OF_STUDY.
- Household income is at or below the bursary's income limit. Skipped when the bursary has no limit; missing income fails when a limit exists.
- Requested amount is greater than 0.
- The application has no approved allocation already.

## Allocation rules (logic/AllocationRules.kt)

- Application must be UNDER_REVIEW and eligible, with no approved allocation yet.
- Award is a whole number of shillings greater than 0.
- Award is at most the bursary's maximum award, the remaining budget, and the requested amount.
- Rejection and "request information" need a reason of at least 10 characters. An approval reason is optional and defaults to "Approved".

Remaining budget = total budget minus the sum of APPROVED awards (never below 0). It is always calculated, never stored.

## Layers

| Layer | Files | Responsibility |
|---|---|---|
| UI | ui/AdminApplicationsScreen.kt, ui/AdminReviewScreen.kt, ui/AdminUiComponents.kt | Draw the state, report taps. No business rules. |
| ViewModel | viewmodel/AdminApplicationsViewModel.kt, viewmodel/AdminReviewViewModel.kt, viewmodel/AdminViewModelFactories.kt | Hold screen state, validate forms, call the repository. |
| Rules | logic/*.kt | Plain Kotlin on whole-shilling Long values, unit tested without a screen or database. |
| Repository | data/AdminReviewRepository.kt (interface), data/RoomAdminReviewRepository.kt (adapter) | The ViewModels depend on the interface. The adapter is the only class that knows Member 3's entity names. |
| Data source | Member 3's Room repositories | Store and read the records. |

## Approving an application (data flow)

1. The admin types an amount; the ViewModel checks the form with DecisionValidator.
2. The ViewModel calls approveApplication on the repository interface.
3. The adapter opens one database transaction, reloads fresh data, re-checks every rule, saves the AllocationRecord, changes the application status, and updates the stored bursary budget copy. If any step fails, nothing is saved.
4. The ViewModel reloads the screen data and calls onDecisionRecorded so notifications can react.

## Tests

Unit tests are in app/src/test/java/com/example/bursaryaid/feature/admin/logic/ (eligibility, allocation, workflow, filter, form parsing, decision validation, review models).
Status: written, but not yet run, because the main branch does not compile (errors are in MainActivity.kt and feature/student, not in this module).

## Known limitations and open decisions

- The merged schema has no income limit on Bursary and only a text income level on StudentProfile, so the income rule is skipped for now.
- AllocationRecord has no reviewedByUserId column, so the reviewing admin is not stored.
- Bursary.remainingBudget is stored by Member 3; this module updates it on approval but treats the calculated value as the truth.
- Admin navigation routes are pending from Member 1.
- Proposals awaiting the group lead's confirmation: income limit is a ceiling; explicit criteria instead of scoring; rejections recorded with amount 0; an award cannot exceed the request; an ineligible application cannot be approved; requestInformation belongs to this module; this module writes the budget rule while Member 3 provides the transaction.