package exam.db.dto.skill;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UpdateSkillRequest extends CreateSkillRequest {
	private String id;
}
