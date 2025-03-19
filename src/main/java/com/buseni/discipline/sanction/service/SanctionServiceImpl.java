package com.buseni.discipline.sanction.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.buseni.discipline.children.domain.Child;
import com.buseni.discipline.children.dto.ChildDto;
import com.buseni.discipline.children.repository.ChildRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SanctionServiceImpl implements SanctionService {

    private final ChildRepository childRepository;
    private final RegleDisciplineService regleDisciplineService;

    @Override
    @Transactional
    public ChildDto applySanction(String childId, int points) {
        if (points > 0) {
            throw new IllegalArgumentException("Points must be negative for sanctions");
        }

        Child child = childRepository.findById(childId)
                .orElseThrow(() -> new IllegalArgumentException("Child not found"));

        child.setPoints(child.getPoints() + points);
        Child savedChild = childRepository.save(child);

        return toDto(savedChild);
    }

    @Override
    @Transactional
    public ChildDto applySanctionByRule(String childId, String ruleCode) {
        var rule = regleDisciplineService.findByCode(ruleCode)
                .orElseThrow(() -> new IllegalArgumentException("Rule not found: " + ruleCode));

        return applySanction(childId, rule.points());
    }

    private ChildDto toDto(Child child) {
        return ChildDto.builder()
                .id(child.getId())
                .name(child.getName())
                .age(child.getAge())
                .points(child.getPoints())
                .build();
    }
} 