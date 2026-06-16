# Test Plan: Grit — Android Habit Tracker & Todo App (Habitly POC)

---

## 1. Document Control

| Field | Details |
|---|---|
| **Document Name** | Test Plan — Grit Android Habit Tracker & Todo App (Habitly POC) |
| **Version** | 1.0 |
| **Epic ID** | SCRUM-6 |
| **Prepared By** | QA Architect (assigned to SCRUM-6 test planning) |
| **Reviewed By** | *[To be assigned — QA Lead / Tech Lead]* |
| **Approved By** | *[To be assigned — Product Owner / Engineering Manager]* |
| **Date** | 2026-06-16 |
| **Status** | Draft |

---

### Revision History

| Version | Date | Author | Description of Change |
|---|---|---|---|
| 0.1 | 2026-06-16 | QA Architect | Initial draft created from Epic SCRUM-6 requirements |
| 1.0 | 2026-06-16 | QA Architect | Baseline version submitted for stakeholder review |
| — | *TBD* | *TBD* | Updates pending child issue decomposition and sprint planning |

---

## 2. Objective

### 2.1 Purpose

This test plan defines the complete quality assurance strategy for the **Grit** Android application, a free, open-source productivity tool that unifies a daily habit tracker with a categorized todo list in a single Material You interface. The plan covers all testable features described in Epic SCRUM-6, establishes the testing scope, approach, design techniques, and deliverables, and provides the framework for ongoing quality validation across all planned releases.

### 2.2 Quality Risks Being Mitigated

The testing effort is specifically intended to reduce the following quality risks inherent to the product and its technical implementation:

| Quality Risk | Why It Matters |
|---|---|
| **Streak calculation incorrectness** | Streak logic is a core differentiator; a miscalculated streak (e.g., not resetting on missed applicable days) directly damages user trust and drives churn |
| **Notification delivery failures** | Both habit reminders (daily recurring) and task reminders (one-time) are triggered by Android alarm/notification infrastructure, which is highly fragmented across OEMs and Android versions |
| **Data loss during backup/restore** | With no cloud sync, the export/import mechanism is the sole data recovery path; any silent data corruption is catastrophic and irreversible |
| **Widget rendering breakage** | Jetpack Glance widget behavior varies across Android versions and launcher implementations; broken widgets reduce daily active usage |
| **Incorrect heatmap and analytics aggregation** | OverallAnalytics calculations (consistency score, weekly comparison, heatmap) must be accurate to motivate behavior change — erroneous data is the core product failing |
| **Drag-and-drop reordering data integrity** | Task and category reordering must persist correct index values; index corruption leads to permanently disordered lists |
| **RTL layout correctness** | Localization support with RTL languages (Arabic, Hebrew, etc.) involves layout mirroring in Jetpack Compose; incorrect RTL rendering breaks usability for those locales |
| **Theming failures on older Android versions** | Dynamic theming fallback for pre-Android 12 devices requires the custom color picker path to function correctly — failure silently degrades the visual experience |
| **Room database schema migration errors** | Schema versioning is explicitly called out in the architecture; failed migrations can corrupt the entire local database on app update |
| **Koin DI initialization failures** | Compile-time KSP validation guards against some DI issues, but runtime injection failures on edge-case module configurations must be caught in testing |

### 2.3 Main Product Capabilities Being Verified

The following primary capability areas will be verified through this test plan:

1. **Habit Tracker Module** — creation, scheduling, daily check-in, streak tracking, analytics, and recurring reminders
2. **Todo / Task Module** — category management (with drag-and-drop), task management (with drag-and-drop), and one-time task reminders
3. **Home Screen Widgets** — Glance widget display accuracy, tap-to-complete interaction without opening the app
4. **Backup & Restore** — full data export/import cycle covering all entity types and user-selectable export location
5. **Settings & Customization** — Material You dynamic theming on Android 12+, custom color picker on older devices, in-app language picker, and RTL layout support
6. **Data Integrity & Persistence** — Room database correctness across app restarts, updates (schema migration), and after backup/restore cycles
7. **Cross-feature Workflows** — integrated user journeys spanning habits, tasks, widgets, notifications, and settings simultaneously

---

## 3. Product / Requirement Summary

### 3.1 Application Overview

**Grit** (codename: Habitly POC) is a native Android application built with **Kotlin** and **Jetpack Compose Multiplatform**. It targets Android users who want a single, lightweight, privacy-respecting productivity app that handles both long-term behavioral habits and short-term task management without paywalls, subscriptions, or advertisements.

The application is distributed as a fully open-source product under the **GPLv3 license** via:
- Google Play Store
- F-Droid
- IzzyOnDroid
- GitHub (direct APK releases)

A **WASM web demo** (hosted on GitHub Pages) serves as a showcase for the KMP shared business logic layer but is not a primary test target.

### 3.2 Technical Stack Summary

| Layer | Technology |
|---|---|
| **UI Framework** | Jetpack Compose Multiplatform |
| **Language** | Kotlin |
| **Persistence** | Room (Android) with schema versioning |
| **Dependency Injection** | Koin + Koin Annotations (KSP compile-time validation) |
| **Async / Reactive** | Kotlin Coroutines + Flow |
| **Widgets** | Jetpack Glance (`androidx.glance.appwidget`) |
| **Architecture** | Clean Architecture + MVI-inspired presentation, domain-driven feature structure |
| **Multiplatform** | KMP shared business logic (Android + WASM demo) |
| **Drag-and-Drop** | Compose Reorderable library |
| **Localization** | Weblate (community translations), Android 13+ in-app language picker |

### 3.3 Key Data Models

| Model | Key Fields | Notes |
|---|---|---|
| **Habit** | `id`, `title`, `description`, `time (LocalDateTime)`, `days (Set<DayOfWeek>)`, `index`, `reminder (Boolean)` | Serializable for backup/restore |
| **HabitStatus** | `habitId`, `date (LocalDate)` | Completion record per habit per calendar date |
| **OverallAnalytics** | `heatMapData (Map<LocalDate, Int>)`, `weekDayFrequencyData`, `consistency (Float 0–1)`, `topHabits` | Aggregated at the domain layer |
| **Task** | `id`, `categoryId`, `title`, `index`, `status (Boolean)`, `reminder (LocalDateTime, nullable)` | Belongs to exactly one Category |
| **Category** | `id`, `name`, `index`, `color (String)` | `color` is reserved — not rendered in current UI |
| **StreakPosition** | Enum: current position within streak sequence | Drives UI badge rendering |

### 3.4 Key User Workflows

The following end-to-end workflows represent the primary usage paths and must be fully covered by testing:

**Workflow A — Daily Habit Check-In:**
User opens app → sees today's applicable habits on Habits List → taps a habit to mark complete → HabitStatus record created → streak increments → consistency score updates

**Workflow B — Quick Widget Check-In:**
User sees today's habits on home screen Glance widget → taps habit directly on widget → completion recorded without opening app → widget state refreshes

**Workflow C — Habit Analytics Review:**
User navigates to Habit Analytics → views monthly calendar heatmap (GitHub-style) → views weekly comparison chart (up to 1 year of history) → views consistency score and top habits

**Workflow D — Task Creation and Management:**
User creates a Category → creates Tasks within the Category → reorders tasks via drag-and-drop → marks tasks complete → optionally sets a one-time reminder on a task

**Workflow E — Backup and Restore:**
User navigates to Settings → triggers export → selects export location → all Habit, HabitStatus, Task, Category data serialized → User installs fresh app → triggers import → all data restored intact

**Workflow F — Habit Reminder Notification:**
Habit has reminder enabled with a scheduled time → Android alarm fires at scheduled time → notification shown → user taps quick check-in action button → habit marked complete without opening full app

**Workflow G — Theming Configuration:**
On Android 12+: user opens Settings → dynamic color wallpaper palette applied automatically
On Android < 12: user opens custom color picker → selects color → theme updates throughout app

**Workflow H — Language Change:**
On Android 13+: user selects language in-app → app re-renders in selected locale with correct RTL layout if applicable

### 3.5 Target Platforms and Distribution

| Platform | Support Level |
|---|---|
| **Android (Primary)** | Full support — primary test target |
| **iOS** | Out of scope — not supported |
| **WASM Web Demo** | Showcase only — not a test target for this plan |
| **Wear OS** | Out of scope — explicitly excluded |
| **Desktop** | Out of scope — future roadmap only |

### 3.6 Feature Priority Summary

| Priority | Features |
|---|---|
| **P0 (Must Have)** | F-01 Habit Creation & Scheduling, F-02 Daily Check-In, F-03 Streak Tracking, F-04 Habit Analytics, F-05 Habit Reminders, F-06 Task Categories, F-07 Task Management, F-11 Home Screen Widgets |
| **P1 (Should Have)** | F-08 Task Reminders, F-09 Material You Dynamic Theming, F-10 Localization, F-12 Backup & Restore |

---

## 4. Scope of Testing

### 4.1 In Scope

All features described in Epic SCRUM-6 with priority P0 and P1 are in scope for this test plan. Testing will cover functional correctness, boundary conditions, negative paths, data integrity, persistence, and platform-specific behaviors on Android.

---

#### F-01 — Habit Creation & Scheduling

- Creating a new habit with all fields: title, description, reminder time, and active days (`Set<DayOfWeek>`)
- Creating habits with minimum required fields only (title)
- Validating that `days` accepts any combination of days of the week, including single-day, multi-day, and all-seven-days configurations
- Validating that the `time` field stores and retrieves `LocalDateTime` correctly with no timezone conversion errors
- Editing an existing habit and verifying all field updates persist
- Deleting a habit and verifying all associated `HabitStatus` records are also removed (cascade behavior)
- Verifying the `index` field correctly represents display ordering
- Confirming the Habit model is serializable for backup/restore operations
- Confirming habits with no active days cannot be saved (if such validation exists — flagged as open question)

#### F-02 — Daily Check-In

- Habits List correctly filters and shows only habits applicable to today's `DayOfWeek`
- A habit scheduled for other days does not appear on today's Habits List
- Tapping a habit marks it complete and creates a `HabitStatus` record linked to today's `LocalDate` and the correct `habitId`
- A habit already marked complete today shows the correct completed visual state
- Re-tapping a completed habit correctly toggles it back to incomplete (if undo/toggle is supported — flagged as open question)
- `HabitStatus` records are persisted across app restarts
- Midnight rollover: habits reset to incomplete state at the start of a new day

#### F-03 — Streak Tracking

- Current streak increments correctly when a habit is completed on consecutive applicable days
- Current streak does NOT increment when a non-applicable day passes (e.g., habit is Mon/Wed/Fri only — Tuesday passing without check-in must not break streak)
- Current streak resets to zero when an applicable day is missed (no check-in recorded)
- Best streak is retained even after current streak resets
- Best streak is updated when current streak exceeds it
- `StreakPosition` enum is assigned correctly for streak start, middle, and end positions
- Streak display is accurate on the day immediately after a missed day
- Streak calculation is correct for habits with varied active-day configurations (daily, weekdays only, weekends only, custom combos)
- Historical streak data is consistent with `HabitStatus` records in the database

