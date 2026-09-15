package exam.db.dto.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import exam.db.enums.RoleEnum;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class ExamUser implements Serializable {
	private static final long serialVersionUID = 0L;

	private String userId;
	private String username;
	private Set<String> roles;
	private String firstName;
	private String lastName;
	private String displayName;
	private String email;
	private String phone;
	private String address;

	@JsonFormat(pattern = "dd/MM/yyyy")
	private LocalDate dob;

	private String gender;

	@JsonIgnore
	public boolean isUser() {
		return roles != null && roles.contains(RoleEnum.ROLE_USER.getName());
	}

	@JsonIgnore
	public boolean isAdmin() {
		return roles != null && roles.contains(RoleEnum.ROLE_ADMIN.getName());
	}

	@JsonIgnore
	public boolean isTeachingAssistant() {
		return roles != null && roles.contains(RoleEnum.ROLE_TEACHING_ASSISTANT.getName());
	}
}
