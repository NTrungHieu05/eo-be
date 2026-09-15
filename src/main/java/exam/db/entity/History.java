package exam.db.entity;

import exam.db.enums.CollectionConst;
import exam.db.enums.FieldConst;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Document(collection = CollectionConst.HISTORY)
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class History extends AbstractVersion {
	@Id
	private String id;

	@Field(FieldConst.USER_ID)
	private String userId;

	@Field(FieldConst.STATUS)
	private Integer status;

	@Field(FieldConst.EXAM_RESULTS)
	private List<ExamResult> examResults;
}
