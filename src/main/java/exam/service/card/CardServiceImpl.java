package exam.service.card;

import exam.db.dto.card.CardResponse;
import exam.db.dto.card.CreateCardRequest;
import exam.db.dto.card.DeleteCardRequest;
import exam.db.dto.card.ImportCardRequest;
import exam.db.dto.card.ListCardRequest;
import exam.db.dto.card.ListCardResponse;
import exam.db.dto.card.UpdateCardRequest;
import exam.db.dto.user.ExamUser;
import exam.db.entity.Answer;
import exam.db.entity.Card;
import exam.db.entity.Exam;
import exam.db.entity.Question;
import exam.db.entity.Topic;
import exam.db.repository.topic.TopicRepository;
import exam.db.enums.Assert;
import exam.db.enums.ErrorInfo;
import exam.db.repository.card.CardRepository;
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
public class CardServiceImpl implements CardService {

	@Value("${app.upload.root:D:/DATN/eo-be/data}")
	private String uploadRoot;

	@Autowired
	private CardRepository cardRepo;

	@Autowired
	private ExamRepository examRepo;

	@Autowired
	private TopicRepository topicRepo;

	@Override
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public CardResponse create(CreateCardRequest request) throws ExamBaseException {
		requireUser();
		Assert.notEmpty(request.getTopicId(), ErrorInfo.TOPIC_NOT_FOUND_ERROR);
		Topic topic = topicRepo.findFirstByIdAndDeletedIsFalse(request.getTopicId());
		Assert.notNull(topic, ErrorInfo.TOPIC_NOT_FOUND_ERROR);
		Card card = new Card();
		BeanUtils.copyProperties(request, card);
		card.setTopicId(request.getTopicId());
		cardRepo.save(card);
		if (StringUtils.hasText(request.getExamId())) {
			Exam exam = examRepo.findFirstByIdAndDeletedIsFalse(request.getExamId());
			if (exam != null && canPutCardInExam(card, exam)) {
				List<String> cardIds = exam.getCardIds() == null ? new ArrayList<>() : new ArrayList<>(exam.getCardIds());
				cardIds.add(card.getId());
				exam.setCardIds(cardIds);
				examRepo.save(exam);
			}
		}
		return new CardResponse(card);
	}

	@Override
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public CardResponse update(UpdateCardRequest request) throws ExamBaseException {
		requireUser();
		Card card = cardRepo.findFirstByIdAndDeletedIsFalse(request.getId());
		Assert.notNull(card, ErrorInfo.CARD_IS_NULL);
		String topicId = StringUtils.hasText(request.getTopicId()) ? request.getTopicId() : card.getTopicId();
		Assert.notEmpty(topicId, ErrorInfo.TOPIC_NOT_FOUND_ERROR);
		Topic topic = topicRepo.findFirstByIdAndDeletedIsFalse(topicId);
		Assert.notNull(topic, ErrorInfo.TOPIC_NOT_FOUND_ERROR);
		String examId = card.getExamId();
		Answer answer = card.getAnswer();
		Question question = card.getQuestion();
		List<Card> childCards = card.getChildCards();
		BeanUtils.copyProperties(request, card);
		card.setId(request.getId());
		card.setTopicId(topicId);
		if (!StringUtils.hasText(request.getExamId())) {
			card.setExamId(examId);
		}
		if (request.getQuestion() == null) {
			card.setQuestion(question);
		}
		if (request.getAnswer() == null) {
			card.setAnswer(answer);
		}
		if (Boolean.FALSE.equals(request.getIsQuestionGroup())) {
			card.setChildCards(new ArrayList<>());
		} else if (request.getChildCards() == null) {
			card.setChildCards(childCards);
		}
		cardRepo.save(card);
		return new CardResponse(card);
	}

	@Override
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public Boolean deleteMultiple(DeleteCardRequest request) throws ExamBaseException {
		requireUser();
		if (request == null || request.getIds() == null) {
			throw new ExamBaseException(ErrorInfo.BAD_REQUEST);
		}
		List<Card> cardList = cardRepo.findByIdInAndDeletedIsFalse(request.getIds());
		cardList.forEach(p -> p.setDeleted(true));
		cardRepo.saveAll(cardList);
		detachCardsFromExams(request.getIds());
		return Boolean.TRUE;
	}

	@Override
	public CardResponse getDetail(String id) throws ExamBaseException {
		requireUser();
		Card card = cardRepo.findFirstByIdAndDeletedIsFalse(id);
		Assert.notNull(card, ErrorInfo.CARD_IS_NULL);
		return new CardResponse(card);
	}

