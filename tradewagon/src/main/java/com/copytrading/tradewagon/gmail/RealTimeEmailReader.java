package com.copytrading.tradewagon.gmail;

import javax.mail.*;
import javax.mail.event.MessageCountEvent;
import javax.mail.event.MessageCountListener;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Properties;

public class RealTimeEmailReader {

    public static void main(String[] args) throws MessagingException {
        final String HOST = "imap.gmail.com";
        String username = getLocalProperties().getProperty("gmail.username");
        String password = getLocalProperties().getProperty("gmail.password");

        Store store = Session.getDefaultInstance(getProperties()).getStore("imaps");
        store.connect(HOST, username, password);

        Folder folder = store.getFolder("inbox");

        folder.addMessageCountListener(new MessageCountListener() {
            @Override
            public void messagesAdded(MessageCountEvent messageCountEvent) {
                System.out.println("CALLED MESSAGES ADDED");
                Message[] messages = messageCountEvent.getMessages();
                for (Message s : messages) {
                    try {
                        System.out.println("New email: " + s.getSubject());
                    } catch (MessagingException e) {
                        throw new RuntimeException(e); //todo
                    }
                }
            }

            @Override
            public void messagesRemoved(MessageCountEvent messageCountEvent) {
                System.out.println("Message removed");
            }
        });

        // Бесконечный цикл для ожидания событий
        while (true) {
            try {
                Thread.sleep(1); // Приостанавливаем выполнение на 1 секунду
            } catch (InterruptedException e) {
                // Обработка ошибок
            }
        }

       /* try {
            Session session = Session.getDefaultInstance(getProperties());
            Store store = session.getStore("imaps");
            store.connect(HOST, username, password);

            // Open the inbox folder in read/write mode
            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);
            MessageCountListener messageCountListener = new MessageCountListener() {
                @Override
                public void messagesAdded(MessageCountEvent event) {
                    try {
                        // Get the new messages
                        Message[] messages = event.getMessages();

                        for (Message message : messages) {
                            System.out.println("New Email Subject: " + message.getSubject());
                            System.out.println("New Email Text: " + message);
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
        }*/
    }

    private static Properties getProperties() {
        Properties properies = new Properties();
        properies.put("mail.smtp.host", "smtp.gmail.com");
        properies.put("mail.smtp.port", "465");
        properies.put("mail.smtp.auth", "true");
        properies.put("mail.smtp.starttls.enable", "true");
        properies.put("mail.smtp.starttls.required", "true");
        properies.put("mail.smtp.ssl.protocols", "TLSv1.2");
        properies.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        return properies;
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