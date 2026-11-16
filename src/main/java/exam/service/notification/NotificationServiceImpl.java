package exam.service.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.mail.internet.MimeMessage;

@Service
public class NotificationServiceImpl implements NotificationService {
	private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

	@Autowired(required = false)
	private JavaMailSender mailSender;

	@Value("${spring.mail.username:}")
	private String mailUsername = "";

	@Value("${spring.mail.password:}")
	private String mailPassword = "";

	@Value("${app.mail.from:}")
	private String mailFrom = "";

	@Override
	public void sendMailResetPwd(String email, String pwd) {
		log.info("Reset code for {} is {}", email, pwd);
		sendMail(
				email,
				"Dat lai mat khau luyen thi TOEIC",
				"<p>Ma dat lai mat khau cua ban la:</p>"
						+ "<p style=\"font-size:24px;font-weight:bold;letter-spacing:4px\">" + pwd + "</p>");
	}

	private boolean mailConfigured() {
		return mailSender != null
				&& StringUtils.hasText(mailUsername)
				&& StringUtils.hasText(mailPassword);
	}

	private void sendMail(String to, String subject, String html) {
		if (!mailConfigured()) {
			log.warn("Chua cau hinh Gmail (spring.mail.username / spring.mail.password). Chi in ma ra log.");
			return;
		}
		if (!StringUtils.hasText(to) || !to.contains("@") || to.endsWith(".local")) {
			log.warn("Bo qua gui mail toi dia chi khong hop le: {}", to);
			return;
		}
		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
			String from = StringUtils.hasText(mailFrom) ? mailFrom : mailUsername;
			helper.setFrom(from);
			helper.setTo(to.trim());
			helper.setSubject(subject);
			helper.setText(html, true);
			mailSender.send(message);
			log.info("Da gui email {} toi {}", subject, to);
		} catch (Exception e) {
			log.error("Gui email that bai toi {}: {}", to, e.getMessage());
		}
	}
}
