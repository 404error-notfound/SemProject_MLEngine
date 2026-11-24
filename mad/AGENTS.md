# Task: Redesign the fake news detector Android app and add quality-of-life features

You are working on an existing Android app that checks whether a social media post or forwarded message is likely false. The backend already works. Your job is to redesign the UI, add dark mode, and add the features below, without breaking the existing backend integration.

## 0. Before you write any code

1. Explore the repo. Identify the language, UI toolkit (I expect Kotlin and Jetpack Compose with Material 3, but verify), navigation, DI, networking layer, and any existing persistence.
2. Find the existing API client and its request/response models. Do NOT change the backend contract. Write down what the response contains (verdict, confidence, reasons, sources, detected language, etc.) and map it to the UI below. If a field I describe does not exist, derive it client-side or hide that UI element, and list the gap in your final report.
3. Run the build and existing tests so you know the baseline.
4. Post a short plan (phases, files you expect to touch) and then proceed. Do not stop to ask unless something blocks you.

Reference screenshots of the current app and the target design are in `docs/design/` (if empty, ask me to add them). Follow the target design closely.

## 1. Design system

Material 3. No emoji as icons anywhere; use Material icons (outlined/rounded). Follow the system light/dark setting (`isSystemInDarkTheme`). No manual theme toggle in v1 except a "Theme: System / Light / Dark" row in Settings.

### Colour tokens (define as a light and a dark ColorScheme plus a custom `VerdictColors` extension)

| Role | Light | Dark |
|---|---|---|
| background | `#FDF7FF` | `#141218` |
| surface container (cards, bottom sheet) | `#F3EDF7` | `#211F26` |
| nav bar | `#F3EDF7` | `#211F26` |
| on-surface | `#1D1B20` | `#E6E0E9` |
| on-surface-variant (secondary text) | `#49454F` | `#CAC4D0` |
| outline | `#79747E` | `#938F99` |
| primary | `#6750A4` | `#D0BCFF` |
| on-primary | `#FFFFFF` | `#381E72` |
| secondary container (selected chip, nav pill) | `#E8DEF8` | `#4A4458` |
| verdict false: container / on-container / accent | `#F9DEDC` / `#410E0B` / `#8C1D18` | `#601410` / `#F9DEDC` / `#F2B8B5` |
| verdict unverified: container / on-container / accent | `#FFE8B3` / `#3A2600` / `#6B4500` | `#4A3500` / `#FFE8B3` / `#F5C46A` |
| verdict true: container / on-container / accent | `#D8F0DF` / `#0F3D22` / `#14532D` | `#0F3D22` / `#D8F0DF` / `#8FD9A8` |

Never use pure black. Verdicts must always show an icon AND a text label, never colour alone (false = close/X icon, unverified = info icon, true = check icon).

### Shape scale (use everywhere, as a Material `Shapes` object, no ad-hoc radii)

- 8dp: chips and filter chips
- 12dp: text fields
- 16dp: cards and list rows
- 24dp: buttons (fully rounded), verdict cards
- 28dp: top corners of bottom sheets
- The app window itself: use edge-to-edge with the system's own rounded corners; do not draw a fake frame.

### Type and spacing
Roboto / system default. Screen title 32/40, section title 16/24 medium, body 16/24, secondary 14/20, caption 12/16. 16dp screen margins, 8dp base spacing unit, minimum 48dp touch targets. Body text must meet WCAG AA contrast in both themes.

## 2. Screens to build

**Bottom navigation:** two destinations, Check and History, Material 3 NavigationBar with an icon pill on the selected item. Detail screens (Result) hide the bottom bar and use a top app bar with a back arrow.

1. **Check (empty)**: Title "Check a post". Helper text "Paste a claim, or share it to this app from WhatsApp, X or Telegram." Outlined multi-line field (placeholder "Post text (English, Swahili or Sheng)"), "Paste from clipboard" text button, character counter (max 2000). Buttons: Check (disabled until the field is non-blank) and Clear. Below, a tonal card "Skip the copy and paste" explaining the share route.
2. **Check (ready)**: field is focused, a "Swahili detected" (or English / Sheng) chip appears under it, Check is enabled with a search icon.
3. **Checking**: replaces the Check screen content in place. Shows the post (3-line clamp), a thin progress bar, and a step list: Reading the post, Spotting the claim, Searching trusted sources, Comparing what we found, Writing your result. If the backend gives no real progress events, advance the steps on a timer purely as a visual, but never mark the last step done until the response arrives. Cancel button must cancel the in-flight request.
4. **Result**: top app bar "Result". Verdict card (icon, "VERDICT" label, verdict name, confidence label "High / Medium / Low confidence" plus a 5-segment meter with text, not colour alone). One-sentence summary. The checked post in a collapsed card with its language. "Why we think so" list of reasons. "Related fact-checks" list (open in a Custom Tab). Sticky bottom bar: "Share a correction" (primary) and "Check another". The unverified variant replaces "Why we think so" with "What we found" and adds a "What you can do" list, and its share button reads "Share a caution".
5. **History**: title, overflow/clear icon, filter chips (All, Likely false, Unverified, Likely true), list grouped by Today / Yesterday / Earlier. Each row: verdict pill, relative time, 2-line post snippet, chevron. Tapping opens the stored result without re-calling the backend. Empty state keeps the current copy: "No checks yet. Posts you check will appear here."
6. **Quick check from share**: see section 3.

