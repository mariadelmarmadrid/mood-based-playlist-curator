# 🎵 Mood-Based Playlist Curator

An Android app that lets users **track their daily moods**, add contextual details, view **visual insights**, and explore **enhanced features** like photos, playlists, and location-based mood tracking.

---

## 🌈 Overview

**Mood-Based Playlist Curator** is a Kotlin + Android Studio project that demonstrates clean architecture, Material Design components, JSON persistence, and data visualization.

Users can:
- Log their **daily moods** (Happy, Relaxed, Neutral, Sad, Angry)
- Add optional details like **sleep quality**, **social activity**, **hobby**, and **food type**
- Write short **notes** about the day
- Attach **photos** to mood entries (add/change/remove)
- View their mood history grouped by day
- See **insights** via a color-coded “mood ring” and daily averages
- Get **playlist recommendations** based on daily average mood
- Set **location** for moods and explore nearby locations on a map

---

## 🖼️ Screenshot

<p align="center">
  <img src="screenshots/home_screen.png" alt="Home screen" width="300"/>
  <img src="screenshots/insights_screen.png" alt="Insights screen" width="300"/>
</p>

---

## 🧠 Features

| Category | Description |
|-----------|--------------|
| **Mood Tracking** | Add, edit, or delete mood entries with rich context, emoji feedback, and optional photos. |
| **Daily Summaries** | Automatically groups moods by date with computed daily averages. |
| **Insights Screen** | Displays a custom circular chart (`DailyMoodRingView`) showing mood distribution per day. |
| **Filter & Search** | Filter moods by text, date range (All / Today / Last 7 days), or minimum daily average. |
| **Playlist Recommendations** | Suggests Spotify playlists based on your average mood for the day. |
| **Location Support** | Tag moods with a location and explore nearby cafés, parks, gyms, and takeaways on Google Maps. |
| **Persistent Storage** | Moods are stored in a local JSON file (`moods.json`) using `MoodJSONStore`. |
| **Accessibility** | All visible text moved to `strings.xml` and icons include content descriptions. |
| **Material Design UI** | Uses Material 3 components, chips, sliders, cards, and photo previews for a clean look. |
| **MVP Architecture** | Refactored screens into Model-View-Presenter pattern for better separation of concerns. |

---

## 📂 Project Structure (Highlights)

- **activities/** — All Activity classes including MoodActivity, MoodListActivity, InsightsActivity, MoodMapActivity  
- **models/** — Data classes, JSON persistence store (`MoodJSONStore`), enums, daily summaries  
- **adapters/** — RecyclerView adapters for mood cards and daily summaries  
- **views/** — Custom views like `DailyMoodRingView`  
- **resources/** — XML layouts, strings, colors, and drawable assets

---

## ✨ Author

**María del Mar Madrid Delgado**  
📍 Mobile App Development 1 — South East Technological University (SETU)  
📅 October 2025  

