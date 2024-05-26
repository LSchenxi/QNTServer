package com.ninelock.api.service;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.ninelock.api.request.DetectReq;
import com.ninelock.api.response.QntDetectResponse;
import com.ninelock.api.response.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class PathfinderService {

    @Value("${detectDbInfo.jdbcDriver}")
    private String jdbcDriver;
    @Value("${detectDbInfo.dbUrl}")
    private String dbUrl;
    @Value("${detectDbInfo.dbName}")
    private String dbName;
    @Value("${detectDbInfo.dbUser}")
    private String dbUser;
    @Value("${detectDbInfo.dbPassword}")
    private String dbPassword;

    public Result<?> getDetectIndexList() throws ClassNotFoundException, SQLException {
        List<Map<String, Object>> result = new ArrayList<>();
        Class.forName(jdbcDriver);
        Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
        Statement smt = conn.createStatement();
        String sql_1 = "SELECT id, dname FROM detect_index ORDER BY dsort ASC;";
        ResultSet res1 = smt.executeQuery(sql_1);
        while (res1.next()) {
            Integer id = res1.getInt("id");
            String dname = res1.getString("dname");
            Map<String, Object> info = new HashMap<>();
            info.put("id", id);
            info.put("dname", dname);
            result.add(info);
        }
        res1.close();
        // 关闭流 (先开后关)
        smt.close();
        conn.close();
        return Result.ok(result);
    }


    public Result<?> getDetectColumns(Integer currentDetectInfo) throws ClassNotFoundException, SQLException {
        Map<String, Object> result = new HashMap<>();
        int columnsTotal = 0;
        List<Map<String, Object>> data = new ArrayList<>();
        String tableName = "";
        Class.forName(jdbcDriver);
        Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
        Statement smt = conn.createStatement();
        String sql_1 = "SELECT tablename FROM detect_index WHERE id = " + currentDetectInfo + ";";
        ResultSet res1 = smt.executeQuery(sql_1);
        while (res1.next()) {
            tableName = res1.getString("tablename");
        }
        res1.close();
        if ("".equals(tableName)) {
            smt.close();
            conn.close();
            return Result.error("数据查询错误", null);
        }
        String columnsName = "";
        String sql_2 = "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = '" + dbName + "' AND TABLE_NAME = '" + tableName + "' ORDER BY ORDINAL_POSITION;";
        ResultSet res2 = smt.executeQuery(sql_2);
        while (res2.next()) {
            columnsName = res2.getString("COLUMN_NAME");
            if(!"id".equals(columnsName)){
                Map<String, Object> info = new HashMap<>();
                info.put("title", columnsName);
                info.put("key", columnsName);
                if("币对".equals(columnsName)){
                    info.put("width", 200);
                }else {
                    info.put("width", 100);
                }
                data.add(info);
                columnsTotal++;
            }
        }
        // 关闭流 (先开后关)
        smt.close();
        conn.close();
        result.put("data", data);
        result.put("totalWidth", 100 * columnsTotal + 100);
        return Result.ok(result);
    }

    public Result<?> getDetectPage(DetectReq detectReq) throws SQLException, ClassNotFoundException {
        String tableName = "";
        Class.forName(jdbcDriver);
        Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
        Statement smt = conn.createStatement();
        String sql_1 = "SELECT tablename FROM detect_index WHERE id = " + detectReq.getCurrentDetectInfo() + ";";
        ResultSet res1 = smt.executeQuery(sql_1);
        while (res1.next()) {
            tableName = res1.getString("tablename");
        }
        res1.close();
        if ("".equals(tableName)) {
            smt.close();
            conn.close();
            return Result.error("数据查询错误", null);
        }
        List<String> columnsNameList = new ArrayList<>();
        String sql_2 = "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = '" + dbName + "' AND TABLE_NAME = '" + tableName + "' ORDER BY ORDINAL_POSITION;";
        ResultSet res2 = smt.executeQuery(sql_2);
        while (res2.next()) {
            columnsNameList.add(res2.getString("COLUMN_NAME"));
        }
        res2.close();

        if (CollectionUtils.isEmpty(columnsNameList)) {
            smt.close();
            conn.close();
            return Result.error("数据查询错误", null);
        }
        Map<String,Object> result = new HashMap<>();
        List<Map<String, String>> dataList = new ArrayList<>();
        final int page = detectReq.getPage();
        final int size = detectReq.getSize();
        int limit = size*(page-1);
        Long total = 0L;
        final String symbolName = detectReq.getSymbolName();
        // 计算总数
        String sql_3 = "";
        if (null != symbolName && !"".equals(symbolName)) {
            sql_3 = "SELECT COUNT(id) FROM " + tableName + " WHERE 币对 LIKE '%"+ symbolName +"%';";
        } else {
            sql_3 = "SELECT COUNT(id) FROM " + tableName + ";";
        }
        ResultSet res3 = smt.executeQuery(sql_3);
        while (res3.next()) {
            total = res3.getLong("COUNT(id)");
        }
        res3.close();
        result.put("total", total);
        if(total == 0L){
            result.put("records", dataList);
            smt.close();
            conn.close();
            return Result.ok(result);
        }
        String sql_4 = "";
        if (null != symbolName && !"".equals(symbolName)) {
            sql_4 = "SELECT " + String.join(",", columnsNameList) + " FROM " + tableName + " WHERE 币对 LIKE '%"+ symbolName +"%' LIMIT "+limit+" ,"+ size +";";
        } else {
            sql_4 = "SELECT " + String.join(",", columnsNameList) + " FROM " + tableName + " LIMIT "+limit+" ,"+ size +";";
        }
        ResultSet res4 = smt.executeQuery(sql_4);
        while (res4.next()) {
            Map<String, String> oneRowData = new HashMap<>();
            for(String columnsName : columnsNameList){
                oneRowData.put(columnsName, res4.getString(columnsName));
            }
            dataList.add(oneRowData);
        }
        res4.close();
        smt.close();
        conn.close();
        result.put("records", dataList);
        return Result.ok(result);
    }

}