#### F-04 — Habit Analytics

- Consistency score (`Float 0.0–1.0`) is calculated correctly as completed applicable days ÷ total applicable days over the measurement period
- Consistency score displays as a meaningful percentage or ratio in the UI
- Monthly calendar heatmap correctly renders `heatMapData (Map<LocalDate, Int>)` — each date cell shaded proportional to completion intensity
- Weekly comparison chart renders up to 1 year of historical completion data (52 weeks)
- `weekDayFrequencyData` correctly reflects which days of the week have the highest completion rates
- `topHabits` correctly identifies and ranks the highest-consistency habits
- `OverallAnalytics` aggregation is accurate across all habits, not just a subset
- Analytics update immediately (or within one render cycle) after a new check-in
- Analytics display correctly for a brand new user with zero data (empty state)
- Analytics display correctly for users with up to 1 year of history (365+ `HabitStatus` records per habit)

#### F-05 — Habit Reminders

- Enabling the reminder toggle on a habit schedules a daily recurring notification at the specified `time`
- Notification fires at the correct time on the configured days
- Notification does NOT fire on days when the habit is not applicable
- Notification includes a **quick check-in action button** that marks the habit complete without opening the app
- Quick check-in from notification correctly creates a `HabitStatus` record for today
- Quick check-in from notification updates streak and analytics
- Disabling the reminder toggle cancels the scheduled notification
- Editing reminder time reschedules the notification to the new time
- Deleting a habit cancels all associated pending notifications
- Notifications survive app process death and device reboot (alarm re-registration required — verify boot receiver behavior)
- Notifications respect Android Do Not Disturb mode (verify behavior is consistent with system settings)
- On Android 13+, notification permission is requested and gracefully handled if denied

#### F-06 — Task Categories

- Creating a named category persists correctly with the assigned `index`
- Renaming a category persists the new name and retains all associated tasks
- Deleting a category and verifying behavior of associated tasks (deletion cascade or orphan handling — flagged as open question)
- `color (String)` field is stored but **not rendered in UI** per current scope (verify no visual color display)
- Reordering categories via drag-and-drop correctly updates `index` values for all affected categories
- Category order persists across app restarts
- Creating multiple categories and verifying each renders as a separate group
- Empty state displayed correctly when no categories exist

#### F-07 — Task Management

- Creating a task with title, `categoryId`, and optional fields persists correctly
- Task `index` is set correctly at creation (appended to end of category list)
- Marking a task complete sets `status = true` and applies correct visual completion state
- Marking a completed task incomplete sets `status = false`
- Drag-and-drop reordering within a category correctly updates `index` for all affected tasks
- Task order within category persists across app restarts
- Attempting to drag a task to a different category (if cross-category drag is unsupported — flagged as open question)
- Deleting a task removes it from the list and database
- Tasks belonging to different categories do not appear in each other's groups
- Empty state displayed correctly for a category with no tasks

#### F-08 — Task Reminders

- Setting a `reminder (LocalDateTime)` on a task schedules a one-time notification at the specified datetime
- Notification fires at the correct datetime for the task
- Notification does not repeat (one-time trigger only)
- Removing the reminder from a task cancels the pending notification
- Editing the reminder datetime reschedules the notification to the new time
- Completing a task before the reminder fires — verify whether the notification is cancelled (flagged as open question)
- Setting a reminder datetime in the past — verify validation or error handling
- Task reminders survive app process death and device reboot
- On Android 13+, notification permission is checked before scheduling

#### F-09 — Material You Dynamic Theming

- **On Android 12+ (API 31+):** Dynamic color is extracted from the current wallpaper palette and applied to the app theme automatically
- Theme updates when wallpaper is changed and app is reopened
- **On Android < 12 (API < 31):** Custom color picker is displayed as the theming option
- Custom color picker allows selection and applies selected color throughout the app
- Selected custom color persists across app restarts
- Theming applies consistently across all screens: Habits List, Analytics, Tasks, Settings, Widgets
- Dark mode and light mode respect the selected/dynamic theme colors
- No crashes or visual artifacts when switching between themes

#### F-10 — Localization

- In-app language picker is available and functional on **Android 13+ (API 33+)**
- Switching language re-renders all UI strings in the selected language
- **RTL layout support:** selecting an RTL language (e.g., Arabic, Hebrew) correctly mirrors the layout
- RTL mirroring applies to navigation, list items, icons, and input fields in Jetpack Compose
- Fallback behavior when a translation string is missing (English fallback or graceful degradation)
- No text truncation or overflow in translated strings (especially German, French, long-form languages)
- Weblate-sourced translation strings are correctly integrated into the build
- On Android < 13: language follows system locale (in-app picker not shown)

#### F-11 — Home Screen Widgets (Jetpack Glance)

- Widget can be added to the home screen via Android's widget picker
- Widget displays today's applicable habits correctly (filtering by `DayOfWeek`)
- Completed habits are visually distinguished from pending habits on the widget
- Tapping a habit on the widget marks it complete **without opening the app**
- Widget state updates after tap-to-complete (reflects new completion status)
- Widget data is consistent with in-app data (no stale state)
- Widget renders correctly at different widget size configurations (if resizable)
- Widget renders correctly on Android 12+ with rounded corners and updated Glance widget APIs
- Widget survives app update and device reboot (re-registration behavior)
- Widget empty state when no habits are applicable today

#### F-12 — Backup & Restore

- Export function serializes all data entities: Habits, HabitStatus records, Tasks, Categories
- User-selectable export location via Android Storage Access Framework (SAF) file picker
- Exported file is a valid, parseable format (JSON, ZIP, or defined format — flagged as open question)
- Export completes successfully for large datasets (e.g., 50 habits × 365 days of HabitStatus records)
- Import reads the exported file from user-selected location
- Import restores all Habits, HabitStatus, Tasks, and Categories with correct field values
- Import correctly restores `index` ordering for categories and tasks
- Import correctly restores streak-relevant HabitStatus history (streaks recalculate correctly post-import)
- Importing a corrupt or malformed file is handled gracefully (error message, no crash, no partial corruption)
- Importing a file from a different app version (schema version mismatch) is handled gracefully
- Duplicate import (importing into an app that already has data) — behavior is defined and tested (overwrite, merge, or reject — flagged as open question)
- Export location access is revoked by user — graceful error handling

#### Cross-Cutting Concerns

- **Room Database Schema Migration:** App update from a previous schema version migrates data without loss or corruption
- **Data Persistence:** All entities persist correctly across app process kill and restart
- **Kotlin Coroutines / Flow:** Repository layer emits correct data on updates; no missed emissions or flow cancellation bugs
- **Koin DI:** Application initializes without injection errors on fresh install and after app update
- **Empty States:** All screens display appropriate empty states when no data exists
- **Accessibility:** Content descriptions on interactive elements (habit cards, task checkboxes, widget tap targets)
- **Performance:** App does not exhibit jank (dropped frames) on Habits List, Analytics heatmap rendering, or drag-and-drop operations

---

### 4.2 Out of Scope

The following items are explicitly excluded from this test plan, either by the Epic's defined constraints or because they are future roadmap items not yet specified:

| Out of Scope Item | Reason |
|---|---|
| **iOS native app testing** | iOS is not supported; explicitly out of scope in SCRUM-6 |
| **Wear OS testing** | Explicitly excluded in Epic constraints |
| **WASM web demo functional testing** | Showcase only; not a primary product surface |
| **Desktop app testing** | Future roadmap — not yet specified |
| **Cloud sync functionality** | Intentionally excluded; no cloud sync in v1 by design |
| **Social progress sharing (shareable cards)** | Future roadmap item; not in current Epic scope |
| **Category color UI rendering** | Color field is reserved for future use; not rendered in current release |
| **Paid tier / IAP / Subscription flows** | No monetization in this product |
| **Advertisement SDK behavior** | No ads in this product |
| **Google Drive auto-backup (v2 feature)** | Mentioned as a future mitigation only |
| **Full KMP cross-platform business logic unit testing** | Covered separately at the unit/module level; integration testing is in scope |
| **Weblate translation pipeline infrastructure testing** | Translation CI/CD pipeline is an infrastructure concern outside app QA |
| **F-Droid / IzzyOnDroid build pipeline validation** | Build/distribution pipeline QA is outside app functional testing scope |
| **GPLv3 license compliance verification** | Legal/compliance concern outside QA test plan |
| **Full performance load testing (sustained load)** | Requires dedicated performance engineering; flagged under non-functional considerations (Section 17) |
| **Penetration testing / security audit** | Out of scope for this release; flagged under non-functional considerations |
| **Android TV / large screen specific testing** | Not mentioned in requirements |

---

## 5. Test Items / Features to be Tested

> **Note:** Epic SCRUM-6 currently has **0 child issues** decomposed. The table below maps directly to the Feature Requirements (F-01 through F-12) defined in the Epic description, as no finer-grained child stories, tasks, or sub-issues are available at this time. Once child issues are created and assigned, this table must be updated with corresponding issue IDs and any refined acceptance criteria.

