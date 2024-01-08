package com.lylh.app.api;

import com.lylh.biz.model.common.CommonResult;
import com.lylh.biz.model.common.ResultUtils;
import com.lylh.biz.model.dto.TeamDTO;
import com.lylh.biz.model.vo.TeamWithGroupVO;
import com.lylh.biz.model.vo.TeamVO;
import com.lylh.biz.service.TeamService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/team")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @PostMapping("/create")
    public CommonResult<String> createTeam(@RequestBody TeamDTO teamDTO) {
        String uuid = teamService.create(teamDTO);
        return ResultUtils.ok(uuid);
    }

    @PostMapping("/createBatch")
    public CommonResult<?> createBatchTeam(@RequestBody List<TeamDTO> teamDTOList) {
        teamService.createBatch(teamDTOList);
        return ResultUtils.ok();
    }

    @PostMapping("/update")
    public CommonResult<?> updateTeam(@RequestBody TeamDTO teamDTO) {
        teamService.update(teamDTO);
        return ResultUtils.ok();
    }

    @PostMapping("/updateBatch")
    public CommonResult<?> updateBatchTeam(@RequestBody List<TeamDTO> teamDTOList) {
        teamService.updateBatch(teamDTOList);
        return ResultUtils.ok();
    }

    @PostMapping("/delete")
    public CommonResult<?>deleteTeam(String uuid) {
        teamService.delete(uuid);
        return ResultUtils.ok();
    }

    @GetMapping("/detail")
    public CommonResult<?> detailTeam(String uuid) {
        TeamVO teamVO = teamService.getByUuid(uuid);
        return ResultUtils.ok(teamVO);
    }

    @GetMapping("/detailWithGroup")
    public CommonResult<?> detailTeamWithGroup(String uuid) {
        TeamWithGroupVO teamWithGroupVO = teamService.getTeamWithGroupByUuid(uuid);
        return ResultUtils.ok(teamWithGroupVO);
    }

    @GetMapping("/list")
    public CommonResult<?> listTeam(@RequestParam(value = "limit") Integer limit) {
        return ResultUtils.ok(teamService.listTeam(limit));
    }

    @GetMapping("/listWithGroup")
    public CommonResult<?> listTeamWithGroup(@RequestParam(value = "limit") Integer limit) {
        return ResultUtils.ok(teamService.listTeamWithGroup(limit));
    }


}
