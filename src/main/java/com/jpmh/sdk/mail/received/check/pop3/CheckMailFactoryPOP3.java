package com.jpmh.sdk.mail.received.check.pop3;

import com.jpmh.sdk.mail.received.check.ICheckMail;
import com.jpmh.sdk.mail.received.check.ICheckMailFactory;

public class CheckMailFactoryPOP3 implements ICheckMailFactory{

    @Override
    public ICheckMail produceICheckMail() {
        return new CheckMailPOP3();
    }

}
