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
import exam.ultis.ExamBaseException;

public interface AdminService {
	ListUserResponse listUserForAdmin(ListUserRequest request) throws ExamBaseException;

	UserResponse createUserForAdmin(CreateUserRequest request) throws ExamBaseException;

	Boolean blockUser(BlockUserRequest request) throws ExamBaseException;

	Boolean unblockUser(UnblockUserRequest request) throws ExamBaseException;

	ListQuestionResponse getListQuestionForAdmin(ListQuestionRequest request) throws ExamBaseException;

	ListSkillResponse getListSkillForAdmin(ListSkillRequest request) throws ExamBaseException;

	ListTopicResponse getListTopicForAdmin(ListTopicRequest request) throws ExamBaseException;

	ListExamResponse getListExamForAdmin(ListExamRequest request) throws ExamBaseException;
}
