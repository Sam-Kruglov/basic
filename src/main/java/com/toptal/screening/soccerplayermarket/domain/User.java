package com.toptal.screening.soccerplayermarket.domain;

import com.toptal.screening.soccerplayermarket.domain.common.Identifiable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.NaturalId;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.util.List;
import java.util.function.Predicate;

@Entity
@ToString(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@RequiredArgsConstructor
@Getter
@Table(name = "users")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class User extends Identifiable {

    /**
     * This is used as a username for Spring Security
     */
    @NonNull
    @NaturalId
    @Column(columnDefinition = "varchar_ignorecase(70)", unique = true, nullable = false)
    private String email;

    @NonNull
    @Column(columnDefinition = "varchar_ignorecase(500)", nullable = false)
    private String encodedPassword;

    @NonNull
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "users_to_roles",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"),
            indexes = @Index(columnList = "user_id")
    )
    private List<Role> roles;

    public boolean hasRole(String roleName) {
        return roles.stream()
                    .map(Role::getName)
                    .anyMatch(Predicate.isEqual(roleName));
    }
}
