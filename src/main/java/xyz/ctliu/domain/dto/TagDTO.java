package xyz.ctliu.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;
import xyz.ctliu.domain.BaseData;

/**
 * @author ctliu
 * <p>
 * 创建时间：2024/1/4 10:42
 */
// set返回本身
@Accessors(chain = true)
@Data
public class TagDTO implements BaseData {
    //标签id
    private Long id;
    //标签名称
    @NotBlank(message = "标签名称不能为空")
    @Length(max = 20, message = "标签名称长度不能超过20")
    private String tagName;
}
