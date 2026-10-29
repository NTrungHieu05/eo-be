package exam.service.topic;

import exam.db.dto.topic.TopicResponse;
import exam.db.entity.Exam;
import exam.db.entity.ExamResult;
import exam.db.entity.History;
import exam.db.entity.Topic;
import exam.db.enums.RoleEnum;
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

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TopicServiceImplTest {

	@Mock
	private TopicRepository topicRepo;

	@Mock
	private ExamRepository examRepo;

	@Mock
	private HistoryRepository historyRepo;

	@InjectMocks
	private TopicServiceImpl topicService;

	@AfterEach
	void tearDown() {
		TestUsers.clear();
	}

	@Test
	void topicExamListUsesNewestProgress() throws ExamBaseException {
		TestUsers.authenticate(TestUsers.user("user-1", RoleEnum.ROLE_USER));
		Topic topic = new Topic();
		topic.setId("topic-1");
		topic.setName("Phần 5");
		Exam exam = new Exam();
		exam.setId("exam-1");
		exam.setName("Đề 1");
		exam.setTopicId("topic-1");
		when(topicRepo.findFirstByIdAndDeletedIsFalse("topic-1")).thenReturn(topic);
		when(examRepo.findAllByTopicIdAndDeletedIsFalse("topic-1")).thenReturn(List.of(exam));

		History newer = history("exam-1", 90, new Date(2_000L));
		History older = history("exam-1", 20, new Date(1_000L));
		when(historyRepo.findAllByUserIdAndDeletedIsFalse("user-1")).thenReturn(List.of(newer, older));

		TopicResponse response = topicService.getAllExamByTopicId("topic-1");

		assertThat(response.getExams()).hasSize(1);
		assertThat(response.getExams().get(0).getProgress()).isEqualTo(90);
	}

	@Test
	void topicExamListKeepsTopicExamOrder() throws ExamBaseException {
		TestUsers.authenticate(TestUsers.user("user-1", RoleEnum.ROLE_USER));
		Topic topic = new Topic();
		topic.setId("topic-1");
		topic.setName("Phần 1");
		topic.setExamIds(List.of("exam-1", "exam-2"));
		Exam first = new Exam();
		first.setId("exam-1");
		first.setName("Luyện trước");
		first.setTopicId("topic-1");
		Exam second = new Exam();
		second.setId("exam-2");
		second.setName("Luyện sau");
		second.setTopicId("topic-1");
		when(topicRepo.findFirstByIdAndDeletedIsFalse("topic-1")).thenReturn(topic);
		when(examRepo.findByIdInAndDeletedIsFalse(List.of("exam-1", "exam-2"))).thenReturn(List.of(second, first));
		when(examRepo.findAllByTopicIdAndDeletedIsFalse("topic-1")).thenReturn(List.of(second, first));
		when(historyRepo.findAllByUserIdAndDeletedIsFalse("user-1")).thenReturn(List.of());

		TopicResponse response = topicService.getAllExamByTopicId("topic-1");

		assertThat(response.getExams()).extracting("id").containsExactly("exam-1", "exam-2");
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
