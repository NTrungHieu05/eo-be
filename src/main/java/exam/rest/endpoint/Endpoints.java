package exam.rest.endpoint;

public interface Endpoints {
	// ==================== USER =======================
	public static final String USER_URL = "/user";
	public static final String CHANGE_PASSWORD_URL = "/user/pwd/change";
	public static final String USER_INFO_URL = "/user/info";
	public static final String FORGOT_PASSWORD_URL = "/user/pwd/forgot";
	public static final String RESET_PASSWORD_URL = "/user/pwd/reset";
	// ==================== ADMIN =======================
	public static final String ADMIN_USERS_URL = "/admin/users";
	public static final String ADMIN_USERS_BLOCK = "/admin/users/block";
	public static final String ADMIN_USERS_UNBLOCK = "/admin/users/unblock";
	public static final String ADMIN_QUESTION_URL = "/admin/question";
	public static final String ADMIN_SKILL_URL = "/admin/skill";
	public static final String ADMIN_TOPIC_URL = "/admin/topic";
	public static final String ADMIN_EXAM_URL = "/admin/exam";
	//=========================== PUBLIC ======================
	String REGISTER_USER_URL = "public/register/user";
	//=========================== LOGIN/ LOGOUT ======================
	String LOGIN_URL = "/app/login";
	String LOGOUT_URL = "/app/logout";
	//=========================== COMMONS =====================
	public static final String PATH_VARIABLE_URL = "/{id}";
	public static final String PATH_VARIABLE_EXAM_ID_URL = "/{examId}";
	public static final String PATH_VARIABLE_TOPIC_ID_URL = "/{topicId}";
	//=========================== MEDIA =====================
	public static final String MEDIA_URL_BASE64 = "/media/url/base64";
	public static final String UPLOAD_URL = "/upload";
	//=========================== QUESTION =====================
	public static final String QUESTION_URL = "/question";
	public static final String QUESTION_IMPORT_URL = "/question/import";
	public static final String QUESTION_DELETE_MULTIPLE = "/question/delete/multiple";
	public static final String PRACTICE_TEST_URL = "/practice-test";
	//=========================== SKILL =====================
	public static final String SKILL_URL = "/skill";
	public static final String SKILL_HEADER_URL = "/skill/header";
	public static final String SKILL_ADD_TOPIC_URL = "/skill/add-topic";
	public static final String SKILL_DELETE_MULTIPLE = "/skill/delete/multiple";
	//=========================== TOPIC =====================
	public static final String TOPIC_URL = "/topic";
	public static final String TOPIC_EXAM_URL = "/topic/exam";
	public static final String TOPIC_ADD_EXAM_URL = "/topic/add-exam";
	public static final String TOPIC_DELETE_MULTIPLE = "/topic/delete/multiple";
	//=========================== EXAM =====================
	public static final String EXAM_URL = "/exam";
	public static final String EXAM_QUESTION_URL = "/exam/question";
	public static final String EXAM_ADD_QUESTION_URL = "/exam/add-question";
	public static final String EXAM_DELETE_MULTIPLE = "/exam/delete/multiple";
	public static final String EXAM_SCORE_URL = "/exam/score";
	//=========================== HISTORY =====================
	public static final String HISTORY_URL = "/exam/collect";
}
