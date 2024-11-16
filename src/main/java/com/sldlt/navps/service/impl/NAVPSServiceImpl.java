package com.sldlt.navps.service.impl;

import static com.sldlt.navps.entity.QNAVPSEntry.nAVPSEntry;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.querydsl.core.BooleanBuilder;
import com.sldlt.navps.dto.FundDto;
import com.sldlt.navps.dto.NAVPSEntryDto;
import com.sldlt.navps.entity.NAVPSEntry;
import com.sldlt.navps.repository.NAVPSEntryRepository;
import com.sldlt.navps.service.FundService;
import com.sldlt.navps.service.NAVPSService;

@Service
public class NAVPSServiceImpl implements NAVPSService {

    @Autowired
    private ModelMapper mapper;

    @Autowired
    private NAVPSEntryRepository navpsEntryRepository;

    @Autowired
    private FundService fundService;

    @Override
    @Transactional
    public void saveNAVPS(final NAVPSEntryDto entry) {
        saveNAVPS(Collections.singletonList(entry));
    }

    @Override
    @Transactional
    public void saveNAVPS(final List<NAVPSEntryDto> entries) {
        navpsEntryRepository.saveAll(entries.stream()
            .filter(entry -> navpsEntryRepository
                .count(nAVPSEntry.entryDate.eq(entry.getEntryDate()).and(nAVPSEntry.fund.eq(entry.getFund()))) == 0)
            .map(entry -> mapper.map(entry, NAVPSEntry.class)).toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<NAVPSEntryDto> listNAVPS(final String fund, final LocalDate dateFrom, final LocalDate dateTo) {
        final BooleanBuilder predicate = new BooleanBuilder();
        if (StringUtils.hasText(fund)) {
            predicate.and(nAVPSEntry.fund.eq(fund));
        }
        if (dateFrom != null) {
            predicate.and(nAVPSEntry.entryDate.goe(dateFrom));
        }
        if (dateTo != null) {
            predicate.and(nAVPSEntry.entryDate.loe(dateTo));
        }

        return StreamSupport.stream(navpsEntryRepository.findAll(predicate, nAVPSEntry.entryDate.desc()).spliterator(), false)
            .map(entry -> mapper.map(entry, NAVPSEntryDto.class)).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NAVPSEntryDto> listNAVPS(final String fund, final LocalDate dateFrom, final LocalDate dateTo, final Pageable page) {
        final Map<String, String> fundNames = new HashMap<>();
        final BooleanBuilder predicate = new BooleanBuilder();
        if (StringUtils.hasText(fund)) {
            predicate.and(nAVPSEntry.fund.eq(fund));
            fundNames.put(fund, fundService.getFundByCode(fund).getName());
        } else {
            fundNames.putAll(fundService.listAllFunds().stream().collect(Collectors.toMap(FundDto::getCode, FundDto::getName)));
        }
        if (dateFrom != null) {
            predicate.and(nAVPSEntry.entryDate.goe(dateFrom));
        }
        if (dateTo != null) {
            predicate.and(nAVPSEntry.entryDate.loe(dateTo));
        }

        return navpsEntryRepository.findAll(predicate, page).map(entry -> mapper.map(entry, NAVPSEntryDto.class))
            .map(entry -> setNavpsEntryFundName(entry, fundNames));
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, List<NAVPSEntryDto>> listNAVPS(final Set<String> funds, final LocalDate dateFrom, final LocalDate dateTo) {
        final BooleanBuilder predicate = new BooleanBuilder();
        if (funds != null && !funds.isEmpty()) {
            predicate.and(nAVPSEntry.fund.in(funds));
        }
        if (dateFrom != null) {
            predicate.and(nAVPSEntry.entryDate.goe(dateFrom));
        }
        if (dateTo != null) {
            predicate.and(nAVPSEntry.entryDate.loe(dateTo));
        }

        return StreamSupport
            .stream(navpsEntryRepository.findAll(predicate, nAVPSEntry.fund.desc(), nAVPSEntry.entryDate.desc()).spliterator(), false)
            .map(entry -> mapper.map(entry, NAVPSEntryDto.class)).collect(Collectors.groupingBy(NAVPSEntryDto::getFund));
    }

    private NAVPSEntryDto setNavpsEntryFundName(final NAVPSEntryDto entry, final Map<String, String> fundNames) {
        entry.setFundName(fundNames.getOrDefault(entry.getFund(), ""));
        return entry;
    }

    @Override
    @Transactional(readOnly = true)
    public List<NAVPSEntryDto> listAllNAVPS(final String fund) {
        final BooleanBuilder predicate = new BooleanBuilder();
        predicate.and(nAVPSEntry.fund.eq(fund));
        return StreamSupport.stream(navpsEntryRepository.findAll(predicate, nAVPSEntry.entryDate.desc()).spliterator(), false)
            .map(entry -> mapper.map(entry, NAVPSEntryDto.class)).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Map<String, BigDecimal>> listAllCorrelations(final LocalDate dateFrom) {
        final List<FundDto> funds = fundService.listAllFunds();

        if (funds == null || funds.isEmpty()) {
            return Collections.emptyMap();
        }

        final List<FundDto> sortedFunds = funds.stream().sorted((fund1, fund2) -> fund1.getCode().compareTo(fund2.getCode())).toList();

        final Map<String, List<NAVPSEntryDto>> allNavps = new TreeMap<>();
        for (final FundDto fund : sortedFunds) {
            allNavps.put(fund.getCode(), getNavpsFromDate(fund.getCode(), dateFrom));
        }

        final Map<String, Map<String, BigDecimal>> correlations = new TreeMap<>();
        for (final FundDto fund : sortedFunds) {
            final Set<FundDto> exclude = new HashSet<>();
            boolean found = false;
            for (final FundDto comparedFund : sortedFunds) {
                if (fund.getCode().equals(comparedFund.getCode())) {
                    found = true;
                }
                if (found) {
                    exclude.add(comparedFund);
                }
            }
            correlations.put(fund.getCode(), calculateCorrelations(fund, sortedFunds, exclude, allNavps));
        }

        return correlations;
    }

    private List<NAVPSEntryDto> getNavpsFromDate(final String fundCode, final LocalDate dateFrom) {
        if (!StringUtils.hasText(fundCode)) {
            return Collections.emptyList();
        }

        final BooleanBuilder predicate = new BooleanBuilder();
        predicate.and(nAVPSEntry.fund.eq(fundCode));
        if (dateFrom != null) {
            predicate.and(nAVPSEntry.entryDate.goe(dateFrom));
        }
        return StreamSupport.stream(navpsEntryRepository.findAll(predicate, nAVPSEntry.entryDate.desc()).spliterator(), false)
            .map(entry -> mapper.map(entry, NAVPSEntryDto.class)).toList();
    }

    private Map<String, BigDecimal> calculateCorrelations(final FundDto fund, final List<FundDto> funds, final Set<FundDto> exclude,
        final Map<String, List<NAVPSEntryDto>> allNavps) {

        final Map<String, BigDecimal> correlations = new TreeMap<>();

        for (final FundDto comparedFund : funds) {
            if (fund.getCode().equals(comparedFund.getCode()) || exclude.contains(comparedFund)) {
                correlations.put(comparedFund.getCode(), null);
            } else {
                correlations.put(comparedFund.getCode(),
                    computeCorrelation(allNavps.get(fund.getCode()), allNavps.get(comparedFund.getCode())));
            }
        }

        return correlations;
    }

    private BigDecimal computeCorrelation(final List<NAVPSEntryDto> list1, final List<NAVPSEntryDto> list2) {
        if (list1 == null || list1.isEmpty() || list2 == null || list2.isEmpty()) {
            return null;
        }

        final Optional<LocalDate> minDate1 = list1.stream().map(NAVPSEntryDto::getEntryDate)
            .reduce((date1, date2) -> date2.isAfter(date1) ? date1 : date2);
        final Optional<LocalDate> minDate2 = list2.stream().map(NAVPSEntryDto::getEntryDate)
            .reduce((date1, date2) -> date2.isAfter(date1) ? date1 : date2);

        if (!minDate1.isPresent() || !minDate2.isPresent()) {
            return null;
        }

        final LocalDate minDateAll = minDate1.get().isAfter(minDate2.get()) ? minDate1.get() : minDate2.get();

        final List<Pair<LocalDate, BigDecimal>> normalized1 = normalizeNavps(list1, minDateAll);
        final List<Pair<LocalDate, BigDecimal>> normalized2 = normalizeNavps(list2, minDateAll);

        BigDecimal accum = BigDecimal.ZERO;
        int index1 = 0;
        int index2 = 0;
        int totalMatched = 0;

        while (index1 < normalized1.size() && index2 < normalized2.size()) {
            final Pair<LocalDate, BigDecimal> entry1 = normalized1.get(index1);
            final Pair<LocalDate, BigDecimal> entry2 = normalized2.get(index2);
            final LocalDate date1 = entry1.getFirst();
            final LocalDate date2 = entry2.getFirst();
            if (date1.equals(date2)) {
                accum = accum.add(entry1.getSecond().multiply(entry2.getSecond()));
                index1++;
                index2++;
                totalMatched++;
            } else if (date1.isBefore(date2)) {
                index2++;
            } else if (date2.isBefore(date1)) {
                index1++;
            }
        }

        return accum.divide(BigDecimal.valueOf(totalMatched), 10, RoundingMode.HALF_UP);
    }

    private List<Pair<LocalDate, BigDecimal>> normalizeNavps(final List<NAVPSEntryDto> navpsList, final LocalDate minDate) {
        if (navpsList == null || navpsList.isEmpty()) {
            return Collections.emptyList();
        }

        final List<Pair<LocalDate, BigDecimal>> values = navpsList.stream().filter(entry -> !entry.getEntryDate().isBefore(minDate))
            .map(entry -> Pair.of(entry.getEntryDate(), entry.getFundValue())).toList();

        final BigDecimal size = BigDecimal.valueOf(values.size());
        final BigDecimal sum = values.stream().map(Pair::getSecond).reduce(BigDecimal.ZERO, (value1, value2) -> value1.add(value2));
        final BigDecimal sumSq = values.stream().map(Pair::getSecond).map(value -> value.pow(2)).reduce(BigDecimal.ZERO,
            (value1, value2) -> value1.add(value2));

        final BigDecimal avg = sum.divide(size, 10, RoundingMode.HALF_UP);
        final BigDecimal variance = sumSq.subtract(sum.multiply(sum).divide(size, 10, RoundingMode.HALF_UP)).divide(size, 10,
            RoundingMode.HALF_UP);
        final BigDecimal standardDev = BigDecimal.valueOf(Math.sqrt(variance.doubleValue()));

        return values.stream().map(
            entryPair -> Pair.of(entryPair.getFirst(), entryPair.getSecond().subtract(avg).divide(standardDev, 10, RoundingMode.HALF_UP)))
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Pair<BigDecimal, BigDecimal>> listNAVPSPaired(String fundX, String fundY, LocalDate dateFrom, LocalDate dateTo) {
        List<Pair<BigDecimal, BigDecimal>> result = new LinkedList<>();

        List<NAVPSEntryDto> navpsX = listNAVPS(fundX, dateFrom, dateTo);
        List<NAVPSEntryDto> navpsY = listNAVPS(fundY, dateFrom, dateTo);

        int navpsXSize = navpsX.size();
        int navpsYSize = navpsY.size();
        int navpsXIndex = 0;
        int navpsYIndex = 0;

        LocalDate currDateX = null;
        LocalDate currDateY = null;

        while (navpsXIndex < navpsXSize && navpsYIndex < navpsYSize) {
            currDateX = navpsX.get(navpsXIndex).getEntryDate();
            currDateY = navpsY.get(navpsYIndex).getEntryDate();

            if (currDateX.isBefore(currDateY)) {
                navpsYIndex++;
            } else if (currDateX.isAfter(currDateY)) {
                navpsXIndex++;
            } else {
                result.add(Pair.of(navpsX.get(navpsXIndex).getFundValue(), navpsY.get(navpsYIndex).getFundValue()));
                navpsXIndex++;
                navpsYIndex++;
            }
        }

        return result;
    }
}
