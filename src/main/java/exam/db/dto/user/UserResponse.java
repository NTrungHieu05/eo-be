package exam.db.dto.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import exam.db.entity.User;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserResponse {
	private String userId;
	private String displayName;
	private String firstName;
	private String lastName;
	private Set<String> roles;
	private String email;
	private String address;
	private String phone;
	@JsonFormat(pattern = "dd/MM/yyyy")
	private LocalDate dob;
	private String gender;
	private Boolean accountNonLocked;

	public UserResponse(User user) {
		this.userId = user.getUserId();
		this.firstName = user.getFirstName();
		this.lastName = user.getLastName();
		this.displayName = user.getDisplayName();
		this.roles = user.getRoleStrings();
		this.email = user.getEmail();
		this.phone = user.getPhone();
		this.dob = user.getDob();
		this.gender = user.getGender();
		this.address = user.getAddress();
		this.accountNonLocked = user.isAccountNonLocked();
	}
}
