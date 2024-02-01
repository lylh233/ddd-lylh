package com.lylh.app.api;


import com.lylh.biz.model.common.CommonResult;
import com.lylh.biz.model.common.ResultUtils;
import com.lylh.biz.model.dto.OccupationDTO;
import com.lylh.biz.model.vo.OccupationVO;
import com.lylh.biz.service.OccupationService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/occupation")
public class OccupationController {

    private final OccupationService occupationService;

    public OccupationController(OccupationService occupationService) {
        this.occupationService = occupationService;
    }

    @PostMapping("/create")
    public CommonResult<String> createOccupation(@RequestBody OccupationDTO occupationDTO) {
        String uuid = occupationService.create(occupationDTO);
        return ResultUtils.ok(uuid);
    }

    @PostMapping("/update")
    public CommonResult<?> updateOccupation(@RequestBody OccupationDTO occupationDTO) {
        occupationService.update(occupationDTO);
        return ResultUtils.ok();
    }

    @PostMapping("/delete")
    public CommonResult<?>deleteOccupation(String uuid) {
        occupationService.delete(uuid);
        return ResultUtils.ok();
    }

    @GetMapping("/detail")
    public CommonResult<?> detailOccupation(String uuid) {
        OccupationVO occupationVO = occupationService.getByUuid(uuid);
        return ResultUtils.ok(occupationVO);
    }

    @GetMapping("/list")
    public CommonResult<?> listOccupation(@RequestParam(value = "limit", required = false) Integer limit) {
        return ResultUtils.ok(occupationService.listOccupation(limit));
    }

}
