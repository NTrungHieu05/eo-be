package exam.db.changelog;

import com.github.cloudyrock.mongock.ChangeLog;
import com.github.cloudyrock.mongock.ChangeSet;
import exam.db.entity.Answer;
import exam.db.entity.Card;
import exam.db.entity.Exam;
import exam.db.entity.Question;
import exam.db.entity.Topic;
import exam.db.repository.card.CardRepository;
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
	public void seedPracticeTest(TopicRepository topicRepo, ExamRepository examRepo, CardRepository cardRepo) {
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

		List<Card> cards = new ArrayList<>();
		cards.addAll(buildPart1(part1));
		cards.addAll(buildPart2(part2));
		cards.addAll(buildPart3(part3));
		cards.addAll(buildPart4(part4));
		cards.addAll(buildPart5(part5));
		cards.addAll(buildPart6(part6));
		cards.addAll(buildPart7Single(part7a));
		cards.addAll(buildPart7Double(part7b));
		cards.addAll(buildPart7Triple(part7c));

		cardRepo.saveAll(cards);

		List<String> cardIds = cards.stream().map(Card::getId).collect(Collectors.toList());
		Exam exam = new Exam();
		exam.setId(PRACTICE_EXAM_ID);
		exam.setName(PRACTICE_EXAM_NAME);
		exam.setPracticeTest(true);
		exam.setCardIds(cardIds);
		examRepo.save(exam);
		log.info("Seeded full TOEIC practice test {} with {} parent cards", PRACTICE_EXAM_ID, cardIds.size());
	}

	private List<Card> buildPart1(String topicId) {
		List<Card> cards = new ArrayList<>();
		String[] stems = {
				"Look at the picture. What is the man doing?",
				"Look at the picture. Where are the people?",
				"Look at the picture. What is on the table?",
				"Look at the picture. What is the woman holding?",
				"Look at the picture. How is the weather?",
				"Look at the picture. What is next to the building?"
		};
		for (int i = 0; i < 6; i++) {
			cards.add(singleCard(topicId, stems[i], new String[]{
					"He is sitting at a desk.",
					"He is running in a park.",
					"He is cooking in a kitchen.",
					"He is driving a truck."
			}, i % 4, false));
		}
		return cards;
	}

	private List<Card> buildPart2(String topicId) {
		List<Card> cards = new ArrayList<>();
		for (int i = 1; i <= 25; i++) {
			cards.add(singleCard(topicId, "When will the meeting start? (Q" + i + ")", new String[]{
					"In the conference room.",
					"At nine o'clock.",
					"Yes, I received it."
			}, i % 3, false));
		}
		return cards;
	}

	private List<Card> buildPart3(String topicId) {
		List<Card> cards = new ArrayList<>();
		for (int i = 1; i <= 13; i++) {
			cards.add(groupCard(topicId,
					"Conversation " + i + ": A man and a woman talk about a workplace schedule.",
					buildChildren(3, "According to the conversation, what will they do?", 4)));
		}
		return cards;
	}

	private List<Card> buildPart4(String topicId) {
		List<Card> cards = new ArrayList<>();
		for (int i = 1; i <= 10; i++) {
			cards.add(groupCard(topicId,
					"Talk " + i + ": A short announcement at a train station.",
					buildChildren(3, "What is the purpose of the talk?", 4)));
		}
		return cards;
	}

	private List<Card> buildPart5(String topicId) {
		List<Card> cards = new ArrayList<>();
		for (int i = 1; i <= 30; i++) {
			cards.add(singleCard(topicId,
					"The manager asked the staff to submit the report _____ Friday.",
					new String[]{"by", "until", "during", "within"},
					i % 4, false));
		}
		return cards;
	}

	private List<Card> buildPart6(String topicId) {
		List<Card> cards = new ArrayList<>();
		for (int i = 1; i <= 4; i++) {
			cards.add(groupCard(topicId,
					"Memo " + i + ": All employees must complete the training before the deadline. Please _____ the attached form.",
					buildChildren(4, "Choose the best word or phrase for the blank.", 4)));
		}
		return cards;
	}

	private List<Card> buildPart7Single(String topicId) {
		int[] childCounts = {2, 2, 3, 3, 3, 3, 3, 3, 4, 3};
		List<Card> cards = new ArrayList<>();
		for (int i = 0; i < childCounts.length; i++) {
			cards.add(groupCard(topicId,
					"Single passage " + (i + 1) + ": A notice about office hours and visitor registration.",
					buildChildren(childCounts[i], "What is indicated in the passage?", 4)));
		}
		return cards;
	}

	private List<Card> buildPart7Double(String topicId) {
		List<Card> cards = new ArrayList<>();
		for (int i = 1; i <= 2; i++) {
			cards.add(groupCard(topicId,
					"Double passage " + i + ": An email and a related schedule.",
					buildChildren(5, "What is suggested about the two texts?", 4)));
		}
		return cards;
	}

	private List<Card> buildPart7Triple(String topicId) {
		List<Card> cards = new ArrayList<>();
		for (int i = 1; i <= 3; i++) {
			cards.add(groupCard(topicId,
					"Triple passage " + i + ": An advertisement, a review, and a customer email.",
					buildChildren(5, "What is true according to the three texts?", 4)));
		}
		return cards;
	}

	private List<Card> buildChildren(int count, String stem, int choiceCount) {
		List<Card> children = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			String[] choices = choiceCount == 3
					? new String[]{"At the office.", "Tomorrow morning.", "Yes, she did."}
					: new String[]{"To request a refund.", "To confirm a reservation.", "To cancel an order.", "To apply for a job."};
			children.add(singleCard(null, stem + " (" + (i + 1) + ")", choices, i % choiceCount, true));
		}
		return children;
	}

	private Card singleCard(String topicId, String text, String[] choices, int correctIndex, boolean child) {
		Question question = new Question();
		question.setText(text);
		question.setHint("");
		question.setImage("");
		question.setSound("");

		Answer answer = new Answer();
		answer.setChoices(Arrays.asList(choices));
		String[] letters = choices.length == 3 ? LETTERS_3 : LETTERS_4;
		answer.setTexts(Collections.singletonList(letters[correctIndex % letters.length]));
		answer.setHint(answer.getTexts().get(0));
		answer.setImage("");

		Card card = new Card();
		card.setExamId(PRACTICE_EXAM_ID);
		card.setTopicId(topicId);
		card.setQuestion(question);
		card.setAnswer(answer);
		card.setIsQuestionGroup(false);
		if (!child) {
			card.setChildCards(new ArrayList<>());
		}
		return card;
	}

	private Card groupCard(String topicId, String passage, List<Card> children) {
		Question question = new Question();
		question.setText(passage);
		question.setHint("");
		question.setImage("");
		question.setSound("");

		Answer answer = new Answer();
		answer.setChoices(new ArrayList<>());
		answer.setTexts(new ArrayList<>());
		answer.setHint("");
		answer.setImage("");

		Card card = new Card();
		card.setExamId(PRACTICE_EXAM_ID);
		card.setTopicId(topicId);
		card.setQuestion(question);
		card.setAnswer(answer);
		card.setIsQuestionGroup(true);
		card.setChildCards(children);
		return card;
	}

	private String requireTopic(Map<String, String> topicIdByName, String name) {
		String id = topicIdByName.get(name);
		if (id == null) {
			throw new IllegalStateException("Missing topic: " + name);
		}
		return id;
	}
}