| Feature ID | Feature / Module | Description | Test Coverage Required | Priority | Test Type(s) |
|---|---|---|---|---|---|
| **F-01** | Habit Creation & Scheduling | Create, edit, and delete habits with title, description, reminder time, and active days (`Set<DayOfWeek>`). Model must be serializable. | Field validation, CRUD operations, day-combination permutations, serialization round-trip, persistence across restart | P0 | Functional, Boundary, Negative, Data Validation |
| **F-02** | Daily Check-In | Habits List filtered to today's `DayOfWeek`. Tap to mark complete. `HabitStatus` record stored per date + habitId. | Correct daily filtering, check-in record creation, completed state display, midnight rollover, persistence | P0 | Functional, Boundary, Integration, Data Validation |
| **F-03** | Streak Tracking | Current streak, best streak, `StreakPosition` enum. Missed applicable day resets streak. Non-applicable days do not break streak. | Streak increment/reset logic for all day configurations, best streak retention, enum assignment, historical accuracy | P0 | Functional, Boundary, Negative, Data Validation |
| **F-04** | Habit Analytics | Consistency score (`Float 0–1`), weekly comparison chart (1yr history), monthly calendar heatmap, `OverallAnalytics` aggregation. | Calculation accuracy, chart rendering with full history, empty state, heatmap data mapping, top habits ranking | P0 | Functional, Boundary, Data Validation, Exploratory |
| **F-05** | Habit Reminders | Daily recurring notifications. Quick check-in from notification action button. Toggle per habit. | Notification scheduling/cancellation, notification on correct days only, quick check-in updates data, reboot persistence, Android 13 permission | P0 | Functional, Negative, Integration, Platform-Specific |
| **F-06** | Task Categories | Named, colored (stored not rendered), reorderable groups. Drag-and-drop via Compose Reorderable. | Category CRUD, drag-and-drop index updates, persistence, empty state, color field stored not rendered | P0 | Functional, Negative, Data Validation, Boundary |
| **F-07** | Task Management | Tasks with title, completion status, display order. Drag-to-reorder within categories. | Task CRUD, completion toggle, drag-and-drop index updates, category isolation, persistence | P0 | Functional, Negative, Data Validation, Boundary |
| **F-08** | Task Reminders | One-time datetime reminders for tasks (`LocalDateTime`, nullable). | Notification scheduling for exact datetime, one-time only (no repeat), cancellation on edit/delete, past datetime handling, reboot persistence | P1 | Functional, Negative, Boundary, Integration, Platform-Specific |
| **F-09** | Material You Dynamic Theming | Dynamic wallpaper palette on Android 12+; custom color picker for Android < 12. | Theme auto-application on API 31+, custom picker on API < 31, color persistence, dark/light mode, all screens consistently themed | P1 | Functional, Platform-Specific, Exploratory |
| **F-10** | Localization | Weblate translations, in-app language picker (Android 13+), RTL layout support. | Language switching and re-render, RTL layout mirroring, missing string fallback, text overflow, Android < 13 system locale fallback | P1 | Functional, Negative, Platform-Specific, Exploratory |
| **F-11** | Home Screen Widgets (Glance) | Jetpack Glance widgets showing today's habits. Tap to complete without opening app. | Widget rendering (today's habits, correct filter), tap-to-complete updates data, widget state refresh, data consistency with in-app state, widget resize, reboot survival | P0 | Functional, Integration, Platform-Specific, Boundary |
| **F-12** | Backup & Restore | Export/import all habit, task, category, and status data. User-selectable export location via SAF. | Full export completeness, import restores all data correctly, index ordering preserved, streaks recalculate post-import, corrupt file handling, large dataset, schema version mismatch | P1 | Functional, Negative, Boundary, Data Validation, Integration |
| **CROSS-01** | Room Database Schema Migration | Schema versioning across app updates. | Migration from previous schema version preserves all data without loss or corruption | P0 | Functional, Data Validation, Regression |
| **CROSS-02** | Data Persistence (App Lifecycle) | All data entities survive app process kill and restart. | Habits, HabitStatus, Tasks, Categories all persist after force-stop and cold start | P0 | Functional, Integration |
| **CROSS-03** | Koin DI Initialization | App-level Koin DI initializes correctly on fresh install and after update. | No injection errors on cold start, all dependencies resolved, no runtime crashes on app launch | P0 | Smoke, Functional, Regression |
| **CROSS-04** | Kotlin Coroutines / Flow | Repository layer emits correct updates reactively; no missed updates or flow leaks. | Data changes propagate to UI layer correctly, concurrent operations do not cause race conditions | P0 | Functional, Integration, Exploratory |
| **CROSS-05** | Empty States | All feature screens handle zero-data condition gracefully. | Correct empty state UI shown for habits, tasks, categories, analytics, and widgets when no data exists | P0 | Functional, Boundary |
| **CROSS-06** | Accessibility | Interactive elements are accessible with content descriptions. | Habit cards, task checkboxes, widget tap targets, and buttons have meaningful content descriptions for TalkBack | P1 | Functional, Exploratory |

---

## 6. Test Approach / Test Strategy

The following testing types have been selected based on the nature of the Grit application — a local-data-only Android app with complex business logic (streak calculation, analytics aggregation), reactive data flows (Coroutines/Flow), platform-dependent behaviors (notifications, widgets, theming), and a serialization-based data safety mechanism (backup/restore). No API/backend testing is required. All testing is Android on-device or emulator based.

---

### 6.1 Smoke Testing

**Why needed:** Grit is a Kotlin/Compose app with Koin DI — any initialization failure, database creation failure, or Compose rendering crash will prevent all other testing. Smoke testing establishes a baseline before functional testing begins.

**What will be tested:**
- App installs and launches without crash on target Android versions
- Koin DI module initialization completes successfully
- Room database is created on first launch
- All primary navigation destinations render without crash: Habits List, Analytics, Tasks, Settings
- Home screen widget can be added without crash

**Example coverage:**
- Fresh install on Android 8.0 (API 26), Android 12 (API 31), and Android 14 (API 34)
- Force-stop and cold relaunch without crash
- Widget addition via home screen long-press

---

### 6.2 Functional Testing

**Why needed:** The core value of Grit lies in the correctness of its functional behavior — habits must be created and scheduled correctly, streaks must calculate per defined rules, tasks must sort and persist, analytics must aggregate accurately. Every feature requirement must be verified end-to-end.

**What will be tested:**
- All P0 and P1 features (F-01 through F-12) as defined in Section 4.1
- Happy path user workflows (Workflows A through H from Section 3.4)
- All CRUD operations for Habit, Task, and Category entities
- Business rule verification: streak reset logic, applicable-day filtering, consistency score formula
- Notification scheduling, firing, and action handling

**Example coverage:**
- Create a habit with Mon/Wed/Fri schedule → check in on Monday → skip Wednesday → verify streak resets on Thursday
- Mark 7 consecutive daily habits complete → verify current streak = 7, best streak = 7 → miss day → verify current streak = 0, best streak = 7
- Create 3 categories with 5 tasks each → reorder tasks in category 2 → restart app → verify order persists

---

### 6.3 Positive Testing

**Why needed:** Confirming that all expected inputs and interactions produce the correct, defined outputs is the foundation of functional validation.

**What will be tested:**
- All valid input combinations for Habit creation (title only, all fields, various day combinations)
- Valid task creation with and without reminders
- Successful backup export to a valid location
- Successful backup import of a valid file
- Theme selection (dynamic on API 31+, custom color on older)
- Language switch to a supported locale

**Example coverage:**
- Create habit with title = "Morning Run", description = "10 km", days = {MONDAY, WEDNESDAY, FRIDAY}, time = 07:00 → all fields saved correctly
- Export backup to Downloads folder → file created at selected path with all data

---

### 6.4 Negative Testing

**Why needed:** The app must not crash, corrupt data, or silently fail when users provide unexpected inputs, take unusual actions, or the system returns errors (e.g., storage access denied, notification permission denied).

**What will be tested:**
- Habit creation with empty title (if validation required)
- Task reminder set to a past datetime
- Backup import of a corrupt or zero-byte file
- Backup export when storage permission/access is denied
- Notification permission denied on Android 13+ (app must not crash, must degrade gracefully)
- Deleting a category that contains tasks
- Importing a backup file with an unrecognized schema version
- Extreme input values (title with 1000 characters, 0 active days selected)

**Example coverage:**
- Attempt to create habit with no title → expect validation error or button disabled, no crash
- Select corrupt backup file for import → expect error dialog, no data corruption
- Deny notification permission → habit reminders fail gracefully, app continues to function

---

### 6.5 Boundary Value Analysis Testing

**Why needed:** Grit has numerous numerical and date-based boundaries: streak counts, consistency float range (0.0–1.0), habit index ordering, 1-year analytics window, and LocalDateTime/LocalDate edge cases.

**What will be tested:**
- Streak = 0 (no check-ins ever), streak = 1 (first check-in), very long streaks (365+ days)
- Consistency score at exactly 0.0 (no completions),

---

## 8. Test Scenarios

> Generated across 1 batch(es) — 0 issues total

### Batch 1 — Issues 1–0 (0 issues)

> **Note:** This batch contains 0 child issues. No individual issue-level test scenario tables have been generated, as there are no child issues assigned under Epic SCRUM-6 at this time.
>
> Once child issues (stories, tasks, or sub-tasks) are created and linked to this Epic, subsequent batches will produce per-issue test scenario tables covering the following feature areas identified in the Epic description:
>
> | Feature ID | Feature Area | Module |
> |------------|--------------------------------------------------|--------------------------|
> | F-01 | Habit Creation & Scheduling | Habit Tracker |
> | F-02 | Daily Check-In | Habit Tracker |
> | F-03 | Streak Tracking | Habit Tracker |
> | F-04 | Habit Analytics | Habit Tracker |
> | F-05 | Habit Reminders | Habit Tracker |
> | F-06 | Task Categories | Todo / Task |
> | F-07 | Task Management | Todo / Task |
> | F-08 | Task Reminders | Todo / Task |
> | F-09 | Material You Dynamic Theming | Settings & Customization |
> | F-10 | Localization & RTL Support | Settings & Customization |
> | F-11 | Home Screen Widgets (Glance) | Settings & Customization |
> | F-12 | Backup & Restore | Settings & Customization |
>
> No scenario IDs have been assigned in this batch. The TS-001 sequence will begin with the first child issue in the next populated batch.

---

## 9. Test Data Strategy

### 9.1 Overview

Since Grit is a local-only Android application with no backend API, all test data is stored in a Room database on the device. Test data strategy covers valid, invalid, boundary, and edge-case inputs across all four primary data entities: **Habit**, **HabitStatus**, **Task**, and **Category**. Data is seeded programmatically via instrumented test setup or manually via the UI for exploratory sessions.

---

### 9.2 Habit Entity Test Data

#### 9.2.1 Valid Habit Data

| Field | Valid Example 1 | Valid Example 2 | Valid Example 3 |
|---|---|---|---|
| id | Auto-generated UUID | Auto-generated UUID | Auto-generated UUID |
| title | "Morning Run" | "Read 20 Pages" | "Meditate" |
| description | "Run 5km before 8am" | "Non-fiction only" | "" (empty, optional) |
| time (LocalDateTime) | 06:30:00 | 21:00:00 | 07:00:00 |
| days (Set of DayOfWeek) | MON, WED, FRI | MON, TUE, WED, THU, FRI, SAT, SUN | SAT, SUN |
| reminder (Boolean) | true | false | true |
| index | 0 | 1 | 2 |

#### 9.2.2 Invalid / Negative Habit Data

| Field | Invalid Value | Expected Behavior |
|---|---|---|
| title | "" (empty string) | Validation error — title is required |
| title | null | Validation error — title must not be null |
| title | String of 1001 characters | Validation error — exceeds max length (assumption: 255 chars) |
| title | "<script>alert('xss')</script>" | Stored as plain text, not executed |
| title | "𝕳𝖆𝖇𝖎𝖙 😊 ★ ❤️" | Accepted — Unicode and emoji support |
| days | Empty Set (no days selected) | Validation error — at least one day required |
| time | null | Validation error — reminder time required |
| reminder | null | Defaults to false or validation error |
| index | -1 | Validation error or corrected to 0 |
| description | String of 5000 characters | Accepted up to defined max or gracefully truncated |

#### 9.2.3 Boundary Habit Data

| Scenario | Value | Expected Behavior |
|---|---|---|
| Title minimum length | 1 character (e.g., "A") | Accepted |
| Title at assumed maximum | 255 characters | Accepted |
| Title one over maximum | 256 characters | Rejected with validation message |
| Days — minimum selection | 1 day (e.g., MON only) | Accepted |
| Days — maximum selection | All 7 days | Accepted |
| Reminder time — midnight | 00:00:00 | Accepted |
| Reminder time — end of day | 23:59:59 | Accepted |
| First habit created | index = 0 | Accepted |
| Maximum habits (stress) | 500 habits | App remains responsive, no crash |

