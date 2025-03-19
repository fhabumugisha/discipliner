package com.buseni.discipline.sanction.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.buseni.discipline.sanction.domain.RegleDiscipline;

@Service
public class RegleDisciplineServiceImpl implements RegleDisciplineService {

    //Ajout moins 1 point pour une petite betise
    private static final List<RegleDiscipline> RULES = List.of(
        new RegleDiscipline("SMALL_MISCONDUCT", "Petite betise", -1),
        new RegleDiscipline("BAD_GRADE", "Mauvaise note", -3),
        new RegleDiscipline("DISOBEDIENCE", "Désobéissance", -5),
        new RegleDiscipline("BAD_BEHAVIOR", "Très mauvaise conduite", -10)
    );

    @Override
    public List<RegleDiscipline> getAllRules() {
        return RULES;
    }

    @Override
    public Optional<RegleDiscipline> findByCode(String code) {
        return RULES.stream()
                .filter(rule -> rule.code().equals(code))
                .findFirst();
    }
} 