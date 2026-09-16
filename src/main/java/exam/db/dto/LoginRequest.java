package exam.db.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
public class LoginRequest {
	@Email
	@NotBlank
	private String email;
	@NotBlank
	private String password;
}
