package xyz.ctliu.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import xyz.ctliu.annotation.AccessLimit;
import xyz.ctliu.annotation.CheckBlacklist;
import xyz.ctliu.annotation.LogAnnotation;
import xyz.ctliu.constants.LogConst;
import xyz.ctliu.domain.dto.CommentIsCheckDTO;
import xyz.ctliu.domain.dto.SearchCommentDTO;
import xyz.ctliu.domain.dto.UserCommentDTO;
import xyz.ctliu.domain.response.ResponseResult;
import xyz.ctliu.domain.vo.ArticleCommentVO;
import xyz.ctliu.domain.vo.CommentListVO;
import xyz.ctliu.domain.vo.PageVO;
import xyz.ctliu.service.CommentService;
import xyz.ctliu.utils.ControllerUtils;

import java.util.List;

/**
 * @author ctliu
 * <p>
 * 创建时间：2023/11/3 10:12
 */
@RestController
@Tag(name = "评论相关接口")
@RequestMapping("/comment")
@Validated
public class CommentController {

    @Resource
    private CommentService commentService;

    @Operation(summary = "获取评论")
    @Parameters({
            @Parameter(name = "type", description = "评论类型", required = true),
            @Parameter(name = "typeId", description = "评论id", required = true),
            @Parameter(name = "pageNum", description = "页码", required = true),
            @Parameter(name = "pageSize", description = "每页数量", required = true)
    })
    @AccessLimit(seconds = 60, maxCount = 60)
    @GetMapping("/getComment")
    public ResponseResult<PageVO<List<ArticleCommentVO>>> comment(
            @Valid @NotNull Integer type,
            @Valid @NotNull Integer typeId,
            @Valid @NotNull Integer pageNum,
            @Valid @NotNull Integer pageSize
    ) {
        return ControllerUtils.messageHandler((() -> commentService.getComment(type, typeId, pageNum, pageSize)));
    }

    @CheckBlacklist
    @Operation(summary = "用户添加评论")
    @Parameter(name = "commentDTO", description = "评论信息", required = true)
    @AccessLimit(seconds = 60, maxCount = 10)
    @PostMapping("/auth/add/comment")
    public ResponseResult<String> userComment(@Valid @RequestBody UserCommentDTO commentDTO) {
        return commentService.userComment(commentDTO);
    }

    @PreAuthorize("hasAnyAuthority('blog:comment:list')")
    @Operation(summary = "后台评论列表")
    @AccessLimit(seconds = 60, maxCount = 30)
    @LogAnnotation(module="评论管理",operation= LogConst.GET)
    @GetMapping("/back/list")
    public ResponseResult<List<CommentListVO>> backList() {
        return ControllerUtils.messageHandler(() -> commentService.getBackCommentList(null));
    }

    @PreAuthorize("hasAnyAuthority('blog:comment:search')")
    @Operation(summary = "搜索后台评论列表")
    @AccessLimit(seconds = 60, maxCount = 30)
    @LogAnnotation(module="评论管理",operation= LogConst.SEARCH)
    @PostMapping("/back/search")
    public ResponseResult<List<CommentListVO>> backList(@RequestBody SearchCommentDTO searchDTO) {
        return ControllerUtils.messageHandler(() -> commentService.getBackCommentList(searchDTO));
    }

    @PreAuthorize("hasAnyAuthority('blog:comment:isCheck')")
    @Operation(summary = "修改评论是否通过")
    @AccessLimit(seconds = 60, maxCount = 30)
    @LogAnnotation(module="评论管理",operation= LogConst.UPDATE)
    @PostMapping("/back/isCheck")
    public ResponseResult<Void> isCheck(@RequestBody @Valid CommentIsCheckDTO commentIsCheckDTO) {
        return commentService.isCheckComment(commentIsCheckDTO);
    }

    @PreAuthorize("hasAnyAuthority('blog:comment:delete')")
    @Operation(summary = "删除评论")
    @AccessLimit(seconds = 60, maxCount = 30)
    @LogAnnotation(module="评论管理",operation= LogConst.DELETE)
    @DeleteMapping("/back/delete/{id}")
    public ResponseResult<Void> delete(@PathVariable("id") Long id) {
        return commentService.deleteComment(id);
    }
}
