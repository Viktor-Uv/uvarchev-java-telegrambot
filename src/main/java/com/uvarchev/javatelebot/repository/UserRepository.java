package com.uvarchev.javatelebot.repository;

import com.uvarchev.javatelebot.entity.User;
import com.uvarchev.javatelebot.enums.UserRole;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {

    /**
     * Deactivates a user by setting their user role to UNAUTHORISED in the database.
     * This method uses a custom query with the @Query annotation and the @Modifying annotation
     * to indicate that it performs an update operation. It also uses the @Transactional annotation
     * to mark the method as transactional and roll back in case of any failure.
     *
     * @param userId the id of the user to deactivate
     * @return the number of rows affected by the update query
     */
    @Transactional
    @Modifying
    @Query(
            value = "UPDATE User u " +
                    " SET u.userRole = 'UNAUTHORISED' " +
                    " WHERE u.telegramId = :userId"
    )
    int deactivateUserByUserId(Long userId);

    /**
     * Returns the number of users in the database whose user role is not equal to the given user role.
     *
     * @param userRole the user role to be excluded from the count
     * @return the number of users with a different user role than the given one
     */
    int countByUserRoleIsNot(UserRole userRole);

    /**
     * Returns the total number of articles received by all users.
     *
     * @return a long value representing the sum of articles received
     */
    @Query(
            value = "SELECT sum(u.articlesReceived) " +
                    " FROM User u "
    )
    long getTotalOfArticlesReceived();

}
