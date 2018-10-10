package com.imc.mvc.controller;

import com.imc.mvc.annotation.Autowired;
import com.imc.mvc.annotation.Controller;
import com.imc.mvc.annotation.RequestMapping;
import com.imc.mvc.annotation.RequestParam;
import com.imc.mvc.service.ModifyService;
import com.imc.mvc.service.QueryService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author luoly
 * @date 2018/10/10 12:05
 * @description
 */
@Controller
@RequestMapping("/mvctest")
public class WebController {


    @Autowired
    private QueryService queryService;

    @Autowired
    private ModifyService modifyService;


    @RequestMapping("/query")
    public void search(@RequestParam("name") String name, HttpServletRequest request, HttpServletResponse response) {

        out(response, queryService.query(name));
    }

    @RequestMapping("/add")
    public void add(@RequestParam("name") String name, @RequestParam("addr") String addr, HttpServletRequest request, HttpServletResponse response) {

        out(response, modifyService.add(name, addr));
    }

    @RequestMapping("/remove")
    public void remove(@RequestParam("id") int id, HttpServletRequest request, HttpServletResponse response) {
        out(response, modifyService.remove(id));
    }

    private void out(HttpServletResponse response, String str) {
        try {
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().print(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
