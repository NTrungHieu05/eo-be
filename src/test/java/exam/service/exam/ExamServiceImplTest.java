package exam.service.exam;

import exam.db.dto.exam.AddQuestionToExamRequest;
import exam.db.dto.exam.CreateExamRequest;
import exam.db.dto.exam.DeleteExamRequest;
import exam.db.dto.exam.ExamResponse;
import exam.db.dto.exam.ListPracticeTestResponse;
import exam.db.dto.exam.PracticeTestResponse;
import exam.db.dto.exam.UpdateExamRequest;
import exam.db.dto.exam.ToeicScoreRequest;
import exam.db.dto.exam.ToeicScoreResponse;
import exam.db.entity.Question;
import exam.db.entity.Exam;
import exam.db.entity.ExamResult;
import exam.db.entity.History;
import exam.db.entity.Topic;
import exam.db.enums.ErrorInfo;
import exam.db.enums.RoleEnum;
import exam.db.repository.question.QuestionRepository;
import exam.db.repository.exam.ExamRepository;
import exam.db.repository.history.HistoryRepository;
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
import java.util.Date;
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
	private QuestionRepository questionRepo;

	@Mock
	private TopicRepository topicRepo;

	@Mock
	private HistoryRepository historyRepo;

	@InjectMocks
	private ExamServiceImpl examService;

	@AfterEach
	void tearDown() {
		TestUsers.clear();
	}

	@Test
	void practiceTestUsesToeicTimingAndMergesReadingIntoPart7() throws ExamBaseException {
		TestUsers.authenticate(TestUsers.user("user-1", RoleEnum.ROLE_USER));
		List<Question> questions = Arrays.asList(
				single("c1", "t1"),
				group("c2", "t2", 3),
				single("c3", "t3"),
				single("c4", "t4"),
				single("c5", "t5"),
				single("c6", "t6"),
				single("c7", "t7"),
				group("c8", "t8", 2));
		Exam exam = practiceExam(idsOf(questions));
		when(examRepo.findFirstByIdAndDeletedIsFalse(exam.getId())).thenReturn(exam);
		when(questionRepo.findByIdInAndDeletedIsFalse(exam.getQuestionIds())).thenReturn(new ArrayList<>(reversed(questions)));

		PracticeTestResponse response = examService.getPracticeTest(exam.getId());

		assertThat(response.getDurationMinutes()).isEqualTo(120);
		assertThat(response.getListeningMinutes()).isEqualTo(45);
		assertThat(response.getReadingMinutes()).isEqualTo(75);
		assertThat(response.getListeningQuestionCount()).isEqualTo(100);
		assertThat(response.getReadingQuestionCount()).isEqualTo(100);
		assertThat(response.getQuestionCount()).isEqualTo(11);
		assertThat(response.getQuestions()).extracting("id")
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
		assertThat(response.getParts().get(6).getQuestionRecordCount()).isEqualTo(2);
	}

	@Test
	void practiceTestOrdersNamedPartsEvenWhenQuestionsWerePickedOutOfOrder() throws ExamBaseException {
		TestUsers.authenticate(TestUsers.user("user-1", RoleEnum.ROLE_USER));
		Topic reading = new Topic("Phần 5:Hoàn Thành Câu");
		reading.setId("t5");
		Topic listening = new Topic("Phần 1:Mô Tả Tranh");
		listening.setId("t1");
		Question readingQuestion = single("c5", "t5");
		Question listeningQuestion = single("c1", "t1");
		Exam exam = practiceExam(List.of("c5", "c1"));
		when(examRepo.findFirstByIdAndDeletedIsFalse(exam.getId())).thenReturn(exam);
		when(questionRepo.findByIdInAndDeletedIsFalse(exam.getQuestionIds())).thenReturn(List.of(readingQuestion, listeningQuestion));
		when(topicRepo.findFirstByIdAndDeletedIsFalse("t5")).thenReturn(reading);
		when(topicRepo.findFirstByIdAndDeletedIsFalse("t1")).thenReturn(listening);

		PracticeTestResponse response = examService.getPracticeTest(exam.getId());

		assertThat(response.getQuestions()).extracting("id").containsExactly("c1", "c5");
		assertThat(response.getQuestions()).extracting("partNumber").containsExactly(1, 5);
		assertThat(response.getParts()).extracting("partNumber").containsExactly(1, 5);
		assertThat(response.getParts()).extracting("name")
				.containsExactly("Photographs", "Incomplete Sentences");
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
		assertThatThrownBy(() -> examService.listPracticeTests())
				.isInstanceOf(ExamBaseException.class)
				.extracting(ex -> ((ExamBaseException) ex).getError().getCode())
				.isEqualTo(ErrorInfo.ACCESS_DENIED_ERROR_CODE);
		assertThatThrownBy(() -> examService.getPracticeTest("exam-1"))
				.isInstanceOf(ExamBaseException.class)
				.extracting(ex -> ((ExamBaseException) ex).getError().getCode())
				.isEqualTo(ErrorInfo.ACCESS_DENIED_ERROR_CODE);
	}

	@Test
	void practiceTestMissingSeedThrowsNotFound() {
		TestUsers.authenticate(TestUsers.user("user-1", RoleEnum.ROLE_USER));
		when(examRepo.findFirstByIdAndDeletedIsFalse("missing")).thenReturn(null);

		assertThatThrownBy(() -> examService.getPracticeTest("missing"))
				.isInstanceOf(ExamBaseException.class)
				.extracting(ex -> ((ExamBaseException) ex).getError().getCode())
				.isEqualTo(ErrorInfo.EXAM_NOT_FOUND_ERROR_CODE);
	}

	@Test
	void listPracticeTestsReturnsAllPracticeExams() throws ExamBaseException {
		TestUsers.authenticate(TestUsers.user("user-1", RoleEnum.ROLE_USER));
		Exam first = practiceExam(List.of("c1"));
		Exam second = practiceExam(List.of("c2"));
		second.setId("exam-2");
		second.setName("Đề thi thử TOEIC 2");
		when(examRepo.findAllByPracticeTestIsTrueAndDeletedIsFalse()).thenReturn(List.of(first, second));
		when(questionRepo.findByIdInAndDeletedIsFalse(first.getQuestionIds())).thenReturn(List.of(single("c1", "t1")));
		when(questionRepo.findByIdInAndDeletedIsFalse(second.getQuestionIds())).thenReturn(List.of(single("c2", "t1")));

		ListPracticeTestResponse response = examService.listPracticeTests();

		assertThat(response.getExams()).extracting(PracticeTestResponse::getName)
				.containsExactly("Thi thử TOEIC", "Đề thi thử TOEIC 2");
		assertThat(response.getExams()).allMatch(item -> item.getQuestions() == null || item.getQuestions().isEmpty());
		assertThat(response.getTotal()).isEqualTo(2);
	}

	@Test
	void listPracticeTestsUsesNewestScore() throws ExamBaseException {
		TestUsers.authenticate(TestUsers.user("user-1", RoleEnum.ROLE_USER));
		Exam exam = practiceExam(List.of("c1"));
		when(examRepo.findAllByPracticeTestIsTrueAndDeletedIsFalse()).thenReturn(List.of(exam));
		when(questionRepo.findByIdInAndDeletedIsFalse(exam.getQuestionIds())).thenReturn(List.of(single("c1", "t1")));
		History newer = history(exam.getId(), 630, new Date(2_000L));
		History older = history(exam.getId(), 100, new Date(1_000L));
		when(historyRepo.findAllByUserIdAndDeletedIsFalse("user-1")).thenReturn(List.of(newer, older));

		ListPracticeTestResponse response = examService.listPracticeTests();

		assertThat(response.getExams()).hasSize(1);
		assertThat(response.getExams().get(0).getProgress()).isEqualTo(630);
		assertThat(response.getExams().get(0).getScaledScore()).isFalse();
	}

	@Test
	void getPracticeTestRejectsRegularExam() {
		TestUsers.authenticate(TestUsers.user("user-1", RoleEnum.ROLE_USER));
		Exam exam = new Exam();
		exam.setId("exam-1");
		exam.setPracticeTest(false);
		when(examRepo.findFirstByIdAndDeletedIsFalse("exam-1")).thenReturn(exam);

		assertThatThrownBy(() -> examService.getPracticeTest("exam-1"))
				.isInstanceOf(ExamBaseException.class)
				.extracting(ex -> ((ExamBaseException) ex).getError().getCode())
				.isEqualTo(ErrorInfo.EXAM_NOT_FOUND_ERROR_CODE);
	}

	@Test
	void getAllQuestionsKeepsExamQuestionOrder() throws ExamBaseException {
		TestUsers.authenticate(TestUsers.user("user-1", RoleEnum.ROLE_USER));
		Exam exam = new Exam();
		exam.setId("exam-1");
		exam.setQuestionIds(List.of("b", "a"));
		Question first = single("a", "t1");
		Question second = single("b", "t1");
		when(examRepo.findFirstByIdAndDeletedIsFalse("exam-1")).thenReturn(exam);
		when(questionRepo.findByIdInAndDeletedIsFalse(exam.getQuestionIds())).thenReturn(List.of(first, second));

		ExamResponse response = examService.getAllQuestionsByExamId("exam-1");

		assertThat(response.getQuestions()).extracting(Question::getId).containsExactly("b", "a");
	}

	@Test
	void getAllQuestionsRejectsPracticeTest() {
		TestUsers.authenticate(TestUsers.user("user-1", RoleEnum.ROLE_USER));
		Exam exam = practiceExam(List.of("c1"));
		when(examRepo.findFirstByIdAndDeletedIsFalse(exam.getId())).thenReturn(exam);

		assertThatThrownBy(() -> examService.getAllQuestionsByExamId(exam.getId()))
				.isInstanceOf(ExamBaseException.class)
				.extracting(ex -> ((ExamBaseException) ex).getError().getCode())
				.isEqualTo(ErrorInfo.EXAM_NOT_FOUND_ERROR_CODE);
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
	void createKeepsOnlyQuestionsOfSelectedTopic() throws ExamBaseException {
		TestUsers.authenticate(TestUsers.user("admin-1", RoleEnum.ROLE_ADMIN));
		Topic topic = new Topic("Part 5");
		topic.setId("topic-5");
		topic.setExamIds(new ArrayList<>());
		when(topicRepo.findFirstByIdAndDeletedIsFalse("topic-5")).thenReturn(topic);
		when(questionRepo.findByIdInAndDeletedIsFalse(List.of("keep", "drop")))
				.thenReturn(List.of(single("keep", "topic-5"), single("drop", "topic-other")));
		when(examRepo.save(any(Exam.class))).thenAnswer(invocation -> {
			Exam saved = invocation.getArgument(0);
			saved.setId("exam-10");
			return saved;
		});

		CreateExamRequest request = new CreateExamRequest();
		request.setName("Part 5 đề 1");
		request.setTopicId("topic-5");
		request.setQuestionIds(List.of("keep", "drop"));
		ExamResponse response = examService.create(request);

		assertThat(response.getQuestionIds()).containsExactly("keep");
		assertThat(response.getTopicId()).isEqualTo("topic-5");
		assertThat(response.getPracticeTest()).isNotEqualTo(Boolean.TRUE);
	}

	@Test
	void createPracticeTestDoesNotBelongToATopic() throws ExamBaseException {
		TestUsers.authenticate(TestUsers.user("admin-1", RoleEnum.ROLE_ADMIN));
		when(examRepo.save(any(Exam.class))).thenAnswer(invocation -> {
			Exam saved = invocation.getArgument(0);
			saved.setId("exam-pt");
			return saved;
		});

		CreateExamRequest request = new CreateExamRequest();
		request.setName("Đề thi thử TOEIC 4");
		request.setPracticeTest(true);
		request.setTopicId("topic-5");
		ExamResponse response = examService.create(request);

		assertThat(response.getId()).isEqualTo("exam-pt");
		assertThat(response.getTopicId()).isNull();
		assertThat(response.getPracticeTest()).isTrue();
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
	void updateKeepsQuestionsWhenQuestionIdsOmitted() throws ExamBaseException {
		TestUsers.authenticate(TestUsers.user("admin-1", RoleEnum.ROLE_ADMIN));
		Exam exam = new Exam();
		exam.setId("exam-1");
		exam.setTopicId("topic-5");
		exam.setQuestionIds(new ArrayList<>(List.of("keep")));
		Topic topic = new Topic("Part 5");
		topic.setId("topic-5");
		when(examRepo.findFirstByIdAndDeletedIsFalse("exam-1")).thenReturn(exam);
		when(topicRepo.findFirstByIdAndDeletedIsFalse("topic-5")).thenReturn(topic);
		when(examRepo.save(exam)).thenReturn(exam);

		UpdateExamRequest request = new UpdateExamRequest();
		request.setId("exam-1");
		request.setName("Renamed");
		request.setTopicId("topic-5");
		request.setPracticeTest(false);

		ExamResponse response = examService.update(request);

		assertThat(response.getName()).isEqualTo("Renamed");
		assertThat(response.getQuestionIds()).containsExactly("keep");
	}

	@Test
	void addQuestionAppendsExistingQuestionsUsingTheSameIdsField() throws ExamBaseException {
		TestUsers.authenticate(TestUsers.user("admin-1", RoleEnum.ROLE_ADMIN));
		Exam exam = new Exam();
		exam.setId("exam-1");
		exam.setQuestionIds(new ArrayList<>(List.of("a")));
		Question question = single("b", "t1");
		when(examRepo.findFirstByIdAndDeletedIsFalse("exam-1")).thenReturn(exam);
		when(questionRepo.findFirstByIdAndDeletedIsFalse("b")).thenReturn(question);
		when(examRepo.save(exam)).thenReturn(exam);

		AddQuestionToExamRequest request = new AddQuestionToExamRequest();
		request.setId("exam-1");
		request.setQuestionIds(List.of("b", "missing"));

		ExamResponse response = examService.addQuestion(request);

		assertThat(response.getQuestionIds()).containsExactly("a", "b");
		assertThat(question.getExamId()).isNull();
	}

	private Exam practiceExam(List<String> questionIds) {
		Exam exam = new Exam();
		exam.setId("68cb00010000000000000001");
		exam.setName("Thi thử TOEIC");
		exam.setPracticeTest(true);
		exam.setQuestionIds(questionIds);
		return exam;
	}

	private Question single(String id, String topicId) {
		Question question = new Question();
		question.setId(id);
		question.setTopicId(topicId);
		question.setIsQuestionGroup(false);
		return question;
	}

	private Question group(String id, String topicId, int childCount) {
		Question question = single(id, topicId);
		question.setIsQuestionGroup(true);
		List<Question> children = new ArrayList<>();
		for (int i = 0; i < childCount; i++) {
			children.add(single(id + "-" + i, topicId));
		}
		question.setChildQuestions(children);
		return question;
	}

	private List<String> idsOf(List<Question> questions) {
		List<String> ids = new ArrayList<>();
		for (Question question : questions) {
			ids.add(question.getId());
		}
		return ids;
	}

	private List<Question> reversed(List<Question> questions) {
		List<Question> copy = new ArrayList<>(questions);
		java.util.Collections.reverse(copy);
		return copy;
	}

	private History history(String examId, int progress, Date modified) {
		ExamResult result = new ExamResult();
		result.setId(examId);
		result.setProgress(progress);
		History history = new History();
		history.setUserId("user-1");
		history.setLastModifiedDate(modified);
		history.setExamResults(List.of(result));
		return history;
	}
}
