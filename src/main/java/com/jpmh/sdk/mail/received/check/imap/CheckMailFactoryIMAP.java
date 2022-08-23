
package com.jpmh.sdk.mail.received.check.imap;

import com.jpmh.sdk.mail.received.check.ICheckMail;
import com.jpmh.sdk.mail.received.check.ICheckMailFactory;

public class CheckMailFactoryIMAP implements ICheckMailFactory {

    @Override
    public ICheckMail produceICheckMail() {
        return new CheckMailIMAP();
    }
    
}
