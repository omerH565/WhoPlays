# WhoPlays - Social Sports App ⚽🏀🎾
**Developed by:** Omer Halili

WhoPlays is a comprehensive social sports application designed to help users find, manage, and join local group sports events in real-time. 
The app transitions from simple list management to a fully-fledged production-ready system with geographic location tracking, real-time chat, and cloud automation.

## 🏗️ Architecture & Technologies
*   **Architecture:** Built using modern MVVM (Model-View-ViewModel) architecture, ensuring clean separation of concerns between UI, Business Logic, and Data layers.
*   **Firebase Suite:**
    *   *Authentication:* User registration and secure login.
    *   *Firestore:* Real-time NoSQL database for events and user profiles.
    *   *Storage:* Hosting for court images and user profile pictures.
    *   *Cloud Messaging (FCM) & Cloud Functions:* Node.js backend logic for automated push notifications.
*   **Libraries:** 
    *   *Glide:* Client-side optimized image loading and caching.
    *   *Coroutines & Flow:* Efficient asynchronous operations for a smooth UI.
*   **UI/UX:** Material Design 3 components (Chips, Cards, FABs) with smooth keyboard management using `adjustResize`.
*   **Location Services:** Integration with Google Play Services Location for accurate geographic filtering.

## ✨ Key Features
*   **Dynamic Main Feed:** Filter events by categories (All, Near, Today, Fav, Joined).
*   **Real-time Group Chat:** Live messaging for event participants, powered by Firestore `callbackFlow`, with automated system messages when users join.
*   **Smart Sync:** Accurate participant counters handling real-time join/leave actions via Unique IDs.
*   **Edge Case Handling:** Includes fallback UI for empty states (e.g., "No games found") and robust runtime permission checks for GPS.
