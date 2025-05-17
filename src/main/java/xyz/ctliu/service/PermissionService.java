package xyz.ctliu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.ctliu.domain.dto.PermissionDTO;
import xyz.ctliu.domain.entity.Permission;
import xyz.ctliu.domain.response.ResponseResult;
import xyz.ctliu.domain.vo.PermissionMenuVO;
import xyz.ctliu.domain.vo.PermissionVO;

import java.util.List;


/**
 * (Permission)表服务接口
 *
 * @author ctliu
 * @since 2023-12-05 19:53:31
 */
public interface PermissionService extends IService<Permission> {

    /**
     * 查询权限
     * @param permissionDesc 权限描述
     * @param permissionKey 权限字符
     * @return 权限
     */
    List<PermissionVO> selectPermission(String permissionDesc, String permissionKey,Long permissionMenuId);

    /**
     * 查询权限菜单
     * @return 权限所在菜单
     */
    List<PermissionMenuVO> selectPermissionMenu();

    /**
     * 修改或添加
     * @param permissionDTO 权限DTO
     * @return  修改或添加权限
     */
    ResponseResult<Void> updateOrInsertPermission(PermissionDTO permissionDTO);

    /**
     * 获取修改的权限信息
     * @param id 权限id
     * @return 需要的信息
     */
    PermissionDTO getPermission(Long id);

    /**
     * 删除权限
     * @param id 权限id
     * @return 是否成功
     */
    ResponseResult<Void> deletePermission(Long id);
}
