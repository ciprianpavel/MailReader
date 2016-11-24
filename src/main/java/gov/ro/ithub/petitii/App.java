package gov.ro.ithub.petitii;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.mail.ImapMailReceiver;
import org.springframework.integration.mail.MailReceiver;
import org.springframework.integration.mail.Pop3MailReceiver;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.messaging.MessageHandler;

import javax.activation.DataHandler;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Multipart;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.Arrays;
import java.util.Properties;

/**
 * Hello world!
 *
 */
@SpringBootApplication
@IntegrationComponentScan
public class App
{
    private static Logger logger = Logger.getLogger(App.class);

    public static void main (String[] args) throws Exception {
        ApplicationContext ctx = SpringApplication.run(App.class, args);
	}
}
