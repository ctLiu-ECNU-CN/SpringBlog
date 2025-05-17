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
import xyz.ctliu.domain.dto.FavoriteIsCheckDTO;
import xyz.ctliu.domain.dto.SearchFavoriteDTO;
import xyz.ctliu.domain.response.ResponseResult;
import xyz.ctliu.domain.vo.FavoriteListVO;
import xyz.ctliu.service.FavoriteService;
import xyz.ctliu.utils.ControllerUtils;

import java.util.List;

/**
 * @author ctliu
 * <p>
 * 创建时间：2023/11/3 10:16
 */
@RestController
@Tag(name = "收藏相关接口")
@RequestMapping("/favorite")
@Validated
public class FavoriteController {

    @Resource
    private FavoriteService favoriteService;

    @CheckBlacklist
    @Operation(summary = "收藏")
    @Parameters({
            @Parameter(name = "type", description = "收藏类型", required = true),
            @Parameter(name = "typeId", description = "收藏id", required = true)
    })
    @AccessLimit(seconds = 60, maxCount = 10)
    @PostMapping("/auth/favorite")
    public ResponseResult<Void> favorite(
            @Valid @NotNull @RequestParam("type") Integer type,
            @RequestParam(value = "typeId", required = false) Long typeId
    ) {
        return favoriteService.userFavorite(type, typeId);
    }

    @CheckBlacklist
    @Operation(summary = "取消收藏")
    @Parameters({
            @Parameter(name = "type", description = "收藏类型", required = true),
            @Parameter(name = "typeId", description = "收藏id", required = true)
    })
    @AccessLimit(seconds = 60, maxCount = 10)
    @DeleteMapping("/auth/favorite")
    public ResponseResult<Void> cancelFavorite(
            @Valid @NotNull @RequestParam("type") Integer type,
            @RequestParam(value = "typeId", required = false) Integer typeId
    ) {
        return favoriteService.cancelFavorite(type, typeId);
    }

    @Operation(summary = "是否已经收藏")
    @Parameters({
            @Parameter(name = "type", description = "收藏类型", required = true),
            @Parameter(name = "typeId", description = "收藏id", required = true)
    })
    @AccessLimit(seconds = 60, maxCount = 60)
    @GetMapping("/whether/favorite")
    public ResponseResult<Boolean> isFavorite(
            @Valid @NotNull @RequestParam("type") Integer type,
            @RequestParam(value = "typeId", required = false) Integer typeId
    ) {
        return ControllerUtils.messageHandler((() -> favoriteService.isFavorite(type, typeId)));
    }

    @PreAuthorize("hasAnyAuthority('blog:favorite:list')")
    @Operation(summary = "后台收藏列表")
    @AccessLimit(seconds = 60, maxCount = 30)
    @LogAnnotation(module="收藏管理",operation= LogConst.GET)
    @GetMapping("/back/list")
    public ResponseResult<List<FavoriteListVO>> backList() {
        return ControllerUtils.messageHandler(() -> favoriteService.getBackFavoriteList(null));
    }

    @PreAuthorize("hasAnyAuthority('blog:favorite:search')")
    @Operation(summary = "搜索后台收藏列表")
    @AccessLimit(seconds = 60, maxCount = 30)
    @LogAnnotation(module="收藏管理",operation= LogConst.SEARCH)
    @PostMapping("/back/search")
    public ResponseResult<List<FavoriteListVO>> backList(@RequestBody SearchFavoriteDTO searchDTO) {
        return ControllerUtils.messageHandler(() -> favoriteService.getBackFavoriteList(searchDTO));
    }

    @PreAuthorize("hasAnyAuthority('blog:favorite:isCheck')")
    @Operation(summary = "修改收藏是否通过")
    @AccessLimit(seconds = 60, maxCount = 30)
    @LogAnnotation(module="收藏管理",operation= LogConst.UPDATE)
    @PostMapping("/back/isCheck")
    public ResponseResult<Void> isCheck(@RequestBody @Valid FavoriteIsCheckDTO favoriteIsCheckDTO) {
        return favoriteService.isCheckFavorite(favoriteIsCheckDTO);
    }

    @PreAuthorize("hasAnyAuthority('blog:favorite:delete')")
    @Operation(summary = "删除收藏")
    @AccessLimit(seconds = 60, maxCount = 30)
    @LogAnnotation(module="收藏管理",operation= LogConst.DELETE)
    @DeleteMapping("/back/delete")
    public ResponseResult<Void> delete(@RequestBody List<Long> ids) {
        return favoriteService.deleteFavorite(ids);
    }

}
