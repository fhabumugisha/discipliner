package com.buseni.discipline.children.domain;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "children")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Child {
    
    @Id
    private String id;

    @Field("name")
    private String name;

    @Field("age")
    private Integer age;

    @Indexed
    @Field("parent_id")
    private String parentId;

    @CreatedDate
    @Field("created_at")
    private Instant createdAt;

    @LastModifiedDate
    @Field("updated_at")
    private Instant updatedAt;

    @CreatedBy
    @Field("created_by")
    private String createdBy;

    @LastModifiedBy
    @Field("updated_by")
    private String updatedBy;

    @Field("is_deleted")
    @Builder.Default
    private boolean deleted = false;

    @Field("deleted_at")
    private Instant deletedAt;
} 