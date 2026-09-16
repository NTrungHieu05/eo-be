package exam.db.dto.user;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
public class ForgotPasswordRequest {
	@Email
	@NotBlank
	private String email;
}
