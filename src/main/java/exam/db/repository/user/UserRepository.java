package exam.db.repository.user;

import exam.db.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
	void deleteByEmail(String email);

	User findByUserId(String userId);

	User findByUserIdAndDeletedIsFalse(String userId);

	User findByEmailIgnoreCase(String email);

	List<User> findByUserIdIn(List<String> userIds);

	@Query("{'$or':[ {'username':?0}, {'email':?0} ] }")
	Optional<User> findByUserNameOrEmail(String username);

	Optional<User> findByPhone(String phone);
}
