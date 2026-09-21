package exam.db.dto.exam;

import exam.db.dto.card.CardResponse;
import exam.db.entity.Exam;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class PracticeTestResponse {
	private String id;
	private String name;
	private Integer durationMinutes;
	private Integer listeningMinutes;
	private Integer readingMinutes;
	private Integer listeningQuestionCount;
	private Integer readingQuestionCount;
	private Integer questionCount;
	private long total;
	private List<PracticeTestPartResponse> parts = new ArrayList<>();
	private List<CardResponse> cards = new ArrayList<>();

	public PracticeTestResponse(Exam exam) {
		BeanUtils.copyProperties(exam, this);
	}
}
