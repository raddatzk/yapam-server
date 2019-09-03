package app.yapam.common.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class UnknownUserException extends YapamException {

    public UnknownUserException() {
        super();
    }

    public UnknownUserException(String userId) {
        super(String.format("User with id %s not found", userId));
    }

}
