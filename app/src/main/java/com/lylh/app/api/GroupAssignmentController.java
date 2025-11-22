package com.lylh.app.api;

import com.lylh.biz.model.common.CommonResult;
import com.lylh.biz.model.common.ResultUtils;
import com.lylh.biz.model.dto.GroupAssignmentDTO;
import com.lylh.biz.model.vo.GroupAssignmentVO;
import com.lylh.biz.service.GroupAssignmentService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/group/assignment")
public class GroupAssignmentController {

    private final GroupAssignmentService groupAssignmentService;

    public GroupAssignmentController(GroupAssignmentService groupAssignmentService) {
        this.groupAssignmentService = groupAssignmentService;
    }

    @PostMapping("/create")
    public CommonResult<String> createGroupAssignment(@RequestBody GroupAssignmentDTO groupAssignmentDTO) {
        String uuid = groupAssignmentService.create(groupAssignmentDTO);
        return ResultUtils.ok(uuid);
    }

    @PostMapping("/update")
    public CommonResult<?> updateGroupAssignment(@RequestBody GroupAssignmentDTO groupAssignmentDTO) {
        groupAssignmentService.update(groupAssignmentDTO);
        return ResultUtils.ok();
    }

    @PostMapping("/delete")
    public CommonResult<?> deleteGroupAssignment(String uuid) {
        groupAssignmentService.delete(uuid);
        return ResultUtils.ok();
    }

    @GetMapping("/detail")
    public CommonResult<?> detailGroupAssignment(String uuid) {
        GroupAssignmentVO groupAssignmentVO = groupAssignmentService.getByUuid(uuid);
        return ResultUtils.ok(groupAssignmentVO);
    }

    @GetMapping("/list")
    public CommonResult<?> listGroupAssignment(@RequestParam(value = "limit") Integer limit) {
        return ResultUtils.ok(groupAssignmentService.listGroupAssignment(limit));
    }
}
