package exam.db.dto.exam;

import exam.db.entity.Card;
import exam.db.entity.Exam;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.util.List;

@Data
@NoArgsConstructor
public class ExamResponse {
	private String id;
	private String name;
	private String topicId;
	private List<String> cardIds;
	private List<Card> cards;
	private Boolean practiceTest;

	public ExamResponse(Exam exam) {
		BeanUtils.copyProperties(exam, this);
	}
}
