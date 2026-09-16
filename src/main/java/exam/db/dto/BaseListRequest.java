package exam.db.dto;

import exam.db.enums.DBConst;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

import java.util.Optional;

@Data
@NoArgsConstructor
public class BaseListRequest {
	private String orderBy;
	private String orderDirection;
	private Integer pageIndex;
	private Integer pageSize;

	public Integer getPageSize() {
		if (this.pageSize == null) {
			return DBConst.DEFAULT_PAGE_SIZE;
		}
		if (this.pageSize == -1) {
			return DBConst.DEFAULT_MAX_PAGE_SIZE;
		}
		return this.pageSize;
	}

	public Pageable getPageableWithSort() {
		int page = Optional.ofNullable(this.pageIndex).orElse(0);
		if (this.orderBy == null) {
			return PageRequest.of(page, getPageSize());
		}
		Sort sort = Sort.by(
				DBConst.DIRECTION_DESC.equalsIgnoreCase(this.orderDirection) ? Direction.DESC : Direction.ASC,
				this.orderBy);
		return PageRequest.of(page, getPageSize(), sort);
	}
}
