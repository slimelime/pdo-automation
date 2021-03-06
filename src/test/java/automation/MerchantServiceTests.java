package automation;

import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import static automation.TestConfig.getWait;
import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalToIgnoringCase;

@Test(groups = "MerchantService")
public class MerchantServiceTests {
  private static Logger logger = LoggerFactory.getLogger(MerchantServiceTests.class);

  private String serviceUrl = TestConfig.getConfig().getProperty("MerchantServiceUrl");
  private boolean isDebug = Boolean.parseBoolean(TestConfig.getConfig().getProperty("DebugMode", "false"));

  public void merchantServiceSanityTest() {
    String id = createMerchant();
    logger.info("masterId=" + id);
    assertThat(getMerchantStatus(id), equalToIgnoringCase("MASTER_ID_ALLOCATED"));
    //on board
    onboardMerchant(id);
    // get merchant
    getWait()
        .withMessage("Failed to get GATEWAY_ACCOUNT_CREATED")
        .until(o -> getMerchantStatus(id).equalsIgnoreCase("GATEWAY_ACCOUNT_CREATED"));
  }

  public void getMerchantStatusByGatewayMid() {
    Response response = jsonRequest()
        .param("gatewayMerchantId", "37375cb8-c17d-4947-8c0e-d9ab086fa6e6")
        .when().get(serviceUrl);

     response.then().assertThat()
        .statusCode(200)
        .extract().jsonPath()
        .getString("status");
  }

  private String getMerchantStatus(String id) {
    Response response = jsonRequest()
        .when().get(serviceUrl + "/" + id);

    return response.then().assertThat()
        .statusCode(200)
        .extract().jsonPath()
        .getString("status");
  }

  private void onboardMerchant(String id) {

    Response response = jsonRequest()
        .when().post(serviceUrl + "/" + id + "/onboard");
    response.then().assertThat()
        .statusCode(200)
        .body(containsString("MID_ALLOCATED"));
  }

  private String createMerchant() {
    String body = "{\"companyName\":\"MYOB\","
        + "\"contactFirstName\":\"First\",\"contactLastName\":\"Last\","
        + "\"contactEmail\":\"email@email.com\",\"contactPhone\":\"7778889\",\"addressLine1\":\"address1\","
        + "\"addressLine2\":\"address2\",\"suburb\":\"suburb\",\"state\":\"VIC\",\"postcode\":\"3001\",\"countryCode\":\"AU\"}";

    Response response = jsonRequest()
        .body(body)
        .when().post(serviceUrl);

    return response.then()
        .assertThat()
          .statusCode(201)
          .body(containsString("MASTER_ID_ALLOCATED"))
        .extract().jsonPath()
        .getString("id");
  }

  private RequestSpecification jsonRequest() {
    return isDebug ? given().contentType("application/json")
        .filter(new RequestLoggingFilter())
        .filter(new ResponseLoggingFilter())
        : given().contentType("application/json");
  }
}
