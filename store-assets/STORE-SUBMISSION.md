# Play Store submission pack — Nonogram

Everything you need to fill in the Play Console listing. Copy/paste the text, upload the
images in this folder, and use the form answers below.

---

## 1. Privacy policy URL

The policy lives in this repo at `docs/index.html`. Turn on GitHub Pages once to get a public URL:

1. Go to https://github.com/pran009/Nonogram/settings/pages
2. Under **Build and deployment → Source**, choose **Deploy from a branch**.
3. Branch: **master**, folder: **/docs**. Click **Save**.
4. Wait ~1 minute, then your policy is live at:

   **https://pran009.github.io/Nonogram/**

Paste that URL into Play Console → Policy → App content → Privacy policy.

---

## 2. Store listing

> Trademark note: do not use "Picross" (Nintendo), "Pic-a-Pix" (Conceptis) or "Griddlers"
> anywhere in the title, description or tags. They are other companies' brand names.

**App name** (max 30): `Nonogram With Challenges`

**Short description** (max 80):
```
Logic picture puzzles: solve nonograms, take on challenges, collect 39 cards.
```

**Full description** (max 4000):
```
Nonogram is a relaxing logic puzzle game where every grid hides a picture. Use the number clues on each row and column to work out which squares to fill, and watch the image appear. This classic puzzle is also known as hanjie, picture cross or Japanese crossword — and every puzzle can be solved by pure logic, so you never have to guess.

CHALLENGE MODE & COLLECTIBLE CARDS
Take on randomly dealt puzzles in Easy, Difficult or Very Difficult and win a collectible card every time you solve one. The faster you finish and the fewer mistakes you make, the rarer the card. Start with common critters and work your way up to the legendary Ancient Dragon — 39 cards to collect, each one hidden until you earn it.

DAILY PUZZLE & ENDLESS PLAY
A fresh daily puzzle every day, plus unlimited random puzzles in three sizes. Every generated puzzle is checked by a solver, so it is always fair.

EASY TO LEARN
A short interactive tutorial teaches you the rules with a practice puzzle you play yourself. Skip it any time if you already know how nonograms work.

FEATURES
• 32 hand-crafted picture puzzles in 5x5, 10x10 and 15x15
• Challenge Mode with 39 collectible cards
• Daily puzzle and unlimited random puzzles
• Tap or drag to fill, cross out empty squares, pinch to zoom
• Undo, hints and a timer with your best times
• Optional mistake checking, or relaxed free play
• Light and dark themes
• Share your results with friends
• Works fully offline — no internet needed, no sign-up

A satisfying brain workout for logic and reasoning, focus and problem solving. If you enjoy number puzzles, grid puzzles, pixel art puzzles, brain teasers or classic number logic games, you will love Nonogram.
```

**App category:** Games → Puzzle
**Tags (pick from Play's list):** Brain games, Logic, Puzzle, Casual, Offline
**Contact email:** pran009@gmail.com

**Graphics to upload (in this folder):**
| Asset | File | Size |
|---|---|---|
| App icon | `icon-512.png` | 512×512 |
| Feature graphic | `feature-graphic-1024x500.png` | 1024×500 |
| Phone screenshots | `screenshots/01-…06-*.png` (captioned, 1080×2100) | 6 files |

**Screenshots:** Play requires at least 2 phone screenshots (min 320px, 16:9 or 9:16).
Capture them from the running app — the home screen, a puzzle in progress, and a solved
puzzle make a good set. On an emulator use the camera icon in the side toolbar; on a phone use
the normal screenshot buttons. Send them to me and I can frame them nicely if you want.

---

## 3. Content rating questionnaire

Play Console → Policy → App content → Content rating. Category: **Game**. Answers:

- Violence, blood, sexual content, nudity, crude humour: **No** to all
- Profanity or crude language: **No**
- References to drugs, alcohol, tobacco: **No**
- Simulated gambling / real gambling: **No** (rewarded video ads are not gambling)
- Scary or disturbing content: **No**
- Does the app share the user's location with other users: **No**
- Do users interact or exchange content (chat, UGC): **No**
- Does the app contain ads: **Yes**
- Does the app let users purchase digital goods: **No** (there are no in-app purchases yet)

Expected result: **Everyone / PEGI 3**.

---

## 4. Data safety form

Play Console → Policy → App content → Data safety. This app itself stores everything on the
device, but AdMob collects an advertising ID, so answer as follows:

**Overview**
- Does your app collect or share any of the required user data types? **Yes**
- Is all of the user data collected by your app encrypted in transit? **Yes**
- Do you provide a way for users to request that their data is deleted? **No**
  (No account exists; uninstalling removes all local data. You may add this note.)

**Data types — declare only this one:**
- Category **Device or other IDs → Device or other IDs** (the advertising ID)
  - Collected: **Yes**
  - Shared: **Yes** (with Google for ads)
  - Processed ephemerally: **No**
  - Required or optional: **Required**
  - Purposes: **Advertising or marketing**, and **Analytics**

Do **not** declare name, email, location, photos, contacts, files, or app activity — the app
does not collect any of those.

---

## 5. Other App content declarations

- **Ads:** Yes, the app contains ads.
- **Target audience:** 13+ is the simplest choice (avoids the stricter Families policy). If you
  target under-13 you must use only child-appropriate ad content in AdMob.
- **News app:** No
- **COVID-19 / government app:** No
- **Data collection for financial features:** No

---

## 6. Release checklist

1. Create your signing key (see main README), add `keystore.properties`.
2. Confirm the final `applicationId` (currently `app.nonogram.puzzle`) — it can never change.
3. Build: `gradlew.bat bundleRelease` → upload `app/build/outputs/bundle/release/app-release.aab`.
4. Start with the **Internal testing** track, install on your phone, confirm ads and gameplay.
5. Fill in the listing, graphics, content rating, data safety, and privacy policy URL above.
6. Promote the release to **Production** and submit for review.
