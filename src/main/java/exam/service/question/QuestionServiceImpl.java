package exam.service.question;

import exam.db.dto.question.QuestionResponse;
import exam.db.dto.question.CreateQuestionRequest;
import exam.db.dto.question.DeleteQuestionRequest;
import exam.db.dto.question.ImportQuestionRequest;
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
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuestionServiceImpl implements QuestionService {

	@Value("${app.upload.root:D:/DATN/eo-be/data}")
	private String uploadRoot;

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

	@Override
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public Boolean importQuestion(ImportQuestionRequest request) throws ExamBaseException {
		requireUser();
		Assert.notNull(request, ErrorInfo.BAD_REQUEST);
		Assert.notEmpty(request.getFilePath(), ErrorInfo.BAD_REQUEST);
		File file = resolveImportFile(request.getFilePath());
		if (file == null || !file.isFile()) {
			throw new ExamBaseException(ErrorInfo.BAD_REQUEST);
		}
		int saved = 0;
		try (FileInputStream inputStream = new FileInputStream(file);
			 Workbook workbook = new XSSFWorkbook(inputStream)) {
			int totalSheet = workbook.getNumberOfSheets();
			for (int i = 0; i < totalSheet; i++) {
				Sheet sheet = workbook.getSheetAt(i);
				for (Row row : sheet) {
					if (row == null || isHeaderRow(row)) {
						continue;
					}
					Question question = createQuestionFromExcelRow(row);
					if (!hasQuestionText(question) || !hasCorrectAnswer(question) || !StringUtils.hasText(question.getTopicId())) {
						continue;
					}
					Topic topic = topicRepo.findFirstByIdAndDeletedIsFalse(question.getTopicId());
					if (topic == null) {
						continue;
					}
					questionRepo.save(question);
					linkQuestionToExam(question);
					saved++;
				}
			}
		} catch (IOException e) {
			throw new ExamBaseException(ErrorInfo.BAD_REQUEST);
		}
		if (saved == 0) {
			throw new ExamBaseException(ErrorInfo.BAD_REQUEST);
		}
		return Boolean.TRUE;
	}

	private File resolveImportFile(String filePath) {
		Path root = Paths.get(uploadRoot).toAbsolutePath().normalize();
		Path direct = Paths.get(filePath).toAbsolutePath().normalize();
		if (direct.startsWith(root) && direct.toFile().isFile()) {
			return direct.toFile();
		}
		String fileName = Paths.get(filePath).getFileName().toString();
		String[] folders = {"", "files", "images", "sounds"};
		for (String folder : folders) {
			Path candidate = folder.isEmpty() ? root.resolve(fileName) : root.resolve(folder).resolve(fileName);
			if (candidate.toFile().isFile()) {
				return candidate.toFile();
			}
		}
		return null;
	}

	private boolean isHeaderRow(Row row) {
		String first = cellText(row.getCell(0)).toLowerCase();
		return first.equals("choice") || first.equals("choices")
				|| first.equals("answer") || first.equals("answers")
				|| first.equals("text");
	}

	private boolean hasCorrectAnswer(Question question) {
		if (question.getChildQuestions() != null && !question.getChildQuestions().isEmpty()) {
			for (Question child : question.getChildQuestions()) {
				if (!hasTextAnswer(child)) {
					return false;
				}
			}
			return true;
		}
		return hasTextAnswer(question);
	}

	private boolean hasTextAnswer(Question question) {
		if (question == null || question.getAnswer() == null || question.getAnswer().getTexts() == null) {
			return false;
		}
		for (String text : question.getAnswer().getTexts()) {
			if (StringUtils.hasText(text)) {
				return true;
			}
		}
		return false;
	}

	private boolean hasQuestionText(Question question) {
		if (question.getQuestion() != null && StringUtils.hasText(question.getQuestion().getText())) {
			return true;
		}
		if (question.getChildQuestions() == null) {
			return false;
		}
		for (Question child : question.getChildQuestions()) {
			if (child.getQuestion() != null && StringUtils.hasText(child.getQuestion().getText())) {
				return true;
			}
		}
		return false;
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

	private void linkQuestionToExam(Question question) {
		if (!StringUtils.hasText(question.getExamId())) {
			return;
		}
		Exam exam = examRepo.findFirstByIdAndDeletedIsFalse(question.getExamId());
		if (exam == null || !canPutQuestionInExam(question, exam)) {
			return;
		}
		List<String> questionIds = exam.getQuestionIds() == null ? new ArrayList<>() : new ArrayList<>(exam.getQuestionIds());
		if (!questionIds.contains(question.getId())) {
			questionIds.add(question.getId());
			exam.setQuestionIds(questionIds);
			examRepo.save(exam);
		}
	}

	private boolean canPutQuestionInExam(Question question, Exam exam) {
		if (Boolean.TRUE.equals(exam.getPracticeTest()) || !StringUtils.hasText(exam.getTopicId())) {
			return true;
		}
		return exam.getTopicId().equals(question.getTopicId());
	}

	private Question createQuestionFromExcelRow(Row row) {
		Question question = new Question();
		QuestionContent content = new QuestionContent();
		Answer answer = new Answer();
		int cellIndex = 0;

		answer.setChoices(cellList(row.getCell(cellIndex++)));
		answer.setHint(cellText(row.getCell(cellIndex++)));
		answer.setImage(cellText(row.getCell(cellIndex++)));
		answer.setTexts(cellList(row.getCell(cellIndex++)));

		content.setHint(cellText(row.getCell(cellIndex++)));
		content.setImage(cellText(row.getCell(cellIndex++)));
		content.setSound(cellText(row.getCell(cellIndex++)));
		content.setText(cellText(row.getCell(cellIndex++)));

		String examId = cellText(row.getCell(cellIndex++));
		String topicId = cellText(row.getCell(cellIndex++));

		List<Question> childQuestions = new ArrayList<>();
		for (int i = 0; i < 3; i++) {
			Answer childAnswer = new Answer();
			QuestionContent childQuestion = new QuestionContent();
			childAnswer.setChoices(cellList(row.getCell(cellIndex++)));
			childAnswer.setHint(cellText(row.getCell(cellIndex++)));
			childAnswer.setImage(cellText(row.getCell(cellIndex++)));
			childAnswer.setTexts(cellList(row.getCell(cellIndex++)));
			childQuestion.setHint(cellText(row.getCell(cellIndex++)));
			childQuestion.setImage(cellText(row.getCell(cellIndex++)));
			childQuestion.setSound(cellText(row.getCell(cellIndex++)));
			childQuestion.setText(cellText(row.getCell(cellIndex++)));
			if (!StringUtils.hasText(childQuestion.getText())) {
				continue;
			}
			Question child = new Question();
			child.setQuestion(childQuestion);
			child.setAnswer(childAnswer);
			child.setIsQuestionGroup(false);
			childQuestions.add(child);
		}

		question.setQuestion(content);
		question.setAnswer(answer);
		question.setExamId(examId);
		question.setTopicId(topicId);
		question.setChildQuestions(childQuestions);
		question.setIsQuestionGroup(!childQuestions.isEmpty());
		return question;
	}

	private final DataFormatter cellFormatter = new DataFormatter();

	private String cellText(Cell cell) {
		if (cell == null) {
			return "";
		}
		return cellFormatter.formatCellValue(cell).trim();
	}

	private List<String> cellList(Cell cell) {
		List<String> list = new ArrayList<>();
		String cellValue = cellText(cell);
		if (!StringUtils.hasText(cellValue)) {
			return list;
		}
		String[] values = cellValue.split("[;|\\n\\r]+");
		for (String value : values) {
			if (StringUtils.hasText(value)) {
				list.add(value.trim());
			}
		}
		return list;
	}

	private void requireUser() throws ExamBaseException {
		ExamUser examUser = SecurityContextService.getUser();
		Assert.notNull(examUser, ErrorInfo.ACCESS_DENIED_ERROR);
	}
}
