package com.toptal.screening.soccerplayermarket.domain.common;

import lombok.Getter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public class Identifiable {

    private static final String SEQUENCE_PER_ENTITY_GENERATOR = "sequence_per_entity_generator";

    @Id
    @GenericGenerator(
            name = Identifiable.SEQUENCE_PER_ENTITY_GENERATOR,
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @Parameter(name = SequenceStyleGenerator.CONFIG_PREFER_SEQUENCE_PER_ENTITY, value = "true")
            }
    )
    @GeneratedValue(generator = SEQUENCE_PER_ENTITY_GENERATOR, strategy = GenerationType.SEQUENCE)
    @Getter
    protected Long id;

    @Override
    public String toString() {
        return "{id=" + id + '}';
    }
}
