# Roadmap & shipped-state conventions

This is the **single standard** for how the NaCode-Studios libraries (Kmemo, Kdrant) record where the
project is going and what has already shipped. It exists so the same fact is written the same way in
every repository and on every surface — no second dialect. Both repos keep an identical copy of this
file; changing the standard means changing it in both.

Its whole reason to exist: **"shipped" has exactly one spelling, and it always names the version.**

## The four surfaces and who owns what

Each fact has one authoritative home. The other surfaces *reference* it; they never restate it in a way
that can drift.

| File | Owns | Does **not** own |
| --- | --- | --- |
| **`CHANGELOG.md`** | The record of **what shipped and in which released version** (and the date). Keep a Changelog + SemVer. This is the source of truth for every version number. | Future plans. |
| **`ROADMAP.md`** | Where the project is going: milestones (`M#`) grouped in tiers, each with a **status that points back at the CHANGELOG version** that shipped it. | The canonical version/date — it *cites* the CHANGELOG, never invents a version. |
| **`README.md`** | A short human summary: a one-line status badge + a "Roadmap" section. Links to `ROADMAP.md`. | The detailed milestone plan. |
| **`STABILITY.md`** | The versioning / stability / deprecation policy. Required from the first stable (`1.0`) line; optional (a short note in the `ROADMAP.md` preamble suffices) while pre-`1.0`. | What shipped, or the plan. |

**Consequence:** a milestone may only be marked shipped once the work is under a *released* version
heading in `CHANGELOG.md`. Merged-but-unreleased work lives under `[Unreleased]` and is **not** yet
`✅ Shipped` (see the lifecycle below).

## The shipped-state vocabulary

Every place a milestone, tier, or sub-item shows its state uses **exactly one** of these tokens — nothing
else, no synonyms ("Done", "Complete", "Live", a bare "✅"):

| Token | Meaning |
| --- | --- |
| **`✅ Shipped in \`X.Y.Z\`.`** | Done and released in `X.Y.Z`. **Always names the version.** Capitalized in every position — table cell, `**Status:**` line, and tier suffix. |
| **`🚧 In progress (targeting \`X.Y.Z\`).`** | Merged to `main` under `[Unreleased]`, or actively being built, for the next release. |
| **`Planned.`** | Not started; no version assigned yet. |
| **`Post-\`1.0\`.`** | Deliberately deferred beyond `1.0`. |
| **`Deferred — <reason>.`** | Decided against, or pushed to a later milestone. Always give the reason. |

Rule 1 — **never write "shipped" without a version.** `✅ Shipped.` is not allowed; `✅ Shipped in \`0.2.0\`.` is.

Rule 2 — **the version must match the CHANGELOG.** If the ROADMAP says `✅ Shipped in \`0.2.0\``, that
work must appear under `## [0.2.0]` in `CHANGELOG.md`.

## The lifecycle of one milestone

```
Planned.
  └─ work starts / first commit merges under CHANGELOG [Unreleased]
     └─ 🚧 In progress (targeting `X.Y.Z`).
        └─ release cut: [Unreleased] entries move under ## [X.Y.Z] - <date>
           └─ ✅ Shipped in `X.Y.Z`.
```

On release you do three things together, so the surfaces never disagree:
1. Move the `[Unreleased]` entries under a new `## [X.Y.Z] - YYYY-MM-DD` heading in `CHANGELOG.md`.
2. Flip every milestone that shipped from `🚧 In progress` to `✅ Shipped in \`X.Y.Z\`` in `ROADMAP.md`.
3. Update the README status badge + Roadmap section.

## `ROADMAP.md` structure (fixed order)

1. **Title + preamble** — one sentence on what the doc is, linking `CHANGELOG.md` and the README Roadmap
   section; one sentence on the stability posture (pre-`1.0` may break on minors / post-`1.0` SemVer,
   linking `STABILITY.md` once it exists).
2. **`## Guiding principles`** — the 3–5 bullets the project judges features against.
3. **`## Status — \`X.Y.Z\`` blocks** — **one block per released version, newest first**; the newest is
   tagged `(current)`. Each summarizes what that version delivered. (Do not keep a single stale block;
   add a new one each release.)
4. **`## Progress`** — one table, columns `| Milestone | Status |`, every row using the vocabulary above.
   Immediately after it, a **`Deferred sub-items`** paragraph naming anything carried forward or decided
   against, each with its reason.
5. **`## Effort legend`** — `S` ≈ hours–1 day · `M` ≈ several days · `L` ≈ 1–2 weeks · `XL` ≈ multi-week.
6. **The tiers and milestones** (details below).

### Tiers

- Heading form: `## Tier N — <theme>`.
- **A tier heading carries a trailing `— ✅ Shipped in \`X.Y.Z\`` only when the *entire* tier shipped in a
  *single* version.** If a tier's milestones shipped across *different* versions (or some are still
  planned), the heading stays **plain** and each milestone row carries its own version. This is the one
  rule that removes the "sometimes marked, sometimes not, sometimes mixed" drift.

### Milestones

- Heading form: `### M# · <title> — \`<effort>\``.
- Unless its status is already given by a version-stamped tier heading, a milestone with any state to
  report opens its body with a bold status lead line: `**Status: <token>**` — then optional
  `Delivered: …` / `Deferred: …` sentences, then the descriptive bullets (kept as the plan of record
  even after shipping). A lead line is always *allowed* (for the extra Delivered/Deferred detail) even
  when the tier heading already carries the marker; it is *required* whenever the tier heading is plain.
- A `Planned.` milestone needs no lead line; its table row already says `Planned.`.

## `CHANGELOG.md` conventions

- Keep a Changelog headings only: `Added` / `Changed` / `Deprecated` / `Removed` / `Fixed` / `Security`
  (plus `Internal` for toolchain/CI-only changes that ship no API).
- A top `## [Unreleased]` section always exists as the staging area.
- Each released heading is `## [X.Y.Z] - YYYY-MM-DD`, with compare-link references maintained at the
  bottom of the file.
- **Tie entries back to the roadmap:** where an entry corresponds to a milestone, tag it with the id,
  e.g. `- Aliases (M19): …`. This is what lets a reader cross-check Rule 2 at a glance.

## README conventions

- **Status badge** — one blockquote line: `> **Status — \`X.Y\`, <one-word maturity>.**`
  (`early development`, `beta`, `stable`, …), then one sentence of context.
- **Roadmap section** — at most three bold labels, in this order:
  **`Shipped (\`X.Y.Z\`)`** — **`Next`** — optionally **`Later`** / **`Post-\`1.0\``**. It ends by linking
  `ROADMAP.md` (and `STABILITY.md` if present). Keep it a paragraph each, not a milestone list.

## Checklist when you cut a release

- [ ] `CHANGELOG.md`: `[Unreleased]` → `## [X.Y.Z] - <date>`; compare links updated.
- [ ] `ROADMAP.md`: new `## Status — \`X.Y.Z\` (current)` block; previous loses `(current)`.
- [ ] `ROADMAP.md`: every shipped milestone → `✅ Shipped in \`X.Y.Z\`` in both the table and its lead line.
- [ ] `ROADMAP.md`: tier headings re-checked against the single-version rule.
- [ ] `README.md`: status badge + Roadmap `Shipped` paragraph updated.
- [ ] Grep check: no `✅` without a following `` `X.Y.Z` ``; no `Shipped`/`Done`/`Live` without a version.
