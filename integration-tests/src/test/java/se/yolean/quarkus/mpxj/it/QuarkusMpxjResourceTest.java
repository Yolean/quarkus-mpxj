package se.yolean.quarkus.mpxj.it;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class QuarkusMpxjResourceTest {

  @Test
  public void testHelloEndpoint() {
    given()
        .when().get("/quarkus-mpxj/example-project")
        .then()
        .statusCode(200)
        .body(is("Success"));
  }
}
