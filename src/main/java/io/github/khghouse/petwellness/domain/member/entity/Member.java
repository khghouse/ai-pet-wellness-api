package io.github.khghouse.petwellness.domain.member.entity;

import io.github.khghouse.petwellness.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberStatus status;

    @Column(nullable = false)
    private boolean deleted;

    private LocalDateTime deletedAt;

    private Member(String email, String password) {
        this.email = email;
        this.password = password;
        this.status = MemberStatus.ACTIVE;
        this.deleted = false;
    }

    public static Member create(String email, String password) {
        return new Member(email, password);
    }

    public boolean isActive() {
        return status == MemberStatus.ACTIVE && !deleted;
    }

    public boolean isWithdrawn() {
        return status == MemberStatus.WITHDRAWN || deleted;
    }

    public void withdraw() {
        this.status = MemberStatus.WITHDRAWN;
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
    }
}