#### 9.2.4 Habit Streak / Analytics Test Data

| Scenario | Data Setup | Expected Output |
|---|---|---|
| Streak of 0 | No check-ins ever | currentStreak = 0, bestStreak = 0 |
| Streak of 1 | Checked in today only | currentStreak = 1, bestStreak = 1 |
| Active streak of 7 | 7 consecutive daily check-ins | currentStreak = 7, bestStreak ≥ 7 |
| Streak reset | 5-day streak, missed yesterday | currentStreak = 0, bestStreak = 5 |
| Non-applicable day skipped | Habit set Mon/Wed/Fri, check-in on Mon and Wed | Streak should not reset for Tue (non-applicable) |
| Best streak preserved | New streak of 3 after reset from 10 | bestStreak = 10 |
| Consistency score — 100% | All applicable days checked in for 30 days | consistency = 1.0 |
| Consistency score — 50% | Half of applicable days checked in for 30 days | consistency = 0.5 |
| Consistency score — 0% | No check-ins for 30 days | consistency = 0.0 |
| Heatmap — 1 year history | 365 days of HabitStatus records | All 365 dates rendered in heatmap |
| Weekly comparison | Current week vs. same week last year | Both data sets populated and comparable |

---

### 9.3 HabitStatus Entity Test Data

| Field | Valid | Invalid / Edge Case |
|---|---|---|
| habitId | Valid existing UUID | Non-existent UUID (orphan record) |
| date (LocalDate) | 2026-06-16 | Future date (2099-12-31) |
| date | 2000-01-01 (historical) | null |
| Duplicate entry | Same habitId + same date | Should prevent duplicate; upsert or reject |
| Check-in on non-scheduled day | Habit scheduled Mon only; check-in on Tuesday | Behavior under test — should be blocked or flagged |

---

### 9.4 Task Entity Test Data

#### 9.4.1 Valid Task Data

| Field | Valid Example 1 | Valid Example 2 |
|---|---|---|
| id | Auto-generated UUID | Auto-generated UUID |
| categoryId | Existing Category UUID | Existing Category UUID |
| title | "Buy groceries" | "Submit Q3 report" |
| index | 0 | 1 |
| status (Boolean) | false (incomplete) | true (complete) |
| reminder (LocalDateTime) | 2026-06-20T09:00:00 | null (no reminder) |

#### 9.4.2 Invalid / Boundary Task Data

| Field | Invalid Value | Expected Behavior |
|---|---|---|
| title | "" (empty) | Validation error |
| title | null | Validation error |
| title | 1001-character string | Validation error or graceful truncation |
| categoryId | null | Validation error — category required |
| categoryId | Non-existent UUID | Integrity error or graceful failure |
| reminder | Past date/time (2020-01-01T00:00) | Accepted but no notification fires; warn user (assumption) |
| reminder | null | Accepted — no reminder scheduled |
| reminder | Date only, no time | Validation error — full LocalDateTime required |
| index | -1 | Corrected or rejected |
| status | null | Defaults to false |

---

### 9.5 Category Entity Test Data

| Field | Valid | Invalid / Edge Case |
|---|---|---|
| id | Auto-generated UUID | — |
| name | "Work", "Personal", "Health" | "" (empty) — validation error |
| name | "日本語カテゴリ" (Japanese) | Accepted — Unicode support |
| name | Duplicate name | Accepted (no unique constraint) or blocked (clarify) |
| index | 0, 1, 2 | -1 — rejected |
| color (String) | "#FF5733" (reserved, not rendered) | "invalid_color", null — accepted (reserved field) |
| Reorder | Move category from index 2 to index 0 | All indexes update correctly |
| Delete category with tasks | Category has 5 tasks | Tasks orphaned or cascade deleted (behavior to verify) |
| Empty category | Category with 0 tasks | Rendered without error |

---

### 9.6 Backup & Restore Test Data

| Scenario | Data Description | Expected Behavior |
|---|---|---|
| Full export — small dataset | 5 habits, 10 tasks, 3 categories, 30-day history | Export file created; all data present |
| Full export — large dataset | 100 habits, 500 tasks, 50 categories, 365-day history | Export completes without timeout or crash |
| Valid import | Previously exported valid file | All data restored; no duplicates |
| Corrupt import file | Manually broken JSON/binary | Graceful error message; no data corruption |
| Partial import file | Missing Category section | Handled gracefully; user informed of partial restore |
| Import on fresh install | Empty DB + import file | All data restored correctly |
| Import when DB has existing data | Existing 5 habits + import 5 habits | Merge or overwrite behavior clearly defined |
| Wrong file type | .txt or .jpg selected | Rejected with descriptive error |
| Export destination — internal storage | Default path | File created at expected path |
| Export destination — user-selected | External SD card or Downloads | File created at selected path |

---

### 9.7 Notification / Reminder Test Data

| Scenario | Data | Expected Behavior |
|---|---|---|
| Habit reminder enabled | reminder = true, time = 08:00 | Notification fires at 08:00 daily |
| Habit reminder disabled | reminder = false | No notification scheduled |
| Quick check-in from notification | Tap action button on notification | HabitStatus record created for today |
| Task reminder set | reminder = 2026-06-20T09:00:00 | One-time notification at specified datetime |
| Reminder after task completed | Task marked complete before reminder fires | Notification suppressed or fires (define expected behavior) |
| Device rebooted | Reminders were set before reboot | Reminders rescheduled after boot (BroadcastReceiver) |

---

### 9.8 Widget Test Data

| Scenario | Data | Expected Behavior |
|---|---|---|
| Widget with habits today | 3 habits scheduled for today | Widget shows 3 habits |
| Widget with no habits today | All habits scheduled for tomorrow | Widget shows empty or "No habits today" |
| Tap habit on widget | Tap check-in button on widget | Habit marked complete without opening app |
| Widget after all habits complete | All today's habits checked | Widget updates to reflect completion |
| Widget on fresh install | No habits created | Widget shows empty/onboarding state |

---

### 9.9 Localization Test Data

| Locale | Script Direction | Test Focus |
|---|---|---|
| English (en) | LTR | Baseline |
| Arabic (ar) | RTL | Layout mirroring, text alignment |
| German (de) | LTR | Long string truncation ("Gewohnheitsverfolgung") |
| Japanese (ja) | LTR | Multi-byte character rendering |
| Hindi (hi) | LTR | Devanagari script rendering |
| Missing translation key | Any locale | Falls back to English gracefully |

---

## 10. Environment Requirements

### 10.1 Environment Overview

| Environment | Purpose | Access | Notes |
|---|---|---|---|
| **Local Dev** | Developer unit testing, initial builds | Developer machine only | Hilt test rules, mock Room DB |
| **CI Build** | Automated unit + instrumented tests on every PR | GitHub Actions (automated) | Android emulator via AVD; no physical device required |
| **QA Device Lab** | Manual and instrumented testing on real devices | QA Engineer | Physical device pool (see 10.3) |
| **Staging (Pre-Release)** | Final regression before Play Store submission | QA Lead + PO | Signed release APK/AAB; production-equivalent config |
| **Production (Play Store)** | Post-release smoke testing | QA Lead | Limited — smoke only; no destructive data operations |

---

### 10.2 Application Under Test Details

| Property | Value |
|---|---|
| Application ID | com.shub39.grit |
| Source Repository | github.com/shub39/Grit |
| Primary Distribution | Google Play Store |
| Secondary Distribution | F-Droid, IzzyOnDroid, GitHub Releases |
| Minimum Android SDK | To be confirmed (Open Question OQ-01) |
| Target Android SDK | Android 15 (API 35) — assumption |
| Build System | Gradle + KSP |
| Language | Kotlin + Jetpack Compose Multiplatform |
| Architecture | Clean Architecture + MVI |

---

### 10.3 Physical Device & Emulator Matrix

| Device / Emulator | Android Version | API Level | Purpose | Material You Support |
|---|---|---|---|---|
| Google Pixel 9 Pro | Android 15 | API 35 | Primary QA device | Yes (Dynamic Color) |
| Google Pixel 6 | Android 13 | API 33 | Language picker testing (Android 13+) | Yes |
| Samsung Galaxy S23 | Android 14 | API 34 | OEM skin compatibility | Yes |
| OnePlus 11 | Android 13 | API 33 | OEM skin compatibility | Yes |
| Android Emulator (x86_64) | Android 12 | API 31 | Dynamic theming boundary (Material You introduced) | Yes |
| Android Emulator (x86_64) | Android 11 | API 30 | Older OS — custom color picker path | No (fallback) |
| Android Emulator (x86_64) | Android 10 | API 29 | Minimum supported OS check | No (fallback) |
| Foldable Emulator | Android 14 | API 34 | Layout adaptation | Yes |

---

### 10.4 Tools & Infrastructure

| Tool / Technology | Version | Purpose |
|---|---|---|
| Android Studio | Latest stable (Ladybug+) | IDE for build, run, debug |
| Gradle | 8.x | Build system |
| KSP (Kotlin Symbol Processing) | Matching Kotlin version | Compile-time DI validation (Koin Annotations) |
| JUnit 4 / JUnit 5 | 4.13.x / 5.x | Unit testing framework |
| Kotlin Coroutines Test | Matching coroutines version | Coroutine-aware unit tests |
| Turbine | Latest | Flow emission testing |
| Room Testing | Matching Room version | In-memory Room DB for instrumented tests |
| Compose UI Test | Matching Compose version | Jetpack Compose UI instrumented tests |
| Espresso | 3.x | Legacy view interaction (if any) |
| Robolectric | 4.x | JVM-based Android unit tests |
| GitHub Actions | N/A | CI/CD pipeline |
| Firebase Test Lab | Optional | Automated testing on real device cloud |
| Postman / cURL | N/A | Not applicable (no backend API) |
| ADB (Android Debug Bridge) | Platform Tools latest | Device communication, log capture |
| Logcat / Android Studio Profiler | Built-in | Log analysis, memory/CPU profiling |
| LeakCanary | 2.x | Memory leak detection in debug builds |
| ACRA / Crashlytics | TBD | Crash reporting (Open Question OQ-02) |
| Allure / Gradle Test Reports | Latest | Test reporting |
| Weblate | Hosted | Translation management |

---

### 10.5 Credentials & Access Strategy

| Resource | Access Method | Owner |
|---|---|---|
| GitHub Repository | GitHub account with repo access | Assignee / QA Lead |
| Google Play Internal Testing | Google Play Console invite | Assignee |
| F-Droid build pipeline | F-Droid submission confirmed | Assignee |
| Debug APK signing key | Shared debug keystore (debug builds) | Developer |
| Release APK signing key | Secure keystore — developer only | Assignee |
| Android emulator (local) | Android Studio AVD Manager | QA Engineer |
| Firebase Test Lab (if used) | Firebase project service account | QA Lead |
| Notification permission | Granted via ADB or UI during test setup | QA Engineer |
| Storage permission | Granted via ADB or UI (backup/restore testing) | QA Engineer |

