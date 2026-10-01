package com.lylh.biz.model.common;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lylh.repository.biz.model.page.MapperPage;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceUtilsTest {

    @Test
    void should_useDefaultPage_when_pageArgsMissingOrInvalid() {
        Page<String> page = ServiceUtils.toPage(null, 0);

        assertThat(page.getCurrent()).isEqualTo(ServiceUtils.PAGE_NUM.longValue());
        assertThat(page.getSize()).isEqualTo(ServiceUtils.PAGE_SIZE.longValue());
    }

    @Test
    void should_capPageSize_when_requestedSizeTooLarge() {
        Page<String> page = ServiceUtils.toPage(2, 500);

        assertThat(page.getCurrent()).isEqualTo(2L);
        assertThat(page.getSize()).isEqualTo(ServiceUtils.MAX_PAGE_SIZE.longValue());
    }

    @Test
    void should_calculateOffset_when_convertMapperPage() {
        MapperPage mapperPage = ServiceUtils.toMapperPage(ServiceUtils.toPage(3, 10));

        assertThat(mapperPage.getOffset()).isEqualTo(20);
        assertThat(mapperPage.getSize()).isEqualTo(10);
    }

    @Test
    void should_ignoreBlankValues_when_compareObjects() {
        Sample oldObject = new Sample();
        oldObject.setName("");
        Sample newObject = new Sample();
        newObject.setName(null);

        assertThat(ServiceUtils.getChangeList(oldObject, newObject)).isEmpty();
    }

    @Test
    void should_returnChangedFields_when_privateFieldDiffers() {
        Sample oldObject = new Sample();
        oldObject.setName("旧");
        Sample newObject = new Sample();
        newObject.setName("新");

        assertThat(ServiceUtils.getChangeList(oldObject, newObject)).containsExactly("name");
    }

    @Test
    void should_putAllOldItemsIntoDelete_when_syncListEmpty() {
        List<String> saveList = new ArrayList<>();
        List<String> deleteList = new ArrayList<>();

        ServiceUtils.dealSyncSaveOrDeleteList(Collections.emptyList(), Arrays.asList("a"), saveList, deleteList);

        assertThat(saveList).isEmpty();
        assertThat(deleteList).containsExactly("a");
    }

    @Test
    void should_splitSaveAndDelete_when_syncListOverlaps() {
        List<String> saveList = new ArrayList<>();
        List<String> deleteList = new ArrayList<>();

        ServiceUtils.dealSyncSaveOrDeleteList(Arrays.asList("b", "c"), Arrays.asList("a", "b"), saveList, deleteList);

        assertThat(saveList).containsExactly("c");
        assertThat(deleteList).containsExactly("a");
    }

    public static class Sample {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
