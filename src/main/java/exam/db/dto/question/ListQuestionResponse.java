package exam.db.dto.question;

import exam.db.dto.TotalResponse;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class ListQuestionResponse extends TotalResponse {
	private List<QuestionResponse> questions = new ArrayList<>();

	public ListQuestionResponse(List<QuestionResponse> questions, long total) {
		this.questions = questions;
		super.setTotal(total);
	}
}
