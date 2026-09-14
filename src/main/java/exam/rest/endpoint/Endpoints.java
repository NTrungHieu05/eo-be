package exam.rest.endpoint;

public interface Endpoints {
	String USER_URL = "/user";
	String CHANGE_PASSWORD_URL = "/user/pwd/change";
	String USER_INFO_URL = "/user/info";
	String FORGOT_PASSWORD_URL = "/user/pwd/forgot";
	String RESET_PASSWORD_URL = "/user/pwd/reset";

	String ADMIN_USERS_URL = "/admin/users";
	String ADMIN_USERS_BLOCK = "/admin/users/block";
	String ADMIN_USERS_UNBLOCK = "/admin/users/unblock";
	String ADMIN_CARD_URL = "/admin/card";
	String ADMIN_SKILL_URL = "/admin/skill";
	String ADMIN_TOPIC_URL = "/admin/topic";
	String ADMIN_EXAM_URL = "/admin/exam";

	String REGISTER_USER_URL = "public/register/user";

	String LOGIN_URL = "/app/login";
	String LOGOUT_URL = "/app/logout";

	String NOTIFICATION = "/notification";
	String VERIFY_OTP = "/verify-otp";

	String PATH_VARIABLE_URL = "/{id}";
	String PATH_VARIABLE_EXAM_ID_URL = "/{examId}";
	String PATH_VARIABLE_TOPIC_ID_URL = "/{topicId}";

	String MEDIA_URL_BASE64 = "/media/url/base64";

	String CARD_URL = "/card";
	String CARD_IMPORT_URL = "/card/import";
	String CARD_DELETE_MULTIPLE = "/card/delete/multiple";
	String MINI_TEST_URL = "/mini-test";

	String SKILL_URL = "/skill";
	String SKILL_HEADER_URL = "/skill/header";
	String SKILL_DELETE_MULTIPLE = "/skill/delete/multiple";

	String TOPIC_URL = "/topic";
	String TOPIC_EXAM_URL = "/topic/exam";
	String TOPIC_DELETE_MULTIPLE = "/topic/delete/multiple";

	String EXAM_URL = "/exam";
	String EXAM_CARD_URL = "/exam/card";
	String EXAM_DELETE_MULTIPLE = "/exam/delete/multiple";

	String HISTORY_URL = "/exam/collect";
}
