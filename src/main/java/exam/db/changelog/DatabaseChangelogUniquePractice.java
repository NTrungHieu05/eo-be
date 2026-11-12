package exam.db.changelog;

import com.github.cloudyrock.mongock.ChangeLog;
import com.github.cloudyrock.mongock.ChangeSet;
import exam.db.entity.Answer;
import exam.db.entity.Exam;
import exam.db.entity.Question;
import exam.db.entity.QuestionContent;
import exam.db.repository.exam.ExamRepository;
import exam.db.repository.question.QuestionRepository;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ChangeLog(order = "010")
@Slf4j
public class DatabaseChangelogUniquePractice {

	static final String EXAM_1 = "68cb00010000000000000001";
	static final String EXAM_2 = "68cb00010000000000000002";
	static final String EXAM_3 = "68cb00010000000000000003";

	@ChangeSet(id = "unique_practice_question_text", author = "system", order = "001")
	public void replaceClonedPracticeQuestions(ExamRepository examRepo, QuestionRepository questionRepo) {
		PracticeQuestionBank bank = PracticeQuestionBank.load();
		bank.validate();
		Exam exam1 = examRepo.findFirstByIdAndDeletedIsFalse(EXAM_1);
		if (exam1 == null || exam1.getQuestionIds() == null || exam1.getQuestionIds().isEmpty()) {
			log.warn("Practice exam 1 is missing, skip distinct question text");
			return;
		}
		List<Question> ordered = loadInOrder(questionRepo, exam1.getQuestionIds());
		if (!matches(bank, ordered, 0)) {
			bank.apply(ordered, 0);
			questionRepo.saveAll(ordered);
			log.info("Replaced cloned text on practice exam 1");
		}
		separate(examRepo, questionRepo, bank, EXAM_2, exam1.getQuestionIds(), ordered, 1);
		separate(examRepo, questionRepo, bank, EXAM_3, exam1.getQuestionIds(), ordered, 2);
	}

	private void separate(ExamRepository examRepo, QuestionRepository questionRepo, PracticeQuestionBank bank,
			String examId, List<String> sharedIds, List<Question> template, int examIndex) {
		Exam exam = examRepo.findFirstByIdAndDeletedIsFalse(examId);
		if (exam == null) {
			log.warn("Practice exam {} is missing, skip", examId);
			return;
		}
		boolean shared = exam.getQuestionIds() != null && exam.getQuestionIds().equals(sharedIds);
		if (!shared) {
			List<Question> own = loadInOrder(questionRepo, exam.getQuestionIds());
			if (matches(bank, own, examIndex)) {
				return;
			}
			bank.apply(own, examIndex);
			questionRepo.saveAll(own);
			log.info("Updated distinct text on practice exam {}", examId);
			return;
		}
		List<Question> owned = questionRepo.findByExamIdAndDeletedIsFalse(examId);
		if (owned.size() == 103) {
			exam.setQuestionIds(align(owned, bank, examIndex));
			exam.setPracticeTest(true);
			examRepo.save(exam);
			log.info("Linked existing questions to practice exam {}", examId);
			return;
		}
		if (!owned.isEmpty()) {
			throw new IllegalStateException("Practice exam " + examId + " has " + owned.size() + " questions");
		}
		List<Question> copies = new ArrayList<>();
		for (Question source : template) {
			copies.add(copyQuestion(source, examId));
		}
		bank.apply(copies, examIndex);
		List<String> ids = new ArrayList<>();
		for (Question saved : questionRepo.saveAll(copies)) {
			ids.add(saved.getId());
		}
		exam.setQuestionIds(ids);
		exam.setPracticeTest(true);
		examRepo.save(exam);
		log.info("Created {} distinct questions for practice exam {}", ids.size(), examId);
	}

	private boolean matches(PracticeQuestionBank bank, List<Question> ordered, int examIndex) {
		if (ordered.size() != 103) {
			return false;
		}
		List<String> expected = bank.parentTexts(examIndex);
		return expected.get(0).equals(textOf(ordered.get(0)))
				&& expected.get(54).equals(textOf(ordered.get(54)))
				&& expected.get(102).equals(textOf(ordered.get(102)));
	}

	private List<String> align(List<Question> owned, PracticeQuestionBank bank, int examIndex) {
		Map<String, Question> byText = new HashMap<>();
		for (Question question : owned) {
			byText.put(textOf(question), question);
		}
		List<String> ids = new ArrayList<>();
		for (String expected : bank.parentTexts(examIndex)) {
			Question question = byText.get(expected);
			if (question == null) {
				throw new IllegalStateException("Could not match an existing practice question");
			}
			ids.add(question.getId());
		}
		return ids;
	}

	private List<Question> loadInOrder(QuestionRepository questionRepo, List<String> ids) {
		if (ids == null) {
			throw new IllegalStateException("Practice exam has no questions");
		}
		Map<String, Question> byId = new HashMap<>();
		for (Question question : questionRepo.findByIdInAndDeletedIsFalse(ids)) {
			byId.put(question.getId(), question);
		}
		List<Question> ordered = new ArrayList<>();
		for (String id : ids) {
			Question question = byId.get(id);
			if (question == null) {
				throw new IllegalStateException("Missing practice question " + id);
			}
			ordered.add(question);
		}
		return ordered;
	}

	private Question copyQuestion(Question source, String examId) {
		Question copy = new Question();
		copy.setExamId(examId);
		copy.setTopicId(source.getTopicId());
		copy.setIsQuestionGroup(Boolean.TRUE.equals(source.getIsQuestionGroup()));
		QuestionContent content = new QuestionContent();
		if (source.getQuestion() != null) {
			content.setText(source.getQuestion().getText());
			content.setImage(source.getQuestion().getImage());
			content.setSound(source.getQuestion().getSound());
			content.setHint(source.getQuestion().getHint() == null ? "" : source.getQuestion().getHint());
		}
		copy.setQuestion(content);
		copy.setAnswer(copyAnswer(source.getAnswer()));
		if (source.getChildQuestions() != null) {
			List<Question> children = new ArrayList<>();
			for (Question child : source.getChildQuestions()) {
				children.add(copyQuestion(child, examId));
			}
			copy.setChildQuestions(children);
		}
		return copy;
	}

	private Answer copyAnswer(Answer source) {
		Answer answer = new Answer();
		answer.setChoices(source == null || source.getChoices() == null ? new ArrayList<>() : new ArrayList<>(source.getChoices()));
		answer.setTexts(source == null || source.getTexts() == null ? new ArrayList<>() : new ArrayList<>(source.getTexts()));
		answer.setHint(source == null || source.getHint() == null ? "" : source.getHint());
		answer.setImage(source == null || source.getImage() == null ? "" : source.getImage());
		return answer;
	}

	private String textOf(Question question) {
		if (question == null || question.getQuestion() == null || question.getQuestion().getText() == null) {
			return "";
		}
		return question.getQuestion().getText();
	}
}
