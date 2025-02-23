package app.user.repository;

import app.user.model.User;
import app.user.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsernameOrAddress(String username, String address);
    Optional<User> findByUsername(String username);
    List<User> findAllByRole(UserRole role);
}
