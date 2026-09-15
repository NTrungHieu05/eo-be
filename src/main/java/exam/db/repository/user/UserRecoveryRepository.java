package exam.db.repository.user;

import exam.db.entity.UserRecovery;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRecoveryRepository extends MongoRepository<UserRecovery, String> {
	UserRecovery findByResetCodeAndDeletedIsFalse(String resetCode);
}
