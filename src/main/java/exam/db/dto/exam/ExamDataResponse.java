package exam.db.dto.exam;

import exam.db.entity.Exam;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ExamDataResponse {
	private String id;
	private String name;
	private Integer progress;
	private List<String> cardIds;

	public ExamDataResponse(Exam exam) {
		this.id = exam.getId();
		this.name = exam.getName();
		this.cardIds = exam.getCardIds();
	}
}
