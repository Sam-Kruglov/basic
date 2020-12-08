package com.samkruglov.base.domain.common;

import com.samkruglov.base.domain.User;
import lombok.Getter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import java.time.Instant;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class Auditable {

    @LastModifiedBy
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_modified_by")
    private User lastModifiedBy;

    @LastModifiedDate
    private Instant lastModifiedAt;

    @CreatedBy
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @CreatedDate
    private Instant createdAt;

    @Override
    public String toString() {
        return "{" +
                "lastModifiedBy=" + lastModifiedBy +
                ", lastModifiedAt=" + lastModifiedAt +
                ", createdBy=" + createdBy +
                ", createdAt=" + createdAt +
                '}';
    }
}
