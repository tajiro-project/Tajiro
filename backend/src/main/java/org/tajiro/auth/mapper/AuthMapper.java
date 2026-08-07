package org.tajiro.auth.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.tajiro.auth.domain.UserVO;

import java.util.List;

@Mapper
public interface AuthMapper {

    // 1. 이메일로 사용자 조회 (로그인, 중복확인용)
    UserVO findByEmail(@Param("email") String email);

    // 2. 회원 생성 (VO의 id에 생성된 PK가 채워짐)
    void insertUser(UserVO user);

    // 3. 현재 필수 약관 ID 목록 조회
    List<Long> selectRequiredTermsIds();

    // 4. 약관 동의 이력 저장 (동의 당시 약관 버전을 terms 테이블에서 그대로 가져와 저장)
    void insertTermsConsent(@Param("userId") Long userId,
                             @Param("termsId") Long termsId,
                             @Param("agreed") boolean agreed);

    // 5. 회원 탈퇴 (soft-delete)
    void softDeleteUser(@Param("userId") Long userId);

    // 6. 탈퇴한 계정의 이메일 반납 (재가입 시 호출)
    void releaseWithdrawnEmail(@Param("userId") Long userId);
}
