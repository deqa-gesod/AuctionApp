# Auction App

A real-time auction app for Android where users can list products, place bids, and track auction results. Built as a group project (Group 7) during our Mobile Development course at USN.

## Features

- **Real-time bidding** — bids update instantly across all devices using Firebase Realtime Database
- **Role-based access** — admins can list products and view all bids, regular users can browse and bid
- **Multi-image upload** — sellers can upload multiple product images with a swipeable image carousel
- **Automatic expiry** — products move to "sold" when their deadline passes
- **Bid history** — admins can view the full bidding history for each product
- **CSV export** — download auction data as CSV for reporting
- **Search** — filter products in real time
- **User profiles** — registration, login, and profile editing

## Tech Stack

- **Language:** Java
- **Platform:** Android (minSdk 21, targetSdk 32)
- **Backend:** Firebase (Auth, Realtime Database, Storage)
- **Image loading:** Glide, Picasso
- **UI:** Material Design, ViewPager2, RecyclerView, ConstraintLayout

## Setup

1. Clone the repo
2. Create a Firebase project at [console.firebase.google.com](https://console.firebase.google.com)
3. Add an Android app with package name `com.mob300.auctionapp`
4. Download `google-services.json` and place it in `AuctionApp/app/`
5. Open in Android Studio and run

See `google-services.json.example` for the expected file format.

## Project Structure

```
app/src/main/java/com/mob300/auctionapp/
├── login.java / register.java     — Authentication
├── MainActivity.java              — Admin home (navigation drawer)
├── BuyProductsActivity.java       — Product browsing (admin)
├── BuyProductsActivityUser.java   — Product browsing (user)
├── ProductActivity.java           — Create new product listing
├── ProductDetailActivity.java     — View product + place bid
├── ProductManager.java            — Handles expired product migration
├── ServiceActivity.java           — Admin dashboard (tables + CSV export)
├── ProductAdapter.java            — RecyclerView adapter for product cards
├── MyOffersActivity/Adapter.java  — View and manage your bids
├── ImageSliderAdapter.java        — ViewPager2 image carousel
├── FullScreenImageActivity.java   — Fullscreen image viewer with swipe
├── EditProfileActivity.java       — Edit user profile
└── Product.java                   — Data model
```

## What I'd Do Differently

This was a learning project. Looking back at the code with more experience, here's what I'd change:

**Architecture** — The app puts too much logic directly in Activities. I'd use MVVM with ViewModels and a Repository layer to separate Firebase calls from UI code. This makes the code easier to test and maintain.

**Code duplication** — `BuyProductsActivity` and `BuyProductsActivityUser` share ~80% of the same code. These should be a single Activity with role-based UI changes, or at minimum share a base class.

**Testing** — There are no unit tests. I'd add JUnit tests for business logic (bid validation, deadline checking) and Espresso tests for critical user flows (login, placing a bid).

**Concurrency** — The multi-image upload in `ProductActivity` has a potential race condition where callbacks can fire out of order. I'd use a synchronized collection or switch to Kotlin coroutines for cleaner async handling.

**Error handling** — Some Firebase callbacks originally had empty error handlers, making failures invisible. Proper logging and user feedback should be in place from the start.

## Team

Built by Group 7 — University of South-Eastern Norway, Mobile Development (MOB3000).
