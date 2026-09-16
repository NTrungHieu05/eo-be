package exam.db.dto.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UpdateUserRequest extends CreateUserRequest {
	private String userId;
	@JsonFormat(pattern = "dd/MM/yyyy")
	private Date dobDate;
}
