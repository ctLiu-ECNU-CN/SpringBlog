package xyz.ctliu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import xyz.ctliu.constants.RespConst;
import xyz.ctliu.constants.SQLConst;
import xyz.ctliu.domain.entity.Banners;
import xyz.ctliu.domain.response.ResponseResult;
import xyz.ctliu.enums.UploadEnum;
import xyz.ctliu.exceptions.FileUploadException;
import xyz.ctliu.mapper.BannersMapper;
import xyz.ctliu.service.BannersService;
import xyz.ctliu.utils.FileUploadUtils;
import xyz.ctliu.utils.SecurityUtils;

import java.util.List;

/**
 * (Banners)表服务实现类
 *
 * @author ctliu
 * @since 2024-08-28 09:51:22
 */
@Service("bannersService")
public class BannersServiceImpl extends ServiceImpl<BannersMapper, Banners> implements BannersService {

    @Resource
    private BannersMapper bannersMapper;

    @Resource
    private FileUploadUtils fileUploadUtils;

    @Override
    public List<String> getBanners() {
        List<Banners> banners = bannersMapper.selectList(new LambdaQueryWrapper<Banners>().orderByAsc(Banners::getSortOrder));
        if (!banners.isEmpty()) return banners.stream().map(Banners::getPath).toList();
        return List.of();
    }

    @Override
    public List<Banners> backGetBanners() {
        List<Banners> banners = bannersMapper.selectList(new LambdaQueryWrapper<Banners>().orderByAsc(Banners::getSortOrder));
        if (!banners.isEmpty()){
            return banners;
        }
        return List.of();
    }

    @Transactional
    @Override
    public ResponseResult<Banners> uploadBannerImage(MultipartFile bannerImage) {
        try {
            String bannerUrl;
            try {
                // 是否到达Banner数量上限
                if (bannersMapper.selectCount(null) >= SQLConst.BANNER_MAX_COUNT) {
                    return ResponseResult.failure(RespConst.BANNER_MAX_COUNT_MSG);
                }
                bannerUrl = fileUploadUtils.upload(UploadEnum.UI_BANNERS, bannerImage);
                Banners banner = Banners.builder().size(bannerImage.getSize())
                        .type(bannerImage.getContentType())
                        .userId(SecurityUtils.getUserId())
                        .sortOrder((int) (bannersMapper.selectCount(null) + 1))
                        .path(bannerUrl).build();
                bannersMapper.insert(banner);
                return ResponseResult.success(banner);
            } catch (FileUploadException e) {
                return ResponseResult.failure(e.getMessage());
            }
        } catch (Exception e) {
            log.error(UploadEnum.UI_BANNERS.getDescription() + "上传失败", e);
            return ResponseResult.failure();
        }
    }

    @Override
    public ResponseResult<String> removeBannerById(Long id) {
        Banners banner = bannersMapper.selectById(id);
        if (this.removeById(id)) {
            // minio是否存在
            if (fileUploadUtils.isFileExist(UploadEnum.UI_BANNERS.getDir(), fileUploadUtils.getFileName(banner.getPath()))) {
                fileUploadUtils.deleteFile(UploadEnum.UI_BANNERS.getDir(), fileUploadUtils.getFileName(banner.getPath()));
            }
        } else return ResponseResult.failure("删除失败");
        return ResponseResult.success("删除成功");
    }

    @Override
    public ResponseResult<String> updateSortOrder(List<Banners> banners) {
        // 删除全部
        bannersMapper.delete(Wrappers.emptyWrapper());
        //  重新排序
        for (int i = 0; i < banners.size(); i++) {
            banners.get(i).setSortOrder(i + 1);
            bannersMapper.insert(banners.get(i));
        }
        return ResponseResult.success();
    }

}
