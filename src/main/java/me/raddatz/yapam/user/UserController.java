package me.raddatz.yapam.user;

import me.raddatz.yapam.common.annotation.userexists.UserExists;
import me.raddatz.yapam.user.model.request.PasswordChangeRequest;
import me.raddatz.yapam.user.model.request.UserRequest;
import me.raddatz.yapam.user.model.response.SimpleUserResponse;
import me.raddatz.yapam.user.model.response.UserResponse;
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
    @UserExists
    public void verifyEmail(@PathVariable(value = "userId") String userId,
                            @RequestParam(value = "token") String token) {
        userService.verifyEmail(userId, token);
    }

    @GetMapping(value = "/api/users/{userId}/email/requestChange")
    @UserExists
    public void requestEmailChange(@PathVariable(value = "userId") String userId,
                                   @RequestParam(value = "email") String email) {
        userService.requestEmailChange(userId, email);
    }

    @GetMapping(value = "/api/users/{userId}/email/change")
    @UserExists
    public void emailChange(@PathVariable(value = "userId") String userId,
                            @RequestParam(value = "token") String token,
                            @RequestParam(value = "email") String email) {
        userService.emailChange(userId, token, email);
    }

    @PutMapping(value = "/api/users/password/change", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void updatePassword(@RequestBody PasswordChangeRequest passwordChangeRequest) {
        userService.passwordChange(passwordChangeRequest);
    }

    @GetMapping(value = "/api/users")
    public Set<SimpleUserResponse> getAllUsers() {
        return userService.getAllUsers();
    }
}