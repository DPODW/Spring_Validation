package hello.itemservice.web.validation;

import hello.itemservice.domain.item.Item;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class itemValidator implements Validator {
/**
 * item 검증 validator
 * Spring 제공 Validator interface 사용
 * supports , validator 을 구현해야 함
 * */

    @Override //검증을 지원하느냐
    public boolean supports(Class<?> clazz) {
        return Item.class.isAssignableFrom(clazz);
        /* 해당 검증 class 가 item class 를 지원하는지 조건을 따지는 로직
        *  item == clazz 와 동일하다.
        * isAssignableFrom 를 사용하는 이유는, 자식 객체까지 동일한지 따질수 있기 때문. (자식 객체도 지원되어야 한다.)
        * */
    }

    @Override //실질적 검증 로직
    public void validate(Object target, Errors errors) {
        Item item = (Item) target; /* 타입이 Object 라 캐스팅 필요
         * 타겟은 검증할 클래스
         * Errors 는 bindingResult 의 부모 타입이다. 고로 v2 에서 사용하던 bindingResult
         * 대신 Errors 를 넣어주면 정상 작동 한다.
         *  
         */

        //검증 로직
        if(!StringUtils.hasText(item.getItemName())){
            errors.rejectValue("itemName","required");
        }
        if(item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000){
            errors.rejectValue("price","range", new Object[]{1000,1000000},null);
        }

        if(item.getQuantity() == null || item.getQuantity() >= 9999){
            errors.rejectValue("quantity","max", new Object[]{9999}, null);
        }

        //특정 필드가 아닌 복합 룰 검증
        if(item.getPrice() != null && item.getQuantity() != null){
            int resultPrice = item.getPrice() * item.getQuantity();
            if(resultPrice < 10000) {
                errors.reject("totalPriceMin", new Object[]{10000,resultPrice},null);
            }
        }
    }
}
