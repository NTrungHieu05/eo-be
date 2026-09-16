package exam.service.exam;

import exam.db.dto.exam.CreateExamRequest;
import exam.db.dto.exam.DeleteExamRequest;
import exam.db.dto.exam.ExamResponse;
import exam.db.dto.exam.ListExamRequest;
import exam.db.dto.exam.ListExamResponse;
import exam.db.dto.exam.UpdateExamRequest;
import exam.db.dto.user.ExamUser;
import exam.db.entity.Card;
import exam.db.entity.Exam;
import exam.db.entity.Topic;
import exam.db.enums.Assert;
import exam.db.enums.ErrorInfo;
import exam.db.repository.card.CardRepository;
import exam.db.repository.exam.ExamRepository;
import exam.db.repository.topic.TopicRepository;
import exam.ultis.ExamBaseException;
import exam.ultis.SecurityContextService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExamServiceImpl implements ExamService {

	@Autowired
	private ExamRepository examRepo;

	@Autowired
	private CardRepository cardRepo;

	@Autowired
	private TopicRepository topicRepo;

	@Override
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public ExamResponse create(CreateExamRequest request) throws ExamBaseException {
		requireUser();
		Exam exam = new Exam();
		BeanUtils.copyProperties(request, exam);
		examRepo.save(exam);
		if (StringUtils.hasText(request.getTopicId())) {
			Topic topic = topicRepo.findFirstByIdAndDeletedIsFalse(request.getTopicId());
			if (topic != null) {
				List<String> examIds = topic.getExamIds() == null ? new ArrayList<>() : new ArrayList<>(topic.getExamIds());
				examIds.add(exam.getId());
				topic.setExamIds(examIds);
				topicRepo.save(topic);
			}
		}
		return new ExamResponse(exam);
	}

	@Override
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public ExamResponse update(UpdateExamRequest request) throws ExamBaseException {
		requireUser();
		Exam exam = examRepo.findFirstByIdAndDeletedIsFalse(request.getId());
		Assert.notNull(exam, ErrorInfo.EXAM_NOT_FOUND_ERROR);
		exam.setName(request.getName());
		exam.setCardIds(request.getCardIds());
		if (StringUtils.hasText(request.getTopicId())) {
			exam.setTopicId(request.getTopicId());
		}
		examRepo.save(exam);
		return new ExamResponse(exam);
	}

	@Override
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public Boolean deleteMultiple(DeleteExamRequest request) throws ExamBaseException {
		requireUser();
		List<Exam> examList = examRepo.findByIdInAndDeletedIsFalse(request.getIds());
		examList.forEach(p -> p.setDeleted(true));
		examRepo.saveAll(examList);
		return Boolean.TRUE;
	}

	@Override
	public ExamResponse getDetail(String id) throws ExamBaseException {
		requireUser();
		Exam exam = examRepo.findFirstByIdAndDeletedIsFalse(id);
		Assert.notNull(exam, ErrorInfo.EXAM_NOT_FOUND_ERROR);
		return new ExamResponse(exam);
	}

	@Override
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public ListExamResponse getListExamForAdmin(ListExamRequest request) throws ExamBaseException {
		requireUser();
		List<ExamResponse> exams = examRepo.findAllByDeletedIsFalse().stream()
				.map(ExamResponse::new)
				.collect(Collectors.toList());
		return new ListExamResponse(exams, exams.size());
	}

	@Override
	public ExamResponse getAllCardsByExamId(String examId) throws ExamBaseException {
		Exam exam = examRepo.findFirstByIdAndDeletedIsFalse(examId);
		Assert.notNull(exam, ErrorInfo.EXAM_NOT_FOUND_ERROR);
		List<String> cardIds = exam.getCardIds() == null ? Collections.emptyList() : exam.getCardIds();
		List<Card> cards = cardIds.isEmpty()
				? new ArrayList<>()
				: cardRepo.findByIdInAndDeletedIsFalse(cardIds);
		ExamResponse response = new ExamResponse(exam);
		response.setCards(cards);
		return response;
	}

	private void requireUser() throws ExamBaseException {
		ExamUser examUser = SecurityContextService.getUser();
		Assert.notNull(examUser, ErrorInfo.ACCESS_DENIED_ERROR);
	}
}
