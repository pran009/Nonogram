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

**App name** (max 30): `Nonogram`

**Short description** (max 80):
```
Picture logic puzzles. Solve nonograms with clean, no-guessing gameplay.
```

**Full description** (max 4000):
```
Nonogram is a relaxing picture logic puzzle, also known as picross or griddlers. Use the
number clues on each row and column to work out which squares to fill, and a hidden picture
appears.

Every puzzle is solvable by pure logic — you never have to guess. That includes the random
puzzles, which are checked by a solver before you ever see them.

FEATURES
• 32 hand-made puzzles in three sizes: 5×5, 10×10 and 15×15
• A new daily puzzle every day
• Unlimited random puzzles, always logically solvable
• Simple controls: tap or drag to fill, switch to cross out squares
• Pinch to zoom and pan on bigger boards
• Undo, smart hints, and a timer with your best times
• Optional mistake checking, or free play with no penalties
• Light and dark themes
• Works fully offline

Whether you have two minutes or an hour, Nonogram is an easy game to pick up and a satisfying
one to master. No sign-up, no clutter — just you and the puzzle.
```

**App category:** Games → Puzzle
**Tags:** nonogram, picross, logic puzzle, griddler
**Contact email:** pran009@gmail.com

**Graphics to upload (in this folder):**
| Asset | File | Size |
|---|---|---|
| App icon | `icon-512.png` | 512×512 |
| Feature graphic | `feature-graphic-1024x500.png` | 1024×500 |
| Phone screenshots | you capture these | see below |

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
