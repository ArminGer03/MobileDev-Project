# SimpleNote – Android & iOS (Course Project)

A tiny cross-platform notes app built for the **Mobile Development** course.  
It demonstrates onboarding, authentication (JWT), note CRUD with auto-save, connection status, pagination, and a minimal settings area.

- **Student:** Armin Geramirad — `401110631`  
- **Teammate:** Alireza Aalaei — `401110591`  
- **Platforms:** Android & iOS (SwiftUI). The iOS app implements a simpler subset of features.

---

## Features

**Common**
- Onboarding → Login / Register → Home
- JWT auth with refresh token; **automatic access-token refresh**
- Create / edit / delete notes
- **Auto-save** editor (no explicit save button)
- Connection status pill: **Online / Connecting… / Timeout (with retry countdown)**

**Android (Jetpack Compose)**
- Material 3 UI matching the design
- Inline form validation & API error surfacing
- Home:
  - Empty state when there are no notes
  - Local search (title/description)
  - **Paginated grid** (6 per page) with horizontal pager + page indicator
  - Center-docked FAB and bottom nav (Home / Settings)
- Editor: auto-save + delete bottom sheet
- Settings: user info, change password, logout

**iOS (SwiftUI)**
- Same flows with a simplified UI:
  - Login / Register / Home (grid + pager + FAB)
  - Editor (auto-save, delete)
  - Settings (user info, change password, logout)
  - Connection pill + timeout auto-retry
- Same REST API + token refresh

---

## Screens

- **Onboarding:** “Let’s get started” → Login  
- **Auth:** Login (username + password), Register (first/last/username/email/password)  
- **Home:** Empty state or 2-column grid, search, pager dots, center FAB  
- **Editor:** Title + description, auto-save, trash icon  
- **Settings:** User info, Change password, Logout

---

## Tech Stack

**Android**
- Kotlin, Jetpack Compose, Material3
- ViewModel/State, Coroutines/Flow
- Retrofit + OkHttp (Auth Interceptor)

**iOS**
- Swift 5, SwiftUI
- URLSession + async/await
- Lightweight API client + TokenStore

---

## Configuration

- Tokens are persisted after login:
  - **Android:** SharedPrefs/DataStore via `TokenRepository`
  - **iOS:** `TokenStore` (UserDefaults/Keychain)
- **Auto refresh:** on `401`, clients call `/api/auth/token/refresh/` with the refresh token and retry once.

---

## Networking & Timeouts

- Both apps use **3s request timeouts**.
- Home shows **Timeout** with a **30s auto-retry countdown**.
- iOS `APIClient` builds URLs with **URLComponents** so query strings (`page`, `page_size`) are correct.

---

## Known Limitations / Future Work

- iOS UI is simpler than Android by design.
- Search is currently **local** (title/description) even though a server filter exists.
- Notes are text-only (no media).
- No push notifications.

---

## Credits

- **Armin Geramirad** — `401110631`
- **Alireza Aalaei** — `401110591`

*This repository is submitted as part of the Mobile Development course project.*