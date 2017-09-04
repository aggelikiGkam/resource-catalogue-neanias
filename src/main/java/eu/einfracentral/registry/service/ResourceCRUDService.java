package eu.einfracentral.registry.service;

import eu.openminted.registry.core.domain.Browsing;
import eu.openminted.registry.core.service.ParserService;

import java.util.List;
import java.util.Map;

/**
 * Created by pgl on 04/08/17.
 */
public interface ResourceCRUDService<T> extends eu.openminted.registry.core.service.ResourceCRUDService<T> {

    Map<String, List<T>> getBy(String field);

    List<T> getSome(String... ids);

    void add(T resourceToAdd, ParserService.ParserServiceTypes type);

    void update(T resourceToAdd, ParserService.ParserServiceTypes type);

    Browsing delAll();
}
