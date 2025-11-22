package com.lylh.app.api;

import com.lylh.biz.model.common.CommonResult;
import com.lylh.biz.model.common.ResultUtils;
import com.lylh.biz.model.dto.GroupDTO;
import com.lylh.biz.service.GroupService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/group")
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @PostMapping("/create")
    public CommonResult<String> createGroup(@RequestBody GroupDTO groupDTO) {
        String uuid = groupService.create(groupDTO);
        return ResultUtils.ok(uuid);
    }

    @PostMapping("/createBatch")
    public CommonResult<?> createBatchMember(@RequestBody List<GroupDTO> groupDTOList) {
        groupService.createBatch(groupDTOList);
        return ResultUtils.ok();
    }

    @PostMapping("/update")
    public CommonResult<?> updateGroup(@RequestBody GroupDTO groupDTO) {
        groupService.update(groupDTO);
        return ResultUtils.ok();
    }

    @PostMapping("/updateBatch")
    public CommonResult<?> updateBatchMember(@RequestBody List<GroupDTO> groupDTOList) {
        groupService.updateBatch(groupDTOList);
        return ResultUtils.ok();
    }

    @PostMapping("/delete")
    public CommonResult<?> deleteGroup(String uuid) {
        groupService.delete(uuid);
        return ResultUtils.ok();
    }

}
