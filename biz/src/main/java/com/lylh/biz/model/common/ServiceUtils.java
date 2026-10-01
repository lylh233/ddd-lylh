package com.lylh.biz.model.common;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lylh.repository.biz.model.page.MapperPage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 分页和对象比对的公共计算。页大小在这里封顶，避免每个查询各自放行超大页。
 */
@Slf4j
public class ServiceUtils {

    public static final Integer PAGE_NUM = 1;
    public static final Integer PAGE_SIZE = 20;
    /** 单页上限，防止一次查出过多数据。 */
    public static final Integer MAX_PAGE_SIZE = 200;

    /**
     * 非法页码和页大小回退到默认值，超过上限时截断。
     *
     * @param current 页码，从 1 开始
     * @param size    每页条数
     * @param <T>     记录类型
     * @return 已校正的分页对象
     */
    public static <T> Page<T> toPage(Integer current, Integer size) {
        int pageNum = current == null || current < 1 ? PAGE_NUM : current;
        int pageSize = size == null || size < 1 ? PAGE_SIZE : Math.min(size, MAX_PAGE_SIZE);
        return new Page<>(pageNum, pageSize);
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
        List<String> changeList = new ArrayList<>();
        if (oldObject == null || newObject == null) {
            return changeList;
        }
        for (String fieldName : getFieldNames(oldObject)) {
            Object valueOld = getFieldValueByName(fieldName, oldObject);
            Object valueNew = getFieldValueByName(fieldName, newObject);
            // 空串和 null 都表示没填，不当成一次修改，避免入参和库里的空值互相误报。
            if (("".equals(valueOld) || valueOld == null) && ("".equals(valueNew) || valueNew == null)) {
                continue;
            }
            if (!Objects.equals(valueOld, valueNew)) {
                changeList.add(fieldName);
            }
        }
        return changeList;
    }

    /**
     * 只序列化指定字段，避免把整行实体（例如密码）写进快照。
     *
     * @param object 取值对象
     * @param fields 字段名
     * @return json，字段为空时返回 null
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
     * 取本类和父类的实例字段。getFields 只能看到 public 字段，Lombok 实体的私有属性会全部漏掉。
     */
    private static List<String> getFieldNames(Object o) {
        List<String> fieldNames = new ArrayList<>();
        Class<?> type = o.getClass();
        while (type != null && type != Object.class) {
            for (Field field : type.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) || "serialVersionUID".equals(field.getName())) {
                    continue;
                }
                fieldNames.add(field.getName());
            }
            type = type.getSuperclass();
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
            log.error("根据属性名获取属性值异常, fieldName={}", fieldName, e);
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
            if (!CollectionUtil.isEmpty(oldList)) {
                deleteList.addAll(oldList);
            }
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
