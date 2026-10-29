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
import java.util.Collections;
import java.util.Date;
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
		when(historyRepo.findAllByUserIdAndDeletedIsFalse("user-1")).thenReturn(Collections.emptyList());

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
		when(historyRepo.findAllByUserIdAndDeletedIsFalse("user-1")).thenReturn(new ArrayList<>(List.of(history)));

		historyService.create(request("exam-1", 80, null));
		historyService.create(request("exam-2", 15, HistoryStatusEnum.COMPLETE.getId()));

		assertThat(history.getExamResults()).hasSize(2);
		assertThat(history.getExamResults().get(0).getProgress()).isEqualTo(80);
		assertThat(history.getExamResults().get(1).getId()).isEqualTo("exam-2");
		assertThat(history.getStatus()).isEqualTo(HistoryStatusEnum.COMPLETE.getId());
	}

	@Test
	void createKeepsNewestResultWhenDuplicateHistoriesExist() throws ExamBaseException {
		TestUsers.authenticate(TestUsers.user("user-1", RoleEnum.ROLE_USER));
		History primary = new History();
		primary.setUserId("user-1");
		primary.setExamResults(new ArrayList<>());
		History extra = new History();
		extra.setUserId("user-1");
		extra.setExamResults(new ArrayList<>());
		when(historyRepo.findAllByUserIdAndDeletedIsFalse("user-1"))
				.thenReturn(new ArrayList<>(List.of(primary, extra)));

		assertThat(historyService.create(request("exam-1", 70, HistoryStatusEnum.COMPLETE.getId()))).isTrue();

		assertThat(primary.getExamResults()).extracting(ExamResult::getProgress).containsExactly(70);
		assertThat(extra.isDeleted()).isTrue();
	}

	@Test
	void createKeepsScoresFromOlderHistoryWhenCollapsingDuplicates() throws ExamBaseException {
		TestUsers.authenticate(TestUsers.user("user-1", RoleEnum.ROLE_USER));
		History newest = new History();
		newest.setUserId("user-1");
		newest.setLastModifiedDate(new Date(2_000L));
		newest.setExamResults(new ArrayList<>());
		History older = new History();
		older.setUserId("user-1");
		older.setLastModifiedDate(new Date(1_000L));
		ExamResult otherExam = new ExamResult();
		otherExam.setId("exam-2");
		otherExam.setProgress(40);
		older.setExamResults(new ArrayList<>(List.of(otherExam)));
		when(historyRepo.findAllByUserIdAndDeletedIsFalse("user-1"))
				.thenReturn(new ArrayList<>(List.of(older, newest)));

		assertThat(historyService.create(request("exam-1", 70, HistoryStatusEnum.COMPLETE.getId()))).isTrue();

		assertThat(newest.getExamResults()).extracting(ExamResult::getId).containsExactly("exam-2", "exam-1");
		assertThat(newest.getExamResults()).extracting(ExamResult::getProgress).containsExactly(40, 70);
		assertThat(older.isDeleted()).isTrue();
	}

	@Test
	void createKeepsExistingProgressWhenNewValueIsMissing() throws ExamBaseException {
		TestUsers.authenticate(TestUsers.user("user-1", RoleEnum.ROLE_USER));
		History history = new History();
		history.setUserId("user-1");
		ExamResult existing = new ExamResult();
		existing.setId("exam-1");
		existing.setProgress(10);
		history.setExamResults(new ArrayList<>(List.of(existing)));
		when(historyRepo.findAllByUserIdAndDeletedIsFalse("user-1")).thenReturn(new ArrayList<>(List.of(history)));

		assertThat(historyService.create(request("exam-1", null, null))).isTrue();

		assertThat(history.getExamResults().get(0).getProgress()).isEqualTo(10);
	}

	@Test
	void createReplacesDuplicateExamRowsWithTheNewScore() throws ExamBaseException {
		TestUsers.authenticate(TestUsers.user("user-1", RoleEnum.ROLE_USER));
		History history = new History();
		history.setUserId("user-1");
		ExamResult older = new ExamResult();
		older.setId("exam-1");
		older.setProgress(10);
		ExamResult newer = new ExamResult();
		newer.setId("exam-1");
		newer.setProgress(80);
		history.setExamResults(new ArrayList<>(List.of(older, newer)));
		when(historyRepo.findAllByUserIdAndDeletedIsFalse("user-1")).thenReturn(new ArrayList<>(List.of(history)));

		assertThat(historyService.create(request("exam-1", 55, HistoryStatusEnum.COMPLETE.getId()))).isTrue();

		assertThat(history.getExamResults()).hasSize(1);
		assertThat(history.getExamResults().get(0).getProgress()).isEqualTo(55);
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
