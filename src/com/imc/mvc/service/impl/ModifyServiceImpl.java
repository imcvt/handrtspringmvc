package com.imc.mvc.service.impl;

import com.imc.mvc.annotation.Service;
import com.imc.mvc.service.ModifyService;

/**
 * @author luoly
 * @date 2018/10/10 11:54
 * @description
 */
@Service("modifyService")
public class ModifyServiceImpl implements ModifyService {

    @Override
    public String add(String name, String addr) {
        return "add name= " + name + ",addr= " +addr;
    }

    @Override
    public String remove(Integer id) {
        return "remove id = " + id;
    }
}
