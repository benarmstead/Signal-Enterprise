package org.thoughtcrime.securesms.enterprise

import android.content.Context
import android.content.RestrictionsManager
import org.signal.core.util.logging.Log

/**
 * Reads Android managed configuration (a.k.a. application restrictions) and applies it as an
 * overlay onto [EnterpriseConfig].
 *
 * Managed configuration is the standard mechanism by which an Enterprise Mobility Management
 * (EMM/MDM) suite — Intune, Workspace ONE, MobileIron, Android Enterprise, etc. — pushes policy
 * to a managed app. The manageable keys are declared in `res/xml/app_restrictions.xml` and are
 * surfaced by the EMM console to administrators.
 *
 * This is refreshed on application start (see `ApplicationContext#onCreate`). Any policy an admin
 * changes therefore takes effect on the next app launch.
 */
object EnterpriseManagedConfig {

  private val TAG = Log.tag(EnterpriseManagedConfig::class.java)

  /** Keys that carry integer values; everything else is read as a boolean. */
  private val INT_KEYS = setOf(EnterpriseConfig.KEY_MAX_MEDIA_BATCH)

  @JvmStatic
  fun refresh(context: Context) {
    try {
      val restrictionsManager = context.getSystemService(Context.RESTRICTIONS_SERVICE) as? RestrictionsManager
      val restrictions = restrictionsManager?.applicationRestrictions

      if (restrictions == null || restrictions.isEmpty) {
        Log.i(TAG, "No managed configuration present; using Enterprise defaults.")
        EnterpriseConfig.applyManagedOverlay(emptyMap())
        return
      }

      val overlay = HashMap<String, Any>()
      for (key in restrictions.keySet()) {
        if (key in INT_KEYS) {
          overlay[key] = restrictions.getInt(key)
        } else {
          overlay[key] = restrictions.getBoolean(key)
        }
      }

      EnterpriseConfig.applyManagedOverlay(overlay)
      Log.i(TAG, "Applied managed configuration overlay for keys: ${overlay.keys}")
    } catch (t: Throwable) {
      // Never let a bad/absent policy read break app startup.
      Log.w(TAG, "Failed to read managed configuration; using Enterprise defaults.", t)
    }
  }
}
