package com.nhom7.quiz.quizapp.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {
    private final JavaMailSender mailSender;

    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendPasswordResetCode(String toEmail, String fullName, String code) {
        String subject = "Mã đặt lại mật khẩu - QuizMaster";
        String name = fullName != null ? fullName : "";
        String text = "Xin chào " + name + ",\n\n" +
                "Mã đặt lại mật khẩu của bạn là: " + code + "\n" +
                "Mã có hiệu lực trong 15 phút. Không chia sẻ mã này cho bất kỳ ai.\n\n" +
                "Nếu không phải bạn yêu cầu, vui lòng bỏ qua email này.";

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(toEmail);
        msg.setSubject(subject);
        msg.setText(text);
        mailSender.send(msg);
    }
}


