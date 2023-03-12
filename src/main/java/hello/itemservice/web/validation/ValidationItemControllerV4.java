package hello.itemservice.web.validation;

import hello.itemservice.domain.item.Item;
import hello.itemservice.domain.item.ItemRepository;
import hello.itemservice.domain.item.SaveCheck;
import hello.itemservice.domain.item.UpdateCheck;
import hello.itemservice.web.validation.form.ItemSaveForm;
import hello.itemservice.web.validation.form.ItemUpdateForm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/validation/v4/items")
@RequiredArgsConstructor
public class ValidationItemControllerV4 {

    private final ItemRepository itemRepository;

    @GetMapping
    public String items(Model model) {
        List<Item> items = itemRepository.findAll();
        model.addAttribute("items", items);
        return "validation/v4/items";
    }

    @GetMapping("/{itemId}")
    public String item(@PathVariable long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v4/item";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("item", new Item());
        return "validation/v4/addForm";
    }

    @PostMapping("/add")
    public String addItemV5(@Validated @ModelAttribute("item") ItemSaveForm form, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
       /* @Validated <- 검증 관련 속성을 적용하라는 어노테이션
       *   검증 범위 제한을 위한 class 지정 가능
       *   기능마다 vo 분리 (ItemSaveForm form)
       * */

        //Object error
        if(form.getPrice() != null && form.getQuantity() != null){
            int resultPrice = form.getPrice() * form.getQuantity();
            if(resultPrice < 10000) {
                bindingResult.reject("totalPriceMin", new Object[]{10000,resultPrice},null);
            }
        }

        //검증에 실패하면 다시 입력 폼으로
        if(bindingResult.hasErrors()) {
            log.info("error = {}", bindingResult);
            return "validation/v4/addForm";
        }

        /* 성공시 item(vo) 에 form(save vo) 를 넣음
        *  매칭 되는 value 만 넣어야 겠지?
        *  */
        Item item = new Item();
        item.setItemName(form.getItemName());
        item.setPrice(form.getPrice());
        item.setQuantity(form.getQuantity());
        
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v4/items/{itemId}";
    }

    @GetMapping("/{itemId}/edit")
    public String editForm(@PathVariable Long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v4/editForm";
    }

    @PostMapping("/{itemId}/edit")
    public String edit(@PathVariable Long itemId, @Validated @ModelAttribute("item") ItemUpdateForm form, BindingResult bindingResult) {
        if(form.getPrice() != null && form.getQuantity() != null){
            int resultPrice = form.getPrice() * form.getQuantity();
            if(resultPrice < 10000) {
                bindingResult.reject("totalPriceMin", new Object[]{10000,resultPrice},null);
            }
        }
        //검증에 실패하면 다시 입력 폼으로
        if(bindingResult.hasErrors()) {
            log.info("error = {}", bindingResult);
            return "validation/v4/editForm";
        }
        Item itemParam = new Item();
        itemParam.setItemName(form.getItemName());
        itemParam.setPrice(form.getPrice());
        itemParam.setQuantity(form.getQuantity());

        itemRepository.update(itemId, itemParam);
        return "redirect:/validation/v4/items/{itemId}";
    }

}

            /**
             * 최종적으로 validation 은 기능에 맞게 VO 를 분리, 어노테이션으로 처리하는것이 이상적이다.
             * ADD,EDIT 등 전혀 다른 수정 VALUE 들이 필요한 경우는 해당 값에 완벽히 매칭되는 VALUE 를 가진 VO 를 MODEL 로 해야한다.
             *
             * ERROR 발생 시 , BindingResult 에 에러가 담긴다. (로그로 확인 가능)
             *   ㄴ 애초에 ERROR 발생 시, 어떠한 로직도 실행 되어서는 안된다. 고로 .hasErrors 로 간단하게 처리한다.
             *
             * 각각 다른 VO 를 사용하기 때문에, 검증이 통과되면 기본 MODEL(ITEM) 에 값을 SET 해주어야 한다.
             *   ㄴ 성공 로직이라고 보면 된다.
             *   ㄴ 실제로 사용시, 다른 곳으로 빼거나 등등 개선할수 있을것 같다.
             * */

