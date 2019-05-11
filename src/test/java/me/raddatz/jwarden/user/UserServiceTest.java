package me.raddatz.jwarden.user;

import me.raddatz.jwarden.common.annotation.AnnotationHandlerInterceptor;
import me.raddatz.jwarden.common.error.EmailAlreadyExistsException;
import me.raddatz.jwarden.common.error.InvalidEmailVerificationTokenException;
import me.raddatz.jwarden.common.error.UserNotFoundException;
import me.raddatz.jwarden.common.service.EmailService;
import me.raddatz.jwarden.common.service.PBKDF2Service;
import me.raddatz.jwarden.config.JWardenConfig;
import me.raddatz.jwarden.user.model.RegisterUser;
import me.raddatz.jwarden.user.model.User;
import me.raddatz.jwarden.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(UserService.class)
@ActiveProfiles("test")
@ContextConfiguration(classes = {UserService.class})
class UserServiceTest {

    @Autowired private UserService userService;
    @MockBean private PBKDF2Service pbkdf2Service;
    @MockBean private UserRepository userRepository;
    @MockBean private EmailService emailService;
    @MockBean private JWardenConfig jWardenConfig;
    @MockBean private AnnotationHandlerInterceptor annotationHandlerInterceptor;

    private RegisterUser createDefaultRegisterUser() {
        var user = new RegisterUser();
        user.setName("Name");
        user.setEmail("email@test.com");
        user.setMasterPassword("password");
        return user;
    }

    private User createDefaultUser() {
        var user = new User();
        user.setEmailToken("token");
        user.setCreationDate(LocalDateTime.now());
        user.setEmailVerified(false);
        return user;
    }

    @Test
    void register_whenUserNotExists_thenRegisterUser() {
        when(pbkdf2Service.generateSalt()).thenReturn("salt");
        when(pbkdf2Service.generateSaltedHash(Mockito.anyString(), Mockito.anyString())).thenReturn("saltedhash");
        var registerUser = createDefaultRegisterUser();

        userService.register(registerUser);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        var user = captor.getValue();

        assertEquals(registerUser.getEmail(), user.getEmail());
        assertEquals(registerUser.getName(), user.getName());
        assertEquals("saltedhash", user.getMasterPasswordHash());
        assertEquals("salt", user.getMasterPasswordSalt());
    }

    @Test
    void register_whenUserExistsInRegistrationUnverified_thenOverrideUser() {
        var registerUser = createDefaultRegisterUser();
        var user = createDefaultUser();
        when(pbkdf2Service.generateSalt()).thenReturn("salt");
        when(pbkdf2Service.generateSaltedHash(Mockito.anyString(), Mockito.anyString())).thenReturn("saltedhash");
        when(userRepository.findOneByEmail(Mockito.anyString())).thenReturn(user);
        when(jWardenConfig.getRegistrationTimeout()).thenReturn(Duration.parse("P1D"));

        userService.register(registerUser);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        user = captor.getValue();

        assertEquals(registerUser.getEmail(), user.getEmail());
        assertEquals(registerUser.getName(), user.getName());
        assertEquals("saltedhash", user.getMasterPasswordHash());
        assertEquals("salt", user.getMasterPasswordSalt());
    }

    @Test
    void register_whenUserExistsNotInRegistrationUnverified_thenThrowException() {
        var registerUser = createDefaultRegisterUser();
        var user = createDefaultUser();
        user.setCreationDate(LocalDateTime.now().minusDays(2));
        when(pbkdf2Service.generateSalt()).thenReturn("salt");
        when(pbkdf2Service.generateSaltedHash(Mockito.anyString(), Mockito.anyString())).thenReturn("saltedhash");
        when(userRepository.findOneByEmail(Mockito.anyString())).thenReturn(user);
        when(jWardenConfig.getRegistrationTimeout()).thenReturn(Duration.parse("P1D"));

        assertThrows(EmailAlreadyExistsException.class, () -> userService.register(registerUser));
    }

    @Test
    void register_whenUserExistsNotInRegistrationVerified_thenThrowException() {
        var registerUser = createDefaultRegisterUser();
        var user = createDefaultUser();
        user.setEmailVerified(true);
        user.setCreationDate(LocalDateTime.now().minusDays(2));
        when(pbkdf2Service.generateSalt()).thenReturn("salt");
        when(pbkdf2Service.generateSaltedHash(Mockito.anyString(), Mockito.anyString())).thenReturn("saltedhash");
        when(userRepository.findOneByEmail(Mockito.anyString())).thenReturn(user);
        when(jWardenConfig.getRegistrationTimeout()).thenReturn(Duration.parse("P1D"));

        assertThrows(EmailAlreadyExistsException.class, () -> userService.register(registerUser));
    }

    @Test
    void register_whenUserExistsInRegistrationVerified_thenThrowException() {
        var registerUser = createDefaultRegisterUser();
        var user = createDefaultUser();
        user.setEmailVerified(true);
        when(pbkdf2Service.generateSalt()).thenReturn("salt");
        when(pbkdf2Service.generateSaltedHash(Mockito.anyString(), Mockito.anyString())).thenReturn("saltedhash");
        when(userRepository.findOneByEmail(Mockito.anyString())).thenReturn(user);
        when(jWardenConfig.getRegistrationTimeout()).thenReturn(Duration.parse("P1D"));

        assertThrows(EmailAlreadyExistsException.class, () -> userService.register(registerUser));
    }

    @Test
    void verifyEmail_validToken_thenSaveUser() {
        var userId = "userid";
        var token = "token";
        var user = createDefaultUser();
        when(userRepository.findOneById(anyString())).thenReturn(user);

        userService.verifyEmail(userId, token);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void verifyEmail_whenInvalidToken_thenThrowException() {
        var userId = "userid";
        var token = "invalidtoken";
        var user = createDefaultUser();
        when(userRepository.findOneById(anyString())).thenReturn(user);

        assertThrows(InvalidEmailVerificationTokenException.class, () -> userService.verifyEmail(userId, token));
    }

    @Test
    void requestEmailChange_whenUserExists_thenRequestEmailChange() {
        var userId = "userId";
        var email = "email";
        var user = createDefaultUser();
        when(userRepository.findOneById(anyString())).thenReturn(user);

        userService.requestEmailChange(userId, email);
        verify(userRepository, times(1)).save(any(User.class));
        verify(emailService, times(1)).sendEmailChangeEmail(any(User.class), anyString());
    }

    @Test
    void emailChange_whenValidToken_thenChangeEmail() {
        var userId = "userId";
        var token = "token";
        var email = "email";
        var user = createDefaultUser();
        when(userRepository.findOneById(anyString())).thenReturn(user);

        userService.emailChange(userId, token, email);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void emailChange_whenInvalidToken_thenChangeEmail() {
        var userId = "userId";
        var token = "invalidtoken";
        var email = "email";
        var user = createDefaultUser();
        when(userRepository.findOneById(anyString())).thenReturn(user);

        assertThrows(InvalidEmailVerificationTokenException.class, () -> userService.emailChange(userId, token, email));
    }
}