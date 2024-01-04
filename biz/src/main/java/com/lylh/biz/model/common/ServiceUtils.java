package com.lylh.biz.model.common;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lylh.biz.model.vo.UserVO;
import com.lylh.repository.biz.model.page.MapperPage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

@Slf4j
public class ServiceUtils {

    public final static Integer PAGE_NUM = 1;
    public final static Integer PAGE_SIZE = 20;


    public static <T> Page<T> toPage(Integer current, Integer size) {
        return new Page<>(Optional.ofNullable(current).orElse(PAGE_NUM),
                Optional.ofNullable(size).map(v -> Math.max(0, v)).orElse(PAGE_SIZE));
    }

    public static <T> Page<T> getPage(Page<T> page, List<T> data, long count) {
        page.setRecords(data);
        page.setTotal(count);
        return page;
    }

    public static <T> MapperPage toMapperPage(Page<T> page) {
        MapperPage mapperPage = new MapperPage();
        mapperPage.setSize((int) page.getSize());
        mapperPage.setOffset(((int) page.getCurrent() - 1) * mapperPage.getSize());
        return mapperPage;
    }

    /**
     * 将旧对象的属性值与新对象的属性值做比较 返回修改的属性值名称
     * @param oldObject 旧对象
     * @param newObject 新对象
     * @return 修改的对象属性值名称集合
     */
    public static List<String> getChangeList(Object oldObject, Object newObject) {
        //修改的对象属性值名称集合
        List<String> changeList = new ArrayList<>();
        //根据对象获取属性值名称
        String[] filedNameArray = getFieldName(oldObject);
        //对比两个对象属性值是否相等
        int i = 0;
        while (i < filedNameArray.length) {
            String fieldName = filedNameArray[i];     //遍历所有属性
            //忽略版本对比
            if ("serialVersionUID".equals(fieldName)) {
                i++;
                continue;
            }

            Object valueOld = getFieldValueByName(fieldName, oldObject);//旧对象的值
            Object valueNew = getFieldValueByName(fieldName, newObject);//新对象的值
            // 过滤掉更新前后都是空值的情况，此类情况一般是入参与数据库分别为空字符与null
            if (("".equals(valueOld) || valueOld == null) && ("".equals(valueNew) || valueNew == null)) {
                i++;
                continue;
            }

            if (valueOld != valueNew && !Objects.equals(valueOld, valueNew)) {
                //属性值不同 ，记录属性名称
                changeList.add(fieldName);
            }
            i++;
        }
        return changeList;
    }

    /**
     * 根据对象字段名称返回 jsonStr
     * @param object 取值对象
     * @param fields 字段名
     * @return json
     */
    public static String setValueWithFields(Object object, List<String> fields) {
        if (CollectionUtils.isEmpty(fields)) {
            return null;
        }
        JSONObject jsonObject = new JSONObject();
        for (String field : fields) {
            jsonObject.put(field, getFieldValueByName(field, object));
        }
        return JSONObject.toJSONString(jsonObject, SerializerFeature.WriteMapNullValue);
    }

    /**
     * 反射获取属性名
     * @param o obj
     * @return fieldNames
     */
    private static String[] getFieldName(Object o) {
        Field[] fields = o.getClass().getFields(); //getDeclaredFields()不取父类
        String[] fieldNames = new String[fields.length];
        for (int i = 0; i < fields.length; i++) {
            fieldNames[i] = fields[i].getName();
        }
        return fieldNames;
    }

    /**
     * 根据属性名获取属性值
     * @param fieldName 属性名称
     * @param o 对象
     * @return obj
     */
    private static Object getFieldValueByName(String fieldName, Object o) {
        try {
            String firstLetter = fieldName.substring(0, 1).toUpperCase();
            String getter = "get" + firstLetter + fieldName.substring(1);
            Method method = o.getClass().getMethod(getter);
            return method.invoke(o);
        } catch (Exception e) {
            log.error("根据属性名获取属性值异常：" + e.getMessage(), e);
            return null;
        }
    }

    /**
     * 旧数据比对同步数据，找出哪些是需要新增的，哪些是需要删除的
     * @param syncList 同步过来的数据
     * @param oldList 已经有的旧数据
     * @param saveList 同步过来的数据是旧数据里没有，需要新增的
     * @param deleteList 旧数据里对同步过来的数据多余的，需要删除的
     * @param <T> 泛型t
     */
    public static <T> void dealSyncSaveOrDeleteList(List<T> syncList, List<T> oldList, List<T> saveList, List<T> deleteList) {
        if (CollectionUtil.isEmpty(syncList)) {
            deleteList.addAll(oldList);
            return;
        }
        if (CollectionUtil.isEmpty(oldList)) {
            saveList.addAll(syncList);
            return;
        }

        Set<T> oldDataSet = new HashSet<>(oldList);
        Set<T> newDataSet = new HashSet<>(syncList);
        Set<T> oldDataSetExt = new HashSet<>(oldList);
        oldDataSet.removeAll(newDataSet);
        if (CollectionUtil.isNotEmpty(oldDataSet)) {

            deleteList.addAll(oldDataSet);
        }
        newDataSet.removeAll(oldDataSetExt);
        if (CollectionUtil.isNotEmpty(newDataSet)) {
            saveList.addAll(newDataSet);
        }
    }
}
