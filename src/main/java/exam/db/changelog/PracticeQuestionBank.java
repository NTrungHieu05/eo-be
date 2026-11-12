package exam.db.changelog;

import com.fasterxml.jackson.databind.ObjectMapper;
import exam.db.entity.Answer;
import exam.db.entity.Question;
import exam.db.entity.QuestionContent;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Three full TOEIC practice banks. Exam 1 is also the part-practice set.
 * Listening audio for exams 2 and 3 lives under /data/sounds/exam2 and exam3.
 */
public class PracticeQuestionBank {

	static final int[] PART7_SINGLE_COUNTS = {2, 2, 3, 3, 3, 3, 3, 3, 4, 3};
	private static final String[] LETTERS = {"A", "B", "C", "D"};

	private final List<ExamSet> exams;

	private PracticeQuestionBank(List<ExamSet> exams) {
		this.exams = exams;
	}

	public static PracticeQuestionBank load() {
		ObjectMapper mapper = new ObjectMapper();
		try (InputStream in = PracticeQuestionBank.class.getResourceAsStream("/practice/practice-questions.json")) {
			if (in == null) {
				throw new IllegalStateException("Missing practice/practice-questions.json");
			}
			FileModel model = mapper.readValue(in, FileModel.class);
			return new PracticeQuestionBank(model.exams);
		} catch (IOException ex) {
			throw new IllegalStateException("Could not read practice questions", ex);
		}
	}

	public void validate() {
		if (exams == null || exams.size() != 3) {
			throw new IllegalStateException("Expected 3 practice exams");
		}
		Set<String> stems = new HashSet<>();
		Set<String> passages = new HashSet<>();
		Set<String> parents = new HashSet<>();
		for (int examIndex = 0; examIndex < exams.size(); examIndex++) {
			ExamSet exam = exams.get(examIndex);
			checkSingles(exam.part1, 6, 4, stems);
			checkSingles(exam.part2, 25, 3, stems);
			checkGroups(exam.part3, 13, 3, 4, stems, passages);
			checkGroups(exam.part4, 10, 3, 4, stems, passages);
			checkSingles(exam.part5, 30, 4, stems);
			checkGroups(exam.part6, 4, 4, 4, stems, passages);
			if (exam.part7single == null || exam.part7single.size() != PART7_SINGLE_COUNTS.length) {
				throw new IllegalStateException("Part 7 single passages are missing");
			}
			for (int i = 0; i < PART7_SINGLE_COUNTS.length; i++) {
				checkOneGroup(exam.part7single.get(i), PART7_SINGLE_COUNTS[i], 4, stems, passages);
			}
			checkGroups(exam.part7double, 2, 5, 4, stems, passages);
			checkGroups(exam.part7triple, 3, 5, 4, stems, passages);
			for (String parent : parentTexts(examIndex)) {
				if (!parents.add(parent)) {
					throw new IllegalStateException("Duplicate parent text: " + parent);
				}
			}
		}
	}

	public List<String> parentTexts(int examIndex) {
		ExamSet exam = exams.get(examIndex);
		List<String> texts = new ArrayList<>();
		addSingleParents(texts, exam.part1);
		addSingleParents(texts, exam.part2);
		addGroupParents(texts, exam.part3);
		addGroupParents(texts, exam.part4);
		addSingleParents(texts, exam.part5);
		addGroupParents(texts, exam.part6);
		addGroupParents(texts, exam.part7single);
		addGroupParents(texts, exam.part7double);
		addGroupParents(texts, exam.part7triple);
		return texts;
	}

	public void apply(List<Question> ordered, int examIndex) {
		if (ordered == null || ordered.size() != 103) {
			throw new IllegalStateException("A practice exam needs 103 parent questions");
		}
		ExamSet exam = exams.get(examIndex);
		int cursor = 0;
		cursor = applySingles(ordered, cursor, exam.part1, 1, examIndex);
		cursor = applySingles(ordered, cursor, exam.part2, 2, examIndex);
		cursor = applyGroups(ordered, cursor, exam.part3, 3, examIndex);
		cursor = applyGroups(ordered, cursor, exam.part4, 4, examIndex);
		cursor = applySingles(ordered, cursor, exam.part5, 0, examIndex);
		cursor = applyGroups(ordered, cursor, exam.part6, 0, examIndex);
		cursor = applyGroups(ordered, cursor, exam.part7single, 0, examIndex);
		cursor = applyGroups(ordered, cursor, exam.part7double, 0, examIndex);
		cursor = applyGroups(ordered, cursor, exam.part7triple, 0, examIndex);
		if (cursor != ordered.size()) {
			throw new IllegalStateException("Practice bank did not cover every question");
		}
	}

	private int applySingles(List<Question> ordered, int cursor, List<Item> items, int part, int examIndex) {
		for (int i = 0; i < items.size(); i++) {
			Question question = ordered.get(cursor++);
			if (question.getChildQuestions() != null && !question.getChildQuestions().isEmpty()) {
				throw new IllegalStateException("Expected a single question at index " + (cursor - 1));
			}
			String image = part == 1 ? "/data/images/part1-" + (i + 1) + ".png" : null;
			fill(question, items.get(i), image, sound(examIndex, part, i + 1));
		}
		return cursor;
	}

