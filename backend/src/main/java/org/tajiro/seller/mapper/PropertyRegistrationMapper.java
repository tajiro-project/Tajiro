package org.tajiro.seller.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.tajiro.seller.domain.PropertyRegistrationVO;

import java.util.List;

@Mapper
public interface PropertyRegistrationMapper {

    int insertProperty(PropertyRegistrationVO property);

    int insertPropertyImages(
            @Param("propertyId") Long propertyId,
            @Param("imageUrls") List<String> imageUrls
    );
}
