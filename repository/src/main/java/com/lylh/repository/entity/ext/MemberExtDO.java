package com.lylh.repository.entity.ext;

import com.lylh.repository.entity.MemberDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class MemberExtDO extends MemberDO {

    private String occupationName;

    private String groupUuid;

}
