package com.jpmh.sdk.mail.received.check.pop3;

import static com.jpmh.sdk.mail.received.check.CheckMail.K_CONTENT_PRIMARYTYPE_APPLICATION;
import static com.jpmh.sdk.mail.received.check.CheckMail.K_CONTENT_PRIMARYTYPE_MULTIPART;
import static com.jpmh.sdk.mail.received.check.CheckMail.K_CONTENT_PRIMARYTYPE_TEXT;
import com.jpmh.sdk.mail.received.check.ICheckMail;
import com.jpmh.sdk.mail.received.check.gral.Ctes;
import com.jpmh.sdk.mail.received.check.types.ProtocolPOP3Type;
import com.jpmh.sdk.mail.received.check.types.SSLType;
import com.jpmh.sdk.mail.utils.Utils;
import com.jpmh.sdk.mail.utils.UtilsFiles;
import com.sun.mail.util.BASE64DecoderStream;
import com.sun.mail.util.QPDecoderStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.Properties;
import java.util.Scanner;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.ContentType;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class CheckMailPOP3 implements ICheckMail {

    private Properties props;
    private Session session;
    private Store store;
    private Folder folder;
    private JSONObject params;

    @Override
    public void validateParams(JSONObject params) {
        this.params = params;
    }

    @Override
    public String check(JSONObject params) {
        JSONObject result = new JSONObject();
        try {
            validateParams(params);
            setProperties();

            session = Session.getDefaultInstance(props);
            session.setDebug(this.params.getBoolean(Ctes.K_PARAM_DEBUG));

            store = session.getStore(
                    ((this.params.getString(Ctes.K_PARAM_SSL).equalsIgnoreCase(SSLType.STARTTLS.name()))
                    || (this.params.getString(Ctes.K_PARAM_SSL).equalsIgnoreCase(SSLType.SSL.name()))) ? ProtocolPOP3Type.pop3s.name() : ProtocolPOP3Type.pop3.name());

            store.connect(
                    this.params.getString(Ctes.K_PARAM_HOST),
                    this.params.getInt(Ctes.K_PARAM_PORT),
                    this.params.getString(Ctes.K_PARAM_USER_NAME),
                    this.params.getString(Ctes.K_PARAM_PASSWORD)
            );

            folder = store.getFolder(Utils.getReadFolder(this.params.getString(Ctes.K_PARAM_FOLDER_READ)));
            folder.open(Folder.READ_WRITE);

            download();

            folder.close(true);
            store.close();

            result.put(Ctes.K_RESULT, 1);
        } catch (MessagingException | JSONException ex) {
            result.put(Ctes.K_RESULT, 0);
            result.put(Ctes.K_MESSAGE, ex.getMessage());
        }
        return result.toString();
    }

    private void setProperties() {
        props = new Properties();
        props.put("mail.mime.allowutf8", Boolean.TRUE.toString());
        props.put("mail.pop3.auth", this.params.getBoolean(Ctes.K_PARAM_REQUIRES_AUTHENTICATION));
        props.setProperty("mail.pop3.connectiontimeout", String.valueOf(this.params.getInt(Ctes.K_PARAM_TIMEOUT)));
        props.setProperty("mail.pop3.timeout", String.valueOf(this.params.getInt(Ctes.K_PARAM_TIMEOUT)));
        setPropertiesSSLType();
        setPropertiesDomain();
        setPropertiesTLS();
    }

    private void setPropertiesSSLType() {
        if (this.params.getString(Ctes.K_PARAM_SSL).equalsIgnoreCase(SSLType.STARTTLS.name())) {
            props.setProperty("mail.pop3.starttls.enable", Boolean.TRUE.toString());
            props.setProperty("mail.store.protocol", ProtocolPOP3Type.pop3s.name());
            props.setProperty("mail.pop3s.ssl.trust", this.params.getString(Ctes.K_PARAM_HOST));
        }
        if (this.params.getString(Ctes.K_PARAM_SSL).equalsIgnoreCase(SSLType.SSL.name())) {
            props.put("mail.pop3.starttls.enable", Boolean.TRUE.toString());
            props.put("mail.pop3.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.setProperty("mail.store.protocol", ProtocolPOP3Type.pop3s.name());
            props.setProperty("mail.pop3s.ssl.trust", this.params.getString(Ctes.K_PARAM_HOST));
        }
        if (this.params.getString(Ctes.K_PARAM_SSL).equalsIgnoreCase(SSLType.NINGUNO.name())) {
            props.setProperty("mail.store.protocol", ProtocolPOP3Type.pop3.name());
        }
    }

    private void setPropertiesDomain() {
        if (!this.params.getString(Ctes.K_PARAM_DOMAIN).isEmpty()) {
            props.setProperty("mail.auth.ntlm.domain", this.params.getString(Ctes.K_PARAM_DOMAIN));
        }
    }

    private void setPropertiesTLS() {
        if (!this.params.getString(Ctes.K_PARAM_TLS_VERSION).isEmpty()) {
            props.put("mail.pop3s.ssl.protocols", this.params.getString(Ctes.K_PARAM_TLS_VERSION).trim());
            props.put("https.protocols", this.params.getString(Ctes.K_PARAM_TLS_VERSION).trim());
        }
    }

    private void download() throws MessagingException {
        if (this.params.getBoolean(Ctes.K_PARAM_DOWNLOAD_ATTACH)) {
            Message messages[] = folder.getMessages();
            for (Message message : messages) {
                try {
                    MimeMessage cmsg = new MimeMessage((MimeMessage) message);
                    ContentType contentType = new ContentType(cmsg.getContentType());
                    // MULTIPART
                    if (contentType.getPrimaryType().toUpperCase().equals(K_CONTENT_PRIMARYTYPE_MULTIPART)) {
                        downloadContentTypeMultipart(cmsg);
                    } else {
                        // TEXT OR APPLICATION
                        Message msgText = new MimeMessage(session, new ByteArrayInputStream(message.toString().getBytes()));
                        contentType = new ContentType(msgText.getContentType());
                        if (contentType.getPrimaryType().toUpperCase().equals(K_CONTENT_PRIMARYTYPE_TEXT)) {
                            downloadContentTypeText(message);
                        } else {
                            if (contentType.getPrimaryType().toUpperCase().equals(K_CONTENT_PRIMARYTYPE_APPLICATION)) {
                                downloadContentTypeApplication(message);
                            } else {
                                // ERROR
                            }
                        }
                    }
                    delete(message);
                } catch (IOException | MessagingException e) {
                    // LOG ERROR
                    e.printStackTrace();
                }
            }
        }
    }

    private void downloadContentTypeMultipart(MimeMessage cmsg) throws MessagingException, IOException {
        Multipart multiPart = (Multipart) cmsg.getContent();
        for (int i = 0; i < multiPart.getCount(); i++) {
            MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(i);
            BodyPart bodyPart = multiPart.getBodyPart(i);
            if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition()) || StringUtils.isNotBlank(part.getFileName())) {
                String decoded = MimeUtility.decodeText(part.getFileName());
                String fileName = Normalizer.normalize(decoded, Normalizer.Form.NFC);
                String path = this.params.getString(Ctes.K_PARAM_FOLDER_DOWNLOAD_ATTACH) + UtilsFiles.normalizeFileName(fileName);
                File file = new File(path);
                if (!file.exists()) {
                    if (part.getContent() instanceof BASE64DecoderStream) {
                        BASE64DecoderStream base64DecoderStream = ((BASE64DecoderStream) part.getContent());
                        StringWriter writer = new StringWriter();
                        IOUtils.copy(base64DecoderStream, writer);
                        String base64decodedString = writer.toString();
                        UtilsFiles.saveFile(path, new ByteArrayInputStream((base64decodedString).getBytes(StandardCharsets.UTF_8.name())));
                    } else {
                        part.saveFile(path);
                    }
                }
            } else {
                if (bodyPart.getContent().getClass().equals(MimeMultipart.class)) {
                    MimeMultipart mimemultipart = (MimeMultipart) bodyPart.getContent();
                    for (int mime = 0; mime < mimemultipart.getCount(); mime++) {
                        if (mimemultipart.getBodyPart(mime).getFileName() != null) {
                            UtilsFiles.saveFile(
                                    Ctes.K_PARAM_FOLDER_DOWNLOAD_ATTACH + UtilsFiles.normalizeFileName(mimemultipart.getBodyPart(mime).getFileName()),
                                    mimemultipart.getBodyPart(mime).getInputStream()
                            );
                        }
                    }
                }
            }
        }
    }

    private void downloadContentTypeText(Message message) throws MessagingException, IOException {
        if (message.getDataHandler().getContent() != null) {
            String fileName = MimeUtility.decodeText(message.getFileName());
            String path = Ctes.K_PARAM_FOLDER_DOWNLOAD_ATTACH + UtilsFiles.normalizeFileName(fileName);
            File file = new File(path);
            if (!file.exists()) {
                if (message.getDataHandler().getContent() instanceof String) {
                    UtilsFiles.saveFile(path, new ByteArrayInputStream(((String) message.getDataHandler().getContent()).getBytes()));
                }
                if (message.getDataHandler().getContent() instanceof BASE64DecoderStream) {
                    BASE64DecoderStream base64DecoderStream = ((BASE64DecoderStream) message.getDataHandler().getContent());
                    StringWriter writer = new StringWriter();
                    IOUtils.copy(base64DecoderStream, writer);
                    String base64decodedString = writer.toString();
                    UtilsFiles.saveFile(path, new ByteArrayInputStream((base64decodedString).getBytes(StandardCharsets.UTF_8.name())));
                }
                if (message.getDataHandler().getContent() instanceof QPDecoderStream) {
                    Scanner s = new Scanner(((InputStream) message.getDataHandler().getContent())).useDelimiter("\\A");
                    String xml = s.hasNext() ? s.next() : "";
                    UtilsFiles.saveFile(path, new ByteArrayInputStream((xml).getBytes()));
                }
            }
        }
    }

    private void downloadContentTypeApplication(Message message) throws MessagingException, IOException {
        Message msgText = new MimeMessage(session, new ByteArrayInputStream(message.toString().getBytes()));
        Multipart multiPart = (Multipart) msgText.getContent();
        for (int x = 0; x < multiPart.getCount(); x++) {
            MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(x);
            if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition()) || StringUtils.isNotBlank(part.getFileName())) {
                if (part.getFileName() != null) {
                    String decoded = MimeUtility.decodeText(part.getFileName());
                    String filename = Normalizer.normalize(decoded, Normalizer.Form.NFC);
                    String path = Ctes.K_PARAM_FOLDER_DOWNLOAD_ATTACH + UtilsFiles.normalizeFileName(filename);
                    File file = new File(path);
                    if (!file.exists()) {
                        part.saveFile(path);
                    }
                }
            }
        }
    }

    private void delete(Message message) throws MessagingException {
        if (this.params.getBoolean(Ctes.K_PARAM_DELETE_READ_MAIL)) {
            message.setFlag(Flags.Flag.DELETED, true);
        }
    }

}
