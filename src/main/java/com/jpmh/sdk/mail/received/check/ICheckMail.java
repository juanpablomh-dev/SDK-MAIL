package com.jpmh.sdk.mail.received.check;

import org.json.JSONObject;

public interface ICheckMail {
    void validateParams(JSONObject params);
    String check(JSONObject params);
}
