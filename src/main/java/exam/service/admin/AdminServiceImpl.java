package exam.service.admin;

import exam.db.dto.question.ListQuestionRequest;
import exam.db.dto.question.ListQuestionResponse;
import exam.db.dto.exam.ListExamRequest;
import exam.db.dto.exam.ListExamResponse;
import exam.db.dto.skill.ListSkillRequest;
import exam.db.dto.skill.ListSkillResponse;
import exam.db.dto.topic.ListTopicRequest;
import exam.db.dto.topic.ListTopicResponse;
import exam.db.dto.user.BlockUserRequest;
import exam.db.dto.user.CreateUserRequest;
import exam.db.dto.user.ListUserRequest;
import exam.db.dto.user.ListUserResponse;
import exam.db.dto.user.UnblockUserRequest;
import exam.db.dto.user.UserResponse;
import exam.service.question.QuestionService;
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
	private QuestionService questionService;

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
	public UserResponse createUserForAdmin(CreateUserRequest request) throws ExamBaseException {
		return userService.createUserForAdmin(request);
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
	public ListQuestionResponse getListQuestionForAdmin(ListQuestionRequest request) throws ExamBaseException {
		return questionService.getListQuestionForAdmin(request);
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
