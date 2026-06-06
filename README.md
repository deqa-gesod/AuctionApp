# Auction App

An Android app for running live auctions. Users list products, place bids, and watch the highest bid update in real time on every device. Built by Group 7 in the Mobile Development course at the University of South-Eastern Norway, where it received the top grade.

## What it does

- **Live bidding.** Place a bid and it shows up on every connected device right away, backed by Firebase Realtime Database.
- **Two roles.** Admins list products and see all bids. Regular users browse and bid.
- **Multiple product images.** Sellers upload several photos per listing, shown in a swipeable carousel.
- **Automatic expiry.** When a listing's deadline passes, it moves to "sold" on its own.
- **Bid history.** Admins can open any product and read its full bidding record.
- **CSV export.** Auction data can be downloaded as CSV for reporting.
- **Search.** Filter the product list as you type.
- **User profiles.** Register, log in, and edit your profile.

## Tech stack

- **Language:** Java
- **Platform:** Android (minSdk 21, targetSdk 32)
- **Backend:** Firebase Auth, Realtime Database, and Storage
- **Image loading:** Glide, Picasso
- **UI:** Material Design, ViewPager2, RecyclerView, ConstraintLayout

## Running it

1. Clone the repo.
2. Create a Firebase project at [console.firebase.google.com](https://console.firebase.google.com).
3. Add an Android app with the package name `com.mob300.auctionapp`.
4. Download `google-services.json` and put it in `AuctionApp/app/`.
5. Open the project in Android Studio and run it.

`app/google-services.json.example` shows the expected file format.

## How the code is organized

Activities live under `app/src/main/java/com/mob300/auctionapp/`. The main pieces:

```
login.java / register.java     Authentication
MainActivity.java              Admin home (navigation drawer)
BuyProductsActivity.java       Product browsing (admin)
BuyProductsActivityUser.java   Product browsing (user)
ProductActivity.java           Create a new listing
ProductDetailActivity.java     View a product and place a bid
ProductManager.java            Migrates expired products to "sold"
ServiceActivity.java           Admin dashboard (tables + CSV export)
ProductAdapter.java            RecyclerView adapter for product cards
MyOffersActivity / Adapter     View and manage your own bids
ImageSliderAdapter.java        ViewPager2 image carousel
FullScreenImageActivity.java   Fullscreen image viewer
EditProfileActivity.java       Edit profile
Product.java                   Data model
```

## What I would change

The grade was good, but the code has rough edges I can see now. A few things I would do differently:

**Architecture.** Too much logic sits directly inside Activities. I would move to MVVM with ViewModels and a repository layer so the Firebase calls are separated from the UI. That makes the app easier to test and to change later.

**Duplication.** `BuyProductsActivity` and `BuyProductsActivityUser` are roughly 80 percent the same code. They should be one Activity that adjusts its UI by role, or at least share a common base class.

**Tests.** There are none. I would add JUnit tests for the logic that actually matters, bid validation and deadline checks, and Espresso tests for the main flows like login and placing a bid.

**Concurrency.** The multi-image upload in `ProductActivity` can hit a race condition when callbacks return out of order. A synchronized collection, or Kotlin coroutines, would handle the async work more cleanly.

**Error handling.** Some Firebase callbacks started out with empty error handlers, so failures were silent. Logging and clear user feedback should have been there from day one.

## Team

Built by Group 7, University of South-Eastern Norway, Mobile Development (MOB3000).
