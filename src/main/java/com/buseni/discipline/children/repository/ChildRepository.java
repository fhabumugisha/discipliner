package com.buseni.discipline.children.repository;

import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.buseni.discipline.children.domain.Child;

@Repository
public interface ChildRepository extends MongoRepository<Child, String> {
    
    @Query("{ 'parent_id': ?0, 'deleted': false }")
    List<Child> findByParentId(String parentId);
    
    @Query("{ 'parent_id': ?0, 'deleted': true }")
    List<Child> findDeletedByParentId(String parentId);
    
    @Query("{ 'parent_id': ?0 }")
    List<Child> findAllByParentId(String parentId);
} 