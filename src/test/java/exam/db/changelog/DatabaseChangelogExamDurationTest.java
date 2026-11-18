package exam.db.changelog;

import exam.db.entity.Exam;
import exam.db.entity.Question;
import exam.db.repository.exam.ExamRepository;
import exam.db.repository.question.QuestionRepository;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DatabaseChangelogExamDurationTest {

	private static Question single(String id) {
		Question question = new Question();
		question.setId(id);
		return question;
	}

	private static Question group(String id, int children) {
		Question question = single(id);
		List<Question> childQuestions = new ArrayList<>();
		for (int i = 0; i < children; i++) {
			childQuestions.add(new Question());
		}
		question.setChildQuestions(childQuestions);
		return question;
	}

	@Test
	void partMinutesStaysBetweenTenAndTwenty() {
		assertThat(DatabaseChangelogExamDuration.partMinutes(0)).isEqualTo(10);
		assertThat(DatabaseChangelogExamDuration.partMinutes(6)).isEqualTo(10);
		assertThat(DatabaseChangelogExamDuration.partMinutes(15)).isEqualTo(15);
		assertThat(DatabaseChangelogExamDuration.partMinutes(39)).isEqualTo(20);
	}

	@Test
	void groupQuestionsCountTheirChildren() {
		assertThat(DatabaseChangelogExamDuration.countQuestions(List.of(single("a"), group("g", 3)))).isEqualTo(4);
	}

	@Test
	void setsFullTestTo120AndPartByQuestionCountAndKeepsExistingDuration() {
		ExamRepository examRepo = mock(ExamRepository.class);
		QuestionRepository questionRepo = mock(QuestionRepository.class);

		Exam full = new Exam();
		full.setPracticeTest(true);
		Exam part = new Exam();
		part.setQuestionIds(List.of("g1", "g2", "g3", "g4", "g5"));
		Exam alreadySet = new Exam();
		alreadySet.setDurationMinutes(45);

		when(examRepo.findAllByDeletedIsFalse()).thenReturn(List.of(full, part, alreadySet));
		when(questionRepo.findByIdInAndDeletedIsFalse(any())).thenReturn(List.of(
				group("g1", 3), group("g2", 3), group("g3", 3), group("g4", 3), group("g5", 3)));

		new DatabaseChangelogExamDuration().setDefaultExamDuration(examRepo, questionRepo);

		assertThat(full.getDurationMinutes()).isEqualTo(120);
		assertThat(part.getDurationMinutes()).isEqualTo(15);
		assertThat(alreadySet.getDurationMinutes()).isEqualTo(45);
		verify(examRepo, never()).save(alreadySet);
	}
}
