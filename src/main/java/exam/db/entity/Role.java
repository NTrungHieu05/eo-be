package exam.db.entity;

import exam.db.enums.CollectionConst;
import exam.db.enums.FieldConst;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.security.core.GrantedAuthority;

@Document(collection = CollectionConst.ROLE)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Role extends AbstractVersion implements GrantedAuthority {
	private static final long serialVersionUID = 0L;

	@Id
	@Getter
	@Setter
	private String id;

	@Indexed(unique = true)
	@Getter
	@Setter
	@Field(FieldConst.NAME)
	private String name;

	public Role(String name) {
		this.name = name;
	}

	@Override
	public String getAuthority() {
		return name;
	}
}
