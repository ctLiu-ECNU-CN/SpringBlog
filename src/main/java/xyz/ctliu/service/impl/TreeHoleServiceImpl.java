package xyz.ctliu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import xyz.ctliu.constants.SQLConst;
import xyz.ctliu.domain.dto.SearchTreeHoleDTO;
import xyz.ctliu.domain.dto.TreeHoleIsCheckDTO;
import xyz.ctliu.domain.entity.TreeHole;
import xyz.ctliu.domain.entity.User;
import xyz.ctliu.domain.response.ResponseResult;
import xyz.ctliu.domain.vo.TreeHoleListVO;
import xyz.ctliu.domain.vo.TreeHoleVO;
import xyz.ctliu.mapper.TreeHoleMapper;
import xyz.ctliu.mapper.UserMapper;
import xyz.ctliu.service.TreeHoleService;
import xyz.ctliu.utils.SecurityUtils;
import xyz.ctliu.utils.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * (TreeHole)表服务实现类
 *
 * @author ctliu
 * @since 2023-10-30 11:14:14
 */
@Service("treeHoleService")
public class TreeHoleServiceImpl extends ServiceImpl<TreeHoleMapper, TreeHole> implements TreeHoleService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private TreeHoleMapper treeHoleMapper;

    @Override
    public ResponseResult<Void> addTreeHole(String content) {
        if (this.save(TreeHole.builder().userId(SecurityUtils.getUserId()).content(content).build())) {
            return ResponseResult.success();
        }
        return ResponseResult.failure();
    }

    @Override
    public List<TreeHoleVO> getTreeHole() {
        List<TreeHole> treeHoles = treeHoleMapper.selectList(new LambdaQueryWrapper<TreeHole>().eq(TreeHole::getIsCheck, SQLConst.IS_CHECK_YES));
        if (treeHoles.isEmpty()) return null;
        return treeHoles.stream().map(treeHole -> treeHole.asViewObject(TreeHoleVO.class, treeHoleVO -> {
            User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getId, treeHole.getUserId()));
            treeHoleVO.setNickname(user.getUsername());
            treeHoleVO.setAvatar(user.getAvatar());
        })).collect(Collectors.toList());
    }

    @Override
    public List<TreeHoleListVO> getBackTreeHoleList(SearchTreeHoleDTO searchDTO) {
        LambdaQueryWrapper<TreeHole> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotNull(searchDTO)) {
            // 搜索
            List<User> users = userMapper.selectList(new LambdaQueryWrapper<User>().like(User::getUsername, searchDTO.getUserName()));
            if (!users.isEmpty())
                wrapper.in(StringUtils.isNotEmpty(searchDTO.getUserName()), TreeHole::getUserId, users.stream().map(User::getId).collect(Collectors.toList()));
            else
                wrapper.eq(StringUtils.isNotNull(searchDTO.getUserName()), TreeHole::getUserId, null);

            wrapper.eq(StringUtils.isNotNull(searchDTO.getIsCheck()), TreeHole::getIsCheck, searchDTO.getIsCheck());
            if (StringUtils.isNotNull(searchDTO.getStartTime()) && StringUtils.isNotNull(searchDTO.getEndTime()))
                wrapper.between(TreeHole::getCreateTime, searchDTO.getStartTime(), searchDTO.getEndTime());
        }
        List<TreeHole> treeHoles = treeHoleMapper.selectList(wrapper);
        if (!treeHoles.isEmpty()) {
            return treeHoles.stream().map(treeHole -> treeHole.asViewObject(TreeHoleListVO.class,
                    v -> v.setUserName(userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getId, treeHole.getUserId()))
                            .getUsername()))).toList();
        }
        return null;
    }

    @Override
    public ResponseResult<Void> isCheckTreeHole(TreeHoleIsCheckDTO isCheckDTO) {
        if (treeHoleMapper.updateById(TreeHole.builder().id(isCheckDTO.getId()).isCheck(isCheckDTO.getIsCheck()).build()) > 0)
            return ResponseResult.success();

        return ResponseResult.failure();
    }

    @Override
    public ResponseResult<Void> deleteTreeHole(List<Long> ids) {
        if (treeHoleMapper.deleteBatchIds(ids) > 0) {
            return ResponseResult.success();
        }
        return ResponseResult.failure();
    }


}
