package exam.db.changelog;

import com.github.cloudyrock.mongock.ChangeLog;
import com.github.cloudyrock.mongock.ChangeSet;
import exam.db.entity.Answer;
import exam.db.entity.Question;
import exam.db.entity.Exam;
import exam.db.entity.QuestionContent;
import exam.db.entity.Topic;
import exam.db.repository.question.QuestionRepository;
import exam.db.repository.exam.ExamRepository;
import exam.db.repository.topic.TopicRepository;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ChangeLog(order = "006")
@Slf4j
public class DatabaseChangelogPracticeTest {

	public static final String PRACTICE_EXAM_ID = "68cb00010000000000000001";
	public static final String PRACTICE_EXAM_NAME = "Thi thử TOEIC";

	private static final String[] LETTERS_4 = {"A", "B", "C", "D"};
	private static final String[] LETTERS_3 = {"A", "B", "C"};

	@ChangeSet(id = "practice_test_full_toeic", author = "system", order = "001")
	public void seedPracticeTest(TopicRepository topicRepo, ExamRepository examRepo, QuestionRepository questionRepo) {
		if (examRepo.findFirstByIdAndDeletedIsFalse(PRACTICE_EXAM_ID) != null) {
			log.info("Practice test {} already exists, skip seed", PRACTICE_EXAM_ID);
			return;
		}

		Map<String, String> topicIdByName = topicRepo.findAllByDeletedIsFalse().stream()
				.collect(Collectors.toMap(Topic::getName, Topic::getId));

		String part1 = requireTopic(topicIdByName, "Phần 1:Mô Tả Tranh");
		String part2 = requireTopic(topicIdByName, "Phần 2:Hỏi - Đáp");
		String part3 = requireTopic(topicIdByName, "Phần 3:Đoạn Hội Thoại");
		String part4 = requireTopic(topicIdByName, "Phần 4:Bài Nói Ngắn");
		String part5 = requireTopic(topicIdByName, "Phần 5:Hoàn Thành Câu");
		String part6 = requireTopic(topicIdByName, "Phần 6:Hoàn Thành Đoạn Văn");
		String part7a = requireTopic(topicIdByName, "Phần 7:Đọc Hiểu - Đoạn Đơn");
		String part7b = requireTopic(topicIdByName, "Phần 7:Đọc Hiểu - Đoạn Kép");
		String part7c = requireTopic(topicIdByName, "Phần 7:Đọc Hiểu - Đoạn Ba");

		List<Question> questions = new ArrayList<>();
		questions.addAll(buildPart1(part1));
		questions.addAll(buildPart2(part2));
		questions.addAll(buildPart3(part3));
		questions.addAll(buildPart4(part4));
		questions.addAll(buildPart5(part5));
		questions.addAll(buildPart6(part6));
		questions.addAll(buildPart7Single(part7a));
		questions.addAll(buildPart7Double(part7b));
		questions.addAll(buildPart7Triple(part7c));

		questionRepo.saveAll(questions);

		List<String> questionIds = questions.stream().map(Question::getId).collect(Collectors.toList());
		Exam exam = new Exam();
		exam.setId(PRACTICE_EXAM_ID);
		exam.setName(PRACTICE_EXAM_NAME);
		exam.setPracticeTest(true);
		exam.setQuestionIds(questionIds);
		examRepo.save(exam);
		log.info("Seeded full TOEIC practice test {} with {} parent questions", PRACTICE_EXAM_ID, questionIds.size());
	}

	@ChangeSet(id = "practice_test_listening_media", author = "system", order = "002")
	public void attachListeningMedia(ExamRepository examRepo, QuestionRepository questionRepo) {
		Exam exam = examRepo.findFirstByIdAndDeletedIsFalse(PRACTICE_EXAM_ID);
		if (exam == null || exam.getQuestionIds() == null || exam.getQuestionIds().isEmpty()) {
			log.warn("Practice test {} not found, skip media attach", PRACTICE_EXAM_ID);
			return;
		}
		List<Question> found = questionRepo.findByIdInAndDeletedIsFalse(exam.getQuestionIds());
		Map<String, Question> byId = found.stream().collect(Collectors.toMap(Question::getId, question -> question, (a, b) -> a));
		int part1 = 0;
		int part2 = 0;
		int part3 = 0;
		int part4 = 0;
		int updated = 0;
		for (String id : exam.getQuestionIds()) {
			Question question = byId.get(id);
			if (question == null || question.getQuestion() == null) {
				continue;
			}
			if (part1 < 6) {
				part1++;
				question.getQuestion().setImage("/data/images/part1-" + part1 + ".png");
				question.getQuestion().setSound("/data/sounds/part1-" + part1 + ".wav");
			} else if (part2 < 25) {
				part2++;
				question.getQuestion().setSound("/data/sounds/part2-" + part2 + ".wav");
			} else if (part3 < 13) {
				part3++;
				question.getQuestion().setSound("/data/sounds/part3-" + part3 + ".wav");
			} else if (part4 < 10) {
				part4++;
				question.getQuestion().setSound("/data/sounds/part4-" + part4 + ".wav");
			} else {
				break;
			}
			questionRepo.save(question);
			updated++;
		}
		log.info("Attached listening media to {} practice-test questions", updated);
	}

