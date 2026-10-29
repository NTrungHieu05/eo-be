package exam.service.skill;

import exam.db.dto.skill.AddSkillRequest;
import exam.db.dto.skill.CreateSkillRequest;
import exam.db.dto.skill.DeleteSkillRequest;
import exam.db.dto.skill.ListSkillRequest;
import exam.db.dto.skill.ListSkillResponse;
import exam.db.dto.skill.SkillResponse;
import exam.db.dto.skill.SkillTopicResponse;
import exam.db.dto.skill.UpdateSkillRequest;
import exam.db.dto.user.ExamUser;
import exam.db.entity.Skill;
import exam.db.entity.Topic;
import exam.db.enums.Assert;
import exam.db.enums.ErrorInfo;
import exam.db.repository.skill.SkillRepository;
import exam.db.repository.topic.TopicRepository;
import exam.ultis.ExamBaseException;
import exam.ultis.SecurityContextService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SkillServiceImpl implements SkillService {

	@Autowired
	private SkillRepository skillRepo;

	@Autowired
	private TopicRepository topicRepo;

	@Override
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public SkillResponse create(CreateSkillRequest request) throws ExamBaseException {
		requireUser();
		Assert.notEmpty(request.getName(), ErrorInfo.BAD_REQUEST);
		Skill skill = new Skill();
		BeanUtils.copyProperties(request, skill);
		skillRepo.save(skill);
		return new SkillResponse(skill);
	}

	@Override
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public SkillResponse update(UpdateSkillRequest request) throws ExamBaseException {
		requireUser();
		Skill skill = skillRepo.findFirstByIdAndDeletedIsFalse(request.getId());
		Assert.notNull(skill, ErrorInfo.SKILL_NOT_FOUND_ERROR);
		if (StringUtils.hasText(request.getName())) {
			skill.setName(request.getName());
		}
		if (request.getTopicIds() != null) {
			skill.setTopicIds(request.getTopicIds());
		}
		skillRepo.save(skill);
		return new SkillResponse(skill);
	}

	@Override
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public Boolean deleteMultiple(DeleteSkillRequest request) throws ExamBaseException {
		requireUser();
		if (request == null || request.getIds() == null) {
			throw new ExamBaseException(ErrorInfo.BAD_REQUEST);
		}
		List<Skill> skillList = skillRepo.findByIdInAndDeletedIsFalse(request.getIds());
		skillList.forEach(p -> p.setDeleted(true));
		skillRepo.saveAll(skillList);
		return Boolean.TRUE;
	}

	@Override
	public SkillResponse getDetail(String id) throws ExamBaseException {
		requireUser();
		Skill skill = skillRepo.findFirstByIdAndDeletedIsFalse(id);
		Assert.notNull(skill, ErrorInfo.SKILL_NOT_FOUND_ERROR);
		return new SkillResponse(skill);
	}

	@Override
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public ListSkillResponse getListSkillForAdmin(ListSkillRequest request) throws ExamBaseException {
		requireUser();
		List<SkillResponse> skills = skillRepo.findAllByDeletedIsFalse().stream()
				.map(SkillResponse::new)
				.collect(Collectors.toList());
		return new ListSkillResponse(skills, skills.size());
	}

	@Override
	public List<SkillResponse> getAllSkillForHeader() throws ExamBaseException {
		List<Skill> skills = skillRepo.findAllByDeletedIsFalse().stream()
				.filter(skill -> skill.getTopicIds() != null && !skill.getTopicIds().isEmpty())
				.collect(Collectors.toList());
		List<String> topicIds = new ArrayList<>();
		skills.forEach(s -> {
			if (s.getTopicIds() != null) {
				topicIds.addAll(s.getTopicIds());
			}
		});
		List<Topic> topics = topicIds.isEmpty()
				? new ArrayList<>()
				: topicRepo.findAllByIdInAndDeletedIsFalse(topicIds);
		return buildListSkillResponse(skills, topics).stream()
				.filter(skill -> skill.getTopics() != null && !skill.getTopics().isEmpty())
				.collect(Collectors.toList());
	}

	@Override
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public SkillResponse addTopic(AddSkillRequest request) throws ExamBaseException {
		requireUser();
		Assert.notEmpty(request.getId(), ErrorInfo.SKILL_NOT_FOUND_ERROR);
		Assert.notEmpty(request.getTopicIds(), ErrorInfo.BAD_REQUEST);
		Skill skill = skillRepo.findFirstByIdAndDeletedIsFalse(request.getId());
		Assert.notNull(skill, ErrorInfo.SKILL_NOT_FOUND_ERROR);
		List<String> topicIds = skill.getTopicIds() == null ? new ArrayList<>() : new ArrayList<>(skill.getTopicIds());
		for (String topicId : request.getTopicIds()) {
			if (!StringUtils.hasText(topicId) || topicIds.contains(topicId)) {
				continue;
			}
			if (topicRepo.findFirstByIdAndDeletedIsFalse(topicId) != null) {
				topicIds.add(topicId);
			}
		}
		skill.setTopicIds(topicIds);
		skillRepo.save(skill);
		return new SkillResponse(skill);
	}

	private List<SkillResponse> buildListSkillResponse(List<Skill> skills, List<Topic> topics) {
		List<SkillResponse> responses = new ArrayList<>();
		skills.forEach(s -> {
			List<SkillTopicResponse> topicResponseList = new ArrayList<>();
			if (s.getTopicIds() != null) {
				s.getTopicIds().forEach(topicId -> topics.stream()
						.filter(topic -> topic.getId().equals(topicId))
						.forEach(topic -> {
							SkillTopicResponse item = new SkillTopicResponse();
							String topicName = topic.getName() == null ? "" : topic.getName();
							item.setTopicId(topic.getId());
							item.setTopicName(topicName);
							item.setSlug(topicName.toLowerCase().replace(" ", "-"));
							topicResponseList.add(item);
						}));
			}
			SkillResponse response = new SkillResponse(s);
			response.setTopics(topicResponseList);
			responses.add(response);
		});
		return responses;
	}

	private void requireUser() throws ExamBaseException {
		ExamUser examUser = SecurityContextService.getUser();
		Assert.notNull(examUser, ErrorInfo.ACCESS_DENIED_ERROR);
	}
}
