# yi4k-remote-android

Android app (Kotlin + Jetpack Compose) that connects to a Yi 4K Action Camera over its
built-in Wi-Fi hotspot, remotely controls it (photo, record start/stop, battery/status),
and displays its live video feed.

Uses [`yi4k-sdk`](https://github.com/adidoo/yi4k-sdk) for the camera communication protocol.

## Getting started

1. Check out this repo and `yi4k-sdk` as siblings on disk:
   ```
   dev/
     yi4k-remote-android/
     yi4k-sdk/
   ```
   (`settings.gradle.kts` wires them together via `includeBuild("../yi4k-sdk")`. Once
   `yi4k-sdk` is published to Maven/JitPack, this can be swapped for a normal version
   dependency in `app/build.gradle.kts`.)
2. Open `yi4k-remote-android` in Android Studio.
3. On the phone: turn on the camera's Wi-Fi, join its hotspot from Android's Wi-Fi settings
   (SSID usually starts with `YDXJ`), then launch the app and tap **Connecter**.

## What it does

- Connects to the camera control channel at `192.168.42.1:7878`.
- Shows the live view (RTSP, via Media3/ExoPlayer), full screen, portrait and landscape.
- Take photo / start-stop recording, with a live recording timer and an estimated
  remaining-recording-time readout (calibrated from the real SD-card drain rate — see
  `yi4k-sdk`'s README for why a fixed formula doesn't work).
- Battery level, current video resolution, SD card status.
- Detects a recording already in progress when (re)connecting (e.g. started from the
  camera's own shutter button).
- **Advanced settings screen**: exposes every setting the camera reports (not just the
  handful of curated ones), grouped into categories (Vidéo, Photo, Qualité d'image, Son,
  Réseau, Système, Stockage, État). Known settings are editable through a dropdown when the
  camera reports valid choices; anything the app doesn't recognize (other camera models/
  firmwares) shows up read-only under "Autres", with an explicit toggle to force editing.
  Fields that look like credentials or a device serial number are masked by default.

## Status

The control protocol is reverse-engineered (see `yi4k-sdk`'s README) — some commands may
need adjusting for your exact firmware. Only manual Wi-Fi pairing is supported for now (no
in-app Wi-Fi connect flow yet). The setting catalog was built and verified live against one
real Yi 4K (firmware `Z16V13L_1.10.9`) — other firmware versions may expose different keys,
which is exactly what the "Autres" category is for.

## License

MIT — see [LICENSE](LICENSE).
