package exam.db.repository.user;

import exam.db.entity.UserRecovery;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRecoveryRepository extends MongoRepository<UserRecovery, String> {
	UserRecovery findByResetCodeAndDeletedIsFalse(String resetCode);

	List<UserRecovery> findAllByUserIdAndDeletedIsFalse(String userId);
}
