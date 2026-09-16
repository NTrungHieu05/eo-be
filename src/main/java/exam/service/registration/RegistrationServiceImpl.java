package exam.service.registration;

import exam.db.dto.user.CreateUserRequest;
import exam.db.dto.user.UserResponse;
import exam.db.enums.RoleEnum;
import exam.service.user.UserService;
import exam.ultis.ExamBaseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RegistrationServiceImpl implements RegistrationService {

	@Autowired
	private UserService userSv;

	@Override
	public UserResponse registerUser(CreateUserRequest request) throws ExamBaseException {
		request.setRole(RoleEnum.ROLE_USER.getName());
		return userSv.createUser(request);
	}
}
