package xyz.ctliu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.ctliu.domain.dto.AddBlackListDTO;
import xyz.ctliu.domain.dto.SearchBlackListDTO;
import xyz.ctliu.domain.dto.UpdateBlackListDTO;
import xyz.ctliu.domain.entity.BlackList;
import xyz.ctliu.domain.response.ResponseResult;
import xyz.ctliu.domain.vo.BlackListVO;

import java.util.List;


/**
 * (BlackList)表服务接口
 *
 * @author ctliu
 * @since 2024-09-05 16:13:20
 */
public interface BlackListService extends IService<BlackList> {

    /**
     * 新增数据
     * @param addBlackListDTO 新增对象
     * @return 新增结果
     */
    ResponseResult<Void> addBlackList(AddBlackListDTO addBlackListDTO);

    /**
     * 获取黑名单
     * @return 黑名单
     */
    List<BlackListVO> getBlackList(SearchBlackListDTO searchBlackListDTO);

    /**
     * 修改数据
     * @param updateBlackListDTO 修改对象
     * @return 修改结果
     */
    ResponseResult<Void> updateBlackList(UpdateBlackListDTO updateBlackListDTO);

    /**
     * 删除黑名单
     * @param ids 黑名单id
     * @return 是否成功
     */
    ResponseResult<Void> deleteBlackList(List<Long> ids);
}
