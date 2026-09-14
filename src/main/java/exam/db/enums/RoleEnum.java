package exam.db.enums;

public enum RoleEnum {
	ROLE_ADMIN("ROLE_ADMIN"),
	ROLE_TEACHING_ASSISTANT("ROLE_TEACHING_ASSISTANT"),
	ROLE_USER("ROLE_USER");

	private String name;

	RoleEnum(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
