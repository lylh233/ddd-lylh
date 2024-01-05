package com.lylh.app.api;

import com.lylh.biz.model.common.CommonResult;
import com.lylh.biz.model.common.PageListResult;
import com.lylh.biz.model.common.ResultUtils;
import com.lylh.biz.model.dto.MemberDTO;
import com.lylh.biz.model.vo.MemberVO;
import com.lylh.biz.service.MemberService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/member")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping("/create")
    public CommonResult<String> createMember(@RequestBody MemberDTO memberDTO) {
        String uuid = memberService.create(memberDTO);
        return ResultUtils.ok(uuid);
    }

    @PostMapping("/createBatch")
    public CommonResult<?> createBatchMember(@RequestBody List<MemberDTO> memberDTOList) {
        memberService.createBatch(memberDTOList);
        return ResultUtils.ok();
    }

    @PostMapping("/update")
    public CommonResult<?> updateMember(@RequestBody MemberDTO memberDTO) {
        memberService.update(memberDTO);
        return ResultUtils.ok();
    }

    @PostMapping("/updateBatch")
    public CommonResult<?> updateBatchMember(@RequestBody List<MemberDTO> memberDTOList) {
        memberService.updateBatch(memberDTOList);
        return ResultUtils.ok();
    }

    @DeleteMapping("/delete")
    public CommonResult<?> delete(String uuid) {
        memberService.delete(uuid);
        return ResultUtils.ok();
    }

    @GetMapping("/query")
    public PageListResult<?> queryMember(@RequestParam(defaultValue = "{}") String filter,
                                       @RequestParam(value = "page_num", defaultValue = "1") Integer pageNum,
                                       @RequestParam(value = "page_size", defaultValue = "10") Integer pageSize) {
        return ResultUtils.ok(memberService.queryMember(filter, pageNum, pageSize));
    }

    @GetMapping("/list")
    public CommonResult<?> listMember(@RequestParam(defaultValue = "{}") String filter,
                                      @RequestParam(value = "limit") Integer limit) {
        return ResultUtils.ok(memberService.listMember(filter, limit));
    }

    @GetMapping("/detail")
    public CommonResult<?> detailMember(String uuid) {
        MemberVO memberVO = memberService.getByUuid(uuid);
        return ResultUtils.ok(memberVO);
    }

}
