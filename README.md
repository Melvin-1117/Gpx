# VirtualGamepad (Android)

This is a minimal Android app (Kotlin + Jetpack Compose) that demonstrates a virtual gamepad which connects to a PC server over TCP and sends simple KEY_DOWN / KEY_UP messages when a virtual button is pressed and released.

Key points
- Platform: Android
- Language: Kotlin + Jetpack Compose
- Hardcoded server: 192.168.1.50:8080 (change in `ConnectionViewModel.kt`)

Files added
- app/src/main/java/com/example/virtualgamepad/network/TcpClient.kt — simple TCP client using java.net.Socket
- app/src/main/java/com/example/virtualgamepad/ConnectionViewModel.kt — ViewModel that manages connection state and sends messages
- app/src/main/java/com/example/virtualgamepad/ui/MainScreen.kt — Compose UI: connection status and a full-screen draggable button
- app/src/main/java/com/example/virtualgamepad/MainActivity.kt — Activity that hosts the Compose UI

How to open and run
1. Open this folder in Android Studio (File > Open > C:\Users\anton\Gpx).
2. Let Android Studio sync Gradle. You may need to update Kotlin/Compose versions to match your Android Studio version.
3. Run on an emulator or physical device.

Notes
- The project scaffold is minimal; Gradle wrapper is not included. Use your local Gradle/Android Studio to build.
- The server IP is hardcoded in `ConnectionViewModel.kt` — change it to your PC's local IP and ensure the PC server is listening on the specified port.
