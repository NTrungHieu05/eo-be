package exam.service.registration;

import exam.db.dto.user.CreateUserRequest;
import exam.db.dto.user.UserResponse;
import exam.db.enums.RoleEnum;
import exam.service.user.UserService;
import exam.ultis.ExamBaseException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceImplTest {

	@Mock
	private UserService userSv;

	@InjectMocks
	private RegistrationServiceImpl registrationService;

	@Test
	void registerUserForcesLearnerRole() throws ExamBaseException {
		CreateUserRequest request = new CreateUserRequest();
		request.setEmail("new@toeic.local");
		request.setPassword("User@123");
		request.setRole(RoleEnum.ROLE_ADMIN.getName());
		UserResponse expected = new UserResponse();
		when(userSv.createUser(request)).thenReturn(expected);

		UserResponse response = registrationService.registerUser(request);

		assertThat(response).isSameAs(expected);
		assertThat(request.getRole()).isEqualTo(RoleEnum.ROLE_USER.getName());
		verify(userSv).createUser(request);
	}
}
