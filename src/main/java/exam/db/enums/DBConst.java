package exam.db.enums;

public class DBConst {

	private DBConst() {
	}

	public static final String ID = "id";
	public static final int DEFAULT_PAGE_SIZE = 70;
	public static final int DEFAULT_MAX_PAGE_SIZE = Integer.MAX_VALUE;
	public static final int MIN_PASSWORD = 8;
	public static final int MAX_PASSWORD = 32;
	public static final int MIN_CHARACTER_PASSWORD = 1;
	public static final String DIRECTION_DESC = "DESC";
	public static final String DIRECTION_ASC = "ASC";
	public static final String ROLES = "roles";
	public static final String COUNT = "count";
	public static final String EXAM_ID = "examId";
	public static final String TOPIC_ID = "topicId";
}
