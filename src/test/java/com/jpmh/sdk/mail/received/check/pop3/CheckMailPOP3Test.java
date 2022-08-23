package com.jpmh.sdk.mail.received.check.pop3;

import com.jpmh.sdk.mail.received.check.CheckMail;
import com.jpmh.sdk.mail.received.check.gral.Ctes;
import junit.framework.TestCase;
import org.json.JSONObject;

public class CheckMailPOP3Test extends TestCase {
    
    public void testCheck() {
        JSONObject expected  = new JSONObject();
        expected.put(Ctes.K_RESULT, 1);
        
        JSONObject params = new JSONObject();
        params.put(Ctes.K_PARAM_HOST, "pop.gmail.com");
        params.put(Ctes.K_PARAM_USER_NAME, "jp.pruebas.dev@gmail.com");
        params.put(Ctes.K_PARAM_PASSWORD, "qorcalumaljjhqjqnwq");
        params.put(Ctes.K_PARAM_PORT, 995);
        params.put(Ctes.K_PARAM_SSL, "SSL");
        params.put(Ctes.K_PARAM_REQUIRES_AUTHENTICATION, true);
        params.put(Ctes.K_PARAM_FOLDER_DOWNLOAD_ATTACH, "C:\\temp\\");
        params.put(Ctes.K_PARAM_PROTOCOL, "POP3");
        //spam
        params.put(Ctes.K_PARAM_DOMAIN, "");
        params.put(Ctes.K_PARAM_TIMEOUT, 0);
        params.put(Ctes.K_PARAM_TLS_VERSION, "TLSv1.2");
        params.put(Ctes.K_PARAM_DEBUG, true);
        params.put(Ctes.K_PARAM_DOWNLOAD_ATTACH, false);
        params.put(Ctes.K_PARAM_DELETE_READ_MAIL, false);

        CheckMail checkMail = new CheckMail();
        JSONObject result = new JSONObject(checkMail.check(params.toString()));
        
        assertEquals("Result:", expected.toString(), result.toString());
    }

}