---

### 10.6 Environment-Specific Configuration

| Configuration | Dev | CI | QA Device Lab | Staging |
|---|---|---|---|---|
| Build Variant | debug | debug | debug + release | release |
| LeakCanary | Enabled | Enabled | Enabled | Disabled |
| Room DB | In-memory (unit tests) | In-memory (unit tests) | On-device DB | On-device DB |
| Notifications | Mocked in unit tests | Mocked in unit tests | Real notifications | Real notifications |
| Widget | Emulator-based | Emulator (CI emulator) | Real device | Real device |
| Database Pre-population | Via test fixtures | Via test fixtures | Manual + seeded | Manual or imported backup |

---

## 11. Entry Criteria

The following criteria must be satisfied before formal test execution begins for any phase:

- [ ] **EC-01 — Requirements Baselined:** The Epic description (SCRUM-6) and all associated feature requirements (F-01 through F-12) have been reviewed, clarified, and baselined. All open questions identified in this test plan have been resolved or formally deferred.
- [ ] **EC-02 — Build Availability:** A stable, installable debug APK or AAB has been successfully compiled, passes Gradle build without errors, and is deployed to the QA environment (physical device lab or CI emulator) without requiring workarounds.
- [ ] **EC-03 — Smoke Test Passed:** The basic smoke test suite (app launches, habit can be created, task can be created, category can be created, navigation between all primary screens is functional) passes without critical blockers on at least one target device.
- [ ] **EC-04 — Test Environment Ready:** All required physical devices and emulators are provisioned, configured, and accessible. Required Android SDK versions (as per device matrix in Section 10.3) are installed. Notification and storage permissions are pre-grantable via ADB.
- [ ] **EC-05 — Test Data Strategy Finalized:** Test data fixtures, seed scripts, and manual data entry guides are prepared and reviewed by the QA lead for all entities (Habit, HabitStatus, Task, Category) as defined in Section 9.
- [ ] **EC-06 — Test Cases Written & Reviewed:** All test cases derived from this test plan's scenarios (Section 8) have been authored, peer-reviewed, and loaded into the defect/test management tool. Traceability to requirement IDs (F-01 through F-12) is verified.
- [ ] **EC-07 — Defect Management Tool Configured:** The chosen defect management tool (e.g., GitHub Issues with defined labels, or Jira) is configured with the defect template from Section 13, severity/priority labels, and the QA-to-developer assignment workflow is operational.
- [ ] **EC-08 — Development Unit Tests Passing:** All developer-authored unit tests in the repository pass on the CI pipeline (GitHub Actions) without failures. No known build-breaking issues exist on the main or release branch under test.
- [ ] **EC-09 — Feature Completeness Declared:** The development team has declared that all in-scope features for the current test cycle (as agreed with the Product Owner) are code-complete and ready for QA. Features not declared complete are excluded from this cycle's entry.
- [ ] **EC-10 — QA Team Onboarding Complete:** The assigned QA engineer(s) have access to the source repository, understand the Kotlin/Jetpack Compose architecture, have reviewed the Epic description, and have set up local development/test environments successfully.

---

## 12. Exit Criteria

Testing for a release cycle is considered complete when all of the following criteria are met:

- [ ] **EX-01 — Planned Test Case Execution:** A minimum of **95%** of all planned test cases have been executed. Any unexecuted test cases are formally documented with justification (e.g., environment unavailability, deferred feature) and approved by the QA Lead.
- [ ] **EX-02 — Critical & High Defect Resolution:** **100%** of Severity-1 (Critical) and **100%** of Severity-2 (High) defects are resolved (status: Fixed + Verified) or formally accepted as Known Issues with documented business justification and Product Owner sign-off.
- [ ] **EX-03 — Medium Defect Threshold:** No more than **3** open Severity-3 (Medium) defects exist that are not deferred to a subsequent release. Each deferred medium defect must be logged with a target fix version.
- [ ] **EX-04 — Regression Suite Passed:** The full regression test suite (manual and/or automated) passes with a **≥ 95%** pass rate on all primary target devices (minimum: Pixel 9 Pro with Android 15 and one Android 11/API 30 device for backward compatibility).
- [ ] **EX-05 — All P0 Features Verified:** All Priority-0 features (F-01: Habit Creation, F-02: Daily Check-In, F-03: Streak Tracking, F-04: Analytics, F-05: Habit Reminders, F-06: Task Categories, F-07: Task Management, F-11: Home Screen Widgets) have verified test coverage with documented pass results.
- [ ] **EX-06 — Backup & Restore Verified End-to-End:** The full backup export and import cycle (F-12) has been successfully executed including large dataset, corrupt file rejection, and cross-device restore scenarios.
- [ ] **EX-07 — No Crash on Core Workflows:** Zero unhandled crash events (ANR or Force Close) observed during any of the following: habit creation, daily check-in, streak calculation, task creation, category reorder, widget interaction, and backup/restore.
- [ ] **EX-08 — Localization Smoke Passed:** RTL layout (Arabic) and at least two non-English LTR locales have been verified for correct rendering, text overflow handling, and no missing translation keys falling back to key codes (e.g., rendering "habits.title" instead of translated text).
- [ ] **EX-09 — Test Deliverables Complete:** All test deliverables listed in Section 14 have been produced, reviewed, and stored in the agreed repository/location.
- [ ] **EX-10 — Stakeholder Sign-Off Received:** The Product Owner (Sourav Das Mahapatra) and QA Lead have formally reviewed the Test Execution Report and Test Summary Report and provided written approval (sign-off table in Section 22) to proceed to release or the next development cycle.

---

## 13. Defect Management Process

### 13.1 Defect Reporting Tool

**Primary Tool:** GitHub Issues (native to the open-source project at github.com/shub39/Grit)

**Labels to configure in GitHub:**
- Severity: `severity: critical`, `severity: high`, `severity: medium`, `severity: low`
- Priority: `priority: P1`, `priority: P2`, `priority: P3`, `priority: P4`
- Type: `bug`, `ui/ux`, `regression`, `performance`, `test-infra`
- Status: `status: new`, `status: confirmed`, `status: in-progress`, `status: fixed`, `status: ready-for-retest`, `status: verified`, `status: closed`, `status: deferred`, `status: wont-fix`
- Feature: `module: habit-tracker`, `module: todo`, `module: widget`, `module: settings`, `module: backup-restore`, `module: notifications`

---

### 13.2 Defect Lifecycle

```
NEW → CONFIRMED → IN PROGRESS → FIXED → READY FOR RETEST → VERIFIED → CLOSED
                                                ↓
                                          REOPEN → IN PROGRESS
                                                ↓
                                         DEFERRED / WON'T FIX
```

| State | Description | Owner |
|---|---|---|
| **New** | Defect filed by QA; awaiting triage | QA Engineer |
| **Confirmed** | Developer or QA Lead reproduces and validates the defect | Developer / QA Lead |
| **In Progress** | Developer actively working on fix | Developer |
| **Fixed** | Developer applies fix and marks ready for QA retest | Developer |
| **Ready for Retest** | Fix deployed to QA environment; awaiting verification | QA Engineer |
| **Verified** | QA confirms the fix works on target device/OS | QA Engineer |
| **Closed** | Verified fix accepted; defect lifecycle complete | QA Lead |
| **Reopened** | Fix did not resolve the issue; regression detected | QA Engineer |
| **Deferred** | Valid defect but not targeted for current release | Product Owner |
| **Won't Fix** | Defect acknowledged but not planned for fix (by design or low ROI) | Product Owner |

---

### 13.3 Severity & Priority Matrix

#### Severity Definitions

| Severity Level | Label | Definition | Example |
|---|---|---|---|
| **S1 — Critical** | `severity: critical` | App crash, data loss, complete feature failure with no workaround | App crashes on habit creation; backup corrupts all data; widget causes ANR |
| **S2 — High** | `severity: high` | Major feature broken; workaround exists but is unacceptable for release | Streak resets incorrectly; notification never fires; tasks cannot be reordered |
| **S3 — Medium** | `severity: medium` | Feature partially broken; workaround available; noticeable but not blocking | Heatmap misses one date; consistency score off by small margin; RTL layout slightly misaligned |
| **S4 — Low** | `severity: low` | Minor visual issue, typo, cosmetic defect; does not affect functionality | Label truncated by 2px; animation slightly janky; minor translation inconsistency |

#### Priority Definitions

| Priority Level | Label | Definition |
|---|---|---|
| **P1 — Immediate** | `priority: P1` | Must be fixed before release; blocks QA or end-user workflows |
| **P2 — High** | `priority: P2` | Must be fixed in current release cycle |
| **P3 — Medium** | `priority: P3` | Should be fixed; can be deferred to next minor release |
| **P4 — Low** | `priority: P4` | Nice to fix; can be deferred to backlog |

#### Severity × Priority Decision Guide

| | S1 Critical | S2 High | S3 Medium | S4 Low |
|---|---|---|---|---|
| **Core user workflow affected** | P1 | P1 | P2 | P3 |
| **Secondary feature affected** | P1 | P2 | P3 | P4 |
| **Edge case / rare condition** | P1 | P2 | P3 | P4 |
| **Cosmetic / non-functional** | P2 | P3 | P4 | P4 |

---

### 13.4 Defect Triage Process

1. QA Engineer files defect using the standard template (Section 13.5) within **24 hours** of discovery.
2. QA Lead reviews all new defects at the **daily triage** (or async on small team) — typically within 1 business day.
3. Developer confirms reproducibility; marks **Confirmed** or requests more info.
4. Product Owner is involved for **Severity 1** defects immediately and for all **Deferred / Won't Fix** decisions.
5. Critical (S1) defects trigger an immediate Slack/Discord notification to the developer and QA Lead.
6. All open defects are reviewed weekly to ensure no aging defects block release.

---

### 13.5 Retesting Process

1. Developer marks issue as **Fixed** and adds a comment describing: what was changed, which commit, and which build includes the fix.
2. QA Engineer pulls the updated build (or downloads from CI artifact).
3. QA re-executes the **exact reproduction steps** from the defect report on the **same device/OS** as the original failure.
4. QA also executes **adjacent test cases** to check for regression.
5. If fix is verified: status → **Verified** → **Closed**.
6. If fix is incomplete or causes regression: status → **Reopened**, developer notified, comment added.

---

### 13.6 Defect Template

