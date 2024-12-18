package com.sldlt.navps.service.impl;

import static com.sldlt.navps.entity.QNAVPSPrediction.nAVPSPrediction;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.core.BooleanBuilder;
import com.sldlt.navps.dto.NAVPSPredictionDto;
import com.sldlt.navps.entity.NAVPSPrediction;
import com.sldlt.navps.repository.NAVPSPredictionRepository;
import com.sldlt.navps.service.NAVPSPredictionService;
import com.sldlt.navps.service.NAVPSPredictorService;

@Service
public class NAVPSPredictionServiceImpl implements NAVPSPredictionService {

    @Autowired
    private ModelMapper mapper;

    @Autowired
    private NAVPSPredictionRepository navpsPredictionRepository;

    @Autowired
    private List<NAVPSPredictorService> navpsPredictorServices;

    @Override
    @Transactional
    public NAVPSPredictionDto savePrediction(NAVPSPredictionDto newPrediction) {
        BooleanBuilder predicate = new BooleanBuilder();
        predicate.and(nAVPSPrediction.fund.eq(newPrediction.getFund()));
        predicate.and(nAVPSPrediction.type.eq(newPrediction.getType()));
        predicate.and(nAVPSPrediction.predictionDate.eq(newPrediction.getPredictionDate()));
        predicate.and(nAVPSPrediction.daysInAdvance.eq(newPrediction.getDaysInAdvance()));

        NAVPSPrediction predictionObj = navpsPredictionRepository.findOne(predicate).orElseGet(NAVPSPrediction::new);
        mapper.map(newPrediction, predictionObj);

        return mapper.map(navpsPredictionRepository.save(predictionObj), NAVPSPredictionDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<LocalDate, Set<NAVPSPredictionDto>> getPredictions(String fund, String predictionType, LocalDate dateFrom,
        LocalDate dateTo) {
        BooleanBuilder predicate = new BooleanBuilder();
        predicate.and(nAVPSPrediction.fund.eq(fund));
        predicate.and(nAVPSPrediction.type.eq(predictionType));
        predicate.and(nAVPSPrediction.predictionDate.goe(dateFrom));
        predicate.and(nAVPSPrediction.predictionDate.loe(dateTo));

        return StreamSupport
            .stream(navpsPredictionRepository.findAll(predicate, nAVPSPrediction.predictionDate.desc(), nAVPSPrediction.daysInAdvance.asc())
                .spliterator(), false)
            .map(entry -> mapper.map(entry, NAVPSPredictionDto.class)).collect(Collectors.groupingBy(NAVPSPredictionDto::getPredictionDate,
                TreeMap<LocalDate, Set<NAVPSPredictionDto>>::new, Collectors.toCollection(TreeSet<NAVPSPredictionDto>::new)));
    }

    @Override
    public Set<String> getPredictionsTypes() {
        return navpsPredictorServices.stream().map(NAVPSPredictorService::getType).collect(Collectors.toCollection(TreeSet<String>::new));
    }

}
