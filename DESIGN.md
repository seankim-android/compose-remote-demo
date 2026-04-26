# DESIGN.md

The visual and interaction design spec for compose-remote-demo. Hand this to Figma Make, Figma AI, or a human designer. The goal is a real, opinionated editorial product, not a generic Material 3 sample, and the design has to make server-driven UI legible — including navigation, which is a first-class server concept here.

## Product context

A Ktor server emits binary Compose Remote documents over HTTP. The Android client is a thin renderer that fetches bytes, draws them, and routes user actions back through the server. Three home variants share one route, `/screens/home?variant=brief|sparse|catalog`, and a detail route `/screens/item?id=N` is reached only by tapping into the Catalog. Every layout, including the detail page, is authored on the server. The Android app ships zero hard-coded screens.

Reference points: Stratechery's web reader, Bloomberg's Brief section, the New York Times Now app circa 2017. Calm, dense where it earns it, never decorative for its own sake.

## What to design

Six screens at iPhone 14 Pro size (393 x 852). They live together in one Figma file as a single flow.

1. Home / Brief variant (default)
2. Home / Sparse variant (a quiet day)
3. Home / Catalog variant (six headline rows)
4. Item detail (reached by tapping a Catalog row)
5. Inspector bottom sheet, opened over Brief
6. Loading state (no bytes yet)

All screens share the same top app bar and, where applicable, the same variant picker.

## Shared chrome

- Top app bar, single line, ~64 dp.
  - Title: `Brief` in the brand display face on home; the section label (e.g. `Item 3`) on detail screens.
  - Leading: nothing on home. Back chevron on every non-home screen.
  - Trailing: 24 dp info icon that opens the inspector. Incidental, not decorative.
- Variant picker directly below the top bar, only on home.
  - Three segments: Brief, Sparse, Catalog. Single choice, pill row.
  - Active segment uses a tonal container, leading checkmark.
- Page background: paper white in light mode, deep ink in dark mode.

## Variant 1: Brief (home, default)

The reading state.

1. Eyebrow `BRIEF`, all caps, tracked +0.08 em.
2. Section label: `Today`.
3. Meta: `3 new since yesterday`. Muted.
4. Hero block, edge to edge minus 16 dp gutters, 16:9, solid accent fill, 12 dp corners.
5. Headline: `Featured release`.
6. Deck: `A short summary of the headline release for today.`
7. Primary CTA: `Read featured`. Filled, 48 dp, full width minus gutters, trailing arrow.
8. Secondary CTA: `Save for later`. Tonal, smaller, anchored right of the primary.

## Variant 2: Sparse (home)

Deliberately quiet.

1. Eyebrow `BRIEF`.
2. Section: `Quiet day`.
3. Body: `Nothing new since yesterday.`
4. Single CTA, centered, tonal: `Catch me up`.

No hero, no list, no decorative line art. Whitespace is the design.

## Variant 3: Catalog (home)

Dense list of six items. Glanceability over delight.

1. Eyebrow `BRIEF`.
2. Section: `Catalog`.
3. Six rows. Each row has a 1 dp accent rule above, an 18 sp item title, a one-line muted note, and a trailing 20 dp chevron. The whole row is tappable and navigates to the item detail screen.
4. Footer text CTA: `Refresh`. Centered.

Resist adding per-item CTAs beyond the row tap.

## Variant 4: Item detail

Reached only by tapping a Catalog row. Server-emitted, not client-built.

1. Top app bar with back chevron and title `Item N`.
2. Eyebrow: the item's source or category, e.g. `RELEASES`.
3. Headline: full item title, display face, two lines max.
4. Byline + timestamp, muted small caps.
5. Body block: 4 to 6 paragraphs of placeholder copy. Real product would be the article body.
6. Primary CTA: `Mark read`.
7. Secondary CTA: `Open in source`.

Back chevron returns to the previous home screen with the same variant selected. The picker state survives navigation.

## Inspector bottom sheet

Modal, fully expanded, scrim above the page.

1. Drag handle.
2. Title: `Wire payload`.
3. Four labeled fields, label muted small caps:
   - URL
   - Size, e.g. `601 bytes`
   - Round trip, e.g. `30 ms`
   - First 256 bytes, monospaced, eight bytes per line.
4. Dismiss is scrim tap or swipe. No close button.

