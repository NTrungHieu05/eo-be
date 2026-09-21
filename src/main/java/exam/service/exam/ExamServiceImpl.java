package exam.service.exam;

import exam.db.dto.exam.CreateExamRequest;
import exam.db.dto.exam.DeleteExamRequest;
import exam.db.dto.exam.ExamResponse;
import exam.db.dto.exam.ListExamRequest;
import exam.db.dto.exam.ListExamResponse;
import exam.db.dto.exam.PracticeTestPartResponse;
import exam.db.dto.exam.PracticeTestResponse;
import exam.db.dto.exam.UpdateExamRequest;
import exam.db.dto.card.CardResponse;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
		response.setCards(orderCards(exam.getCardIds(), cards));
		return response;
	}

	@Override
	public PracticeTestResponse getPracticeTest() throws ExamBaseException {
		requireUser();
		Exam exam = examRepo.findFirstByPracticeTestIsTrueAndDeletedIsFalse();
		Assert.notNull(exam, ErrorInfo.EXAM_NOT_FOUND_ERROR);
		List<String> cardIds = exam.getCardIds() == null ? Collections.emptyList() : exam.getCardIds();
		List<Card> cards = cardIds.isEmpty()
				? new ArrayList<>()
				: orderCards(cardIds, cardRepo.findByIdInAndDeletedIsFalse(cardIds));

		PracticeTestResponse response = new PracticeTestResponse(exam);
		response.setCards(cards.stream().map(CardResponse::new).collect(Collectors.toList()));
		response.setDurationMinutes(120);
		response.setListeningMinutes(45);
		response.setReadingMinutes(75);
		response.setListeningQuestionCount(100);
		response.setReadingQuestionCount(100);
		int questionCount = countQuestions(cards);
		response.setQuestionCount(questionCount);
		response.setTotal(questionCount);
		response.setParts(buildParts(cards));
		return response;
	}

	private List<PracticeTestPartResponse> buildParts(List<Card> cards) {
		Map<String, List<Card>> byTopic = new LinkedHashMap<>();
		for (Card card : cards) {
			byTopic.computeIfAbsent(card.getTopicId(), key -> new ArrayList<>()).add(card);
		}
		List<PracticeTestPartResponse> parts = new ArrayList<>();
		int part7QuestionCount = 0;
		int part7CardCount = 0;
		String part7TopicId = null;
		int partNumber = 1;
		for (Map.Entry<String, List<Card>> entry : byTopic.entrySet()) {
			int questions = countQuestions(entry.getValue());
			int cardCount = entry.getValue().size();
			if (partNumber <= 6) {
				parts.add(new PracticeTestPartResponse(partNumber, partName(partNumber), entry.getKey(), questions, cardCount));
				partNumber++;
			} else {
				part7QuestionCount += questions;
				part7CardCount += cardCount;
				if (part7TopicId == null) {
					part7TopicId = entry.getKey();
				}
			}
		}
		if (part7CardCount > 0) {
			parts.add(new PracticeTestPartResponse(7, partName(7), part7TopicId, part7QuestionCount, part7CardCount));
		}
		return parts;
	}

	private String partName(int partNumber) {
		switch (partNumber) {
			case 1:
				return "Photographs";
			case 2:
				return "Question-Response";
			case 3:
				return "Conversations";
			case 4:
				return "Talks";
			case 5:
				return "Incomplete Sentences";
			case 6:
				return "Text Completion";
			case 7:
				return "Reading Comprehension";
			default:
				return "Part " + partNumber;
		}
	}

	private int countQuestions(List<Card> cards) {
		int total = 0;
		for (Card card : cards) {
			if (Boolean.TRUE.equals(card.getIsQuestionGroup()) && card.getChildCards() != null) {
				total += card.getChildCards().size();
			} else {
				total += 1;
			}
		}
		return total;
	}

	private List<Card> orderCards(List<String> cardIds, List<Card> cards) {
		if (cardIds == null || cardIds.isEmpty()) {
			return cards == null ? new ArrayList<>() : cards;
		}
		Map<String, Card> byId = cards.stream()
				.filter(Objects::nonNull)
				.collect(Collectors.toMap(Card::getId, card -> card, (left, right) -> left));
		List<Card> ordered = new ArrayList<>();
		for (String id : cardIds) {
			Card card = byId.get(id);
			if (card != null) {
				ordered.add(card);
			}
		}
		return ordered;
	}

	private void requireUser() throws ExamBaseException {
		ExamUser examUser = SecurityContextService.getUser();
		Assert.notNull(examUser, ErrorInfo.ACCESS_DENIED_ERROR);
	}
}
