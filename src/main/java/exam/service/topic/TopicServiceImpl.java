package exam.service.topic;

import exam.db.dto.exam.ExamDataResponse;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
		topic.setName(request.getName());
		topic.setExamIds(request.getExamIds());
		topicRepo.save(topic);
		return new TopicResponse(topic);
	}

	@Override
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public Boolean deleteMultiple(DeleteTopicRequest request) throws ExamBaseException {
		requireUser();
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
		List<TopicResponse> topics = topicRepo.findAllByDeletedIsFalse().stream()
				.map(TopicResponse::new)
				.collect(Collectors.toList());
		return new ListTopicResponse(topics, topics.size());
	}

	@Override
	public TopicResponse getAllExamByTopicId(String topicId) throws ExamBaseException {
		ExamUser examUser = SecurityContextService.getUser();
		Assert.notNull(examUser, ErrorInfo.ACCESS_DENIED_ERROR);
		Topic topic = topicRepo.findFirstByIdAndDeletedIsFalse(topicId);
		Assert.notNull(topic, ErrorInfo.TOPIC_NOT_FOUND_ERROR);
		List<String> examIds = topic.getExamIds() == null ? Collections.emptyList() : topic.getExamIds();
		List<Exam> exams = examIds.isEmpty()
				? new ArrayList<>()
				: examRepo.findByIdInAndDeletedIsFalse(examIds);
		List<History> histories = historyRepo.findAllByUserIdAndDeletedIsFalse(examUser.getUserId());
		if (histories == null) {
			histories = new ArrayList<>();
		}
		return buildTopicByIdResponse(topic, exams, histories);
	}

	private TopicResponse buildTopicByIdResponse(Topic topic, List<Exam> exams, List<History> histories) {
		TopicResponse topicResponse = new TopicResponse(topic);
		List<ExamDataResponse> examDataResponse = new ArrayList<>();
		exams.forEach(p -> {
			ExamDataResponse response = new ExamDataResponse(p);
			histories.forEach(h -> {
				if (h.getExamResults() == null) {
					return;
				}
				h.getExamResults().forEach(e -> {
					if (e.getId() != null && e.getId().equals(p.getId())) {
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
