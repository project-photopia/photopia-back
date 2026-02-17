package com.photopia.photopia_back.config;

import com.photopia.photopia_back.model.EventType;
import com.photopia.photopia_back.repository.EventTypeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(EventTypeRepository eventTypeRepository) {
        return args -> {
            List<String> defaultEventTypes = Arrays.asList(
                    "Trip",
                    "Event",
                    "Voyage",
                        "Mariage",
                        "Soirée",
                        "Anniversaire",
                        "Concert",
                        "Festival",
                        "Sport",
                        "Autre");

            for (String typeName : defaultEventTypes) {
                if (eventTypeRepository.findByName(typeName).isEmpty()) {
                    eventTypeRepository.save(EventType.builder().name(typeName).build());
                }
            }
        };
    }
}
