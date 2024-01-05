package com.lylh.repository.entity.ext;

import com.lylh.repository.entity.GroupDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class GroupExtDO extends GroupDO {

    private String groupAssignmentName;

}
