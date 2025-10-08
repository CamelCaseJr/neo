package org.acme.domain.mapper;

import java.net.URI;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.acme.domain.dtos.NeoObjectResponse;
import org.acme.domain.models.NeoObject;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "cdi")
public interface NeoObjectMapper {

    @Mapping(target = "self", source = "selfUri")
    NeoObjectResponse toResponse(NeoObject neo, URI selfUri);

    /**
     * Método default para mapear listas de NeoObject para NeoObjectResponse
     * Usa uma Function para construir a URI de cada item
     */
    default List<NeoObjectResponse> toResponseList(List<NeoObject> neoList, Function<NeoObject, URI> uriBuilder) {
        if (neoList == null) {
            return null;
        }
        
        return neoList.stream()
                .map(neo -> toResponse(neo, uriBuilder.apply(neo)))
                .collect(Collectors.toList());
    }
} 