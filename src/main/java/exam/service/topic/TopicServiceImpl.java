package exam.service.topic;

import exam.db.dto.exam.ExamDataResponse;
import exam.db.dto.topic.AddExamToTopicRequest;
import exam.db.dto.topic.CreateTopicRequest;
import exam.db.dto.topic.DeleteTopicRequest;
import exam.db.dto.topic.ListTopicRequest;
import exam.db.dto.topic.ListTopicResponse;
import exam.db.dto.topic.TopicResponse;
import exam.db.dto.topic.UpdateTopicRequest;
import exam.db.dto.user.ExamUser;
import exam.db.entity.Exam;
import exam.db.entity.History;
import exam.db.entity.Topic;
import exam.db.enums.Assert;
import exam.db.enums.ErrorInfo;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TopicServiceImpl implements TopicService {

	@Autowired
	private TopicRepository topicRepo;

	@Autowired
	private ExamRepository examRepo;

	@Autowired
	private HistoryRepository historyRepo;

	@Override
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public TopicResponse create(CreateTopicRequest request) throws ExamBaseException {
		requireUser();
		Assert.notEmpty(request.getName(), ErrorInfo.BAD_REQUEST);
		Topic topic = new Topic();
		BeanUtils.copyProperties(request, topic);
		topicRepo.save(topic);
		return new TopicResponse(topic);
	}

	@Override
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public TopicResponse update(UpdateTopicRequest request) throws ExamBaseException {
		requireUser();
		Topic topic = topicRepo.findFirstByIdAndDeletedIsFalse(request.getId());
		Assert.notNull(topic, ErrorInfo.TOPIC_NOT_FOUND_ERROR);
		if (StringUtils.hasText(request.getName())) {
			topic.setName(request.getName());
		}
		topicRepo.save(topic);
		return new TopicResponse(topic);
	}

	@Override
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public Boolean deleteMultiple(DeleteTopicRequest request) throws ExamBaseException {
		requireUser();
		if (request == null || request.getIds() == null) {
			throw new ExamBaseException(ErrorInfo.BAD_REQUEST);
		}
		List<Topic> topicList = topicRepo.findByIdInAndDeletedIsFalse(request.getIds());
		topicList.forEach(p -> p.setDeleted(true));
		topicRepo.saveAll(topicList);
		return Boolean.TRUE;
	}

	@Override
	public TopicResponse getDetail(String id) throws ExamBaseException {
		requireUser();
		Topic topic = topicRepo.findFirstByIdAndDeletedIsFalse(id);
		Assert.notNull(topic, ErrorInfo.TOPIC_NOT_FOUND_ERROR);
		return new TopicResponse(topic);
	}

	@Override
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public ListTopicResponse getListTopicForAdmin(ListTopicRequest request) throws ExamBaseException {
		requireUser();
		List<TopicResponse> topics = new ArrayList<>();
		for (Topic topic : topicRepo.findAllByDeletedIsFalse()) {
			TopicResponse item = new TopicResponse(topic);
			List<String> liveExamIds = listPracticeExamsOfTopic(topic).stream()
					.map(Exam::getId)
					.collect(Collectors.toList());
			item.setExamIds(liveExamIds);
			topics.add(item);
		}
		return new ListTopicResponse(topics, topics.size());
	}

	@Override
	public TopicResponse getAllExamByTopicId(String topicId) throws ExamBaseException {
		ExamUser examUser = SecurityContextService.getUser();
		Assert.notNull(examUser, ErrorInfo.ACCESS_DENIED_ERROR);
		Topic topic = topicRepo.findFirstByIdAndDeletedIsFalse(topicId);
		Assert.notNull(topic, ErrorInfo.TOPIC_NOT_FOUND_ERROR);
		List<Exam> exams = listPracticeExamsOfTopic(topic);
		List<History> histories = historyRepo.findAllByUserIdAndDeletedIsFalse(examUser.getUserId());
		if (histories == null) {
			histories = new ArrayList<>();
		}
		return buildTopicByIdResponse(topic, exams, histories);
	}

	@Override
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public TopicResponse addExam(AddExamToTopicRequest request) throws ExamBaseException {
		requireUser();
		Assert.notEmpty(request.getId(), ErrorInfo.TOPIC_NOT_FOUND_ERROR);
		Assert.notEmpty(request.getExamIds(), ErrorInfo.BAD_REQUEST);
		Topic topic = topicRepo.findFirstByIdAndDeletedIsFalse(request.getId());
		Assert.notNull(topic, ErrorInfo.TOPIC_NOT_FOUND_ERROR);
		List<String> examIds = topic.getExamIds() == null ? new ArrayList<>() : new ArrayList<>(topic.getExamIds());
		for (String examId : request.getExamIds()) {
			if (!StringUtils.hasText(examId) || examIds.contains(examId)) {
				continue;
			}
			Exam exam = examRepo.findFirstByIdAndDeletedIsFalse(examId);
			if (exam == null || Boolean.TRUE.equals(exam.getPracticeTest())) {
				continue;
			}
			if (StringUtils.hasText(exam.getTopicId()) && !topic.getId().equals(exam.getTopicId())) {
				continue;
			}
			if (!StringUtils.hasText(exam.getTopicId())) {
				exam.setTopicId(topic.getId());
				examRepo.save(exam);
			}
			examIds.add(examId);
		}
		topic.setExamIds(examIds);
		topicRepo.save(topic);
		return new TopicResponse(topic);
	}

	private List<Exam> listPracticeExamsOfTopic(Topic topic) {
		List<Exam> exams = new ArrayList<>();
		Set<String> seen = new LinkedHashSet<>();
		List<String> examIds = topic.getExamIds() == null ? Collections.emptyList() : topic.getExamIds();
		if (!examIds.isEmpty()) {
			Map<String, Exam> byId = new HashMap<>();
			List<Exam> byIds = examRepo.findByIdInAndDeletedIsFalse(examIds);
			if (byIds != null) {
				for (Exam exam : byIds) {
					if (exam != null && StringUtils.hasText(exam.getId())) {
						byId.put(exam.getId(), exam);
					}
				}
			}
			for (String examId : examIds) {
				addPartExamOfTopic(exams, seen, byId.get(examId), topic.getId());
			}
		}
		List<Exam> byTopic = examRepo.findAllByTopicIdAndDeletedIsFalse(topic.getId());
		if (byTopic != null) {
			for (Exam exam : byTopic) {
				addPartExamOfTopic(exams, seen, exam, topic.getId());
			}
		}
		return exams;
	}

	private void addPartExam(List<Exam> exams, Set<String> seen, Exam exam) {
		if (exam == null || !StringUtils.hasText(exam.getId()) || Boolean.TRUE.equals(exam.getPracticeTest())) {
			return;
		}
		if (seen.add(exam.getId())) {
			exams.add(exam);
		}
	}

	private void addPartExamOfTopic(List<Exam> exams, Set<String> seen, Exam exam, String topicId) {
		if (exam == null || Boolean.TRUE.equals(exam.getPracticeTest())) {
			return;
		}
		if (StringUtils.hasText(exam.getTopicId()) && !topicId.equals(exam.getTopicId())) {
			return;
		}
		addPartExam(exams, seen, exam);
	}

	private TopicResponse buildTopicByIdResponse(Topic topic, List<Exam> exams, List<History> histories) {
		TopicResponse topicResponse = new TopicResponse(topic);
		List<History> orderedHistories = new ArrayList<>();
		if (histories != null) {
			orderedHistories.addAll(histories);
		}
		orderedHistories.sort(Comparator.comparing(History::getLastModifiedDate, Comparator.nullsFirst(Date::compareTo)));
		List<ExamDataResponse> examDataResponse = new ArrayList<>();
		exams.forEach(p -> {
			ExamDataResponse response = new ExamDataResponse(p);
			orderedHistories.forEach(h -> {
				if (h == null || h.getExamResults() == null) {
					return;
				}
				h.getExamResults().forEach(e -> {
					if (e != null && e.getId() != null && e.getId().equals(p.getId()) && e.getProgress() != null) {
						response.setProgress(e.getProgress());
					}
				});
			});
			examDataResponse.add(response);
		});
		topicResponse.setExams(examDataResponse);
		return topicResponse;
	}

	private void requireUser() throws ExamBaseException {
		ExamUser examUser = SecurityContextService.getUser();
		Assert.notNull(examUser, ErrorInfo.ACCESS_DENIED_ERROR);
	}
}
