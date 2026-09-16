package exam.db.dto.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class CreateUserRequest {
	@Email
	@NotBlank
	private String email;
	@NotBlank
	@Size(min = 8, message = "must have at least 8 characters")
	private String password;
	private String username;
	private String firstName;
	private String lastName;
	private String displayName;
	private String address;
	private String phone;
	private String role;
	@JsonFormat(pattern = "dd/MM/yyyy")
	private LocalDate dob;
	private String gender;
	private String otp;
}
