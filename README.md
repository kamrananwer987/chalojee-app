# Chaloje App (Android & iPhone)

This project turns **chaloje.com** into a phone app. The app opens the live website inside a native shell, so flight search, bookings and payments work exactly as they do on the site. When you update the website, the app updates too — no new app release needed.

## What's in this folder

| File | What it does |
|---|---|
| `package.json` | Lists the app's tools (Capacitor 7) and shortcut commands |
| `capacitor.config.json` | App name, app ID (`com.chaloje.app`), which site to load, splash screen settings |
| `www/index.html` | Tiny start page that hands off to chaloje.com |
| `www/offline.html` | Friendly "You're offline" screen with a Try again button |
| `resources/` | Put your app icon and splash image here (see step 3) |

## What you need

- A computer with **Node.js 20+** (nodejs.org)
- **Android:** Android Studio (free) — works on Windows, Mac or Linux
- **iPhone:** a Mac with Xcode (free) — iOS apps can only be built on a Mac
- To publish: Google Play developer account (one-time $25) and/or Apple Developer Program ($99/year)

## Steps

### 1. Install
Open a terminal in this folder and run:
```
npm install
```

### 2. Create the Android and iOS projects
```
npm run setup:android
npm run setup:ios        # Mac only
```

### 3. Add your icon and splash screen
Place these in the `resources/` folder:
- `icon.png` — 1024 × 1024, your Chaloje logo, no transparency for iOS
- `splash.png` — 2732 × 2732, logo centred on a plain background

Then run:
```
npm run icons
```
If your brand colour isn't white, change `#ffffff` in `package.json` and `capacitor.config.json` before running it.

### 4. Open and test
```
npm run open:android     # opens Android Studio → press Run ▶ with a phone or emulator
npm run open:ios         # opens Xcode → pick a simulator or iPhone → Run ▶
```

### 5. Build for the stores
- **Android:** in Android Studio, *Build → Generate Signed App Bundle* → upload the `.aab` to Google Play Console. Keep the signing key safe; you need it for every update.
- **iPhone:** in Xcode, set your Team under *Signing & Capabilities*, then *Product → Archive → Distribute App* to App Store Connect.

Whenever you change the config, run `npm run sync` before building again.

## Things to know before publishing

- **Apple may reject a plain website wrapper** (App Store guideline 4.2, "minimum functionality"). Adding app-only features improves your chances — push notifications for fare alerts and booking reminders are the usual first step. Google Play is generally more relaxed.
- **Payments:** if the site's payment page redirects to a bank or gateway on another domain (e.g. JazzCash, Easypaisa, a card gateway), add that domain to `allowNavigation` in `capacitor.config.json`, otherwise it may open outside the app. Test a full booking before release.
- **Privacy policy:** both stores require a privacy policy URL — chaloje.com needs one live.
- **App ID** `com.chaloje.app` is permanent once published. Change it now if you want something different.

## Good next additions
1. Push notifications (`@capacitor/push-notifications` + Firebase)
2. Opening WhatsApp / phone links natively
3. A bottom tab bar (Search · My trips · Support) for a more app-like feel
