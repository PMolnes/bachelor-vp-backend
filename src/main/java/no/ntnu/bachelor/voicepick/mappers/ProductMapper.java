package no.ntnu.bachelor.voicepick.mappers;

import no.ntnu.bachelor.voicepick.dtos.LocationDto;
import no.ntnu.bachelor.voicepick.dtos.ProductDto;
import no.ntnu.bachelor.voicepick.models.Location;
import no.ntnu.bachelor.voicepick.models.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.Collection;

@Mapper(componentModel = "spring")
public abstract class ProductMapper {

  private LocationMapper locationMapper = Mappers.getMapper(LocationMapper.class);

  @Mapping(target = "location", source = "location", qualifiedByName = "locationToLocationDto")
  public abstract ProductDto toProductDto(Product product);
  public abstract Collection<ProductDto> toProductDto(Collection<Product> product);

  @Named("locationToLocationDto")
  public LocationDto locationToLocationDto(Location location) {
    return locationMapper.toLocationDto(location);
  }

}