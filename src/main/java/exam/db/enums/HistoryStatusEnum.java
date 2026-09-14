package exam.db.enums;

public enum HistoryStatusEnum {
	PENDING(0, "PENDING"),
	COMPLETE(1, "COMPLETE");

	private final Integer id;
	private final String name;

	HistoryStatusEnum(Integer id, String name) {
		this.id = id;
		this.name = name;
	}

	public Integer getId() {
		return id;
	}

	public String getName() {
		return name;
	}
}
