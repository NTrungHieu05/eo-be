package exam.service.exam;

import exam.db.dto.exam.AddQuestionToExamRequest;
import exam.db.dto.exam.CreateExamRequest;
import exam.db.dto.exam.DeleteExamRequest;
import exam.db.dto.exam.ExamResponse;
import exam.db.dto.exam.ListExamRequest;
import exam.db.dto.exam.ListExamResponse;
import exam.db.dto.exam.ListPracticeTestResponse;
import exam.db.dto.exam.PracticeTestPartResponse;
import exam.db.dto.exam.PracticeTestResponse;
import exam.db.dto.exam.ToeicScoreRequest;
import exam.db.dto.exam.ToeicScoreResponse;
import exam.db.dto.exam.UpdateExamRequest;
import exam.db.dto.question.QuestionResponse;
import exam.db.dto.user.ExamUser;
import exam.db.entity.Question;
import exam.db.entity.Exam;
import exam.db.entity.ExamResult;
import exam.db.entity.History;
import exam.db.entity.Topic;
import exam.db.enums.Assert;
import exam.db.enums.ErrorInfo;
import exam.db.repository.question.QuestionRepository;
import exam.db.repository.exam.ExamRepository;
import exam.db.repository.history.HistoryRepository;
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
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ExamServiceImpl implements ExamService {

	@Autowired
	private ExamRepository examRepo;

	@Autowired
	private QuestionRepository questionRepo;

	@Autowired
	private TopicRepository topicRepo;

	@Autowired
	private HistoryRepository historyRepo;

	@Override
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public ExamResponse create(CreateExamRequest request) throws ExamBaseException {
		requireUser();
		boolean practiceTest = Boolean.TRUE.equals(request.getPracticeTest());
		if (!practiceTest) {
			Assert.notEmpty(request.getTopicId(), ErrorInfo.TOPIC_NOT_FOUND_ERROR);
			Assert.notNull(topicRepo.findFirstByIdAndDeletedIsFalse(request.getTopicId()), ErrorInfo.TOPIC_NOT_FOUND_ERROR);
		}
		Exam exam = new Exam();
		BeanUtils.copyProperties(request, exam);
		if (practiceTest) {
			exam.setTopicId(null);
			exam.setPracticeTest(true);
		}
		exam.setQuestionIds(filterQuestionIds(request.getQuestionIds(), exam.getTopicId(), practiceTest));
		examRepo.save(exam);
		attachExamToTopic(exam.getId(), exam.getTopicId());
		return new ExamResponse(exam);
	}

	@Override
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public ExamResponse update(UpdateExamRequest request) throws ExamBaseException {
		requireUser();
		Exam exam = examRepo.findFirstByIdAndDeletedIsFalse(request.getId());
		Assert.notNull(exam, ErrorInfo.EXAM_NOT_FOUND_ERROR);
		if (StringUtils.hasText(request.getName())) {
			exam.setName(request.getName());
		}
		boolean practiceTest = request.getPracticeTest() != null
				? Boolean.TRUE.equals(request.getPracticeTest())
				: Boolean.TRUE.equals(exam.getPracticeTest());
		String previousTopicId = exam.getTopicId();
		if (practiceTest) {
			exam.setPracticeTest(true);
			exam.setTopicId(null);
			detachExamFromTopic(exam.getId(), previousTopicId);
		} else {
			exam.setPracticeTest(false);
			if (StringUtils.hasText(request.getTopicId())) {
				Assert.notNull(topicRepo.findFirstByIdAndDeletedIsFalse(request.getTopicId()), ErrorInfo.TOPIC_NOT_FOUND_ERROR);
				exam.setTopicId(request.getTopicId());
			}
			Assert.notEmpty(exam.getTopicId(), ErrorInfo.TOPIC_NOT_FOUND_ERROR);
			if (!exam.getTopicId().equals(previousTopicId)) {
				detachExamFromTopic(exam.getId(), previousTopicId);
				attachExamToTopic(exam.getId(), exam.getTopicId());
			}
		}
		if (request.getQuestionIds() != null) {
			exam.setQuestionIds(filterQuestionIds(request.getQuestionIds(), exam.getTopicId(), practiceTest));
		}
		examRepo.save(exam);
		return new ExamResponse(exam);
	}

	@Override
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public Boolean deleteMultiple(DeleteExamRequest request) throws ExamBaseException {
		requireUser();
		if (request == null || request.getIds() == null) {
			throw new ExamBaseException(ErrorInfo.BAD_REQUEST);
		}
		List<Exam> examList = examRepo.findByIdInAndDeletedIsFalse(request.getIds());
		examList.forEach(p -> {
			p.setDeleted(true);
			detachExamFromTopic(p.getId(), p.getTopicId());
		});
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
	public ExamResponse getAllQuestionsByExamId(String examId) throws ExamBaseException {
		requireUser();
		Exam exam = examRepo.findFirstByIdAndDeletedIsFalse(examId);
		Assert.notNull(exam, ErrorInfo.EXAM_NOT_FOUND_ERROR);
		Assert.isTrue(!Boolean.TRUE.equals(exam.getPracticeTest()), ErrorInfo.EXAM_NOT_FOUND_ERROR);
		List<String> questionIds = exam.getQuestionIds() == null ? Collections.emptyList() : exam.getQuestionIds();
		List<Question> questions = questionIds.isEmpty()
				? new ArrayList<>()
				: questionRepo.findByIdInAndDeletedIsFalse(questionIds);
		ExamResponse response = new ExamResponse(exam);
		response.setQuestions(orderQuestions(exam.getQuestionIds(), questions));
		return response;
	}

	@Override
	public ListPracticeTestResponse listPracticeTests() throws ExamBaseException {
		ExamUser examUser = SecurityContextService.getUser();
		Assert.notNull(examUser, ErrorInfo.ACCESS_DENIED_ERROR);
		List<Exam> exams = examRepo.findAllByPracticeTestIsTrueAndDeletedIsFalse();
		if (exams == null) {
			exams = Collections.emptyList();
		}
		Map<String, Integer> progressByExam = newestProgress(examUser.getUserId());
		List<PracticeTestResponse> items = new ArrayList<>();
		for (Exam exam : exams) {
			PracticeTestResponse item = toPracticeTestResponse(exam, false);
			item.setProgress(progressByExam.get(exam.getId()));
			items.add(item);
		}
		items.sort(Comparator.comparing(PracticeTestResponse::getName, Comparator.nullsLast(String::compareToIgnoreCase)));
		return new ListPracticeTestResponse(items, items.size());
	}

	@Override
	public PracticeTestResponse getPracticeTest(String id) throws ExamBaseException {
		requireUser();
		Exam exam = examRepo.findFirstByIdAndDeletedIsFalse(id);
		Assert.notNull(exam, ErrorInfo.EXAM_NOT_FOUND_ERROR);
		Assert.isTrue(Boolean.TRUE.equals(exam.getPracticeTest()), ErrorInfo.EXAM_NOT_FOUND_ERROR);
		return toPracticeTestResponse(exam, true);
	}

	@Override
	public ToeicScoreResponse score(ToeicScoreRequest request) throws ExamBaseException {
		requireUser();
		int listeningCorrect = request == null || request.getListeningCorrect() == null
				? 0
				: request.getListeningCorrect();
		int readingCorrect = request == null || request.getReadingCorrect() == null
				? 0
				: request.getReadingCorrect();
		ToeicScoreResponse response = new ToeicScoreResponse();
		response.setListeningCorrect(Math.max(0, Math.min(100, listeningCorrect)));
		response.setReadingCorrect(Math.max(0, Math.min(100, readingCorrect)));
		response.setListeningScore(ToeicScore.listening(listeningCorrect));
		response.setReadingScore(ToeicScore.reading(readingCorrect));
		response.setTotalScore(response.getListeningScore() + response.getReadingScore());
		return response;
	}

	@Override
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public ExamResponse addQuestion(AddQuestionToExamRequest request) throws ExamBaseException {
		requireUser();
		Assert.notEmpty(request.getId(), ErrorInfo.EXAM_NOT_FOUND_ERROR);
		Assert.notEmpty(request.getQuestionIds(), ErrorInfo.BAD_REQUEST);
		Exam exam = examRepo.findFirstByIdAndDeletedIsFalse(request.getId());
		Assert.notNull(exam, ErrorInfo.EXAM_NOT_FOUND_ERROR);
		List<String> questionIds = exam.getQuestionIds() == null ? new ArrayList<>() : new ArrayList<>(exam.getQuestionIds());
		for (String questionId : request.getQuestionIds()) {
			if (!StringUtils.hasText(questionId) || questionIds.contains(questionId)) {
				continue;
			}
			Question question = questionRepo.findFirstByIdAndDeletedIsFalse(questionId);
			if (question == null) {
				continue;
			}
			if (!Boolean.TRUE.equals(exam.getPracticeTest())
					&& StringUtils.hasText(exam.getTopicId())
					&& !exam.getTopicId().equals(question.getTopicId())) {
				continue;
			}
			questionIds.add(questionId);
		}
		exam.setQuestionIds(questionIds);
		examRepo.save(exam);
		return new ExamResponse(exam);
	}

	private List<PracticeTestPartResponse> buildParts(List<Question> questions, Map<String, Integer> partByTopic) {
		Map<String, List<Question>> byTopic = new LinkedHashMap<>();
		for (Question question : questions) {
			byTopic.computeIfAbsent(question.getTopicId(), key -> new ArrayList<>()).add(question);
		}
		Map<Integer, PracticePartBucket> buckets = new TreeMap<>();
		int sequence = 1;
		for (Map.Entry<String, List<Question>> entry : byTopic.entrySet()) {
			int parsed = partByTopic.getOrDefault(entry.getKey(), 0);
			int part;
			if (parsed > 0) {
				part = parsed;
			} else if (sequence <= 6) {
				part = sequence;
				sequence++;
			} else {
				part = 7;
			}
			PracticePartBucket bucket = buckets.computeIfAbsent(part, key -> new PracticePartBucket());
			bucket.questionCount += countQuestions(entry.getValue());
			bucket.questionRecordCount += entry.getValue().size();
			if (bucket.topicId == null) {
				bucket.topicId = entry.getKey();
			}
		}
		List<PracticeTestPartResponse> parts = new ArrayList<>();
		for (Map.Entry<Integer, PracticePartBucket> entry : buckets.entrySet()) {
			PracticePartBucket bucket = entry.getValue();
			parts.add(new PracticeTestPartResponse(
					entry.getKey(), partName(entry.getKey()), bucket.topicId, bucket.questionCount, bucket.questionRecordCount));
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

	private List<String> filterQuestionIds(List<String> questionIds, String topicId, boolean practiceTest) {
		if (questionIds == null || questionIds.isEmpty()) {
			return questionIds == null ? new ArrayList<>() : questionIds;
		}
		List<Question> found = questionRepo.findByIdInAndDeletedIsFalse(questionIds);
		if (found == null) {
			found = new ArrayList<>();
		}
		Map<String, Question> byId = found.stream()
				.filter(Objects::nonNull)
				.collect(Collectors.toMap(Question::getId, question -> question, (left, right) -> left));
		List<String> filtered = new ArrayList<>();
		for (String id : questionIds) {
			Question question = byId.get(id);
			if (question == null) {
				continue;
			}
			if (!practiceTest && StringUtils.hasText(topicId) && !topicId.equals(question.getTopicId())) {
				continue;
			}
			filtered.add(id);
		}
		return filtered;
	}

	private void attachExamToTopic(String examId, String topicId) {
		if (!StringUtils.hasText(examId) || !StringUtils.hasText(topicId)) {
			return;
		}
		Topic topic = topicRepo.findFirstByIdAndDeletedIsFalse(topicId);
		if (topic == null) {
			return;
		}
		List<String> examIds = topic.getExamIds() == null ? new ArrayList<>() : new ArrayList<>(topic.getExamIds());
		if (!examIds.contains(examId)) {
			examIds.add(examId);
			topic.setExamIds(examIds);
			topicRepo.save(topic);
		}
	}

	private void detachExamFromTopic(String examId, String topicId) {
		if (!StringUtils.hasText(examId) || !StringUtils.hasText(topicId)) {
			return;
		}
		Topic topic = topicRepo.findFirstByIdAndDeletedIsFalse(topicId);
		if (topic == null || topic.getExamIds() == null) {
			return;
		}
		List<String> examIds = new ArrayList<>(topic.getExamIds());
		if (examIds.remove(examId)) {
			topic.setExamIds(examIds);
			topicRepo.save(topic);
		}
	}

	private PracticeTestResponse toPracticeTestResponse(Exam exam, boolean includeQuestions) {
		List<String> questionIds = exam.getQuestionIds() == null ? Collections.emptyList() : exam.getQuestionIds();
		List<Question> questions = questionIds.isEmpty()
				? new ArrayList<>()
				: orderQuestions(questionIds, questionRepo.findByIdInAndDeletedIsFalse(questionIds));

		PracticeTestResponse response = new PracticeTestResponse(exam);
		response.setDurationMinutes(120);
		response.setListeningMinutes(45);
		response.setReadingMinutes(75);
		response.setListeningQuestionCount(100);
		response.setReadingQuestionCount(100);
		int questionCount = countQuestions(questions);
		response.setQuestionCount(questionCount);
		response.setTotal(questionCount);
		response.setScaledScore(isOfficialScale(questions));
		if (includeQuestions) {
			Map<String, Integer> partByTopic = partNumbers(questions);
			questions = orderQuestionsByToeicPart(questions, partByTopic);
			response.setQuestions(questions.stream()
					.map(question -> toQuestionResponse(question, partByTopic))
					.collect(Collectors.toList()));
			response.setParts(buildParts(questions, partByTopic));
		}
		return response;
	}

	private QuestionResponse toQuestionResponse(Question question, Map<String, Integer> partByTopic) {
		QuestionResponse response = new QuestionResponse(question);
		int part = partByTopic.getOrDefault(question.getTopicId(), 0);
		if (part > 0) {
			response.setPartNumber(part);
		}
		return response;
	}

	private List<Question> orderQuestionsByToeicPart(List<Question> questions, Map<String, Integer> partByTopic) {
		if (questions == null || questions.size() < 2 || partByTopic.values().stream().noneMatch(part -> part > 0)) {
			return questions;
		}
		Map<String, Integer> firstIndex = new HashMap<>();
		List<Integer> order = new ArrayList<>();
		for (int i = 0; i < questions.size(); i++) {
			order.add(i);
			firstIndex.putIfAbsent(questions.get(i).getTopicId(), i);
		}
		order.sort(Comparator
				.comparingInt((Integer index) -> {
					int part = partByTopic.getOrDefault(questions.get(index).getTopicId(), 0);
					return part > 0 ? part : 99;
				})
				.thenComparingInt(index -> firstIndex.getOrDefault(questions.get(index).getTopicId(), index))
				.thenComparingInt(index -> index));
		List<Question> sorted = new ArrayList<>();
		for (Integer index : order) {
			sorted.add(questions.get(index));
		}
		return sorted;
	}

	private Map<String, Integer> partNumbers(List<Question> questions) {
		Map<String, Integer> parts = new HashMap<>();
		for (Question question : questions) {
			String topicId = question.getTopicId();
			if (topicId == null || parts.containsKey(topicId)) {
				continue;
			}
			Topic topic = topicRepo.findFirstByIdAndDeletedIsFalse(topicId);
			parts.put(topicId, topic == null ? 0 : partNumberFromName(topic.getName()));
		}
		return parts;
	}

	private int partNumberFromName(String name) {
		if (!StringUtils.hasText(name)) {
			return 0;
		}
		Matcher matcher = Pattern.compile("(\\d+)").matcher(name);
		if (!matcher.find()) {
			return 0;
		}
		int part = Integer.parseInt(matcher.group(1));
		return part >= 1 && part <= 7 ? part : 0;
	}

	private static class PracticePartBucket {
		private String topicId;
		private int questionCount;
		private int questionRecordCount;
	}

	private int countQuestions(List<Question> questions) {
		int total = 0;
		if (questions == null) {
			return 0;
		}
		for (Question question : questions) {
			if (question == null) {
				continue;
			}
			if (isQuestionGroup(question)) {
				total += question.getChildQuestions() == null ? 0 : question.getChildQuestions().size();
			} else {
				total += 1;
			}
		}
		return total;
	}

	private boolean isQuestionGroup(Question question) {
		return Boolean.TRUE.equals(question.getIsQuestionGroup())
				|| (question.getChildQuestions() != null && !question.getChildQuestions().isEmpty());
	}

	private List<Question> orderQuestions(List<String> questionIds, List<Question> questions) {
		if (questionIds == null || questionIds.isEmpty()) {
			return questions == null ? new ArrayList<>() : questions;
		}
		Map<String, Question> byId = questions.stream()
				.filter(Objects::nonNull)
				.collect(Collectors.toMap(Question::getId, question -> question, (left, right) -> left));
		List<Question> ordered = new ArrayList<>();
		for (String id : questionIds) {
			Question question = byId.get(id);
			if (question != null) {
				ordered.add(question);
			}
		}
		return ordered;
	}

	private Map<String, Integer> newestProgress(String userId) {
		Map<String, Integer> progress = new HashMap<>();
		List<History> histories = historyRepo.findAllByUserIdAndDeletedIsFalse(userId);
		if (histories == null) {
			return progress;
		}
		List<History> ordered = new ArrayList<>(histories);
		ordered.sort(Comparator.comparing(History::getLastModifiedDate, Comparator.nullsFirst(Date::compareTo)));
		for (History history : ordered) {
			if (history == null || history.getExamResults() == null) {
				continue;
			}
			for (ExamResult result : history.getExamResults()) {
				if (result != null && result.getId() != null && result.getProgress() != null) {
					progress.put(result.getId(), result.getProgress());
				}
			}
		}
		return progress;
	}

	private boolean isOfficialScale(List<Question> questions) {
		if (questions == null) {
			return false;
		}
		Map<String, Integer> partByTopic = partNumbers(questions);
		int listening = 0;
		int reading = 0;
		int questionNo = 0;
		for (Question question : questions) {
			if (question == null) {
				continue;
			}
			int part = partByTopic.getOrDefault(question.getTopicId(), 0);
			int leaves = isQuestionGroup(question)
					? (question.getChildQuestions() == null ? 0 : question.getChildQuestions().size())
					: 1;
			for (int i = 0; i < leaves; i++) {
				questionNo++;
				if (isListening(part, questionNo)) {
					listening++;
				} else {
					reading++;
				}
			}
		}
		return listening == 100 && reading == 100;
	}

	private boolean isListening(int part, int questionNo) {
		if (part >= 1 && part <= 7) {
			return part <= 4;
		}
		return questionNo <= 100;
	}

	private void requireUser() throws ExamBaseException {
		ExamUser examUser = SecurityContextService.getUser();
		Assert.notNull(examUser, ErrorInfo.ACCESS_DENIED_ERROR);
	}
}
