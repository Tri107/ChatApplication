/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package chatapp;
import javax.activation.DataHandler;
import javax.activation.DataSource;
/**
 *
 * @author DELL
 */

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

public class EmailSender {
    public static void sendEmail(String to, String subject, String body) {
        String from = "nguyencongvinh2909@gmail.com";  // Thay bằng email của bạn
        String password = "dfbs xtdp brco gagd";  // Thay bằng mật khẩu ứng dụng Gmail của bạn

        // Cấu hình thuộc tính
        Properties properties = new Properties();
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");

        // Tạo Session và thiết lập xác thực
        Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from, password);
            }
        });

        try {
            // Tạo email
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(body);

            // Gửi email
            Transport.send(message);
            System.out.println("OTP đã được gửi đến: " + to);
        } catch (MessagingException e) {
            e.printStackTrace();
            System.out.println("Không thể gửi email.");
        }
    }
}

