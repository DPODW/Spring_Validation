package hello.itemservice.web.validation;

import hello.itemservice.domain.item.Item;
import hello.itemservice.domain.item.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/validation/v2/items")
@RequiredArgsConstructor
public class ValidationItemControllerV2 {

    private final ItemRepository itemRepository;
    private final itemValidator itemValidator;

    @InitBinder
    /* InitBinder: 특정 컨트롤러에서 바인딩 또는 검증 설정을 변경하고 싶을 때 사용. (ValidationItemControllerV2 에만 해당된다는 뜻)
    *  WebDataBinder: 스프링의 파라미터 바인딩의 역할을 해주고 검증 기능도 내부에 포함한다. (스프링 제공 객체)
    *   ㄴ 우리가 만든 검증기를 여기에 넣으면 해당 컨트롤러에서는 검증기를 자동으로 적용할수 있다.
    *   ㄴ 사용자 요청이 있을때 마다 새로 요청된다.
    *   ㄴ 컨트롤러 호출 시, 제일 먼저 호출된다.
    * */
    public void init(WebDataBinder dataBinder) {
        dataBinder.addValidators(itemValidator);
    }


    @GetMapping
    public String items(Model model) {
        List<Item> items = itemRepository.findAll();
        model.addAttribute("items", items);
        return "validation/v2/items";
    }

