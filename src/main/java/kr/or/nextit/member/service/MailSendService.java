package kr.or.nextit.member.service;

import java.util.Random;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

@Service
public class MailSendService {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private JavaMailSenderImpl mailSender;
	
	
	public String sendAuthMail(String mail) {
		String authKey = getkey(6);
		
		logger.info("authKey : " + authKey);
		
		MimeMessage mailMessage = mailSender.createMimeMessage();
		
		String mailContent = "인증 번호 : " + authKey; //보낼 메세지
		
		try {
			mailMessage.setSubject("NextIT 가입 확인 이메일 인증번호", "utf-8");
			mailMessage.setText(mailContent, "utf-8", "html");
			mailMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(mail));
		
			mailSender.send(mailMessage);
			
		} catch (MessagingException e) {
			e.printStackTrace();
			return "false";
		}
		
		return authKey;
	}


	private String getkey(int size) {
		// TODO Auto-generated method stub
		Random random = new Random();
		
		String key = "";
		for(int i =0; i< size; i++) {
			int ran = random.nextInt(10);  //0~9
			key += ran;
		}
		return key;
	}


}
