package com.copytrading.gmail;

import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IdleManager;

import javax.mail.*;
import javax.mail.event.MessageCountAdapter;
import javax.mail.event.MessageCountEvent;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.copytrading.util.ConfigUtils.getProperty;

public class EmailJavaxApp {
    private static String host = "smtp.gmail.com";
    private static String protocol = "smtp";
    private static String debug = "true";
    private static String auth = "true";
    private static String enable = "true";

    public static void main(String[] args) throws MessagingException, IOException {
        String username = getProperty("gmail.username");
        String password = getProperty("gmail.password");

        IMAPFolder folder = null;
        Store store = null;
        String subject = null;
        Flags.Flag flag = null;
        System.out.println(password);
        try
        {
            Properties props = System.getProperties();
            props.setProperty("mail.store.protocol", "imaps");
            props.setProperty("mail.imaps.usesocketchannels", "true");
            props.setProperty("mail.transport.protocol", protocol);
            props.setProperty("mail.debug", debug);
            props.setProperty("mail.smtp.auth", auth);
            props.setProperty("mail.smtp.starttls.enable", enable);
            props.setProperty("mail.smtp.ssl.protocols", "TLSv1.2");

            Session session = Session.getDefaultInstance(props, null);

            ExecutorService es = Executors.newCachedThreadPool();
            final IdleManager idleManager = new IdleManager(session, es);

            store = session.getStore("imaps");
            store.connect(host, username, password);

            folder = (IMAPFolder) store.getFolder("INBOX");


            if(!folder.isOpen())
                folder.open(Folder.READ_WRITE);

            folder.addMessageCountListener(new MessageCountAdapter() {
                public void messagesAdded(MessageCountEvent ev) {
                    Folder folder = (Folder)ev.getSource();
                    Message[] msgs = ev.getMessages();
                    System.out.println("Folder: " + folder +
                            " got " + msgs.length + " new messages");
                    try {
                        // process new messages
                        idleManager.watch(folder); // keep watching for new messages
                    } catch (MessagingException mex) {
                        // handle exception related to the Folder
                    }
                }
            });
            idleManager.watch(folder);


            Message[] messages = folder.getMessages();
            System.out.println("No of Messages : " + folder.getMessageCount());
            System.out.println("No of Unread Messages : " + folder.getUnreadMessageCount());
            System.out.println(messages.length);


            for (int i = messages.length-1; i >= 0; i--) {
                System.out.println("*****************************************************************************");
                System.out.println("MESSAGE " + (i + 1) + ":");
                Message msg =  messages[i];

                subject = msg.getSubject();

                System.out.println("Subject: " + subject);
                System.out.println("From: " + msg.getFrom()[0]);
                System.out.println("To: " + msg.getAllRecipients()[0]);
                System.out.println();
            }
        } finally {
            if (folder != null && folder.isOpen()) {
                folder.close(true);
            }
            if (store != null) {
                store.close();
            }
        }

    }
}