    @GetMapping("/{itemId}")
    public String item(@PathVariable long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v2/item";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("item", new Item());
        return "validation/v2/addForm";
    }
    /** *****
     *  특정 필드와 관련된 에러는 FieldError("객체 이름", "필드 이름", "에러 메시지")로 넘기고,
     *  전체와 관련 있는 에러는 ObjectError("객체 이름", "에러 메시지")로 작성해주면 된다.
     * ******/

//    @PostMapping("/add")
    public String addItemV1(@ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {

         /**
          * BindingResult
          * 검증 오류가 발생할 경우, 오류의 내용을 보관하는 스프링 프레임워크 제공 객체다.
          * v1 에서 우리가 임의로 만든 error 객체와 동일한 역할을 한다.
          *
          * ++ sql 예외 (integer 타입인데 string 접근등) 로 인해 컨트롤러 접근 조차 못하고 예외 페이지로 넘어가는 경우,
          * BindingResult 를 사용하면 일단 컨트롤러를 실행헤주고, 예외 내용을 출력해준다. (로그나 타임리프를 통해)
          * */
        //검증 로직
        if(!StringUtils.hasText(item.getItemName())){
            bindingResult.addError(new FieldError("item", "itemName", "상품 이름은 필수입니다"));
        }

        if(item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000){
            bindingResult.addError(new FieldError("item", "price", "가격은 1000 이상 1000000 이하 여야 한다"));
        }

        if(item.getQuantity() == null || item.getQuantity() >= 9999){
            bindingResult.addError(new FieldError("item", "price", "수량은 최대 9999"));
        }

        //특정 필드가 아닌 복합 룰 검증
        if(item.getPrice() != null && item.getQuantity() != null){
            int resultPrice = item.getPrice() * item.getQuantity();
            if(resultPrice < 10000) {
               bindingResult.addError(new ObjectError("item","가격 * 수량의 합은 10,000원 이상 이어야 합니다."+resultPrice));
            }
        }

        //검증에 실패하면 다시 입력 폼으로
        if(bindingResult.hasErrors()) {
            log.info("error = {}", bindingResult);
            model.addAttribute("error={}",bindingResult);
            return "validation/v2/addForm";
        }

        // 성공 로직
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }

//    @PostMapping("/add")
    public String addItem(@ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {

        //검증 로직
        if(!StringUtils.hasText(item.getItemName())){
            bindingResult.addError(new FieldError("item", "itemName",item.getItemName(), false , null ,null ,"상품 이름은 필수입니다"));
        }
        //field error 는 사용자가 잘못된 값을 적어도, 그 값을 기억하고, 지우지 않는 기능이 있음. -> field 옆에("itemName") 사용자 로부터 받아온 값을 적으면 됌.(rejectedValue)
        //bindingFailure : 바인딩이 실패했는지(model 에) 묻는 속성. 우린 정상적으로 바인딩 되었음으로 false

        if(item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000){
            bindingResult.addError(new FieldError("item", "price",item.getPrice(),false,null,null, "가격은 1000 이상 1000000 이하 여야 한다"));
        }

        if(item.getQuantity() == null || item.getQuantity() >= 9999){
            bindingResult.addError(new FieldError("item", "quantity",item.getQuantity(),false,null,null, "수량은 최대 9999"));
        }

        //특정 필드가 아닌 복합 룰 검증
        if(item.getPrice() != null && item.getQuantity() != null){
            int resultPrice = item.getPrice() * item.getQuantity();
            if(resultPrice < 10000) {
                bindingResult.addError(new ObjectError("item",null,null,"가격 * 수량의 합은 10,000원 이상 이어야 합니다."+resultPrice));
                //object error -> 여러 필드들이 넘어오면, 그 필드들을 조합해서 검증함 + 오류를 뱉음
            }
        }

        //검증에 실패하면 다시 입력 폼으로
        if(bindingResult.hasErrors()) {
            log.info("error = {}", bindingResult);
            model.addAttribute("error={}",bindingResult);
            return "validation/v2/addForm";
        }

        // 성공 로직
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }

//    @PostMapping("/add")
    public String addItemV3_1(@ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {

        //검증 로직
        if(!StringUtils.hasText(item.getItemName())){
            bindingResult.addError(new FieldError("item", "itemName",item.getItemName(), false , null ,null ,"상품 이름은 필수입니다"));
        }
        //field error 는 사용자가 잘못된 값을 적어도, 그 값을 기억하고, 지우지 않는 기능이 있음. -> field 옆에("itemName") 사용자 로부터 받아온 값을 적으면 됌.(rejectedValue)
        //bindingFailure : 바인딩이 실패했는지(model 에) 묻는 속성. 우린 정상적으로 바인딩 되었음으로 false

        if(item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000){
            bindingResult.addError(new FieldError("item", "price",item.getPrice(),false,null,null, "가격은 1000 이상 1000000 이하 여야 한다"));
        }

        if(item.getQuantity() == null || item.getQuantity() >= 9999){
            bindingResult.addError(new FieldError("item", "quantity",item.getQuantity(),false,null,null, "수량은 최대 9999"));
        }

        //특정 필드가 아닌 복합 룰 검증
        if(item.getPrice() != null && item.getQuantity() != null){
            int resultPrice = item.getPrice() * item.getQuantity();
            if(resultPrice < 10000) {
                bindingResult.addError(new ObjectError("item",null,null,"가격 * 수량의 합은 10,000원 이상 이어야 합니다."+resultPrice));
                //object error -> 여러 필드들이 넘어오면, 그 필드들을 조합해서 검증함 + 오류를 뱉음
            }
        }

        //검증에 실패하면 다시 입력 폼으로
        if(bindingResult.hasErrors()) {
            log.info("error = {}", bindingResult);
            model.addAttribute("error={}",bindingResult);
            return "validation/v2/addForm";
        }

        // 성공 로직
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }



//    @PostMapping("/add")
    public String addItemV3_2(@ModelAttribute Item item,
                              BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {
        log.info("objectName={}",bindingResult.getObjectName());
        log.info("target={}",bindingResult.getTarget());

        //검증 로직
        if(!StringUtils.hasText(item.getItemName())){
            bindingResult.addError(new FieldError("item", "itemName",item.getItemName(), false , new String[]{"required.item.itemName","required.default"} ,null ,null));
        }
        /*field error 는 사용자가 잘못된 값을 적어도, 그 값을 기억하고, 지우지 않는 기능이 있음. -> field 옆에("itemName") 사용자 로부터 받아온 값을 적으면 됌.(rejectedValue)
        bindingFailure : 바인딩이 실패했는지(model 에) 묻는 속성. 우린 정상적으로 바인딩 되었음으로 false
        code: 프로퍼티에서 가져올 문자 (String array 타입) ++ code 를 사용하면 더이상 디폴트 메시지는 필요없어진다.
        argument: 프로퍼티에 {0} {1} 부분과 매칭될 아규먼트
        */

        if(item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000){
            bindingResult.addError(new FieldError("item", "price",item.getPrice(),false,new String[]{"range.item.price","required.default"},new Object[]{1000,1000000}, null));
        }

        if(item.getQuantity() == null || item.getQuantity() >= 9999){
            bindingResult.addError(new FieldError("item", "quantity",item.getQuantity(),false,new String[]{"max.item.quantity","required.default"},new Object[]{9999}, null));
        }

        //특정 필드가 아닌 복합 룰 검증
        if(item.getPrice() != null && item.getQuantity() != null){
            int resultPrice = item.getPrice() * item.getQuantity();
            if(resultPrice < 10000) {
                bindingResult.addError(new ObjectError("item",new String[]{"totalPriceMin"},new Object[]{10000,item.getPrice()},null));
                //object error -> 여러 필드들이 넘어오면, 그 필드들을 조합해서 검증함 + 오류를 뱉음
            }
        }
        /**
         * code 부분에서 첫번째 프로퍼티를 찾지 못하면, required.default 프로퍼티가 출력되고,(<- 필수 x )
         * 그것 마저도 찾지 못하면 디폴트 메시지가 출력된다.
         * 상단의 모든것을 찾지 못하면 에러 페이지로 넘어간다.
         * */
        //검증에 실패하면 다시 입력 폼으로
        if(bindingResult.hasErrors()) {
            log.info("error = {}", bindingResult);
            model.addAttribute("error={}",bindingResult);
            return "validation/v2/addForm";
        }

        // 성공 로직
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }

//    @PostMapping("/add")
    public String addItemV3_3(@ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {
        log.info("objectName={}",bindingResult.getObjectName());
        log.info("target={}",bindingResult.getTarget());
        /**
         * rejectValue: v3_2 에서 일일히 하던 일을 자동으로 제공해주는 bindingResult 제공 속성
         *  rejectValue() -> 필드(객체의 프로퍼티)에 대한 에러정보 추가(에러코드 및 메시지, 메시지 인자 전달)
         *  
         *  reject: 같은 기능이나, 글로벌 에러에 사용
         * */
        //검증 로직
        if(!StringUtils.hasText(item.getItemName())){
            bindingResult.rejectValue("itemName","required");
        }
        if(item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000){
            bindingResult.rejectValue("price","range", new Object[]{1000,1000000},null);

        }

        if(item.getQuantity() == null || item.getQuantity() >= 9999){
            bindingResult.rejectValue("quantity","max", new Object[]{9999}, null);
        }

        //특정 필드가 아닌 복합 룰 검증
        if(item.getPrice() != null && item.getQuantity() != null){
            int resultPrice = item.getPrice() * item.getQuantity();
            if(resultPrice < 10000) {
                bindingResult.reject("totalPriceMin", new Object[]{10000,resultPrice},null);
            }
        }
        //검증에 실패하면 다시 입력 폼으로
        if(bindingResult.hasErrors()) {
            log.info("error = {}", bindingResult);
            model.addAttribute("error={}",bindingResult);
            return "validation/v2/addForm";
        }

        // 성공 로직
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }


//    @PostMapping("/add")
    public String addItemV4(@ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {
        itemValidator.validate(item, bindingResult);

        //검증에 실패하면 다시 입력 폼으로
        if(bindingResult.hasErrors()) {
            log.info("error = {}", bindingResult);
            model.addAttribute("error={}",bindingResult);
            return "validation/v2/addForm";
        }

        // 성공 로직
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }

    @PostMapping("/add")
    public String addItemV5(@Validated  @ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {
       //@Validated <- 검증기 (dataBinder) 를 실행하라는 어노테이션

        //검증에 실패하면 다시 입력 폼으로
        if(bindingResult.hasErrors()) {
            log.info("error = {}", bindingResult);
            model.addAttribute("error={}",bindingResult);
            return "validation/v2/addForm";
        }

        // 성공 로직
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }

    @GetMapping("/{itemId}/edit")
    public String editForm(@PathVariable Long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v2/editForm";
    }

    @PostMapping("/{itemId}/edit")
    public String edit(@PathVariable Long itemId, @ModelAttribute Item item) {
        itemRepository.update(itemId, item);
        return "redirect:/validation/v2/items/{itemId}";
    }

}

