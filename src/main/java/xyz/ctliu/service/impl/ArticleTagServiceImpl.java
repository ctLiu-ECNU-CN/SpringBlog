package xyz.ctliu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import xyz.ctliu.domain.entity.ArticleTag;
import xyz.ctliu.mapper.ArticleTagMapper;
import xyz.ctliu.service.ArticleTagService;

/**
 * (ArticleTag)表服务实现类
 *
 * @author ctliu
 * @since 2023-10-15 02:29:13
 */
@Service("articleTagService")
public class ArticleTagServiceImpl extends ServiceImpl<ArticleTagMapper, ArticleTag> implements ArticleTagService {

}
