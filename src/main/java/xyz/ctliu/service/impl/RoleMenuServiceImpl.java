package xyz.ctliu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import xyz.ctliu.domain.entity.RoleMenu;
import xyz.ctliu.mapper.RoleMenuMapper;
import xyz.ctliu.service.RoleMenuService;

/**
 * (RoleMenu)表服务实现类
 *
 * @author ctliu
 * @since 2023-11-28 10:23:17
 */
@Service("roleMenuService")
public class RoleMenuServiceImpl extends ServiceImpl<RoleMenuMapper, RoleMenu> implements RoleMenuService {

}
