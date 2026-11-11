package exam.db.changelog;

import com.github.cloudyrock.mongock.ChangeLog;
import com.github.cloudyrock.mongock.ChangeSet;
import exam.db.entity.Answer;
import exam.db.entity.Question;
import exam.db.repository.question.QuestionRepository;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ChangeLog(order = "008")
@Slf4j
public class DatabaseChangelogAnswerKey {

	@ChangeSet(id = "repair_letter_answer_keys", author = "system", order = "001")
	public void repairLetterAnswerKeys(QuestionRepository questionRepo) {
		List<Question> questions = questionRepo.findAllByDeletedIsFalse();
		int updated = 0;
		for (Question question : questions) {
			boolean changed = repairQuestion(question);
			if (changed) {
				questionRepo.save(question);
				updated++;
			}
		}
		log.info("Repaired letter answer keys on {} questions", updated);
	}

	static boolean repairQuestion(Question question) {
		if (question == null) {
			return false;
		}
		boolean changed = repairAnswer(question.getAnswer());
		if (question.getChildQuestions() != null) {
			for (Question child : question.getChildQuestions()) {
				changed = repairAnswer(child == null ? null : child.getAnswer()) || changed;
			}
		}
		return changed;
	}

	static boolean repairAnswer(Answer answer) {
		if (answer == null || answer.getTexts() == null || answer.getTexts().size() != 1
				|| answer.getChoices() == null || answer.getChoices().size() < 2) {
			return false;
		}
		int index = letterIndex(answer.getTexts().get(0));
		if (index < 0 || index >= answer.getChoices().size()) {
			return false;
		}
		for (String choice : answer.getChoices()) {
			if (choice == null || choice.trim().length() <= 2) {
				return false;
			}
		}
		String correct = answer.getChoices().get(index);
		List<String> wrong = new ArrayList<>();
		for (int i = 0; i < answer.getChoices().size(); i++) {
			if (i != index) {
				wrong.add(answer.getChoices().get(i));
			}
		}
		String mark = answer.getTexts().get(0).trim();
		answer.setTexts(Collections.singletonList(correct));
		answer.setChoices(wrong);
		if (answer.getHint() == null || letterIndex(answer.getHint()) == letterIndex(mark)) {
			answer.setHint(mark.replace("(", "").replace(")", "").trim().toUpperCase());
		}
		return true;
	}

	private static int letterIndex(String mark) {
		if (mark == null) {
			return -1;
		}
		String normalized = mark.replace("(", "").replace(")", "").trim().toUpperCase();
		if (normalized.length() != 1) {
			return -1;
		}
		char letter = normalized.charAt(0);
		if (letter < 'A' || letter > 'D') {
			return -1;
		}
		return letter - 'A';
	}
}
