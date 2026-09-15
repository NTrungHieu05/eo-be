package exam.db.entity;

import exam.db.enums.CollectionConst;
import exam.db.enums.FieldConst;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Document(collection = CollectionConst.USER)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class User extends AbstractVersion implements UserDetails, CredentialsContainer {
	private static final long serialVersionUID = 0L;

	public static final String[] IGNORE_UPDATE_PROPERTY = {"id", "username", "password", "email"};

	@Id
	private String userId;

	@Field(FieldConst.USERNAME)
	private String username;

	@Field(FieldConst.PASSWORD)
	private String password;

	@Field(FieldConst.ROLES)
	private Set<Role> roles = new HashSet<>();

	@Field(FieldConst.ACCOUNT_NON_EXPIRED)
	private boolean accountNonExpired;

	@Field(FieldConst.ACCOUNT_NON_LOCKED)
	private boolean accountNonLocked;

	@Field(FieldConst.CREDENTIALS_NON_EXPIRED)
	private boolean credentialsNonExpired;

	@Field(FieldConst.ENABLED)
	private boolean enabled;

	@Field(FieldConst.FIRST_NAME)
	private String firstName;

	@Field(FieldConst.LAST_NAME)
	private String lastName;

	@Field(FieldConst.DISPLAY_NAME)
	private String displayName;

	@Field(FieldConst.EMAIL)
	private String email;

	@Field(FieldConst.PHONE)
	private String phone;

	@Field(FieldConst.ADDRESS)
	private String address;

	@Field(FieldConst.DOB)
	private LocalDate dob;

	@Field(FieldConst.GENDER)
	private String gender;

	@Field(FieldConst.OTP)
	private String otp;

	public Set<String> getRoleStrings() {
		return this.roles.stream().map(Role::getName).collect(Collectors.toSet());
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return roles;
	}

	@Override
	public void eraseCredentials() {
		password = null;
	}
}
