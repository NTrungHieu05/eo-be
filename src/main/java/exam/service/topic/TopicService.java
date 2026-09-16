package exam.service.topic;

import exam.db.dto.topic.CreateTopicRequest;
import exam.db.dto.topic.DeleteTopicRequest;
import exam.db.dto.topic.ListTopicRequest;
import exam.db.dto.topic.ListTopicResponse;
import exam.db.dto.topic.TopicResponse;
import exam.db.dto.topic.UpdateTopicRequest;
import exam.ultis.ExamBaseException;

public interface TopicService {
	TopicResponse create(CreateTopicRequest request) throws ExamBaseException;

	TopicResponse update(UpdateTopicRequest request) throws ExamBaseException;

	Boolean deleteMultiple(DeleteTopicRequest request) throws ExamBaseException;

	TopicResponse getDetail(String id) throws ExamBaseException;

	ListTopicResponse getListTopicForAdmin(ListTopicRequest request) throws ExamBaseException;

	TopicResponse getAllExamByTopicId(String topicId) throws ExamBaseException;
}
