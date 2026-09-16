package exam.db.dto.card;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class DeleteCardRequest {
	private List<String> ids;
}
