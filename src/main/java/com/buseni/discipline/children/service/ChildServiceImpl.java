package com.buseni.discipline.children.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.buseni.discipline.children.domain.Child;
import com.buseni.discipline.children.dto.ChildDto;
import com.buseni.discipline.children.repository.ChildRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChildServiceImpl implements ChildService {

    private final ChildRepository childRepository;

    @Override
    public List<ChildDto> getChildrenByParentId(String parentId) {
        return childRepository.findByParentId(parentId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional
    public ChildDto createChild(String parentId, ChildDto childDto) {
        Child child = Child.builder()
                .name(childDto.getName())
                .age(childDto.getAge())
                .parentId(parentId)
                .build();
        
        return toDto(childRepository.save(child));
    }

    @Override
    public ChildDto getChildById(String childId) {
        return childRepository.findById(childId)
                .map(this::toDto)
                .orElseThrow(() -> new RuntimeException("Child not found"));
    }

    @Override
    @Transactional
    public ChildDto updateChild(String childId, ChildDto childDto) {
        Child child = childRepository.findById(childId)
                .orElseThrow(() -> new RuntimeException("Child not found"));
        
        child.setName(childDto.getName());
        child.setAge(childDto.getAge());
        
        return toDto(childRepository.save(child));
    }

    private ChildDto toDto(Child child) {
        return new ChildDto(
            child.getId(),
            child.getName(),
            child.getAge(),
            child.getPoints()
        );
    }
} 