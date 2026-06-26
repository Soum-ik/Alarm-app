# Alearm landing page + release checklist

A single static page (`index.html`) that lets people download and sideload the Alearm APK.
No build step, no backend — host the file as-is.

## One-time: release signing

1. Generate a keystore (back it up forever — losing it means you can never update the app):
   ```sh
   keytool -genkeypair -v -keystore alearm-release.keystore \
     -alias alearm -keyalg RSA -keysize 2048 -validity 10000
   ```
2. Copy the template and fill it in:
   ```sh
   cp keystore.properties.template keystore.properties   # (run from repo root)
   ```
   `keystore.properties` is gitignored — secrets never get committed.

## Each release: build the APK

```sh
./gradlew assembleRelease
# -> app/build/outputs/apk/release/app-release.apk
```

Verify the *release* APK on a real device before shipping (R8 minify is on, so confirm
the alarm, barcode scan, charger gate, and reboot survival all work from a clean install):

```sh
adb install -r app/build/outputs/apk/release/app-release.apk
```

Bump `versionCode` and `versionName` in `app/build.gradle.kts` for every new release.

## Host the APK (GitHub Releases)

1. Rename the artifact to `alearm.apk` (the landing button links to `.../releases/latest/download/alearm.apk`).
2. Create a release tag and upload it:
   ```sh
   gh release create v1.0 app/build/outputs/apk/release/app-release.apk#alearm.apk \
     --title "Alearm 1.0" --notes "First public build."
   ```
   Using `latest/download/alearm.apk` means the landing page button auto-points at the newest release.

## Host the page (GitHub Pages)

- Repo **Settings → Pages → Source: Deploy from a branch**, pick `main` and folder `/landing`
  (or move `index.html` to the path Pages expects).
- Optional custom domain: add `alearm.growthmap.co` in Pages settings and a CNAME DNS record.

## Notes

- The download button in `index.html` reads its real URL from the `href-release` attribute.
  Update that attribute if your repo/asset name changes.
- The page is Android-only by design and says so — iPhone users cannot install an APK.
