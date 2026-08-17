package org.tajiro.seller.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.tajiro.seller.domain.SellerPropertyCountsVO;
import org.tajiro.seller.domain.SellerPropertyListVO;
import org.tajiro.property.domain.PropertyVO;

import java.util.List;

@Mapper
public interface SellerPropertyMapper {
    PropertyVO findOwnedDetail(
            @Param("sellerId") Long sellerId,
            @Param("propertyId") Long propertyId);

    List<String> findImageUrls(@Param("propertyId") Long propertyId);

    List<SellerPropertyListVO> findPage(
            @Param("sellerId") Long sellerId,
            @Param("status") String status,
            @Param("limit") int limit,
            @Param("offset") int offset);

    long countByStatus(
            @Param("sellerId") Long sellerId,
            @Param("status") String status);

    SellerPropertyCountsVO getCounts(@Param("sellerId") Long sellerId);

    int updateStatus(
            @Param("sellerId") Long sellerId,
            @Param("propertyId") Long propertyId,
            @Param("transactionStatus") boolean transactionStatus);

    int deleteProperty(@Param("sellerId") Long sellerId, @Param("propertyId") Long propertyId);

}
