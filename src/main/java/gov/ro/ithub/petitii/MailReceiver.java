package gov.ro.ithub.petitii;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.mail.ImapIdleChannelAdapter;
import org.springframework.integration.mail.ImapMailReceiver;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.support.GenericMessage;


import javax.activation.DataHandler;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.Properties;

/**
 * Created by ciprian.pavel on 11/23/2016.
 */
@Configuration
@EnableIntegration
public class MailReceiver {

    private static Logger logger = Logger.getLogger(MailReceiver.class);

    @Value("${imap.connection.username}")
    private String username;

    @Value("${imap.connection.password}")
    private String password;

    @Value("${imap.connection.serverName}")
    private String serverName;

    @Value("${imap.connection.serverPort}")
    private String serverPort;

    @Value("${imap.folder}")
    private String imapFolder;

    @Value("${imap.useSSL}")
    private String useSSL;

    @Value("${imap.connection.ssl.serverPort}")
    private String sslServerPort;


    @Bean(name = "receiveChannel")
    public MessageChannel receiveChannel() {
        DirectChannel inputChannel = new DirectChannel();
        inputChannel.subscribe(this.handleMessage());
        return inputChannel;
    }

    @Bean
    public MessageHandler handleMessage(){
        return new MessageHandler() {
            public void handleMessage(Message<?> message) throws MessagingException {
                try {
                    processMessage((GenericMessage)message);
                } catch (Exception e) {
                    e.printStackTrace();
                    //then do something usefull
                }
            }
        };
    }

    @Bean
    public ImapMailReceiver imapReceiver(){
        //TODO this will have to be created enternally
        ImapMailReceiver imapReceiver = new ImapMailReceiver("imap://"+username+":"+password+"@"+serverName+":"+serverPort+"/"+imapFolder);
        imapReceiver.setJavaMailProperties(this.javaMailProperties());
        imapReceiver.setShouldDeleteMessages(false);
        imapReceiver.setShouldMarkMessagesAsRead(true);
        imapReceiver.setJavaMailAuthenticator(this.javaMailAuthenticator());
        imapReceiver.setSearchTermStrategy(new AcceptAllSearchTermStrategy());//this will have to be commented out in production ...or the messagaes should be deleted
        return imapReceiver;
    }

    @Bean
    public ImapIdleChannelAdapter imapAdapter(){
        ImapIdleChannelAdapter imapAdapter = new ImapIdleChannelAdapter(this.imapReceiver());
        imapAdapter.setOutputChannel(this.receiveChannel());
        return imapAdapter;
    }

    @Bean
    public Properties javaMailProperties(){
        Properties javaMailProperties = new Properties();
        if (useSSL.equalsIgnoreCase("true")){
            javaMailProperties.setProperty("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            javaMailProperties.setProperty("mail.imap.socketFactory.fallback", "false");
            javaMailProperties.setProperty("mail.store.protocol", "imaps");
            javaMailProperties.setProperty("mail.debug", "false");//change to true in order to debug IMAP communication
            javaMailProperties.setProperty("mail.imap.ssl.socketFactory.port", sslServerPort);
        }
        return javaMailProperties;
    }

    @Bean
    public Authenticator javaMailAuthenticator(){
        return new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        };
    }

    public void processMessage(GenericMessage msg) throws Exception {
        logger.debug("Message ID="+msg.getHeaders().get("id"));
        logger.debug("fetchDate"+msg.getHeaders().get("timestamp"));

        MimeMessage mimeMsg = (MimeMessage)msg.getPayload();
        logger.debug("receivedDate="+mimeMsg.getReceivedDate().toString());
        logger.debug("sentDate="+mimeMsg.getSentDate().toString());
        logger.debug("from="+((InternetAddress)mimeMsg.getFrom()[0]).getAddress());
        logger.debug("subject="+mimeMsg.getSubject());
        String asciiContent = new String();
        String htmlContent = new String();
        MimeMultipart msgContent = (MimeMultipart) mimeMsg.getContent();
                     /* Check if content is pure text/html or in parts */
        if (msgContent instanceof Multipart) {
            Multipart multipart = (Multipart) msgContent;
            for (int j = 0; j < multipart.getCount(); j++) {
                BodyPart bodyPart = multipart.getBodyPart(j);
                String disposition = bodyPart.getDisposition();
                if (disposition != null && (disposition.equalsIgnoreCase("ATTACHMENT"))) {
                    DataHandler handler = bodyPart.getDataHandler();
                    logger.debug("file name : " + handler.getName());
                    logger.debug("file content : " + handler.getContent());
                }
                else {
                    if (bodyPart.getContentType().startsWith("text/html"))
                        htmlContent = bodyPart.getContent().toString();  // the changed code
                    if (bodyPart.getContentType().startsWith("text/plain"))
                        asciiContent = bodyPart.getContent().toString();  // the changed code
                }
            }
        }
        else
            htmlContent= msgContent.toString();
        logger.debug("asciiContent="+asciiContent);
        logger.debug("htmlContent="+htmlContent);
        //TODO persist the received meesage into the database
    }

}

