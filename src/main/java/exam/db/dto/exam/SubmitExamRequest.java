package exam.db.dto.exam;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/** Bài làm học viên gửi lên khi nộp. Server tự chấm, không nhận điểm từ client. */
@Data
@NoArgsConstructor
public class SubmitExamRequest {
	private String examId;
	private List<Item> answers = new ArrayList<>();

	@Data
	@NoArgsConstructor
	public static class Item {
		private String questionId;
		/** Vị trí câu con trong câu hỏi nhóm; null với câu đơn. */
		private Integer childIndex;
		private String answer;
	}
}
