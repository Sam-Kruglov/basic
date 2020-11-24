package com.samkruglov.base.domain;

import com.samkruglov.base.domain.common.Identifiable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.NaturalIdCache;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

@Entity
@ToString(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@RequiredArgsConstructor
@Getter
@Table(name = User.TABLE_NAME)
@Cacheable
@Cache(region = User.TABLE_NAME, usage = CacheConcurrencyStrategy.READ_WRITE)
@NaturalIdCache
public class User extends Identifiable {

    public static final String TABLE_NAME = "users";

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
    @Cache(region = Role.TABLE_NAME, usage = CacheConcurrencyStrategy.READ_WRITE)
    // roles cache isn't shared between entities here. see https://hibernate.atlassian.net/browse/HHH-14281
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "users_to_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private List<Role> roles;

    public boolean hasRole(String roleName) {
        return getRoles().stream().map(Role::getName).anyMatch(Predicate.isEqual(roleName));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User that = (User) o;
        if (id != null && that.id != null) {
            return id.equals(that.id);
        }
        return email.equals(that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email);
    }
}
