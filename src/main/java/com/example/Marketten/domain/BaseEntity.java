package com.example.Marketten.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@MappedSuperclass // 테이블로 생성되지 않고, 엔티티 필드를 상속받을 수 있도록 함
@EntityListeners(AuditingEntityListener.class) // JPA Auditing 활성화
@Getter
public abstract class BaseEntity {

    // 엔티티가 생성되어 저장될 때 시간이 자동 저장됩니다.
    @CreatedDate
    @Column(name = "created_at", updatable = false) // 최초 생성 시점에만 값이 지정되고 이후 변경되지 않음
    private LocalDateTime createdAt;

    // 엔티티의 값을 변경할 때 시간이 자동 저장됩니다.
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