	@ChangeSet(id = "practice_test_multiple_exams", author = "system", order = "003")
	public void seedMorePracticeTests(ExamRepository examRepo) {
		Exam original = examRepo.findFirstByIdAndDeletedIsFalse(PRACTICE_EXAM_ID);
		if (original == null) {
			log.warn("Practice test {} not found, skip extra exam seed", PRACTICE_EXAM_ID);
			return;
		}
		original.setName("Đề thi thử TOEIC 1");
		original.setPracticeTest(true);
		examRepo.save(original);
		seedPracticeExamIfMissing(examRepo, "68cb00010000000000000002", "Đề thi thử TOEIC 2", original.getQuestionIds());
		seedPracticeExamIfMissing(examRepo, "68cb00010000000000000003", "Đề thi thử TOEIC 3", original.getQuestionIds());
	}

	private void seedPracticeExamIfMissing(ExamRepository examRepo, String id, String name, List<String> questionIds) {
		if (examRepo.findFirstByIdAndDeletedIsFalse(id) != null) {
			return;
		}
		Exam exam = new Exam();
		exam.setId(id);
		exam.setName(name);
		exam.setPracticeTest(true);
		exam.setQuestionIds(questionIds == null ? new ArrayList<>() : new ArrayList<>(questionIds));
		examRepo.save(exam);
		log.info("Seeded practice test {} ({})", id, name);
	}

	private List<Question> buildPart1(String topicId) {
		List<Question> questions = new ArrayList<>();
		String[] stems = {
				"Look at the picture. What is the man doing?",
				"Look at the picture. Where are the people?",
				"Look at the picture. What is on the table?",
				"Look at the picture. What is the woman holding?",
				"Look at the picture. How is the weather?",
				"Look at the picture. What is next to the building?"
		};
		for (int i = 0; i < 6; i++) {
			int n = i + 1;
			questions.add(singleQuestion(topicId, stems[i], new String[]{
					"He is sitting at a desk.",
					"He is running in a park.",
					"He is cooking in a kitchen.",
					"He is driving a truck."
			}, i % 4, false, "/data/images/part1-" + n + ".png", "/data/sounds/part1-" + n + ".wav"));
		}
		return questions;
	}

	private List<Question> buildPart2(String topicId) {
		List<Question> questions = new ArrayList<>();
		for (int i = 1; i <= 25; i++) {
			questions.add(singleQuestion(topicId, "When will the meeting start? (Q" + i + ")", new String[]{
					"In the conference room.",
					"At nine o'clock.",
					"Yes, I received it."
			}, i % 3, false, "", "/data/sounds/part2-" + i + ".wav"));
		}
		return questions;
	}

	private List<Question> buildPart3(String topicId) {
		List<Question> questions = new ArrayList<>();
		for (int i = 1; i <= 13; i++) {
			questions.add(groupQuestion(topicId,
					"Conversation " + i + ": A man and a woman talk about a workplace schedule.",
					buildChildren(3, "According to the conversation, what will they do?", 4),
					"", "/data/sounds/part3-" + i + ".wav"));
		}
		return questions;
	}

	private List<Question> buildPart4(String topicId) {
		List<Question> questions = new ArrayList<>();
		for (int i = 1; i <= 10; i++) {
			questions.add(groupQuestion(topicId,
					"Talk " + i + ": A short announcement at a train station.",
					buildChildren(3, "What is the purpose of the talk?", 4),
					"", "/data/sounds/part4-" + i + ".wav"));
		}
		return questions;
	}

	private List<Question> buildPart5(String topicId) {
		List<Question> questions = new ArrayList<>();
		for (int i = 1; i <= 30; i++) {
			questions.add(singleQuestion(topicId,
					"The manager asked the staff to submit the report _____ Friday.",
					new String[]{"by", "until", "during", "within"},
					i % 4, false));
		}
		return questions;
	}

