package app.yapam.secret;

import app.yapam.YapamBaseTest;
import app.yapam.common.service.MappingService;
import app.yapam.common.service.RequestHelperService;
import app.yapam.secret.model.Secret;
import app.yapam.secret.model.request.SecretRequest;
import app.yapam.secret.model.response.SecretResponse;
import app.yapam.secret.repository.SecretDBO;
import app.yapam.secret.repository.SecretRepository;
import app.yapam.secret.repository.SecretTransactions;
import app.yapam.user.repository.UserDBO;
import app.yapam.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@ExtendWith(SpringExtension.class)
@WebMvcTest(SecretService.class)
@ActiveProfiles("test")
class SecretServiceTest extends YapamBaseTest {

    @Autowired private SecretService secretService;
    @MockBean private UserRepository userRepository;
    @MockBean private RequestHelperService requestHelperService;
    @MockBean private SecretTransactions secretTransactions;
    @MockBean private MappingService mappingService;
    @MockBean private SecretRepository secretRepository;

    @Test
    void createSecret_whenUserForSecretExists_thenCreateSecret() {
        var user = createDefaultUserDBO();
        var secretRequest = createDefaultSecretRequest();
        var secret = createDefaultSecret();
        var secretDBO = createDefaultSecretDBO();
        when(requestHelperService.getUserName()).thenReturn(DEFAULT_USER_EMAIL);
        when(userRepository.findOneByEmail(DEFAULT_USER_EMAIL)).thenReturn(user);
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
        when(secretRepository.findCurrentVersion(anyString())).thenReturn(0);
        when(mappingService.secretToDBO(any(Secret.class))).thenReturn(secretDBO);

        secretService.updateSecret(DEFAUlT_SECRET_SECRETID, secretRequest);

        ArgumentCaptor<SecretDBO> captor = ArgumentCaptor.forClass(SecretDBO.class);
        verify(secretTransactions).saveSecret(captor.capture());
        secretDBO = captor.getValue();

        assertEquals(Integer.valueOf(1), secretDBO.getVersion());
    }

    @Test
    void getAllSecrets() {
        var userDBO = createDefaultUserDBO();
        var secretResponse = createDefaultSecretResponse();
        var secrets = new HashSet<SecretDBO>();
        secrets.add(createDefaultSecretDBO());
        when(secretRepository.findAllByCurrentByUser(DEFAULT_USER_ID)).thenReturn(secrets);
        when(userRepository.findOneByEmail(DEFAULT_USER_EMAIL)).thenReturn(userDBO);
        when(mappingService.secretDBOToResponse(any(SecretDBO.class))).thenReturn(secretResponse);
        when(requestHelperService.getUserName()).thenReturn(DEFAULT_USER_EMAIL);

        var result = secretService.getAllSecrets();
        assertEquals(1, result.size());
    }
}
