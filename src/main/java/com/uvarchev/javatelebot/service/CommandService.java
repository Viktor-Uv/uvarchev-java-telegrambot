package com.uvarchev.javatelebot.service;

import com.uvarchev.javatelebot.bot.command.*;
import com.uvarchev.javatelebot.entity.User;
import com.uvarchev.javatelebot.enums.CommandType;
import com.uvarchev.javatelebot.enums.UserRole;
import com.uvarchev.javatelebot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class CommandService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private SubscriptionService subscriptionService;

    /**
     * Processes a start command and responds by activating a returning user or creating a new one.
     *
     * @param command the start command issued by the user
     * @return a reply String that greets the user and indicates whether they are new or returning
     * @Usage: /start
     */
    public String processAndRespond(StartCommand command) {
        return userService.activateUser(
                command,
                findUserById(command.getUserId())
        );
    }

    /**
     * Processes a stop command and deactivates the user account if the user is authorised.
     *
     * @param command the stop command issued by the user
     * @return the result of the deactivation as a string,
     * or an error message if the user is not authorised or the account does not exist
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
     * Processes a subscribe command and adds a subscription for the user
     * to one or more news providers if the user is authorised.
     *
     * @param command the subscribe command issued by the user
     * @return the result of the subscription as a string,
     * or an error message if the user is not authorised or no options are provided
     * @Usage: /subscribe [provider || ALL]
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
     * Processes an unsubscribe command and deactivates the subscription for the user
     * to one or more news providers if the user is authorised.
     *
     * @param command the unsubscribe command issued by the user
     * @return the result of the deactivation as a string,
     * or an error message if the user is not authorised or the subscription does not exist
     * @Usage: /unsubscribe [provider || ALL]
     */
    public String processAndRespond(UnsubscribeCommand command) {
        return processIfAuthorised(
                command.getUserId(),
                command.getUserName(),
                command.getType(),
                user -> subscriptionService.deactivateSubscription(command, user)
        );
    }

    /**
     * Lists all active subscriptions if the user is authorised.
     *
     * @param command the command issued by the user
     * @return a response String if the user is authorised, or an error message otherwise
     * @Usage: /subscriptions
     */
    public String processAndRespond(SubscriptionsCommand command) {
        return processIfAuthorised(
                command.getUserId(),
                command.getUserName(),
                command.getType(),
                user -> subscriptionService.listSubscriptions(command, user)
        );
    }

    /**
     * Processes the given command and returns a response String if the user is an admin,
     * or an error message otherwise.
     *
     * @param command the command to be processed
     * @return a response String if the user is an admin, or an error message otherwise
     * @Usage: /statistics
     */
    public String processAndRespond(StatisticsCommand command) {
        return processIfAuthorised(
                command.getUserId(),
                command.getUserName(),
                command.getType(),
                user -> userService.getAdminStatistics()
        );
    }

    /**
     * Processes the given Unrecognised command and returns a response String with the
     * available commands for the given user role.
     *
     * @param command the command to be processed
     * @return a response String with the available commands for the requesting user
     * @Usage: ANY_OTHER_COMMAND || request from unauthorised User or User with insufficient rights
     */
    public String processAndRespond(UnrecognisedCommand command) {
        // Get requesting user's UserRole
        UserRole userRole = getUserRoleByUserId(command.getUserId());

        // Load all commands available to this user based on the UserRole
        String availableCommands = getCommandsForUserRole(userRole);

        // Create and return user-specific response
        return composeUnrecognisedCommandMessage(command.getUserName(), availableCommands);
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
     * Returns the user role of the user with the given user ID.
     * If the user ID is not found in the database, returns {@link UserRole} UNAUTHORISED.
     *
     * @param userId the user ID to be searched in the database
     * @return the user role of the user with the given user ID, or UNAUTHORISED if not found
     */
    private UserRole getUserRoleByUserId(Long userId) {
        // Try to get requested user from DB
        User user = findUserById(userId);

        // If no user was found - return UNAUTHORISED, or the found user's role otherwise
        if (user == null) {
            return UserRole.UNAUTHORISED;
        } else {
            return user.getUserRole();
        }
    }

    /**
     * Returns a String with the available commands in lowercase prefixed
     * with "/" and separated by commas for the given user role.
     *
     * @param userRole the user role whose commands are to be returned
     * @return a String with the available commands for the user role
     */
    private String getCommandsForUserRole(UserRole userRole) {
        return "/" + Arrays.stream(CommandType.values())
                .filter(commandType -> hasAccess(userRole, commandType))
                .map(commandType -> commandType.name().toLowerCase())
                .collect(Collectors.joining(", /"));
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

    /**
     * Returns a String with an error message for an unrecognised command.
     *
     * @param userName          the name of the user who entered the unrecognised command
     * @param availableCommands the String with the available commands for the user
     * @return a String with an error message for the unrecognised command
     */
    private String composeUnrecognisedCommandMessage(String userName, String availableCommands) {
        return "Unfortunately, " + userName +
                ", this command was not recognised.\n" +
                "Currently available commands are: "
                + availableCommands + ".";
    }

}