	@Override
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public ListCardResponse getListCardForAdmin(ListCardRequest request) throws ExamBaseException {
		requireUser();
		List<CardResponse> cards = cardRepo.findAllByDeletedIsFalse().stream()
				.map(CardResponse::new)
				.collect(Collectors.toList());
		return new ListCardResponse(cards, cards.size());
	}

	@Override
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public Boolean importCard(ImportCardRequest request) throws ExamBaseException {
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
					Card card = createCardFromExcelRow(row);
					if (!hasQuestionText(card) || !hasCorrectAnswer(card) || !StringUtils.hasText(card.getTopicId())) {
						continue;
					}
					Topic topic = topicRepo.findFirstByIdAndDeletedIsFalse(card.getTopicId());
					if (topic == null) {
						continue;
					}
					cardRepo.save(card);
					linkCardToExam(card);
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

	private boolean hasCorrectAnswer(Card card) {
		if (card.getChildCards() != null && !card.getChildCards().isEmpty()) {
			for (Card child : card.getChildCards()) {
				if (!hasTextAnswer(child)) {
					return false;
				}
			}
			return true;
		}
		return hasTextAnswer(card);
	}

	private boolean hasTextAnswer(Card card) {
		if (card == null || card.getAnswer() == null || card.getAnswer().getTexts() == null) {
			return false;
		}
		for (String text : card.getAnswer().getTexts()) {
			if (StringUtils.hasText(text)) {
				return true;
			}
		}
		return false;
	}

	private boolean hasQuestionText(Card card) {
		if (card.getQuestion() != null && StringUtils.hasText(card.getQuestion().getText())) {
			return true;
		}
		if (card.getChildCards() == null) {
			return false;
		}
		for (Card child : card.getChildCards()) {
			if (child.getQuestion() != null && StringUtils.hasText(child.getQuestion().getText())) {
				return true;
			}
		}
		return false;
	}

	private void detachCardsFromExams(List<String> cardIds) {
		if (cardIds == null || cardIds.isEmpty()) {
			return;
		}
		List<Exam> exams = examRepo.findAllByDeletedIsFalse();
		if (exams == null || exams.isEmpty()) {
			return;
		}
		for (Exam exam : exams) {
			if (exam.getCardIds() == null || exam.getCardIds().isEmpty()) {
				continue;
			}
			List<String> next = new ArrayList<>(exam.getCardIds());
			if (next.removeAll(cardIds)) {
				exam.setCardIds(next);
				examRepo.save(exam);
			}
		}
	}

	private void linkCardToExam(Card card) {
		if (!StringUtils.hasText(card.getExamId())) {
			return;
		}
		Exam exam = examRepo.findFirstByIdAndDeletedIsFalse(card.getExamId());
		if (exam == null || !canPutCardInExam(card, exam)) {
			return;
		}
		List<String> cardIds = exam.getCardIds() == null ? new ArrayList<>() : new ArrayList<>(exam.getCardIds());
		if (!cardIds.contains(card.getId())) {
			cardIds.add(card.getId());
			exam.setCardIds(cardIds);
			examRepo.save(exam);
		}
	}

	private boolean canPutCardInExam(Card card, Exam exam) {
		if (Boolean.TRUE.equals(exam.getPracticeTest()) || !StringUtils.hasText(exam.getTopicId())) {
			return true;
		}
		return exam.getTopicId().equals(card.getTopicId());
	}

	private Card createCardFromExcelRow(Row row) {
		Card card = new Card();
		Question question = new Question();
		Answer answer = new Answer();
		int cellIndex = 0;

		answer.setChoices(cellList(row.getCell(cellIndex++)));
		answer.setHint(cellText(row.getCell(cellIndex++)));
		answer.setImage(cellText(row.getCell(cellIndex++)));
		answer.setTexts(cellList(row.getCell(cellIndex++)));

		question.setHint(cellText(row.getCell(cellIndex++)));
		question.setImage(cellText(row.getCell(cellIndex++)));
		question.setSound(cellText(row.getCell(cellIndex++)));
		question.setText(cellText(row.getCell(cellIndex++)));

		String examId = cellText(row.getCell(cellIndex++));
		String topicId = cellText(row.getCell(cellIndex++));

		List<Card> childCards = new ArrayList<>();
		for (int i = 0; i < 3; i++) {
			Answer childAnswer = new Answer();
			Question childQuestion = new Question();
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
			Card child = new Card();
			child.setQuestion(childQuestion);
			child.setAnswer(childAnswer);
			child.setIsQuestionGroup(false);
			childCards.add(child);
		}

		card.setQuestion(question);
		card.setAnswer(answer);
		card.setExamId(examId);
		card.setTopicId(topicId);
		card.setChildCards(childCards);
		card.setIsQuestionGroup(!childCards.isEmpty());
		return card;
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
