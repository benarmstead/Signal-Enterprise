package org.thoughtcrime.securesms.enterprise

/**
 * Central policy layer for Signal-Enterprise.
 *
 * Every behavioural difference from upstream Signal-Android lives here as a single named,
 * documented flag with a compile-time default (the Enterprise baseline). This replaces the
 * scattered `if (true) return;` hacks and hardcoded defaults that previously lived across the
 * codebase, so the whole policy surface can be read and reasoned about in one place.
 *
 * Defaults may additionally be overridden at runtime by an EMM/MDM (Intune, Workspace ONE,
 * MobileIron, Android Enterprise, ...) through Android managed configuration. The managed
 * values are read by [EnterpriseManagedConfig] and declared for management consoles in
 * `res/xml/app_restrictions.xml`. The key constants below are shared between all three.
 *
 * Precedence: managed configuration overlay (if present) > compile-time default.
 */
object EnterpriseConfig {

  // --- Managed-configuration keys (must match res/xml/app_restrictions.xml) ------------------

  const val KEY_SEND_READ_RECEIPTS = "send_read_receipts"
  const val KEY_SEND_TYPING_INDICATORS = "send_typing_indicators"
  const val KEY_HONOR_INCOMING_REMOTE_DELETES = "honor_incoming_remote_deletes"
  const val KEY_DEFAULT_HIGH_QUALITY_MEDIA = "default_high_quality_media"
  const val KEY_INCOGNITO_KEYBOARD = "incognito_keyboard"
  const val KEY_SCREEN_SECURITY = "screen_security"
  const val KEY_DISABLE_PIN_REMINDERS = "disable_pin_reminders"
  const val KEY_HIDE_STORE_RATING_PROMPT = "hide_store_rating_prompt"
  const val KEY_MAX_MEDIA_BATCH = "max_media_batch"

  // --- Compile-time defaults (the Enterprise baseline) ---------------------------------------

  private const val DEFAULT_SEND_READ_RECEIPTS = false
  private const val DEFAULT_SEND_TYPING_INDICATORS = false
  private const val DEFAULT_HONOR_INCOMING_REMOTE_DELETES = false
  private const val DEFAULT_DEFAULT_HIGH_QUALITY_MEDIA = true
  private const val DEFAULT_INCOGNITO_KEYBOARD = true
  private const val DEFAULT_SCREEN_SECURITY = true
  private const val DEFAULT_DISABLE_PIN_REMINDERS = true
  private const val DEFAULT_HIDE_STORE_RATING_PROMPT = true

  /** Maximum number of media items allowed in a single send (upstream default is 32). */
  const val DEFAULT_MAX_MEDIA_BATCH = 100

  // --- Runtime overlay pushed by managed configuration ---------------------------------------

  @Volatile
  private var overlay: Map<String, Any> = emptyMap()

  /** Replace the managed-configuration overlay. Called by [EnterpriseManagedConfig]. */
  @JvmStatic
  fun applyManagedOverlay(values: Map<String, Any>) {
    overlay = values
  }

  private fun bool(key: String, default: Boolean): Boolean = (overlay[key] as? Boolean) ?: default

  private fun int(key: String, default: Int): Int = (overlay[key] as? Int) ?: default

  // --- Public policy accessors ---------------------------------------------------------------

  /** When false, outgoing read receipts are never sent regardless of the in-app setting. */
  @get:JvmStatic
  val sendReadReceipts: Boolean get() = bool(KEY_SEND_READ_RECEIPTS, DEFAULT_SEND_READ_RECEIPTS)

  /** When false, typing indicators are never sent regardless of the in-app setting. */
  @get:JvmStatic
  val sendTypingIndicators: Boolean get() = bool(KEY_SEND_TYPING_INDICATORS, DEFAULT_SEND_TYPING_INDICATORS)

  /** When false, remote deletes received from other users/admins are ignored (kept on-device). */
  @get:JvmStatic
  val honorIncomingRemoteDeletes: Boolean get() = bool(KEY_HONOR_INCOMING_REMOTE_DELETES, DEFAULT_HONOR_INCOMING_REMOTE_DELETES)

  /** When true, media sends default to high quality. */
  @get:JvmStatic
  val defaultHighQualityMedia: Boolean get() = bool(KEY_DEFAULT_HIGH_QUALITY_MEDIA, DEFAULT_DEFAULT_HIGH_QUALITY_MEDIA)

  /** When true, the incognito-keyboard (`IME_FLAG_NO_PERSONALIZED_LEARNING`) flag defaults on. */
  @get:JvmStatic
  val incognitoKeyboard: Boolean get() = bool(KEY_INCOGNITO_KEYBOARD, DEFAULT_INCOGNITO_KEYBOARD)

  /** When true, screen security (`FLAG_SECURE`, blocks screenshots/recents preview) defaults on. */
  @get:JvmStatic
  val screenSecurity: Boolean get() = bool(KEY_SCREEN_SECURITY, DEFAULT_SCREEN_SECURITY)

  /** When true, periodic PIN reminders are suppressed. */
  @get:JvmStatic
  val disablePinReminders: Boolean get() = bool(KEY_DISABLE_PIN_REMINDERS, DEFAULT_DISABLE_PIN_REMINDERS)

  /** When true, the app store rating prompt is never shown. */
  @get:JvmStatic
  val hideStoreRatingPrompt: Boolean get() = bool(KEY_HIDE_STORE_RATING_PROMPT, DEFAULT_HIDE_STORE_RATING_PROMPT)

  /** Maximum media items per send. */
  @get:JvmStatic
  val maxMediaBatch: Int get() = int(KEY_MAX_MEDIA_BATCH, DEFAULT_MAX_MEDIA_BATCH)
}
