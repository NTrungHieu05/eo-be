package exam.db.dto.card;

import com.fasterxml.jackson.annotation.JsonGetter;
import exam.db.entity.Answer;
import exam.db.entity.Card;
import exam.db.entity.Question;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.util.List;

@Data
@NoArgsConstructor
public class CardResponse {
	private String id;
	private Question question;
	private Answer answer;
	private Boolean isQuestionGroup;
	private List<Card> childCards;
	private String examId;
	private String topicId;

	public CardResponse(Card card) {
		BeanUtils.copyProperties(card, this);
	}

	@JsonGetter("_id")
	public String getJsonId() {
		return id;
	}
}
