package hello.itemservice.web.validation.form;
import lombok.Data;
import org.hibernate.validator.constraints.Range;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class ItemUpdateForm {
/***
 * Edit 전용 vo
 *
 */

    @NotNull
    private Long id;

    @NotBlank
    private String itemName;

    @NotNull
    @Range(min = 1000, max = 1000000, message = "가격 오류")
    private Integer price;

    @NotNull(message = "null x")
    //수량은 자유
    private Integer quantity;
}
