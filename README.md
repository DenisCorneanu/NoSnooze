# NoSnooze

NoSnooze is an Android smart alarm application designed to help users wake up properly and avoid falling back asleep after dismissing an alarm.

Instead of allowing the user to stop or snooze the alarm immediately, the application requires the user to get out of bed and hold a push-up position for 15 seconds. The position is verified using the front camera and Google ML Kit Pose Detection.

## Project idea

The main problem addressed by this project is the habit of stopping or snoozing an alarm while still half asleep.

NoSnooze tries to solve this by adding a physical challenge before the alarm can be stopped. The user must move, get into position, and stay active long enough to fully wake up.

## Main features

- Set an alarm using a time picker
- Enable or disable the alarm
- Select repeat days
- Start an alarm screen when the alarm time is reached
- Play an alarm sound in a loop
- Open the front camera
- Detect a push-up position using pose detection
- Require the position to be held for 15 seconds
- Stop the alarm only after the challenge is completed
- Display a final achievement screen

## Technologies used

### Java

The main programming language used for the application logic.

### Android Studio

The development environment used to build and test the Android application.

### CameraX

CameraX is used to access the front camera and display a live camera preview inside the application.

### Google ML Kit Pose Detection

ML Kit Pose Detection is used to detect body landmarks from the camera image. In this project, the application mainly uses landmarks such as the nose, shoulders, and wrists.

### AlarmManager

AlarmManager is used to schedule the alarm at the selected time.

### BroadcastReceiver

BroadcastReceiver receives the alarm event triggered by AlarmManager and opens the alarm challenge screen.

### MediaPlayer

MediaPlayer is used to play the alarm sound until the challenge is completed.

## Application structure

### MainActivity

MainActivity is the home screen of the application.

It allows the user to:

- view the selected alarm time
- set a new alarm
- enable or disable the alarm
- choose repeat days
- view the wake-up challenge
- view simple progress statistics

### AlarmReceiver

AlarmReceiver is triggered when the scheduled alarm time is reached.

Its role is to open AlarmActivity.

### AlarmActivity

AlarmActivity is the main challenge screen.

It handles:

- starting the alarm sound
- opening the front camera
- analyzing the camera frames
- detecting the push-up position
- counting the 15-second challenge timer
- stopping the alarm after the challenge is completed

### SuccessActivity

SuccessActivity is the final screen shown after the user completes the challenge.

It displays a short achievement summary and confirms that the alarm has been stopped.

## How the pose detection works

The application uses Google ML Kit Pose Detection to detect body landmarks in real time.

For this project, the goal is not to perfectly validate a professional push-up. Instead, the app checks whether the user is in a front-facing push-up-like position.

The detection logic checks if:

- the shoulders are visible
- the shoulders are reasonably horizontal
- the head is near the center of the shoulders
- the wrists are below the shoulders
- the wrists are placed far enough apart
- the position is stable for several frames

If these conditions are met, the app considers the push-up position valid and starts increasing the timer.

## User flow

1. The user opens the application.
2. The user sets an alarm time.
3. When the alarm time is reached, the alarm screen opens.
4. The alarm sound starts playing.
5. The front camera starts.
6. The user must hold a push-up position.
7. If the position is detected, the timer increases.
8. After 15 seconds, the alarm stops.
9. The success screen is displayed.

## Current limitations

This application is a student project and a functional prototype, not a production-ready alarm app.

Current limitations include:

- pose detection is approximate
- the app does not count real push-ups
- the app does not perfectly validate exercise form
- detection works best with the user facing the camera
- good lighting and camera positioning are important
- alarm persistence after device restart is not fully implemented
- the statistics are mostly visual/demo data

## Possible future improvements

- Add real push-up counting
- Improve pose validation accuracy
- Add multiple wake-up challenges
- Save statistics locally
- Add alarm persistence after device restart
- Add custom alarm sounds
- Add dark mode
- Allow the user to customize challenge duration
- Improve UI animations
- Add a proper settings screen

## Purpose of the project

The purpose of this project is to demonstrate the use of multiple Android development concepts in one application:

- multiple activities
- XML-based user interfaces
- alarm scheduling
- broadcast receivers
- camera access
- real-time image analysis
- machine learning SDK integration
- navigation between screens

## Author

Denis Corneanu

## Conclusion

NoSnooze is a smart alarm prototype that turns waking up into an active task. By requiring the user to hold a push-up position before stopping the alarm, the application encourages movement and reduces the chance of going back to sleep.
