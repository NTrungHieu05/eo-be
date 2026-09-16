package exam.service.admin;

import exam.db.dto.card.ListCardRequest;
import exam.db.dto.card.ListCardResponse;
import exam.db.dto.exam.ListExamRequest;
import exam.db.dto.exam.ListExamResponse;
import exam.db.dto.skill.ListSkillRequest;
import exam.db.dto.skill.ListSkillResponse;
import exam.db.dto.topic.ListTopicRequest;
import exam.db.dto.topic.ListTopicResponse;
import exam.db.dto.user.BlockUserRequest;
import exam.db.dto.user.ListUserRequest;
import exam.db.dto.user.ListUserResponse;
import exam.db.dto.user.UnblockUserRequest;
import exam.service.card.CardService;
import exam.service.exam.ExamService;
import exam.service.skill.SkillService;
import exam.service.topic.TopicService;
import exam.service.user.UserService;
import exam.ultis.ExamBaseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminServiceImpl implements AdminService {

	@Autowired
	private UserService userService;

	@Autowired
	private CardService cardService;

	@Autowired
	private SkillService skillService;

	@Autowired
	private TopicService topicService;

	@Autowired
	private ExamService examService;

	@Override
	public ListUserResponse listUserForAdmin(ListUserRequest request) throws ExamBaseException {
		return userService.listUserForAdmin(request);
	}

	@Override
	public Boolean blockUser(BlockUserRequest request) throws ExamBaseException {
		return userService.blockUser(request);
	}

	@Override
	public Boolean unblockUser(UnblockUserRequest request) throws ExamBaseException {
		return userService.unblockUser(request);
	}

	@Override
	public ListCardResponse getListCardForAdmin(ListCardRequest request) throws ExamBaseException {
		return cardService.getListCardForAdmin(request);
	}

	@Override
	public ListSkillResponse getListSkillForAdmin(ListSkillRequest request) throws ExamBaseException {
		return skillService.getListSkillForAdmin(request);
	}

	@Override
	public ListTopicResponse getListTopicForAdmin(ListTopicRequest request) throws ExamBaseException {
		return topicService.getListTopicForAdmin(request);
	}

	@Override
	public ListExamResponse getListExamForAdmin(ListExamRequest request) throws ExamBaseException {
		return examService.getListExamForAdmin(request);
	}
}
