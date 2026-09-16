package exam.service.user;

import exam.db.dto.user.ChangePasswordRequest;
import exam.db.dto.user.CreateUserRequest;
import exam.db.dto.user.ExamUser;
import exam.db.dto.user.ForgotPasswordRequest;
import exam.db.dto.user.ResetPasswordRequest;
import exam.db.dto.user.UpdateUserRequest;
import exam.db.dto.user.UserResponse;
import exam.db.entity.Role;
import exam.db.entity.User;
import exam.db.entity.UserRecovery;
import exam.db.enums.Assert;
import exam.db.enums.DBConst;
import exam.db.enums.ErrorInfo;
import exam.db.repository.role.RoleRepository;
import exam.db.repository.user.UserRecoveryRepository;
import exam.db.repository.user.UserRepository;
import exam.service.ExamUserDetailsService;
import exam.service.notification.NotificationService;
import exam.ultis.ExamBaseException;
import exam.ultis.SecurityContextService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private UserRepository userRepo;

	@Autowired
	private RoleRepository roleRepo;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private ExamUserDetailsService userDetailSv;

	@Autowired
	private UserRecoveryRepository userRecoveryRepo;

	@Autowired
	private NotificationService notificationSv;

	@Override
	public UserResponse createUser(CreateUserRequest request) throws ExamBaseException {
		Role role = roleRepo.findByName(request.getRole());
		Assert.notNull(role, ErrorInfo.BAD_REQUEST);
		User user = createUser(request, role);
		return buildUserResponse(user);
	}

	@Override
	public User createUser(CreateUserRequest request, Role role) throws ExamBaseException {
		Assert.notNull(role, ErrorInfo.BAD_REQUEST);

		String displayName = request.getDisplayName();
		if (!StringUtils.hasText(displayName)) {
			displayName = String.format("%s %s",
							Optional.ofNullable(request.getFirstName()).orElse(""),
							Optional.ofNullable(request.getLastName()).orElse(""))
					.trim();
			request.setDisplayName(displayName);
		}

		User user = new User();
		BeanUtils.copyProperties(request, user);
		String email = request.getEmail().trim().toLowerCase();
		user.setPassword(passwordEncoder.encode(request.getPassword().trim()));
		user.setEmail(email);
		user.setUsername(email);
		user.getRoles().add(role);
		user.setAccountNonExpired(true);
		user.setAccountNonLocked(true);
		user.setCredentialsNonExpired(true);
		user.setEnabled(true);
		checkExists(user, request.getEmail());
		user.setOtp(buildAndSendOTP(request.getEmail()));
		userDetailSv.createUser(user);
		return user;
	}

	private void checkExists(User user, String email) throws ExamBaseException {
		userDetailSv.validateUserDetails(user);
		if (userDetailSv.userExists(email.trim())) {
			throw new ExamBaseException(ErrorInfo.EMAIL_ALREADY_EXISTS_ERROR);
		}
	}

	@Override
	public Boolean changePassword(ChangePasswordRequest request) throws ExamBaseException {
		ExamUser examUser = SecurityContextService.getUser();
		Assert.notNull(examUser, ErrorInfo.ACCESS_DENIED_ERROR);
		User user = userRepo.findByUserId(examUser.getUserId());
		Assert.notNull(user, ErrorInfo.USER_NOT_FOUND_ERROR);

		if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
			throw new ExamBaseException(ErrorInfo.INCORRECT_PASSWORD_ERROR);
		}
		if (!request.getNewPassword().equals(request.getConfirmPassword())) {
			throw new ExamBaseException(ErrorInfo.INCORRECT_CONFIRM_PASSWORD_ERROR);
		}
		user.setPassword(passwordEncoder.encode(request.getNewPassword()));
		userRepo.save(user);
		return Boolean.TRUE;
	}

	@Override
	public UserResponse updateUserInfo(UpdateUserRequest request) throws ExamBaseException {
		Assert.notNull(request.getUserId(), ErrorInfo.USER_NOT_FOUND_ERROR);
		User user = userRepo.findByUserId(request.getUserId());
		Assert.notNull(user, ErrorInfo.USER_NOT_FOUND_ERROR);
		if (StringUtils.hasText(request.getGender())) {
			user.setGender(request.getGender());
		}
		if (StringUtils.hasText(request.getFirstName())) {
			user.setFirstName(request.getFirstName());
		}
		if (StringUtils.hasText(request.getLastName())) {
			user.setLastName(request.getLastName());
		}
		if (StringUtils.hasText(request.getDisplayName())) {
			user.setDisplayName(request.getDisplayName());
		} else {
			user.setDisplayName(String.format("%s %s",
							Optional.ofNullable(request.getFirstName()).orElse(""),
							Optional.ofNullable(request.getLastName()).orElse(""))
					.trim());
		}
		if (StringUtils.hasText(request.getAddress())) {
			user.setAddress(request.getAddress());
		}
		if (StringUtils.hasText(request.getPhone())) {
			user.setPhone(request.getPhone());
		}
		if (request.getDob() != null) {
			user.setDob(request.getDob());
		}
		userRepo.save(user);
		return buildUserResponse(user);
	}

	@Override
	public UserResponse getSimpleUsersInfo(String userId) throws ExamBaseException {
		User user = userRepo.findByUserIdAndDeletedIsFalse(userId);
		Assert.notNull(user, ErrorInfo.USER_NOT_FOUND_ERROR);
		return buildUserResponse(user);
	}

	@Override
	public Boolean forgotPassword(ForgotPasswordRequest request) throws ExamBaseException {
		User user = userRepo.findByEmailIgnoreCase(request.getEmail());
		Assert.notNull(user, ErrorInfo.USER_NOT_FOUND_ERROR);
		String resetCode = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
		UserRecovery recovery = new UserRecovery();
		recovery.setUserId(user.getUserId());
		recovery.setEmail(user.getEmail());
		recovery.setResetCode(resetCode);
		userRecoveryRepo.save(recovery);
		notificationSv.sendMailResetPwd(user.getEmail(), resetCode);
		return Boolean.TRUE;
	}

	@Override
	public Boolean resetPassword(ResetPasswordRequest request) throws ExamBaseException {
		UserRecovery userRecovery = userRecoveryRepo.findByResetCodeAndDeletedIsFalse(request.getResetCode());
		Assert.notNull(userRecovery, ErrorInfo.RESET_PASSWORD_CODE_ERROR);
		User user = userRepo.findByUserId(userRecovery.getUserId());
		Assert.notNull(user, ErrorInfo.USER_NOT_FOUND_ERROR);
		if (!request.getNewPassword().equals(request.getConfirmPassword())) {
			throw new ExamBaseException(ErrorInfo.INCORRECT_CONFIRM_PASSWORD_ERROR);
		}
		user.setPassword(passwordEncoder.encode(request.getNewPassword()));
		userRepo.save(user);
		userRecovery.setDeleted(true);
		userRecoveryRepo.save(userRecovery);
		return true;
	}

	private String buildAndSendOTP(String email) {
		String otp = notificationSv.generateOTP();
		notificationSv.sendOTPEmail(email, otp);
		return otp;
	}

	private UserResponse buildUserResponse(User user) {
		UserResponse userResponse = new UserResponse();
		BeanUtils.copyProperties(user, userResponse, DBConst.ROLES);
		userResponse.setRoles(user.getRoleStrings());
		return userResponse;
	}
}
