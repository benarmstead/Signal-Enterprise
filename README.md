# Signal-Enterprise

A fork of [Signal for Android](https://github.com/signalapp/Signal-Android) with a centralized,
policy-driven configuration layer and **managed-configuration (MDM/EMM) support** for
organizational deployment. It tracks upstream Signal closely.

[Install](https://github.com/benarmstead/Signal-Enterprise/releases/latest)

## Main differences to the official Signal app

- Does not send read receipts, regardless of the in-app setting
- Does not send typing indicators, regardless of the in-app setting
- Remotely deleted messages (from users **and** group admins) are ignored and stay on your device
- Defaults to high-quality mode for sending images and video
- Allows sending up to 100 media items at once (Signal's default is 32)
- **Screen security (`FLAG_SECURE`) on by default** — blocks screenshots and hides content in the
  recent-apps preview (enterprise data-loss prevention)

Quality-of-life differences:

- No app-store rating popups
- PIN reminders disabled by default
- Incognito keyboard on by default

## Enterprise policy & MDM

Every difference above is a single named flag in
[`EnterpriseConfig`](app/src/main/java/org/thoughtcrime/securesms/enterprise/EnterpriseConfig.kt),
with a documented default that an EMM/MDM console (Intune, Workspace ONE, Android Enterprise, …)
can override at runtime via Android managed configuration. See **[docs/ENTERPRISE.md](docs/ENTERPRISE.md)**
for the full policy table, the managed-config schema, and deployment details.

## Updating from upstream

```bash
scripts/update-from-upstream.sh --dry-run   # show how far behind upstream you are
scripts/update-from-upstream.sh             # merge upstream/main
```

Conflicts almost always land in the small customization surface documented in
[docs/ENTERPRISE.md](docs/ENTERPRISE.md).

## Building & releasing

CI runs `./gradlew ci` on PRs and `./gradlew qa` on pushes. Tagged releases are built, signed, and
published by [`build.yml`](.github/workflows/build.yml) — see
[docs/ENTERPRISE.md § 4](docs/ENTERPRISE.md#4-ci--cd) for the required signing secrets.

---

Feel free to open an [issue](https://github.com/benarmstead/Signal-Enterprise/issues) for feature
requests.

> For the original Signal README and legal/license information, see [README.old.md](/README.old.md).