```
---
### Defect Report

**Defect ID:** [Auto-assigned by GitHub Issues — e.g., #142]
**Title:** [Concise, action-oriented summary — e.g., "Streak resets incorrectly when habit is not scheduled for Sunday"]
**Date Filed:** [YYYY-MM-DD]
**Filed By:** [Name / GitHub handle]
**Assigned To:** [Developer GitHub handle]

---

#### Environment
| Property | Value |
|---|---|
| Device | e.g., Google Pixel 9 Pro |
| Android Version | e.g., Android 15 (API 35) |
| App Version / Build | e.g., v1.2.0 (Build 42) |
| Build Variant | Debug / Release |
| Install Source | Sideload APK / Play Store / F-Droid |
| Room DB Version | e.g., Schema v3 |

---

#### Feature / Module
[e.g., Habit Tracker — Streak Tracking (F-03)]

---

#### Steps to Reproduce
1. [Step 1 — e.g., Launch the app]
2. [Step 2 — e.g., Create a habit scheduled for MON, WED, FRI only]
3. [Step 3 — e.g., Mark the habit complete on MON and WED]
4. [Step 4 — e.g., Do not open the app on THU (non-scheduled day)]
5. [Step 5 — e.g., Open the app on FRI and observe streak count]

---

#### Expected Result
[e.g., Streak should read 3 because Thursday is not an applicable day and should not trigger a reset]

---

#### Actual Result
[e.g., Streak reads 0 — it was reset as if Thursday was a missed applicable day]

---

#### Test Data Used
| Field | Value |
|---|---|
| Habit Title | "Morning Run" |
| Scheduled Days | MON, WED, FRI |
| Check-in dates | 2026-06-15 (MON), 2026-06-17 (WED) |
| Observation date | 2026-06-19 (FRI) |
| HabitStatus records in DB | 2 records (MON + WED) |

---

#### Frequency of Occurrence
[e.g., Reproducible 100% of the time / Intermittent (3 out of 5 attempts)]

---

#### Impact
[e.g., Core streak feature is functionally broken for any habit not scheduled every day — affects majority of users]

---

#### Severity | Priority
| Severity | Priority |
|---|---|
| S2 — High | P1 — Immediate |

---

#### Attachments
- [ ] Screenshot / Screen recording: [attach link or file]
- [ ] Logcat output: [attach .txt or inline relevant log lines]
- [ ] Room DB dump (if applicable): [attach .db export]

---

#### Relevant Logcat Output
```
[Paste relevant logcat lines here — tag, timestamp, error message, stack trace if available]
```

---

#### Additional Notes
[Any workaround found; related defects; suspected code location if known]

---

**Status:** New
**Labels:** `bug` `severity: high` `priority: P1` `module: habit-tracker` `status: new`
```

---

### 13.7 Defect Closure Criteria

A defect is eligible for **Closed** status only when:
1. The QA Engineer has re-executed the original reproduction steps and confirmed the defect no longer occurs.
2. Adjacent and regression test cases related to the fix have been executed and passed.
3. The fix has been verified on the **same OS version and device** as the original report AND on at least one additional target device.
4. No new defects were introduced by the fix (regression check complete).
5. The QA Lead has reviewed the verification result (for S1 and S2 defects).

---

## 14. Test Deliverables

| # | Deliverable | Description | Format | Owner | Target Due Date |
|---|---|---|---|---|---|
| D-01 | **Test Plan** (this document) | Complete test planning document covering strategy, scope, approach, and all supporting sections | Markdown / PDF | QA Lead | [Sprint 1 — Day 5] |
| D-02 | **Test Scenarios Document** | High-level test scenarios grouped by feature (F-01 through F-12), mapped to requirement IDs | Markdown / Spreadsheet | QA Engineer | [Sprint 1 — Day 7] |
| D-03 | **Test Cases — Habit Tracker Module** | Detailed step-by-step test cases for F-01, F-02, F-03, F-04, F-05 with expected results | Spreadsheet / Test Tool | QA Engineer | [Sprint 2 — Day 3] |
| D-04 | **Test Cases — Todo/Task Module** | Detailed step-by-step test cases for F-06, F-07, F-08 | Spreadsheet / Test Tool | QA Engineer | [Sprint 2 — Day 3] |
| D-05 | **Test Cases — Settings, Theming, Localization** | Detailed step-by-

---

## 17. Non-Functional Testing Considerations

### 17.1 Performance Testing

**Applicability: HIGH — Required**

The app targets Android devices across a wide hardware spectrum (budget to flagship). Performance regressions directly impact Day-7 retention KPIs (target >35% at Month 3, >45% at Month 12).

| Test Area | Description | Threshold / Acceptance Criteria | Tool |
|---|---|---|---|
| App Cold Start | Time from launch intent to first meaningful frame on Habits List screen | < 600 ms on mid-range device (e.g., Snapdragon 680, 4 GB RAM) | Android Profiler, Macrobenchmark |
| App Warm Start | Resume from background | < 200 ms | Android Profiler |
| Habit List Render | Scroll through 100+ habits without frame drops | Maintain 60 fps; < 5 jank frames per 100 frames | Systrace, Compose Recomposition Counter |
| Task List Render | Scroll and drag-to-reorder with 50+ tasks across 10+ categories | Maintain 60 fps; drag response latency < 16 ms | Compose Layout Inspector |
| Analytics Screen Load | Load heatmap calendar (1-year history = ~365 data points) and weekly comparison chart | < 1,500 ms on mid-range device | Android Profiler, custom instrumentation |
| Glance Widget Refresh | Widget data update cycle | < 2,000 ms after habit completion triggers update | Logcat + manual measurement |
| Database Query Performance | Room queries for streak calculation and OverallAnalytics aggregation with 365 days × 20 habits = 7,300 HabitStatus rows | < 200 ms per query on main thread (must be off main thread by architecture); < 50 ms on IO dispatcher | Room query explain, Macrobenchmark |
| Backup Export | Export of full dataset (habits, tasks, categories, statuses) to user-selected location | < 3,000 ms for datasets up to 10,000 rows | Manual timing + Logcat |
| Backup Import | Import and database hydration | < 5,000 ms for maximum expected dataset | Manual timing + Logcat |
| Reminder Notification Delivery | AlarmManager / WorkManager trigger accuracy | Notification delivered within ±30 seconds of scheduled time under Doze mode | Manual device testing with ADB |

**Note:** Full automated load testing is not applicable given local-only, single-user architecture. Performance focus is on rendering, DB throughput, and widget responsiveness.

---

### 17.2 Load Testing

**Applicability: LIMITED — Applicable only at data-volume boundary level**

There is no server-side component. Load testing in the traditional sense (concurrent users, API throughput) is **not applicable**. However, data-volume stress testing is required to validate Room schema performance and UI rendering at realistic maximum data volumes.

| Scenario | Data Volume | Validation |
|---|---|---|
| Maximum Habits | 100 active habits with 365 days of HabitStatus records each | App remains responsive; no ANR; streak calculations complete within 500 ms |
| Maximum Tasks | 200 tasks across 20 categories | Drag-to-reorder remains fluid; no dropped frames |
| Maximum Backup File Size | Export of above maximum dataset | File size < 10 MB; export completes without OOM crash |
| Analytics Aggregation Stress | OverallAnalytics computed over full 1-year history with 100 habits | UI renders heatmap without OOM; computation stays off main thread |

---

### 17.3 Security Testing

**Applicability: MEDIUM — Required for local data privacy and open-source distribution trust**

Given the app's explicit privacy positioning ("no cloud sync — intentional for privacy") and GPLv3 open-source distribution, security validation is a trust-critical activity even in the absence of network endpoints.

| Test Area | Description | Acceptance Criteria | Tool |
|---|---|---|---|
| Local Data Storage | Verify Room database is stored in app-private internal storage, not world-readable | Database file not accessible without root; no plain-text data exposed on non-rooted device | ADB shell + manual inspection |
| Backup File Contents | Inspect exported backup file for sensitive data exposure | Exported file contains only habit/task/category data; no credentials, device IDs, or PII beyond user-entered content | Manual file inspection |
| Backup File Import Validation | Verify import handler validates file format and rejects malformed or malicious input | Malformed JSON/corrupt file triggers graceful error, not crash or data corruption | Negative test cases with crafted files |
| Notification Permission | Verify POST_NOTIFICATIONS permission is requested correctly (Android 13+) and gracefully handles denial | App does not crash on denial; reminders are silently disabled with user-visible feedback | Manual + permission revocation test |
| Intent Handling | Verify exported intents (quick check-in from notification action, widget tap) cannot be invoked by third-party apps without expected context | Broadcast receivers / pending intents use explicit targeting or signature-level protection | Static analysis + manual ADB intent injection |
| Dependency Vulnerability Scan | Scan third-party dependencies (Koin, Room, Glance, Compose) for known CVEs | No critical or high CVEs in dependency tree at time of release | OWASP Dependency-Check, GitHub Dependabot |
| ProGuard / R8 Obfuscation | Verify release build applies R8 minification, reducing reverse-engineering surface | Release APK classes are obfuscated; no internal package paths exposed in decompiled output | APKTool / jadx decompile inspection |
| No Hardcoded Secrets | Confirm no API keys, tokens, or credentials are hardcoded in source | Zero secrets detected in source scan (N/A if no external services used — confirm via open questions) | Trufflehog / git-secrets on repository |

---

### 17.4 Reliability Testing

**Applicability: HIGH — Required**

As a daily-use productivity app, reliability directly impacts retention KPIs. A single ANR or notification miss erodes user trust.

| Test Area | Description | Acceptance Criteria |
|---|---|---|
| Extended Usage Stability | Run app continuously for 24 hours with automated UI interactions (Monkey or Espresso stress) | Zero crashes, zero ANRs after 24-hour Monkey run with 10,000 events |
| Reminder Reliability Under Doze | Schedule habits reminders and subject device to Doze mode; verify delivery | All scheduled reminders deliver within acceptable window; no silent drops without OS-level explanation |
| Widget Reliability on OS Upgrade | Simulate OS upgrade path (Android 12 → 13 → 14); verify widget survives upgrade | Widget displays correctly and remains functional after simulated upgrade |
| Database Integrity After Force-Close | Force-stop app mid-write operation; verify Room WAL ensures no corruption | App reopens cleanly; data integrity maintained; no corrupted state |
| Backup/Restore Round-Trip | Export full data, wipe app data, reimport; verify 100% data fidelity | All habits, tasks, categories, HabitStatus records, streaks, and order indices restored exactly |
| Streak Calculation Correctness on Date Change | Verify streak logic at midnight boundary (day rollover) | Streak does not reset prematurely or extend incorrectly across midnight; timezone handling verified |
| Memory Leak Detection | Run LeakCanary in debug builds across all major screens and navigation flows | Zero memory leaks detected on Habits, Analytics, Tasks, Settings, and Widget update flows |

---

### 17.5 Availability and Health Check Testing

**Applicability: LIMITED — Applicable only for distribution channel health**

There is no server backend. However, the following availability concerns apply:

| Area | Test | Acceptance Criteria |
|---|---|---|
| F-Droid / IzzyOnDroid Build Reproducibility | Verify the reproducible build process produces a binary that F-Droid can index | F-Droid build succeeds; app metadata (version, permissions) matches Play Store declaration |
| GitHub Release Artifact | Verify each GitHub release tag produces a correctly signed APK | APK signature verifiable; SHA256 checksum matches release notes |
| Weblate Translation Sync | Verify translation strings exported from Weblate do not break app compilation or introduce missing-key crashes | App compiles and launches successfully after pulling latest Weblate strings |
| Web Demo (WASM) | Verify shub39.github.io/Grit loads and renders shared KMP business logic | Web demo loads within 5 seconds; shared models render without JS console errors |

