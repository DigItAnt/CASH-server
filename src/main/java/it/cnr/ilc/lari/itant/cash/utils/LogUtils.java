package it.cnr.ilc.lari.itant.cash.utils;

import java.security.Principal;

public class LogUtils {
    public static String CASH_INVOCATION_LOG_MSG = "CASH INVOCATION, USER: {}, REQ_ID: {}, CUSTOM MSG: {}";

    public static String getPrincipalName(Principal principal) {
        if (principal == null)
            return "anonymous";
        return principal.getName();
    }
}
