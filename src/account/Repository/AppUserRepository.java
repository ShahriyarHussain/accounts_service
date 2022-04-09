package account.Repository;

import account.Model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    boolean existsAppUserByEmailIgnoreCase(String email);

    Optional<AppUser> findAppUserByEmailIgnoreCase(String email);

    void delete(AppUser entity);
}
