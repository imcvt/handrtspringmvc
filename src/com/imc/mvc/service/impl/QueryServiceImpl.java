package com.imc.mvc.service.impl;

import com.imc.mvc.annotation.Service;
import com.imc.mvc.service.QueryService;

/**
 * @author luoly
 * @date 2018/10/10 11:53
 * @description
 */
@Service("queryService")
public class QueryServiceImpl implements QueryService{

    @Override
    public String query(String name) {
        return "query name = " + name;
    }
}
