package me.raddatz.yapam.secret.repository;

import lombok.Getter;
import lombok.Setter;
import me.raddatz.yapam.secret.model.SecretType;
import me.raddatz.yapam.user.repository.UserDBO;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity(name = "secret")
@Getter
@Setter
public class SecretDBO {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    private String id;

    @Column(name = "secret_id")
    private String secretId;
    private Integer version;
    @Column(name = "creation_date")
    private LocalDateTime creationDate;
    @Lob
    private String data;
    private SecretType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserDBO user;
}
