package com.cotiviti.loginportal.service;

import com.sun.mail.smtp.SMTPTransport;
import org.springframework.stereotype.Service;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Date;
import java.util.Properties;

import static com.cotiviti.loginportal.constansts.EmailConstant.*;
import static javax.mail.Message.RecipientType.CC;
import static javax.mail.Message.RecipientType.TO;

@Service
public class EmailService {
    public void sendNewPasswordEmail(String firstName,String password,String email) throws MessagingException {
        Message message = createMail(firstName,password,email);
        SMTPTransport smtpTransport = (SMTPTransport) getEmailSession().getTransport(SIMPLE_EMAIL_TRANSFER_PROTOCOL);
        smtpTransport.connect(GMAIL_SMTP_SERVER,USERNAME,PASSWORD);
        smtpTransport.sendMessage(message,message.getAllRecipients());
        smtpTransport.close();
    }

    private Message createMail(String firstName, String password, String email) throws MessagingException {
        Message message = new MimeMessage(getEmailSession());
        message.setFrom(new InternetAddress(FROM_EMAIL));
        message.setRecipients(TO, InternetAddress.parse(email,false));
        message.setRecipients(CC,InternetAddress.parse(CC_EMAIL,false));
        message.setSubject(EMAIL_SUBJECT);
        message.setText("Hello " +firstName + ", \n \n Your new account password is: " +password +" \n \n The Support Team");
        message.setSentDate(new Date());
        message.saveChanges();
        return message;
    }

    private Session getEmailSession(){
        Properties properties = System.getProperties();
        properties.put(SMTP_HOST,GMAIL_SMTP_SERVER);
        properties.put(SMTP_AUTH,true);
        properties.put(SMTP_PORT,DEFAULT_PORT);
        properties.put(SMTP_STARTTLE_ENABLE,true);
        properties.put(SMTP_STARTTLE_REQUIRED,true);
        return Session.getInstance(properties,null);
    }
}
