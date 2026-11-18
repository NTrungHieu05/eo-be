package exam.db.changelog;

import com.github.cloudyrock.mongock.ChangeLog;
import com.github.cloudyrock.mongock.ChangeSet;
import exam.db.entity.Exam;
import exam.db.entity.Question;
import exam.db.repository.exam.ExamRepository;
import exam.db.repository.question.QuestionRepository;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Đặt thời gian làm bài cho các đề tạo trước khi có trường durationMinutes:
 * đề thi thử (full test) 120 phút; đề luyện từng phần 10–20 phút tùy số câu
 * (khoảng 1 phút/câu, câu nhóm tính theo số câu con).
 */
@ChangeLog(order = "012")
@Slf4j
public class DatabaseChangelogExamDuration {

	static final int FULL_TEST_MINUTES = 120;
	static final int PART_MIN_MINUTES = 10;
	static final int PART_MAX_MINUTES = 20;

	@ChangeSet(id = "set_default_exam_duration", author = "system", order = "001")
	public void setDefaultExamDuration(ExamRepository examRepo, QuestionRepository questionRepo) {
		int updated = 0;
		for (Exam exam : examRepo.findAllByDeletedIsFalse()) {
			if (exam.getDurationMinutes() != null) {
				continue;
			}
			if (Boolean.TRUE.equals(exam.getPracticeTest())) {
				exam.setDurationMinutes(FULL_TEST_MINUTES);
			} else {
				List<String> ids = exam.getQuestionIds();
				List<Question> questions = ids == null || ids.isEmpty()
						? List.of()
						: questionRepo.findByIdInAndDeletedIsFalse(ids);
				exam.setDurationMinutes(partMinutes(countQuestions(questions)));
			}
			examRepo.save(exam);
			updated++;
		}
		log.info("Set default duration on {} exams", updated);
	}

	static int partMinutes(int questionCount) {
		return Math.max(PART_MIN_MINUTES, Math.min(PART_MAX_MINUTES, questionCount));
	}

	static int countQuestions(List<Question> questions) {
		int count = 0;
		for (Question question : questions) {
			if (question == null) {
				continue;
			}
			List<Question> children = question.getChildQuestions();
			count += children != null && !children.isEmpty() ? children.size() : 1;
		}
		return count;
	}
}
