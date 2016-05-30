package org.thiki.kanban.entry;

import com.jayway.restassured.http.ContentType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.thiki.kanban.TestBase;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertEquals;

/**
 * Created by xubt on 5/14/16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class EntriesControllerTest extends TestBase {

    @Test
    public void shouldReturn201WhenCreateEntrySuccessfully() {
        given().header("userId", "11222")
                .body("{\"title\":\"this is the entry title.\",\"boardId\":\"feeId\"}")
                .contentType(ContentType.JSON)
                .when()
                .post("/boards/feeId/entries")
                .then()
                .statusCode(201)
                .body("title", equalTo("this is the entry title."))
                .body("reporter", equalTo(11222))
                .body("creationTime", notNullValue())
                .body("_links.all.href", equalTo("http://localhost:8007/boards/feeId/entries"))
                .body("_links.tasks.href", equalTo("http://localhost:8007/entries/fooId/tasks"))
                .body("_links.self.href", equalTo("http://localhost:8007/boards/feeId/entries/fooId"));
    }

    @Test
    public void shouldReturnEntryWhenFindEntryById() {
        jdbcTemplate.execute("INSERT INTO  kb_entry (id,title,reporter,board_id) VALUES ('fooId','this is the first entry.',1,'feeId')");
        given().header("userId", "11222")
                .when()
                .get("/boards/feeId/entries/fooId")
                .then()
                .statusCode(200)
                .body("id", equalTo("fooId"))
                .body("title", equalTo("this is the first entry."))
                .body("_links.all.href", equalTo("http://localhost:8007/boards/feeId/entries"))
                .body("_links.self.href", equalTo("http://localhost:8007/boards/feeId/entries/fooId"));
    }

    @Test
    public void shouldUpdateSuccessfully() {
        jdbcTemplate.execute("INSERT INTO  kb_entry (id,title,reporter,board_id) VALUES ('fooId','this is the first entry.',1,'feeId')");
        given().header("userId", "11222")
                .contentType(ContentType.JSON)
                .body("{\"title\":\"newTitle\"}")
                .when()
                .put("/boards/feeId/entries/fooId")
                .then()
                .statusCode(200)
                .body("title", equalTo("newTitle"))
                .body("_links.all.href", equalTo("http://localhost:8007/boards/feeId/entries"))
                .body("_links.self.href", equalTo("http://localhost:8007/boards/feeId/entries/fooId"));
        assertEquals("newTitle", jdbcTemplate.queryForObject("select title from kb_entry where id='fooId'", String.class));
    }

    @Test
    public void shouldDeleteSuccessfullyWhenTheEntryIsExist() {
        jdbcTemplate.execute("INSERT INTO  kb_entry (id,title,reporter,board_id) VALUES ('fooId','this is the first entry.',1,'feeId')");
        given().header("userId", "11222")
                .when()
                .delete("/boards/feeId/entries/fooId")
                .then()
                .statusCode(200);
        assertEquals(1, jdbcTemplate.queryForList("select * FROM kb_entry WHERE  delete_status=1").size());
    }

    @Test
    public void shouldThrowResourceNotFoundExceptionWhenEntryToDeleteIsNotExist() throws Exception {
        given().header("userId", "11222")
                .when()
                .delete("/boards/feeId/entries/fooId")
                .then()
                .statusCode(404)
                .body("message", equalTo("entry[fooId] is not found."));
    }

    @Test
    public void shouldReturnAllEntriesSuccessfully() {
        jdbcTemplate.execute("INSERT INTO  kb_entry (id,title,reporter,board_id) VALUES ('fooId','this is the first entry.',1,'feeId')");
        given().header("userId", "11222")
                .when()
                .get("/boards/feeId/entries")
                .then()
                .statusCode(200)
                .body("[0].title", equalTo("this is the first entry."))
                .body("[0].reporter", equalTo(1))
                .body("[0].creationTime", notNullValue())
                .body("[0]._links.all.href", equalTo("http://localhost:8007/boards/feeId/entries"))
                .body("[0]._links.self.href", equalTo("http://localhost:8007/boards/feeId/entries/fooId"))
                .body("[0]._links.tasks.href", equalTo("http://localhost:8007/entries/fooId/tasks"));
    }
}
