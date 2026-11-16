package exam.db.dto.exam;

import exam.db.entity.ExamAttempt;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.util.Date;

/** Một dòng trong danh sách lịch sử làm bài. */
@Data
@NoArgsConstructor
public class AttemptSummaryResponse {
	private String id;
	private String examId;
	private String examName;
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
	private int progress;

	public AttemptSummaryResponse(ExamAttempt attempt) {
		BeanUtils.copyProperties(attempt, this);
	}
}
