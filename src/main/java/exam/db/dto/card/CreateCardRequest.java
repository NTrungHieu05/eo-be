package exam.db.dto.card;

import exam.db.entity.Answer;
import exam.db.entity.Card;
import exam.db.entity.Question;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class CreateCardRequest {
	private Question question;
	private Answer answer;
	private Boolean isQuestionGroup;
	private List<Card> childCards;
	private String examId;
	private String topicId;
}
