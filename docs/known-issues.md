# Known issues

## Brief CTAs do not register taps (alpha05 / alpha08 / alpha09)

`primaryCta` and `textCta` on the Brief, Sparse, and Item screens render
correctly but never deliver `onAction(id)`. Catalog rows on the Catalog screen
do navigate, so the wiring is correct.

### Repro

1. Run the server (`./gradlew :server:run`).
2. Install and open the Android client.
3. Tap "Read featured", "Save for later", "Catch me up", "Refresh", or
   "Mark read". Nothing happens.
4. Tap a Catalog row title. Navigation works.

### What's been ruled out

- Action ID mismatch — IDs match between server `Actions` and client
  `RemoteActions`, and the `HostAction` op is serialized into the document
  bytes (`MODIFIER_CLICK` + `HOST_ACTION` + 4-byte id, op `0x3bd1` followed
  by the action id).
- Library version — same behavior on `1.0.0-alpha05`, `alpha08`, `alpha09`.
- Element type — tried `box` and `row`, with and without `background` /
  `clip`, with weighted column children matching the catalog row pattern
  byte-for-byte. None fire.
- Action delivery path — bypassing `onClick` entirely with
  `addClickArea(id, name, l, t, r, b, "")` (op `CLICK_AREA = 64`, separate
  from the modifier path) also does not fire while the outer
  `width(viewport.w)` box is in place.
- Touch routing under non-uniform scale — `SCALE_FILL_WIDTH` makes every
  tap fire as `id=1002` (Save for later) regardless of where the user
  presses, confirming the player projects view px to canvas coords assuming
  uniform scaling. `SCALE_FIT` (uniform) restores correct routing for the
  catalog row but the CTAs still never fire.

### Root cause (best current theory)

`render()` wraps the document body in
`box(modifier = width(viewport.w).fillMaxHeight())` so descendants inherit a
finite max-width and `text()` actually wraps (without it, `fillMaxWidth`
resolves to `Float.MAX_VALUE` in `DimensionModifierOperation` and overflows
the canvas). With that EXACT-width box in place, the player's hit-test
projection drops every click event addressed below the catalog-row layer,
including those registered via `addClickArea`.

Removing the outer box restores click delivery (taps fire) and breaks the
visual layout (text overflows, content draws past the right edge). There is
no in-tree workaround that gives both wrap and clicks under alpha09.

### Where to pick this up

- File against `androidx.compose.remote` once an internal channel exists, or
  wait for `alpha10` and re-test. Repro is simply: build a doc with an
  EXACT-width box at root, put `onClick` on a child far from the constraint,
  observe taps not delivered.
- Refactor option that probably works: switch to `SIZING_LAYOUT` mode and
  thread `viewport.density` through every helper to multiply font sizes /
  paddings. That avoids the canvas-to-view scale entirely (canvas == view
  px), which sidesteps both the wrap problem and the hit-test projection.
  Deferred because it touches every helper signature.
- Or: replace every text helper's wrap modifier with a parent `row` that
  uses `horizontalWeight(1f)` (the catalog row pattern) instead of an outer
  width box. Untested but avoids the EXACT-width ancestor that seems to
  break hit-testing.

Relevant commits: `1f7075c`, `e24fcf8`, `ee0f36a`.
