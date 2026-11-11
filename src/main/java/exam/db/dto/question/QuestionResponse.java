package exam.db.dto.question;

import com.fasterxml.jackson.annotation.JsonGetter;
import exam.db.entity.Answer;
import exam.db.entity.Question;
import exam.db.entity.QuestionContent;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.util.List;

@Data
@NoArgsConstructor
public class QuestionResponse {
	private String id;
	private QuestionContent question;
	private Answer answer;
	private Boolean isQuestionGroup;
	private List<Question> childQuestions;
	private String examId;
	private String topicId;
	private Integer partNumber;

	public QuestionResponse(Question question) {
		BeanUtils.copyProperties(question, this);
	}

	@JsonGetter("_id")
	public String getJsonId() {
		return id;
	}
}
