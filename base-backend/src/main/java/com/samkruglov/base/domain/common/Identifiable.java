package com.samkruglov.base.domain.common;

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
            name = SEQUENCE_PER_ENTITY_GENERATOR,
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = @Parameter(name = SequenceStyleGenerator.CONFIG_PREFER_SEQUENCE_PER_ENTITY, value = "true")
    )
    @GeneratedValue(generator = SEQUENCE_PER_ENTITY_GENERATOR, strategy = GenerationType.SEQUENCE)
    @Getter
    protected Long id;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Identifiable that = (Identifiable) o;
        if (id != null && that.id != null) {
            return id.equals(that.id);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public String toString() {
        return "{id=" + id + '}';
    }
}
