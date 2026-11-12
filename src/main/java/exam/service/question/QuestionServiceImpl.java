package exam.service.question;

import exam.db.dto.question.QuestionResponse;
import exam.db.dto.question.CreateQuestionRequest;
import exam.db.dto.question.DeleteQuestionRequest;
import exam.db.dto.question.ListQuestionRequest;
import exam.db.dto.question.ListQuestionResponse;
import exam.db.dto.question.UpdateQuestionRequest;
import exam.db.dto.user.ExamUser;
import exam.db.entity.Answer;
import exam.db.entity.Question;
import exam.db.entity.Exam;
import exam.db.entity.QuestionContent;
import exam.db.entity.Topic;
import exam.db.repository.topic.TopicRepository;
import exam.db.enums.Assert;
import exam.db.enums.ErrorInfo;
import exam.db.repository.question.QuestionRepository;
import exam.db.repository.exam.ExamRepository;
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
public class QuestionServiceImpl implements QuestionService {

	@Autowired
	private QuestionRepository questionRepo;

	@Autowired
	private ExamRepository examRepo;

	@Autowired
	private TopicRepository topicRepo;

	@Override
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public QuestionResponse create(CreateQuestionRequest request) throws ExamBaseException {
		requireUser();
		Assert.notEmpty(request.getTopicId(), ErrorInfo.TOPIC_NOT_FOUND_ERROR);
		Topic topic = topicRepo.findFirstByIdAndDeletedIsFalse(request.getTopicId());
		Assert.notNull(topic, ErrorInfo.TOPIC_NOT_FOUND_ERROR);
		Question question = new Question();
		BeanUtils.copyProperties(request, question);
		question.setTopicId(request.getTopicId());
		questionRepo.save(question);
		if (StringUtils.hasText(request.getExamId())) {
			Exam exam = examRepo.findFirstByIdAndDeletedIsFalse(request.getExamId());
			if (exam != null && canPutQuestionInExam(question, exam)) {
				List<String> questionIds = exam.getQuestionIds() == null ? new ArrayList<>() : new ArrayList<>(exam.getQuestionIds());
				questionIds.add(question.getId());
				exam.setQuestionIds(questionIds);
				examRepo.save(exam);
			}
		}
		return new QuestionResponse(question);
	}

	@Override
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public QuestionResponse update(UpdateQuestionRequest request) throws ExamBaseException {
		requireUser();
		Question question = questionRepo.findFirstByIdAndDeletedIsFalse(request.getId());
		Assert.notNull(question, ErrorInfo.QUESTION_IS_NULL);
		String topicId = StringUtils.hasText(request.getTopicId()) ? request.getTopicId() : question.getTopicId();
		Assert.notEmpty(topicId, ErrorInfo.TOPIC_NOT_FOUND_ERROR);
		Topic topic = topicRepo.findFirstByIdAndDeletedIsFalse(topicId);
		Assert.notNull(topic, ErrorInfo.TOPIC_NOT_FOUND_ERROR);
		String examId = question.getExamId();
		Answer answer = question.getAnswer();
		QuestionContent content = question.getQuestion();
		List<Question> childQuestions = question.getChildQuestions();
		BeanUtils.copyProperties(request, question);
		question.setId(request.getId());
		question.setTopicId(topicId);
		if (!StringUtils.hasText(request.getExamId())) {
			question.setExamId(examId);
		}
		if (request.getQuestion() == null) {
			question.setQuestion(content);
		}
		if (request.getAnswer() == null) {
			question.setAnswer(answer);
		}
		if (Boolean.FALSE.equals(request.getIsQuestionGroup())) {
			question.setChildQuestions(new ArrayList<>());
		} else if (request.getChildQuestions() == null) {
			question.setChildQuestions(childQuestions);
		}
		questionRepo.save(question);
		return new QuestionResponse(question);
	}

	@Override
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public Boolean deleteMultiple(DeleteQuestionRequest request) throws ExamBaseException {
		requireUser();
		if (request == null || request.getIds() == null) {
			throw new ExamBaseException(ErrorInfo.BAD_REQUEST);
		}
		List<Question> questionList = questionRepo.findByIdInAndDeletedIsFalse(request.getIds());
		questionList.forEach(p -> p.setDeleted(true));
		questionRepo.saveAll(questionList);
		detachQuestionsFromExams(request.getIds());
		return Boolean.TRUE;
	}

	@Override
	public QuestionResponse getDetail(String id) throws ExamBaseException {
		requireUser();
		Question question = questionRepo.findFirstByIdAndDeletedIsFalse(id);
		Assert.notNull(question, ErrorInfo.QUESTION_IS_NULL);
		return new QuestionResponse(question);
	}

	@Override
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public ListQuestionResponse getListQuestionForAdmin(ListQuestionRequest request) throws ExamBaseException {
		requireUser();
		List<QuestionResponse> questions = questionRepo.findAllByDeletedIsFalse().stream()
				.map(QuestionResponse::new)
				.collect(Collectors.toList());
		return new ListQuestionResponse(questions, questions.size());
	}

	private void detachQuestionsFromExams(List<String> questionIds) {
		if (questionIds == null || questionIds.isEmpty()) {
			return;
		}
		List<Exam> exams = examRepo.findAllByDeletedIsFalse();
		if (exams == null || exams.isEmpty()) {
			return;
		}
		for (Exam exam : exams) {
			if (exam.getQuestionIds() == null || exam.getQuestionIds().isEmpty()) {
				continue;
			}
			List<String> next = new ArrayList<>(exam.getQuestionIds());
			if (next.removeAll(questionIds)) {
				exam.setQuestionIds(next);
				examRepo.save(exam);
			}
		}
	}

	private boolean canPutQuestionInExam(Question question, Exam exam) {
		if (Boolean.TRUE.equals(exam.getPracticeTest()) || !StringUtils.hasText(exam.getTopicId())) {
			return true;
		}
		return exam.getTopicId().equals(question.getTopicId());
	}

	private void requireUser() throws ExamBaseException {
		ExamUser examUser = SecurityContextService.getUser();
		Assert.notNull(examUser, ErrorInfo.ACCESS_DENIED_ERROR);
	}
}
