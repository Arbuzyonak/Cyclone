<p align="center">
  <img src="logo.png" width="140" alt="Cyclone logo"/>
</p>

<h1 align="center">Cyclone</h1>

<p align="center">Play Vortex on your Android phone.</p>

---

Cyclone is an unofficial Android client for [Vortex](https://playvortex.io). There is no Android version of the game, so Cyclone runs the real Windows client on your phone: it translates x86 to ARM with Box64, runs the game through Wine, and maps Direct3D to your phone's GPU with DXVK/vkd3d-proton and the Turnip Vulkan driver. All of that is built on top of [MiceWine](https://github.com/KreitinnSoftware/MiceWine-Application), stripped down and tuned for Vortex.

You just log in on the site like you normally would, tap Play, and the game launches fullscreen with touch controls.

## What you get

- The normal playvortex.io experience (login, avatar, your game library) in-app
- One tap to play, no setup screens. The first launch downloads everything it needs
- Touch controls: tap to click, drag to move the camera, on-screen buttons for moving, jumping and shift lock
- A chat button that opens the in-game chat and brings up your keyboard
- A menu button that drops you back to your library when you're done

## Requirements

- An ARM64 phone on Android 9 or newer
- A Snapdragon chip with an Adreno GPU is strongly recommended. Developed and tested on a Galaxy S23+ (Snapdragon 8 Gen 2 / Adreno 740)
- Around 5 GB of free storage
- A Vortex account

## Installing

1. Grab the latest `.apk` from the [Releases](../../releases) page
2. Open it. Android will ask you to allow installs from your browser or file manager, allow it
3. Open Cyclone and log into your account
4. Pick a game and hit Play

The first launch takes a while: it downloads the runtime (about 400 MB) and the game itself, then starts. Every launch after that goes straight into the game. Keep the phone plugged in for long sessions, translation is heavy and the phone will get warm.

## Controls

| Input | Action |
|---|---|
| Tap | Left click |
| Drag | Rotate camera |
| W / S | Move forward / back |
| Space | Jump |
| LShift | Shift lock |
| Chat | Opens chat and the keyboard (Enter sends and closes it) |
| Menu | Leave the game, back to your library |

## Building from source

You need JDK 17, the Android SDK and NDK 26.

```
git clone --recursive https://github.com/Arbuzyonak/Cyclone
cd Cyclone
./gradlew assembleDebug
```

The APK lands in `app/build/outputs/apk/debug/`.

## Credits

Cyclone stands on a lot of other people's work:

- [MiceWine](https://github.com/KreitinnSoftware/MiceWine-Application) - the Android Wine runtime this project is forked from (MIT)
- [Wine](https://www.winehq.org/), [Box64](https://github.com/ptitSeb/box64), [DXVK](https://github.com/doitsujin/dxvk), [vkd3d-proton](https://github.com/HansKristian-Work/vkd3d-proton) and [Mesa/Turnip](https://mesa3d.org/)

## Disclaimer

This is a community project. It is not made, endorsed or supported by Vortex or its developers. You need your own account, and the game is downloaded from the official site through your own login, exactly like the Windows launcher does.
