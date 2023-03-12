package hello.itemservice.domain.item;
import lombok.Data;
import org.hibernate.validator.constraints.Range;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
//@ScriptAssert(lang = "javascript", script = "_this.price * _this.quantity >= 10000", message = "총합이 10000원 넘게 입력해주세요")
/* object error (필드의 결과로 오류를 잡아내는) 생성 법 (@ScriptAssert)
*  ++ 실제 사용해보면 제약이 많고 복잡함. 고로 오브젝트 오류 부분은 직접 자바 코드로 작성하는 것이 안전함.
* */
public class Item {

//    @NotNull(groups = UpdateCheck.class)
    private Long id;
   // @NotBlank(groups = {SaveCheck.class, UpdateCheck.class}, message = "공백 안됌") /* 필수 아님(message) 우선순위 제일 낮음 */
    private String itemName;

   // @NotNull(groups = {SaveCheck.class, UpdateCheck.class},message = "널 안됌")
   // @Range(groups = {SaveCheck.class, UpdateCheck.class},min = 1000, max = 1000000)
    private Integer price;
   // @NotNull(message = "널 안됌",groups = {SaveCheck.class, UpdateCheck.class})
   // @Max(value = 9999, groups = {SaveCheck.class})
    private Integer quantity;

    public Item() {
    }

    public Item(String itemName, Integer price, Integer quantity) {
        this.itemName = itemName;
        this.price = price;
        this.quantity = quantity;
    }
}
