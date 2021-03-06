package com.sldlt.config;

import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sldlt.navps.dto.FundDto;
import com.sldlt.navps.dto.NAVPSPredictionDto;
import com.sldlt.navps.entity.Fund;
import com.sldlt.navps.entity.NAVPSPrediction;

@Configuration
public class MapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.addMappings(new PropertyMap<FundDto, Fund>() {

            @Override
            protected void configure() {
                skip().setId(null);
            }
        });
        modelMapper.addMappings(new PropertyMap<NAVPSPredictionDto, NAVPSPrediction>() {

            @Override
            protected void configure() {
                skip().setId(null);
            }
        });
        return modelMapper;
    }
}
