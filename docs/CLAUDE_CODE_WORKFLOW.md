# Claude Code Workflow

You're new to Kotlin and Android. Claude Code can write the code, but **you
have to drive it**. These rules keep you from shipping things you don't
understand.

---

## One-Time Setup

1. **Install Android Studio** — not optional. You need the gradle sync,
   linting, and emulator/device runner. Claude Code can't substitute for it.
2. **Enable developer mode** on your spare Android device. Connect via USB.
3. **Clone your fork** locally and run `./gradlew build` once. Make sure it
   works *before* you change anything.
4. **Run the app on the real device** at least once before starting Phase 1.
   Your baseline.
5. **Install Claude Code** in your terminal. Use it from inside the repo
   directory.

---

## Session Hygiene

**Start every Claude Code session with this prompt:**

> Read `docs/VISION.md`, `docs/ROADMAP.md`, and the current phase brief in
> `docs/phase-N-*.md`. Also read `IMPLEMENTATION_PLAN.md` if you haven't this
> session. Then tell me what's next on the task list and what your plan is
> before writing any code.

This forces Claude Code to ground itself in your design instead of guessing.

**End every session with:**

> Summarize what we did this session into a few bullet points I can paste
> into a commit message. List any files you touched.

You commit the summary. You stay in the loop on what's changing.

---

## The Read-Before-Write Rule

Before Claude Code edits a file, it should have read:
- The file it's editing
- The data classes it depends on
- Any callers of the function it's changing

If it tries to skip this step, push back. Ask it to read first.

This costs a little time per session but saves hours of debugging mystery
breakages.

---

## Commit Discipline

- **One logical change per commit.** "Added type field to ores.json" is one
  commit. "Added type field to ores.json and trees.json and fish.json" is
  three commits.
- **Build between commits.** If `./gradlew build` fails, you don't commit.
- **Commit messages reference the phase.** `[phase-1] add type field to ore data`
- **Push to your fork after every working session.** GitHub is your backup.

If you break something and can't figure out what, `git diff HEAD~5` and
`git log --oneline -10` are your friends. Small commits = small diffs to read.

---

## Branch Strategy

```
main                  — synced with upstream, always shippable
phase-1-types         — current work
phase-2-combat-types  — next phase, branched from main after phase-1 merges
```

**Periodically sync upstream:**
```bash
git remote add upstream https://github.com/tristinbaker/IdleFantasy.git
git fetch upstream
git checkout main
git merge upstream/main
git push
```

Do this between phases, not during. Mid-phase upstream pulls are painful.

---

## When To Stop and Test On Device

After any of:
- Changes to `SkillSimulator`, `CombatSimulator`, `CraftingSimulator`
- Changes to Room entities or migrations
- Changes to `WorkManager` / `SessionRepository`
- Changes to notification logic

The emulator doesn't faithfully reproduce session/notification behavior.
Test the real thing.

---

## When Claude Code Is Wrong

It will be sometimes. Signs:
- It's confidently rewriting something without reading the original
- It's adding dependencies that aren't in `build.gradle.kts`
- It's referencing APIs that don't exist in the project
- It's "fixing" lint warnings by silencing them rather than addressing them

When in doubt, ask:
> Show me the existing pattern in the codebase for this. Where else is
> something similar already done?

If it can't find an existing pattern, you might be doing something the
codebase doesn't support yet. That's a design conversation, not a code
conversation.

---

## Learning Kotlin As You Go

Don't try to learn Kotlin first and then build. Learn by doing.

For each non-trivial diff Claude Code produces, ask:
> Walk me through this change. Explain any Kotlin syntax that would be new
> to someone coming from [your background language].

Over a few weeks of this, you'll genuinely know Kotlin. Without the homework
problems.

---

## When To Take A Break

- After a phase merges. Always.
- When a session has been going more than 2 hours.
- When you've made 5+ failed attempts at the same thing. (Sleep on it.)
- When you stop understanding the diffs. Slow down, read, re-ground.

The fork will still be here tomorrow.
