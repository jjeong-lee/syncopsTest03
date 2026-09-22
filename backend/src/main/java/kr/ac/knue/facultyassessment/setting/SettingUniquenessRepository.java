package kr.ac.knue.facultyassessment.setting;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SettingUniquenessRepository {

    @Select("select exists(select 1 from menu where menu_id = #{menuId})")
    boolean menuExists(@Param("menuId") String menuId);

    @Select("select exists(select 1 from detail_code where group_id = #{groupId} and code_value = #{codeValue})")
    boolean detailCodeKeyExists(@Param("groupId") String groupId, @Param("codeValue") String codeValue);

    @Select("select exists(select 1 from common_environment_setting where setting_key = #{settingKey} and deleted_yn = 'N')")
    boolean commonEnvironmentSettingKeyExists(@Param("settingKey") String settingKey);

    @Select("select exists(select 1 from reference_year_setting where deleted_yn = 'N' and target_year is null)")
    boolean activeReferenceYearConfigurationExists();

    @Select("select exists(select 1 from reference_year_setting where deleted_yn = 'N' and target_year = #{targetYear})")
    boolean activeReferenceYearTargetExists(@Param("targetYear") Integer targetYear);
}
