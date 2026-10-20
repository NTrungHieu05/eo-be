package exam.service.exam;

import exam.db.dto.exam.AddCardToExamRequest;
import exam.db.dto.exam.CreateExamRequest;
import exam.db.dto.exam.DeleteExamRequest;
import exam.db.dto.exam.ExamResponse;
import exam.db.dto.exam.PracticeTestResponse;
import exam.db.dto.exam.ToeicScoreRequest;
import exam.db.dto.exam.ToeicScoreResponse;
import exam.db.entity.Card;
import exam.db.entity.Exam;
import exam.db.entity.Topic;
import exam.db.enums.ErrorInfo;
import exam.db.enums.RoleEnum;
import exam.db.repository.card.CardRepository;
import exam.db.repository.exam.ExamRepository;
import exam.db.repository.topic.TopicRepository;
import exam.support.TestUsers;
import exam.ultis.ExamBaseException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExamServiceImplTest {

	@Mock
	private ExamRepository examRepo;

	@Mock
	private CardRepository cardRepo;

	@Mock
	private TopicRepository topicRepo;

	@InjectMocks
	private ExamServiceImpl examService;

	@AfterEach
	void tearDown() {
		TestUsers.clear();
	}

	@Test
	void practiceTestUsesToeicTimingAndMergesReadingIntoPart7() throws ExamBaseException {
		TestUsers.authenticate(TestUsers.user("user-1", RoleEnum.ROLE_USER));
		List<Card> cards = Arrays.asList(
				single("c1", "t1"),
				group("c2", "t2", 3),
				single("c3", "t3"),
				single("c4", "t4"),
				single("c5", "t5"),
				single("c6", "t6"),
				single("c7", "t7"),
				group("c8", "t8", 2));
		Exam exam = practiceExam(idsOf(cards));
		when(examRepo.findFirstByPracticeTestIsTrueAndDeletedIsFalse()).thenReturn(exam);
		when(cardRepo.findByIdInAndDeletedIsFalse(exam.getCardIds())).thenReturn(new ArrayList<>(reversed(cards)));

		PracticeTestResponse response = examService.getPracticeTest();

		assertThat(response.getDurationMinutes()).isEqualTo(120);
		assertThat(response.getListeningMinutes()).isEqualTo(45);
		assertThat(response.getReadingMinutes()).isEqualTo(75);
		assertThat(response.getListeningQuestionCount()).isEqualTo(100);
		assertThat(response.getReadingQuestionCount()).isEqualTo(100);
		assertThat(response.getQuestionCount()).isEqualTo(11);
		assertThat(response.getCards()).extracting("id")
				.containsExactly("c1", "c2", "c3", "c4", "c5", "c6", "c7", "c8");
		assertThat(response.getParts()).hasSize(7);
		assertThat(response.getParts().get(0).getName()).isEqualTo("Photographs");
		assertThat(response.getParts().get(0).getQuestionCount()).isEqualTo(1);
		assertThat(response.getParts().get(1).getName()).isEqualTo("Question-Response");
		assertThat(response.getParts().get(1).getQuestionCount()).isEqualTo(3);
		assertThat(response.getParts().get(5).getName()).isEqualTo("Text Completion");
		assertThat(response.getParts().get(6).getPartNumber()).isEqualTo(7);
		assertThat(response.getParts().get(6).getName()).isEqualTo("Reading Comprehension");
		assertThat(response.getParts().get(6).getTopicId()).isEqualTo("t7");
		assertThat(response.getParts().get(6).getQuestionCount()).isEqualTo(3);
		assertThat(response.getParts().get(6).getCardCount()).isEqualTo(2);
	}

	@Test
	void scoreUsesThePracticeChartForTheSignedInUser() throws ExamBaseException {
		TestUsers.authenticate(TestUsers.user("user-1", RoleEnum.ROLE_USER));
		ToeicScoreRequest request = new ToeicScoreRequest();
		request.setListeningCorrect(70);
		request.setReadingCorrect(60);

		ToeicScoreResponse response = examService.score(request);

		assertThat(response.getListeningScore()).isEqualTo(355);
		assertThat(response.getReadingScore()).isEqualTo(275);
		assertThat(response.getTotalScore()).isEqualTo(630);
	}

	@Test
	void practiceTestRequiresLogin() {
		assertThatThrownBy(() -> examService.getPracticeTest())
				.isInstanceOf(ExamBaseException.class)
				.extracting(ex -> ((ExamBaseException) ex).getError().getCode())
				.isEqualTo(ErrorInfo.ACCESS_DENIED_ERROR_CODE);
	}

	@Test
	void practiceTestMissingSeedThrowsNotFound() {
		TestUsers.authenticate(TestUsers.user("user-1", RoleEnum.ROLE_USER));
		when(examRepo.findFirstByPracticeTestIsTrueAndDeletedIsFalse()).thenReturn(null);

		assertThatThrownBy(() -> examService.getPracticeTest())
				.isInstanceOf(ExamBaseException.class)
				.extracting(ex -> ((ExamBaseException) ex).getError().getCode())
				.isEqualTo(ErrorInfo.EXAM_NOT_FOUND_ERROR_CODE);
	}

	@Test
	void getAllCardsKeepsExamCardOrder() throws ExamBaseException {
		Exam exam = new Exam();
		exam.setId("exam-1");
		exam.setCardIds(List.of("b", "a"));
		Card first = single("a", "t1");
		Card second = single("b", "t1");
		when(examRepo.findFirstByIdAndDeletedIsFalse("exam-1")).thenReturn(exam);
		when(cardRepo.findByIdInAndDeletedIsFalse(exam.getCardIds())).thenReturn(List.of(first, second));

		ExamResponse response = examService.getAllCardsByExamId("exam-1");

		assertThat(response.getCards()).extracting(Card::getId).containsExactly("b", "a");
	}

	@Test
	void createLinksSavedExamOntoTopic() throws ExamBaseException {
		TestUsers.authenticate(TestUsers.user("admin-1", RoleEnum.ROLE_ADMIN));
		Topic topic = new Topic("Part 5");
		topic.setId("topic-5");
		topic.setExamIds(new ArrayList<>());
		when(examRepo.save(any(Exam.class))).thenAnswer(invocation -> {
			Exam saved = invocation.getArgument(0);
			saved.setId("exam-9");
			return saved;
		});
		when(topicRepo.findFirstByIdAndDeletedIsFalse("topic-5")).thenReturn(topic);

		CreateExamRequest request = new CreateExamRequest();
		request.setName("Incomplete Sentences 1");
		request.setTopicId("topic-5");
		ExamResponse response = examService.create(request);

		assertThat(response.getId()).isEqualTo("exam-9");
		assertThat(topic.getExamIds()).containsExactly("exam-9");
		verify(topicRepo).save(topic);
	}

	@Test
	void deleteMultipleMarksExamsDeleted() throws ExamBaseException {
		TestUsers.authenticate(TestUsers.user("admin-1", RoleEnum.ROLE_ADMIN));
		Exam exam = new Exam();
		exam.setId("exam-1");
		when(examRepo.findByIdInAndDeletedIsFalse(List.of("exam-1"))).thenReturn(List.of(exam));
		DeleteExamRequest request = new DeleteExamRequest();
		request.setIds(List.of("exam-1"));

		assertThat(examService.deleteMultiple(request)).isTrue();
		assertThat(exam.isDeleted()).isTrue();
		verify(examRepo).saveAll(List.of(exam));
	}

	@Test
	void addCardAppendsExistingCardsUsingTheSameIdsField() throws ExamBaseException {
		TestUsers.authenticate(TestUsers.user("admin-1", RoleEnum.ROLE_ADMIN));
		Exam exam = new Exam();
		exam.setId("exam-1");
		exam.setCardIds(new ArrayList<>(List.of("a")));
		Card card = single("b", "t1");
		when(examRepo.findFirstByIdAndDeletedIsFalse("exam-1")).thenReturn(exam);
		when(cardRepo.findFirstByIdAndDeletedIsFalse("b")).thenReturn(card);
		when(examRepo.save(exam)).thenReturn(exam);

		AddCardToExamRequest request = new AddCardToExamRequest();
		request.setId("exam-1");
		request.setCardIds(List.of("b", "missing"));

		ExamResponse response = examService.addCard(request);

		assertThat(response.getCardIds()).containsExactly("a", "b");
		assertThat(card.getExamId()).isEqualTo("exam-1");
	}

	private Exam practiceExam(List<String> cardIds) {
		Exam exam = new Exam();
		exam.setId("68cb00010000000000000001");
		exam.setName("Thi thử TOEIC");
		exam.setPracticeTest(true);
		exam.setCardIds(cardIds);
		return exam;
	}

	private Card single(String id, String topicId) {
		Card card = new Card();
		card.setId(id);
		card.setTopicId(topicId);
		card.setIsQuestionGroup(false);
		return card;
	}

	private Card group(String id, String topicId, int childCount) {
		Card card = single(id, topicId);
		card.setIsQuestionGroup(true);
		List<Card> children = new ArrayList<>();
		for (int i = 0; i < childCount; i++) {
			children.add(single(id + "-" + i, topicId));
		}
		card.setChildCards(children);
		return card;
	}

	private List<String> idsOf(List<Card> cards) {
		List<String> ids = new ArrayList<>();
		for (Card card : cards) {
			ids.add(card.getId());
		}
		return ids;
	}

	private List<Card> reversed(List<Card> cards) {
		List<Card> copy = new ArrayList<>(cards);
		java.util.Collections.reverse(copy);
		return copy;
	}
}
