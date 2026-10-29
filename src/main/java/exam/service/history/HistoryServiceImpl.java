package exam.service.history;

import exam.db.dto.history.HistoryRequest;
import exam.db.dto.user.ExamUser;
import exam.db.entity.ExamResult;
import exam.db.entity.History;
import exam.db.enums.Assert;
import exam.db.enums.ErrorInfo;
import exam.db.repository.history.HistoryRepository;
import exam.ultis.ExamBaseException;
import exam.ultis.SecurityContextService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class HistoryServiceImpl implements HistoryService {

	@Autowired
	private HistoryRepository historyRepo;

	@Override
	public Boolean create(HistoryRequest request) throws ExamBaseException {
		ExamUser examUser = SecurityContextService.getUser();
		Assert.notNull(examUser, ErrorInfo.ACCESS_DENIED_ERROR);
		Assert.notNull(request, ErrorInfo.EXAM_NOT_FOUND_ERROR);
		Assert.notEmpty(request.getExamId(), ErrorInfo.EXAM_NOT_FOUND_ERROR);

		List<History> histories = historyRepo.findAllByUserIdAndDeletedIsFalse(examUser.getUserId());
		if (histories == null || histories.isEmpty()) {
			History created = new History();
			created.setUserId(examUser.getUserId());
			created.setExamResults(new ArrayList<>());
			applyResult(created, request);
			historyRepo.save(created);
			return Boolean.TRUE;
		}

		List<History> ordered = new ArrayList<>(histories);
		ordered.sort(Comparator.comparing(History::getLastModifiedDate, Comparator.nullsFirst(Date::compareTo)).reversed());
		History primary = ordered.get(0);
		for (int i = 1; i < ordered.size(); i++) {
			History extra = ordered.get(i);
			if (extra == null) {
				continue;
			}
			mergeResults(primary, extra);
			extra.setDeleted(true);
			historyRepo.save(extra);
		}
		applyResult(primary, request);
		historyRepo.save(primary);
		return Boolean.TRUE;
	}

	private void mergeResults(History primary, History extra) {
		if (extra.getExamResults() == null) {
			return;
		}
		List<ExamResult> results = primary.getExamResults() == null
				? new ArrayList<>()
				: primary.getExamResults();
		for (ExamResult incoming : extra.getExamResults()) {
			if (incoming == null || incoming.getId() == null) {
				continue;
			}
			ExamResult current = null;
			for (ExamResult existing : results) {
				if (existing != null && incoming.getId().equals(existing.getId())) {
					current = existing;
					break;
				}
			}
			if (current == null) {
				ExamResult copy = new ExamResult();
				copy.setId(incoming.getId());
				copy.setProgress(incoming.getProgress());
				results.add(copy);
			} else if (current.getProgress() == null && incoming.getProgress() != null) {
				current.setProgress(incoming.getProgress());
			}
		}
		primary.setExamResults(results);
	}

	private void applyResult(History history, HistoryRequest request) {
		List<ExamResult> examResults = history.getExamResults() == null
				? new ArrayList<>()
				: history.getExamResults();
		Map<String, ExamResult> lastById = new LinkedHashMap<>();
		for (ExamResult examResult : examResults) {
			if (examResult == null || examResult.getId() == null) {
				continue;
			}
			lastById.put(examResult.getId(), examResult);
		}
		ExamResult current = lastById.get(request.getExamId());
		if (current == null) {
			ExamResult examResult = new ExamResult();
			examResult.setId(request.getExamId());
			examResult.setProgress(request.getProgress());
			lastById.put(request.getExamId(), examResult);
		} else if (request.getProgress() != null) {
			current.setProgress(request.getProgress());
		}
		history.setExamResults(new ArrayList<>(lastById.values()));
		if (request.getStatus() != null) {
			history.setStatus(request.getStatus());
		}
	}
}
