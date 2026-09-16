package exam.db.dto.user;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;

@Data
@NoArgsConstructor
public class BlockUserRequest {
	private Collection<String> ids;
}
