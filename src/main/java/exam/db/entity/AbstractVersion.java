package exam.db.entity;

import exam.db.enums.FieldConst;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;

@Data
public abstract class AbstractVersion {
	protected String version = "1.0";

	@Field(FieldConst.CREATED_DATE)
	@CreatedDate
	private Date createdDate;

	@Field(FieldConst.LAST_MODIFIED_DATE)
	@LastModifiedDate
	private Date lastModifiedDate;

	@Field(FieldConst.DELETED)
	private boolean deleted = false;

	@Field(FieldConst.DELETED_DATE)
	private Date deletedDate;
}