---

### 17.6 Observability and Logging Validation

**Applicability: MEDIUM — Required for debug builds; restricted in release builds**

| Test Area | Description | Acceptance Criteria |
|---|---|---|
| Debug Logging | Verify Logcat output provides sufficient diagnostic information for crash investigation | All key lifecycle events (habit creation, check-in, streak calculation, reminder scheduling) are logged at DEBUG level in debug builds |
| Release Logging | Verify no verbose or sensitive logs are emitted in release builds | No user-entered habit/task content appears in Logcat on release build |
| Crash Reporting Integration | Confirm whether Firebase Crashlytics or equivalent is integrated (see Open Questions) | If integrated: crash reports are structured and actionable. If not integrated: document manual crash investigation process |
| Widget Update Logs | Verify Glance widget update events are traceable | Widget update triggers and completion events are logged; failures produce actionable error messages |
| ANR Detection | Verify no main thread blocking operations exist | Zero ANR dialogs during standard testing; StrictMode enabled in debug builds |

---

## 18. Risks and Mitigations

| Risk ID | Risk Description | Impact | Probability | Mitigation Strategy | Owner |
|---|---|---|---|---|---|
| R-01 | **Incomplete or ambiguous requirements for streak edge cases** — The Epic defines that a "missed applicable day resets streak" but does not specify behavior for timezone changes, system clock manipulation, or DST transitions. Test cases may be designed against incorrect assumptions. | High — Streak logic is a core P0 feature; incorrect tests will not catch real bugs | High | Raise open questions (see Section 21) with the assignee. Document assumed behavior explicitly in test cases. Add specific test scenarios for clock manipulation and timezone edge cases once clarified. | QA Lead / Assignee |
| R-02 | **No dedicated test environment — testing on personal devices only** — As a solo-maintained open-source project, there is no managed device farm or CI-connected physical device pool. Test coverage across Android versions (8.0 through 15) and OEM skins may be incomplete. | High — Device-specific bugs (OEM notification channels, Glance widget rendering on MIUI/OneUI) may escape to production | Medium | Prioritize testing on Android 12 (Dynamic Theming baseline), Android 13 (per-app language), and Android 14/15 (latest). Use Firebase Test Lab or BrowserStack App Automate for automated runs across device matrix. Document tested device/OS combinations. | QA Lead |
| R-03 | **Glance widget API instability** — The Epic explicitly identifies this as a known project risk. Jetpack Glance is still maturing; breaking changes in minor releases have historically occurred. | High — Widget is a P0 feature (F-11); breakage directly impacts user experience and Play Store ratings | Medium | Pin Glance dependency version in `build.gradle`; monitor Glance release notes as part of sprint review. Maintain a dedicated widget regression suite that runs on every build. | Developer / QA |
| R-04 | **Solo maintainer bandwidth** — The Epic acknowledges this as a High risk. Bug fixes and test environment setup may be delayed, blocking QA activities and defect resolution. | High — Extended defect resolution time can delay release and miss KPI targets | High | Prioritize test automation to reduce manual regression burden. Label QA-actionable items clearly in GitHub Issues. Escalate blockers to the assigned maintainer within 24 hours via GitHub Discussions. | QA Lead / Assignee |
| R-05 | **Backup/restore data fidelity across schema versions** — Room schema migrations and future schema changes may cause silent data loss or corruption during import of backups created by older app versions. | High — Data loss is irreversible; users have no other recovery mechanism given no cloud sync | Medium | Test backup import with files generated by previous schema versions. Validate all Room migration scripts as part of test scope. Maintain a schema version compatibility matrix. | Developer / QA |
| R-06 | **Notification delivery unreliability on restricted OEMs** — Manufacturers such as Xiaomi (MIUI), Huawei (EMUI), and OnePlus (OxygenOS) apply aggressive battery optimization that can silently kill background processes and suppress AlarmManager triggers, breaking F-05 and F-08 reminders. | High — Reminder failure is a silent bug; users may not notice until habits are missed consistently | High | Test reminder delivery explicitly on at least one MIUI and one Samsung OneUI device. Document OEM-specific workarounds (e.g., battery optimization whitelist prompts). Consider adding an in-app prompt guiding users to whitelist the app. | QA Lead / Developer |
| R-07 | **Weblate translation strings introduce runtime crashes** — Missing translation keys, malformed pluralization rules, or RTL marker characters in string resources can cause `MissingFormatArgumentException` or layout breaks at runtime for non-English locales. | Medium — Affects non-English users; not caught by English-only test runs | Medium | Add automated string resource validation to CI pipeline (e.g., lint checks for missing keys). Test at minimum English, a RTL language (Arabic or Hebrew), and one non-Latin script (e.g., Hindi) before each release. | QA Lead / Translator Community |
| R-08 | **Test data reset complexity** — Each test run requires precise control of HabitStatus records, streak history, and system date to reproduce streak, heatmap, and analytics scenarios. Manual test data setup is time-consuming and error-prone; incorrect data setup produces false-positive passes. | Medium — Incorrect test data causes missed defects and unreliable results | Medium | Define a seeding script or ADB-based test data setup procedure. Create a dedicated `TestDataFactory` in the shared test module. Document exact database state required per test scenario. | QA Lead |
| R-09 | **KMP / WASM web demo divergence** — Shared business logic compiled to WASM may exhibit different behavior from Android runtime due to Kotlin/JS and WASM-specific serialization nuances, particularly for `LocalDate`, `LocalDateTime`, and `DayOfWeek` multiplatform types. | Medium — WASM demo is a showcase, but business logic bugs exposed only in WASM may indicate latent Android bugs | Low | Include at least smoke-level validation of shared domain logic (streak calculation, analytics aggregation) via WASM demo as a secondary verification channel. Treat WASM failures as investigation triggers, not blocking defects. | Developer / QA |
| R-10 | **Material You Dynamic Theming untestable on Android < 12** — F-09 specifies wallpaper palette on Android 12+; custom color picker for older versions. If the conditional logic is incorrectly implemented, Android 11 users may see a broken or empty theming screen. | Medium — Affects all users on Android 11 and below | Medium | Maintain a dedicated Android 11 device (or emulator API 30) in the test matrix. Explicitly test the theming code path for both Android 12+ (dynamic palette) and sub-12 (custom picker) branches. | QA Lead |
| R-11 | **GPLv3 dependency compliance** — Third-party libraries used (Koin, Room, Compose, SQLDelight) must be license-compatible with GPLv3. An incompatible dependency inclusion could create legal risk for the open-source project. | High — Legal/compliance risk for open-source distribution on F-Droid (which enforces free software compliance) | Low | Run a license audit (e.g., `licensee` Gradle plugin) as part of the release checklist. QA to verify the LICENSES file and `build.gradle` dependency declarations are consistent. | Developer / QA Lead |
| R-12 | **Drag-and-drop reorder persistence** — The Compose Reorderable library used for both category reordering (F-06) and task reordering (F-07) must persist the updated `index` field to Room after every drag event. A missed persistence call would result in silent order resets on app restart. | High — Data ordering loss is a UX regression that may not be immediately noticed by users | Medium | Include explicit test scenarios that restart the app after reordering and verify persisted order. Test rapid drag sequences (5+ items reordered in quick succession) to detect race conditions in index persistence. | QA Lead |

---

## 19. Assumptions

The following assumptions have been made in the absence of explicit specification in the Epic or child issues. All assumptions must be reviewed and confirmed by the assignee (Sourav Das Mahapatra) before test execution begins.

1. **Minimum Android SDK Version is API 26 (Android 8.0).** The Epic does not state a `minSdk`. This is assumed based on common Jetpack Compose + Room + Kotlin Coroutines support matrices. All test environment planning is based on this assumption. If `minSdk` is lower, additional regression scenarios for older API behaviors (e.g., notification channels pre-O) will be required.

2. **The app does not integrate any crash reporting or analytics SDK (e.g., Firebase Crashlytics, Google Analytics).** The Epic explicitly states "no ads" and emphasizes privacy; no analytics integration is mentioned. Testing will not include validation of analytics event firing. If a crash reporting SDK is present, additional data-privacy validation will be added to Section 17.3.

3. **Habit reminders are implemented via AlarmManager with exact alarm support, not WorkManager alone.** This is assumed because WorkManager's inexact timing is unsuitable for user-facing time-sensitive reminders. If WorkManager is used, battery optimization and Doze mode test cases must be adjusted accordingly.

4. **The backup/restore format is JSON.** The Epic states "export/import all data" but does not specify the serialization format. JSON is assumed given the use of Kotlin Serialization (implied by "Serializable model for backup/restore" in F-01). If a different format (e.g., SQLite dump, CSV, ZIP archive) is used, test data and file validation scenarios must be updated.

5. **Streak calculation uses the device's local timezone and does not account for cross-timezone travel scenarios.** Streak logic will be tested against the device timezone only. Cross-timezone streak preservation is treated as out of scope unless explicitly raised as a requirement.

6. **The "quick check-in from notification action button" (F-05) does not require the user to open the app.** The notification action directly invokes a BroadcastReceiver or PendingIntent that marks the habit complete and updates the Glance widget. If the app is required to open for check-in, the corresponding test scenarios will be revised.

7. **The Glance widget supports only "today's habits" display and does not support individual habit completion from a list widget; instead it shows a summary or individual tappable items based on Glance API capability.** The exact widget layout (list widget vs. collection widget) is not specified. Testing will cover both a single-habit widget and a list-style widget scenario; the correct variant will be confirmed before test design is finalized.

8. **All Room database migrations are scripted and versioned in the codebase.** Testing assumes that schema migration scripts exist for any schema version increments. If migration scripts are missing, destructive migration behavior (data wipe on upgrade) must be explicitly tested and documented.

9. **The "custom color picker for older versions" in F-09 is a predefined palette selector, not a freeform HSL/RGB picker.** This assumption affects the number of test cases required for theming. If a freeform color picker is implemented, additional boundary and input validation tests for hex color codes or RGB value ranges will be added.

10. **The WASM web demo shares domain/business logic (streak calculation, analytics aggregation) but does not share UI components with the Android app.** Testing of WASM is limited to smoke-level validation of the deployed demo at `shub39.github.io/Grit`. Full functional equivalence testing between Android and WASM is out of scope for this test plan.

---

## 20. Dependencies

### 20.1 Requirement Dependencies