The inspector is a debug surface. It should look like Xcode or a Chrome devtools panel, not the rest of the app.

## Loading state

- Top app bar visible, styled like the other states.
- Picker visible if on home.
- Centered single line below: `Loading…`. No spinner, no skeletons.

## Server-driven actions and navigation

This is the part most SDUI demos skip. Actions are not encoded in the client; they are encoded in the bytes the server sends.

### Action model

Compose Remote supports two callback shapes:

- `HostAction(id: Int)` — a numeric id the client interprets. Lightweight. Best for app-level intents that the client already knows about (open inspector, mark read locally).
- `NamedAction(name, payload)` — a string name plus arbitrary payload. Best for navigation and analytics, because the server can introduce new destinations without a client release.

We use both. Numeric ids for in-app intents. Named actions for navigation, because navigation is the canonical server-driven concern.

### Action map

| Source | Action shape | Meaning |
|---|---|---|
| Brief / `Read featured` CTA | `HostAction(1001)` | Client logs intent, shows snackbar. Stand-in for "open featured". |
| Brief / `Save for later` CTA | `HostAction(1002)` | Client toggles a local saved flag, shows snackbar. |
| Sparse / `Catch me up` CTA | `HostAction(1003)` | Client switches the variant picker to Catalog. Pure UI state, no server hit. |
| Catalog / row tap (item N) | `NamedAction("navigate", "/screens/item?id=N")` | Client pushes a new screen that fetches the URL and renders it. |
| Catalog / `Refresh` text CTA | `HostAction(1004)` | Client triggers the same code path as pull-to-refresh. |
| Item detail / `Mark read` | `HostAction(2001)` | Client pops the back stack and shows a snackbar. |
| Item detail / `Open in source` | `NamedAction("open-url", "https://example.com/item/N")` | Client fires an intent to the system browser. |

The design must make these mappings legible. Annotate each CTA in the Figma file with a small inline comment noting the action shape and value. The README frame at the top of the file restates the table.

### Navigation rules

- The Android client treats `NamedAction("navigate", url)` as the only way to open a new screen. Hard-coded routes are not allowed.
- Back navigation is client-owned. The server does not emit `pop`. The client back stack is what got us here.
- The variant picker state is preserved across detail navigation. Coming back from `Item 3` keeps the user on Catalog.
- The inspector is per-screen. Opening it on the detail page shows the detail bytes, not the home bytes.

## Visual system

Define these as Figma variables / tokens. Make them shippable, not placeholders.

### Color (light)

- `bg/page` paper white
- `bg/elevated` cooler white for sheet and inspector
- `text/primary` near black
- `text/muted` 60% black
- `accent` editorial blue, not Material default
- `accent/on` text color on accent
- `divider` thin warm gray, 1 dp

### Color (dark)

Mirror with appropriate inversions. Accent shifts for ink contrast.

### Typography

- Display: serif with strong italic. Recommend Source Serif 4.
- Body: humanist sans. 16 sp default, 22 sp line height.
- Mono: fixed face for inspector hex. Recommend JetBrains Mono.
- Sizes: eyebrow 12 sp, section label 14 sp, headline 18 sp, deck 16 sp, hero label 24 sp.

### Spacing and grid

- 4 dp base unit.
- 16 dp side gutters on phone.
- Vertical rhythm: 8, 12, 16, 24, 32, 48.

### Components

Build these as reusable Figma components with variants:

- TopAppBar (home, detail)
- VariantPicker (3 segments, single choice)
- HeroBlock (filled, image)
- HeadlineBlock (title + deck)
- CatalogRow (default, last)
- PrimaryCTA, SecondaryCTA, TextCTA
- InspectorSheet
- LoadingState
- DetailBody

## Deliverables

- The six screens above, each a separate Figma frame.
- A components page with every reusable element.
- A tokens page with color, typography, and spacing variables.
- A README frame at the top of the file restating the action map and navigation rules.

## Things to avoid

- Stock photography in the hero. Solid color is fine.
- Drop shadows beyond the bottom sheet elevation. Editorial product.
- More than one accent color.
- Skeleton loaders. Loading state is intentionally plain.
- Em dashes in any user-facing copy. Use commas, parens, or split sentences.
- Client-owned navigation routes. All destinations come from the server bytes.
