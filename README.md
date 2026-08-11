# NoSnooze

NoSnooze is an Android alarm app that makes waking up harder to ignore.

Instead of letting the user dismiss the alarm immediately, the app opens the front camera and requires the user to do push-ups for 15 seconds. The position is detected in real time using Google ML Kit Pose Detection.

## Screenshots

### Alarm Management

<img src="docs/screenshots/NoSnooze_main_page.png" width="300">

### Wake-up Challenge

<img src="docs/screenshots/NoSnooze_camera_page.png" width="300">

### Challenge Completed

<img src="docs/screenshots/NoSnooze_completion_page.png" width="300">

## Features

- Create and manage multiple alarms
- Enable or disable alarms individually
- Select repeat days for each alarm
- Edit or delete existing alarms
- Persist alarm settings locally
- Trigger alarms using Android `AlarmManager`
- Play the alarm sound continuously until the challenge is completed
- Open the front camera using CameraX
- Detect a push-up-like position using ML Kit Pose Detection
- Require the detected position to be held for 15 seconds
- Display a success screen after the challenge is completed

## How it works

1. The user creates an alarm and optionally selects repeat days.
2. Android schedules the alarm using `AlarmManager`.
3. When the alarm fires, a `BroadcastReceiver` opens the challenge screen.
4. The alarm sound starts and the front camera is activated.
5. Camera frames are analyzed using Google ML Kit Pose Detection.
6. The app checks several body landmarks to determine whether the user is in a push-up-like position.
7. The position must remain valid for 15 seconds, if not  the timer decreases from 15 seconds to 0.
8. Once the challenge is completed, the alarm stops and the success screen is displayed.

## Tech Stack

- Java
- Android SDK
- XML layouts
- CameraX
- Google ML Kit Pose Detection
- AlarmManager
- BroadcastReceiver
- MediaPlayer
- SharedPreferences

## Pose Detection

The app uses body landmarks detected by ML Kit to validate a front-facing push-up-like position.

The detection logic checks factors such as:

- shoulder visibility and alignment
- head position relative to the shoulders
- wrist position
- distance between the wrists
- stability across multiple camera frames

The goal is not to evaluate professional exercise form, but to make sure the user physically gets out of bed and maintains the required position long enough to dismiss the alarm.

## Project Structure

`MainActivity`  
Handles alarm creation, editing, repeat days and alarm management.

`AlarmScheduler`  
Schedules and cancels alarms using Android `AlarmManager`.

`AlarmReceiver`  
Receives scheduled alarm events and launches the alarm screen.

`AlarmActivity`  
Handles the alarm sound, CameraX preview, pose detection and challenge timer.

`SuccessActivity`  
Displays the result after the challenge is successfully completed.

`AlarmStorage`  
Stores alarm configuration locally.

## Running the project

1. Clone the repository.

```bash
git clone https://github.com/DenisCorneanu/NoSnooze.git
