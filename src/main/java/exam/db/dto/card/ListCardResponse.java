package exam.db.dto.card;

import exam.db.dto.TotalResponse;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class ListCardResponse extends TotalResponse {
	private List<CardResponse> cards = new ArrayList<>();

	public ListCardResponse(List<CardResponse> cards, long total) {
		this.cards = cards;
		super.setTotal(total);
	}
}