	private List<Question> buildPart6(String topicId) {
		List<Question> questions = new ArrayList<>();
		for (int i = 1; i <= 4; i++) {
			questions.add(groupQuestion(topicId,
					"Memo " + i + ": All employees must complete the training before the deadline. Please _____ the attached form.",
					buildChildren(4, "Choose the best word or phrase for the blank.", 4)));
		}
		return questions;
	}

	private List<Question> buildPart7Single(String topicId) {
		int[] childCounts = {2, 2, 3, 3, 3, 3, 3, 3, 4, 3};
		List<Question> questions = new ArrayList<>();
		for (int i = 0; i < childCounts.length; i++) {
			questions.add(groupQuestion(topicId,
					"Single passage " + (i + 1) + ": A notice about office hours and visitor registration.",
					buildChildren(childCounts[i], "What is indicated in the passage?", 4)));
		}
		return questions;
	}

	private List<Question> buildPart7Double(String topicId) {
		List<Question> questions = new ArrayList<>();
		for (int i = 1; i <= 2; i++) {
			questions.add(groupQuestion(topicId,
					"Double passage " + i + ": An email and a related schedule.",
					buildChildren(5, "What is suggested about the two texts?", 4)));
		}
		return questions;
	}

	private List<Question> buildPart7Triple(String topicId) {
		List<Question> questions = new ArrayList<>();
		for (int i = 1; i <= 3; i++) {
			questions.add(groupQuestion(topicId,
					"Triple passage " + i + ": An advertisement, a review, and a customer email.",
					buildChildren(5, "What is true according to the three texts?", 4)));
		}
		return questions;
	}

	private List<Question> buildChildren(int count, String stem, int choiceCount) {
		List<Question> children = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			String[] choices = choiceCount == 3
					? new String[]{"At the office.", "Tomorrow morning.", "Yes, she did."}
					: new String[]{"To request a refund.", "To confirm a reservation.", "To cancel an order.", "To apply for a job."};
			children.add(singleQuestion(null, stem + " (" + (i + 1) + ")", choices, i % choiceCount, true));
		}
		return children;
	}

	private Question singleQuestion(String topicId, String text, String[] choices, int correctIndex, boolean child) {
		return singleQuestion(topicId, text, choices, correctIndex, child, "", "");
	}

	private Question singleQuestion(String topicId, String text, String[] choices, int correctIndex, boolean child,
			String image, String sound) {
		QuestionContent content = new QuestionContent();
		content.setText(text);
		content.setHint("");
		content.setImage(image == null ? "" : image);
		content.setSound(sound == null ? "" : sound);

		Answer answer = new Answer();
		int index = correctIndex % choices.length;
		List<String> wrong = new ArrayList<>();
		for (int i = 0; i < choices.length; i++) {
			if (i != index) {
				wrong.add(choices[i]);
			}
		}
		String[] letters = choices.length == 3 ? LETTERS_3 : LETTERS_4;
		answer.setChoices(wrong);
		answer.setTexts(Collections.singletonList(choices[index]));
		answer.setHint(letters[index]);
		answer.setImage("");

		Question question = new Question();
		question.setExamId(PRACTICE_EXAM_ID);
		question.setTopicId(topicId);
		question.setQuestion(content);
		question.setAnswer(answer);
		question.setIsQuestionGroup(false);
		if (!child) {
			question.setChildQuestions(new ArrayList<>());
		}
		return question;
	}

	private Question groupQuestion(String topicId, String passage, List<Question> children) {
		return groupQuestion(topicId, passage, children, "", "");
	}

	private Question groupQuestion(String topicId, String passage, List<Question> children, String image, String sound) {
		QuestionContent content = new QuestionContent();
		content.setText(passage);
		content.setHint("");
		content.setImage(image == null ? "" : image);
		content.setSound(sound == null ? "" : sound);

		Answer answer = new Answer();
		answer.setChoices(new ArrayList<>());
		answer.setTexts(new ArrayList<>());
		answer.setHint("");
		answer.setImage("");

		Question question = new Question();
		question.setExamId(PRACTICE_EXAM_ID);
		question.setTopicId(topicId);
		question.setQuestion(content);
		question.setAnswer(answer);
		question.setIsQuestionGroup(true);
		question.setChildQuestions(children);
		return question;
	}

	private String requireTopic(Map<String, String> topicIdByName, String name) {
		String id = topicIdByName.get(name);
		if (id == null) {
			throw new IllegalStateException("Missing topic: " + name);
		}
		return id;
	}
}
