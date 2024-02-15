package com.uvarchev.javatelebot.repository;

import com.uvarchev.javatelebot.entity.User;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {

    @Transactional
    @Modifying
    @Query(
            value = "UPDATE User u " +
                    " SET u.isActive = false, " +
                    " u.userRole = 'GUEST'" +
                    " WHERE u.telegramId = :userId"
    )
    int deactivateById(Long userId);
}
