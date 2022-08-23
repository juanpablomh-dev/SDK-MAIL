package com.jpmh.sdk.mail.received.check.graphapi;

import com.jpmh.sdk.mail.received.check.ICheckMail;
import com.jpmh.sdk.mail.received.check.ICheckMailFactory;

public class CheckMailFactoryGRAPHAPI implements ICheckMailFactory {

    @Override
    public ICheckMail produceICheckMail() {
        return new CheckMailGRAPHAPI();
    }

}