| Dependency ID | Type | Description | Required By | Status |
|---|---|---|---|---|
| DEP-R-01 | Requirement | Finalized streak reset logic specification (edge cases: missed day definition, timezone, DST, clock skew) | F-03 test design | Open — see OQ-01 |
| DEP-R-02 | Requirement | Confirmed backup/restore file format specification (JSON schema or equivalent) | F-12 test design | Open — see OQ-04 |
| DEP-R-03 | Requirement | Confirmed Glance widget layout design (list widget vs. summary widget; number of habits shown) | F-11 test design | Open — see OQ-05 |
| DEP-R-04 | Requirement | Confirmation of minimum supported Android SDK version | All environment setup | Open — see OQ-02 |
| DEP-R-05 | Requirement | Task reminder recurrence model — F-08 states "one-time datetime" but confirmation needed that no recurring task reminders exist | F-08 test design | Open |

### 20.2 Environment Dependencies

| Dependency ID | Type | Description | Required By | Status |
|---|---|---|---|---|
| DEP-E-01 | Environment | Physical Android device running Android 12 (API 32) for Dynamic Theming validation | F-09 testing | To be provisioned |
| DEP-E-02 | Environment | Physical Android device running Android 13 (API 33) for per-app language picker (F-10) | F-10 testing | To be provisioned |
| DEP-E-03 | Environment | Physical Android device running Android 11 or below (API 30) for fallback theming (F-09) | F-09 testing | To be provisioned |
| DEP-E-04 | Environment | OEM device (MIUI or Samsung OneUI) for notification delivery reliability testing (F-05, F-08) | Non-functional testing | To be provisioned |
| DEP-E-05 | Environment | Android Emulator images for API 26, 28, 30, 31, 33, 34, 35 for broad API coverage | All functional testing | To be configured |
| DEP-E-06 | Environment | Firebase Test Lab or BrowserStack App Automate account for device matrix automation | Automation strategy | Pending access setup |
| DEP-E-07 | Environment | CI/CD pipeline (GitHub Actions) access and secrets configuration for automated test execution | Automation / regression | Pending configuration |

### 20.3 Build and Code Dependencies

| Dependency ID | Type | Description | Required By | Status |
|---|---|---|---|---|
| DEP-B-01 | Build | Debug APK with LeakCanary and StrictMode enabled | Reliability testing | Developer to provide |
| DEP-B-02 | Build | Release APK (signed) for ProGuard/R8 and security validation | Security testing | Developer to provide |
| DEP-B-03 | Build | Room migration scripts for all schema versions present and tested with `MigrationTestHelper` | Backup/restore testing | Developer to confirm |
| DEP-B-04 | Build | Instrumented test module with `TestDataFactory` or equivalent seeding utilities | All instrumented tests | Developer / QA to create |

### 20.4 Data Dependencies

| Dependency ID | Type | Description | Required By | Status |
|---|---|---|---|---|
| DEP-D-01 | Test Data | Pre-seeded database states for streak scenarios (0-day, 7-day, 30-day, 365-day streaks) | F-03, F-04 testing | QA to create via seeding scripts |
| DEP-D-02 | Test Data | Sample backup files: valid (current schema), valid (previous schema version), malformed JSON, empty file, oversized file | F-12 testing | QA to author manually |
| DEP-D-03 | Test Data | Full 1-year HabitStatus dataset (365 × 20 habits = 7,300 rows) for analytics performance testing | Section 17.1 performance testing | QA to generate via script |

### 20.5 Third-Party and Distribution Dependencies

| Dependency ID | Type | Description | Required By | Status |
|---|---|---|---|---|
| DEP-T-01 | Third-Party | Weblate translation platform — access to pull latest translated strings before localization testing | F-10 testing | Weblate project access required |
| DEP-T-02 | Third-Party | F-Droid / IzzyOnDroid build infrastructure — reproducible build validation | Section 17.5 availability testing | F-Droid build trigger by maintainer |
| DEP-T-03 | Third-Party | Google Play internal test track — APK upload for Play-specific permission and notification behavior testing | F-05, F-08 testing | Google Play Console access |

### 20.6 Team and Process Dependencies

| Dependency ID | Type | Description | Required By | Status |
|---|---|---|---|---|
| DEP-P-01 | Process | Defect triage availability from assignee (Sourav Das Mahapatra) — minimum 2× per week | All testing phases | Ongoing |
| DEP-P-02 | Process | Open question resolution (see Section 21) — minimum answers within 5 business days of test plan approval | Test case design | Pending |
| DEP-P-03 | Process | GitHub Issues access for defect logging; label taxonomy agreed (bug, enhancement, question, good first issue) | Defect management | To be confirmed |

---

## 21. Open Questions

The following questions must be resolved before or during testing to ensure accurate test case design and avoid wasted effort on incorrect assumptions.

| OQ ID | Question | Impact if Unresolved | Raised By | Target Resolution Date | Status |
|---|---|---|---|---|---|
| OQ-01 | **Streak reset edge cases:** What is the exact definition of a "missed applicable day" for streak reset purposes? Specifically: (a) If a user checks in at 11:58 PM on an applicable day, then the device clock is adjusted forward past midnight — is the streak preserved? (b) How does the app handle DST transitions (23-hour days and 25-hour days)? (c) Is a future-scheduled habit that the user has not yet had the opportunity to complete (e.g., it is 9 AM and the habit reminder is set for 8 PM) treated as "missed" if the user force-closes the app? | High — Streak is a P0 feature. Incorrect streak logic will cause user complaints and Play Store rating damage. Without clear spec, test cases may validate wrong behavior. | QA Lead | TBD | Open |
| OQ-02 | **Minimum supported Android SDK version:** What is the `minSdk` declared in the app's `build.gradle`? Is Android 8.0 (API 26) the floor, or is it lower/higher? This affects the size of the device test matrix and which Android API behaviors must be validated. | Medium — Affects test environment provisioning and scope of OS-version-specific tests (notification channels, exact alarms permission, per-app language) | QA Lead | TBD | Open |
| OQ-03 | **Crash reporting and analytics:** Does the app integrate any third-party SDK for crash reporting (e.g., Firebase Crashlytics), usage analytics (e.g., Countly), or rating prompts (e.g., In-App Review API)? If so, what data is collected, and does it require explicit user consent under GDPR? | Medium — Privacy-focused users and F-Droid policy may reject apps with proprietary analytics SDKs. Testing scope and privacy validation depends on this answer. | QA Lead | TBD | Open |
| OQ-04 | **Backup/restore file format and schema versioning:** What is the exact file format for export (JSON, ZIP+JSON, SQLite copy)? What happens when a user imports a backup file created by an older version of the app with a different Room schema version? Is there a version field in the backup manifest? Does the app perform forward migration of the backup data? | High — Without this, F-12 test cases cannot be designed for cross-version compatibility. Data loss risk is highest during version upgrades. | QA Lead | TBD | Open |
| OQ-05 | **Glance widget layout and behavior:** What is the exact widget layout? Does it show a scrollable list of all today's habits (Glance `LazyColumn`)? Or a fixed summary card? What happens when there are more habits than fit in the widget? Does tapping an individual habit in the widget mark only that habit complete, or does it open the app? Is the widget resizable? | High — Widget is a P0 feature (F-11). Test scenarios for widget interaction, overflow handling, and tap behavior cannot be designed without this specification. | QA Lead | TBD | Open |
| OQ-06 | **Per-app language picker behavior on Android 12 and below:** F-10 states "Android 13+ in-app language picker." On Android 12 and below, is there a custom in-app locale switcher, or is language selection entirely delegated to system settings? Does the app gracefully degrade, or does the language picker UI simply disappear? | Medium — Affects F-10 test scope for pre-Android-13 devices. If a custom picker exists for Android 12-, it must be independently tested. | QA Lead | TBD | Open |
| OQ-07 | **Category color implementation status:** The Epic explicitly states "category color in UI" is Out of Scope. However, the `Category` data model includes a `color (String, reserved for future use)` field. Should QA validate that: (a) the color field is persisted correctly to Room even though it is not displayed, (b) the backup/restore round-trip preserves the color field, and (c) no UI element accidentally renders or references the color field? | Low-Medium — Determines whether color field persistence and backup fidelity are in scope for this test cycle. | QA Lead | TBD | Open |
| OQ-08 | **Exact alarm permission handling on Android 12+:** Android 12 introduced `SCHEDULE_EXACT_ALARMS` permission, which can be revoked by the user. Android 13 introduced `USE_EXACT_ALARM`. Does the app request the correct permission for habit and task reminders? What is the fallback behavior if exact alarm permission is denied — does the app fall back to inexact alarms, disable reminders silently, or show a user-facing prompt? | High — Silent reminder failure is a critical UX defect. This must be explicitly tested on Android 12 and 13 devices. | QA Lead | TBD | Open |
| OQ-09 | **RTL layout testing scope:** F-10 mentions RTL layout support. Which specific screens and components have been explicitly designed for RTL? Are there any known RTL layout issues deferred to a later release? Which RTL language should be prioritized for testing (Arabic, Hebrew, Farsi)? | Medium — Without this, RTL testing scope cannot be bounded; testing all screens in RTL may exceed available time. | QA Lead | TBD | Open |

---

## 22. Approval / Sign-Off

This section records the formal review and approval of the Grit Android Test Plan. All designated approvers must sign off before test execution begins on any phase beyond smoke testing.

| # | Name | Role | Organization / Team | Approval Status | Signature / Confirmation | Date | Comments |
|---|---|---|---|---|---|---|---|
| 1 | Sourav Das Mahapatra | Product Owner & Lead Developer | Grit Open Source Project | ⬜ Pending | | | Primary approver; must confirm all open questions in Section 21 and validate assumptions in Section 19 before sign-off |
| 2 | [QA Lead Name — TBD] | QA Lead / Test Architect | QA Team | ⬜ Pending | | | Authored this test plan; responsible for confirming scope boundaries, risk register, and automation strategy are executable within sprint capacity |
| 3 | [Co-Maintainer Name — TBD] | Technical Reviewer / Co-Maintainer | Grit Open Source Project | ⬜ Pending | | | To be assigned per Epic risk mitigation (Risk R-04: recruit co-maintainers); reviews technical architecture alignment in Sections 5 and 17 |
| 4 | [Community Contributor — TBD] | Localization / F-Droid Representative | Weblate / F-Droid Community | ⬜ Pending | | | Optional reviewer; recommended for F-10 localization scope and F-Droid reproducible build validation in Section 17.5 |

### Sign-Off Status Legend

| Status | Symbol | Meaning |
|---|---|---|
| Pending | ⬜ | Review not yet started |
| In Review | 🔄 | Document under active review; feedback in progress |
| Approved with Comments | 🟡 | Approved subject to minor documented revisions |
| Approved | ✅ | Unconditional approval granted |
| Rejected | ❌ | Material deficiencies identified; document must be revised and re-reviewed |

### Revision Trigger Conditions

The following events require this test plan to be revised, re-circulated, and re-approved:

1. Any open question in Section 21 receives an answer that materially changes feature behavior assumptions (particularly OQ-01, OQ-04, OQ-05, OQ-08).
2. A new child issue is added to Epic SCRUM-6 that introduces a feature or integration not covered by the current scope.
3. The minimum SDK version is confirmed to be different from the assumed API 26.
4. A third-party analytics or crash reporting SDK is confirmed to be integrated (impacting Section 17.3 and Section 