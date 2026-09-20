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
- Shows the live view (RTSP, via Media3/ExoPlayer).
- Take photo / start-stop recording.
- Battery level and current video resolution.

## Status

The control protocol is reverse-engineered (see `yi4k-sdk`'s README) — some commands may
need adjusting for your exact firmware. Only manual Wi-Fi pairing is supported for now (no
in-app Wi-Fi connect flow yet).

## License

MIT — see [LICENSE](LICENSE).
