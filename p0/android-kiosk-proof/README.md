# FocusForge P0-A — Device Owner / Lock Task Proof

Disposable risk-validation harness; not the production app.

## Purpose
- Prove Device Owner provisioning on the target physical device.
- Prove Lock Task behavior.
- Test startup/recovery after process death and reboot.
- Identify Android/OEM limitations before P1.

## Target hardware
The supplied device screenshot establishes:
- Device: **OPPO K13 Turbo Pro 5G**
- OEM: **OPPO**
- ColorOS: **16.0.9**
- SoC: Snapdragon 8s Gen 4
- RAM: 12 GB
- Storage: 256 GB

**Android version is not established by the screenshot and remains TODO.** ColorOS version must not be treated as the Android version.

## Important safety boundary
A successful build does NOT mean P0-A passes. P0-A requires real-device evidence.
Device Owner provisioning can require a fresh/unprovisioned device and may wipe device data. Back up important data first.
This harness does not disable emergency functionality, bypass Android security, hide itself, or attempt to make the device unrecoverable.

## Build
Open this directory in Android Studio with a compatible Android SDK/Gradle environment.

Application ID: `com.focusforge.p0.kiosk`

## Development provisioning
For a suitable development device, Android's device-management tooling can provision the package as Device Owner. One commonly used ADB development path is:

`adb shell dpm set-device-owner com.focusforge.p0.kiosk/.FocusDeviceAdminReceiver`

Exact provisioning requirements vary by Android release and device state. Do not run provisioning against a phone containing data you cannot afford to lose.

## Hardware test record
| Field | Result |
|---|---|
| Device model | **OPPO K13 Turbo Pro 5G** |
| OEM | **OPPO** |
| ColorOS | **16.0.9** |
| Android version | **16** |
| Build/security patch | TODO |
| Provisioning method | TODO |
| Device Owner | TODO |
| Lock Task | TODO |
| Launcher/startup behavior | TODO |
| Allowed apps | TODO |
| Restricted apps | TODO |
| Process restart recovery | TODO |
| Reboot recovery | TODO |
| Session persistence | TODO |
| Safe recovery | TODO |
| OEM limitations | TODO |

## Acceptance
P0-A passes only after Device Owner and Lock Task are demonstrated on hardware, recovery and reboot/process-failure behavior are tested, unsupported guarantees are documented, and the resulting architecture decision is recorded in the repository.

## Current status
Implementation harness: READY FOR REAL-DEVICE TESTING.
Hardware proof: NOT YET VERIFIED.
