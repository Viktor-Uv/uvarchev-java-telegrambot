package com.uvarchev.javatelebot.service;

import com.uvarchev.javatelebot.bot.command.*;
import com.uvarchev.javatelebot.entity.User;
import com.uvarchev.javatelebot.enums.CommandType;
import com.uvarchev.javatelebot.enums.UserRole;
import com.uvarchev.javatelebot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Function;

@Service
@Transactional
public class CommandHandler {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private SubscriptionService subscriptionService;

    /**
     * Registers new user or reactivates guest
     *
     * @Usage: /start
     */
    public String processAndRespond(StartCommand command) {
        return userService.activateUser(
                command,
                findUserById(command.getUserId())
        );
    }

    /**
     * Deactivates leaving user by lowering its UserRole to guest
     *
     * @Usage: /stop
     */
    public String processAndRespond(StopCommand command) {
        return processIfAuthorised(
                command.getUserId(),
                command.getUserName(),
                command.getType(),
                user -> userService.deactivateUser(command)
        );
    }

    /**
     * Adds subscription for a user
     *
     * @Usage: /subscribe [provider]
     */
    public String processAndRespond(SubscribeCommand command) {
        return processIfAuthorised(
                command.getUserId(),
                command.getUserName(),
                command.getType(),
                user -> subscriptionService.addSubscription(command, user)
        );
    }

    /**
     * Deactivates subscription with the given ID
     *
     * @Usage: /unsubscribe [subscription_id]
     */
    public String processAndRespond(UnsubscribeCommand command) {
        // TODO Unsubscribe command logic
        return command.getMsgText();
    }

    /**
     * Lists all active subscriptions
     *
     * @Usage: /subscriptions
     */
    public String processAndRespond(SubscriptionsCommand command) {
        // TODO List Subscriptions command logic
        return command.getMsgText();
    }

    /**
     * Shows application statistics for Administrators
     *
     * @Usage: /statistics
     */
    public String processAndRespond(StatisticsCommand command) {
        // TODO Statistics for administrators logic
        return command.getMsgText();
    }

    /**
     * Informs that command wasn't recognised, lists all available commands
     *
     * @Usage: ANY_OTHER_COMMAND
     */
    public String processAndRespond(UnrecognisedCommand command) {
        // TODO Unrecognised Command logic
        return "UNRECOGNISED";
    }

    /**
     * Processes a command if the user is authorised to execute it.
     *
     * @param userId    the ID of the user who initiated the command
     * @param userName  the name of the user who initiated the command
     * @param command   the type of the command to be executed
     * @param operation the function that performs the operation corresponding to the command
     * @return the result of the operation as a string,
     * or an error message if the user is not authorised or does not exist
     */
    private String processIfAuthorised(
            Long userId,
            String userName,
            CommandType command,
            Function<User, String> operation
    ) {
        // Try to get requested user from DB
        User user = findUserById(userId);

        // Check if the user exists and has access to the command type
        if (user != null && hasAccess(user.getUserRole(), command)) {
            // Proceed performing requested operation
            return operation.apply(user);
        } else {
            // Reroute to the UnrecognisedCommand method
            return processAndRespond(
                    new UnrecognisedCommand(userName, userId)
            );
        }
    }

    /**
     * Finds a user by their id in the database.
     *
     * @param userId the id of the user to find
     * @return the user object if found, or null otherwise
     */
    private User findUserById(Long userId) {
        return userRepository
                .findById(userId)
                .orElse(null);
    }

    /**
     * Checks if a user has sufficient access level to execute a command.
     *
     * @return true if the user access level is equal or higher than the command required access level,
     * false otherwise
     */
    private boolean hasAccess(UserRole user, CommandType command) {
        return user.getAccessLevel() >= command.getRequiredAccessLevel();
    }

}
