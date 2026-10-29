package exam.service.user;

import exam.db.dto.user.BlockUserRequest;
import exam.db.dto.user.ChangePasswordRequest;
import exam.db.dto.user.CreateUserRequest;
import exam.db.dto.user.ForgotPasswordRequest;
import exam.db.dto.user.ResetPasswordRequest;
import exam.db.dto.user.UnblockUserRequest;
import exam.db.dto.user.UpdateUserRequest;
import exam.db.dto.user.UserResponse;
import exam.db.entity.Role;
import exam.db.entity.User;
import exam.db.entity.UserRecovery;
import exam.db.enums.ErrorInfo;
import exam.db.enums.RoleEnum;
import exam.db.repository.role.RoleRepository;
import exam.db.repository.user.UserRecoveryRepository;
import exam.db.repository.user.UserRepository;
import exam.service.ExamUserDetailsService;
import exam.service.notification.NotificationService;
import exam.support.TestUsers;
import exam.ultis.ExamBaseException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

	@Mock
	private UserRepository userRepo;

	@Mock
	private RoleRepository roleRepo;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private ExamUserDetailsService userDetailSv;

	@Mock
	private UserRecoveryRepository userRecoveryRepo;

	@Mock
	private NotificationService notificationSv;

	@InjectMocks
	private UserServiceImpl userService;

	@BeforeEach
	void setUp() {
		TestUsers.clear();
	}

	@AfterEach
	void tearDown() {
		TestUsers.clear();
	}

	@Test
	void createUserNormalizesEmailAndEncodesPassword() throws ExamBaseException {
		when(roleRepo.findByName(RoleEnum.ROLE_USER.getName())).thenReturn(new Role(RoleEnum.ROLE_USER.getName()));
		when(passwordEncoder.encode("User@123")).thenReturn("hashed");
		when(userDetailSv.userExists("User@Toeic.local")).thenReturn(false);

		CreateUserRequest request = new CreateUserRequest();
		request.setEmail("  User@Toeic.local  ");
		request.setPassword("User@123");
		request.setFirstName("An");
		request.setLastName("Nguyen");
		request.setRole(RoleEnum.ROLE_USER.getName());

		UserResponse response = userService.createUser(request);

		ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
		verify(userDetailSv).createUser(saved.capture());
		User user = saved.getValue();
		assertThat(user.getEmail()).isEqualTo("user@toeic.local");
		assertThat(user.getUsername()).isEqualTo("user@toeic.local");
		assertThat(user.getPassword()).isEqualTo("hashed");
		assertThat(user.getDisplayName()).isEqualTo("An Nguyen");
		assertThat(user.getOtp()).isNull();
		assertThat(user.isEnabled()).isTrue();
		assertThat(response.getEmail()).isEqualTo("user@toeic.local");
		assertThat(response.getOtp()).isNull();
		assertThat(response.getRoles()).containsExactly(RoleEnum.ROLE_USER.getName());
		verify(notificationSv, never()).sendMailResetPwd(anyString(), anyString());
	}

	@Test
	void updateUserInfoSavesDisplayNamePhoneAndDob() throws ExamBaseException {
		User current = TestUsers.user("user-1", RoleEnum.ROLE_USER);
		TestUsers.authenticate(current);
		when(userRepo.findByUserIdAndDeletedIsFalse("user-1")).thenReturn(current);

		UpdateUserRequest request = new UpdateUserRequest();
		request.setDisplayName("Ngo Trung Hieu");
		request.setFirstName("Trung Hieu");
		request.setLastName("Ngo");
		request.setPhone("0901234567");
		request.setDob(java.time.LocalDate.of(2005, 12, 22));

		UserResponse response = userService.updateUserInfo(request);

		assertThat(current.getDisplayName()).isEqualTo("Ngo Trung Hieu");
		assertThat(current.getPhone()).isEqualTo("0901234567");
		assertThat(current.getDob()).isEqualTo(java.time.LocalDate.of(2005, 12, 22));
		assertThat(response.getDisplayName()).isEqualTo("Ngo Trung Hieu");
		verify(userRepo).save(current);
	}

	@Test
	void updateUserInfoClearsLastNameWhenDisplayNameIsASingleWord() throws ExamBaseException {
		User current = TestUsers.user("user-1", RoleEnum.ROLE_USER);
		current.setFirstName("Trung Hieu");
		current.setLastName("Ngo");
		current.setDisplayName("Ngo Trung Hieu");
		TestUsers.authenticate(current);
		when(userRepo.findByUserIdAndDeletedIsFalse("user-1")).thenReturn(current);

		UpdateUserRequest request = new UpdateUserRequest();
		request.setDisplayName("Hieu");
		request.setFirstName("Hieu");
		request.setLastName("");

		userService.updateUserInfo(request);

		assertThat(current.getDisplayName()).isEqualTo("Hieu");
		assertThat(current.getFirstName()).isEqualTo("Hieu");
		assertThat(current.getLastName()).isEmpty();
	}

	@Test
	void createUserRejectsUnknownRole() {
		when(roleRepo.findByName("NO_ROLE")).thenReturn(null);
		CreateUserRequest request = new CreateUserRequest();
		request.setRole("NO_ROLE");

		assertThatThrownBy(() -> userService.createUser(request))
				.isInstanceOf(ExamBaseException.class)
				.extracting(ex -> ((ExamBaseException) ex).getError().getCode())
				.isEqualTo(ErrorInfo.BAD_REQUEST_ERROR_CODE);
	}

	@Test
	void createUserRejectsDuplicateEmail() throws ExamBaseException {
		when(roleRepo.findByName(RoleEnum.ROLE_USER.getName())).thenReturn(new Role(RoleEnum.ROLE_USER.getName()));
		when(passwordEncoder.encode(anyString())).thenReturn("hashed");
		when(userDetailSv.userExists("user@toeic.local")).thenReturn(true);

		CreateUserRequest request = new CreateUserRequest();
		request.setEmail("user@toeic.local");
		request.setPassword("User@123");
		request.setDisplayName("An");
		request.setRole(RoleEnum.ROLE_USER.getName());

		assertThatThrownBy(() -> userService.createUser(request))
				.isInstanceOf(ExamBaseException.class)
				.extracting(ex -> ((ExamBaseException) ex).getError().getCode())
				.isEqualTo(ErrorInfo.EMAIL_ALREADY_EXISTS_ERROR_CODE);
		verify(userDetailSv, never()).createUser(any());
	}

	@Test
	void changePasswordRejectsWrongOldPassword() {
		User user = TestUsers.user("user-1", RoleEnum.ROLE_USER);
		user.setPassword("hashed-old");
		TestUsers.authenticate(user);
		when(userRepo.findByUserIdAndDeletedIsFalse("user-1")).thenReturn(user);
		when(passwordEncoder.matches("wrong", "hashed-old")).thenReturn(false);

		assertThatThrownBy(() -> userService.changePassword(changePassword("wrong", "New@1234", "New@1234")))
				.isInstanceOf(ExamBaseException.class)
				.extracting(ex -> ((ExamBaseException) ex).getError().getCode())
				.isEqualTo(ErrorInfo.INCORRECT_PASSWORD_ERROR_CODE);
		verify(userRepo, never()).save(any());
	}

	@Test
	void changePasswordRejectsMismatchedConfirmation() {
		User user = TestUsers.user("user-1", RoleEnum.ROLE_USER);
		user.setPassword("hashed-old");
		TestUsers.authenticate(user);
		when(userRepo.findByUserIdAndDeletedIsFalse("user-1")).thenReturn(user);
		when(passwordEncoder.matches("Old@1234", "hashed-old")).thenReturn(true);

		assertThatThrownBy(() -> userService.changePassword(changePassword("Old@1234", "New@1234", "Other@123")))
				.isInstanceOf(ExamBaseException.class)
				.extracting(ex -> ((ExamBaseException) ex).getError().getCode())
				.isEqualTo(ErrorInfo.INCORRECT_CONFIRM_PASSWORD_ERROR_CODE);
	}

	@Test
	void changePasswordRejectsValueShorterThanEightAfterTrim() {
		User user = TestUsers.user("user-1", RoleEnum.ROLE_USER);
		user.setPassword("hashed-old");
		TestUsers.authenticate(user);
		when(userRepo.findByUserIdAndDeletedIsFalse("user-1")).thenReturn(user);
		when(passwordEncoder.matches("Old@1234", "hashed-old")).thenReturn(true);

		assertThatThrownBy(() -> userService.changePassword(changePassword("Old@1234", "  abcdef1  ", "  abcdef1  ")))
				.isInstanceOf(ExamBaseException.class)
				.extracting(ex -> ((ExamBaseException) ex).getError().getCode())
				.isEqualTo(ErrorInfo.PASSWORD_TOO_SHORT_CODE);
		verify(userRepo, never()).save(user);
	}

	@Test
	void changePasswordSavesEncodedPassword() throws ExamBaseException {
		User user = TestUsers.user("user-1", RoleEnum.ROLE_USER);
		user.setPassword("hashed-old");
		TestUsers.authenticate(user);
		when(userRepo.findByUserIdAndDeletedIsFalse("user-1")).thenReturn(user);
		when(passwordEncoder.matches("Old@1234", "hashed-old")).thenReturn(true);
		when(passwordEncoder.encode("New@1234")).thenReturn("hashed-new");

		Boolean changed = userService.changePassword(changePassword("  Old@1234  ", "  New@1234  ", " New@1234 "));

		assertThat(changed).isTrue();
		assertThat(user.getPassword()).isEqualTo("hashed-new");
		verify(userRepo).save(user);
	}

	@Test
	void resetPasswordMarksRecoveryUsed() throws ExamBaseException {
		User user = TestUsers.user("user-1", RoleEnum.ROLE_USER);
		user.setOtp("123456");
		UserRecovery recovery = new UserRecovery();
		recovery.setUserId("user-1");
		recovery.setResetCode("abc12345");
		when(userRecoveryRepo.findByResetCodeAndDeletedIsFalse("abc12345")).thenReturn(recovery);
		when(userRepo.findByUserIdAndDeletedIsFalse("user-1")).thenReturn(user);
		when(passwordEncoder.encode("New@1234")).thenReturn("hashed-new");

		ResetPasswordRequest request = new ResetPasswordRequest();
		request.setResetCode("  abc12345  ");
		request.setNewPassword("New@1234");
		request.setConfirmPassword("New@1234");

		assertThat(userService.resetPassword(request)).isTrue();
		assertThat(user.getPassword()).isEqualTo("hashed-new");
		assertThat(user.getOtp()).isNull();
		assertThat(recovery.isDeleted()).isTrue();
		verify(userRepo).save(user);
		verify(userRecoveryRepo).save(recovery);
	}

	@Test
	void resetPasswordRejectsUnknownCode() {
		when(userRecoveryRepo.findByResetCodeAndDeletedIsFalse("missing")).thenReturn(null);
		ResetPasswordRequest request = new ResetPasswordRequest();
		request.setResetCode("missing");

		assertThatThrownBy(() -> userService.resetPassword(request))
				.isInstanceOf(ExamBaseException.class)
				.extracting(ex -> ((ExamBaseException) ex).getError().getCode())
				.isEqualTo(ErrorInfo.RESET_PASSWORD_ERROR_CODE);
	}

	@Test
	void forgotPasswordStoresResetCode() throws ExamBaseException {
		User user = TestUsers.user("user-1", RoleEnum.ROLE_USER);
		when(userRepo.findByEmailIgnoreCase("user-1@toeic.local")).thenReturn(user);

		ForgotPasswordRequest request = new ForgotPasswordRequest();
		request.setEmail("  user-1@toeic.local  ");

		assertThat(userService.forgotPassword(request)).isTrue();
		ArgumentCaptor<UserRecovery> recovery = ArgumentCaptor.forClass(UserRecovery.class);
		verify(userRecoveryRepo).save(recovery.capture());
		assertThat(recovery.getValue().getResetCode()).hasSize(8);
		assertThat(recovery.getValue().getUserId()).isEqualTo("user-1");
		verify(notificationSv).sendMailResetPwd("user-1@toeic.local", recovery.getValue().getResetCode());
	}

	@Test
	void forgotPasswordRetiresOlderCodes() throws ExamBaseException {
		User user = TestUsers.user("user-1", RoleEnum.ROLE_USER);
		when(userRepo.findByEmailIgnoreCase("user-1@toeic.local")).thenReturn(user);
		UserRecovery older = new UserRecovery();
		older.setUserId("user-1");
		older.setResetCode("old-code");
		when(userRecoveryRepo.findAllByUserIdAndDeletedIsFalse("user-1")).thenReturn(new ArrayList<>(List.of(older)));

		ForgotPasswordRequest request = new ForgotPasswordRequest();
		request.setEmail("user-1@toeic.local");

		assertThat(userService.forgotPassword(request)).isTrue();
		assertThat(older.isDeleted()).isTrue();
		verify(userRecoveryRepo).saveAll(List.of(older));
	}

	@Test
	void blockUserRequiresAdminAndLocksAccount() throws ExamBaseException {
		TestUsers.authenticate(TestUsers.user("admin-1", RoleEnum.ROLE_ADMIN));
		User target = TestUsers.user("user-1", RoleEnum.ROLE_USER);
		when(userRepo.findByUserIdIn(List.of("user-1"))).thenReturn(List.of(target));
		BlockUserRequest request = new BlockUserRequest();
		request.setIds(List.of("user-1"));

		assertThat(userService.blockUser(request)).isTrue();
		assertThat(target.isAccountNonLocked()).isFalse();
		verify(userRepo).saveAll(List.of(target));
	}

	@Test
	void blockUserRejectsCurrentAdmin() {
		TestUsers.authenticate(TestUsers.user("admin-1", RoleEnum.ROLE_ADMIN));
		BlockUserRequest request = new BlockUserRequest();
		request.setIds(List.of("admin-1"));

		assertThatThrownBy(() -> userService.blockUser(request))
				.isInstanceOf(ExamBaseException.class)
				.extracting(ex -> ((ExamBaseException) ex).getError().getCode())
				.isEqualTo(ErrorInfo.CANNOT_BLOCK_SELF_CODE);
	}

	@Test
	void blockUserRejectsLearner() {
		TestUsers.authenticate(TestUsers.user("user-1", RoleEnum.ROLE_USER));
		BlockUserRequest request = new BlockUserRequest();
		request.setIds(List.of("user-2"));

		assertThatThrownBy(() -> userService.blockUser(request))
				.isInstanceOf(ExamBaseException.class)
				.extracting(ex -> ((ExamBaseException) ex).getError().getCode())
				.isEqualTo(ErrorInfo.ACCESS_DENIED_ERROR_CODE);
	}

	@Test
	void unblockUserUnlocksAccount() throws ExamBaseException {
		TestUsers.authenticate(TestUsers.user("admin-1", RoleEnum.ROLE_ADMIN));
		User target = TestUsers.user("user-1", RoleEnum.ROLE_USER);
		target.setAccountNonLocked(false);
		when(userRepo.findByUserIdIn(List.of("user-1"))).thenReturn(List.of(target));
		UnblockUserRequest request = new UnblockUserRequest();
		request.setIds(List.of("user-1"));

		assertThat(userService.unblockUser(request)).isTrue();
		assertThat(target.isAccountNonLocked()).isTrue();
	}

	private ChangePasswordRequest changePassword(String oldPassword, String newPassword, String confirmPassword) {
		ChangePasswordRequest request = new ChangePasswordRequest();
		request.setOldPassword(oldPassword);
		request.setNewPassword(newPassword);
		request.setConfirmPassword(confirmPassword);
		return request;
	}
}