	private int applyGroups(List<Question> ordered, int cursor, List<Group> groups, int part, int examIndex) {
		for (int i = 0; i < groups.size(); i++) {
			Question question = ordered.get(cursor++);
			Group group = groups.get(i);
			List<Question> children = question.getChildQuestions();
			if (children == null || children.size() != group.questions.size()) {
				throw new IllegalStateException("Group size mismatch at index " + (cursor - 1));
			}
			QuestionContent content = content(question);
			content.setText(group.passage);
			String sound = sound(examIndex, part, i + 1);
			if (sound != null) {
				content.setSound(sound);
			}
			if (question.getAnswer() == null) {
				question.setAnswer(emptyAnswer());
			}
			for (int childIndex = 0; childIndex < children.size(); childIndex++) {
				fill(children.get(childIndex), group.questions.get(childIndex), null, null);
			}
		}
		return cursor;
	}

	private void fill(Question question, Item item, String image, String sound) {
		QuestionContent content = content(question);
		content.setText(item.text);
		if (image != null) {
			content.setImage(image);
		}
		if (sound != null) {
			content.setSound(sound);
		}
		int index = item.correct;
		List<String> wrong = new ArrayList<>();
		for (int i = 0; i < item.options.size(); i++) {
			if (i != index) {
				wrong.add(item.options.get(i));
			}
		}
		Answer answer = question.getAnswer() == null ? new Answer() : question.getAnswer();
		answer.setChoices(wrong);
		answer.setTexts(Collections.singletonList(item.options.get(index)));
		answer.setHint(LETTERS[index]);
		if (answer.getImage() == null) {
			answer.setImage("");
		}
		question.setAnswer(answer);
	}

	private QuestionContent content(Question question) {
		if (question.getQuestion() == null) {
			QuestionContent content = new QuestionContent();
			content.setHint("");
			content.setImage("");
			content.setSound("");
			question.setQuestion(content);
		}
		return question.getQuestion();
	}

	private Answer emptyAnswer() {
		Answer answer = new Answer();
		answer.setChoices(new ArrayList<>());
		answer.setTexts(new ArrayList<>());
		answer.setHint("");
		answer.setImage("");
		return answer;
	}

	private String sound(int examIndex, int part, int number) {
		if (part < 1 || part > 4) {
			return null;
		}
		String folder = examIndex == 0 ? "/data/sounds/" : "/data/sounds/exam" + (examIndex + 1) + "/";
		return folder + "part" + part + "-" + number + ".wav";
	}

	private void checkSingles(List<Item> items, int count, int options, Set<String> stems) {
		if (items == null || items.size() != count) {
			throw new IllegalStateException("Expected " + count + " single questions");
		}
		for (Item item : items) {
			checkItem(item, options, stems);
		}
	}

	private void checkGroups(List<Group> groups, int count, int questions, int options,
			Set<String> stems, Set<String> passages) {
		if (groups == null || groups.size() != count) {
			throw new IllegalStateException("Expected " + count + " question groups");
		}
		for (Group group : groups) {
			checkOneGroup(group, questions, options, stems, passages);
		}
	}

	private void checkOneGroup(Group group, int questions, int options, Set<String> stems, Set<String> passages) {
		if (group == null || group.passage == null || group.passage.trim().isEmpty() || !passages.add(group.passage)) {
			throw new IllegalStateException("Duplicate or empty passage");
		}
		if (group.questions == null || group.questions.size() != questions) {
			throw new IllegalStateException("Unexpected child-question count");
		}
		for (Item item : group.questions) {
			checkItem(item, options, stems);
		}
	}

	private void checkItem(Item item, int options, Set<String> stems) {
		if (item == null || item.text == null || item.text.trim().isEmpty() || !stems.add(item.text)) {
			throw new IllegalStateException("Duplicate or empty question: " + (item == null ? "" : item.text));
		}
		if (item.options == null || item.options.size() != options || item.correct < 0 || item.correct >= options) {
			throw new IllegalStateException("Bad choices for: " + item.text);
		}
		if (new HashSet<>(item.options).size() != options) {
			throw new IllegalStateException("Repeated choices for: " + item.text);
		}
	}

	private void addSingleParents(List<String> texts, List<Item> items) {
		for (Item item : items) {
			texts.add(item.text);
		}
	}

	private void addGroupParents(List<String> texts, List<Group> groups) {
		for (Group group : groups) {
			texts.add(group.passage);
		}
	}

	static class FileModel {
		public List<ExamSet> exams;
	}

	static class ExamSet {
		public List<Item> part1;
		public List<Item> part2;
		public List<Group> part3;
		public List<Group> part4;
		public List<Item> part5;
		public List<Group> part6;
		public List<Group> part7single;
		public List<Group> part7double;
		public List<Group> part7triple;
	}

	static class Item {
		public String text;
		public List<String> options;
		public int correct;
	}

	static class Group {
		public String passage;
		public List<Item> questions;
	}
}
