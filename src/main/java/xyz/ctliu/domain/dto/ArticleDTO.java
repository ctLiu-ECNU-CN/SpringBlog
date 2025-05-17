package xyz.ctliu.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import xyz.ctliu.domain.BaseData;

import java.util.List;

/**
 * @author ctliu
 * <p>
 * 创建时间：2024/1/4 14:11
 */
@Data
/**
 * ArticleDTO 是用于封装文章数据的传输对象（Data Transfer Object），
 * 该类通常用于 Controller 与 Service 或 API 接口之间传输文章相关的数据。
 * <p>
 * 包含文章的基础信息，例如标题、内容、分类、标签、状态等。
 * 并通过 Jakarta Bean Validation 注解对必填字段进行数据校验。
 * <p>
 * 注意事项：
 * - tagId 为标签 ID 列表，需在业务层处理与标签的关联关系。
 * - 不包含作者、阅读数等字段，若有需要可在扩展类中添加。
 */
public class ArticleDTO implements BaseData {
//     文章id
    private Long id;
//     分类id
    @NotNull(message = "分类id不能为空")
    private Long categoryId;
    // 标签id
    @NotNull(message = "标签id不能为空")
    private List<Long> tagId;
    //文章缩略图
    @NotNull(message = "文章缩略图不能为空")
    private String articleCover;
    //文章标题
    @NotNull(message = "文章标题不能为空")
    private String articleTitle;
    //文章内容
    @NotNull(message = "文章内容不能为空")
    private String articleContent;
    //类型 (1原创 2转载 3翻译)
    @NotNull(message = "文章类型不能为空")
    private Integer articleType;
    //是否置顶 (0否 1是）
    @NotNull(message = "是否置顶不能为空")
    private Integer isTop;
    //文章状态 (1公开 2私密 3草稿)
    @NotNull(message = "文章状态不能为空")
    private Integer status;
}
