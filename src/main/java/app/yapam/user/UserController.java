package app.yapam.user;

import app.yapam.user.model.request.PasswordChangeRequest;
import app.yapam.user.model.request.UserRequest;
import app.yapam.user.model.response.SimpleUserResponse;
import app.yapam.user.model.response.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
public class UserController {

    @Autowired private UserService userService;

    @PostMapping(value = "/api/users", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public UserResponse createUser(@RequestBody UserRequest user) {
        return userService.createUser(user);
    }

    @GetMapping(value = "/api/users/{userId}/email/verify")
    public void verifyEmail(@PathVariable("userId") String userId,
                            @RequestParam(value = "token") String token) {
        userService.verifyEmail(userId, token);
    }

    @GetMapping(value = "/api/users/currentuser/email/request-change")
    public void requestEmailChange(@RequestParam(value = "email") String email) {
        userService.requestEmailChange(email);
    }

    @GetMapping(value = "/api/users/{userId}/email/change")
    public void emailChange(@PathVariable("userId") String userId,
                            @RequestParam(value = "token") String token,
                            @RequestParam(value = "email") String email) {
        userService.emailChange(userId, token, email);
    }

    @PutMapping(value = "/api/users/currentuser/password/change", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void updatePassword(@RequestBody PasswordChangeRequest passwordChangeRequest) {
        userService.passwordChange(passwordChangeRequest);
    }

    @GetMapping(value = "/api/users")
    public Set<SimpleUserResponse> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping(value = "/api/users/{userId}")
    public SimpleUserResponse getUserById(@PathVariable("userId") String userId) {
        return userService.getSimpleUserById(userId);
    }

    @GetMapping(value = "/api/users/currentuser")
    public UserResponse getCurrentUser() {
        return userService.getCurrentUser();
    }
}