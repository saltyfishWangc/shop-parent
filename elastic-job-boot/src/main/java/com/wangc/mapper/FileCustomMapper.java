package com.wangc.mapper;

import com.wangc.domain.FileCustom;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * @author
 * @Description:
 * @date 2022/10/14 15:55
 */
@Mapper
public interface FileCustomMapper {

    @Select("select * from t_file_custom where backUp = 0")
    List<FileCustom> selectAll();

    @Select("select * from t_file_custom where type = #{type}")
    List<FileCustom> selectByType(String type);

    @Update("update t_file_custom set backUp = #{backUp} where id = #{id}")
    void updateBackUpById(Integer id, Integer backUp);

    @Select("select * from t_file_custom where backUp = 0 limit #{limit}")
    List<FileCustom> selectByLimit(Integer limit);
}
