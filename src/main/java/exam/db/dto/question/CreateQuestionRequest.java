package exam.db.dto.question;

import exam.db.entity.Answer;
import exam.db.entity.Question;
import exam.db.entity.QuestionContent;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class CreateQuestionRequest {
	private QuestionContent question;
	private Answer answer;
	private Boolean isQuestionGroup;
	private List<Question> childQuestions;
	private String examId;
	private String topicId;
}
