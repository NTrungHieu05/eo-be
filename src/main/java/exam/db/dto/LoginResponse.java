package exam.db.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import exam.db.dto.user.ExamUser;
import exam.db.entity.User;
import lombok.Data;
import org.springframework.beans.BeanUtils;

@Data
public class LoginResponse {
	private String access_token;
	private ExamUser examUser = new ExamUser();

	public LoginResponse(String access_token, User user) {
		this.access_token = access_token;
		BeanUtils.copyProperties(user, this.examUser);
		this.examUser.setRoles(user.getRoleStrings());
	}

	@JsonProperty("token")
	public String getToken() {
		return access_token;
	}

	@JsonProperty("englishUser")
	public ExamUser getEnglishUser() {
		return examUser;
	}
}
