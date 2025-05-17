package xyz.ctliu.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import xyz.ctliu.constants.FunctionConst;
import xyz.ctliu.constants.SQLConst;
import xyz.ctliu.domain.dto.LeaveWordIsCheckDTO;
import xyz.ctliu.domain.dto.SearchLeaveWordDTO;
import xyz.ctliu.domain.entity.*;
import xyz.ctliu.domain.response.ResponseResult;
import xyz.ctliu.domain.vo.LeaveWordListVO;
import xyz.ctliu.domain.vo.LeaveWordVO;
import xyz.ctliu.enums.CommentEnum;
import xyz.ctliu.enums.FavoriteEnum;
import xyz.ctliu.enums.LikeEnum;
import xyz.ctliu.enums.MailboxAlertsEnum;
import xyz.ctliu.mapper.*;
import xyz.ctliu.service.LeaveWordService;
import xyz.ctliu.service.PublicService;
import xyz.ctliu.utils.SecurityUtils;
import xyz.ctliu.utils.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * (LeaveWord)表服务实现类
 *
 * @author ctliu
 * @since 2023-11-03 15:01:11
 */
@Service("leaveWordService")
public class LeaveWordServiceImpl extends ServiceImpl<LeaveWordMapper, LeaveWord> implements LeaveWordService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private CommentMapper commentMapper;

    @Resource
    private LikeMapper likeMapper;

    @Resource
    private FavoriteMapper favoriteMapper;

    @Resource
    private LeaveWordMapper leaveWordMapper;

    @Override
    public List<LeaveWordVO> getLeaveWordList(String id) {
        return this.query()
                .eq(SQLConst.IS_CHECK, SQLConst.IS_CHECK_YES)
                .eq(id != null, SQLConst.ID, id)
                .orderByDesc(SQLConst.CREATE_TIME)
                .list().stream().map(leaveWord -> {
                    User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getId, leaveWord.getUserId()));
                    return leaveWord.asViewObject(LeaveWordVO.class, leaveWordVO -> leaveWordVO.setNickname(user.getNickname())
                            .setAvatar(user.getAvatar())
                            .setCommentCount(commentMapper.selectCount(new LambdaQueryWrapper<Comment>().eq(Comment::getType, CommentEnum.COMMENT_TYPE_LEAVE_WORD.getType()).eq(Comment::getIsCheck, SQLConst.IS_CHECK_YES).eq(Comment::getTypeId, leaveWord.getId())))
                            .setLikeCount(likeMapper.selectCount(new LambdaQueryWrapper<Like>().eq(Like::getType, LikeEnum.LIKE_TYPE_LEAVE_WORD.getType()).eq(Like::getTypeId, leaveWord.getId())))
                            .setFavoriteCount(favoriteMapper.selectCount(new LambdaQueryWrapper<Favorite>().eq(Favorite::getType, CommentEnum.COMMENT_TYPE_LEAVE_WORD.getType()).eq(Favorite::getTypeId, leaveWord.getId()))));
                }).toList();
    }

    @Resource
    private PublicService publicService;

    @Value("${spring.mail.username}")
    private String email;

    @Value("${mail.message-new-notice}")
    private Boolean messageNewNotice;

    @Override
    public ResponseResult<Void> userLeaveWord(String content) {
        String parse = (String) JSON.parse(content);
        if (parse.length() > FunctionConst.LEAVE_WORD_CONTENT_LENGTH) {
            return ResponseResult.failure("留言内容过长");
        }
        LeaveWord build = LeaveWord.builder().content(parse)
                .userId(SecurityUtils.getUserId()).build();

        if (this.save(build)){
            // 是否是站长本人留言
            User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getId, SecurityUtils.getUserId()));
            if (Objects.equals(user.getEmail(), email) || !messageNewNotice) return ResponseResult.success();

            // 留言成功，发送邮箱提醒给站长
            Map<String, Object> map = new HashMap<>();
            map.put("messageId",build.getId());
            publicService.sendEmail(MailboxAlertsEnum.MESSAGE_NOTIFICATION_EMAIL.getCodeStr(), email, map);

            return ResponseResult.success();
        }
        return ResponseResult.failure();
    }

    @Override
    public List<LeaveWordListVO> getBackLeaveWordList(SearchLeaveWordDTO searchDTO) {
        LambdaQueryWrapper<LeaveWord> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotNull(searchDTO)) {
            // 搜索
            List<User> users = userMapper.selectList(new LambdaQueryWrapper<User>().like(User::getUsername, searchDTO.getUserName()));
            if (!users.isEmpty())
                wrapper.in(StringUtils.isNotEmpty(searchDTO.getUserName()), LeaveWord::getUserId, users.stream().map(User::getId).collect(Collectors.toList()));
            else
                wrapper.eq(StringUtils.isNotNull(searchDTO.getUserName()), LeaveWord::getUserId, null);

            wrapper.eq(StringUtils.isNotNull(searchDTO.getIsCheck()), LeaveWord::getIsCheck, searchDTO.getIsCheck());
            if (StringUtils.isNotNull(searchDTO.getStartTime()) && StringUtils.isNotNull(searchDTO.getEndTime()))
                wrapper.between(LeaveWord::getCreateTime, searchDTO.getStartTime(), searchDTO.getEndTime());
        }
        List<LeaveWord> leaveWords = leaveWordMapper.selectList(wrapper);
        if (!leaveWords.isEmpty()) {
            return leaveWords.stream().map(leaveWord -> leaveWord.asViewObject(LeaveWordListVO.class,
                    v -> v.setUserName(userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getId, leaveWord.getUserId()))
                            .getUsername()))).toList();
        }
        return null;
    }

    @Override
    public ResponseResult<Void> isCheckLeaveWord(LeaveWordIsCheckDTO isCheckDTO) {
        if (leaveWordMapper.updateById(LeaveWord.builder().id(isCheckDTO.getId()).isCheck(isCheckDTO.getIsCheck()).build()) > 0)
            return ResponseResult.success();

        return ResponseResult.failure();
    }

    @Override
    public ResponseResult<Void> deleteLeaveWord(List<Long> ids) {
        if (leaveWordMapper.deleteBatchIds(ids) > 0) {
            // 删除点赞、收藏、评论
            likeMapper.delete(new LambdaQueryWrapper<Like>().eq(Like::getType, LikeEnum.LIKE_TYPE_LEAVE_WORD.getType()).and(a -> a.in(Like::getTypeId, ids)));
            favoriteMapper.delete(new LambdaQueryWrapper<Favorite>().eq(Favorite::getType, FavoriteEnum.FAVORITE_TYPE_LEAVE_WORD.getType()).and(a -> a.in(Favorite::getTypeId, ids)));
            commentMapper.delete(new LambdaQueryWrapper<Comment>().eq(Comment::getType, CommentEnum.COMMENT_TYPE_LEAVE_WORD.getType()).and(a -> a.in(Comment::getTypeId, ids)));
            return ResponseResult.success();
        }
        return ResponseResult.failure();
    }
}
