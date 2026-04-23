package com.example.Yoga_fitness.module;

import lombok.Data;

@Data
public class QueryModule {
    private Integer userId;

    private String userRole;

    private String searchKeyword;

    private Integer pageNum;

    private Integer pageSize;

    private String sortField;

    private String sortOrder;

    private String sortPart;


}
