package app.yapam.kdf.model.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KdfInfo {

    private Integer iterations;
    private Boolean secure;
}