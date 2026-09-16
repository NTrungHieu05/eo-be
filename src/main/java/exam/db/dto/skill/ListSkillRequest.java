package exam.db.dto.skill;

import exam.db.dto.BaseListRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ListSkillRequest extends BaseListRequest {
	private String keyword;
}
