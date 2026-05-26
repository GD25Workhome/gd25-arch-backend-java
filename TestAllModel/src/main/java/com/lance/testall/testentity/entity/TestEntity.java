package com.lance.testall.testentity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 示例实体，对应表 test_entity。
 */
@Data
@NoArgsConstructor
@TableName("test_entity")
public class TestEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String description;

    public TestEntity(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
