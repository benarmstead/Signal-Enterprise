# Signal-Enterprise

Signal-Enterprise is a fork of [Signal-Android](https://github.com/signalapp/Signal-Android)
that turns Signal's ad-hoc privacy tweaks into a **centralized, policy-driven configuration
layer** suitable for managed / organizational deployment — while tracking upstream closely.

This document is the reference for the fork's customization surface, its enterprise policy layer,
managed-configuration (MDM/EMM) support, and the process for staying in sync with upstream.

---

## 1. Policy layer: `EnterpriseConfig`

Every behavioural difference from upstream Signal lives in a single class:

`app/src/main/java/org/thoughtcrime/securesms/enterprise/EnterpriseConfig.kt`

Each policy is a named flag with a documented compile-time default (the "Enterprise baseline").
Nothing else in the codebase hardcodes these values — call sites read `EnterpriseConfig`. This
replaces the previous approach of scattered `if (true) return;` hacks and magic-number defaults,
which silently broke whenever upstream refactored the surrounding code.

| Policy | Key | Default | Effect |
| --- | --- | --- | --- |
| Send read receipts | `send_read_receipts` | `false` | Never send read receipts, regardless of the in-app setting |
| Send typing indicators | `send_typing_indicators` | `false` | Never send typing indicators, regardless of the in-app setting |
| Honor incoming remote deletes | `honor_incoming_remote_deletes` | `false` | Deletes from other users / group admins are ignored; messages stay on-device |
| Default high-quality media | `default_high_quality_media` | `true` | Media sends default to high quality |
| Incognito keyboard | `incognito_keyboard` | `true` | Request keyboards disable personalized learning |
| Screen security | `screen_security` | `true` | `FLAG_SECURE` on by default (blocks screenshots / recents preview) |
| Disable PIN reminders | `disable_pin_reminders` | `true` | Suppress periodic PIN reminder prompts |
| Hide store rating prompt | `hide_store_rating_prompt` | `true` | Never show the app-store rating dialog |
| Max media per send | `max_media_batch` | `100` | Maximum photos/videos per message (upstream default is 32) |

### Where each policy is applied

- `jobs/SendReadReceiptJob.java` — gated on `sendReadReceipts`
- `jobs/TypingSendJob.java` — gated on `sendTypingIndicators`
- `util/MessageConstraintsUtil.kt` — `honorIncomingRemoteDeletes` controls the receive thresholds
- `keyvalue/SettingsValues.java` — `defaultHighQualityMedia`
- `util/TextSecurePreferences.java` — `incognitoKeyboard`, `screenSecurity`
- `keyvalue/PinValues.java` — `disablePinReminders`
- `conversationlist/ConversationListFragment.java` — `hideStoreRatingPrompt`
- `mediasend/v2/MediaSelectionState.kt` — `maxMediaBatch` (the live media cap; the old
  `MediaSendConstants.MAX_PUSH` was orphaned by upstream's media-send v2 refactor)

---

## 2. Managed configuration (MDM / EMM)

Signal-Enterprise supports **Android managed configuration** (a.k.a. application restrictions),
the standard mechanism by which an Enterprise Mobility Management suite — Microsoft Intune,
VMware Workspace ONE, MobileIron, Android Enterprise, etc. — pushes policy to a managed app.

- The manageable keys are declared in `app/src/main/res/xml/app_restrictions.xml`. EMM consoles
  read this schema and present each policy to administrators with a title and description.
- At app startup, `enterprise/EnterpriseManagedConfig.kt` reads the current restrictions via
  `RestrictionsManager` and overlays them onto `EnterpriseConfig`.
- **Precedence:** managed value (if set) > compile-time default.
- Policy changes take effect on the **next app launch**.

### Example (Android Enterprise managed-config JSON)

```json
{
  "send_read_receipts": false,
  "screen_security": true,
  "honor_incoming_remote_deletes": false,
  "max_media_batch": 100
}
```

### Adding a new managed policy

1. Add `KEY_…`, a `DEFAULT_…`, and an accessor to `EnterpriseConfig.kt`.
2. Add a matching `<restriction>` to `app_restrictions.xml` (same key).
3. If it carries an integer, add the key to `EnterpriseManagedConfig.INT_KEYS`.
4. Read `EnterpriseConfig.<accessor>` at the relevant call site.

---

## 3. Staying in sync with upstream

Use the helper script:

```bash
scripts/update-from-upstream.sh              # merge upstream/main into the current branch
scripts/update-from-upstream.sh --dry-run    # fetch + show divergence and likely conflict files
scripts/update-from-upstream.sh --branch main
```

It ensures the `upstream` remote (`https://github.com/signalapp/Signal-Android.git`) exists,
fetches it, reports how far behind/ahead you are, and merges. Conflicts almost always land in the
small customization surface listed in section 1 — resolve by keeping the Enterprise policy while
adopting upstream's surrounding changes, then `git add` + `git commit`.

After merging, always run the same checks CI runs:

```bash
./gradlew qa      # full Android lint + checks (what CI runs on pushes)
./gradlew ci      # faster lint (what CI runs on PRs)
```

---

## 4. CI / CD

- **`.github/workflows/android.yml`** — runs the `ci` task (fast-lint + compile all modules + unit
  tests + instrumentation compile) on PRs and on pushes to `master` / `main` / version branches.
  Two fork-specific fixes vs upstream's workflow: (1) it builds `master` (the fork's default branch;
  upstream only built `main`/`8.**`), and (2) it constrains the Gradle/Kotlin daemon heap for a free
  16GB hosted runner — upstream's committed `-Xmx12g` targets their 32GB runners + private remote
  build cache and OOM-kills a free runner during the test phase.
- **`.github/workflows/build.yml`** — builds `assemblePlayProdRelease`, signs the APKs with the
  `KEYSTORE_BASE64` / `KEYSTORE_PASS` repo secrets, and publishes a GitHub Release. Triggered by
  pushes to a `v*` branch or manually via **workflow_dispatch**.
- **`.github/workflows/yearly-release.yml`** — on Jan 1 each year, cuts a `v<year>` branch, which
  triggers `build.yml`.
- **`.github/workflows/diffuse.yml`** — posts an APK size/diff comment on PRs.

### Release secrets

| Secret | Purpose |
| --- | --- |
| `KEYSTORE_BASE64` | base64 of the release keystore (`base64 -i apksign.keystore`) |
| `KEYSTORE_PASS` | keystore password |

Generate a keystore with:

```bash
keytool -genkey -v -keystore apksign.keystore -alias apksign -keyalg RSA -keysize 4096
```
