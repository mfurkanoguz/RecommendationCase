import com.mfurkanoguz.eticaretapi.Application;
import com.mfurkanoguz.eticaretapi.controller.BestSellerController;
import com.mfurkanoguz.eticaretapi.controller.HistoryController;
import com.mfurkanoguz.eticaretapi.dbconnections.MongoDBConnection;
import com.mfurkanoguz.eticaretapi.model.User;
import com.mongodb.MongoSocketException;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashSet;
import java.util.Set;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class RecommendationApiTest {

    @Test
    public void userNotFound() throws Exception {
        HistoryController historyController=new HistoryController();
        ResponseEntity<String> responseEntity = historyController.deleteHistory("user-7", "product-7");
        MatcherAssert.assertThat(responseEntity.getStatusCodeValue(), Matchers.equalTo(404));
    }


    @Test
    public void connectMongoSuccess() throws Exception {
        MongoCollection<Document> connect = MongoDBConnection.MongoDBUtil
                .getConnect("localhost", 27017, "recommendationDB", "ProductView");
        MatcherAssert.assertThat(connect.find().first().get("product_id"), Matchers.equalTo("product-230"));
    }

    @Test
    public void getCategoryTesting() throws Exception {
        Set<String> products=new HashSet<>();
        products.add("product-5");
        BestSellerController bestSellerController=new BestSellerController();
        Set<String> productCategory = bestSellerController.getProductCategory(products);
        MatcherAssert.assertThat(productCategory.iterator().next(), Matchers.equalTo("category-1"));
    }




}
