package org.thoughtcrime.securesms.mediasend;

import org.thoughtcrime.securesms.enterprise.EnterpriseConfig;

public class MediaSendConstants {
  // Signal-Enterprise policy: media batch size is centralized in EnterpriseConfig (default 100).
  public static final int MAX_PUSH = EnterpriseConfig.DEFAULT_MAX_MEDIA_BATCH;
  public static final int MAX_SMS  = 1;
}
