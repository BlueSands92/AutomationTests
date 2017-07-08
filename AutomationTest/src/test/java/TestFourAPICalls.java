import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import static com.jayway.restassured.RestAssured.given;
import static org.cthul.matchers.CthulMatchers.matchesPattern;
import static org.hamcrest.Matchers.equalTo;

/**
 * Created by bluesands92 on 07/07/2017.
 */

/**
 * Test	4:	API	Calls
 1. Visit			https://reqres.in/ where	you	will	find	the	documentation	needed	to
 create	your	API	calls using	that	base	URL.
 2. Create	‘happy	path’	tests	for	each	of	the	CRUD	actions,	demonstrating	that	you
 have	asserted	against	one	or	more	things	of	value	which	demonstrate	success	or
 failure	of	the	test
 */
public class TestFourAPICalls {


    @BeforeSuite
    public void setup(){
        //set uri and path
        RestAssured.baseURI = "https://reqres.in/";
        RestAssured.basePath = "/api/";

    }
    //TEST USERS CRUDPAQ

    @Test()
    public void GetSingleUser(){

        given().
                header("Content-Type", ContentType.JSON).
        expect().
                statusCode(200).
                body("data.id", equalTo(2)).
                body("data.first_name", equalTo("lucille")).
                body("data.last_name", equalTo("bluth")).
        when().
                get("users/2");
    }

    @Test
    public void GetListUser(){

        given().
                header("Content-Type", ContentType.JSON).
                param("page", 2).
        expect().
                statusCode(200).
                body("page", equalTo("2")).
                body("data[1].id", equalTo(5)).
                body("data[1].first_name", equalTo("gob")).
                body("data[1].last_name", equalTo("bluth")).
         when().
                get("users");
    }

    @Test
    public void PostCreateUser(){

        given().
                header("Content-Type", ContentType.JSON).
                body("{\"name\": \"alex\",\"job\": \"tester\"}").
                expect().
                statusCode(201).
                body("name", equalTo("alex")).
                body("job", equalTo("tester")).
                when().
                post("users");
    }

    @Test
    public void PutUpdateUser(){

        given().
                header("Content-Type", ContentType.JSON).
                body("{\"name\": \"alex\",\"job\": \"tester\"}").
        expect().
                statusCode(200).
                body("name", equalTo("alex")).
                body("job", equalTo("tester")).
        when().
                put("users/265");
    }

    @Test
    public void PatchUpdateUser(){

        given().
                header("Content-Type", ContentType.JSON).
                body("{\"name\": \"alex\",\"job\": \"tester\"}").
        expect().
                statusCode(200).
                body("name", equalTo("alex")).
                body("job", equalTo("tester")).
        when().
                patch("users/187");
    }

    @Test
    public void DeleteUser(){

        given().
                header("Content-Type", ContentType.JSON).
        expect().
                statusCode(204).
        when().
                delete("users/187");

        given().
                header("Content-Type", ContentType.JSON).
        expect().
                statusCode(404).
        when().
                get("users/187");

    }

    @Test
    public void GetDelayListUser(){

        given().
                header("Content-Type", ContentType.JSON).
                param("delay", 3).
        expect().
                statusCode(200).
                body("data[1].id", equalTo(2)).
                body("data[1].first_name", equalTo("lucille")).
                body("data[1].last_name", equalTo("bluth")).
        when().
                get("users");
    }



    //Resources
    @Test
    public void GetListResources(){

        given().
                header("Content-Type", ContentType.JSON).
        expect().
                statusCode(200).
                body("page", equalTo(1)).
                body("data[1].id", equalTo(2)).
                body("data[1].name", equalTo("fuchsia rose")).
                body("data[1].year", equalTo(2001)).
                body("data[1].pantone_value", equalTo("17-2031")).
        when().
                get("unknown");
    }

    @Test
    public void GetDelayListResources(){

        given().
                header("Content-Type", ContentType.JSON).
                param("delay", 3).
        expect().
                statusCode(200).
                body("page", equalTo(1)).
                body("data[1].id", equalTo(2)).
                body("data[1].name", equalTo("fuchsia rose")).
                body("data[1].year", equalTo(2001)).
                body("data[1].pantone_value", equalTo("17-2031")).
        when().
                get("unknown");
    }

    @Test
    public void GetSingleResource(){

        given().
                header("Content-Type", ContentType.JSON).
        expect().
                statusCode(200).
                body("data.id", equalTo(2)).
                body("data.name", equalTo("fuchsia rose")).
                body("data.year", equalTo(2001)).
                body("data.pantone_value", equalTo("17-2031")).
        when().
                get("unknown/2");
    }

    @Test
    public void PostCreateResource(){

        given().
                header("Content-Type", ContentType.JSON).
                body("{\"name\": \"Alex Walker\", \"year\": 1992, \"pantone_value\": \"17-1234\"}").
        expect().
                statusCode(201).
                body("name", equalTo("Alex Walker")).
                body("year", equalTo(1992)).
                body("pantone_value", equalTo("17-1234")).
        when().
                post("users");
    }

    @Test
    public void PutUpdateResource(){

        given().
                header("Content-Type", ContentType.JSON).
                body("{\"name\": \"Alex Walker\", \"year\": 1992, \"pantone_value\": \"17-1234\"}").
        expect().
                statusCode(200).
                body("name", equalTo("Alex Walker")).
                body("year", equalTo(1992)).
                body("pantone_value", equalTo("17-1234")).
        when().
                put("users/265");
    }

    @Test
    public void PatchUpdateResource(){

        given().
                header("Content-Type", ContentType.JSON).
                body("{\"name\": \"Alex Walker\", \"year\": 1992, \"pantone_value\": \"17-1234\"}").
                expect().
        statusCode(200).
                body("name", equalTo("Alex Walker")).
                body("year", equalTo(1992)).
                body("pantone_value", equalTo("17-1234")).
        when().
                patch("resource/187");
    }

    @Test
    public void DeleteResource(){

        given().
                header("Content-Type", ContentType.JSON).
        expect().
                statusCode(204).
        when().
                delete("resource/187");

        given().
                header("Content-Type", ContentType.JSON).
        expect().
                statusCode(404).
        when().
                get("resource/187");

    }

    //register
    @Test
    public void PostRegister(){

        given().
                log().all().
                header("Content-Type", ContentType.JSON).
                body("{\"email\": \"sydney@fife\", \"password\": \"pistol\"}").
        expect().
                statusCode(201).
                body("token", matchesPattern("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$")).
        when().
                post("register");
    }

    //login
    @Test
    public void PostLogin(){

        given().
                log().all().
                header("Content-Type", ContentType.JSON).
                body("{\"email\": \"peter@klaven\", \"password\": \"cityslicka\"}").
        expect().
                statusCode(200).
                body("token", matchesPattern("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$")).
        when().
                post("login");
    }
}
