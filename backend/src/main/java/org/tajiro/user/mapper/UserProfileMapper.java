package org.tajiro.user.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.tajiro.user.domain.UserProfileVO;


@Mapper
public interface UserProfileMapper {
    UserProfileVO getUserProfileById(@Param("userId") Long userId);
}
