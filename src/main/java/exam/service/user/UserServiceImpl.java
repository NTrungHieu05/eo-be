package exam.service.user;

import exam.db.dto.user.BlockUserRequest;
import exam.db.dto.user.ChangePasswordRequest;
import exam.db.dto.user.CreateUserRequest;
import exam.db.dto.user.ExamUser;
import exam.db.dto.user.ListUserRequest;
import exam.db.dto.user.ListUserResponse;
import exam.db.dto.user.UnblockUserRequest;
import exam.db.dto.user.UpdateUserRequest;
import exam.db.dto.user.UserResponse;
import exam.db.entity.Role;
import exam.db.entity.User;
import exam.db.enums.Assert;
import exam.db.enums.RoleEnum;
import exam.db.enums.DBConst;
import exam.db.enums.ErrorInfo;
import exam.db.enums.FieldConst;
import exam.db.repository.role.RoleRepository;
import exam.db.repository.user.UserRepository;
import exam.service.ExamUserDetailsService;
import exam.ultis.ExamBaseException;
import exam.ultis.SecurityContextService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

	@Override
	public UserResponse createUser(CreateUserRequest request) throws ExamBaseException {
		Role role = roleRepo.findByName(request.getRole());
		Assert.notNull(role, ErrorInfo.BAD_REQUEST);
		User user = saveNewUser(request, role);
		return buildUserResponse(user);
	}

	@Override
	public User createUser(CreateUserRequest request, Role role) throws ExamBaseException {
		return saveNewUser(request, role);
	}

	@Override
	public UserResponse createUserForAdmin(CreateUserRequest request) throws ExamBaseException {
		requireAdmin();
		Role role = roleRepo.findByName(RoleEnum.ROLE_USER.getName());
		Assert.notNull(role, ErrorInfo.BAD_REQUEST);
		User user = saveNewUser(request, role);
		return buildUserResponse(user);
	}

	private User saveNewUser(CreateUserRequest request, Role role) throws ExamBaseException {
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
		String password = request.getPassword() == null ? "" : request.getPassword().trim();
		requirePasswordLength(password);
		user.setPassword(passwordEncoder.encode(password));
		user.setEmail(email);
		user.setUsername(email);
		user.getRoles().add(role);
		user.setAccountNonExpired(true);
		user.setAccountNonLocked(true);
		user.setCredentialsNonExpired(true);
		user.setEnabled(true);
		user.setOtp(null);
		checkExists(user, request.getEmail());
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
		User user = userRepo.findByUserIdAndDeletedIsFalse(examUser.getUserId());
		Assert.notNull(user, ErrorInfo.USER_NOT_FOUND_ERROR);

		String oldPassword = request.getOldPassword() == null ? "" : request.getOldPassword().trim();
		String newPassword = request.getNewPassword() == null ? "" : request.getNewPassword().trim();
		String confirmPassword = request.getConfirmPassword() == null ? "" : request.getConfirmPassword().trim();
		if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
			throw new ExamBaseException(ErrorInfo.INCORRECT_PASSWORD_ERROR);
		}
		if (!newPassword.equals(confirmPassword)) {
			throw new ExamBaseException(ErrorInfo.INCORRECT_CONFIRM_PASSWORD_ERROR);
		}
		requirePasswordLength(newPassword);
		user.setPassword(passwordEncoder.encode(newPassword));
		userRepo.save(user);
		return Boolean.TRUE;
	}

	@Override
	public UserResponse updateUserInfo(UpdateUserRequest request) throws ExamBaseException {
		ExamUser current = SecurityContextService.getUser();
		Assert.notNull(current, ErrorInfo.ACCESS_DENIED_ERROR);
		String userId = request.getUserId();
		if (!StringUtils.hasText(userId) || !current.isAdmin()) {
			userId = current.getUserId();
		}
		if (!StringUtils.hasText(userId)) {
			throw new ExamBaseException(ErrorInfo.BAD_REQUEST);
		}
		User user = userRepo.findByUserIdAndDeletedIsFalse(userId);
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
			user.setDisplayName(request.getDisplayName().trim());
			if (request.getFirstName() != null) {
				user.setFirstName(request.getFirstName().trim());
			}
			if (request.getLastName() != null) {
				user.setLastName(request.getLastName().trim());
			}
		} else if (StringUtils.hasText(request.getFirstName()) || StringUtils.hasText(request.getLastName())) {
			String builtName = String.format("%s %s",
							Optional.ofNullable(request.getFirstName()).orElse(""),
							Optional.ofNullable(request.getLastName()).orElse(""))
					.trim();
			if (StringUtils.hasText(builtName)) {
				user.setDisplayName(builtName);
			}
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
		ExamUser current = SecurityContextService.getUser();
		Assert.notNull(current, ErrorInfo.ACCESS_DENIED_ERROR);
		if (!StringUtils.hasText(userId) || (!current.isAdmin() && !userId.equals(current.getUserId()))) {
			throw new ExamBaseException(ErrorInfo.ACCESS_DENIED_ERROR);
		}
		User user = userRepo.findByUserIdAndDeletedIsFalse(userId);
		Assert.notNull(user, ErrorInfo.USER_NOT_FOUND_ERROR);
		return buildUserResponse(user);
	}

	@Override
	public ListUserResponse listUserForAdmin(ListUserRequest request) throws ExamBaseException {
		requireAdmin();
		List<User> users = userRepo.findAllByDeletedIsFalse();
		List<UserResponse> responses = users.stream().map(this::buildUserResponse).collect(Collectors.toList());
		return new ListUserResponse(responses, responses.size());
	}

	@Override
	public Boolean blockUser(BlockUserRequest request) throws ExamBaseException {
		requireAdmin();
		ExamUser current = SecurityContextService.getUser();
		if (request == null || request.getIds() == null) {
			throw new ExamBaseException(ErrorInfo.BAD_REQUEST);
		}
		if (request.getIds().contains(current.getUserId())) {
			throw new ExamBaseException(ErrorInfo.CANNOT_BLOCK_SELF);
		}
		List<User> users = userRepo.findByUserIdIn(new ArrayList<>(request.getIds()));
		if (users != null && !users.isEmpty()) {
			users.forEach(u -> u.setAccountNonLocked(false));
			userRepo.saveAll(users);
		}
		return Boolean.TRUE;
	}

	@Override
	public Boolean unblockUser(UnblockUserRequest request) throws ExamBaseException {
		requireAdmin();
		if (request == null || request.getIds() == null) {
			throw new ExamBaseException(ErrorInfo.BAD_REQUEST);
		}
		List<User> users = userRepo.findByUserIdIn(new ArrayList<>(request.getIds()));
		if (users != null && !users.isEmpty()) {
			users.forEach(u -> u.setAccountNonLocked(true));
			userRepo.saveAll(users);
		}
		return Boolean.TRUE;
	}

	private void requirePasswordLength(String password) throws ExamBaseException {
		if (password == null || password.length() < 8) {
			throw new ExamBaseException(ErrorInfo.PASSWORD_TOO_SHORT);
		}
	}

	private void requireAdmin() throws ExamBaseException {
		ExamUser examUser = SecurityContextService.getUser();
		Assert.notNull(examUser, ErrorInfo.ACCESS_DENIED_ERROR);
		Assert.isTrue(examUser.isAdmin(), ErrorInfo.ACCESS_DENIED_ERROR);
	}

	private UserResponse buildUserResponse(User user) {
		UserResponse userResponse = new UserResponse();
		BeanUtils.copyProperties(user, userResponse, DBConst.ROLES, FieldConst.OTP);
		userResponse.setRoles(user.getRoleStrings());
		return userResponse;
	}
}