## 3. Features to add (in priority order)

**P0, ship these first**
1. **Share target.** Register an `ACTION_SEND` / `text/plain` intent filter so the app appears in the Android share sheet from WhatsApp, X, Telegram and anywhere else. On receipt, auto-start the check.
2. **Quick check sheet.** When launched from a share intent, show the result as a bottom sheet over the previous app (use a translucent activity or a dialog-themed activity) with: "Shared from <source app>" label, verdict card, one-line summary, "See full result", "Share a correction", "Dismiss". The user should get an answer without leaving the chat. "See full result" opens the main app on the Result screen.
3. **Persistent history** using Room (or the project's existing store). Save post text, detected language, verdict, confidence, summary, reasons, sources, timestamp, source app if known. Cap at 500 entries (drop oldest).
4. **Dark mode** per section 1.
5. **Error and offline states** for: no network, timeout (set a sensible timeout, 20s), server error, empty or too-long text. Each has a plain-language message and a Retry button. Never show a stack trace or raw exception.

**P1**
6. **Share a correction.** Generates a short, ready-to-forward message in the post's language (Swahili if the post was Swahili or Sheng, otherwise English), e.g. a verdict line plus one-line reason, with no tracking links. Opens the system share sheet. Keep templates in string resources (`values/` and `values-sw/`) so they can be edited without code changes.
7. **Paste from clipboard** button, plus a dismissible "Paste copied text?" suggestion chip when the app opens and the clipboard holds text (do not read the clipboard silently on Android 12+; only on explicit tap or on the suggestion chip).
8. **History management.** Swipe to delete with an Undo snackbar, a Clear history action with a confirmation dialog, and filter chips. Add a search field only if it is cheap.
9. **Re-check** action on a history item (calls the backend again and replaces the stored result), since facts change.
10. **Language chip** showing the detected language (backend field if present, otherwise a lightweight heuristic or omit).

**P2, only if P0 and P1 are done and stable**
11. **Settings screen** (reached from an overflow menu on Check): Theme (System/Light/Dark), App language (English / Kiswahili), Clear history, a short privacy note ("Posts you check are sent to our server for analysis"), About and version.
12. **First-run onboarding**: one screen, skippable, explaining the share route with a simple illustration. Show once, store the flag.
13. **Localisation**: all user-facing strings in resources with English and Swahili (`values-sw`). Mark any Swahili strings you are not confident about with a `TODO(review-sw)` comment so a native speaker can check them.
14. Haptic tick on result arrival, and a content-description-rich result screen for TalkBack.

## 4. Engineering requirements

- Architecture: single-activity, unidirectional state (ViewModel + StateFlow, sealed UI state per screen: Idle, Checking, Result, Error). Keep the network layer behind a repository interface so UI is testable with fakes.
- Compose previews for every screen in light and dark, for each state.
- Accessibility: TalkBack labels on all icon buttons, logical focus order, verdict announced as text, support font scale up to 200% without clipping, and all touch targets at least 48dp.
- Privacy: do not log post text or results in release builds. Do not add analytics or third-party SDKs.
- Do not hardcode colours, dimensions or strings in composables; use theme tokens and string resources.
- Keep dependencies minimal. If you add one (Room, Navigation, Coil, etc.), say why in the final report.
- Do not modify the backend or its contract. If you believe a backend change would help (for example returning `reasons[]` or `language`), list it as a recommendation instead.
- Commit in small, logically separate commits (theme, navigation shell, check flow, share target, history, etc.).

## 5. Tests

- Unit tests: ViewModel state transitions (idle to checking to result and error), history repository (insert, cap at 500, delete, clear), correction-message generation for English and Swahili.
- At least one UI test for: share intent launches the quick-check sheet; Check is disabled on empty input; history item opens a stored result.
- Run the full test suite and a debug build before declaring done.

## 6. Definition of done

- App builds and all tests pass.
- Both themes render correctly on every screen; no hardcoded colours remain.
- Sharing a post from WhatsApp, X or Telegram shows the quick-check sheet and a verdict.
- History persists across app restarts and survives process death.
- No crash on rotation, no network, or backgrounding mid-check.
- Final report (max one page): what you built per section above, anything you skipped and why, backend gaps or recommendations, and any `TODO(review-sw)` strings.

Work phase by phase (theme and shell, then Check flow, then share target and quick sheet, then history, then P1, then P2). After each phase, build and run the tests, then continue.
