# UI pattern: list-first screens (header + primary action + empty state)

Use this pattern whenever you add a **drawer destination or nested list screen** that shows a **scrollable list** with an **Add / primary action** and sometimes an **empty list**.

## Source of truth (Compose)

Implementation lives in:

- `app/src/main/java/com/example/gearkeeper/ui/patterns/ListScreenPattern.kt`

Consult that file for exact signatures. This document describes **when** and **how** to apply it.

## Layout rules

| Element | Rule |
|--------|------|
| Screen padding | `ListScreenHorizontalPadding` and `ListScreenVerticalPadding` (24.dp each) on the `LazyColumn` (or outer scroll container). |
| Space between cards | `ListScreenSectionSpacing` (12.dp) as `verticalArrangement = Arrangement.spacedBy(...)`. |
| Header title | `MaterialTheme.typography.headlineSmall`, start-aligned, full width. |
| Supporting line | Optional `bodyMedium` + `onSurfaceVariant` when **not** using search (forms, settings). On filterable lists prefer **search** instead of a long subtitle. |
| Search | `SectionListHeader` with `searchQuery`, `onSearchQueryChange`, optional `onSearchSubmit`, `searchPlaceholder`. Typing updates **draft** text only; the list query runs when the user taps the trailing **search** icon (or when you handle `onSearchSubmit`). On resume / navigation, reload using the last **applied** query (see each screen’s `LaunchedEffect` / lifecycle). |
| Primary action | `Button` (filled), **full width** (`fillMaxWidth()`), label in **Title Case**. Placed below subtitle or search field. |
| Empty list message | `SectionEmptyState`: main line `bodyLarge`, optional second line `bodyMedium` + `onSurfaceVariant`, **centered**, full width. |

## Composables

### `SectionListHeader`

- **Always** use for the first `LazyColumn` item on list-first screens.
- Parameters: `title`, optional `supportingText` **or** `searchQuery` + `onSearchQueryChange` + optional `onSearchSubmit` (search wins—supporting text hidden), optional `primaryActionLabel` + `onPrimaryAction`.
- If there is no primary button, omit the label and callback (e.g. read-only hub screens).

### `SectionEmptyState`

- Use as a **dedicated `LazyColumn` item** when the data list is empty (after the header, after `items { ... }` or in place of items).
- First sentence: short, factual (**No … yet.** / **No … right now.**).
- Hint: tells the user what to do, usually referencing **Add … above** or another screen by name.

## Copy tone

- Prefer **parallel structure** across sections: “No X yet.” + “Use Add X above.” or “Add X from the Y screen.”
- Avoid mixing centered headers (old Vehicles) with start-aligned headers (Services); **headers are start-aligned** everywhere.

## Screens that follow this pattern

- **Incoming services** — header only (no global Add); empty state when the list is empty.
- **Vehicles** — header + **Add vehicle** + tonal **Add maintenance** (opens form with vehicle picker); row tool icon logs for that vehicle; empty state when no vehicles.
- **Services (drawer)** — **Service catalog** list (`service_type`); read-only; log maintenance from **Vehicles**.
- **Vehicle maintenance list** — header (vehicle name) + **Add maintenance**; empty state when no visits.
- **Maintenance history** — header only + empty state until real data exists.
- **Settings** — header only + placeholder empty-style message until real toggles exist.

Diagnostics (**Database health**) uses the same **header + full-width primary** shape for “Refresh check” so it feels consistent, even though it is not a typical list.

## Related: simple form screens

- **Add service type** uses the same **screen padding** and **SectionListHeader** (title + supporting text), then fields and a **full-width** primary save button.
- **Maintenance form** reuses the same **horizontal/vertical padding** constants so it lines up with list screens; it does not use `SectionListHeader` (too many field groups).

## When *not* to use it

- **Dialogs** and dense multi-section forms: empty/error states may need `error` color instead of `SectionEmptyState`.
- **Settings** single-column toggles: no list header/empty pattern unless you add a list with empty behavior.

## Adding a new similar screen

1. Open `ListScreenPattern.kt` and reuse `SectionListHeader` / `SectionEmptyState` + padding constants.
2. Use a single `LazyColumn` when possible; first `item { SectionListHeader(...) }`, then `items()`, then `if (list.isEmpty()) { item { SectionEmptyState(...) } }` (or equivalent order per UX).
3. Update this doc with the new screen name in the list above.
