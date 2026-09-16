package exam.db.dto.user;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class ResetPasswordRequest {
	@NotBlank
	private String resetCode;
	@NotBlank
	@Size(min = 8)
	private String newPassword;
	@NotBlank
	private String confirmPassword;
}
