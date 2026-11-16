package exam.db.entity;

import exam.db.enums.CollectionConst;
import exam.db.enums.FieldConst;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = CollectionConst.USER_RECOVERY)
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class UserRecovery extends AbstractVersion {
	@Id
	protected String id;

	@Field(FieldConst.USER_ID)
	protected String userId;

	@Field(FieldConst.EMAIL)
	protected String email;

	@Field(FieldConst.RESET_CODE)
	protected String resetCode;
}
