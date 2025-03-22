import edu.restaurant.app.dao.entity.Ingredient;
import edu.restaurant.app.dao.operations.IngredientCrudOperations;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IngredientTest {
    IngredientCrudOperations subject = new IngredientCrudOperations();

    @Test
    void oeuf_get_available_quantity() {
        Ingredient oeuf = subject.findById(1L);

        assertEquals(80.0, oeuf.getAvailableQuantity());
    }

    @Test
    void pain_get_available_quantity() {
        Ingredient pain = subject.findById(4L);

        assertEquals(30.0, pain.getAvailableQuantity());
    }

    @Test
    void oeuf_get_price() {
        Ingredient oeuf = subject.findById(1L);

        assertEquals(1000.0, oeuf.getActualPrice());
    }
}
