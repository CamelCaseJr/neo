package org.acme.repository;

import org.acme.domain.models.NeoObject;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class NeoRepository implements PanacheRepository<NeoObject> {
    
}
