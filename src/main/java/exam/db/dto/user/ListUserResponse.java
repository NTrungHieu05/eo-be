package exam.db.dto.user;

import exam.db.dto.TotalResponse;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class ListUserResponse extends TotalResponse {
	private List<UserResponse> data = new ArrayList<>();

	public ListUserResponse(List<UserResponse> data, long total) {
		this.data = data;
		super.setTotal(total);
	}
}
