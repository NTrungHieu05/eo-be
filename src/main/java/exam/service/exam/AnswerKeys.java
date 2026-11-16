package exam.service.exam;

import exam.db.entity.Answer;
import exam.db.entity.Question;
import exam.db.entity.QuestionContent;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Ẩn đáp án trước khi gửi đề cho người làm bài.
 * Đáp án đúng (answer.texts) và gợi ý chỉ trả về qua POST /exam/submit sau khi nộp.
 */
public final class AnswerKeys {

	private AnswerKeys() {
	}

	public static Question redact(Question source) {
		if (source == null) {
			return null;
		}
		Question copy = new Question();
		BeanUtils.copyProperties(source, copy);
		copy.setAnswer(redact(source.getAnswer()));
		if (source.getQuestion() != null) {
			QuestionContent content = new QuestionContent();
			BeanUtils.copyProperties(source.getQuestion(), content);
			content.setHint(null);
			copy.setQuestion(content);
		}
		if (source.getChildQuestions() != null) {
			List<Question> children = new ArrayList<>();
			for (Question child : source.getChildQuestions()) {
				children.add(redact(child));
			}
			copy.setChildQuestions(children);
		}
		return copy;
	}

	private static Answer redact(Answer source) {
		if (source == null) {
			return null;
		}
		Set<String> options = new LinkedHashSet<>();
		if (source.getChoices() != null) {
			options.addAll(source.getChoices());
		}
		if (source.getTexts() != null) {
			options.addAll(source.getTexts());
		}
		options.remove(null);
		List<String> sorted = new ArrayList<>(options);
		// Sắp theo chữ cái để vị trí đáp án đúng không lộ qua thứ tự.
		Collections.sort(sorted);
		Answer copy = new Answer();
		copy.setChoices(sorted);
		copy.setTexts(new ArrayList<>());
		copy.setImage(source.getImage());
		copy.setHint(null);
		return copy;
	}

	/** Cùng quy tắc với answersMatch ở frontend: so sánh sau khi trim, không rỗng. */
	public static boolean matches(String userAnswer, List<String> texts) {
		if (userAnswer == null || texts == null) {
			return false;
		}
		String given = userAnswer.trim();
		if (given.isEmpty()) {
			return false;
		}
		for (String text : texts) {
			if (text != null && given.equals(text.trim())) {
				return true;
			}
		}
		return false;
	}
}
