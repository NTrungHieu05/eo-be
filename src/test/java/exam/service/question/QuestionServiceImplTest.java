package exam.service.question;

import exam.db.dto.question.CreateQuestionRequest;
import exam.db.entity.Answer;
import exam.db.entity.Question;
import exam.db.entity.Topic;
import exam.db.enums.ErrorInfo;
import exam.db.enums.RoleEnum;
import exam.db.repository.exam.ExamRepository;
import exam.db.repository.question.QuestionRepository;
import exam.db.repository.topic.TopicRepository;
import exam.support.TestUsers;
import exam.ultis.ExamBaseException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuestionServiceImplTest {

	@Mock
	private QuestionRepository questionRepo;

	@Mock
	private ExamRepository examRepo;

	@Mock
	private TopicRepository topicRepo;

	@InjectMocks
	private QuestionServiceImpl questionService;

	@BeforeEach
	void setUp() {
		TestUsers.authenticate(TestUsers.user("admin-1", RoleEnum.ROLE_ADMIN));
		Topic topic = new Topic();
		topic.setId("t1");
		when(topicRepo.findFirstByIdAndDeletedIsFalse("t1")).thenReturn(topic);
	}

	@AfterEach
	void tearDown() {
		TestUsers.clear();
	}

	@Test
	void createRejectsSingleQuestionWithoutCorrectAnswer() {
		CreateQuestionRequest request = request(answer(""), false, null);

		assertThatThrownBy(() -> questionService.create(request))
				.isInstanceOf(ExamBaseException.class)
				.extracting(ex -> ((ExamBaseException) ex).getError().getCode())
				.isEqualTo(ErrorInfo.ANSWER_REQUIRED_CODE);
		verify(questionRepo, never()).save(any());
	}

	@Test
	void createRejectsGroupWhenAChildHasNoCorrectAnswer() {
		CreateQuestionRequest request = request(null, true, List.of(child(answer("A")), child(answer(" "))));

		assertThatThrownBy(() -> questionService.create(request))
				.isInstanceOf(ExamBaseException.class)
				.extracting(ex -> ((ExamBaseException) ex).getError().getCode())
				.isEqualTo(ErrorInfo.ANSWER_REQUIRED_CODE);
	}

	@Test
	void createSavesQuestionWithCorrectAnswer() throws ExamBaseException {
		CreateQuestionRequest request = request(answer("Right"), false, null);

		assertThat(questionService.create(request).getTopicId()).isEqualTo("t1");
		verify(questionRepo).save(any(Question.class));
	}

	private CreateQuestionRequest request(Answer answer, boolean group, List<Question> children) {
		CreateQuestionRequest request = new CreateQuestionRequest();
		request.setTopicId("t1");
		request.setAnswer(answer);
		request.setIsQuestionGroup(group);
		request.setChildQuestions(children == null ? new ArrayList<>() : children);
		return request;
	}

	private Question child(Answer answer) {
		Question question = new Question();
		question.setAnswer(answer);
		return question;
	}

	private Answer answer(String correct) {
		Answer answer = new Answer();
		answer.setChoices(new ArrayList<>(List.of("x", "y")));
		answer.setTexts(new ArrayList<>(List.of(correct)));
		return answer;
	}
}
