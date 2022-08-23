package com.jpmh.sdk.mail.received.check.ews;

import com.jpmh.sdk.mail.received.check.ICheckMail;
import com.jpmh.sdk.mail.received.check.ICheckMailFactory;

public class CheckMailFactoryEWS implements ICheckMailFactory {

    @Override
    public ICheckMail produceICheckMail() {
        return new CheckMailEWS();
    }

}
