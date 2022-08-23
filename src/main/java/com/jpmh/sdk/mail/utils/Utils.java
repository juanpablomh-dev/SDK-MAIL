
package com.jpmh.sdk.mail.utils;

import com.jpmh.sdk.mail.received.check.gral.Ctes;

public class Utils {
    public static String getReadFolder(String paramFolder) {
        return (!paramFolder.isEmpty() ? paramFolder : Ctes.K_FOLDER_INBOX);
    }
}
