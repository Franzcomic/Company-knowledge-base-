package com.corpedia.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.corpedia.entity.KbDocument;
import org.apache.ibatis.annotations.Mapper;

/**
 * 知识文档表 Mapper。继承 MyBatis-Plus BaseMapper，无自定义 SQL。
 */
@Mapper
public interface KbDocumentMapper extends BaseMapper<KbDocument> {
}
