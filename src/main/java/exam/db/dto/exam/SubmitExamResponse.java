package exam.db.dto.exam;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class SubmitExamResponse {
	private String examId;
	/** Id lần làm vừa lưu, dùng để mở lại trong Lịch sử làm bài. */
	private String attemptId;
	private boolean practiceTest;
	private int total;
	private int correct;
	private int incorrect;
	private int unanswered;
	private int listeningCorrect;
	private int listeningTotal;
	private int readingCorrect;
	private int readingTotal;
	/** true khi đề đủ 100 câu nghe và 100 câu đọc, điểm quy đổi theo thang TOEIC. */
	private boolean officialScale;
	private Integer listeningScore;
	private Integer readingScore;
	private Integer totalScore;
	/** Giá trị đã lưu vào lịch sử: phần trăm với luyện Part, điểm với thi thử. */
	private int progress;
	private List<QuestionResult> results = new ArrayList<>();

	@Data
	@NoArgsConstructor
	public static class QuestionResult {
		private String questionId;
		private Integer childIndex;
		private boolean correct;
		private List<String> correctAnswers;
		private String hint;
	}
}
