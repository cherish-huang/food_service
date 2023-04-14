import com.cherish.CartApp;
import com.cherish.dao.CartDao;
import com.cherish.entity.cart.CartItem;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {CartApp.class})
public class CartDaoTest {

    @Autowired
    private CartDao cartDao;

    @Test
    public void  addCartItem(){
        CartItem cartItem = CartItem.builder()
                        .buyerId(1L)
                        .itemId(1L)
                        .storeId(1L)
                        .id(11L)
                        .quantity(1)
                        .build();

        cartDao.addCartItem(cartItem);
    }
}
