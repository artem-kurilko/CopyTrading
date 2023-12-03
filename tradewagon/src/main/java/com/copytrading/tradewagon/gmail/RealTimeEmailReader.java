package com.copytrading.tradewagon.gmail;

import javax.mail.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Properties;
import javax.mail.event.MessageCountListener;
import javax.mail.event.MessageCountEvent;

import com.sun.mail.imap.IdleManager;
import lombok.Value;

public class RealTimeEmailReader {

    public static void main(String[] args) {
        final String HOST = "imap.gmail.com";
        String username = getLocalProperties().getProperty("gmail.username");
        String password = getLocalProperties().getProperty("gmail.password");

        Properties properies = new Properties();
        properies.put("mail.smtp.host", "smtp.gmail.com");
        properies.put("mail.smtp.port", "465");
        properies.put("mail.smtp.auth", "true");
        properies.put("mail.smtp.starttls.enable", "true");
        properies.put("mail.smtp.starttls.required", "true");
        properies.put("mail.smtp.ssl.protocols", "TLSv1.2");
        properies.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
     /*   properies.put("mail.store.protocol", "imaps");
        properies.put("mail.imaps.host", HOST);
        properies.put("mail.imaps.port", "993");
        properies.put("mail.imaps.ssl.enable", "true");
        properies.put("mail.smtp.starttls.required", "true");
        properies.put("mail.smtp.ssl.protocols", "TLSv1.2");*/

        try {
            Session session = Session.getDefaultInstance(properies);
            Store store = session.getStore("imaps");
            store.connect(HOST, username, password);

            // Open the inbox folder in read/write mode
            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);
            System.out.println("Finished");
            MessageCountListener messageCountListener = new MessageCountListener() {
                @Override
                public void messagesAdded(MessageCountEvent event) {
                    try {
                        // Get the new messages
                        Message[] messages = event.getMessages();

                        for (Message message : messages) {
                            System.out.println("New Email Subject: " + message.getSubject());
                        }
                    } catch (MessagingException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void messagesRemoved(MessageCountEvent e) {
                // TODO Auto-generated method stub
                }
            };

            // Register the message count listener with the folder
            inbox.addMessageCountListener(messageCountListener);

            // Start the IMAP IDLE mechanism to receive real-time notifications
            IdleManager idleManager = new IdleManager(session, null);
            idleManager.watch(inbox);

            while (true) {
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Properties getLocalProperties() {
        String rootPath = Paths.get("").toAbsolutePath().toString();
        String appConfigPath = rootPath + "/tradewagon/src/main/resources/application.properties";
        Properties appProps = new Properties();
        try {
            appProps.load(new FileInputStream(appConfigPath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } return appProps;
    }
}