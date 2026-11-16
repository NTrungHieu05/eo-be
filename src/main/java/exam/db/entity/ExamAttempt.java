package exam.db.entity;

import exam.db.enums.CollectionConst;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/** Một lần nộp bài: kết quả và đáp án đã chọn, để học viên xem lại sau. */
@Document(collection = CollectionConst.EXAM_ATTEMPT)
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ExamAttempt extends AbstractVersion {
	@Id
	private String id;

	@Indexed
	private String userId;

	private String examId;
	private String examName;
	/** Part của đề luyện; null với đề thi thử. */
	private String topicId;
	private boolean practiceTest;
	private Date submittedAt;

	private int total;
	private int correct;
	private int incorrect;
	private int unanswered;
	private int listeningCorrect;
	private int listeningTotal;
	private int readingCorrect;
	private int readingTotal;
	private boolean officialScale;
	private Integer listeningScore;
	private Integer readingScore;
	private Integer totalScore;
	/** Phần trăm với luyện Part, điểm với thi thử (giống History). */
	private int progress;

	private List<Answer> answers = new ArrayList<>();

	@Data
	@NoArgsConstructor
	public static class Answer {
		private String questionId;
		private Integer childIndex;
		private String answer;
	}
}
