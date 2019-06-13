package app.yapam.user;

import app.yapam.common.annotation.userexists.UserExists;
import app.yapam.user.model.request.PasswordChangeRequest;
import app.yapam.user.model.response.SimpleUserResponse;
import app.yapam.user.model.response.UserResponse;
import app.yapam.user.model.request.UserRequest;
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

    @GetMapping(value = "/api/currentuser/email/verify")
    public void verifyEmail(@RequestParam(value = "token") String token) {
        userService.verifyEmail(token);
    }

    @GetMapping(value = "/api/currentuser/email/requestChange")
    public void requestEmailChange(@RequestParam(value = "email") String email) {
        userService.requestEmailChange(email);
    }

    @GetMapping(value = "/api/currentuser/email/change")
    public void emailChange(@RequestParam(value = "token") String token,
                            @RequestParam(value = "email") String email) {
        userService.emailChange(token, email);
    }

    @PutMapping(value = "/api/currentuser/password/change", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void updatePassword(@RequestBody PasswordChangeRequest passwordChangeRequest) {
        userService.passwordChange(passwordChangeRequest);
    }

    @GetMapping(value = "/api/users")
    public Set<SimpleUserResponse> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping(value = "/api/currentuser")
    public UserResponse getCurrentUser() {
        return userService.getCurrentUser();
    }
}