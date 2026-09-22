package kr.ac.knue.facultyassessment.common;

import java.time.OffsetDateTime;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UsageAuthorizationMapper {

    @Select("select use_yn as \"useYn\", exposure_start_at as \"exposureStartAt\", exposure_end_at as \"exposureEndAt\" from menu where menu_id = #{menuId}")
    MenuUsage findMenuUsage(@Param("menuId") String menuId);

    record MenuUsage(String useYn, OffsetDateTime exposureStartAt, OffsetDateTime exposureEndAt) {
    }
}
