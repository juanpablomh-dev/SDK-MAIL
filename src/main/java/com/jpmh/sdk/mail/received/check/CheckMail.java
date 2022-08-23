package com.jpmh.sdk.mail.received.check;

import com.jpmh.sdk.mail.received.check.graphapi.CheckMailFactoryGRAPHAPI;
import com.jpmh.sdk.mail.received.check.imap.CheckMailFactoryIMAP;
import com.jpmh.sdk.mail.received.check.ews.CheckMailFactoryEWS;
import com.jpmh.sdk.mail.received.check.pop3.CheckMailFactoryPOP3;
import com.jpmh.sdk.mail.received.check.gral.Ctes;
import com.jpmh.sdk.mail.received.check.types.ProtocolType;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;

public class CheckMail {

    public static final String K_CONTENT_PRIMARYTYPE_MULTIPART = "MULTIPART";
    public static final String K_CONTENT_PRIMARYTYPE_TEXT = "TEXT";
    public static final String K_CONTENT_PRIMARYTYPE_APPLICATION = "APPLICATION";
    
    private JSONObject params;

    public CheckMail() {
    }

    public JSONObject getParams() {
        return this.params;
    }

    public void setParams(JSONObject paramsJSON) {
        this.params = paramsJSON;
    }

    public String check(String params) {
        setParams(new JSONObject(params));
        ICheckMailFactory factory = createSpecificFactory();
        ICheckMail checkMail = factory.produceICheckMail();
        System.setProperty("file.encoding", StandardCharsets.UTF_8.name());
        return checkMail.check(this.params);
    }

    private ICheckMailFactory createSpecificFactory() {
        final ProtocolType protocol = ProtocolType.valueOf(this.params.getString(Ctes.K_PARAM_PROTOCOL));
        switch (protocol) {
            case POP3:
                return new CheckMailFactoryPOP3();
            case IMAP:
                return new CheckMailFactoryIMAP();
            case EWS:
                return new CheckMailFactoryEWS();
            case GRAPHAPI:
                return new CheckMailFactoryGRAPHAPI();
            default:
                return null;
        }
    }
    
}
