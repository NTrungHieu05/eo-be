package exam.db.dto.exam;

import exam.db.dto.question.QuestionResponse;
import exam.db.entity.ExamAttempt;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Xem lại một lần làm: kết quả, đáp án đã chọn và đề kèm đáp án đúng.
 * Đề lấy theo nội dung hiện tại; nếu admin đã sửa câu hỏi sau lần làm thì hiển thị bản mới.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AttemptDetailResponse extends AttemptSummaryResponse {
	private List<ExamAttempt.Answer> answers = new ArrayList<>();
	private List<QuestionResponse> questions = new ArrayList<>();

	public AttemptDetailResponse(ExamAttempt attempt) {
		super(attempt);
		if (attempt.getAnswers() != null) {
			answers = new ArrayList<>(attempt.getAnswers());
		}
	}
}
