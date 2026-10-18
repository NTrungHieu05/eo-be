package exam.service.history;

import exam.db.dto.history.HistoryRequest;
import exam.db.entity.ExamResult;
import exam.db.entity.History;
import exam.db.enums.ErrorInfo;
import exam.db.enums.HistoryStatusEnum;
import exam.db.enums.RoleEnum;
import exam.db.repository.history.HistoryRepository;
import exam.support.TestUsers;
import exam.ultis.ExamBaseException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HistoryServiceImplTest {

	@Mock
	private HistoryRepository historyRepo;

	@InjectMocks
	private HistoryServiceImpl historyService;

	@AfterEach
	void tearDown() {
		TestUsers.clear();
	}

	@Test
	void createStoresFirstAttemptForCurrentUser() throws ExamBaseException {
		TestUsers.authenticate(TestUsers.user("user-1", RoleEnum.ROLE_USER));
		when(historyRepo.findByUserIdAndDeletedIsFalse("user-1")).thenReturn(null);

		assertThat(historyService.create(request("exam-1", 40, HistoryStatusEnum.PENDING.getId()))).isTrue();

		ArgumentCaptor<History> saved = ArgumentCaptor.forClass(History.class);
		verify(historyRepo).save(saved.capture());
		assertThat(saved.getValue().getUserId()).isEqualTo("user-1");
		assertThat(saved.getValue().getStatus()).isEqualTo(HistoryStatusEnum.PENDING.getId());
		assertThat(saved.getValue().getExamResults()).hasSize(1);
		assertThat(saved.getValue().getExamResults().get(0).getId()).isEqualTo("exam-1");
		assertThat(saved.getValue().getExamResults().get(0).getProgress()).isEqualTo(40);
	}

	@Test
	void createUpdatesExistingExamProgressAndAppendsNewExam() throws ExamBaseException {
		TestUsers.authenticate(TestUsers.user("user-1", RoleEnum.ROLE_USER));
		History history = new History();
		history.setUserId("user-1");
		history.setStatus(HistoryStatusEnum.PENDING.getId());
		ExamResult existing = new ExamResult();
		existing.setId("exam-1");
		existing.setProgress(10);
		history.setExamResults(new ArrayList<>(List.of(existing)));
		when(historyRepo.findByUserIdAndDeletedIsFalse("user-1")).thenReturn(history);

		historyService.create(request("exam-1", 80, null));
		historyService.create(request("exam-2", 15, HistoryStatusEnum.COMPLETE.getId()));

		assertThat(history.getExamResults()).hasSize(2);
		assertThat(history.getExamResults().get(0).getProgress()).isEqualTo(80);
		assertThat(history.getExamResults().get(1).getId()).isEqualTo("exam-2");
		assertThat(history.getStatus()).isEqualTo(HistoryStatusEnum.COMPLETE.getId());
	}

	@Test
	void createRequiresLogin() {
		assertThatThrownBy(() -> historyService.create(request("exam-1", 1, 0)))
				.isInstanceOf(ExamBaseException.class)
				.extracting(ex -> ((ExamBaseException) ex).getError().getCode())
				.isEqualTo(ErrorInfo.ACCESS_DENIED_ERROR_CODE);
	}

	@Test
	void createRejectsBlankExamId() {
		TestUsers.authenticate(TestUsers.user("user-1", RoleEnum.ROLE_USER));

		assertThatThrownBy(() -> historyService.create(request("", 1, 0)))
				.isInstanceOf(ExamBaseException.class)
				.extracting(ex -> ((ExamBaseException) ex).getError().getCode())
				.isEqualTo(ErrorInfo.EXAM_NOT_FOUND_ERROR_CODE);
	}

	private HistoryRequest request(String examId, Integer progress, Integer status) {
		HistoryRequest request = new HistoryRequest();
		request.setExamId(examId);
		request.setProgress(progress);
		request.setStatus(status);
		return request;
	}
}
