package exam.service.card;

import exam.db.dto.card.CardResponse;
import exam.db.dto.card.CreateCardRequest;
import exam.db.dto.card.DeleteCardRequest;
import exam.db.dto.card.ListCardRequest;
import exam.db.dto.card.ListCardResponse;
import exam.db.dto.card.UpdateCardRequest;
import exam.db.dto.user.ExamUser;
import exam.db.entity.Card;
import exam.db.entity.Exam;
import exam.db.enums.Assert;
import exam.db.enums.ErrorInfo;
import exam.db.repository.card.CardRepository;
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
public class CardServiceImpl implements CardService {

	@Autowired
	private CardRepository cardRepo;

	@Autowired
	private ExamRepository examRepo;

	@Override
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public CardResponse create(CreateCardRequest request) throws ExamBaseException {
		requireUser();
		Card card = new Card();
		BeanUtils.copyProperties(request, card);
		cardRepo.save(card);
		if (StringUtils.hasText(request.getExamId())) {
			Exam exam = examRepo.findFirstByIdAndDeletedIsFalse(request.getExamId());
			if (exam != null) {
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
		BeanUtils.copyProperties(request, card);
		cardRepo.save(card);
		return new CardResponse(card);
	}

	@Override
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public Boolean deleteMultiple(DeleteCardRequest request) throws ExamBaseException {
		requireUser();
		List<Card> cardList = cardRepo.findByIdInAndDeletedIsFalse(request.getIds());
		cardList.forEach(p -> p.setDeleted(true));
		cardRepo.saveAll(cardList);
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

	private void requireUser() throws ExamBaseException {
		ExamUser examUser = SecurityContextService.getUser();
		Assert.notNull(examUser, ErrorInfo.ACCESS_DENIED_ERROR);
	}
}
