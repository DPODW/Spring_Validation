package hello.itemservice.validadation;


import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.validation.DefaultMessageCodesResolver;
import org.springframework.validation.MessageCodesResolver;
import org.springframework.validation.ObjectError;

import static org.assertj.core.api.Assertions.*;

public class MessageCodesResolverTest {

    MessageCodesResolver codesResolver = new DefaultMessageCodesResolver();
            //에러코드를 하나 넣으면 여러개의 코드를 반환해줌


    @Test
    void messageCodesResolverObject() {
        String[] messageCodes = codesResolver.resolveMessageCodes("required", "item");
        for(String messageCode : messageCodes){

            /* 에러 코드를 하나 넣으면 여러개의 코드를 반환해줌 -> 즉 범용 에러 "required" 부터 세부적인 "required.item" 까지 다 반환해줌
            * 그러므로 [] 배열 타입으로 지정해주어야 함. 반환 확인시 -> for 반복으로 배열을 돌려서 확인 할수 있음
            * 우선순위는 디테일 할수록 높아짐 ( 1.  "required.item" 2. "required")
            * */
        }
        assertThat(messageCodes).containsExactly("required.item","required");
        // containsExactly: 배열 원소가 정확한지 확인하는 메소드
    }

    @Test
    void messageCodesResolverField() {
        String[] messageCodes = codesResolver.resolveMessageCodes("required", "item", "itemName", String.class);
        /* 필드 name(html view 에서 사용하는 이름) 과 필드의 타입을 지정해준다. */
        for (String messageCode : messageCodes) {

        }
        /* 필드 에러는 오브젝트 에러에 비해 좀 더 자세하게 에러 코드들을 반환 한다.
        * 디테일 한 순서로 내려오다가, 에러의 타입을 묻는 부분도 제공 해준다. (3번때로 디테일) */

        assertThat(messageCodes).containsExactly("required.item.itemName","required.itemName","required.java.lang.String","required");

    }
}
