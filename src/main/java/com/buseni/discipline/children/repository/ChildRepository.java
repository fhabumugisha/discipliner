package com.buseni.discipline.children.repository;

import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.buseni.discipline.children.domain.Child;


public interface ChildRepository extends MongoRepository<Child, String> {
    
  
    
    
    List<Child> findByParentId(String parentId);
   
    List<Child> findByParentIdAndDeletedFalseOrderByCreatedAtDesc(String parentId);
} 