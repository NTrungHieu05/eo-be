package exam.service.card;

import exam.db.dto.card.CardResponse;
import exam.db.dto.card.CreateCardRequest;
import exam.db.dto.card.DeleteCardRequest;
import exam.db.dto.card.ListCardRequest;
import exam.db.dto.card.ListCardResponse;
import exam.db.dto.card.UpdateCardRequest;
import exam.ultis.ExamBaseException;

public interface CardService {
	CardResponse create(CreateCardRequest request) throws ExamBaseException;

	CardResponse update(UpdateCardRequest request) throws ExamBaseException;

	Boolean deleteMultiple(DeleteCardRequest request) throws ExamBaseException;

	CardResponse getDetail(String id) throws ExamBaseException;

	ListCardResponse getListCardForAdmin(ListCardRequest request) throws ExamBaseException;
}
