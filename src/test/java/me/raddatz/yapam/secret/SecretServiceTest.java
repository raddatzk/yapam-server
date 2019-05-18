package me.raddatz.yapam.secret;

import me.raddatz.yapam.common.service.MappingService;
import me.raddatz.yapam.common.service.RequestHelperService;
import me.raddatz.yapam.secret.model.Secret;
import me.raddatz.yapam.secret.model.request.SecretRequest;
import me.raddatz.yapam.secret.repository.SecretDBO;
import me.raddatz.yapam.secret.repository.SecretRepository;
import me.raddatz.yapam.secret.repository.SecretTransactions;
import me.raddatz.yapam.user.repository.UserDBO;
import me.raddatz.yapam.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@ExtendWith(SpringExtension.class)
@WebMvcTest(SecretService.class)
@ActiveProfiles("test")
public class SecretServiceTest {

    @Autowired private SecretService secretService;
    @MockBean private UserRepository userRepository;
    @MockBean private RequestHelperService requestHelperService;
    @MockBean private SecretTransactions secretTransactions;
    @MockBean private MappingService mappingService;
    @MockBean private SecretRepository secretRepository;

    private UserDBO createDefaultUserDBO() {
        return new UserDBO();
    }

    private SecretRequest createDefaultSecretRequest() {
        return new SecretRequest();
    }

    private SecretDBO createDefaultSecretDBO() {
        return new SecretDBO();
    }

    private Secret createDefaultSecret() {
        return new Secret();
    }

    @Test
    void createSecret_whenUserForSecretExists_thenCreateSecret() {
        var user = createDefaultUserDBO();
        var secretRequest = createDefaultSecretRequest();
        var secret = createDefaultSecret();
        var secretDBO = createDefaultSecretDBO();
        when(requestHelperService.getUserName()).thenReturn("user@email.com");
        when(userRepository.findOneByEmail("user@email.com")).thenReturn(user);
        when(mappingService.secretFromRequest(secretRequest)).thenReturn(secret);
        when(mappingService.secretToDBO(secret)).thenReturn(secretDBO);

        secretService.createSecret(secretRequest);

        verify(secretTransactions, times(1)).saveSecret(any(SecretDBO.class));
    }

    @Test
    void updateSecret() {
        var secretRequest = createDefaultSecretRequest();
        var secret = createDefaultSecret();
        var secretDBO = createDefaultSecretDBO();
        secretDBO.setVersion(1);
        when(mappingService.secretFromRequest(secretRequest)).thenReturn(secret);
        when(secretRepository.findFirstVersionBySecretIdOrderByVersionDesc(anyString())).thenReturn(0);
        when(mappingService.secretToDBO(any(Secret.class))).thenReturn(secretDBO);

        secretService.updateSecret("secretId", secretRequest);

        ArgumentCaptor<SecretDBO> captor = ArgumentCaptor.forClass(SecretDBO.class);
        verify(secretTransactions).saveSecret(captor.capture());
        secretDBO = captor.getValue();

        assertEquals(Integer.valueOf(1), secretDBO.getVersion());
    }
}
