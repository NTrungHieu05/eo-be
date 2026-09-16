package exam.db.dto.skill;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class DeleteSkillRequest {
	private List<String> ids;
}
